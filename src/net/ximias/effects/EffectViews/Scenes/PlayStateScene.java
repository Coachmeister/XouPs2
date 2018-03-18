package net.ximias.effects.EffectViews.Scenes;

import javafx.scene.paint.Color;
import net.ximias.effects.EffectView;
import net.ximias.effects.impl.*;
import net.ximias.network.CurrentPlayer;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.*;
import net.ximias.psEvent.handler.*;

import java.io.IOException;
import java.util.HashMap;

public class PlayStateScene implements EffectScene{
	EffectView view;
	Ps2EventStreamingConnection connection;
	
	private HashMap<String, String> experienceid = new HashMap<>(4);
	
	private SingleCondition isPlayer = new SingleCondition(Condition.EQUALS,
			new EventData(CurrentPlayer.getInstance().getPlayerID(),ConditionDataSource.CONSTANT),
			new EventData("character_id", ConditionDataSource.EVENT)
	);
	
	private SingleCondition isNotHive = new SingleCondition(Condition.NOT_CONTAINS,
			new CensusData("experience","description", new HashMap<String, String>(0), experienceid),
			new EventData(" hive", ConditionDataSource.CONSTANT));
	
	public PlayStateScene(EffectView view) {
		experienceid.put("experience_id", "experience_id");
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
		repair();
		experience();
		
		//Version 0.0.4
		multiKill();
		revive();
		heal();
		levelUp();
		alert();
		
		// Version 0.0.5
		killVehicle();
		vehicleDied();
		achievement();
		facility();
		
		//Version 0.0.6
		logout();
		/*
		
		// Version 0.0.6
		logging to files
		killingXimias();
		//Pre-Alpha release
		// Alpha feedback fixes
		
		// Version 0.1.0
		// Settings GUI
		// Abstract keyboard integration
		// Logitech implementation
		// Persistence
		// Keymap import? from InputProfile_User.xml
		
		// Version 0.2.0
		// Configuration files
		// Bug reporting

		
		// Version 0.3.0
		// Hue integration
		
		*/
		
	}
	
	private void logout(){
		FadingEffectProducer fade = new FadingEffectProducer(SceneConstants.OTHER.deriveColor(0,0,0.5,1),5000);
		SingleEventHandler logout = new SingleEventHandler(view, fade, isPlayer, Ps2EventType.PLAYER, "PlayerLogout", "Logout fade");
		logout.register(connection);
	}
	
	private void facility(){
		Color mutedFaction = bias(CurrentPlayer.getInstance().getFactionColor(),0.2); // Use faction color
		
		TimedEffectProducer facilityBegin = new TimedEffectProducer(mutedFaction, 400);
		FadingEffectProducer facilityfade = new FadingEffectProducer(mutedFaction, 500);
		MultiEffectProducer facility = new MultiEffectProducer(facilityBegin, facilityfade);
		
		SingleEventHandler facilityCap = new SingleEventHandler(view, facility, isPlayer, Ps2EventType.PLAYER, "PlayerFacilityCapture", "Facility capture");
		SingleEventHandler facilityDef = new SingleEventHandler(view, facility, isPlayer, Ps2EventType.PLAYER, "PlayerFacilityDefend", "Facility capture");
		facilityCap.register(connection);
		facilityDef.register(connection);
	}
	
	private void achievement(){
		TimedEffectProducer start = new TimedEffectProducer(Color.YELLOW,600);
		FadingEffectProducer end = new FadingEffectProducer(Color.YELLOW,1000);
		MultiEffectProducer ribbon = new MultiEffectProducer(start, end);
		
		SingleEventHandler achievement = new SingleEventHandler(view, ribbon, isPlayer, Ps2EventType.PLAYER, "AchievementEarned", "Ribbon earned");
		achievement.register(connection);
	}
	
