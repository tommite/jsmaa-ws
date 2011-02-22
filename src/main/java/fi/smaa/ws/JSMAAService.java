package fi.smaa.ws;

import java.util.List;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebService;

import noNamespace.AlternativeType;
import noNamespace.SMAATRIModelDocument;

@WebService
public interface JSMAAService {
	public Map<AlternativeType, List<Double>> solveSMAATRIModel(
			@WebParam(name="model") SMAATRIModelDocument model);
}
