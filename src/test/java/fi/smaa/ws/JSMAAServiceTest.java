package fi.smaa.ws;


import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.xmlbeans.XmlBeansDataBinding;
import org.decisionDeck.xmcda3.SMAA2ModelDocument;
import org.decisionDeck.xmcda3.SMAA2ResultsDocument;
import org.junit.Before;
import org.junit.Test;

import fi.smaa.jsmaa.model.Alternative;
import fi.smaa.jsmaa.model.ExactMeasurement;
import fi.smaa.jsmaa.model.SMAAModel;
import fi.smaa.jsmaa.model.ScaleCriterion;
import fi.smaa.jsmaa.xml.InvalidModelException;
import fi.smaa.jsmaa.xml.XMCDA3Marshaller;

public class JSMAAServiceTest {
	
	private JSMAAService clientService;
	private SMAAModel model;
	private SMAA2ModelDocument xmlModel;

	@Before
	public void setUp() throws InvalidModelException {
		JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
		sf.setAddress("local://jsmaa");
		sf.setDataBinding(new XmlBeansDataBinding());
		sf.setServiceClass(JSMAAService.class);
		sf.setServiceBean(new JSMAAServiceImpl());
		sf.create();
		
		JaxWsProxyFactoryBean cf = new JaxWsProxyFactoryBean();
		cf.setAddress("local://jsmaa");
		cf.setServiceClass(JSMAAService.class);
		cf.setDataBinding(new XmlBeansDataBinding());
		clientService = (JSMAAService) cf.create();
		
		model = createSMAA2Model();
		xmlModel = XMCDA3Marshaller.marshallModel(model);
	}
	
	@Test
	public void testService() throws InvalidModelException {
		SMAA2ResultsDocument ret = clientService.smaa2(xmlModel);
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