package fi.smaa.jsmaa.xml;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.decisionDeck.xmcda3.AlternativeSetType;
import org.decisionDeck.xmcda3.AlternativeType;
import org.decisionDeck.xmcda3.AttributeSetType;
import org.decisionDeck.xmcda3.AttributeType;
import org.decisionDeck.xmcda3.CriterionSetType;
import org.decisionDeck.xmcda3.CriterionType;
import org.decisionDeck.xmcda3.DirectedCriterionType;
import org.decisionDeck.xmcda3.ExactMeasurementType;
import org.decisionDeck.xmcda3.ExactValuedEntitySetType;
import org.decisionDeck.xmcda3.ExactValuedEntityType;
import org.decisionDeck.xmcda3.ExactValuedPairType;
import org.decisionDeck.xmcda3.ExactValuedRelationType;
import org.decisionDeck.xmcda3.IntervalType;
import org.decisionDeck.xmcda3.KeyedEntityReference;
import org.decisionDeck.xmcda3.MeasurementType;
import org.decisionDeck.xmcda3.SMAA2ModelDocument;
import org.decisionDeck.xmcda3.SMAA2ModelDocument.SMAA2Model;
import org.decisionDeck.xmcda3.SMAA2ResultsDocument;
import org.decisionDeck.xmcda3.ValuedEntitySetType;
import org.decisionDeck.xmcda3.ValuedEntityType;
import org.decisionDeck.xmcda3.ValuedPairType;
import org.decisionDeck.xmcda3.ValuedRelationType;

import fi.smaa.jsmaa.model.Alternative;
import fi.smaa.jsmaa.model.CardinalMeasurement;
import fi.smaa.jsmaa.model.CardinalPreferenceInformation;
import fi.smaa.jsmaa.model.Criterion;
import fi.smaa.jsmaa.model.ExactMeasurement;
import fi.smaa.jsmaa.model.Interval;
import fi.smaa.jsmaa.model.Measurement;
import fi.smaa.jsmaa.model.OrdinalPreferenceInformation;
import fi.smaa.jsmaa.model.SMAAModel;
import fi.smaa.jsmaa.model.ScaleCriterion;
import fi.smaa.jsmaa.simulator.SMAA2Results;

public class XMCDA3Marshaller {
	
	public static SMAA2ResultsDocument marshallResults(SMAA2Results results) {
		SMAA2ResultsDocument doc = SMAA2ResultsDocument.Factory.newInstance();
		org.decisionDeck.xmcda3.SMAA2ResultsDocument.SMAA2Results docres = doc.addNewSMAA2Results();
		addRankAcceptabilityIndices(docres.addNewRankAcceptabilityIndices(), results.getRankAcceptabilities());
		addConfidenceFactors(docres.addNewConfidenceFactors(), results.getConfidenceFactors());
		addCentralWeights(docres.addNewCentralWeightVectors(), results.getCentralWeightVectors());
		return doc;
	}

	private static void addCentralWeights(ExactValuedRelationType doccw,
			Map<Alternative, Map<Criterion, Double>> cws) {
		for (Map.Entry<Alternative, Map<Criterion, Double>> e : cws.entrySet()) {
			String altName = e.getKey().getName();
			for (Map.Entry<Criterion, Double> e2 : e.getValue().entrySet()) {
				ExactValuedPairType vp = doccw.addNewValuedPair();
				KeyedEntityReference from = vp.addNewFrom();
				from.setRef(altName);
				KeyedEntityReference to = vp.addNewTo();
				to.setRef(e2.getKey().getName());
				ExactMeasurementType meas = vp.addNewMeasurement();
				meas.setValue(e2.getValue());
			}
		}
	}

	private static void addConfidenceFactors(ExactValuedEntitySetType cfdoc,
			Map<Alternative, Double> confidenceFactors) {
		for (Map.Entry<Alternative, Double> e : confidenceFactors.entrySet()) {
			ExactValuedEntityType p = cfdoc.addNewEntry();
			KeyedEntityReference ent = p.addNewEntity();
			ent.setRef(e.getKey().getName());
			ExactMeasurementType meas = p.addNewMeasurement();
			meas.setValue(e.getValue());
		}
	}

