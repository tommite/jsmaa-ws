package fi.smaa.ws;

import static org.junit.Assert.*;

import org.junit.Test;

import fi.smaa.jsmaa.model.Alternative;
import fi.smaa.jsmaa.model.ExactMeasurement;
import fi.smaa.jsmaa.model.OutrankingCriterion;
import fi.smaa.jsmaa.model.SMAATRIModel;

public class XMLBeanHelperTest {

	@Test
	public void testFromBeanToJSMAA() {
		SMAATRIModel m = new SMAATRIModel("model");
		Alternative a1 = new Alternative("a1");
		Alternative a2 = new Alternative("a2");
		Alternative cat1 = new Alternative("cat1");
		Alternative cat2 = new Alternative("caty2");
		OutrankingCriterion oc1 = new OutrankingCriterion("oc1", true, new ExactMeasurement(2.0), new ExactMeasurement(3.0));
		OutrankingCriterion oc2 = new OutrankingCriterion("oc2", true, new ExactMeasurement(2.0), new ExactMeasurement(3.0));
		
		m.addAlternative(a1);
		m.addAlternative(a2);
		m.addCategory(cat1);
		m.addCategory(cat2);
		m.addCriterion(oc1);
		m.addCriterion(oc2);
		
		fail();
	}
}
