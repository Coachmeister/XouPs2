package net.ximias.effects.EffectViews.Scenes;

import javafx.scene.paint.Color;
import net.ximias.effects.EffectView;
import net.ximias.effects.impl.*;
import net.ximias.network.CurrentPlayer;

import java.util.Timer;
import java.util.TimerTask;

public class DebugScene implements EffectScene {
	EffectView view;
	
	public DebugScene(EffectView view) {
		this.view = view;
		System.out.println("debug scene started");
		
		Color darkblue = Color.color(0.45,0.65,0.8,0.1);
		EventEffectProducer effect = new EventEffectProducer( darkblue,"blue");
		view.addEffect(effect.build());
		
		FadingEffectProducer killEffect = new FadingEffectProducer(Color.WHITE, 500);
		FadingEffectProducer teamKillEffect = new FadingEffectProducer(Color.HOTPINK,500);
		FadingEffectProducer VSKillEnd = new FadingEffectProducer(SceneConstants.VS, 300);
		FadingEffectProducer NCKillEnd = new FadingEffectProducer(SceneConstants.NC, 300);
		FadingEffectProducer TRKillEnd = new FadingEffectProducer(SceneConstants.TR, 300);
		TimedEffectProducer blank = new TimedEffectProducer(Color.TRANSPARENT, 100);
		
		MultiEffectProducer VSKill = new MultiEffectProducer(blank, VSKillEnd);
		MultiEffectProducer NCKill = new MultiEffectProducer(blank, NCKillEnd);
		MultiEffectProducer TRKill = new MultiEffectProducer(blank, TRKillEnd);
		
		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				death();
				
			}
		},2000);
		
	}
	
	private void death(){
		BlendingEffectProducer teamDeathFade = new BlendingEffectProducer(SceneConstants.TR,Color.BLACK,1000);
		FadingEffectProducer fadeout = new FadingEffectProducer(Color.BLACK, 500);
		TimedEffectProducer black = new TimedEffectProducer(Color.BLACK, 1000);
		MultiEffectProducer teamDeath = new MultiEffectProducer(
				teamDeathFade,
				black,
				fadeout
		);
		
		view.addEffect(teamDeath.build());
	}
	
}
