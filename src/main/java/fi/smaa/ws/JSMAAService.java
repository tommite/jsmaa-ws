package fi.smaa.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;

import fi.smaa.jsmaa.model.SMAATRIModel;
import fi.smaa.jsmaa.simulator.SMAATRIResults;

@WebService(serviceName="JSMAAService")
public class JSMAAService {

	@WebMethod(operationName="solveSMAATRIModel")
	public SMAATRIResults solveModel(SMAATRIModel model) {
		return null;
	}
}
