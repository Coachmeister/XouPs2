package net.ximias.effects.EffectViews.Scenes;

import javafx.scene.paint.Color;
import org.json.JSONObject;

public interface SceneConstants {
	Color INDAR = new Color(1,0.8,0.7,1.0);
	//public static final Color INDAR = new Color(0.0,0.75,0.6,1.0);
	Color ESAMIR = new Color(0.7,0.9,1,1.0);
	Color AMERISH = new Color(0.0,0.8,0.4,1.0);
	Color HOSSIN = new Color(0.7,0.9,0.1,1.0);
	Color OTHER = new Color(1.0,0.85,.75,1.0);
	//public static final Color OTHER = new Color(1.0,0,0,1.0);
	
	Color VS = new Color(0.4,0.0,1.0,1.0);
	Color TR = new Color(0.8,0.0,0.0,1.0);
	Color NC = new Color(0.2,0.2,0.9,1.0);
	Color MISSING = new Color(0.2,1.0,0.2,1.0);
	
	int VS_ID = 1;
	int NC_ID = 2;
	int TR_ID = 3;
	
	int INDAR_ID = 2;
	int HOSSIN_ID = 4;
	int AMERISH_ID = 6;
	int ESAMIR_ID = 8;
	
	String[] XIMIAS_IDS = {"8287548916321388337","5428653157989617089"};
	
	JSONObject EMPTY_JSON = new JSONObject("{}");
	
	String VERSION_NAME = "Experimental";
	String VERSION = "0.0.10";
}