	private void killVehicle(){
		Color ex1 = new Color(1,1,0.3,1);
		Color ex2 = new Color(1,0.6,0,1);
		Color ex3 = new Color(1,0.3,0,1);
		
		BlendingEffectProducer e0 = new BlendingEffectProducer(Color.WHITE, ex1,100);
		BlendingEffectProducer e1 = new BlendingEffectProducer(ex1, ex2, 100);
		BlendingEffectProducer e2 = new BlendingEffectProducer(ex2,ex3, 100);
		FadingEffectProducer e3 = new FadingEffectProducer(ex3,1000);
		
		MultiEffectProducer explosion = new MultiEffectProducer(e0,e1,e2,e1,e2,e1,e2,e3);
		SingleCondition isNotDeath = new SingleCondition(Condition.NOT_EQUALS,
				new EventData("character_id", ConditionDataSource.EVENT),
				new EventData(CurrentPlayer.getInstance().getPlayerID(), ConditionDataSource.CONSTANT));
		
		SingleCondition isKill = new SingleCondition(Condition.EQUALS,
				new EventData("attacker_character_id", ConditionDataSource.EVENT),
				new EventData(CurrentPlayer.getInstance().getPlayerID(), ConditionDataSource.CONSTANT));
		
		AllCondition isKillAndNotDeath = new AllCondition(isNotDeath, isKill);
		SingleEventHandler killVehicle = new SingleEventHandler(view, explosion, isKillAndNotDeath, Ps2EventType.PLAYER, "VehicleDestroy","Vehicle kill");
		killVehicle.register(connection);
	}
	
	private void vehicleDied(){
		TimedEffectProducer vehicleBegin = new TimedEffectProducer(Color.WHITE, 600);
		FadingEffectProducer vehicleDestroy = new FadingEffectProducer(Color.WHITE, 1000);
		MultiEffectProducer vehicleDied = new MultiEffectProducer(vehicleBegin, vehicleDestroy);
		
		SingleEventHandler vehicleLost = new SingleEventHandler(view, vehicleDied, isPlayer, Ps2EventType.PLAYER, "VehicleDestroy", "Vehicle Lost");
		vehicleLost.register(connection);
	}
	
	private void alert(){
		Color alert = bias(Color.RED,0.1);
		FadingEffectProducer whoop = new FadingEffectProducer(alert, 300);
		
		MultiEffectProducer alertEffect = new MultiEffectProducer(whoop, whoop, whoop, whoop, whoop);
		
		SingleCondition isPlayerWorld = new SingleCondition(Condition.EQUALS,
				new EventData("world_id", ConditionDataSource.EVENT),
				new EventData("world_id", ConditionDataSource.PLAYER));
		
		SingleEventHandler metagameEvent = new SingleEventHandler(view, alertEffect, isPlayerWorld, Ps2EventType.WORLD, "MetagameEvent", "Alert");
		metagameEvent.register(connection);
	}
	
	private void levelUp(){
		BlendingEffectProducer start = new BlendingEffectProducer(Color.YELLOW, Color.ORANGE, 500);
		TimedEffectProducer middle = new TimedEffectProducer(Color.ORANGE, 1000);
		FadingEffectProducer end = new FadingEffectProducer(Color.ORANGE, 800);
		MultiEffectProducer brup = new MultiEffectProducer(start, middle, end);
		
		SingleEventHandler battleRankEvent = new SingleEventHandler(view, brup, isPlayer, Ps2EventType.PLAYER, "BattleRankUp", "Battle rank up");
		battleRankEvent.register(connection);
	}
	
	private void heal(){
		Color healGreen = bias(new Color(0,0.95,0.1,1),0.1);
		FadingEffectProducer heal = new FadingEffectProducer(healGreen, 800);
		
		AllCondition isHeal = new AllCondition(isPlayer, isNotHive, experienceDescriptionContains("heal"));
		SingleEventHandler healing = new SingleEventHandler(view, heal, isHeal, Ps2EventType.PLAYER, "GainExperience", "healing");
		healing.register(connection);
	}
	
	private void revive(){
		Color reviveGreen = new Color(0.0,1,0.2,1);
		FadingEffectProducer revive = new FadingEffectProducer(reviveGreen, 1000);
		
		SingleCondition isReviveExperience = experienceDescriptionContains("revive");
		AllCondition isRevive = new AllCondition(isReviveExperience, isPlayer, isNotHive );
		
		SingleEventHandler reviveEvent = new SingleEventHandler(view, revive, isRevive, Ps2EventType.PLAYER, "GainExperience", "Revive");
		
		reviveEvent.register(connection);
	}
	
