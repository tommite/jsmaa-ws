package fi.smaa.ws;

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.decisionDeck.xmcda3.AlternativeSetType;
import org.decisionDeck.xmcda3.AlternativeType;
import org.decisionDeck.xmcda3.CriterionType;
import org.decisionDeck.xmcda3.SMAA2ModelDocument;
import org.decisionDeck.xmcda3.SMAA2ResultsDocument;
import org.decisionDeck.xmcda3.UtilityCriterionSetType;
import org.decisionDeck.xmcda3.UtilityCriterionType;
import org.drugis.common.threading.ThreadHandler;

import fi.smaa.jsmaa.model.Alternative;
import fi.smaa.jsmaa.model.Criterion;
import fi.smaa.jsmaa.model.SMAAModel;
import fi.smaa.jsmaa.model.SMAATRIModel;
import fi.smaa.jsmaa.model.ScaleCriterion;
import fi.smaa.jsmaa.simulator.SMAA2Results;
import fi.smaa.jsmaa.simulator.SMAA2Simulation;
import fi.smaa.jsmaa.simulator.SMAATRIResults;
import fi.smaa.jsmaa.simulator.SMAATRISimulation;

@WebService(serviceName="JSMAAService")
public class JSMAAServiceImpl implements JSMAAService{
	
	private static ThreadHandler handler = ThreadHandler.getInstance();

	@WebMethod(operationName="smaa2svc")
	synchronized public SMAA2ResultsDocument smaa2(
			@WebParam(name="model") SMAA2ModelDocument model) {

		SMAAModel smaaModel = deserializeSMAA2Model(model);
		SMAA2Simulation simul = new SMAA2Simulation(smaaModel, 10000);

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

	private SMAAModel deserializeSMAA2Model(SMAA2ModelDocument doc) {
		Map<String, Alternative> altMap = new HashMap<String, Alternative>();
		Map<String, Criterion> critMap = new HashMap<String, Criterion>();
		
		SMAATRIModel m = new SMAATRIModel("deserialized model");
		SMAA2ModelDocument.SMAA2Model docm = doc.getSMAA2Model();
		AlternativeSetType aset = docm.getAlternativeSet();
	
		for (AlternativeType at : aset.getAlternativeArray()) {
			String key = at.getKey();
			Alternative a = new Alternative(key);
			m.addAlternative(a);
			altMap.put(key, a);
		}
		
		UtilityCriterionSetType cset = docm.getCriterionSet();
		
		for (UtilityCriterionType ct : cset.getCriterionArray()) {
			String key = ct.getKey();
			ScaleCriterion c = new ScaleCriterion(key, ascending)
		}
		
		return m;
	}
}
