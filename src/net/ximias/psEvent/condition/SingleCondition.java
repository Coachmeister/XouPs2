package net.ximias.psEvent.condition;

import net.ximias.fileParser.JsonSerializable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Receives an array of Condition data and a Condition to apply to the given data.
 */
public class SingleCondition extends JsonSerializable implements EventCondition {
	
	/*
	Need to know:
	
	What data to obtain (and how to do so)
	
	What condition to be applied to data set.
	
	
	
	Receiving data:
	Receive payload
	Process payload
	Return true or false
	 */
	ArrayList<ConditionData> data;
	Logger logger = Logger.getLogger(getClass().getName());
	
	Condition condition;
	
	public SingleCondition(ArrayList<ConditionData> data, Condition condition) {
		this.data = data;
		this.condition = condition;
	}
	public SingleCondition(Condition condition, ConditionData... data) {
		this.data = new ArrayList<>();
		this.data.addAll(Arrays.asList(data));
		this.condition = condition;
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
			JSONObject conData = conditionData.getJSONObject(i);
			
			if(conData.getString("source").equalsIgnoreCase("census")){
				dataSources[i] = new CensusData(conData);
			}else{
				dataSources[i] = new EventData(conData);
			}
		}
		new SingleCondition(condition, dataSources);
	}
	
	@Override
	public HashMap<String, String> toJson() {
		
		HashMap<String, String> h = new HashMap<>(10);
		h.put("condition", condition.toString());
		JSONArray dataArray = new JSONArray();
		for (ConditionData datum : data) {
			dataArray.put(datum.toJson());
		}
		h.put("conditionData", dataArray.toString());
		
		return h;
	}
	
	public boolean evaluate(JSONObject payload){
			for (int i = 0; i < data.size()-1; i++) {
				logger.finer("evaluating: "+data.get(i).get(payload)+" "+condition.name()+" "+data.get(i+1).get(payload));
				try{
					if (!condition.eval(data.get(i).get(payload),data.get(i+1).get(payload))) {
						logger.finer("Evaluated to false");
						return false;
					}
				}catch (NumberFormatException e){
					logger.severe("Letters can not be compared using "+condition.name()+
							"\ncomparison was: "+data.get(i).get(payload)+" "+condition.name()+" "+data.get(i+1).get(payload));
					return false;
				}
			}
			logger.finer("Evaluated to true");
			return true;
	}
	
	public static void main(String[] args) {
		ArrayList<ConditionData> data = new ArrayList<>();
		data.add(new EventData("a", ConditionDataSource.EVENT));
		data.add(new EventData("1", ConditionDataSource.CONSTANT));
		SingleCondition econ = new SingleCondition(data, Condition.GREATER_OR_EQUALS);
		System.out.println(econ.evaluate(new JSONObject("{\"a\":\"1\"}")));
	}
}
