package net.ximias.effect.views;

import net.ximias.effect.Renderer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import net.ximias.logging.Logger;


public abstract class PauseableContainer {
	private ArrayList<Renderer> renderers = new ArrayList<>(9);
	private final Timer resumeTimer = new Timer("Resume rendering timer"+getClass().getSimpleName(), true);
	private TimerTask resumeTask = resumeTask();
	private final Logger logger = Logger.getLogger(getClass().getName());
	public static final long MAX_PAUSE_TIME_MS = 50_000;
	/**
	 * Subscribe to be notified when rendering should be resumed.
	 * @param renderer the render on which to call the resumeRendering method when the color again changes.
	 */
	public synchronized void subscribeResumeRendering(Renderer renderer) {
		renderers.add(renderer);
	}
	
	/**
	 * Used to determine if the rendering can be paused.
	 * @return true, if the rendering can be paused.
	 */
	public boolean isPausable(){
		if (canPauseRendering()){
			long resumeDelay = calculateResumeTime();
			if (resumeDelay == 0) return false;
			resumeIn(Math.min(MAX_PAUSE_TIME_MS, resumeDelay >0? resumeDelay : MAX_PAUSE_TIME_MS));
			return true;
		}
		return false;
	}
	
	/**
	 * Used to calculate how long in milliseconds the rendering can be paused for.
	 * A negative number means the rendering can be paused until the resumeNow method is called.
	 * 0 will not pause the rendering.
	 * @return the time in milliseconds where no change is going to happen.
	 */
	protected abstract long calculateResumeTime();
	
	/**
	 * Used internally to determine of the rendering can be paused.
	 * @return true, if the rendering can be paused.
	 */
	protected abstract boolean canPauseRendering();
	
	/**
	 * Immediately resumes all renderes.
	 */
	protected synchronized void resumeNow(){
		resetTimer();
		resumeAll();
	}
	
	private void resumeAll() {
		logger.effects().warning("Resuming all renderers...");
		renderers.forEach(Renderer::resumeRendering);
	}
	
	/**
	 * Resumes rendering after delay.
	 * @param milliseconds the delay in milliseconds.
	 */
	protected synchronized void resumeIn(long milliseconds){
		resetTimer();
		resumeTimer.schedule(resumeTask, milliseconds);
	}
	
	private void resetTimer() {
		resumeTask.cancel();
		resumeTask = resumeTask();
	}
	
	private TimerTask resumeTask() {
		return new TimerTask() {
			@Override
			public void run() {
				resumeAll();
			}
		};
	}
}
