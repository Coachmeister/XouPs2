package net.ximias.effect.views.scenes;

import javafx.scene.paint.Color;
import net.ximias.effect.EffectView;
import net.ximias.effect.producers.EventEffectProducer;
import net.ximias.effect.producers.FadingEffectProducer;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.Condition;
import net.ximias.psEvent.condition.ConditionDataSource;
import net.ximias.psEvent.condition.SingleCondition;
import net.ximias.psEvent.condition.EventData;
import net.ximias.psEvent.handler.Ps2EventType;
import net.ximias.psEvent.handler.SingleEventHandler;

public class LoginEventsScene implements EffectScene {
	private final EffectView view;
	
	public LoginEventsScene(EffectView view) {
		this.view = view;
		simple();
	}
	
	private void simple(){
		
		FadingEffectProducer white = new FadingEffectProducer(Color.WHITE,2100);
		FadingEffectProducer red = new FadingEffectProducer(Color.RED,2100);
		
		EventData c13 = new EventData("13", ConditionDataSource.CONSTANT);
		EventData c1 = new EventData("1", ConditionDataSource.CONSTANT);
		EventData world = new EventData("world_id", ConditionDataSource.EVENT);
		
		SingleCondition worldIs1 = new SingleCondition(Condition.EQUALS, c1, world);
		SingleCondition worldIs13 = new SingleCondition(Condition.EQUALS, c13, world);
		
		SingleEventHandler world1 = new SingleEventHandler(view, white, worldIs1, Ps2EventType.WORLD, "PlayerLogin", "is1");
		SingleEventHandler world13 = new SingleEventHandler(view, red, worldIs13, Ps2EventType.WORLD, "PlayerLogin", "is13");
		
		Ps2EventStreamingConnection connection = new Ps2EventStreamingConnection();
		
		
		Color background = new Color(0.45f,0.65f,0.8f,0.05);
		EventEffectProducer eep = new EventEffectProducer(background,"background");
		view.addEffect(eep.build());
		
		world1.register(connection);
		world13.register(connection);
		
		
	}
}
