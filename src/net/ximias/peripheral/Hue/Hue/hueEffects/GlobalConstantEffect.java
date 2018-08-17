package net.ximias.peripheral.Hue.Hue.hueEffects;

import com.philips.lighting.hue.sdk.wrapper.entertainment.Area;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.AreaEffect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import javafx.scene.paint.Color;

import java.util.HashMap;
import net.ximias.logging.Logger;


public class GlobalConstantEffect implements HueEffect {
	private Color color;
	private String name;
	private static final HashMap<String, AreaEffect> constantEffects = new HashMap<>();
	private double opacityMultiplier;
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	public GlobalConstantEffect(Color color, String name) {
		this.color = color;
		this.name = name;
	}
	
	public void setColor(Color color) {
		AreaEffect thisEffect = constantEffects.get(name);
		logger.info("Color of "+name+" changed to: "+color);
		if (thisEffect != null) {
			thisEffect.setFixedColor(new com.philips.lighting.hue.sdk.wrapper.entertainment.Color(color.getRed(), color.getGreen(), color.getBlue()));
			thisEffect.setFixedOpacity(color.getOpacity() * opacityMultiplier);
		}
		this.color = color;
	}
	
	@Override
	public synchronized Effect getEffect() {
		AreaEffect thisEffect = constantEffects.get(name);
		if (thisEffect != null) {
			setColor(color);
			return thisEffect;
		}
		thisEffect = new AreaEffect();
		thisEffect.setArea(Area.Predefine.All);
		thisEffect.setFixedColor(new com.philips.lighting.hue.sdk.wrapper.entertainment.Color(color.getRed(), color.getGreen(), color.getBlue()));
		thisEffect.setFixedOpacity(color.getOpacity()*opacityMultiplier);
		constantEffects.put(name, thisEffect);
		return thisEffect;
	}
	
	@Override
	public void setOpacity(double opacity) {
		opacityMultiplier = opacity;
		setColor(color);
	}
	
	@Override
	public int getDuration() {
		return -1;
	}
}