	private static void addRankAcceptabilityIndices(ExactValuedRelationType docrel,
			Map<Alternative, List<Double>> indices) {
		for (Map.Entry<Alternative, List<Double>> e : indices.entrySet()) {
			int index = 1;
			for (Double d : e.getValue()) {
				ExactValuedPairType vp = docrel.addNewValuedPair();
				KeyedEntityReference from = KeyedEntityReference.Factory.newInstance();
				from.setRef(e.getKey().getName());
				KeyedEntityReference to = KeyedEntityReference.Factory.newInstance();
				to.setRef("r"+index);
				vp.setFrom(from);
				vp.setTo(to);
				ExactMeasurementType meas = ExactMeasurementType.Factory.newInstance();
				meas.setValue(d);
				vp.setMeasurement(meas);
				index++;
			}
		}
	}

	public static SMAAModel unmarshallModel(SMAA2ModelDocument doc) throws InvalidModelException {

		SMAAModel m = new SMAAModel("Unmarshalled model");
		SMAA2ModelDocument.SMAA2Model docm = doc.getSMAA2Model();
		AlternativeSetType aset = docm.getAlternativeSet();
	
		for (AlternativeType at : aset.getAlternativeArray()) {
			String key = at.getKey();
			Alternative a = new Alternative(key);
			m.addAlternative(a);
		}
		
		CriterionSetType cset = docm.getCriterionSet();
		
		for (CriterionType ct : cset.getCriterionArray()) {
			if (ct instanceof DirectedCriterionType) {
				DirectedCriterionType dc = (DirectedCriterionType) ct;
				String key = dc.getAttribute().getRef(); // use attribute names instead of criterion names
				ScaleCriterion c = new ScaleCriterion(key, true);
				if (dc.getPreferenceDirection().equals(DirectedCriterionType.PreferenceDirection.DESCENDING)) {
					c.setAscending(false);
				}
				m.addCriterion(c);
			} else {
				throw new InvalidModelException("Unsupported criterion type:" + ct.getClass().getCanonicalName());
			}
		}
		
		ValuedRelationType perf = docm.getPerformanceTable();
		for (ValuedPairType vp : perf.getValuedPairArray()) {
			String fromName = vp.getFrom().getRef();
			String toName = vp.getTo().getRef();
						
			Alternative a = findAlternative(fromName, m.getAlternatives());
			Criterion c = findCriterion(toName, m.getCriteria());
			Measurement meas = XMCDA3Marshaller.constructMeasurement(vp.getMeasurement());
			m.setMeasurement(c, a, meas);
		}
		
		ValuedEntitySetType w = docm.getWeights();
		if (w != null) {
			ValuedEntityType[] ws = w.getEntryArray();
			CardinalPreferenceInformation pref = new CardinalPreferenceInformation(m.getCriteria());
			for (ValuedEntityType v : ws) {
				Criterion c = findCriterion(v.getEntity().getRef(), m.getCriteria());
				pref.setMeasurement(c, constructMeasurement(v.getMeasurement()));
			}
			m.setPreferenceInformation(pref);
		}
		
		return m;
	}

	public static Criterion findCriterion(String toName, List<Criterion> criteria) throws InvalidModelException {
		for (Criterion c : criteria) {
			if (c.getName().equals(toName)) {
				return c;
			}
		}
		throw new InvalidModelException("no criterion with name " + toName);
	}

	public static Alternative findAlternative(String fromName, List<Alternative> alternatives) throws InvalidModelException {
		for (Alternative a : alternatives) {
			if (a.getName().equals(fromName)) {
				return a;
			}
		}
		throw new InvalidModelException("no alternative with name "+ fromName);
	}

	private static CardinalMeasurement constructMeasurement(MeasurementType measurement) throws InvalidModelException {
		if (measurement instanceof ExactMeasurementType) {
			ExactMeasurementType t = (ExactMeasurementType) measurement;
			ExactMeasurement m = new ExactMeasurement(t.getValue());
			return m;
		} else if (measurement instanceof IntervalType) {
			IntervalType it = (IntervalType) measurement;
			Interval m = new Interval(it.getBegin(), it.getEnd());
			return m;
		}
		throw new InvalidModelException("Unknown measurement type: " + measurement.getClass().getCanonicalName());
	}

