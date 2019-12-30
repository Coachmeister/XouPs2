package net.ximias.effect.views.scenes;

import javafx.scene.paint.Color;
import net.ximias.effect.EffectView;
import net.ximias.effect.producers.BlendingEffectProducer;
import net.ximias.effect.producers.FadingEffectProducer;
import net.ximias.effect.producers.MultiEffectProducer;
import net.ximias.effect.producers.TimedEffectProducer;
import net.ximias.network.CurrentPlayer;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.peripheral.Hue.Hue.hueEffects.*;
import net.ximias.peripheral.keyboard.effects.*;
import net.ximias.persistence.ApplicationConstants;
import net.ximias.persistence.Persisted;
import net.ximias.psEvent.condition.*;
import net.ximias.psEvent.handler.MultiEventHandler;
import net.ximias.psEvent.handler.Ps2EventHandler;
import net.ximias.psEvent.handler.Ps2EventType;
import net.ximias.psEvent.handler.SingleEventHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;

public class DefaultScene extends EffectScene {
	
	private final HashMap<String, String> experienceid = new HashMap<>(4);
	
	static final SingleCondition isPlayer = new SingleCondition(
			"Is active player",
			Condition.EQUALS,
			new EventData("character_id", ConditionDataSource.PLAYER),
			new EventData("character_id", ConditionDataSource.EVENT)
	);
	
	private final SingleCondition isNotHive = new SingleCondition(
			"Is not hive",
			Condition.NOT_CONTAINS,
			new CensusData("experience", "description", new HashMap<>(0), experienceid),
			new EventData(" hive", ConditionDataSource.CONSTANT));
	
