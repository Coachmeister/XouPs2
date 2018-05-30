package net.ximias.psEvent.handler;

public enum WorldEvents implements PsEvent {
	CONTINENT_LOCK("ContinentLock"),
	CONTINENT_UNLOCK("ContinentUnlock"),
	FACILITY_CONTROL("FacilityControl"),
	METAGAME_EVENT("MetagameEvent"),
	PLAYER_LOGIN("PlayerLogin"),
	PLAYER_LOGOUT("PlayerLogout");
	
	
	private final String name;
	
	WorldEvents(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
}
