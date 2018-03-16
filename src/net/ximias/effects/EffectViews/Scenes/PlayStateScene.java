package net.ximias.effects.EffectViews.Scenes;

import javafx.scene.paint.Color;
import net.ximias.effects.EffectView;
import net.ximias.effects.impl.EventEffectProducer;
import net.ximias.effects.impl.FadingEffectProducer;
import net.ximias.effects.impl.MultiEffectProducer;
import net.ximias.effects.impl.TimedEffectProducer;
import net.ximias.network.CurrentPlayer;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.*;
import net.ximias.psEvent.handler.GlobalHandler;
import net.ximias.psEvent.handler.Ps2EventType;
import net.ximias.psEvent.handler.SingleEventHandler;

import java.io.IOException;
import java.util.HashMap;

public class PlayStateScene implements EffectScene{
	EffectView view;
	Ps2EventStreamingConnection connection;
	
	public PlayStateScene(EffectView view) {
		this.view = view;
		try {
			magic();
		} catch (IOException e) {
			throw new Error("Error in setting up playstate");
		}
	}
	
	private void magic() throws IOException {
		connection = new Ps2EventStreamingConnection();
		background();
		death();
		kill();
		//First playtest
		
		/*multiKill();
		experience();
		headshot();
		//Play test
		
		reviving();
		healing();
		repairing();
		amsSpawn();
		vehicle();
		level();
		achievement();
		facility();
		// Play test
		
		alert();
		logging();
		killingXimias();
		//Alpha release
		
		*/
		
	}
	
	private void kill() {
		
		
		FadingEffectProducer killEffect = new FadingEffectProducer(Color.WHITE, 500);
		FadingEffectProducer teamKillEffect = new FadingEffectProducer(Color.HOTPINK,500);
		FadingEffectProducer VSKillEnd = new FadingEffectProducer(SceneConstants.VS, 300);
		FadingEffectProducer NCKillEnd = new FadingEffectProducer(SceneConstants.NC, 300);
		FadingEffectProducer TRKillEnd = new FadingEffectProducer(SceneConstants.TR, 300);
		TimedEffectProducer blank = new TimedEffectProducer(Color.TRANSPARENT, 100);
		
		MultiEffectProducer VSKill = new MultiEffectProducer(blank, VSKillEnd);
		MultiEffectProducer NCKill = new MultiEffectProducer(blank, NCKillEnd);
		MultiEffectProducer TRKill = new MultiEffectProducer(blank, TRKillEnd);
		
		
		SingleCondition isKill = new SingleCondition(Condition.EQUALS,
				new EventData(CurrentPlayer.getInstance().getPlayerID(),ConditionDataSource.CONSTANT),
				new EventData("attacker_character_id", ConditionDataSource.EVENT)
		);
		
		HashMap<String, String> eventPlayer = new HashMap<>(4);
		eventPlayer.put("character_id","character_id");
		
		SingleCondition isVS = new SingleCondition(Condition.EQUALS,
				new EventData(String.valueOf(SceneConstants.VS_ID),ConditionDataSource.CONSTANT),
				new CensusData("character", "faction_id", new HashMap<>(0),eventPlayer));
		
		SingleCondition isNC = new SingleCondition(Condition.EQUALS,
				new EventData(String.valueOf(SceneConstants.NC_ID),ConditionDataSource.CONSTANT),
				new CensusData("character", "faction_id", new HashMap<>(0),eventPlayer));
		
		SingleCondition isTR = new SingleCondition(Condition.EQUALS,
				new EventData(String.valueOf(SceneConstants.TR_ID),ConditionDataSource.CONSTANT),
				new CensusData("character", "faction_id", new HashMap<>(0),eventPlayer));
		
		SingleCondition isSame = new SingleCondition(Condition.EQUALS,
				new EventData("faction_id",ConditionDataSource.PLAYER),
				new CensusData("character", "faction_id", new HashMap<>(0),eventPlayer));
		
		SingleCondition isNotSame = new SingleCondition(Condition.NOT_EQUALS,
				new EventData("faction_id",ConditionDataSource.PLAYER),
				new CensusData("character", "faction_id", new HashMap<>(0),eventPlayer));
		
		AllCondition isTeamKill = new AllCondition(isKill, isSame);
		AllCondition isHonestKill = new AllCondition(isKill, isNotSame);
		AllCondition isVSKill = new AllCondition(isVS, isKill);
		AllCondition isNCKill = new AllCondition(isNC, isKill);
		AllCondition isTRKill = new AllCondition(isTR, isKill);
		
		SingleEventHandler teamKill = new SingleEventHandler(view , teamKillEffect, isTeamKill, Ps2EventType.PLAYER, "Death", "teamKill");
		SingleEventHandler honestKill = new SingleEventHandler(view,killEffect, isHonestKill, Ps2EventType.PLAYER,"Death","honestKill");
		SingleEventHandler factionVSKill = new SingleEventHandler(view, VSKill, isVSKill, Ps2EventType.PLAYER, "Death", "VSKill");
		SingleEventHandler factionNCKill = new SingleEventHandler(view, NCKill, isNCKill, Ps2EventType.PLAYER, "Death", "NCKill");
		SingleEventHandler factionTRKill = new SingleEventHandler(view, TRKill, isTRKill, Ps2EventType.PLAYER, "Death", "TRKill");
		
		teamKill.register(connection);
		honestKill.register(connection);
		factionVSKill.register(connection);
		factionNCKill.register(connection);
		factionTRKill.register(connection);
	}
	
