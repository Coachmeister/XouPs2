package net.ximias.peripheral.keyboard;


import javafx.scene.paint.Color;

import java.util.Map;

public interface Keyboard {
	int getRows();
	
	int getColumns();
	
	void setAndExemptColors(Map<String, Color> keyColorMap);
	
	void enable();
	
	void disable();
	
	KeyboardEffectContainer getEffectContainer();
	
	void resetExemptions();
	
	void setMultiKey(boolean enableMultiKey);
	
	boolean isMultiKey();
	
	static String convertNumberToWord(String number) {
		String[] numNames = new String[]{"ZERO", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE"};
		try {
			return numNames[Integer.parseInt(number)];
		} catch (NumberFormatException e) {
			return "UNDEFINED";
		}
		
	}
}