	private void multiKill(){
		TimedEffectProducer delay = new TimedEffectProducer(Color.TRANSPARENT,300);
		BlendingEffectProducer penta = new BlendingEffectProducer(Color.ORANGE, Color.YELLOW,100);
		BlendingEffectProducer pentaReverse = new BlendingEffectProducer(Color.YELLOW, Color.ORANGE,100);
		FadingEffectProducer fadeout = new FadingEffectProducer(Color.ORANGE, 200);
		
		MultiEffectProducer pentaKill = new MultiEffectProducer(delay, penta, pentaReverse, fadeout);
		
		SingleCondition isKill = new SingleCondition(Condition.EQUALS,
				new EventData(CurrentPlayer.getInstance().getPlayerID(),ConditionDataSource.CONSTANT),
				new EventData("attacker_character_id", ConditionDataSource.EVENT)
		);
		
		HashMap<String, String> eventPlayer = new HashMap<>(4);
		eventPlayer.put("character_id","character_id");
		
		SingleCondition isNotSame = new SingleCondition(Condition.NOT_EQUALS,
				new EventData("faction_id",ConditionDataSource.PLAYER),
				new CensusData("character", "faction_id", new HashMap<>(0),eventPlayer));
		
		
		SingleCondition isDeath = isPlayer;
		
		AllCondition isHonestKill = new AllCondition(isKill, isNotSame);
		SingleEventHandler honestKill = new SingleEventHandler(view, null, isHonestKill, Ps2EventType.PLAYER, "Death", "honestKillPart");
		SingleEventHandler death = new SingleEventHandler(view, null, isDeath, Ps2EventType.PLAYER, "Death", "DeathPart");
		MultiEventHandler pentaKillEvent = new MultiEventHandler(
				new Ps2EventHandler[] {honestKill, honestKill, honestKill, honestKill, honestKill},
				new Ps2EventHandler[]{death},true, true, pentaKill, view, "Pentakill");
		
		pentaKillEvent.register(connection);
	}
	
	private void kill() {
		Color lightOrange = new Color(1,0.8,0.5,1);
		
		FadingEffectProducer killEffect = new FadingEffectProducer(Color.WHITE, 500);
		FadingEffectProducer headShotEffect = new FadingEffectProducer(lightOrange, 500);
		FadingEffectProducer teamKillEffect = new FadingEffectProducer(Color.HOTPINK,500);
		FadingEffectProducer VSKillEnd = new FadingEffectProducer(SceneConstants.VS, 300);
		FadingEffectProducer NCKillEnd = new FadingEffectProducer(SceneConstants.NC, 300);
		FadingEffectProducer TRKillEnd = new FadingEffectProducer(SceneConstants.TR, 300);
		TimedEffectProducer blank = new TimedEffectProducer(Color.TRANSPARENT, 100);
		
		
		
		
		MultiEffectProducer VSKill = new MultiEffectProducer(blank, VSKillEnd);
		MultiEffectProducer NCKill = new MultiEffectProducer(blank, NCKillEnd);
		MultiEffectProducer TRKill = new MultiEffectProducer(blank, TRKillEnd);
		
		SingleCondition isNotHeadshot = new SingleCondition(Condition.NOT_EQUALS,
				new EventData("is_headshot", ConditionDataSource.EVENT),
				new EventData("1",ConditionDataSource.CONSTANT));
		
		SingleCondition isHeadshot = new SingleCondition(Condition.EQUALS,
				new EventData("is_headshot", ConditionDataSource.EVENT),
				new EventData("1",ConditionDataSource.CONSTANT));
		
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
		AllCondition isHonestKill = new AllCondition(isKill, isNotSame, isNotHeadshot);
		AllCondition isHeadshotHonestKill = new AllCondition(isKill, isNotSame, isHeadshot);
		AllCondition isVSKill = new AllCondition(isVS, isKill);
		AllCondition isNCKill = new AllCondition(isNC, isKill);
		AllCondition isTRKill = new AllCondition(isTR, isKill);
		
		SingleEventHandler teamKill = new SingleEventHandler(view , teamKillEffect, isTeamKill, Ps2EventType.PLAYER, "Death", "teamKill");
		SingleEventHandler honestKill = new SingleEventHandler(view,killEffect, isHonestKill, Ps2EventType.PLAYER,"Death","honestKill");
		SingleEventHandler honestHeadshotKill = new SingleEventHandler(view,headShotEffect, isHeadshotHonestKill, Ps2EventType.PLAYER,"Death","honestKill");
		SingleEventHandler factionVSKill = new SingleEventHandler(view, VSKill, isVSKill, Ps2EventType.PLAYER, "Death", "VSKill");
		SingleEventHandler factionNCKill = new SingleEventHandler(view, NCKill, isNCKill, Ps2EventType.PLAYER, "Death", "NCKill");
		SingleEventHandler factionTRKill = new SingleEventHandler(view, TRKill, isTRKill, Ps2EventType.PLAYER, "Death", "TRKill");
		
		teamKill.register(connection);
		honestKill.register(connection);
		honestHeadshotKill.register(connection);
		factionVSKill.register(connection);
		factionNCKill.register(connection);
		factionTRKill.register(connection);
	}
	
