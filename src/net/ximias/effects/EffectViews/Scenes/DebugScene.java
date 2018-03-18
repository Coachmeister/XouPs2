package net.ximias.effects.EffectViews.Scenes;

import javafx.scene.paint.Color;
import net.ximias.effects.EffectView;
import net.ximias.effects.impl.*;
import net.ximias.network.CurrentPlayer;
import net.ximias.psEvent.handler.MultiEventHandler;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class DebugScene implements EffectScene {
	EffectView view;
	int count =0;
	
	public DebugScene(EffectView view) {
		this.view = view;
		System.out.println("debug scene started");
		
		Color darkblue = SceneConstants.INDAR.deriveColor(0.0,1.0,1.0,0.1);
		EventEffectProducer effect = new EventEffectProducer( darkblue,"blue");
		view.addEffect(effect.build());
		
		new Timer(true).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				count++;
				//heal();
				//kill();
				//revive();
				//death();
				//vehicleKilled();
				//battleRankUp();
				//experience();
				//metagame();
			}
		},2000,800);
		
	}
	private void achievement(){
		TimedEffectProducer start = new TimedEffectProducer(Color.YELLOW,600);
		FadingEffectProducer end = new FadingEffectProducer(Color.YELLOW,1000);
		MultiEffectProducer ribbon = new MultiEffectProducer(start, end);
		view.addEffect(ribbon.build());
	}
	
	private void metagame(){
		Color alert = bias(Color.RED,0.1);
		FadingEffectProducer whoop = new FadingEffectProducer(alert, 300);
		
		MultiEffectProducer alertEffect = new MultiEffectProducer(whoop, whoop, whoop, whoop, whoop);
		
		view.addEffect(alertEffect.build());
	}
	
	private void facility(){
		Color mutedFaction = bias(SceneConstants.VS,0.2); // Use facility faction color
		//Maybe use the world event.
		
		TimedEffectProducer facilityBegin = new TimedEffectProducer(mutedFaction, 400);
		FadingEffectProducer facilityfade = new FadingEffectProducer(mutedFaction, 500);
		MultiEffectProducer facility = new MultiEffectProducer(facilityBegin, facilityfade);
		view.addEffect(facility.build());
	}
	
	private void battleRankUp(){
		BlendingEffectProducer start = new BlendingEffectProducer(Color.YELLOW, Color.ORANGE, 500);
		TimedEffectProducer middle = new TimedEffectProducer(Color.ORANGE, 1000);
		FadingEffectProducer end = new FadingEffectProducer(Color.ORANGE, 800);
		MultiEffectProducer brup = new MultiEffectProducer(start, middle, end);
		view.addEffect(brup.build());
	}
	
	private void experience(){
		Color expColor = bias(Color.DARKCYAN, 0.1);
		BlendingEffectProducer fadein = new BlendingEffectProducer(Color.TRANSPARENT, expColor, 100);
		FadingEffectProducer fadeout = new FadingEffectProducer(expColor, 200);
		MultiEffectProducer exp = new MultiEffectProducer(fadein, fadeout);
		view.addEffect(exp.build());
	}
	
	private Color bias(Color color,double bias){
		return color.deriveColor(0,1,1,bias);
	}
	
	private void spawn(){
		Color spawnOrange = bias(new Color(1,0.65,0.2,1),0.1);
		FadingEffectProducer spawn = new FadingEffectProducer(spawnOrange, 300);
		view.addEffect(spawn.build());
	}
	
	private void vehicleKilled(){
		Color ex1 = new Color(1,1,0.3,1);
		Color ex2 = new Color(1,0.6,0,1);
		Color ex3 = new Color(1,0.3,0,1);
		
		BlendingEffectProducer e0 = new BlendingEffectProducer(Color.WHITE, ex1,100);
		BlendingEffectProducer e1 = new BlendingEffectProducer(ex1, ex2, 100);
		BlendingEffectProducer e2 = new BlendingEffectProducer(ex2,ex3, 100);
		FadingEffectProducer e3 = new FadingEffectProducer(ex3,1000);
		
		MultiEffectProducer explosion = new MultiEffectProducer(e0,e1,e2,e1,e2,e1,e2,e3);
		view.addEffect(explosion.build());
	}
	
	private void vehicleDied(){
		TimedEffectProducer vehicleBegin = new TimedEffectProducer(Color.WHITE, 600);
		FadingEffectProducer vehicleDestroy = new FadingEffectProducer(Color.WHITE, 1000);
		MultiEffectProducer vehicleDied = new MultiEffectProducer(vehicleBegin, vehicleDestroy);
		view.addEffect(vehicleDied.build());
	}
	
	private void multiKill(){
		TimedEffectProducer delay = new TimedEffectProducer(Color.TRANSPARENT,300);
		BlendingEffectProducer pent = new BlendingEffectProducer(Color.ORANGE, Color.YELLOW,100);
		BlendingEffectProducer pentrev = new BlendingEffectProducer(Color.YELLOW, Color.ORANGE,100);
		
		FadingEffectProducer fadeout = new FadingEffectProducer(Color.ORANGE, 200);
		view.addEffect(new MultiEffectProducer(delay ,pent, pentrev, fadeout).build());
	}
	
	private void kill(){
		FadingEffectProducer killEffect = new FadingEffectProducer(Color.WHITE, 500);
		FadingEffectProducer headEffect = new FadingEffectProducer(Color.LIGHTYELLOW, 500);
		FadingEffectProducer teamKillEffect = new FadingEffectProducer(Color.HOTPINK,500);
		FadingEffectProducer VSKillEnd = new FadingEffectProducer(SceneConstants.VS, 300);
		FadingEffectProducer NCKillEnd = new FadingEffectProducer(SceneConstants.NC, 300);
		FadingEffectProducer TRKillEnd = new FadingEffectProducer(SceneConstants.TR, 300);
		TimedEffectProducer blank = new TimedEffectProducer(Color.TRANSPARENT, 100);
		
		MultiEffectProducer VSKill = new MultiEffectProducer(blank, VSKillEnd);
		MultiEffectProducer NCKill = new MultiEffectProducer(blank, NCKillEnd);
		MultiEffectProducer TRKill = new MultiEffectProducer(blank, TRKillEnd);
		
		view.addEffect(killEffect.build());
		view.addEffect(NCKill.build());
	}
	
	private void headshot(){
		Color lightOrange = new Color(1,0.8,0.5,1);
		FadingEffectProducer headEffect = new FadingEffectProducer(lightOrange, 500);
		FadingEffectProducer NCKillEnd = new FadingEffectProducer(SceneConstants.NC, 300);
		TimedEffectProducer blank = new TimedEffectProducer(Color.TRANSPARENT, 100);
		MultiEffectProducer NCKill = new MultiEffectProducer(blank, NCKillEnd);
		
		view.addEffect(headEffect.build());
		view.addEffect(NCKill.build());
	}
	
	private void revive(){
		Color reviveGreen = new Color(0.0,1,0.2,1);
		FadingEffectProducer revive = new FadingEffectProducer(reviveGreen, 1000);
		view.addEffect(revive.build());
	}
	
	private void heal(){
		Color healGreen = bias(new Color(0,0.95,0.1,1),0.1);
		BlendingEffectProducer fadeing = new BlendingEffectProducer(Color.TRANSPARENT, healGreen, 250);
		FadingEffectProducer heal = new FadingEffectProducer(healGreen, 250);
		MultiEffectProducer healingEffect = new MultiEffectProducer(fadeing, heal);
		view.addEffect(healingEffect.build());
	}
	
	private void repair(){
		Color repair = bias(new Color(0.6,0.7,1,1),0.1);
		FadingEffectProducer repairing = new FadingEffectProducer(repair, 600);
		view.addEffect(repairing.build());
	}
	
	private void death(){
		
		BlendingEffectProducer teamDeathFade = new BlendingEffectProducer(SceneConstants.NC,Color.BLACK,1000);
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
