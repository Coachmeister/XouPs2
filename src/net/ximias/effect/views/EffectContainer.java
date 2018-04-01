package net.ximias.effect.views;

import javafx.scene.paint.Color;
import net.ximias.effect.Effect;
import net.ximias.effect.EffectView;
import net.ximias.effect.FixedEffect;
import net.ximias.effect.Renderer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Effect container. Poll getColor to get the color of this instant.
 * Contains methods to determine if rendering can be paused. And will call resume on all provided Renderers.
 */
public class EffectContainer implements EffectView {
	private final ArrayList<Effect> effects;
	private final Logger logger = Logger.getLogger(getClass().getName());
	private double effectIntensity;
	private ArrayList<Renderer> renderers = new ArrayList<>(9);
	private final Timer resumeTimer = new Timer("Resume rendering timer", true);
	private TimerTask resumeTask = resumeTask();
	private static final long STANDARD_DELAY_TIME = 60_000L;
	
	
	private EffectContainer() {
		effects = new ArrayList<>(80);
	}
	
	/**
	 * @param effectIntensity the transparency of the added effects.
	 * @param renderer a Renderer implementation, on which to call resume.
	 */
	public EffectContainer(double effectIntensity, Renderer renderer) {
		this();
		renderers.add(renderer);
		this.effectIntensity = effectIntensity;
	}
	
	/**
	 * Used to obtain the blended color of all contained effects. At a given instant.
	 * @return The total color of this container.
	 */
	public Color getColor() {
		return getColorAndClearFinishedEffects();
	}
	
	/**
	 * Where the color blending happens.
	 */
	private synchronized Color getColorAndClearFinishedEffects() {
		effects.removeIf(Effect::isDone);
		Color result = Effect.blend(effectIntensity, effects.toArray(new Effect[0]));
		if (result.equals(Color.BLACK) || result.getOpacity() == 0) {
			logger.warning("Color is fully black or transparent");
		}
		return result;
	}
	
	/**
	 * Add effect to the effectContainer. All effects will be removed when their isDone returns true.
	 * @param effect the effect to add.
	 */
	public synchronized void addEffect(Effect effect) {
		resetTimer();
		resumeAll();
		if (!effects.contains(effect)) {
			effects.add(effect);
			logger.info(effect.getClass().getName() + " added. current effect size: " + effects.size());
			logger.fine("Current color: " + getColorAndClearFinishedEffects());
		}
		if (effect.getColor().getOpacity() == 0) {
			logger.info("Added effect is fully transparent");
		}
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
	
	private void resumeAll() {
		renderers.forEach(Renderer::resumeRendering);
	}
	
	/**
	 * Used to determine if there will be no changes in the color in the foreseeable future.
	 * @return true, if the color will remain the same for a while.
	 */
	public boolean canPauseRendering() {
		boolean canPause = effects.stream().allMatch(it -> it instanceof FixedEffect);
		if (canPause) {
			calculateAndResumeLater();
		}
		return canPause;
	}
	
	/**
	 * Calculates when the rendering should resume, and resumes it at that time.
	 */
	private synchronized void calculateAndResumeLater() {
		Long delay;
		if (effects.isEmpty()) {
			delay = STANDARD_DELAY_TIME;
		} else {
			delay = effects.stream().map(effect -> ((FixedEffect) effect).getRemainingTime()).reduce(STANDARD_DELAY_TIME, Math::min);
		}
		logger.info("Can be paused for: " + delay + " ms");
		resetTimer();
		resumeTimer.schedule(resumeTask, delay);
	}
	
	/**
	 * Subscribe to be notified when rendering should be resumed.
	 * @param renderer the render on which to call the resumeRendering method when the color again changes.
	 */
	public void subscribeResumeRendering(Renderer renderer) {
		renderers.add(renderer);
	}
	
	/**
	 * used to get the transparency multiplier of all effects with intensity.
	 * @return the intensity modifier.
	 */
	@Override
	public double getEffectIntensity() {
		return effectIntensity;
	}
	
	@Override
	public String toString() {
		final StringBuilder effectsNames = new StringBuilder();
		effects.forEach(it -> effectsNames.append(it.getName()).append(" color: ").append(it.getColor()).append("\n"));
		return "Effects: " + effectsNames.toString();
	}
	
	public void setEffectIntensity(double effectIntensity) {
		this.effectIntensity = effectIntensity;
	}
}
