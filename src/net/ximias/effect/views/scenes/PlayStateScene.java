package net.ximias.effect.views.scenes;

import javafx.scene.paint.Color;
import net.ximias.effect.EffectView;
import net.ximias.effect.producers.*;
import net.ximias.network.CurrentPlayer;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.*;
import net.ximias.psEvent.handler.*;

import java.util.HashMap;
import java.util.logging.Logger;

public class PlayStateScene implements EffectScene{
	private final EffectView view;
	private Ps2EventStreamingConnection connection;
	private Logger logger = Logger.getLogger(getClass().getName());
	
	private PlayStateBackground background;
	private final HashMap<String, String> experienceid = new HashMap<>(4);
	
	private final SingleCondition isPlayer = new SingleCondition(Condition.EQUALS,
			new EventData(CurrentPlayer.getInstance().getPlayerID(),ConditionDataSource.CONSTANT),
			new EventData("character_id", ConditionDataSource.EVENT)
	);
	
	private final SingleCondition isNotHive = new SingleCondition(Condition.NOT_CONTAINS,
			new CensusData("experience","description", new HashMap<>(0), experienceid),
			new EventData(" hive", ConditionDataSource.CONSTANT));
	
	public PlayStateScene(EffectView view) {
		experienceid.put("experience_id", "experience_id");
		this.view = view;
			magic();
	}
	
	public void intensityChanged(double brightness, double intensity){
		if (background == null) {
			background = new PlayStateBackground(view, connection, intensity, brightness);
		}
		background.intensityChanged(brightness,intensity);
	}
	
	public void updateBackground(){
		background.updateHandlers();
	}
	
	private void magic() {
		connection = new Ps2EventStreamingConnection();
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
		// Logging used.
		killingXimias();
		/*
		
		// Version 0.0.6
		// Intensity Slider
		// File logging
		// Expiration date
		//Pre-Alpha release
		// Pre-Alpha feedback fixes
		
		//Version 0.0.?
		// Alpha release
		// Alpha feedback fixes.
		
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
	
	private void killingXimias() {
		for (String ximiasId : SceneConstants.XIMIAS_IDS) {
			if (CurrentPlayer.getInstance().getPlayerID().equals(ximiasId)) return;
		}
		BlendingEffectProducer rainbow1 = new BlendingEffectProducer(Color.RED, Color.ORANGE, 250);
		BlendingEffectProducer rainbow2 = new BlendingEffectProducer(Color.ORANGE, Color.YELLOW, 250);
		BlendingEffectProducer rainbow3 = new BlendingEffectProducer(Color.YELLOW, Color.GREEN, 250);
		BlendingEffectProducer rainbow4 = new BlendingEffectProducer(Color.GREEN, Color.CYAN, 250);
		BlendingEffectProducer rainbow5 = new BlendingEffectProducer(Color.CYAN, Color.BLUE, 250);
		BlendingEffectProducer rainbow6 = new BlendingEffectProducer(Color.BLUE, Color.VIOLET, 250);
		FadingEffectProducer rainbowEnd = new FadingEffectProducer(Color.VIOLET, 500);
		
		MultiEffectProducer rainbow = new MultiEffectProducer(rainbow1, rainbow2, rainbow3, rainbow4, rainbow5, rainbow6, rainbowEnd);
		MultiEffectProducer doubleRainbow = new MultiEffectProducer(rainbow, rainbow);
		
		SingleEventHandler ximias = new SingleEventHandler(view,doubleRainbow, isXimias(), Ps2EventType.PLAYER, "Death", "Ximias easter egg");
		ximias.register(connection);
	}
	
	private EventCondition isXimias(){
		
		SingleCondition[] ximiasDeaths = new SingleCondition[SceneConstants.XIMIAS_IDS.length];
		
		for (int i = 0; i < ximiasDeaths.length; i++) {
			ximiasDeaths[i] = new SingleCondition(
					Condition.EQUALS,
					new EventData(SceneConstants.XIMIAS_IDS[i],ConditionDataSource.CONSTANT),
					new EventData("character_id", ConditionDataSource.EVENT));
		}
		
		SingleCondition[] ximiasKills = new SingleCondition[SceneConstants.XIMIAS_IDS.length];
		for (int i = 0; i < ximiasKills.length; i++) {
			ximiasKills[i] = new SingleCondition(
					Condition.EQUALS,
					new EventData(SceneConstants.XIMIAS_IDS[i],ConditionDataSource.CONSTANT),
					new EventData("attacker_character_id", ConditionDataSource.EVENT));
		}
		
		SingleCondition[] isXimias = new SingleCondition[ximiasKills.length+ximiasDeaths.length-1];
		System.arraycopy(ximiasKills, 0, isXimias, 0, ximiasKills.length);
		System.arraycopy(ximiasDeaths, 0, isXimias, 0 + ximiasKills.length - 1, ximiasDeaths.length);
		
		return new AnyCondition(isXimias);
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
		Color expColor = bias(Color.CYAN, 0.02);
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
	
	private Color bias(Color color,double bias){
		return color.deriveColor(0,1,1,bias);
	}
	
	private Color dimm(Color color, double amount){return color.deriveColor(0,1,amount,1);}
	private SingleCondition experienceDescriptionContains(String contains){
		return new SingleCondition(Condition.CONTAINS,
				new CensusData("experience","description", new HashMap<>(0), experienceid),
				new EventData(contains, ConditionDataSource.CONSTANT));
	}
}