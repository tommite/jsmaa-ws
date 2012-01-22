package fi.smaa.ws;


import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.decisionDeck.xmcda3.SMAA2ModelDocument;
import org.decisionDeck.xmcda3.SMAA2ResultsDocument;
import org.drugis.common.threading.ThreadHandler;

import fi.smaa.jsmaa.model.SMAAModel;
import fi.smaa.jsmaa.simulator.SMAA2Results;
import fi.smaa.jsmaa.simulator.SMAA2Simulation;
import fi.smaa.jsmaa.xml.InvalidModelException;
import fi.smaa.jsmaa.xml.XMCDA3Marshaller;

@WebService(serviceName="JSMAAService")
public class JSMAAServiceImpl implements JSMAAService{
	
	private static final int NR_ITERS = 10000;
	private static ThreadHandler handler = ThreadHandler.getInstance();

	@WebMethod(operationName="smaa2svc")
	synchronized public SMAA2ResultsDocument smaa2(
			@WebParam(name="model") SMAA2ModelDocument model) throws InvalidModelException {

		SMAAModel smaaModel = XMCDA3Marshaller.unmarshallModel(model);
		SMAA2Simulation simul = new SMAA2Simulation(smaaModel, NR_ITERS);

		handler.scheduleTask(simul.getTask());
		while(handler.getRunningThreads() > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}	
		}	
		SMAA2Results res = simul.getResults();
		return serializeSMAA2Results(res);
	}

	private SMAA2ResultsDocument serializeSMAA2Results(SMAA2Results res) {
		return null;
	}	
}
