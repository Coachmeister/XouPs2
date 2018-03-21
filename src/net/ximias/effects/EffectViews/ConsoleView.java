package net.ximias.effects.EffectViews;

import javafx.scene.paint.Color;
import net.ximias.effects.impl.EventEffectProducer;
import net.ximias.effects.impl.TimedEffectProducer;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Debug view. Used for displaying the colors in the console.
 */
public class ConsoleView implements EffectView {
	private final ArrayList<Effect> effects;
	
	public ConsoleView(){
		effects = new ArrayList<>(60);
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				System.out.println(getColorAndClearFinishedEffects());
			}
		},200,500);
	}
	
	@Override
	public synchronized void addEffect(Effect effect) {
		effects.add(effect);
		System.out.println("Effect added. current effect size: "+effects.size());
	}
	
	@Override
	public double getEffectIntensity() {
		return 1;
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
	
	public static void main(String[] args) {
		ConsoleView view = new ConsoleView();
		
		EventEffectProducer effect = new EventEffectProducer( Color.BLUE,"blue");
		view.addEffect(effect.build());
		
		Color white = Color.color(1.0,1.0,1.0,1.0);
		TimedEffectProducer effect1 = new TimedEffectProducer(white,1300);
		view.addEffect(effect1.build());
	}
	
}
