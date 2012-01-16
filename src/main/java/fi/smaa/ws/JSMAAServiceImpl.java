package fi.smaa.ws;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.decisionDeck.xmcda3.AlternativeSetType;
import org.decisionDeck.xmcda3.AlternativeType;
import org.decisionDeck.xmcda3.CriterionSetType;
import org.decisionDeck.xmcda3.CriterionType;
import org.decisionDeck.xmcda3.DirectedCriterionType;
import org.decisionDeck.xmcda3.ExactMeasurementType;
import org.decisionDeck.xmcda3.IntervalType;
import org.decisionDeck.xmcda3.MeasurementType;
import org.decisionDeck.xmcda3.SMAA2ModelDocument;
import org.decisionDeck.xmcda3.SMAA2ResultsDocument;
import org.decisionDeck.xmcda3.ValuedPairType;
import org.decisionDeck.xmcda3.ValuedRelationType;
import org.drugis.common.threading.ThreadHandler;

import fi.smaa.jsmaa.model.Alternative;
import fi.smaa.jsmaa.model.Criterion;
import fi.smaa.jsmaa.model.ExactMeasurement;
import fi.smaa.jsmaa.model.Interval;
import fi.smaa.jsmaa.model.Measurement;
import fi.smaa.jsmaa.model.SMAAModel;
import fi.smaa.jsmaa.model.ScaleCriterion;
import fi.smaa.jsmaa.simulator.SMAA2Results;
import fi.smaa.jsmaa.simulator.SMAA2Simulation;

@WebService(serviceName="JSMAAService")
public class JSMAAServiceImpl implements JSMAAService{
	
	private static final int NR_ITERS = 10000;
	private static ThreadHandler handler = ThreadHandler.getInstance();

	@WebMethod(operationName="smaa2svc")
	synchronized public SMAA2ResultsDocument smaa2(
			@WebParam(name="model") SMAA2ModelDocument model) throws InvalidModelException {

		SMAAModel smaaModel = deserializeSMAA2Model(model);
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

	private SMAAModel deserializeSMAA2Model(SMAA2ModelDocument doc) throws InvalidModelException {
		
		SMAAModel m = new SMAAModel("deserialized model");
		SMAA2ModelDocument.SMAA2Model docm = doc.getSMAA2Model();
		AlternativeSetType aset = docm.getAlternativeSet();
	
		for (AlternativeType at : aset.getAlternativeArray()) {
			String key = at.getKey();
			Alternative a = new Alternative(key);
			m.addAlternative(a);
		}
		
		CriterionSetType cset = docm.getCriterionSet();
		
		for (CriterionType ct : cset.getCriterionArray()) {
			if (ct instanceof DirectedCriterionType) {
				DirectedCriterionType dc = (DirectedCriterionType) ct;
				String key = dc.getAttribute().getRef(); // use attribute names instead of criterion names
				ScaleCriterion c = new ScaleCriterion(key, true);
				if (dc.getPreferenceDirection().equals(DirectedCriterionType.PreferenceDirection.DESCENDING)) {
					c.setAscending(false);
				}
				m.addCriterion(c);
			} else {
				throw new InvalidModelException("Unsupported criterion type:" + ct.getClass().getCanonicalName());
			}
		}
		
		ValuedRelationType perf = docm.getPerformanceTable();
		for (ValuedPairType vp : perf.getValuedPairArray()) {
			String fromName = vp.getFrom().getRef();
			String toName = vp.getTo().getRef();
			
			Alternative a = getAlternativeWithName(fromName, m.getAlternatives());
			Criterion c = getCriterionWithName(toName, m.getCriteria());
			Measurement meas = constructMeasurement(vp.getMeasurement());
			m.setMeasurement(c, a, meas);
		}
		
		return m;
	}

	private Measurement constructMeasurement(MeasurementType measurement) throws InvalidModelException {
		if (measurement instanceof ExactMeasurementType) {
			ExactMeasurementType t = (ExactMeasurementType) measurement;
			ExactMeasurement m = new ExactMeasurement(t.getValue());
			return m;
		} else if (measurement instanceof IntervalType) {
			IntervalType it = (IntervalType) measurement;
			Interval m = new Interval(it.getBegin(), it.getEnd());
			return m;
		}
		throw new InvalidModelException("Unknown measurement type: " + measurement.getClass().getCanonicalName());
	}

	private Criterion getCriterionWithName(String name, List<Criterion> criteria) throws InvalidModelException {
		for (Criterion c : criteria) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		throw new InvalidModelException("No alternative with name " + name);		
	}

	private Alternative getAlternativeWithName(String name, List<Alternative> alts) throws InvalidModelException {
		for (Alternative a : alts) {
			if (a.getName().equals(name)) {
				return a;
			}
		}
		throw new InvalidModelException("No alternative with name " + name);
	}	
}
