package net.ximias.effect;

/**
 * Used to optimize performance by pausing rendering when nothing changes.
 */
public interface Renderer {
	/**
	 * Called when it it time to wake up and start drawing frames again.
	 */
	void resumeRendering();
}
