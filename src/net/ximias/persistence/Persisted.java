package net.ximias.persistence;

import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Stores values for to get and set as useful.
 */
public class Persisted implements Serializable {
	public static Persisted getInstance(){
		return PersistLoader.getInstance();
	}
	
	/**
	 * Used to set defaults if no saved data can be loaded.
	 * @return an instance initialized with default values.
	 */
	public Persisted defaults() {
		LAST_LOGIN = "ximias";
		BACKGROUND_BRIGHTNESS_SLIDER = ApplicationConstants.DEFAULT_BACKGROUND_BRIGHTENS;
		BACKGROUND_TRANSPARENCY_SLIDER = ApplicationConstants.DEFAULT_BACKGROUND_INTENSITY;
		EFFECT_TRANSPARENCY_SLIDER = ApplicationConstants.DEFAULT_EFFECT_INTENSITY;
		APPLICATION_WIDTH = 600;
		APPLICATION_HEIGHT = 450;
		PLANETSIDE_INPUT_PROFILE = new HashSet<>(6);
		ACTION_COLORING = populateActionColoring();
		return this;
	}
	
	public String LAST_LOGIN;
	public double BACKGROUND_BRIGHTNESS_SLIDER;
	public double BACKGROUND_TRANSPARENCY_SLIDER;
	public double EFFECT_TRANSPARENCY_SLIDER;
	public double APPLICATION_WIDTH;
	public double APPLICATION_HEIGHT;
	public transient HashMap<String, Color> ACTION_COLORING;
	public HashSet<File> PLANETSIDE_INPUT_PROFILE;
	public File LAST_SELECTED_INPUT_PROFILE;
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
		in.defaultReadObject();
		HashMap<String, PersistColor> actionColorPersist = (HashMap<String, PersistColor>) in.readObject();
		ACTION_COLORING = new HashMap<>();
		actionColorPersist.forEach((s, persistColor) -> ACTION_COLORING.put(s, persistColor.toColor()));
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException{
		out.defaultWriteObject();
		HashMap<String, PersistColor> actionColorPersist = new HashMap<>();
		ACTION_COLORING.forEach((key, value) -> actionColorPersist.put(key, new PersistColor(value.getRed(), value.getGreen(), value.getBlue(), value.getOpacity())));
		out.writeObject(actionColorPersist);
	}
	
	/**
	 * Adds default values to the actionColoring list.
	 * @return the actionColoring list.
	 */
	private HashMap<String, Color> populateActionColoring() {
		HashMap<String, Color> result = new HashMap<>();
		result.put("Jump", Color.GREEN);
		result.put("Crouch", Color.GREEN);
		result.put("CrouchToggle", Color.GREEN);
		result.put("Sprint", Color.GREEN);
		result.put("MoveForward", Color.GREEN);
		result.put("MoveBackward", Color.GREEN);
		result.put("StrafeLeft", Color.GREEN);
		result.put("StrafeRight", Color.GREEN);
		result.put("Interact", Color.BLUE);
		result.put("SpotPlayer", Color.DARKBLUE);
		result.put("Slot1", Color.YELLOW);
		result.put("Slot2", Color.YELLOW);
		result.put("Slot3", Color.YELLOW);
		result.put("Slot4", Color.YELLOW);
		result.put("Slot5", Color.YELLOW);
		result.put("Slot6", Color.YELLOW);
		result.put("Slot7", Color.YELLOW);
		result.put("Slot8", Color.YELLOW);
		result.put("Slot9", Color.YELLOW);
		result.put("Slot10", Color.YELLOW);
		result.put("ToggleRedeployTimer", Color.DARKOLIVEGREEN.darker());
		result.put("ToggleInstantActionTimer", Color.DARKOLIVEGREEN.darker());
		result.put("UseAbility", Color.CYAN);
		result.put("Reload", Color.ORANGERED);
		result.put("UseGrenade", Color.RED);
		result.put("WieldMelee", Color.ORANGE);
		return result;
	}
}
