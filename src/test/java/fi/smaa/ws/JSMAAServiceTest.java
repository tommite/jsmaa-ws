package fi.smaa.ws;

import java.util.HashSet;
import java.util.Set;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
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
import org.junit.Before;
import org.junit.Test;

import fi.smaa.jsmaa.model.Alternative;
import fi.smaa.jsmaa.model.Criterion;
import fi.smaa.jsmaa.model.ExactMeasurement;
import fi.smaa.jsmaa.model.Interval;
import fi.smaa.jsmaa.model.Measurement;
import fi.smaa.jsmaa.model.SMAAModel;
import fi.smaa.jsmaa.model.ScaleCriterion;

public class JSMAAServiceTest {
	
	private JSMAAService clientService;
	private SMAAModel model;
	private SMAA2ModelDocument xmlModel;

	@Before
	public void setUp() throws InvalidModelException {
		JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
		sf.setAddress("local://jsmaa");
		sf.setDataBinding(new org.apache.cxf.xmlbeans.XmlBeansDataBinding());
		sf.setServiceClass(JSMAAService.class);
		sf.setServiceBean(new JSMAAServiceImpl());
		sf.create();
		
		JaxWsProxyFactoryBean cf = new JaxWsProxyFactoryBean();
		cf.setAddress("local://jsmaa");
		cf.setServiceClass(JSMAAService.class);
		cf.setDataBinding(new org.apache.cxf.xmlbeans.XmlBeansDataBinding());
		clientService = (JSMAAService) cf.create();
		
		model = createSMAA2Model();
		xmlModel = marshallModel(model);
	}
	
	@Test
	public void testService() {
		System.out.println(xmlModel);
	}
	
	private SMAA2ModelDocument marshallModel(SMAAModel model2) throws InvalidModelException {
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
				pair.setMeasurement(marshallMeasurement(meas));
			}
		}
		
		return mdoc;
	}

	private MeasurementType marshallMeasurement(Measurement meas) throws InvalidModelException {
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

	private SMAAModel createSMAA2Model() {
		SMAAModel model = new SMAAModel("model");
		Alternative a1 = new Alternative("a1");
		Alternative a2 = new Alternative("a2");
		ScaleCriterion c1 = new ScaleCriterion("c1", true);
		ScaleCriterion c2 = new ScaleCriterion("c2", true);		
		model.addAlternative(a1);
		model.addAlternative(a2);
		model.addCriterion(c1);
		model.addCriterion(c2);
		model.setMeasurement(c1, a1, new ExactMeasurement(3.0));
		model.setMeasurement(c2, a1, new ExactMeasurement(2.0));
		model.setMeasurement(c1, a2, new ExactMeasurement(2.0));
		model.setMeasurement(c2, a2, new ExactMeasurement(4.0));		
		return model;
	}
	
}