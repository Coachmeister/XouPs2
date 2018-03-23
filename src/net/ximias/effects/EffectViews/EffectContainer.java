package net.ximias.effects.EffectViews;

import javafx.scene.paint.Color;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectView;

import java.util.ArrayList;
import java.util.logging.Logger;

public class EffectContainer implements EffectView{
	private final ArrayList<Effect> effects;
	private final Logger logger = Logger.getLogger(getClass().getName());
	private double effectIntensity;
	
	private EffectContainer() {
		effects = new ArrayList<>(80);
	}
	
	public EffectContainer(double effectIntensity) {
		this();
		this.effectIntensity = effectIntensity;
	}
	
	public synchronized void addEffect(Effect effect) {
		if (!effects.contains(effect)){
			effects.add(effect);
		}
		if (effect.getColor().getOpacity()==0){
			logger.info("Added effect is fully transparent");
		}
		logger.info(effect.getClass().getName()+" added. current effect size: "+effects.size());
		logger.fine("Current color: "+getColorAndClearFinishedEffects());
	}
	
	@Override
	public double getEffectIntensity() {
		return effectIntensity;
	}
	
	public Color getColor(){
		return getColorAndClearFinishedEffects();
	}
	
	private synchronized Color getColorAndClearFinishedEffects(){
		double r, g, b, a;
		r=g=b=a=0;
		effects.removeIf(Effect::isDone);
		for (Effect effect : effects) {
			
			r += effect.getColor().getRed()*effect.getColor().getOpacity() * (effect.hasIntensity() ? effectIntensity : 1);
			g += effect.getColor().getGreen()*effect.getColor().getOpacity() * (effect.hasIntensity() ? effectIntensity : 1);
			b += effect.getColor().getBlue()*effect.getColor().getOpacity() * (effect.hasIntensity() ? effectIntensity : 1);
			a += effect.getColor().getOpacity() * (effect.hasIntensity() ? effectIntensity : 1);
		}
		
		Color result = Color.color(Math.min(r / a, 1.0), Math.min(g / a, 1.0), Math.min(b / a, 1.0));
		if (result.equals(Color.BLACK)||result.getOpacity() == 0){
			logger.warning("Color is fully black or transparent");
		}
		return result;
	}
	
	@Override
	public String toString() {
		final StringBuilder effectsNames = new StringBuilder();
		effects.forEach(it-> effectsNames.append(it.getClass().getSimpleName()).append(" color: ").append(it.getColor()).append("\n"));
		return "Effects: "+effectsNames.toString();
	}
	
	public void setEffectIntensity(double effectIntensity) {
		this.effectIntensity = effectIntensity;
	}
}
