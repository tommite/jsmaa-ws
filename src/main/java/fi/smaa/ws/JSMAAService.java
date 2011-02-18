package fi.smaa.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.drugis.common.threading.ThreadHandler;

import noNamespace.SMAATRIModelDocument;
import fi.smaa.jsmaa.model.SMAATRIModel;
import fi.smaa.jsmaa.model.xml.xmlbeans.NonserializableModelException;
import fi.smaa.jsmaa.model.xml.xmlbeans.XMLBeansSerializer;
import fi.smaa.jsmaa.simulator.SMAATRISimulation;

@WebService(serviceName="JSMAAService")
public class JSMAAService {
	
	protected static ThreadHandler handler = ThreadHandler.getInstance();

	@WebMethod(operationName="solveSMAATRI")
	synchronized public String solveSMAATRIModel(SMAATRIModelDocument model) throws NonserializableModelException {
		XMLBeansSerializer ser = new XMLBeansSerializer();
		
		SMAATRIModel smaaModel = ser.deSerialize(model);
		SMAATRISimulation simul = new SMAATRISimulation(smaaModel, 10000);
		
		handler.scheduleTask(simul.getTask());
		while(handler.getRunningThreads() > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		return simul.getResults().toString();
	}
}
