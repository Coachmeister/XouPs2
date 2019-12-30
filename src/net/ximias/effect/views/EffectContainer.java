package net.ximias.effect.views;

import javafx.scene.paint.Color;
import net.ximias.effect.*;
import net.ximias.logging.Logger;

import java.util.ArrayList;


/**
 * Effect container. Poll getColor to get the color of this instant.
 * Contains methods to determine if rendering can be paused. And will call resume on all provided Renderers.
 */
public class EffectContainer extends PauseableContainer implements EffectView {
	private final ArrayList<Effect> effects;
	private final Logger logger = Logger.getLogger(getClass().getName());
	private double effectIntensity;
	private static final long STANDARD_DELAY_TIME = 60_000L;
	private final ArrayList<EffectAddListener> effectAddListeners = new ArrayList<>();
	
	
	private EffectContainer() {
		effects = new ArrayList<>(80);
	}
	
	/**
	 * @param effectIntensity the transparency of the added effects.
	 * @param renderer        a Renderer implementation, on which to call resume.
	 */
	public EffectContainer(double effectIntensity, Renderer renderer) {
		this();
		subscribeResumeRendering(renderer);
		this.effectIntensity = effectIntensity;
	}
	
	/**
	 * Used to obtain the blended color of all contained effects. At a given instant.
	 *
	 * @return The total color of this container.
	 */
	public Color getColor() {
		return getColorAndClearFinishedEffects();
	}
	
	/**
	 * Where the color blending happens.
	 */
	private Color getColorAndClearFinishedEffects() {
		synchronized (effects) {
			effects.removeIf(Effect::isDone);
			Color result = Effect.blend(effectIntensity, effects.toArray(new Effect[0]));
			if (result.equals(Color.BLACK) || result.getOpacity() == 0) {
				logger.effects().warning("Color is fully black or transparent");
			}
			return result;
		}
	}
	
	/**
	 * Add effect to the effectContainer. All effects will be removed when their isDone returns true.
	 *
	 * @param effect the effect to add.
	 */
	public void addEffect(Effect effect) {
		resumeNow();
		synchronized (effects) {
			if (!effects.contains(effect)) {
				effects.add(effect);
				logger.effects().info(effect.getClass().getName() + " added. current effect size: " + effects.size());
			}
			
		}
		if (effect.getColor().getOpacity() == 0) {
			logger.effects().info("Added effect is fully transparent");
		}
		effectAddListeners.forEach(it -> it.onEffectAdded(effect));
	}
	
	
	/**
	 * Used to determine if there will be no changes in the color in the foreseeable future.
	 *
	 * @return true, if the color will remain the same for a while.
	 */
	@Override
	public boolean canPauseRendering() {
		synchronized (effects) {
			return effects.stream().allMatch(it -> it instanceof FixedEffect);
		}
	}
	
	/**
	 * Calculates when the rendering should resume, and resumes it at that time.
	 */
	@Override
	protected long calculateResumeTime() {
		synchronized (effects) {
			if (!canPauseRendering()) return 0;
			if (effects.isEmpty()) {
				return -1;
			} else {
				return effects.stream().map(effect -> ((FixedEffect) effect).getRemainingTime()).reduce(STANDARD_DELAY_TIME, Math::min);
			}
		}
	}
	
	/**
	 * used to get the transparency multiplier of all effects with intensity.
	 *
	 * @return the intensity modifier.
	 */
	@Override
	public double getEffectIntensity() {
		return effectIntensity;
	}
	
	@Override
	public String toString() {
		synchronized (effects) {
			final StringBuilder effectsNames = new StringBuilder();
			effects.forEach(it -> effectsNames.append(it.getName()).append(" color: ").append(it.getColor()).append("\n"));
			return "Effects: " + effectsNames.toString();
		}
	}
	
	public void setEffectIntensity(double effectIntensity) {
		this.effectIntensity = effectIntensity;
	}
	
	/**
	 * Adds an effectListener object.
	 * The added object will immediately be notified of all added effects.
	 * This works retroactively on all currently running effects as well.
	 *
	 * @param listener the listener to add.
	 */
	public void addEffectAddListener(EffectAddListener listener) {
		effectAddListeners.add(listener);
		synchronized (effects) {
			effects.forEach(listener::onEffectAdded);
		}
	}
	
	public void removeEffectListener(EffectAddListener listener) {
		effectAddListeners.remove(listener);
	}
}
