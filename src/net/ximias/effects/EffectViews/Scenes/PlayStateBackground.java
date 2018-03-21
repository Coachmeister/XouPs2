package net.ximias.effects.EffectViews.Scenes;

import javafx.scene.paint.Color;
import net.ximias.effects.EffectView;
import net.ximias.effects.impl.EventEffectProducer;
import net.ximias.effects.impl.FadingEffectProducer;
import net.ximias.network.CurrentPlayer;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.*;
import net.ximias.psEvent.handler.GlobalHandler;
import net.ximias.psEvent.handler.Ps2EventType;
import net.ximias.psEvent.handler.SingleEventHandler;

import java.util.logging.Logger;

public class PlayStateBackground {
	private Logger logger = Logger.getLogger(getClass().getName());
	private EffectView view;
	private Ps2EventStreamingConnection connection;
	private double backgroundBrightness;
	private double backgroundIntensity;
	
	private EventEffectProducer esamir;
	private EventEffectProducer amerish;
	private EventEffectProducer indar;
	private EventEffectProducer hossin;
	private EventEffectProducer other;
	private FadingEffectProducer logoutFade;
	
	private SingleCondition isEsamir;
	private SingleCondition isAmerish;
	private SingleCondition isIndar;
	private SingleCondition isHossin;
	private EventCondition isNone;
	
	private GlobalHandler esamirHandler;
	private GlobalHandler amerishHandler;
	private GlobalHandler indarHandler;
	private GlobalHandler hossinHandler;
	private GlobalHandler noneHandler;
	
	private SingleCondition isPlayer = new SingleCondition(Condition.EQUALS,
			new EventData(CurrentPlayer.getInstance().getPlayerID(),ConditionDataSource.CONSTANT),
			new EventData("character_id", ConditionDataSource.EVENT)
	);
	
	public PlayStateBackground(EffectView view, Ps2EventStreamingConnection connection, double backgroundIntensity, double backgroundBrightness) {
		this.view = view;
		this.connection = connection;
		this.backgroundIntensity = backgroundIntensity;
		this.backgroundBrightness = backgroundBrightness;
		init();
	}
	
	private void init(){
		esamir = new EventEffectProducer(background(SceneConstants.ESAMIR),"background");
		amerish = new EventEffectProducer(background(SceneConstants.AMERISH),"background");
		indar = new EventEffectProducer(background(SceneConstants.INDAR),"background");
		hossin = new EventEffectProducer(background(SceneConstants.HOSSIN),"background");
		other = new EventEffectProducer(SceneConstants.OTHER,"background");
		
		isEsamir = new SingleCondition(Condition.EQUALS, new EventData("zone_id", ConditionDataSource.PLAYER),new EventData(String.valueOf(SceneConstants.ESAMIR_ID),ConditionDataSource.CONSTANT));
		isAmerish = new SingleCondition(Condition.EQUALS, new EventData("zone_id",ConditionDataSource.PLAYER),new EventData(String.valueOf(SceneConstants.AMERISH_ID),ConditionDataSource.CONSTANT));
		isIndar = new SingleCondition(Condition.EQUALS, new EventData("zone_id",ConditionDataSource.PLAYER),new EventData(String.valueOf(SceneConstants.INDAR_ID),ConditionDataSource.CONSTANT));
		isHossin = new SingleCondition(Condition.EQUALS, new EventData("zone_id",ConditionDataSource.PLAYER),new EventData(String.valueOf(SceneConstants.HOSSIN_ID),ConditionDataSource.CONSTANT));
		
		// if                            all(not any continents), (isPlayer)
		isNone = new AllCondition(new NotCondition(new AnyCondition(isEsamir, isAmerish, isHossin, isIndar)), isPlayer);
		
		
		AllCondition isPlayerInEsamir = new AllCondition(isPlayer, isEsamir);
		AllCondition isPlayerInAmerish = new AllCondition(isPlayer, isAmerish);
		AllCondition isPlayerInIndar = new AllCondition(isPlayer, isIndar);
		AllCondition isPlayerInHossin = new AllCondition(isPlayer, isHossin);
		
		esamirHandler = new GlobalHandler(isPlayerInEsamir, esamir, view);
		amerishHandler = new GlobalHandler(isPlayerInAmerish, amerish, view);
		indarHandler = new GlobalHandler(isPlayerInIndar, indar, view);
		hossinHandler = new GlobalHandler(isPlayerInHossin, hossin, view);
		noneHandler = new GlobalHandler(isNone, other, view);
		
		esamirHandler.register(connection);
		amerishHandler.register(connection);
		indarHandler.register(connection);
		hossinHandler.register(connection);
		noneHandler.register(connection);
		
		view.addEffect(other.build());
		logout();
	}
	
	
	private void background() {
		esamir.setColor(background(SceneConstants.ESAMIR));
		amerish.setColor(background(SceneConstants.AMERISH));
		indar.setColor(background(SceneConstants.INDAR));
		hossin.setColor(background(SceneConstants.HOSSIN));
		logoutFade.setColor(Color.BLACK.deriveColor(0,1,1,1.0-backgroundBrightness));
	}
	
	private void logout(){
		logoutFade = new FadingEffectProducer(Color.BLACK.deriveColor(0,1,1,backgroundBrightness),8000);
		SingleEventHandler logout = new SingleEventHandler(view, logoutFade, isPlayer, Ps2EventType.PLAYER, "PlayerLogout", "Logout fade");
		logout.register(connection);
	}
	
	
	public synchronized void intensityChanged(double backgroundBrightness, double backgroundIntensity){
		logger.info("Intensity changed");
		this.backgroundBrightness = backgroundBrightness;
		this.backgroundIntensity = backgroundIntensity;
		background();
	}
	
	private Color background(Color color){
		Color result = color.deriveColor(0, 1, backgroundBrightness, backgroundIntensity);
		if (result.getOpacity() < 0.0009) logger.warning("Background is extremely transparent: "+result.getOpacity());
		return result;
	}
}
