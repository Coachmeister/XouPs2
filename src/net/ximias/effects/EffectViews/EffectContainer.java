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
		effects.add(effect);
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
		return Color.color(r/a,g/a,b/a);
	}
	
}