	public DefaultScene(EffectView view, Ps2EventStreamingConnection connection) {
		super(view, connection);
		experienceid.put("experience_id", "experience_id");
		magic();
		LinkedList<String> lines = new LinkedList<>();
		JSONObject serialized = connection.serializeToJSON();
		lines.add(serialized.toString());
		try {
			Files.write(Paths.get("test.json"), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			throw new Error("Failed to write file", e);
		}
	}
	
	private void magic() {
		
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
		maxKill();
		
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
		// Abstract net.ximias.hardware integration
		// Logitech implementation
		// Persistence
		// Keymap import? from InputProfile_User.xml
		
		// Version 0.3.0
		// Separate peripheral effects.
		// Hue integration.
		
		// Version 0.4.0
		// Effect editor.
		// More sliders.
		// Faction color as data source for editor.
		
		// Version 0.5.0
		// Peripheral effect editor
		// Effect creation tutorial.
		
		// Version 1.0.0
		// Configuration files
		// Bug reporting

		
		*/
		
	}
	
	private void killingXimias() {
		for (String ximiasId : ApplicationConstants.XIMIAS_IDS) {
			if (CurrentPlayer.getInstance().getPlayerID().equals(ximiasId)) return;
		}
		BlendingEffectProducer rainbow1 = new BlendingEffectProducer("Rainbow RO", Color.RED, Color.ORANGE, 250);
		BlendingEffectProducer rainbow2 = new BlendingEffectProducer("Rainbow OY", Color.ORANGE, Color.YELLOW, 250);
		BlendingEffectProducer rainbow3 = new BlendingEffectProducer("Rainbow YG", Color.YELLOW, Color.GREEN, 250);
		BlendingEffectProducer rainbow4 = new BlendingEffectProducer("Rainbow GC", Color.GREEN, Color.CYAN, 250);
		BlendingEffectProducer rainbow5 = new BlendingEffectProducer("Rainbow CB", Color.CYAN, Color.BLUE, 250);
		BlendingEffectProducer rainbow6 = new BlendingEffectProducer("Rainbow BV", Color.BLUE, Color.VIOLET, 250);
		FadingEffectProducer rainbowEnd = new FadingEffectProducer("Rainbow VF", Color.VIOLET, 500);
		
		MultiEffectProducer rainbow = new MultiEffectProducer("Rainbow", rainbow1, rainbow2, rainbow3, rainbow4, rainbow5, rainbow6, rainbowEnd);
		MultiEffectProducer doubleRainbow = new MultiEffectProducer("Double rainbow!", rainbow, rainbow);
		
		Color light = new Color(1, 1, 1, .3);
		MultiKeyEffectProducer outIn = new MultiKeyEffectProducer(new WaveEffectProducer(light, 400, 1, WaveEffectDirection.CENTER_OUT), new WaveEffectProducer(light, 400, 1, WaveEffectDirection.OUT_CENTER));
		doubleRainbow.attachPeripheralEffect(new MultiKeyEffectProducer(outIn, outIn, outIn, outIn, outIn));
		
		doubleRainbow.attachPeripheralEffect(new CircularEffect(4000, 300, Color.WHITE, Color.WHITE, 1));
		
		SingleEventHandler ximias = new SingleEventHandler(view, doubleRainbow, isXimias(), Ps2EventType.PLAYER, "Death", "Ximias easter egg");
		ximias.register(connection);
	}
	
	private EventCondition isXimias() {
		
		SingleCondition[] ximiasDeaths = new SingleCondition[ApplicationConstants.XIMIAS_IDS.length];
		
		for (int i = 0; i < ximiasDeaths.length; i++) {
			ximiasDeaths[i] = new SingleCondition(
					"Ximias Deaths" + i,
					Condition.EQUALS,
					new EventData(ApplicationConstants.XIMIAS_IDS[i], ConditionDataSource.CONSTANT),
					new EventData("character_id", ConditionDataSource.EVENT));
		}
		
		SingleCondition[] ximiasKills = new SingleCondition[ApplicationConstants.XIMIAS_IDS.length];
		for (int i = 0; i < ximiasKills.length; i++) {
			ximiasKills[i] = new SingleCondition(
					"Ximias Kills" + i,
					Condition.EQUALS,
					new EventData(ApplicationConstants.XIMIAS_IDS[i], ConditionDataSource.CONSTANT),
					new EventData("attacker_character_id", ConditionDataSource.EVENT));
		}
		
		SingleCondition[] isXimias = new SingleCondition[ximiasKills.length + ximiasDeaths.length - 1];
		System.arraycopy(ximiasKills, 0, isXimias, 0, ximiasKills.length);
		System.arraycopy(ximiasDeaths, 0, isXimias, ximiasKills.length - 1, ximiasDeaths.length);
		
		return new AnyCondition("is Ximias", isXimias);
	}
	
	private void facility() {
		// TODO: Find a way of making this translate into json.
		Color mutedFaction = bias(CurrentPlayer.getInstance().getFactionColor(), 0.2); // Use faction color
		
		TimedEffectProducer facilityBegin = new TimedEffectProducer("Facility Begin", mutedFaction, 400);
		FadingEffectProducer facilityfade = new FadingEffectProducer("Facility Fade", mutedFaction, 500);
		MultiEffectProducer facility = new MultiEffectProducer("Facility effect", facilityBegin, facilityfade);
		
		facility.attachPeripheralEffect(new MulticolorWaveProducer(new Color[]{Color.WHITE, Color.TRANSPARENT, Color.TRANSPARENT, Color.WHITE}, 400, WaveEffectDirection.CENTER_OUT));
		facility.attachPeripheralEffect(new ExplosionEffect(1000, Color.WHITE, bias(Color.WHITE, 0.5)));
		facility.attachPeripheralEffect(new FrontCenterEffect(mutedFaction, Color.TRANSPARENT, 1200));
		
		SingleEventHandler facilityCap = new SingleEventHandler(view, facility, isPlayer, Ps2EventType.PLAYER, "PlayerFacilityCapture", "Facility capture");
		SingleEventHandler facilityDef = new SingleEventHandler(view, facility, isPlayer, Ps2EventType.PLAYER, "PlayerFacilityDefend", "Facility capture");
		facilityCap.register(connection);
		facilityDef.register(connection);
	}
	
	private void achievement() {
		TimedEffectProducer start = new TimedEffectProducer("Ribbon Start", Color.YELLOW, 600);
		FadingEffectProducer end = new FadingEffectProducer("Ribbon End", Color.YELLOW, 1000);
		MultiEffectProducer ribbon = new MultiEffectProducer("Ribbon Effect", start, end);
		
		ribbon.attachPeripheralEffect(new MultiKeyEffectProducer(
				new MulticolorWaveProducer(new Color[]{Color.ORANGE, Color.WHITE}, 800, WaveEffectDirection.CENTER_OUT),
				new MulticolorWaveProducer(new Color[]{Color.ORANGE, Color.WHITE}, 800, WaveEffectDirection.OUT_CENTER)));
		
		SingleEventHandler achievement = new SingleEventHandler(view, ribbon, isPlayer, Ps2EventType.PLAYER, "AchievementEarned", "Ribbon earned");
		achievement.register(connection);
	}
	
	private void killVehicle() {
		Color ex1 = new Color(1, 1, 0.3, 1);
		Color ex2 = new Color(1, 0.6, 0, 1);
		Color ex3 = new Color(1, 0.3, 0, 1);
		
		BlendingEffectProducer e0 = new BlendingEffectProducer("Explosion White", Color.WHITE, ex1, 100);
		BlendingEffectProducer e1 = new BlendingEffectProducer("Explosion Yellow", ex1, ex2, 100);
		BlendingEffectProducer e2 = new BlendingEffectProducer("Explosion LightOrange", ex2, ex3, 100);
		FadingEffectProducer e3 = new FadingEffectProducer("Explosion Fade", ex3, 1000);
		
		MultiEffectProducer explosion = new MultiEffectProducer("Explosion Effect", e0, e1, e2, e1, e2, e1, e2, e3);
		
		
		Color light = new Color(1, 1, 1, .3);
		ContinualWaveEffectProducer keyExplosion = new ContinualWaveEffectProducer(new MulticolorWaveProducer(new Color[]{Color.BLACK, light, Color.LIGHTSKYBLUE, light}, 300, WaveEffectDirection.CENTER_OUT), 5);
		explosion.attachPeripheralEffect(keyExplosion);
		
		ExplosionEffect hueEffect = new ExplosionEffect(2000, bias(Color.PALEGOLDENROD, 0.8), bias(Color.RED, 0.5));
		explosion.attachPeripheralEffect(hueEffect);
		
		SingleCondition isNotDeath = new SingleCondition(
				"Is not death",
				Condition.NOT_EQUALS,
				new EventData("character_id", ConditionDataSource.EVENT),
				new EventData(CurrentPlayer.getInstance().getPlayerID(), ConditionDataSource.CONSTANT));
		
		SingleCondition isKill = new SingleCondition(
				"Is kill",
				Condition.EQUALS,
				new EventData("attacker_character_id", ConditionDataSource.EVENT),
				new EventData(CurrentPlayer.getInstance().getPlayerID(), ConditionDataSource.CONSTANT));
		
		AllCondition isKillAndNotDeath = new AllCondition("Is kill and not death", isNotDeath, isKill);
		SingleEventHandler killVehicle = new SingleEventHandler(view, explosion, isKillAndNotDeath, Ps2EventType.PLAYER, "VehicleDestroy", "Vehicle kill");
		killVehicle.register(connection);
	}
	
	private void vehicleDied() {
		TimedEffectProducer vehicleBegin = new TimedEffectProducer("VehicleBegin", Color.WHITE, 600);
		FadingEffectProducer vehicleDestroy = new FadingEffectProducer("VehicleDestroy", Color.WHITE, 1000);
		MultiEffectProducer vehicleDied = new MultiEffectProducer("VehicleDied", vehicleBegin, vehicleDestroy);
		
		WaveEffectProducer blackIn = new WaveEffectProducer(Color.BLACK, 800, 1, WaveEffectDirection.OUT_CENTER);
		vehicleDied.attachPeripheralEffect(blackIn);
		vehicleDied.attachPeripheralEffect(new ExplosionEffect(500, Color.WHITE, Color.YELLOW));
		vehicleDied.attachPeripheralEffect(new MultiLightSourceEffect(new HueDelayEffect(300), new ExplosionEffect(600, Color.YELLOW, Color.RED)));
		
		SingleEventHandler vehicleLost = new SingleEventHandler(view, vehicleDied, isPlayer, Ps2EventType.PLAYER, "VehicleDestroy", "Vehicle Lost");
		vehicleLost.register(connection);
	}
	
	private void alert() {
		Color alert = bias(Color.RED, 0.1);
		FadingEffectProducer whoop = new FadingEffectProducer("Whoop!", alert, 400);
		
		MultiEffectProducer alertEffect = new MultiEffectProducer("WhoopWhoopWhoop!", whoop, whoop, whoop, whoop, whoop);
		
		MulticolorWaveProducer keyWhoop = new MulticolorWaveProducer(new Color[]{Color.RED, Color.INDIANRED, Color.WHITE, Color.INDIANRED, Color.RED}, 400, WaveEffectDirection.RIGHT_TO_LEFT);
		ContinualWaveEffectProducer keyAlertEffect = new ContinualWaveEffectProducer(keyWhoop, 5);
		alertEffect.attachPeripheralEffect(keyAlertEffect);
		
		CircularEffect hueEffect = new CircularEffect(2300, 600, Color.RED, Color.PINK, 1.5);
		alertEffect.attachPeripheralEffect(hueEffect);
		
		SingleCondition isPlayerWorld = new SingleCondition(
				"Is same world as player",
				Condition.EQUALS,
				new EventData("world_id", ConditionDataSource.EVENT),
				new EventData("world_id", ConditionDataSource.PLAYER));
		
		SingleEventHandler metagameEvent = new SingleEventHandler(view, alertEffect, isPlayerWorld, Ps2EventType.WORLD, "MetagameEvent", "Alert");
		metagameEvent.register(connection);
	}
	
	private void levelUp() {
		BlendingEffectProducer start = new BlendingEffectProducer("LevelupSunrise",
				Color.ORANGE, Color.YELLOW, 1300);
		TimedEffectProducer middle = new TimedEffectProducer("Levelup Midday", Color.YELLOW, 200);
		FadingEffectProducer end = new FadingEffectProducer("Levelup fadeout", Color.YELLOW, 800);
		MultiEffectProducer brup = new MultiEffectProducer("Level up: A new day", start, middle, end);
		
		MulticolorWaveProducer yellowUp = new MulticolorWaveProducer(new Color[]{Color.YELLOW, Color.WHITE, Color.YELLOW}, 600, WaveEffectDirection.DOWN_TO_UP);
		ContinualWaveEffectProducer keyUp = new ContinualWaveEffectProducer(yellowUp, 2);
		DelayProducer delay = new DelayProducer(300);
		brup.attachPeripheralEffect(new MultiKeyEffectProducer(
				keyUp,
				delay,
				keyUp
		));
		
		SingleEventHandler battleRankEvent = new SingleEventHandler(view, brup, isPlayer, Ps2EventType.PLAYER, "BattleRankUp", "Battle rank up");
		battleRankEvent.register(connection);
	}
	
	private void heal() {
		Color healGreen = bias(new Color(0, 0.95, 0.1, 1), 0.1);
		FadingEffectProducer heal = new FadingEffectProducer("Healing fade", healGreen, 800);
		
		heal.attachPeripheralEffect(new WaveEffectProducer(Color.WHITE, 500, 1, WaveEffectDirection.DOWN_TO_UP));
		
		AllCondition isHeal = new AllCondition("Is heal", isPlayer, isNotHive, experienceDescriptionContains("heal"));
		SingleEventHandler healing = new SingleEventHandler(view, heal, isHeal, Ps2EventType.PLAYER, "GainExperience", "Healing");
		healing.register(connection);
	}
	
	private void revive() {
		Color reviveGreen = new Color(0.0, 1, 0.2, 1);
		FadingEffectProducer revive = new FadingEffectProducer("Revive fade", reviveGreen, 1000);
		
		
		revive.attachPeripheralEffect(new MultiKeyEffectProducer(new DelayProducer(250), new WaveEffectProducer(Color.WHITE, 500, 3, WaveEffectDirection.DOWN_TO_UP)));
		revive.attachPeripheralEffect(new ExplosionEffect(800, Color.WHITE, Color.TRANSPARENT));
		revive.attachPeripheralEffect(MovementEffect.fromBackToFront(600, Color.GREEN, Color.LIME));
		
		SingleCondition isReviveExperience = experienceDescriptionContains("revive");
		AllCondition isRevive = new AllCondition("Is revive", isReviveExperience, isPlayer, isNotHive);
		
		SingleEventHandler reviveEvent = new SingleEventHandler(view, revive, isRevive, Ps2EventType.PLAYER, "GainExperience", "Revive");
		
		reviveEvent.register(connection);
	}
	
	private void multiKill() {
		TimedEffectProducer delay = new TimedEffectProducer("Multikill delay for regular kill", Color.TRANSPARENT, 300);
		BlendingEffectProducer penta = new BlendingEffectProducer("Multikill penta", Color.WHITE, Color.RED, 100);
		BlendingEffectProducer pentaReverse = new BlendingEffectProducer("Multikill penta reversed", Color.RED, Color.WHITE, 100);
		FadingEffectProducer fadeout = new FadingEffectProducer("Multikill fadeout", Color.ORANGE, 200);
		
		MultiEffectProducer pentaKill = new MultiEffectProducer("Multikill: Candycane", delay, penta, pentaReverse, penta, pentaReverse, penta, pentaReverse, fadeout);
		
		pentaKill.attachPeripheralEffect(new MulticolorWaveProducer(new Color[]{Color.WHITE, Color.TRANSPARENT, Color.TRANSPARENT, Color.WHITE, Color.TRANSPARENT, Color.TRANSPARENT, Color.WHITE}, 1000, WaveEffectDirection.CENTER_OUT));
		pentaKill.attachPeripheralEffect(new CircularEffect(1000, 500, Color.ORANGE, Color.YELLOW, 2));
		
		SingleCondition isKill = new SingleCondition(
				"Is kill",
				Condition.EQUALS,
				new EventData(CurrentPlayer.getInstance().getPlayerID(), ConditionDataSource.CONSTANT),
				new EventData("attacker_character_id", ConditionDataSource.EVENT)
		);
		
		HashMap<String, String> eventPlayer = new HashMap<>(4);
		eventPlayer.put("character_id", "character_id");
		
		SingleCondition isNotSame = new SingleCondition(
				"Is not same faction",
				Condition.NOT_EQUALS,
				new EventData("faction_id", ConditionDataSource.PLAYER),
				new CensusData("character", "faction_id", new HashMap<>(0), eventPlayer));
		
		
		AllCondition isHonestKill = new AllCondition("Is honest kill", isKill, isNotSame);
		SingleEventHandler honestKill = new SingleEventHandler(view, null, isHonestKill, Ps2EventType.PLAYER, "Death", "Honest kill");
		SingleEventHandler death = new SingleEventHandler(view, null, isPlayer, Ps2EventType.PLAYER, "Death", "Death");
		MultiEventHandler pentaKillEvent = new MultiEventHandler(
				new Ps2EventHandler[]{honestKill, honestKill, honestKill, honestKill, honestKill},
				new Ps2EventHandler[]{death}, true, true, pentaKill, view, "Penta-kill");
		
		pentaKillEvent.register(connection);
	}
	
	private void kill() {
		Color lightOrange = new Color(1, 0.8, 0.5, 1);
		
		FadingEffectProducer killEffect = new FadingEffectProducer("Kill flash effect", Color.WHITE, 500);
		FadingEffectProducer headShotEffect = new FadingEffectProducer("Headshot orange flash effect", lightOrange, 500);
		FadingEffectProducer teamKillEffect = new FadingEffectProducer("Teamkill pink flash effect", Color.HOTPINK, 500);
		FadingEffectProducer VSKillEnd = new FadingEffectProducer("VS kill faction color fade", Persisted.getInstance().VS, 300);
		FadingEffectProducer NCKillEnd = new FadingEffectProducer("NC kill faction color fade", Persisted.getInstance().NC, 300);
		FadingEffectProducer TRKillEnd = new FadingEffectProducer("TR kill faction color fade", Persisted.getInstance().TR, 300);
		TimedEffectProducer blank = new TimedEffectProducer("slight delay", Color.TRANSPARENT, 100);
		
		
		MultiEffectProducer VSKill = new MultiEffectProducer("VS killed effect", blank, VSKillEnd);
		MultiEffectProducer NCKill = new MultiEffectProducer("NC killed effect", blank, NCKillEnd);
		MultiEffectProducer TRKill = new MultiEffectProducer("TR killed effect", blank, TRKillEnd);
		
		SingleCondition isNotHeadshot = new SingleCondition(
				"Is not headshot",
				Condition.NOT_EQUALS,
				new EventData("is_headshot", ConditionDataSource.EVENT),
				new EventData("1", ConditionDataSource.CONSTANT));
		
		SingleCondition isHeadshot = new SingleCondition(
				"Is headshot",
				Condition.EQUALS,
				new EventData("is_headshot", ConditionDataSource.EVENT),
				new EventData("1", ConditionDataSource.CONSTANT));
		
		SingleCondition isKill = new SingleCondition(
				"Is kill",
				Condition.EQUALS,
				new EventData(CurrentPlayer.getInstance().getPlayerID(), ConditionDataSource.CONSTANT),
				new EventData("attacker_character_id", ConditionDataSource.EVENT)
		);
		
		HashMap<String, String> eventPlayer = new HashMap<>(4);
		eventPlayer.put("character_id", "character_id");
		
		SingleCondition isVS = new SingleCondition(
				"Is killed player VS",
				Condition.EQUALS,
				new EventData(String.valueOf(ApplicationConstants.VS_ID), ConditionDataSource.CONSTANT),
				new CensusData("character", "faction_id", new HashMap<>(0), eventPlayer));
		
		SingleCondition isNC = new SingleCondition(
				"Is killed player NC",
				Condition.EQUALS,
				new EventData(String.valueOf(ApplicationConstants.NC_ID), ConditionDataSource.CONSTANT),
				new CensusData("character", "faction_id", new HashMap<>(0), eventPlayer));
		
		SingleCondition isTR = new SingleCondition(
				"Is killed player TR",
				Condition.EQUALS,
				new EventData(String.valueOf(ApplicationConstants.TR_ID), ConditionDataSource.CONSTANT),
				new CensusData("character", "faction_id", new HashMap<>(0), eventPlayer));
		
		SingleCondition isSame = new SingleCondition(
				"Is same faction as player",
				Condition.EQUALS,
				new EventData("faction_id", ConditionDataSource.PLAYER),
				new CensusData("character", "faction_id", new HashMap<>(0), eventPlayer));
		
		SingleCondition isNotSame = new SingleCondition(
				"Is not same faction as player",
				Condition.NOT_EQUALS,
				new EventData("faction_id", ConditionDataSource.PLAYER),
				new CensusData("character", "faction_id", new HashMap<>(0), eventPlayer));
		
		AllCondition isTeamKill = new AllCondition("is teamkill", isKill, isSame);
		AllCondition isHonestKill = new AllCondition("is honest non-headshot kill", isKill, isNotSame, isNotHeadshot);
		AllCondition isHeadshotHonestKill = new AllCondition("is honest headshot kill", isKill, isNotSame, isHeadshot);
		AllCondition isVSKill = new AllCondition("is VS kill", isVS, isKill);
		AllCondition isNCKill = new AllCondition("is NC kill", isNC, isKill);
		AllCondition isTRKill = new AllCondition("is TR kill", isTR, isKill);
		
		SingleEventHandler teamKill = new SingleEventHandler(view, teamKillEffect, isTeamKill, Ps2EventType.PLAYER, "Death", "Team kill");
		SingleEventHandler honestKill = new SingleEventHandler(view, killEffect, isHonestKill, Ps2EventType.PLAYER, "Death", "Honest regular kill");
		SingleEventHandler honestHeadshotKill = new SingleEventHandler(view, headShotEffect, isHeadshotHonestKill, Ps2EventType.PLAYER, "Death", "Honest headshot kill");
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
	
	private void experience() {
		Color expColor = bias(Color.CYAN, 0.02);
		BlendingEffectProducer fadein = new BlendingEffectProducer("Experience fade in", Color.TRANSPARENT, expColor, 100);
		FadingEffectProducer fadeout = new FadingEffectProducer("Experience fade out", expColor, 200);
		MultiEffectProducer exp = new MultiEffectProducer("Experience effect", fadein, fadeout);
		
		SingleEventHandler expEvent = new SingleEventHandler(view, exp, isPlayer, Ps2EventType.PLAYER, "GainExperience", "Experience ping");
		expEvent.register(connection);
	}
	
	private void repair() {
		Color repair = bias(new Color(0.8, 1, 1, 1), 0.05);
		TimedEffectProducer repairStart = new TimedEffectProducer("Repair start", repair, 1000);
		FadingEffectProducer repairEnd = new FadingEffectProducer("Repair end", repair, 250);
		MultiEffectProducer repairing = new MultiEffectProducer("Repair effect", repairStart, repairEnd);
		
		repairing.attachPeripheralEffect(new ContinualWaveEffectProducer(new WaveEffectProducer(Color.CYAN, 500, 2, WaveEffectDirection.LEFT_TO_RIGHT), 2));
		repairing.attachPeripheralEffect(new ContinualWaveEffectProducer(new WaveEffectProducer(Color.BLUE, 500, 2, WaveEffectDirection.RIGHT_TO_LEFT), 2));
		
		Color lowCyan = bias(Color.CYAN, 0.125);
		Color lowBlue = bias(Color.BLUE, 0.125);
		MovementEffect cyanEffect = MovementEffect.fromLeftToRight(500, lowCyan, lowCyan);
		MovementEffect blueEffect = MovementEffect.fromRightToLeft(500, lowBlue, lowBlue);
		repairing.attachPeripheralEffect(new MultiLightSourceEffect(cyanEffect, cyanEffect));
		repairing.attachPeripheralEffect(new MultiLightSourceEffect(blueEffect, blueEffect));
		
		SingleCondition containsRepair = experienceDescriptionContains("repair");
		
		AllCondition isRepair = new AllCondition("is repair", isPlayer, containsRepair, isNotHive);
		SingleEventHandler repairEvent = new SingleEventHandler(view, repairing, isRepair, Ps2EventType.PLAYER, "GainExperience", "Repair: Light from gun");
		repairEvent.register(connection);
	}
	
	private void maxKill() {
		TimedEffectProducer delay = new TimedEffectProducer("max kill delay", Color.TRANSPARENT, 250);
		FadingEffectProducer flash = new FadingEffectProducer("white flash", Color.WHITE, 250);
		MultiEffectProducer maxKill = new MultiEffectProducer("Max kill: Accelerating flashes", delay, flash, delay, delay, flash, delay, flash, delay, flash, flash, flash, flash);
		maxKill.attachPeripheralEffect(new ContinualWaveEffectProducer(new WaveEffectProducer(Color.YELLOW, 250, 2, WaveEffectDirection.DOWN_TO_UP), 3));
		MovementEffect movementEffect = MovementEffect.fromFrontToBack(300, Color.YELLOW, Color.WHITE);
		maxKill.attachPeripheralEffect(new MultiLightSourceEffect(movementEffect, movementEffect, movementEffect, movementEffect));
		
		SingleCondition containsMax = experienceDescriptionContains("Kill Player Class MAX");
		SingleEventHandler maxKillEvent = new SingleEventHandler(view, maxKill, containsMax, Ps2EventType.PLAYER, "GainExperience", "Max kill");
		maxKillEvent.register(connection);
	}
	
	private void death() {
		
		//Effects
		BlendingEffectProducer VSDeathFade = new BlendingEffectProducer("VSDeath fade", Persisted.getInstance().VS, Color.BLACK, 1000);
		BlendingEffectProducer NCDeathFade = new BlendingEffectProducer("NCDeath fade", Persisted.getInstance().NC, Color.BLACK, 1000);
		BlendingEffectProducer TRDeathFade = new BlendingEffectProducer("TRDeath fade", Persisted.getInstance().TR, Color.BLACK, 1000);
		FadingEffectProducer fadeout = new FadingEffectProducer("Fade to black", Color.BLACK, 500);
		TimedEffectProducer black = new TimedEffectProducer("Black screen", Color.BLACK, 1000);
		
		MultiEffectProducer VSDeath = new MultiEffectProducer(
				"Death: VS fade",
				VSDeathFade,
				black,
				fadeout
		);
		MultiEffectProducer NCDeath = new MultiEffectProducer(
				"Death: NC fade",
				NCDeathFade,
				black,
				fadeout
		);
		MultiEffectProducer TRDeath = new MultiEffectProducer(
				"Death: TR fade",
				TRDeathFade,
				black,
				fadeout
		);
		
		//KeyboardEffects
		WaveEffectProducer keyDeathEffect = new WaveEffectProducer(Color.BLACK, 1250, 12, WaveEffectDirection.UP_TO_DOWN);
		VSDeath.attachPeripheralEffect(keyDeathEffect);
		NCDeath.attachPeripheralEffect(keyDeathEffect);
		TRDeath.attachPeripheralEffect(keyDeathEffect);
		MovementEffect hueDeathEffect = MovementEffect.fromBackToFront(1250, Color.BLACK, Color.BLACK);
		VSDeath.attachPeripheralEffect(hueDeathEffect);
		NCDeath.attachPeripheralEffect(hueDeathEffect);
		TRDeath.attachPeripheralEffect(hueDeathEffect);
		
		HashMap<String, String> eventPlayer = new HashMap<>(4);
		eventPlayer.put("character_id", "attacker_character_id");
		
		
		SingleCondition isVS = new SingleCondition(
				"is attacking player VS",
				Condition.EQUALS,
				new EventData(String.valueOf(ApplicationConstants.VS_ID), ConditionDataSource.CONSTANT),
				new CensusData("character", "faction_id", new HashMap<>(0), eventPlayer));
		
		SingleCondition isNC = new SingleCondition(
				"is attacking player NC",
				Condition.EQUALS,
				new EventData(String.valueOf(ApplicationConstants.NC_ID), ConditionDataSource.CONSTANT),
				new CensusData("character", "faction_id", new HashMap<>(0), eventPlayer));
		
		SingleCondition isTR = new SingleCondition(
				"is attacking player TR",
				Condition.EQUALS,
				new EventData(String.valueOf(ApplicationConstants.TR_ID), ConditionDataSource.CONSTANT),
				new CensusData("character", "faction_id", new HashMap<>(0), eventPlayer));
		
		AllCondition isVSDeath = new AllCondition("Killed by VS", isVS, isPlayer);
		AllCondition isNCDeath = new AllCondition("Killed by NC", isNC, isPlayer);
		AllCondition isTRDeath = new AllCondition("Killed by TR", isTR, isPlayer);
		
		SingleEventHandler VSDeathhandler = new SingleEventHandler(view, VSDeath, isVSDeath, Ps2EventType.PLAYER, "Death", "vsDeath");
		SingleEventHandler NCDeathhandler = new SingleEventHandler(view, NCDeath, isNCDeath, Ps2EventType.PLAYER, "Death", "ncDeath");
		SingleEventHandler TRDeathhandler = new SingleEventHandler(view, TRDeath, isTRDeath, Ps2EventType.PLAYER, "Death", "trDeath");
		
		VSDeathhandler.register(connection);
		NCDeathhandler.register(connection);
		TRDeathhandler.register(connection);
	}
	
	private Color bias(Color color, double bias) {
		return color.deriveColor(0, 1, 1, bias);
	}
	
	private Color dimm(Color color, double amount) {
		return color.deriveColor(0, 1, amount, 1);
	}
	
	private SingleCondition experienceDescriptionContains(String contains) {
		return new SingleCondition(
				"Experience description contains " + contains,
				Condition.CONTAINS,
				new CensusData("experience", "description", new HashMap<>(0), experienceid),
				new EventData(contains, ConditionDataSource.CONSTANT));
	}
}