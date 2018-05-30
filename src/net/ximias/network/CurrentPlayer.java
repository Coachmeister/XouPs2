package net.ximias.network;

import javafx.scene.paint.Color;
import net.ximias.persistence.ApplicationConstants;
import net.ximias.persistence.Persisted;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Logger;

public class CurrentPlayer {
	private static CurrentPlayer ourInstance;
	private final Logger logger = Logger.getLogger(getClass().getName());
	public static CurrentPlayer getInstance() {
		return ourInstance;
	}
	
	
	public static void initCurrentPlayer(String playerID){
		if (ourInstance == null) {
			ourInstance = new CurrentPlayer(playerID);
		}else{
			throw new Error("Player already initialized");
		}
	}
	
	private CurrentPlayer(String playerID){
		setPlayerID(playerID);
		setZoneId(-1);
	}
	
	private String playerID;
	
	private JSONObject playerInfo;
	
	private int zoneId;
	
	private void updatePlayerInfo() {
		JSONObject playerObject = CensusConnection.sendQuery("character/?character_id=" + playerID + "&c:resolve=world");
		if (playerObject.has("character_list")) {
			playerInfo = playerObject.getJSONArray("character_list").getJSONObject(0);
			setZoneId(zoneId);
		} else {
			logger.warning("No character list returned: "+ playerObject.toString());
		}
	}
	
	public void setPlayerID(String playerID) {
		this.playerID = playerID;
		updatePlayerInfo();
	}
	
	public String getPlayerID() {
		return playerID;
	}
	
	public String getValue(String key) {
		if (playerInfo.has(key)) {
			return playerInfo.getString(key);
		}
		logger.warning("Player variable not found: " + key);
		return "";
	}
	
	public void setZoneId(int zoneId) {
		this.zoneId = zoneId;
		playerInfo.put("zone_id",String.valueOf(zoneId));
		logger.info("Zone id updated: "+zoneId);
	}
	
	public Color getFactionColor(){
		if (getValue("faction_id").equals(String.valueOf(ApplicationConstants.VS_ID))) return Persisted.getInstance().VS;
		if (getValue("faction_id").equals(String.valueOf(ApplicationConstants.NC_ID))) return Persisted.getInstance().NC;
		if (getValue("faction_id").equals(String.valueOf(ApplicationConstants.TR_ID))) return Persisted.getInstance().TR;
		return ApplicationConstants.MISSING;
	}
	
	public String getWorld() {
		return getValue("world_id");
	}
}