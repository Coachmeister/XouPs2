package net.ximias.effect.views.scenes;

import javafx.scene.paint.Color;
import net.ximias.effect.EffectView;
import net.ximias.effect.producers.EventEffectProducer;
import net.ximias.effect.producers.FadingEffectProducer;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.persistence.Persisted;

import java.util.Timer;
import java.util.TimerTask;

public class DebugScene extends EffectScene {
	private final EffectView view;
	private int count = 0;
	
	public DebugScene(EffectView view, Ps2EventStreamingConnection con) {
		super(view, con);
		this.view = view;
		System.out.println("debug scene started");
		
		Color darkblue = Persisted.getInstance().INDAR.deriveColor(0.0, 1.0, 1.0, 0.1);
		EventEffectProducer effect = new EventEffectProducer(darkblue, "blue");
		view.addEffect(effect.build());
		
		new Timer("Recurring test effects (only for debug)", true).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				count++;
				spawn();
				//kill();
				//revive();
				//death();
				//vehicleKilled();
				//battleRankUp();
				//experience();
				//metagame();
			}
		}, 2000, 800);
		
	}
	
	private Color bias(Color color, double bias) {
		return color.deriveColor(0, 1, 1, bias);
	}
	
	private void spawn() {
		Color spawnOrange = bias(new Color(1, 0.65, 0.2, 1), 0.1);
		FadingEffectProducer spawn = new FadingEffectProducer("Spawn effect", spawnOrange, 300);
		view.addEffect(spawn.build());
	}
}
