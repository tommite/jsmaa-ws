package fi.smaa.ws;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.soap.SOAPException;

import org.decisionDeck.xmcda3.SMAA2ModelDocument;
import org.decisionDeck.xmcda3.SMAA2ResultsDocument;


@WebService(targetNamespace="http://smaa.fi/svc")
public interface JSMAAService {
	public SMAA2ResultsDocument smaa2(
			@WebParam(name="model") SMAA2ModelDocument model) throws SOAPException;
}
