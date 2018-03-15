package net.ximias.network;

import javafx.scene.paint.Color;
import net.ximias.effects.EffectViews.Scenes.SceneConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;

public class CurrentPlayer {
	private static CurrentPlayer ourInstance = new CurrentPlayer();
	
	public static CurrentPlayer getInstance() {
		return ourInstance;
	}
	
	private CurrentPlayer() {
		initialize();
		
		System.out.println("init");
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
			System.out.println(playerObject.toString());
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
		System.out.println("Player variable not found: " + key);
		return "";
	}
	
	public void setZoneId(int zoneId) {
		this.zoneId = zoneId;
		playerInfo.put("zone_id",String.valueOf(zoneId));
		System.out.println("Zone id updated: "+zoneId);
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
			JSONArray playerNameList = CensusConnection.listPlayersStartsWith(partlyPlayername);
			
			String selectedCharacterId = performCharacterSelection(playerNameList);
			setPlayerID(selectedCharacterId);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "IOException. I recommend trying again:\n" + e, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private String performCharacterSelection(JSONArray results) {
		String[] options = getLimitedOptions(results);
		
		JComboBox<String> selection = new JComboBox<>(options);
		
		return results.getJSONObject(showComboDialog(selection)).getString("character_id");
	}
	
	private int showComboDialog(JComboBox<String> selection) {
		JOptionPane pane = new JOptionPane("Is it any of these?", JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION);
		pane.add(selection);
		JDialog box = pane.createDialog("Select Character");
		box.setVisible(true);
		box.dispose();
		return selection.getSelectedIndex();
	}
	
	private String[] getLimitedOptions(JSONArray results) {
		String[] options = new String[Math.min(results.length(), 8)];
		for (int i = 0; i < options.length; i++) {
			options[i] = results.getJSONObject(i).getJSONObject("name").getString("first");
		}
		return options;
	}
}