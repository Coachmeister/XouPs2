package net.ximias.psEvent.condition;

import net.ximias.fileParser.JsonSerializable;
import net.ximias.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Receives an array of Condition data and a Condition to apply to the given data.
 */
public class SingleCondition implements EventCondition, JsonSerializable {
	
	/*
	Need to know:
	
	What data to obtain (and how to do so)
	
	What condition to be applied to data set.
	
	
	
	Receiving data:
	Receive payload
	Process payload
	Return true or false
	 */
	private ConditionData[] data;
	private final Logger logger = Logger.getLogger(getClass().getName());
	private String name;
	private Condition condition;
	
	public SingleCondition(String name, Condition condition, ConditionData... data) {
		this.data = data;
		this.condition = condition;
		this.name = name;
	}
	
	public SingleCondition(JSONObject data) {
		String con = data.getString("condition");
		Condition condition = null;
		for (int i = 0; i < Condition.values().length; i++) {
			Condition c = Condition.values()[i];
			if (c.toString().equalsIgnoreCase(con)) condition = c;
		}
		if (condition == null) {
			throw new Error("Bad json");
		}
		
		JSONArray conditionData = data.getJSONArray("conditionData");
		ConditionData[] dataSources = new ConditionData[conditionData.length()];
		for (int i = 0; i < conditionData.length(); i++) {
			JSONObject conData = (JSONObject) conditionData.get(i);
			
			if (conData.getString("source").equalsIgnoreCase("census")) {
				dataSources[i] = new CensusData(conData);
			} else {
				dataSources[i] = new EventData(conData);
			}
		}
		this.name = data.getString("name");
		this.condition = condition;
		this.data = dataSources;
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject ret = new JSONObject();
		ret.put("name", name);
		ret.put("type", "Single");
		ret.put("condition", condition.toString());
		JSONArray dataArray = new JSONArray();
		for (ConditionData datum : data) {
			dataArray.put(datum.toJson());
		}
		ret.put("conditionData", dataArray);
		
		return ret;
	}
	
	public boolean evaluate(JSONObject payload) {
		for (int i = 0; i < data.length - 1; i++) {
			logger.effects().fine("evaluating: " + data[i].get(payload) + " " + condition.name() + " " + data[i + 1].get(payload));
			try {
				if (!condition.eval(data[i].get(payload), data[i + 1].get(payload))) {
					logger.effects().fine("Evaluated to false");
					return false;
				}
			} catch (NumberFormatException e) {
				logger.effects().severe("Letters can not be compared using " + condition.name() +
				                        "\ncomparison was: " + data[i].get(payload) + " " + condition.name() + " " + data[i + 1].get(payload));
				return false;
			}
		}
		logger.effects().fine("Evaluated to true");
		return true;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	/*public static void main(String[] args) {
		ArrayList<ConditionData> data = new ArrayList<>();
		data.add(new EventData("a", ConditionDataSource.EVENT));
		data.add(new EventData("1", ConditionDataSource.CONSTANT));
		SingleCondition econ = new SingleCondition(data, Condition.GREATER_OR_EQUALS);
		System.out.println(econ.evaluate(new JSONObject("{\"a\":\"1\"}")));
	}*/
}
