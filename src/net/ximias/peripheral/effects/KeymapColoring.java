package net.ximias.peripheral.effects;

import javafx.scene.paint.Color;
import net.ximias.persistence.Persisted;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class KeymapColoring {
	private HashMap<String, Color> actionColorMap;
	private File userSettings;
	private HashMap<String, Color> keyColorMap;
	private static final int[] USABLE_INDICES_FROM_KEYBIND_FILE = new int[]{0, 4};
	
	public KeymapColoring(File userSettings) {
		
		actionColorMap = Persisted.getInstance().ACTION_COLORING;
		this.userSettings = userSettings;
	}
	
	public HashMap<String, Color> getKeyColors() {
		String xmlFileContents = readFileToString(userSettings);
		JSONArray keymap = XML.toJSONObject(xmlFileContents,true).getJSONObject("Profile").getJSONArray("ActionSet");
		
		return getKeyColorMap(keymap);
	}
	
	private String readFileToString(File file) {
		StringBuilder sb = new StringBuilder();
		try(Scanner scanner = new Scanner(file)){
			while (scanner.hasNextLine()){
				sb.append(scanner.nextLine());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	private HashMap<String, Color> getKeyColorMap(JSONArray keymap) {
		keyColorMap = new HashMap<>(32);
		
		for (int index : USABLE_INDICES_FROM_KEYBIND_FILE) {
			JSONArray keybindActionArray = keymap.getJSONObject(index).getJSONArray("Action");
			addKeyColorsFromAction(keybindActionArray);
		}
		
		return keyColorMap;
	}
	
	private void addKeyColorsFromAction(JSONArray actions) {
		for (int i = 0; i < actions.length(); i++) {
			JSONObject action = actions.getJSONObject(i);
			addIfPresent(action);
		}
	}
	
	private void addIfPresent(JSONObject action) {
		for (String actionName : actionColorMap.keySet()) {
			if (action.getString("name").equals(actionName)){
				addKeyColorsFromAction(action, actionName);
			}
		}
	}
	
	private void addKeyColorsFromAction(JSONObject action, String actionName) {
		Object key = action.get("Trigger");
		if (key instanceof JSONArray){
			addKeyArrayToMap(actionName, (JSONArray) key);
		}else if (key instanceof String){
			addSingleKeyToMap(actionName, (String) key);
		}
	}
	
	private void addSingleKeyToMap(String actionName, String key) {
		keyColorMap.put(key, actionColorMap.get(actionName));
	}
	
	private void addKeyArrayToMap(String actionName, JSONArray key) {
		for (int j = 0; j < key.length(); j++) {
			keyColorMap.put(key.getString(j),actionColorMap.get(actionName));
		}
	}
	
	public HashMap<String, Color> getActionColorMap() {
		return actionColorMap;
	}
	
	public void addActionColor(String action, Color color){
		actionColorMap.put(action, color);
	}
}
