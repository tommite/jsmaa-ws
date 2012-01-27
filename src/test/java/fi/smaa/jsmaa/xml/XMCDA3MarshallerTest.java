package fi.smaa.jsmaa.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.decisionDeck.xmcda3.SMAA2ModelDocument;
import org.decisionDeck.xmcda3.SMAA2ResultsDocument;
import org.junit.Before;
import org.junit.Test;

import fi.smaa.jsmaa.model.Alternative;
import fi.smaa.jsmaa.model.Criterion;
import fi.smaa.jsmaa.model.ExactMeasurement;
import fi.smaa.jsmaa.model.Interval;
import fi.smaa.jsmaa.model.Measurement;
import fi.smaa.jsmaa.model.SMAAModel;
import fi.smaa.jsmaa.model.ScaleCriterion;
import fi.smaa.jsmaa.simulator.SMAA2Results;

public class XMCDA3MarshallerTest {

	
	private SMAAModel model;
	private Alternative a1;
	private Alternative a2;
	private ScaleCriterion c1;
	private ScaleCriterion c2;
	private ExactMeasurement m11;
	private ExactMeasurement m12;
	private Interval m21;
	private Interval m22;
	private SMAA2ModelDocument modelDoc;
	private SMAAModel model2;
	private SMAA2Results results;

	@Before
	public void setUp() throws InvalidModelException {
		model = createSMAA2Model();
		results = new SMAA2Results(model.getAlternatives(), model.getCriteria(), 1);
		results.update(new int[]{0, 1}, new double[]{0.2, 0.8});
		results.confidenceUpdate(new boolean[]{true, false});
				
		modelDoc = XMCDA3Marshaller.marshallModel(model);
		model2 = XMCDA3Marshaller.unmarshallModel(modelDoc);
	}
	
	public SMAAModel createSMAA2ModelWithoutWeights() {
		model = new SMAAModel("model");
		a1 = new Alternative("a1");
		a2 = new Alternative("a2");
		
		c1 = new ScaleCriterion("c1", true);
		c2 = new ScaleCriterion("c2", false);
		
		model.addAlternative(a1);
		model.addAlternative(a2);
		
		model.addCriterion(c1);
		model.addCriterion(c2);
		
		m11 = new ExactMeasurement(1.0);
		m12 = new ExactMeasurement(2.0);
		m21 = new Interval(1.0, 2.0);
		m22 = new Interval(1.0, 1.5);
		
		model.setMeasurement(c1, a1, m11);
		model.setMeasurement(c1, a2, m12);
		model.setMeasurement(c2, a1, m21);
		model.setMeasurement(c2, a2, m22);
		
		return model;
	}

	public SMAAModel createSMAA2Model() {
		return createSMAA2ModelWithoutWeights();
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testMarshallResults() {
		SMAA2ResultsDocument docres = XMCDA3Marshaller.marshallResults(results);
		// cannot really test these currently due to design flaws in JSMAA :/
		// System.out.println(docres);
	}
	
	@Test
	public void testAlternatives() {
		ArrayList<String> names = new ArrayList<String>();
		for (Alternative a : model.getAlternatives()) {
			names.add(a.getName());
		}
		for (Alternative a : model2.getAlternatives()) {
			assertTrue(names.contains(a.getName()));
			names.remove(a.getName());
		}
		assertEquals(0, names.size());
		assertEquals(model.getAlternatives().size(), model2.getAlternatives().size());
	}
	
	@Test
	public void testCriteria() throws InvalidModelException {
		for (Criterion c : model.getCriteria()) {
			ScaleCriterion sc = (ScaleCriterion) c;
			ScaleCriterion sc2 = (ScaleCriterion) XMCDA3Marshaller.findCriterion(sc.getName(), model2.getCriteria());
			assertEquals(sc.getName(), sc2.getName());
			assertEquals(sc.getAscending(), sc2.getAscending());
		}
		assertEquals(model.getCriteria().size(), model2.getCriteria().size());		
	}
	
	@Test
	public void testMeasurements() throws InvalidModelException {
		for (Criterion c : model.getCriteria()) {
			for (Alternative a : model.getAlternatives()) {
				Measurement m1meas = model.getMeasurement(c, a);
				Measurement m2meas = model2.getMeasurement(
						XMCDA3Marshaller.findCriterion(c.getName(), model2.getCriteria()),
						XMCDA3Marshaller.findAlternative(a.getName(), model2.getAlternatives()));
								
				assertEquals(m2meas, m1meas);
			}
		}
	}
}
