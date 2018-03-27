package net.ximias.persistence;

import net.ximias.effect.views.scenes.ApplicationConstants;

import java.io.Serializable;

public class Persisted implements Serializable {
	public static Persisted getInstance(){
		return PersistLoader.getInstance();
	}
	
	public Persisted defaults() {
		LAST_LOGIN = "ximias";
		BACKGROUND_BRIGHTNESS_SLIDER = ApplicationConstants.DEFAULT_BACKGROUND_BRIGHTENS;
		BACKGROUND_TRANSPARENCY_SLIDER = ApplicationConstants.DEFAULT_BACKGROUND_INTENSITY;
		EFFECT_TRANSPARENCY_SLIDER = ApplicationConstants.DEFAULT_EFFECT_INTENSITY;
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
