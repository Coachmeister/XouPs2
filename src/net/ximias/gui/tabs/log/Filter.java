package net.ximias.gui.tabs.log;

import net.ximias.gui.tabs.Log;

public class Filter {
	private final Log log;
	public static final String[] SEVERE_FILTER = {"SEVERE"};
	public static final String[] WARNING_FILTER = {"SEVERE", "WARNING"};
	public static final String[] INFO_FILTER = {"SEVERE", "WARNING", "INFO"};
	private String category = "ALL";
	private String[] currentFilter = INFO_FILTER;
	private boolean and = false;
	
	public Filter(Log log) {
		this.log = log;
	}
	
	public boolean inFilter(String string) {
		if (and) {
			return inLevelFilter(string) && inCategoryFilter(string);
		} else {
			return inLevelFilter(string) || inCategoryFilter(string);
		}
	}
	
	private boolean inLevelFilter(String string) {
		for (String s : currentFilter) {
			if (string.contains("[" + s + "]")) {
				return true;
			}
		}
		return false;
	}
	
	private boolean inCategoryFilter(String string) {
		if (category.equals("All")) return true;
		return string.contains("[" + category.toUpperCase() + "]");
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public void setCurrentFilter(String[] currentFilter) {
		this.currentFilter = currentFilter;
	}
	
	public void setAnd(boolean and) {
		this.and = and;
	}
}