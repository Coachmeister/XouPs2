package net.ximias.EffectViews;

import javafx.scene.paint.Color;
import net.ximias.ColorEffect;
import net.ximias.Effect;
import net.ximias.EffectView;
import net.ximias.EventColorEffect;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Debug view. Used for displaying the colors in the console.
 */
public class ConsoleView implements EffectView {
	ArrayList<ColorEffect> effects;
	
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
		if (effect instanceof ColorEffect){
			System.out.println("effect Added size: "+effects.size());
			System.out.println(((ColorEffect) effect).isDone());
			effects.add((ColorEffect) effect);
		}
	}
	
	private synchronized Color getColorAndClearFinishedEffects(){
		double r, g, b, a;
		r=g=b=a=0;
		effects.removeIf(ColorEffect::isDone);
		for (ColorEffect effect : effects) {
			
			r += effect.getColor().getRed()*effect.getColor().getOpacity();
			g += effect.getColor().getGreen()*effect.getColor().getOpacity();
			b += effect.getColor().getBlue()*effect.getColor().getOpacity();
			a += effect.getColor().getOpacity();
		}
		return Color.color(r/a,g/a,b/a);
	}
	
	public static void main(String[] args) {
		ConsoleView view = new ConsoleView();
		EventColorEffect effect = new EventColorEffect(0,Color.BLUE);
		view.addEffect(effect);
		
		Color yellow = Color.color(0.5,0.5,0,0.5);
		ColorEffect effect1 = new ColorEffect(1300, yellow);
		view.addEffect(effect1);
		
		
	}
	
}
