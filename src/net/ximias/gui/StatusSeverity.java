package net.ximias.gui;

public enum StatusSeverity {
	NOTHING(0), INCONVIENIENCE(2), ANNOYANCE(4), INTERUPTION(6), CUSTOM(8);
	
	private int severity;
	
	StatusSeverity(int severity) {
		this.severity = severity;
	}
	
	public void setSeverity(int severity) {
		if (this == CUSTOM){
			this.severity = severity;
		}
	}
	
	public int getSeverityValue() {
		return severity;
	}
}
