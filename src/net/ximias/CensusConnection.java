package net.ximias;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


/**
 * Used to look up information via the census API
 */
public class CensusConnection {
	
	private static JSONObject censusQuery(String urlParameters) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL("http://census.daybreakgames.com/s:XouPs2/get/ps2/"+urlParameters).openConnection();
		connection.setRequestMethod("GET");
		if (connection.getResponseCode() != 200){
			System.out.println("Unexpected response code: "+connection.getResponseCode());
			if (connection.getResponseCode()-200>=100) throw new Error("server error response ("+connection.getResponseCode()+") to request: ...get/ps2/"+urlParameters);
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		
		return new JSONObject(response.toString());
	}
	
	/**
	 * Returns the first player whose name starts with namePrefix parameter.
	 * Priority goes to in alphabetical order. Inserting ximia will select ximia over ximiaa over ximias
	 * @param namePrefix the player name to select from.
	 * @return the JSONObject with player data.
	 * @throws IOException
	 */
	static JSONObject findPlayerByName(String namePrefix) throws IOException {
		JSONObject players = censusQuery("character_name/?name.first_lower=^"+namePrefix.toLowerCase()+"&c:limit=10");
		
		return censusQuery("character/?character_id="+players.getJSONArray("character_name_list").getJSONObject(0).getString("character_id"));
		
	}
	
	/**
	 * Lists 10 players with the same prefix as the parameter.
	 * @param prefix the prefix of the player names.
	 * @return up to 10 player names and ids with the prefix in their lower name
	 * @throws IOException
	 */
	public static JSONArray listPlayersStartsWith(String prefix) throws IOException {
		JSONObject players = censusQuery("character_name/?name.first_lower=^"+prefix.toLowerCase()+"&c:limit=10");
		return players.getJSONArray("character_name_list");
	}
	
	public static JSONArray listPlayersContains(String contains) throws IOException {
		JSONObject players = censusQuery("character_name/?name.first_lower=*"+contains.toLowerCase()+"&c:limit=10");
		return players.getJSONArray("character_name_list");
	}
	
	public static void main(String[] args) throws IOException {
		listPlayersStartsWith("ximia").forEach(System.out::println);
		//System.out.println(findPlayerByName("trick2scuard"));
	}
}
