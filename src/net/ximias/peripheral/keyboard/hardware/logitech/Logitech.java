package net.ximias.peripheral.keyboard.hardware.logitech;

import com.logitech.gaming.LogiLED;
import javafx.scene.paint.Color;
import net.ximias.effect.Renderer;
import net.ximias.effect.views.EffectContainer;
import net.ximias.peripheral.keyboard.Keyboard;
import net.ximias.peripheral.keyboard.KeyboardEffectContainer;
import net.ximias.peripheral.keyboard.hardware.AbstractKeyboard;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

/**
 * Logitech specific implementation of the Keyboard interface.
 * Used to display hardware effects on logitech peripherals.
 */
public class Logitech extends AbstractKeyboard implements Renderer {
	private static final int FRAME_RATE_MS = 33; //Appears to work better at 30 fps
	private final HashMap<Integer, Color> exemptKeys = new HashMap<>(32);
	private KeyboardEffectContainer effectContainer;
	private boolean multiKey;
	private static final String ANIMATION_TIMER_NAME = "Logitech animation timer";
	private Timer animationTimer = new Timer(ANIMATION_TIMER_NAME,true);
	private Logger logger = Logger.getLogger(getClass().getName());
	private boolean gFlashOn = true;
	private boolean isStarted = false;
	private long lastRender = System.currentTimeMillis();
	
