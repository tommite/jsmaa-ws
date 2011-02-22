package fi.smaa.ws;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

public class WSMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Starting Server");
		JSMAAServiceImpl implementor = new JSMAAServiceImpl();
		JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
		svrFactory.setDataBinding(new org.apache.cxf.xmlbeans.XmlBeansDataBinding());
		svrFactory.setServiceClass(JSMAAServiceImpl.class);
		svrFactory.setAddress("http://localhost:9000/jsmaaService");
		svrFactory.setServiceBean(implementor);
		svrFactory.getInInterceptors().add(new LoggingInInterceptor());
		svrFactory.getOutInterceptors().add(new LoggingOutInterceptor());
		svrFactory.create();
	}

}
