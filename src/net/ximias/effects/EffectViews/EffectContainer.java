package net.ximias.effects.EffectViews;

import javafx.scene.paint.Color;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectView;

import java.util.ArrayList;

public class EffectContainer implements EffectView{
	private ArrayList<Effect> effects;
	
	public EffectContainer() {
		effects = new ArrayList<>(80);
	}
	
	public synchronized void addEffect(Effect effect) {
		if (!effects.contains(effect)){
			effects.add(effect);
		}
		System.out.println("Effect added. current effect size: "+effects.size());
		System.out.println("Current color: "+getColorAndClearFinishedEffects());
	}
	
	public Color getColor(){
		return getColorAndClearFinishedEffects();
	}
	
	private synchronized Color getColorAndClearFinishedEffects(){
		double r, g, b, a;
		r=g=b=a=0;
		effects.removeIf(Effect::isDone);
		for (Effect effect : effects) {
			
			r += effect.getColor().getRed()*effect.getColor().getOpacity();
			g += effect.getColor().getGreen()*effect.getColor().getOpacity();
			b += effect.getColor().getBlue()*effect.getColor().getOpacity();
			a += effect.getColor().getOpacity();
		}
		return Color.color(Math.min(r/a,1.0),Math.min(g/a,1.0),Math.min(b/a,1.0));
	}
	
	@Override
	public String toString() {
		final StringBuilder effectsNames = new StringBuilder();
		effects.forEach(it-> effectsNames.append(it.getClass().getSimpleName()).append(" color: ").append(it.getColor()).append("\n"));
		return "Effects: "+effectsNames.toString();
	}
}
