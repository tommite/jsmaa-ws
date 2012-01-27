package fi.smaa.ws;


import java.util.ArrayList;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.decisionDeck.xmcda3.SMAA2ModelDocument;
import org.decisionDeck.xmcda3.SMAA2ResultsDocument;
import org.drugis.common.threading.Task;
import org.drugis.common.threading.ThreadHandler;

import fi.smaa.jsmaa.model.SMAAModel;
import fi.smaa.jsmaa.simulator.SMAA2Simulation;
import fi.smaa.jsmaa.xml.InvalidModelException;
import fi.smaa.jsmaa.xml.XMCDA3Marshaller;

@WebService(serviceName="JSMAAService", targetNamespace="http://smaa.fi/svc")
public class JSMAAServiceImpl implements JSMAAService {

	private static final int NR_ITERS = 10000;
	private static ThreadHandler handler = ThreadHandler.getInstance();

	@WebMethod(operationName="smaa2svc")
	public SMAA2ResultsDocument smaa2(
			@WebParam(name="model") SMAA2ModelDocument model) throws SOAPException {
		try {
			validateInput(model);

			SMAAModel smaaModel = XMCDA3Marshaller.unmarshallModel(model);
			SMAA2Simulation simul = new SMAA2Simulation(smaaModel, NR_ITERS);

			Task task = simul.getTask();
			handler.scheduleTask(task);
			while(!task.isFinished() && !task.isFailed() && !task.isAborted()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			return XMCDA3Marshaller.marshallResults(simul.getResults());
		} catch (InvalidModelException e) {
			SOAPFactory fac = SOAPFactory.newInstance();
			SOAPFaultException sex = new SOAPFaultException(fac.createFault("Invalid model: " + e.getMessage(), QName.valueOf("1")));
			throw sex;
		}

	}

	private void validateInput(SMAA2ModelDocument model) throws InvalidModelException {
		System.out.println(model);
		ArrayList<XmlError> validationErrors = new ArrayList<XmlError>();
		XmlOptions opt = new XmlOptions();
		opt.setErrorListener(validationErrors);
		boolean valid = model.validate();
		if (!valid) {
			String s = "";
			for (XmlError x : validationErrors) {
				s += x.getMessage() + " ";
			}
			s = s.trim();
			throw new InvalidModelException(s);
		}
	}
}
