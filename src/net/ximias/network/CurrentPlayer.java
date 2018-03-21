package net.ximias.network;

import javafx.scene.paint.Color;
import net.ximias.effects.EffectViews.Scenes.SceneConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.rmi.server.InactiveGroupException;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;

public class CurrentPlayer {
	private static CurrentPlayer ourInstance = new CurrentPlayer();
	private Logger logger = Logger.getLogger(getClass().getName());
	public static CurrentPlayer getInstance() {
		return ourInstance;
	}
	
	private CurrentPlayer() {
		initialize();
		
		logger.finer("Init");
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
		if (getValue("faction_id").equals(String.valueOf(SceneConstants.VS_ID))) return SceneConstants.VS;
		if (getValue("faction_id").equals(String.valueOf(SceneConstants.NC_ID))) return SceneConstants.NC;
		if (getValue("faction_id").equals(String.valueOf(SceneConstants.TR_ID))) return SceneConstants.TR;
		return SceneConstants.MISSING;
		
		
	}
	
	public void initialize() {
		try {
			String partlyPlayername = JOptionPane.showInputDialog(null, "Input (start of) character name", "ximias");
			if (partlyPlayername == null) {
				cancelled();
			}
			JSONArray playerNameList = CensusConnection.listPlayersStartsWith(partlyPlayername);
			
			String selectedCharacterId = performCharacterSelection(playerNameList);
			setPlayerID(selectedCharacterId);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Connection issues. I recommend trying again:\n" + e, "Error", JOptionPane.ERROR_MESSAGE);
			logger.severe("Connection could not be established to the PlanetSide servers. Check your connection and the PlanetSide servers, if the issue persists.");
			System.exit(1);
		}
	}
	
	private String performCharacterSelection(JSONArray results) {
		JSONObject[] options = getLimitedOptions(results);
		
		String[] names = new String[options.length];
		for (int i = 0; i < options.length; i++) {
			names[i] = options[i].getJSONObject("name").getString("first");
		}
		JComboBox<String> selection = new JComboBox<>(names);
		
		return options[(showComboDialog(selection))].getString("character_id");
	}
	
	private int showComboDialog(JComboBox<String> selection) {
		JOptionPane pane = new JOptionPane("Is it any of these?", JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		pane.add(selection,1);
		JDialog box = pane.createDialog("Select Character");
		box.setVisible(true);
		box.dispose();
		Object value = pane.getValue();
		if (value == null){
			cancelled();
		}else{
			if (value instanceof Integer){
				if ((Integer) value != 0){
					cancelled();
				}
			}
		}
		return selection.getSelectedIndex();
	}
	
	private void cancelled() {
		logger.severe("Application initialization canceled by user");
		System.exit(0);
	}
	
	private JSONObject[] getLimitedOptions(JSONArray results) {
		
		ArrayList<JSONObject> fullOptions = new ArrayList<>((int)(results.length()*1.5));
		for (int i = 0; i < results.length(); i++) {
			fullOptions.add(results.getJSONObject(i));
		}
		Collections.sort(fullOptions, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				return o1.getJSONObject("name").getString("first").compareTo(o2.getJSONObject("name").getString("first"));
			}
		});
		
		JSONObject[] options = new JSONObject[Math.min(results.length(), 8)];
		for (int i = 0; i < options.length; i++) {
			options[i] = fullOptions.get(i);
		}
		return options;
	}
}