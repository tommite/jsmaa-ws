package fi.smaa.ws;


import javax.xml.soap.SOAPException;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.xmlbeans.XmlBeansDataBinding;
import org.decisionDeck.xmcda3.SMAA2ModelDocument;
import org.decisionDeck.xmcda3.SMAA2ResultsDocument;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fi.smaa.jsmaa.model.SMAAModel;
import fi.smaa.jsmaa.xml.InvalidModelException;
import fi.smaa.jsmaa.xml.XMCDA3Marshaller;
import fi.smaa.jsmaa.xml.XMCDA3MarshallerTest;

public class JSMAAServiceTest {
	
	private static JSMAAService clientService;
	private SMAAModel model;
	private SMAA2ModelDocument xmlModel;

	@BeforeClass
	public static void setUpSuite() throws InvalidModelException {
		JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
		sf.setAddress("local://smaasvc");
		sf.setDataBinding(new XmlBeansDataBinding());
		sf.setServiceClass(JSMAAService.class);
		sf.setServiceBean(new JSMAAServiceImpl());
		sf.create();
		
		
		JaxWsProxyFactoryBean cf = new JaxWsProxyFactoryBean();
		cf.setAddress("local://smaasvc");
		cf.setServiceClass(JSMAAService.class);
		cf.setDataBinding(new XmlBeansDataBinding());
		clientService = (JSMAAService) cf.create();
	}
	
	@Before
	public void setUp() throws InvalidModelException {
		model = createSMAA2Model();
		xmlModel = XMCDA3Marshaller.marshallModel(model);
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testService() throws SOAPException {
		SMAA2ResultsDocument ret = clientService.smaa2(xmlModel);
		//hard to test as the results are not really deserializable :/
		//System.out.println(ret);
	}
	
	private SMAAModel createSMAA2Model() {
		XMCDA3MarshallerTest t = new XMCDA3MarshallerTest();
		return t.createSMAA2Model();
	}
	
}