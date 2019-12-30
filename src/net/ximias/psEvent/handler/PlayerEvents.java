package net.ximias.psEvent.handler;

public enum PlayerEvents implements PsEvent {
	ACHIEVEMENT_EARNED("AchievementEarned"),
	BATTLE_RANK_UP("BattleRankUp"),
	DEATH("Death"),
	ITEM_ADDED("ItemAdded"),
	SKILL_ADDED("SkillAdded"),
	VEHICLE_DESTROY("VehicleDestroy"),
	GAIN_EXPERIENCE("GainExperience"),
	PLAYER_FACILITY_CAPTURE("PlayerFacilityCapture"),
	PLAYER_FACILITY_DEFEND("PlayerFacilityDefend"),
	PLAYER_LOGIN("PlayerLogin"),
	PLAYER_LOGOUT("PlayerLogout");
	
	private final String name;
	
	PlayerEvents(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
