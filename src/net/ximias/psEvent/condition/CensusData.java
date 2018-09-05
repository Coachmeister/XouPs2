package net.ximias.psEvent.condition;

import net.ximias.network.CensusConnection;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.ximias.logging.Logger;


public class CensusData extends ConditionData {
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
	private String lookup;
	private String get;
	private String resolve;
	private HashMap<String, String> searchTerms;
	private HashMap<String, String> eventSearchTerms;
	private final Logger logger = Logger.getLogger(getClass().getName());
	/**
	 * Example:
	 * HashMap<String, String> params = new HashMap<>();
	 * params.put("character_id", "8287548916321388337");
	 * new CensusData("character", "faction_id", params, new HashMap<>(1));
	 * @param lookup The subdomain to look up
	 * @param get The data to get from result.
	 * @param resolve Any resolve terms.
	 * @param searchTerms The terms to put in the query.
	 * @param eventSearchTerms Ther terms to pull from the received event when querying
	 */
	private CensusData(String lookup, String get, String resolve, HashMap<String, String> searchTerms, HashMap<String, String> eventSearchTerms) {
		this.lookup = lookup;
		this.get = get;
		this.resolve = resolve;
		this.searchTerms = searchTerms;
		this.eventSearchTerms = eventSearchTerms;
	}
	
	public CensusData(JSONObject data) {
		JSONArray terms = data.getJSONArray("searchTerms");
		HashMap<String, String> searchTerms = new HashMap<>(20);
		for (int i = 0; i < terms.length(); i++) {
			JSONObject term = terms.getJSONObject(i);
			searchTerms.put(term.getString("key"),term.getString("value"));
		}
		
		JSONArray eventTerms = data.getJSONArray("eventSearchTerms");
		HashMap<String, String> eventSearchTerms = new HashMap<>(20);
		for (int i = 0; i < eventTerms.length(); i++) {
			JSONObject term = terms.getJSONObject(i);
			eventSearchTerms.put(term.getString("key"),term.getString("value"));
		}
		
		String resolve = data.optString("resolve");
		if (resolve == null) {
			new CensusData(data.getString("lookup"),data.getString("get"), searchTerms, eventSearchTerms);
		}else{
			new CensusData(data.getString("lookup"),data.getString("get"), resolve ,searchTerms, eventSearchTerms);
		}
	}
	/**
	 * Example:
	 * HashMap<String, String> params = new HashMap<>();
	 * params.put("character_id", "8287548916321388337");
	 * new CensusData("character", "faction_id", params, new HashMap<>(1));
	 * @param lookup The subdomain to look up
	 * @param get The data to get from result.
	 * @param searchTerms The terms to put in the query.
	 * @param eventSearchTerms There terms to pull from the received event when querying
	 */
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
		JSONObject response;
		
		if (resolve == null) {
			queryString += "&c:show=" + get;
			response = CensusConnection.sendQuery(queryString);
			
			
			if (response != null) {
					response = unpackSingleResponseFromArray(response);
					
					if (response.has(get) || !response.isNull(get)) {
						return response.getString(get);
					}
					
					return "";
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
		logger.network().severe("possible error in query. Target response was not produced:");
		logger.network().severe("Query: " + queryString);
		logger.network().severe("Response: " + response);
		if (response != null) {
			response.keySet().forEach(logger.network()::info);
		}
		return "";
	}
	
	private JSONObject unpackSingleResponseFromArray(JSONObject response) {
		if (response.keySet().stream().anyMatch(it -> it.contains("list"))) {
			@SuppressWarnings("OptionalGetWithoutIsPresent") // Check ifPresent performed above
			JSONArray responseList = response.getJSONArray(response.keySet().stream()
					.filter(it -> it.contains("list"))
					.findFirst().get());
			if (responseList.length() > 0){
				response = responseList.getJSONObject(0);
			}else return new JSONObject("{}");
		}
		return response;
	}
	
	@Override
	public HashMap<String, String> toJson() {
		HashMap<String, String> h = new HashMap<>(15);
		h.put("lookup", lookup);
		h.put("get",get);
		if (resolve != null) {
			h.put("resolve",resolve);
		}

		JSONArray jsonSearchTerms = mapToJsonArray(searchTerms);
		JSONArray jsonEventSearchTerms = mapToJsonArray(eventSearchTerms);

		h.put("searchTerms",jsonSearchTerms.toString());
		h.put("eventSearchTerms", jsonEventSearchTerms.toString());
		return h;
	}

	private JSONArray mapToJsonArray(HashMap<String, String> map) {
		JSONArray jsonSearchTerms = new JSONArray();
		for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
			JSONObject entry = new JSONObject();
			entry.put("key",stringStringEntry.getKey());
			entry.put("value",stringStringEntry.getValue());
			jsonSearchTerms.put(entry);
		}
		return jsonSearchTerms;
	}

	/*public static void main(String[] args) {
		HashMap<String, String> params = new HashMap<>();
		params.put("character_id", "8287548916321388337");
		CensusData query = new CensusData("character", "faction_id", params, new HashMap<>(1));
		System.out.println(query.get(new JSONObject("{}")));
	}*/
}
