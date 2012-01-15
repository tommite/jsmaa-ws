package fi.smaa.ws;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.decisionDeck.xmcda3.SMAA2ModelDocument;
import org.decisionDeck.xmcda3.SMAA2ResultsDocument;


@WebService
public interface JSMAAService {
	public SMAA2ResultsDocument smaa2(
			@WebParam(name="model") SMAA2ModelDocument model);
}
