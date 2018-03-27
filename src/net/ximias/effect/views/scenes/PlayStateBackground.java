package net.ximias.effect.views.scenes;

import javafx.scene.paint.Color;
import net.ximias.effect.EffectView;
import net.ximias.effect.producers.EventEffectProducer;
import net.ximias.effect.producers.FadingEffectProducer;
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
		esamir = new EventEffectProducer(background(ApplicationConstants.ESAMIR),"background");
		amerish = new EventEffectProducer(background(ApplicationConstants.AMERISH),"background");
		indar = new EventEffectProducer(background(ApplicationConstants.INDAR),"background");
		hossin = new EventEffectProducer(background(ApplicationConstants.HOSSIN),"background");
		EventEffectProducer other = new EventEffectProducer(ApplicationConstants.OTHER, "background");
		
		SingleCondition isEsamir = new SingleCondition(Condition.EQUALS, new EventData("zone_id", ConditionDataSource.PLAYER), new EventData(String.valueOf(ApplicationConstants.ESAMIR_ID), ConditionDataSource.CONSTANT));
		SingleCondition isAmerish = new SingleCondition(Condition.EQUALS, new EventData("zone_id", ConditionDataSource.PLAYER), new EventData(String.valueOf(ApplicationConstants.AMERISH_ID), ConditionDataSource.CONSTANT));
		SingleCondition isIndar = new SingleCondition(Condition.EQUALS, new EventData("zone_id", ConditionDataSource.PLAYER), new EventData(String.valueOf(ApplicationConstants.INDAR_ID), ConditionDataSource.CONSTANT));
		SingleCondition isHossin = new SingleCondition(Condition.EQUALS, new EventData("zone_id", ConditionDataSource.PLAYER), new EventData(String.valueOf(ApplicationConstants.HOSSIN_ID), ConditionDataSource.CONSTANT));
		
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
		esamir.setColor(background(ApplicationConstants.ESAMIR));
		amerish.setColor(background(ApplicationConstants.AMERISH));
		indar.setColor(background(ApplicationConstants.INDAR));
		hossin.setColor(background(ApplicationConstants.HOSSIN));
		logoutFade.setColor(Color.BLACK.deriveColor(0,1,1,1.0-backgroundBrightness));
		updateHandlers();
	}
	
	public void updateHandlers(){
		JSONObject characterJson = ApplicationConstants.EMPTY_JSON;
		characterJson.put("character_id", CurrentPlayer.getInstance().getPlayerID());
		esamirHandler.eventReceived(ApplicationConstants.EMPTY_JSON);
		amerishHandler.eventReceived(ApplicationConstants.EMPTY_JSON);
		indarHandler.eventReceived(ApplicationConstants.EMPTY_JSON);
		hossinHandler.eventReceived(ApplicationConstants.EMPTY_JSON);
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
