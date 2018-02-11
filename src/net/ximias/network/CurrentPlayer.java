package net.ximias.network;

import org.json.JSONObject;

public class CurrentPlayer {
	private static CurrentPlayer ourInstance = new CurrentPlayer();
	
	public static CurrentPlayer getInstance() {
		return ourInstance;
	}
	
	private CurrentPlayer() {
	}
	
	private String playerID;
	
	private JSONObject playerInfo;
	
	private void updatePlayerInfo(){
		playerInfo = CensusConnection.sendQuery("character/?character_id="+playerID).getJSONArray("character_list").getJSONObject(0);
	}
	
	public void setPlayerID(String playerID) {
		this.playerID = playerID;
		updatePlayerInfo();
	}
	
	public String getPlayerID() {
		return playerID;
	}
	
	public String getValue(String key){
		if (playerInfo.has(key)){
			return playerInfo.getString(key);
		}
		System.out.println("Player variable not found: "+key);
		return "";
	}
}