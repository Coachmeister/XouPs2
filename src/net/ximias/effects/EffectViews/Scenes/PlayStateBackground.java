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
import org.json.JSONObject;

import java.util.logging.Logger;

class PlayStateBackground {
	private final Logger logger = Logger.getLogger(getClass().getName());
	private final EffectView view;
	private final Ps2EventStreamingConnection connection;
	private double backgroundBrightness;
	private double backgroundIntensity;
	
	private GlobalHandler esamirHandler;
	private GlobalHandler amerishHandler;
	private GlobalHandler indarHandler;
	private GlobalHandler hossinHandler;
	private GlobalHandler noneHandler;
	
	
	private EventEffectProducer esamir;
	private EventEffectProducer amerish;
	private EventEffectProducer indar;
	private EventEffectProducer hossin;
	private FadingEffectProducer logoutFade;
	
	private final SingleCondition isPlayer = new SingleCondition(Condition.EQUALS,
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
		EventEffectProducer other = new EventEffectProducer(SceneConstants.OTHER, "background");
		
		SingleCondition isEsamir = new SingleCondition(Condition.EQUALS, new EventData("zone_id", ConditionDataSource.PLAYER), new EventData(String.valueOf(SceneConstants.ESAMIR_ID), ConditionDataSource.CONSTANT));
		SingleCondition isAmerish = new SingleCondition(Condition.EQUALS, new EventData("zone_id", ConditionDataSource.PLAYER), new EventData(String.valueOf(SceneConstants.AMERISH_ID), ConditionDataSource.CONSTANT));
		SingleCondition isIndar = new SingleCondition(Condition.EQUALS, new EventData("zone_id", ConditionDataSource.PLAYER), new EventData(String.valueOf(SceneConstants.INDAR_ID), ConditionDataSource.CONSTANT));
		SingleCondition isHossin = new SingleCondition(Condition.EQUALS, new EventData("zone_id", ConditionDataSource.PLAYER), new EventData(String.valueOf(SceneConstants.HOSSIN_ID), ConditionDataSource.CONSTANT));
		
		// if                            all(not any continents), (isPlayer)
		EventCondition isNone = new AllCondition(new NotCondition(new AnyCondition(isEsamir, isAmerish, isHossin, isIndar)), isPlayer);
		
		esamirHandler = new GlobalHandler(isEsamir, esamir, view);
		amerishHandler = new GlobalHandler(isAmerish, amerish, view);
		indarHandler = new GlobalHandler(isIndar, indar, view);
		hossinHandler = new GlobalHandler(isHossin, hossin, view);
		noneHandler = new GlobalHandler(isNone, other, view);
		
		esamirHandler.register(connection);
		amerishHandler.register(connection);
		indarHandler.register(connection);
		hossinHandler.register(connection);
		noneHandler.register(connection);
		
		view.addEffect(other.build());
		logout();
	}
	
	
	private void recalculateBackground() {
		esamir.setColor(background(SceneConstants.ESAMIR));
		amerish.setColor(background(SceneConstants.AMERISH));
		indar.setColor(background(SceneConstants.INDAR));
		hossin.setColor(background(SceneConstants.HOSSIN));
		logoutFade.setColor(Color.BLACK.deriveColor(0,1,1,1.0-backgroundBrightness));
		updateHandlers();
	}
	
	public void updateHandlers(){
		JSONObject characterJson = SceneConstants.EMPTY_JSON;
		characterJson.put("character_id", CurrentPlayer.getInstance().getPlayerID());
		esamirHandler.eventReceived(SceneConstants.EMPTY_JSON);
		amerishHandler.eventReceived(SceneConstants.EMPTY_JSON);
		indarHandler.eventReceived(SceneConstants.EMPTY_JSON);
		hossinHandler.eventReceived(SceneConstants.EMPTY_JSON);
		noneHandler.eventReceived(characterJson);
	}
	
	private void logout(){
		logoutFade = new FadingEffectProducer(Color.BLACK.deriveColor(0,1,1,backgroundBrightness),8000);
		SingleEventHandler logout = new SingleEventHandler(view, logoutFade, isPlayer, Ps2EventType.PLAYER, "PlayerLogout", "Logout fade");
		logout.register(connection);
	}
	
	
	public synchronized void intensityChanged(double backgroundBrightness, double backgroundIntensity){
		logger.finer("Intensity changed");
		this.backgroundBrightness = backgroundBrightness;
		this.backgroundIntensity = backgroundIntensity;
		recalculateBackground();
	}
	
	private Color background(Color color){
		Color result = color.deriveColor(0, 1, backgroundBrightness, backgroundIntensity);
		if (result.getOpacity() < 0.0009) logger.warning("Background is extremely transparent: "+result.getOpacity());
		return result;
	}
}