	public Logitech(EffectContainer globalEffectContainer, boolean enableMultiKey) {
		super(globalEffectContainer);
		multiKey = enableMultiKey;
		effectContainer = new KeyboardEffectContainer(globalEffectContainer, LogiLED.LOGI_LED_BITMAP_WIDTH, LogiLED.LOGI_LED_BITMAP_HEIGHT);
		effectContainer.subscribeResumeRendering(this);
		Timer animationMonitor = new Timer(ANIMATION_TIMER_NAME,true);
		animationMonitor.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (System.currentTimeMillis() > lastRender+61_000){
					logger.severe("Keyboard renderer stopped for over 1 minute");
				}
			}
		},1000,1000);
	}
	
	@Override
	public void resumeRendering() {
		start();
	}
	
	@Override
	public int getRows() {
		return multiKey ? LogiLED.LOGI_LED_BITMAP_HEIGHT : 1;
	}
	
	@Override
	public int getColumns() {
		return multiKey ? LogiLED.LOGI_LED_BITMAP_WIDTH : 1;
	}
	
	@Override
	public void setAndExemptColors(Map<String, Color> keyColorMap) {
		resetExemptions();
		for (Map.Entry<String, Color> keyColor : keyColorMap.entrySet()) {
			setAndExemptKeyByName(keyColor.getKey(), keyColor.getValue());
		}
	}
	
	/**
	 * Used to set and exempt a single key from effects.
	 * @param name the name of the key.
	 * @param color the color of the key.
	 */
	private void setAndExemptKeyByName(String name, Color color) {
		if (!multiKey) return;
		name = getLogitechNameForKey(name);
		for (Field field : LogiLED.class.getDeclaredFields()) {
			if (field.getName().equals(name.toUpperCase())) {
				try {
					exemptKeys.put(field.getInt(null), color);
					setExemptKeyColors();
				} catch (IllegalAccessException e) {
					logger.warning("Looking up key threw Exception: " + e);
				}
			}
		}
	}
	
	/**
	 * Used to translate PlanetSide2 specific keynames into Logitech ones.
	 * @param name the name of a key, as PlanetSide2 names it.
	 * @return the Logitech equivalent of that key.
	 */
	private String getLogitechNameForKey(String name){
		// No key combos
		if (name.contains("+")) return "UNDEFINED";
		
		// Keypad names: KP_2 should be NUM_TWO
		if (name.startsWith("KP")){
			return "NUM_"+Keyboard.convertNumberToWord(name.substring(name.indexOf("_")+1));
		}
		
		// 1 should be ONE
		String convertedNum = Keyboard.convertNumberToWord(name);
		if (!convertedNum.equals("UNDEFINED")) return convertedNum;
		
		//Bracket_Left should be OPEN_BRACKET
		if (name.contains("Bracket")){
			if (name.contains("left")){
				return "OPEN_BRACKET";
			}else return "CLOSE_BRACKET";
		}
		
		// They explain themselves. Must come before use of name.contains("Left")
		if (name.equals("Up")) return "ARROW_UP";
		if (name.equals("Down")) return "ARROW_DOWN";
		if (name.equals("Left")) return "ARROW_LEFT";
		if (name.equals("Right")) return "ARROW_RIGHT";
		
		// Alt_Left should be LEFT_ALT
		if (name.contains("Left")){
			return "LEFT_"+name.substring(0,name.indexOf("_")).toUpperCase();
		}
		if (name.contains("Right")){
			return "RIGHT_"+name.substring(0,name.indexOf("_")).toUpperCase();
		}
		// Delete should be KEYBOARD_DELETE
		if (name.equals("Delete")) return "KEYBOARD_DELETE";
		
		// PageUp should be PAGE_UP;
		return name.replaceAll("(.)(\\p{Upper})", "$1_$2").toUpperCase();
	}
	
	@Override
	public void enable() {
		LogiLED.LogiLedInit();
		LogiLED.LogiLedSetTargetDevice(LogiLED.LOGI_DEVICETYPE_ALL);
		LogiLED.LogiLedStopEffects();
		start();
	}
	
	@Override
	public void disable() {
		stop();
		LogiLED.LogiLedShutdown();
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		LogiLED.LogiLedShutdown();
	}
	
	@Override
	public KeyboardEffectContainer getEffectContainer() {
		return effectContainer;
	}
	
	/**
	 * Called every frame of animation.
	 */
	private void drawFrame() {
		
		Color globalColor = effectContainer.getGlobalColor();
		LogiLED.LogiLedSetTargetDevice(LogiLED.LOGI_DEVICETYPE_RGB | LogiLED.LOGI_DEVICETYPE_MONOCHROME);
		LogiLED.LogiLedSetLighting((int) Math.round(globalColor.getRed() * 100), (int) Math.round(globalColor.getGreen() * 100), (int) Math.round(globalColor.getBlue() * 100));
		
		if (multiKey) {
			LogiLED.LogiLedSetTargetDevice(LogiLED.LOGI_DEVICETYPE_PERKEY_RGB);
			setAllNonBitmapKeys(gFlashOn ? globalColor : globalColor.invert());
			gFlashOn = !gFlashOn;
			LogiLED.LogiLedSetLightingFromBitmap(getFormattedColorArray());
		}
		if (effectContainer.isPausable()) stop();
	}
	
	/**
	 * sets lighting for exempt keys and exempts them from the effects.
	 */
	private void setExemptKeyColors() {
		if (!multiKey) return;
		LogiLED.LogiLedSetTargetDevice(LogiLED.LOGI_DEVICETYPE_PERKEY_RGB);
		int keys[] = new int[exemptKeys.size()];
		int index = 0;
		
		for (Integer keyName : exemptKeys.keySet()) {
			keys[index++] = keyName;
			Color keyColor = exemptKeys.get(keyName);
			LogiLED.LogiLedSetLightingForKeyWithKeyName(keyName, (int) Math.round(keyColor.getRed() * 100), (int) Math.round(keyColor.getGreen() * 100), (int) Math.round(keyColor.getBlue() * 100));
		}
		LogiLED.LogiLedExcludeKeysFromBitmap(keys);
	}
	
	@Override
	public void resetExemptions() {
		LogiLED.LogiLedSetTargetDevice(LogiLED.LOGI_DEVICETYPE_PERKEY_RGB);
		exemptKeys.clear();
		setExemptKeyColors();
		drawFrame();
	}
	
	/**
	 * Sets all keys not included in the bitmap.
	 * This includes the G-Keys and logos.
	 * A bit of possible future proofing has been added, by having the loops go higher than strictly needed.
	 * @param color the color to set the keys.
	 */
	private void setAllNonBitmapKeys(Color color) {
		for (int i = 0xFFF1; i < 0xFFFF; i++) {
			LogiLED.LogiLedSetLightingForKeyWithKeyName(i, (int) Math.round(color.getRed() * 100), (int) Math.round(color.getGreen() * 100), (int) Math.round(color.getBlue() * 100));
		}
		for (int i = 0xFFFF1; i < 0xFFFF8; i++) {
			LogiLED.LogiLedSetLightingForKeyWithKeyName(i, (int) Math.round(color.getRed() * 100), (int) Math.round(color.getGreen() * 100), (int) Math.round(color.getBlue() * 100));
		}
	}
	
	/**
	 * Used to obtain a logitech specific bitmap array of BGRA color values for each key.
	 * @return a byte array with color codes for each key.
	 */
	private byte[] getFormattedColorArray() {
		Color[][] source = effectContainer.getPerKeyColor();
		int width = source.length;
		int height = source[0].length;
		byte[] dest = new byte[LogiLED.LOGI_LED_BITMAP_SIZE];
		
		for (int row = 0; row < height; row++) {
			for (int column = 0; column < width; column++) {
				int pixel = ((row * width) + column) * LogiLED.LOGI_LED_BITMAP_BYTES_PER_KEY;
				dest[pixel] = (byte) Math.round(source[column][row].getBlue() * 255);
				dest[pixel + 1] = (byte) Math.round(source[column][row].getGreen() * 255);
				dest[pixel + 2] = (byte) Math.round(source[column][row].getRed() * 255);
				dest[pixel + 3] = (byte) Math.round(source[column][row].getOpacity() * 255);
			}
		}
		return dest;
	}
	
	/**
	 * Starts the rendering.
	 */
	private synchronized void start() {
		if (!isStarted){
			logger.warning("Keyboard rendering started/resumed");
			isStarted = true;
			animationTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					lastRender = System.currentTimeMillis();
					drawFrame();
				}
			}, 0, FRAME_RATE_MS);
		}
	}
	
	/**
	 * Stops the rendering.
	 */
	private synchronized void stop() {
		logger.warning("Keyboard rendering stopped/paused");
		isStarted = false;
		animationTimer.cancel();
		animationTimer = new Timer(ANIMATION_TIMER_NAME,true);
	}
	
	/**
	 * Sets whether the multikey specific calculations should occur.
	 * @param multiKey true, if a per-key RGB hardware is among the peripherals.
	 */
	public void setMultiKey(boolean multiKey) {
		resumeRendering();
		this.multiKey = multiKey;
	}
	
	/**
	 * Returns whether the multikey property is set or not.
	 * @return the value of the multikey property.
	 */
	@Override
	public boolean isMultiKey() {
		return multiKey;
	}
}
