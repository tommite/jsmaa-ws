package fi.smaa.jsmaa.xml;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import org.decisionDeck.xmcda3.SMAA2ModelDocument;
import org.junit.Before;
import org.junit.Test;

import fi.smaa.jsmaa.model.Alternative;
import fi.smaa.jsmaa.model.Criterion;
import fi.smaa.jsmaa.model.ExactMeasurement;
import fi.smaa.jsmaa.model.Interval;
import fi.smaa.jsmaa.model.SMAAModel;
import fi.smaa.jsmaa.model.ScaleCriterion;

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

	@Before
	public void setUp() throws InvalidModelException {
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
		
		modelDoc = XMCDA3Marshaller.marshallModel(model);
		model2 = XMCDA3Marshaller.unmarshallModel(modelDoc);
	}
	
	@Test
	public void testAlternatives() {
		for (Alternative a : model.getAlternatives()) {
			String name = a.getName();
			boolean found = false;
			for (Alternative a2 : model2.getAlternatives()) {
				if (a2.getName().equals(name)) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
		assertEquals(model.getAlternatives().size(), model2.getAlternatives().size());
	}
	
	@Test
	public void testCriteria() {
		for (Criterion c : model.getCriteria()) {
			ScaleCriterion sc = (ScaleCriterion) c;
			String name = sc.getName();
			boolean found = false;
			for (Criterion a2 : model2.getCriteria()) {
				ScaleCriterion sc2 = (ScaleCriterion) a2;
				if (sc2.getName().equals(name) && sc2.getAscending() == sc.getAscending()) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
		assertEquals(model.getCriteria().size(), model2.getCriteria().size());		
	}
}
