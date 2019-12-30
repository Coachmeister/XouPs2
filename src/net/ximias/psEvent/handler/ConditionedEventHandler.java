package net.ximias.psEvent.handler;

import net.ximias.psEvent.condition.EventCondition;

public interface ConditionedEventHandler {
	EventCondition getCondition();
}