	public static SMAA2ModelDocument marshallModel(SMAAModel model2) throws InvalidModelException {
		SMAA2ModelDocument mdoc = SMAA2ModelDocument.Factory.newInstance();
		SMAA2Model m = mdoc.addNewSMAA2Model();
		AlternativeSetType aset = m.addNewAlternativeSet();
		Set<String> tempSet = new HashSet<String>();
		for (Alternative a : model2.getAlternatives()) {
			AlternativeType da = aset.addNewAlternative();
			da.setKey(a.getName());
			tempSet.add(a.getName());
		}
		if (tempSet.size() != model2.getAlternatives().size()) {
			throw new InvalidModelException("Multiple alternatives with same name");
		}
		tempSet.clear();
		AttributeSetType atset = m.addNewAttributeSet();
		CriterionSetType cset = m.addNewCriterionSet();
		int index = 0;
		CriterionType[] critArr = new CriterionType[model2.getCriteria().size()];
		for (Criterion c : model2.getCriteria()) {
			ScaleCriterion sc = (ScaleCriterion) c;
			AttributeType atype = atset.addNewAttribute();
			DirectedCriterionType ctype = DirectedCriterionType.Factory.newInstance();
			critArr[index] = ctype;
			ctype.setKey("criterion["+(index+1)+"]");
			atype.setKey(sc.getName());
			ctype.setPreferenceDirection(sc.getAscending() ? DirectedCriterionType.PreferenceDirection.ASCENDING :
					DirectedCriterionType.PreferenceDirection.DESCENDING);
			ctype.addNewAttribute().setRef(atype.getKey());
			index++;
			tempSet.add(sc.getName());
		}
		if (tempSet.size() != model2.getCriteria().size()) {
			throw new InvalidModelException("Multiple criteria with same name");
		}
		cset.setCriterionArray(critArr);
		tempSet.clear();
		
		ValuedRelationType perf = m.addNewPerformanceTable();
		for (Alternative a : model2.getAlternatives()) {
			for (Criterion c : model2.getCriteria()) {
				Measurement meas = model2.getImpactMatrix().getMeasurement(c, a);
				ValuedPairType pair = perf.addNewValuedPair();
				pair.addNewFrom().setRef(a.getName());
				pair.addNewTo().setRef(c.getName());
				pair.setMeasurement(XMCDA3Marshaller.marshallMeasurement(meas));
			}
		}
		
		if (model2.getPreferenceInformation() instanceof CardinalPreferenceInformation) {
			CardinalPreferenceInformation p = (CardinalPreferenceInformation) model2.getPreferenceInformation();
			ValuedEntitySetType weights = m.addNewWeights();
			for (Criterion c : model2.getCriteria()) {
				ValuedEntityType e = weights.addNewEntry();
				KeyedEntityReference from = KeyedEntityReference.Factory.newInstance();
				from.setRef(c.getName());
				e.setEntity(from);
				CardinalMeasurement meas = p.getMeasurement(c);
				e.setMeasurement(marshallMeasurement(meas));
			}
		} else if (model2.getPreferenceInformation() instanceof OrdinalPreferenceInformation) {
		 	throw new InvalidModelException("Invalid preference information: cannot marshall ordinal preferences");
		}
		return mdoc;
	}

	private static MeasurementType marshallMeasurement(Measurement meas) throws InvalidModelException {
		if (meas instanceof ExactMeasurement) {
			ExactMeasurementType type = ExactMeasurementType.Factory.newInstance();
			type.setValue(((ExactMeasurement) meas).getValue());
			return type;
		} else if (meas instanceof Interval) {
			Interval im = (Interval) meas;
			IntervalType type = IntervalType.Factory.newInstance();
			type.setBegin(im.getStart());
			type.setEnd(im.getEnd());
			return type;
		}
		throw new InvalidModelException("Unknown measurement type: " + meas.getClass().getCanonicalName());
	}

}
