package net.ximias.psEventHandlers;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class EventCondition {
	
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
	
	Condition condition;
	
	public EventCondition(ArrayList<ConditionData> data, Condition condition) {
		this.data = data;
		this.condition = condition;
	}
	
	public boolean evaluate(JSONObject payload){
			for (int i = 0; i < data.size()-1; i++) {
				try{
					if (!condition.eval(data.get(i).get(payload),data.get(i+1).get(payload))) return false;
				}catch (NumberFormatException e){
					System.out.println("Letters can not be compared using "+condition.name());
					System.out.println("comparison was: "+data.get(i).get(payload)+" "+condition.name()+" "+data.get(i+1).get(payload));
					return false;
				}
			}
			return true;
	}
	
	public static void main(String[] args) {
		ArrayList<ConditionData> data = new ArrayList<>();
		data.add(new ConstantData("a",ConditionDataType.EVENT_VARIABLE));
		data.add(new ConstantData("1",ConditionDataType.CONSTANT_DATA));
		EventCondition econ = new EventCondition(data, Condition.GREATER_OR_EQUALS);
		System.out.println(econ.evaluate(new JSONObject("{\"a\":\"1\"}")));
	}
}
