package net.ximias.persistence;

import java.io.Serializable;

public class Persisted implements Serializable {
	public static Persisted getInstance(){
		return PersistLoader.getInstance();
	}
	
	public Persisted defaults() {
		LAST_LOGIN = "ximias";
		BACKGROUND_BRIGHTNESS_SLIDER = 0.5;
		BACKGROUND_TRANSPARENCY_SLIDER = 0.1;
		EFFECT_TRANSPARENCY_SLIDER = 1;
		APPLICATION_WIDTH = 600;
		APPLICATION_HEIGHT = 450;
		return this;
	}
	
	public String LAST_LOGIN;
	public double BACKGROUND_BRIGHTNESS_SLIDER;
	public double BACKGROUND_TRANSPARENCY_SLIDER;
	public double EFFECT_TRANSPARENCY_SLIDER;
	public double APPLICATION_WIDTH;
	public double APPLICATION_HEIGHT;
}
