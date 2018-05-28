package net.ximias.effect;

import javafx.scene.effect.Blend;
import javafx.scene.paint.Color;

/**
 * A colorful effect.
 */
public interface Effect {
	Color getColor();
	
	/**
	 * Should return true when the effect should be disposed of.
	 * @return true, when the effect has completed, or should be terminated.
	 */
	boolean isDone();
	
	/**
	 * Used to determine if the effect is a background color, or an effect color.
	 * @return true, if the effect should be affected by the effectIntensity property in EffectContainer.
	 */
	boolean hasIntensity();
	
	/**
	 * Used to obtain the name of the effect. Mostly used for debugging purposes.
	 * @return the name of the effect. Defaults to the class name.
	 */
	default String getName(){
		return getClass().getSimpleName();
	}
	
	/**
	 * Used to blend multiple effects together.
	 * @param effectIntensity the intensity (transparency) of effects with the hasIntensity property set.
	 * @param effects The effects to blend together.
	 * @return a single color, representing the blending of all effects.
	 */
	static Color blend(double effectIntensity, Effect ... effects){
		double r, g, b, a;
		r = g = b = a = 0;
		for (Effect effect : effects) {
			r += effect.getColor().getRed() * effect.getColor().getOpacity() * (effect.hasIntensity() ? effectIntensity : 1);
			g += effect.getColor().getGreen() * effect.getColor().getOpacity() * (effect.hasIntensity() ? effectIntensity : 1);
			b += effect.getColor().getBlue() * effect.getColor().getOpacity() * (effect.hasIntensity() ? effectIntensity : 1);
			a += effect.getColor().getOpacity() * (effect.hasIntensity() ? effectIntensity : 1);
		}
		return Color.color(Math.min(r / a, 1.0), Math.min(g / a, 1.0), Math.min(b / a, 1.0));
	}
	
	EffectProducer getProducer();
}
