package net.ximias.psEvent.condition;

public interface MultiCondition extends EventCondition {
	EventCondition[] getContainedConditions();
}