	private void experience(){
		Color expColor = bias(Color.DARKCYAN, 0.025);
		BlendingEffectProducer fadein = new BlendingEffectProducer(Color.TRANSPARENT, expColor, 100);
		FadingEffectProducer fadeout = new FadingEffectProducer(expColor, 200);
		MultiEffectProducer exp = new MultiEffectProducer(fadein, fadeout);
		
		SingleEventHandler expEvent = new SingleEventHandler(view, exp, isPlayer, Ps2EventType.PLAYER, "GainExperience", "experience");
		expEvent.register(connection);
	}
	
	private void repair(){
		Color repair = bias(new Color(0.8,1,1,1),0.05);
		TimedEffectProducer repairStart = new TimedEffectProducer(repair, 1000);
		FadingEffectProducer repairEnd = new FadingEffectProducer(repair, 250);
		MultiEffectProducer repairing = new MultiEffectProducer(repairStart, repairEnd);
		
		SingleCondition containsRepair = experienceDescriptionContains("repair");
		
		AllCondition isRepair = new AllCondition(isPlayer, containsRepair, isNotHive);
		SingleEventHandler repairEvent = new SingleEventHandler(view, repairing, isRepair, Ps2EventType.PLAYER, "GainExperience", "repair");
		repairEvent.register(connection);
	}
	
	private void death() {
		
		BlendingEffectProducer VSDeathFade = new BlendingEffectProducer(SceneConstants.VS,Color.BLACK,1000);
		BlendingEffectProducer NCDeathFade = new BlendingEffectProducer(SceneConstants.NC,Color.BLACK,1000);
		BlendingEffectProducer TRDeathFade = new BlendingEffectProducer(SceneConstants.TR,Color.BLACK,1000);
		FadingEffectProducer fadeout = new FadingEffectProducer(Color.BLACK, 500);
		TimedEffectProducer black = new TimedEffectProducer(Color.BLACK, 1000);
		
		MultiEffectProducer VSDeath = new MultiEffectProducer(
				VSDeathFade,
				black,
				fadeout
		);
		MultiEffectProducer NCDeath = new MultiEffectProducer(
				NCDeathFade,
				black,
				fadeout
		);
		MultiEffectProducer TRDeath = new MultiEffectProducer(
				TRDeathFade,
				black,
				fadeout
		);
		
		HashMap<String, String> eventPlayer = new HashMap<>(4);
		eventPlayer.put("character_id","attacker_character_id");
		
		
		
		SingleCondition isVS = new SingleCondition(Condition.EQUALS, new CensusData("character","faction_id", new HashMap<>(0),eventPlayer),new EventData(String.valueOf(SceneConstants.VS_ID),ConditionDataSource.CONSTANT));
		SingleCondition isNC = new SingleCondition(Condition.EQUALS, new CensusData("character","faction_id", new HashMap<>(0),eventPlayer),new EventData(String.valueOf(SceneConstants.NC_ID),ConditionDataSource.CONSTANT));
		SingleCondition isTR = new SingleCondition(Condition.EQUALS, new CensusData("character","faction_id", new HashMap<>(0),eventPlayer),new EventData(String.valueOf(SceneConstants.TR_ID),ConditionDataSource.CONSTANT));
		
		AllCondition isVSDeath = new AllCondition(isVS, isPlayer);
		AllCondition isNCDeath = new AllCondition(isNC, isPlayer);
		AllCondition isTRDeath = new AllCondition(isTR, isPlayer);
		
		SingleEventHandler VSDeathhandler = new SingleEventHandler(view, VSDeath, isVSDeath, Ps2EventType.PLAYER, "Death", "vsDeath");
		SingleEventHandler NCDeathhandler = new SingleEventHandler(view, NCDeath, isNCDeath, Ps2EventType.PLAYER, "Death", "ncDeath");
		SingleEventHandler TRDeathhandler = new SingleEventHandler(view, TRDeath, isTRDeath, Ps2EventType.PLAYER, "Death", "trDeath");
		
		VSDeathhandler.register(connection);
		NCDeathhandler.register(connection);
		TRDeathhandler.register(connection);
	}
	
