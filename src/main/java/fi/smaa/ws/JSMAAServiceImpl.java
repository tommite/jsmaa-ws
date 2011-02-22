package fi.smaa.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.drugis.common.threading.ThreadHandler;

import noNamespace.AlternativeType;
import noNamespace.SMAATRIModelDocument;
import fi.smaa.jsmaa.model.Alternative;
import fi.smaa.jsmaa.model.SMAATRIModel;
import fi.smaa.jsmaa.model.xml.xmlbeans.NonserializableModelException;
import fi.smaa.jsmaa.model.xml.xmlbeans.XMLBeansSerializer;
import fi.smaa.jsmaa.simulator.SMAATRIResults;
import fi.smaa.jsmaa.simulator.SMAATRISimulation;

@WebService(serviceName="JSMAAService")
public class JSMAAServiceImpl implements JSMAAService{
	
	protected static ThreadHandler handler = ThreadHandler.getInstance();

	@WebMethod(operationName="solveSMAATRI")
	synchronized public Map<AlternativeType, List<Double>> solveSMAATRIModel(
			@WebParam(name="model")SMAATRIModelDocument model
			) {
		XMLBeansSerializer ser = new XMLBeansSerializer();

		try {
			SMAATRIModel smaaModel = ser.deSerialize(model);
			SMAATRISimulation simul = new SMAATRISimulation(smaaModel, 10000);

			handler.scheduleTask(simul.getTask());
			while(handler.getRunningThreads() > 0) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			SMAATRIResults res = simul.getResults();
			return convertResults(res);
		} catch (NonserializableModelException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Map<AlternativeType, List<Double>> convertResults(SMAATRIResults res) {
		Map<AlternativeType, List<Double>> ret = new HashMap<AlternativeType, List<Double>>();
		for (Alternative a : res.getAlternatives()) {
			AlternativeType at = AlternativeType.Factory.newInstance();
			at.setName(a.getName());
			List<Double> vals = new ArrayList<Double>();
			for (int i=0;i<res.getCategories().size();i++) {
				vals.add(res.getCategoryAcceptabilities().get(a).get(i));
			}
			ret.put(at, vals);
		}
		return ret;
	}
}
