package net.ximias.effects.EffectViews;

import javafx.scene.paint.Color;
import net.ximias.effects.impl.TimedColorEffect;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectView;
import net.ximias.effects.impl.EventColorEffect;
import net.ximias.effects.impl.TimedFadingColorAnimation;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Debug view. Used for displaying the colors in the console.
 */
public class ConsoleView implements EffectView {
	private ArrayList<Effect> effects;
	
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
		
		EventColorEffect effect = new EventColorEffect(Color.BLUE);
		view.addEffect(effect);
		
		Color yellow = Color.color(0.5,0.5,0,0.5);
		TimedColorEffect effect1 = new TimedColorEffect(1300, yellow);
		view.addEffect(effect1);
		
		view.addEffect(new TimedFadingColorAnimation(Color.WHITE,3000));
	}
	
}
