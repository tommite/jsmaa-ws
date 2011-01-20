package fi.smaa.ws;

import noNamespace.SMAATRIModelDocument;
import fi.smaa.jsmaa.model.SMAATRIModel;

public class XMLBeanHelper {

	public XMLBeanHelper() {
		
	}
	
	 public SMAATRIModel fromBeanToJSMAA(SMAATRIModelDocument doc) {
		 SMAATRIModelDocument.SMAATRIModel x = doc.getSMAATRIModel();
	 }
	 
	 public SMAATRIModelDocument fromJSMAAToBean(SMAATRIModel model) {
		 return null;
	 }
 
}