	private void death() {
		FadingEffectProducer death = new FadingEffectProducer(Color.BLACK,2500);
		FadingEffectProducer teamDeathFade = new FadingEffectProducer(CurrentPlayer.getInstance().getFactionColor(),500);
		MultiEffectProducer teamDeath = new MultiEffectProducer(
				teamDeathFade,
				new TimedEffectProducer(Color.rgb(128,0,0), 250),
				teamDeathFade,
				new TimedEffectProducer(Color.rgb(64,0,0), 250),
				teamDeathFade,
				new FadingEffectProducer(Color.rgb(32,0,0), 250)
		);
		
		HashMap<String, String> eventPlayer = new HashMap<>(4);
		eventPlayer.put("character_id","attacker_character_id");
		
		SingleCondition isDeath = new SingleCondition(Condition.EQUALS,
				new EventData(CurrentPlayer.getInstance().getPlayerID(),ConditionDataSource.CONSTANT),
				new EventData("character_id", ConditionDataSource.EVENT)
		);
		
		SingleCondition isTeam = new SingleCondition(Condition.EQUALS, new CensusData("character","faction_id", new HashMap<>(0),eventPlayer),new EventData("faction_id",ConditionDataSource.PLAYER));
		SingleCondition isNotTeam = new SingleCondition(Condition.NOT_EQUALS, new CensusData("character","faction_id", new HashMap<>(0),eventPlayer),new EventData("faction_id",ConditionDataSource.PLAYER));
		
		AllCondition isTeamDeath = new AllCondition(isTeam, isDeath);
		AllCondition isNonTeamDeath = new AllCondition(isNotTeam, isDeath);
		
		SingleEventHandler deathHandler = new SingleEventHandler(view, death, isNonTeamDeath, Ps2EventType.PLAYER, "Death","death");
		SingleEventHandler factionDeathHandler = new SingleEventHandler(view, teamDeath, isTeamDeath, Ps2EventType.PLAYER, "Death", "teamDeath");
		
		deathHandler.register(connection);
		factionDeathHandler.register(connection);
	}
	
	private void background() {
		EventEffectProducer esamir = new EventEffectProducer(SceneConstants.ESAMIR.deriveColor(0,1,1,0.05),"background");
		EventEffectProducer amerish = new EventEffectProducer(SceneConstants.AMERISH.deriveColor(0,1,1,0.05),"background");
		EventEffectProducer indar = new EventEffectProducer(SceneConstants.INDAR.deriveColor(0,1,1,0.05),"background");
		EventEffectProducer hossin = new EventEffectProducer(SceneConstants.HOSSIN.deriveColor(0,1,1,0.05),"background");
		EventEffectProducer other = new EventEffectProducer(SceneConstants.OTHER.deriveColor(0,1,1,0.05),"background");
		
		SingleCondition isEsamir = new SingleCondition(Condition.EQUALS, new EventData("zone_id", ConditionDataSource.PLAYER),new EventData(String.valueOf(SceneConstants.ESAMIR_ID),ConditionDataSource.CONSTANT));
		SingleCondition isAmerish = new SingleCondition(Condition.EQUALS, new EventData("zone_id",ConditionDataSource.PLAYER),new EventData(String.valueOf(SceneConstants.AMERISH_ID),ConditionDataSource.CONSTANT));
		SingleCondition isIndar = new SingleCondition(Condition.EQUALS, new EventData("zone_id",ConditionDataSource.PLAYER),new EventData(String.valueOf(SceneConstants.INDAR_ID),ConditionDataSource.CONSTANT));
		SingleCondition isHossin = new SingleCondition(Condition.EQUALS, new EventData("zone_id",ConditionDataSource.PLAYER),new EventData(String.valueOf(SceneConstants.HOSSIN_ID),ConditionDataSource.CONSTANT));
		
		
		GlobalHandler esamirHandler = new GlobalHandler(isEsamir, esamir, view);
		GlobalHandler amerishHandler = new GlobalHandler(isAmerish, amerish, view);
		GlobalHandler indarHandler = new GlobalHandler(isIndar, indar, view);
		GlobalHandler hossinHandler = new GlobalHandler(isHossin, hossin, view);
		
		esamirHandler.register(connection);
		amerishHandler.register(connection);
		indarHandler.register(connection);
		hossinHandler.register(connection);
		
		view.addEffect(other.build());
	}
	
}
