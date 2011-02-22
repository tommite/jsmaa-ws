package fi.smaa.ws;

import javax.jws.WebParam;
import javax.jws.WebService;

import noNamespace.SMAATRIModelDocument;

@WebService
public interface JSMAAService {
	public String solveSMAATRIModel(
			@WebParam(name="model") SMAATRIModelDocument model);
}
