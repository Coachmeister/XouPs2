package net.ximias.persistence;

import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
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
		APPLICATION_WIDTH = 800;
		APPLICATION_HEIGHT = 600;
		PLANETSIDE_INPUT_PROFILE = new HashSet<>(6);
		ACTION_COLORING = populateActionColoring();
		
		INDAR = new Color(1, 0.8, 0.7, 1.0);
		ESAMIR = new Color(0.7,0.9,1,1.0);
		AMERISH = new Color(0.0,0.8,0.4,1.0);
		HOSSIN = new Color(0.7,0.9,0.1,1.0);
		OTHER = new Color(1.0,0.85,.75,1.0);
		VS = new Color(0.4,0.0,1.0,1.0);
		TR = new Color(0.8,0.0,0.0,1.0);
		NC = new Color(0.1,0.3,0.9,1.0);
		return this;
	}
	
	public transient HashMap<String, Color> ACTION_COLORING;
	public transient Color INDAR;
	public transient Color ESAMIR;
	public transient Color AMERISH;
	public transient Color HOSSIN;
	public transient Color OTHER;
	public transient Color VS;
	public transient Color TR;
	public transient Color NC;
	public String LAST_LOGIN;
	public double BACKGROUND_BRIGHTNESS_SLIDER;
	public double BACKGROUND_TRANSPARENCY_SLIDER;
	public double EFFECT_TRANSPARENCY_SLIDER;
	public double APPLICATION_WIDTH;
	public double APPLICATION_HEIGHT;
	public HashSet<File> PLANETSIDE_INPUT_PROFILE;
	public File LAST_SELECTED_INPUT_PROFILE;
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
		in.defaultReadObject();
		@SuppressWarnings("unchecked")
		HashMap<String, PersistColor> actionColorPersist = (HashMap<String, PersistColor>) in.readObject();
		ACTION_COLORING = new HashMap<>();
		actionColorPersist.forEach((s, persistColor) -> ACTION_COLORING.put(s, persistColor.toColor()));
		INDAR = ((PersistColor)in.readObject()).toColor();
		ESAMIR =  ((PersistColor) in.readObject()).toColor();
		AMERISH = ((PersistColor) in.readObject()).toColor();
		HOSSIN =  ((PersistColor) in.readObject()).toColor();
		OTHER =   ((PersistColor) in.readObject()).toColor();
		VS = ((PersistColor) in.readObject()).toColor();
		TR = ((PersistColor) in.readObject()).toColor();
		NC = ((PersistColor) in.readObject()).toColor();
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException{
		out.defaultWriteObject();
		HashMap<String, PersistColor> actionColorPersist = new HashMap<>();
		ACTION_COLORING.forEach((key, value) -> actionColorPersist.put(key, PersistColor.getPersistColor(value)));
		out.writeObject(actionColorPersist);
		out.writeObject(PersistColor.getPersistColor(INDAR));
		out.writeObject(PersistColor.getPersistColor(ESAMIR));
		out.writeObject(PersistColor.getPersistColor(AMERISH));
		out.writeObject(PersistColor.getPersistColor(HOSSIN));
		out.writeObject(PersistColor.getPersistColor(OTHER));
		out.writeObject(PersistColor.getPersistColor(VS));
		out.writeObject(PersistColor.getPersistColor(TR));
		out.writeObject(PersistColor.getPersistColor(NC));
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
