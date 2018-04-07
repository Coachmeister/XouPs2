package net.ximias.effect;

/**
 * Effect which does not animate or animate very slowly. Used to optimize rendering by pausing.
 */
public interface FixedEffect extends Effect {
	/**
	 * Used to obtain the time until a change occurs in the effect.
	 * @return milliseconds until effect terminates or changes in other ways.
	 * NOTE: If this function returns mostly single-digit numbers, using the interface costs performance more than it gains.
	 */
	long getRemainingTime();
}
