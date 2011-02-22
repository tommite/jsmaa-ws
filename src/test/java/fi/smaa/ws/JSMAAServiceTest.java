package fi.smaa.ws;

import noNamespace.SMAATRIModelDocument;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.junit.Before;
import org.junit.Test;

import fi.smaa.jsmaa.model.Alternative;
import fi.smaa.jsmaa.model.ExactMeasurement;
import fi.smaa.jsmaa.model.OutrankingCriterion;
import fi.smaa.jsmaa.model.SMAATRIModel;
import fi.smaa.jsmaa.model.xml.xmlbeans.NonserializableModelException;
import fi.smaa.jsmaa.model.xml.xmlbeans.XMLBeansSerializer;

public class JSMAAServiceTest {
	
	private JSMAAService clientService;
	private SMAATRIModel model;

	@Before
	public void setUp() {
		
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
		
		model = createTRIModel();
	}
	
	private SMAATRIModel createTRIModel() {
		SMAATRIModel model = new SMAATRIModel("model");
		Alternative a1 = new Alternative("a1");
		OutrankingCriterion c1 = new OutrankingCriterion("c1", 
				true, 
				new ExactMeasurement(1.0), 
				new ExactMeasurement(2.0));
		OutrankingCriterion c2 = new OutrankingCriterion("c2", 
				false, 
				new ExactMeasurement(0.0), 
				new ExactMeasurement(3.0));
		Alternative cat1 = new Alternative("cat1");
		Alternative cat2 = new Alternative("cat2");
		model.addAlternative(a1);
		model.addCategory(cat1);
		model.addCategory(cat2);
		model.addCriterion(c1);
		model.addCriterion(c2);
		return model;
	}

	@Test
	public void testSolveModel() throws NonserializableModelException {
		XMLBeansSerializer ser = new XMLBeansSerializer();
		SMAATRIModelDocument doc = ser.docFromModel(model);
		System.out.println(clientService.solveSMAATRIModel(doc));
	}
	
}