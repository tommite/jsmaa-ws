package fi.smaa.jsmaa.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.ws.WebFault;

@SuppressWarnings("serial")
@WebFault(name="InvalidModel")
@XmlAccessorType( XmlAccessType.FIELD )
public class InvalidModelException extends Exception {
	
	public String reason;
	
	public InvalidModelException(String reason) {
		super(reason);
		this.reason = reason;
	}

}
