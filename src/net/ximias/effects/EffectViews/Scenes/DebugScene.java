package net.ximias.effects.EffectViews.Scenes;

import javafx.scene.paint.Color;
import net.ximias.effects.EffectView;
import net.ximias.effects.impl.EventEffectProducer;
import net.ximias.effects.impl.FadingEffectProducer;
import net.ximias.effects.impl.TimedEffectProducer;

public class DebugScene implements EffectScene {
	EffectView view;
	
	public DebugScene(EffectView view) {
		this.view = view;
		System.out.println("debug scene started");
		
		Color darkblue = Color.color(0.0,0.0,1.0,0.2);
		EventEffectProducer effect = new EventEffectProducer( darkblue,"blue");
		view.addEffect(effect.build());
		
		Color white = Color.color(1.0,1.0,1.0,1.0);
		FadingEffectProducer effect1 = new FadingEffectProducer( white, 1300);
		view.addEffect(effect1.build());
		
	}
	
	
}
