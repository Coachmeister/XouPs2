package net.ximias.network;

import net.ximias.datastructures.collections.LeastRecentlyUsedCache;
import net.ximias.logging.Logger;
import net.ximias.persistence.ApplicationConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Used to look up information via the census API
 */
public class CensusConnection {
	private static final Logger staticLogger = Logger.getLogger(CensusConnection.class.getName());
	private static final LeastRecentlyUsedCache<String, CompletableFuture<JSONObject>> recentQueries = new LeastRecentlyUsedCache<>(512); // Guesstimated to be large enough to contain the common queries and experience id name mappings.
	
	private static JSONObject sendAndCacheCensusQuery(String urlParameters) throws IOException {
		CompletableFuture<JSONObject> cached;
		synchronized (recentQueries) {
			cached = recentQueries.get(urlParameters);
			
			if (cached == null ||cached.isCompletedExceptionally()) {
				cached = null;
				recentQueries.put(urlParameters, new CompletableFuture<>());
			}
		}
		
		if (cached != null) {
			try {
				return cached.get();
			} catch (InterruptedException e) {
				staticLogger.network().severe("Waiting census request interrupted: " + e);
				return ApplicationConstants.EMPTY_JSON;
			} catch (ExecutionException e) {
				staticLogger.network().severe("Exception in census request: " + e);
				return ApplicationConstants.EMPTY_JSON;
			}
		}
		staticLogger.network().info("Looking up data in census: "+urlParameters);
		cached = recentQueries.get(urlParameters);
		try{
			JSONObject response = establishConnectionAndQuery(urlParameters);
			cached.complete(response);
			return response;
		}catch (Error e){
			cached.completeExceptionally(e);
			throw e;
		}
	}
	
	private static JSONObject establishConnectionAndQuery(String urlParameters) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL("http://census.daybreakgames.com/s:XouPs2/get/ps2/" + urlParameters).openConnection();
		connection.setRequestMethod("GET");
		if (connection.getResponseCode() != 200) {
			staticLogger.network().severe("Unexpected response code: " + connection.getResponseCode());
			if (connection.getResponseCode() - 200 >= 100) {
				throw new Error("server error response (" + connection.getResponseCode() + ") to request: ...get/ps2/" + urlParameters);
			}
		}
		
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		
		JSONObject result = new JSONObject(response.toString());
		return result;
	}
	
	/**
	 * Lists 10 players with the same prefix as the parameter.
	 *
	 * @param prefix the prefix of the player names.
	 * @return up to 10 player names and ids with the prefix in their lower name
	 * @throws IOException
	 */
	public static JSONArray listPlayersStartsWith(String prefix) throws IOException {
		JSONObject players = sendAndCacheCensusQuery("character_name/?name.first_lower=^" + prefix.toLowerCase() + "&c:limit=10");
		if (players.has("character_name_list")) {
			return players.getJSONArray("character_name_list");
		} else {
			throw new IOException("PlanetSide server responded with: " + players);
		}
	}
	
	/**
	 * Used to get data from the census. Will cache the response.
	 * @param queryString the string after .../get/ps2/
	 * @return the response from the census servers.
	 */
	public static JSONObject sendQuery(String queryString) {
		try {
			return sendAndCacheCensusQuery(queryString);
		} catch (IOException e) {
			staticLogger.network().severe("Query failed: " + e);
			staticLogger.network().warning("retrying...");
			try {
				return sendAndCacheCensusQuery(queryString);
			} catch (IOException e1) {
				staticLogger.network().severe("Second query attempt failed: " + e);
				staticLogger.network().warning("retrying..");
				try {
					return sendAndCacheCensusQuery(queryString);
				} catch (IOException e2) {
					staticLogger.network().severe("Third attempt failed: " + e);
					staticLogger.network().severe("I'll assume trying any more times won't fix the issue. I hope the rest of the program can cope with an empty response");
				}
			}
			return ApplicationConstants.EMPTY_JSON;
		}
	}
	
	/**
	 * Used to poll the census. Wil bypass the cache, as response might change on subsequent calls.
	 * Needless to say, using this function is expensive, and should be avoided if possible.
	 * @param queryString the queryString to poll. Should start with [target]/?[query]
	 * @return the response form the server. Or empty json on exception.
	 */
	public static JSONObject poll(String queryString){
		try {
			return establishConnectionAndQuery(queryString);
		} catch (IOException e) {
			return ApplicationConstants.EMPTY_JSON;
		}
	}
	
	/**
	 * Returns the first player whose name starts with namePrefix parameter.
	 * Priority goes to in alphabetical order. Inserting ximia will select ximia over ximiaa over ximias
	 *
	 * @param namePrefix the player name to select from.
	 * @return the JSONObject with player data.
	 * @throws IOException
	 */
	public static JSONObject findPlayerByName(String namePrefix) throws IOException {
		JSONObject players = sendAndCacheCensusQuery("character_name/?name.first_lower=^" + namePrefix.toLowerCase() + "&c:limit=10");
		
		return sendAndCacheCensusQuery("character/?character_id=" + players.getJSONArray("character_name_list").getJSONObject(0).getString("character_id")).getJSONArray("character_list").getJSONObject(0);
		
	}
	
	
}

/*{"character_list":[{
"times":{"login_count":"29","last_save":"1510257548","last_login":"1510254664","minutes_played":"2797","creation_date":"2017-09-28 18:30:22.0","last_login_date":"2017-11-09 19:11:04.0","creation":"1506623422","last_save_date":"2017-11-09 19:59:08.0"},
"battle_rank":{"percent_to_next":"94","value":"31"},
"profile_id":"20",
"name":{"first_lower":"ximiasfromcobalt","first":"XimiasFromCobalt"},
"title_id":"0",
"faction_id":"1",
"daily_ribbon":{"date":"2017-11-08 23:00:00.0","count":"5","time":"1510182000"},"character_id":"5428653157989617089","head_id":"1","certs":{"earned_points":"3428","spent_points":"4474","percent_to_next":"0.64","available_points":"468","gifted_points":"1514"}}],"returned":1}

 */