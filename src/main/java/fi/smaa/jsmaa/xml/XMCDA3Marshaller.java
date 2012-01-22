package fi.smaa.jsmaa.xml;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.decisionDeck.xmcda3.AlternativeSetType;
import org.decisionDeck.xmcda3.AlternativeType;
import org.decisionDeck.xmcda3.AttributeSetType;
import org.decisionDeck.xmcda3.AttributeType;
import org.decisionDeck.xmcda3.CriterionSetType;
import org.decisionDeck.xmcda3.CriterionType;
import org.decisionDeck.xmcda3.DirectedCriterionType;
import org.decisionDeck.xmcda3.ExactMeasurementType;
import org.decisionDeck.xmcda3.IntervalType;
import org.decisionDeck.xmcda3.MeasurementType;
import org.decisionDeck.xmcda3.SMAA2ModelDocument;
import org.decisionDeck.xmcda3.SMAA2ModelDocument.SMAA2Model;
import org.decisionDeck.xmcda3.ValuedPairType;
import org.decisionDeck.xmcda3.ValuedRelationType;

import fi.smaa.jsmaa.model.Alternative;
import fi.smaa.jsmaa.model.Criterion;
import fi.smaa.jsmaa.model.ExactMeasurement;
import fi.smaa.jsmaa.model.Interval;
import fi.smaa.jsmaa.model.Measurement;
import fi.smaa.jsmaa.model.SMAAModel;
import fi.smaa.jsmaa.model.ScaleCriterion;

public class XMCDA3Marshaller {

	public static SMAAModel unmarshallModel(SMAA2ModelDocument doc) throws InvalidModelException {
		
		SMAAModel m = new SMAAModel("unmarshalled model");
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
			
			Alternative a = XMCDA3Marshaller.getAlternativeWithName(fromName, m.getAlternatives());
			Criterion c = XMCDA3Marshaller.getCriterionWithName(toName, m.getCriteria());
			Measurement meas = XMCDA3Marshaller.constructMeasurement(vp.getMeasurement());
			m.setMeasurement(c, a, meas);
		}
		
		return m;
	}

	private static Criterion getCriterionWithName(String name, List<Criterion> criteria) throws InvalidModelException {
		for (Criterion c : criteria) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		throw new InvalidModelException("No alternative with name " + name);		
	}

	private static Alternative getAlternativeWithName(String name, List<Alternative> alts) throws InvalidModelException {
		for (Alternative a : alts) {
			if (a.getName().equals(name)) {
				return a;
			}
		}
		throw new InvalidModelException("No alternative with name " + name);
	}

	private static Measurement constructMeasurement(MeasurementType measurement) throws InvalidModelException {
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
