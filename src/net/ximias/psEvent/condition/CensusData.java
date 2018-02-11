package net.ximias.psEvent.condition;

import net.ximias.network.CensusConnection;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CensusData implements ConditionData {
	/*
	What to do:
	Returns a single data point from the census
	
	What to know:
	Name of query
	Name of data to obtain from query
	
	 
	 Lookup
	 Searchterm
	 get
	 resolve
	   */
	String lookup;
	String get;
	String resolve;
	HashMap<String, String> searchTerms;
	HashMap<String, String> eventSearchTerms;
	
	public CensusData(String lookup, String get, String resolve, HashMap<String, String> searchTerms, HashMap<String, String> eventSearchTerms) {
		this.lookup = lookup;
		this.get = get;
		this.resolve = resolve;
		this.searchTerms = searchTerms;
		this.eventSearchTerms = eventSearchTerms;
	}
	
	public CensusData(String lookup, String get, HashMap<String, String> searchTerms, HashMap<String, String> eventSearchTerms) {
		this.lookup = lookup;
		this.get = get;
		this.searchTerms = searchTerms;
		this.eventSearchTerms = eventSearchTerms;
	}
	
	public String get(JSONObject payload) {
		ArrayList<String> terms = new ArrayList<>();
		
		//Populate search terms
		searchTerms.forEach((key, value) -> terms.add(key + "=" + value));
		eventSearchTerms.forEach((key, value) -> terms.add(key + "=" + payload.getString(value)));
		
		//form queryString
		String queryString = lookup + "/?" + String.join("&", terms);
		JSONObject response = null;
		
		if (resolve == null) {
			queryString += "&c:show=" + get;
			response = CensusConnection.sendQuery(queryString);
			
			
			if (response != null) {
					response = unpackSingleResponseFromArray(response);
					
					if (!response.isNull(get)) {
						return response.getString(get);
					}
					
					return response.getString(get);
			}
		} else {
			queryString += "&c:resolve=" + resolve + "(" + get + ")";
			
			response = CensusConnection.sendQuery(queryString);
			
			if (response != null) {
				response = unpackSingleResponseFromArray(response);
				
				if (!response.isNull(get)) {
					return response.getString(get);
				}
			}
		}
		System.out.println("possible error in query. Target response was not produced:");
		System.out.println("Query: " + queryString);
		System.out.println("Response: " + response);
		response.keySet().forEach(System.out::println);
		return "";
	}
	
	private JSONObject unpackSingleResponseFromArray(JSONObject response) {
		if (response.keySet().stream().anyMatch(it -> it.contains("list"))) {
			response = response.getJSONArray(response.keySet().stream()
					.filter(it -> it.contains("list"))
					.findFirst().get()).getJSONObject(0);
		}
		return response;
	}
	
	public static void main(String[] args) {
		HashMap<String, String> params = new HashMap<>();
		params.put("character_id", "8287548916321388337");
		CensusData query = new CensusData("character", "faction_id", params, new HashMap<>(1));
		System.out.println(query.get(new JSONObject("{}")));
	}
}
