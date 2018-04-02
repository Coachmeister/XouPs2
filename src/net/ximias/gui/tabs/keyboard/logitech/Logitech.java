package net.ximias.gui.tabs.keyboard.logitech;

import com.logitech.gaming.LogiLED;
import javafx.scene.paint.Color;
import net.ximias.effect.Renderer;
import net.ximias.effect.views.EffectContainer;
import net.ximias.peripheral.Keyboard;
import net.ximias.peripheral.KeyboardEffectContainer;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;


public class Logitech implements Renderer, Keyboard {
	private static final int FRAME_RATE_MS = 17; // 58 frames per second.
	private final HashMap<Integer, Color> exemptKeys = new HashMap<>(32);
	private KeyboardEffectContainer effectContainer;
	private boolean multiKey;
	private Timer animationTimer = new Timer(true);
	private Logger logger = Logger.getLogger(getClass().getName());
	
	
	public Logitech(EffectContainer globalEffectContainer, boolean enableMultiKey) {
		multiKey = enableMultiKey;
		effectContainer = new KeyboardEffectContainer(globalEffectContainer, LogiLED.LOGI_LED_BITMAP_WIDTH, LogiLED.LOGI_LED_BITMAP_HEIGHT);
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
	public void setAndExemptKeyByName(String name, Color color) {
		if (!multiKey) return;
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
	
	public KeyboardEffectContainer getEffectContainer() {
		return effectContainer;
	}
	
	private void saveExemptKeys() {
		if (!multiKey) return;
		for (Integer keyName : exemptKeys.keySet()) {
			LogiLED.LogiLedSaveLightingForKey(keyName);
		}
	}
	
	private void restoreExemptKeys() {
		if (!multiKey) return;
		for (Integer keyName : exemptKeys.keySet()) {
			LogiLED.LogiLedRestoreLightingForKey(keyName);
		}
	}
	
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
	
	private void drawFrame() {
		
		Color globalColor = effectContainer.getGlobalColor();
		LogiLED.LogiLedSetTargetDevice(LogiLED.LOGI_DEVICETYPE_RGB| LogiLED.LOGI_DEVICETYPE_MONOCHROME);
		LogiLED.LogiLedSetLighting((int) Math.round(globalColor.getRed() * 100), (int) Math.round(globalColor.getGreen() * 100), (int) Math.round(globalColor.getBlue() * 100));
		
		if (multiKey){
			LogiLED.LogiLedSetTargetDevice(LogiLED.LOGI_DEVICETYPE_PERKEY_RGB);
			saveExemptKeys();
			setAllNonBitmapKeys(globalColor);
			LogiLED.LogiLedSetLightingFromBitmap(getFormattedColorArray());
			restoreExemptKeys();
		}
	}
	
	private void setAllNonBitmapKeys(Color color){
		for (int i = 0xFFF1; i < 0xFFFF; i++) {
			LogiLED.LogiLedSetLightingForKeyWithKeyName(i,(int) Math.round(color.getRed() * 100), (int) Math.round(color.getGreen() * 100), (int) Math.round(color.getBlue() * 100));
		}
		for (int i = 0xFFFF1; i < 0xFFFF8; i++) {
			LogiLED.LogiLedSetLightingForKeyWithKeyName(i,(int) Math.round(color.getRed() * 100), (int) Math.round(color.getGreen() * 100), (int) Math.round(color.getBlue() * 100));
		}
	}
	
	private byte[] getFormattedColorArray(){
		Color[][] source = effectContainer.getPerKeyColor();
		int width = source.length;
		int height = source[0].length;
		byte[] dest = new byte[LogiLED.LOGI_LED_BITMAP_SIZE];
		
		for (int row = 0; row < height; row++) {
			for (int column = 0; column < width; column++) {
				int pixel = ((row*width)+column)*LogiLED.LOGI_LED_BITMAP_BYTES_PER_KEY;
				dest[pixel] = (byte)Math.round(source[column][row].getBlue()*255);
				dest[pixel+1] = (byte)Math.round(source[column][row].getGreen()*255);
				dest[pixel+2] = (byte)Math.round(source[column][row].getRed()*255);
				dest[pixel+3] = (byte)Math.round(source[column][row].getOpacity()*255);
			}
		}
		return dest;
	}
	
	private void start() {
		stop();
		animationTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				drawFrame();
			}
		}, 0, FRAME_RATE_MS);
	}
	
	private void stop() {
		animationTimer.cancel();
		animationTimer = new Timer(true);
	}
	
	public void setMultiKey(boolean multiKey) {
		this.multiKey = multiKey;
	}
}