	private void background() {
		EventEffectProducer esamir = new EventEffectProducer(bias(SceneConstants.ESAMIR,0.05),"background");
		EventEffectProducer amerish = new EventEffectProducer(bias(SceneConstants.AMERISH, 0.05),"background");
		EventEffectProducer indar = new EventEffectProducer(bias(SceneConstants.INDAR,0.05),"background");
		EventEffectProducer hossin = new EventEffectProducer(bias(SceneConstants.HOSSIN,0.05),"background");
		EventEffectProducer other = new EventEffectProducer(bias(SceneConstants.OTHER,0.05),"background");
		
		SingleCondition isEsamir = new SingleCondition(Condition.EQUALS, new EventData("zone_id", ConditionDataSource.PLAYER),new EventData(String.valueOf(SceneConstants.ESAMIR_ID),ConditionDataSource.CONSTANT));
		SingleCondition isAmerish = new SingleCondition(Condition.EQUALS, new EventData("zone_id",ConditionDataSource.PLAYER),new EventData(String.valueOf(SceneConstants.AMERISH_ID),ConditionDataSource.CONSTANT));
		SingleCondition isIndar = new SingleCondition(Condition.EQUALS, new EventData("zone_id",ConditionDataSource.PLAYER),new EventData(String.valueOf(SceneConstants.INDAR_ID),ConditionDataSource.CONSTANT));
		SingleCondition isHossin = new SingleCondition(Condition.EQUALS, new EventData("zone_id",ConditionDataSource.PLAYER),new EventData(String.valueOf(SceneConstants.HOSSIN_ID),ConditionDataSource.CONSTANT));
		NoneCondition isNone = new NoneCondition(isEsamir, isAmerish, isHossin, isIndar);
		
		GlobalHandler esamirHandler = new GlobalHandler(isEsamir, esamir, view);
		GlobalHandler amerishHandler = new GlobalHandler(isAmerish, amerish, view);
		GlobalHandler indarHandler = new GlobalHandler(isIndar, indar, view);
		GlobalHandler hossinHandler = new GlobalHandler(isHossin, hossin, view);
		GlobalHandler noneHandler = new GlobalHandler(isNone, other, view);
		
		esamirHandler.register(connection);
		amerishHandler.register(connection);
		indarHandler.register(connection);
		hossinHandler.register(connection);
		noneHandler.register(connection);
		
		view.addEffect(other.build());
	}
	private Color bias(Color color,double bias){
		return color.deriveColor(0,1,1,bias);
	}
	
	private SingleCondition experienceDescriptionContains(String contains){
		return new SingleCondition(Condition.CONTAINS,
				new CensusData("experience","description", new HashMap<String, String>(0), experienceid),
				new EventData(contains, ConditionDataSource.CONSTANT));
	}
}
