package net.ximias.peripheral.keyboard;

import javafx.scene.paint.Color;
import net.ximias.effect.Renderer;
import net.ximias.effect.views.EffectContainer;
import net.ximias.effect.views.PauseableContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import net.ximias.logging.Logger;


public class KeyboardEffectContainer extends PauseableContainer implements Renderer {
	private final EffectContainer globalContainer;
	private final LinkedList<KeyEffect> effects = new LinkedList<>();
	private int rows,columns;
	private Logger logger = Logger.getLogger(getClass().getName());
	
	public KeyboardEffectContainer(EffectContainer globalContainer, int columns, int rows) {
		this.globalContainer = globalContainer;
		this.columns = columns;
		this.rows = rows;
		globalContainer.subscribeResumeRendering(this);
	}
	
	public Color getGlobalColor(){
		return globalContainer.getColor();
	}
	
	@Override
	protected long calculateResumeTime() {
		return -1;
	}
	
	@Override
	public boolean canPauseRendering(){
		if (!globalContainer.canPauseRendering()) return false;
		return effects.isEmpty();
	}
	
	public void addEffect(KeyEffect effect){
		synchronized (effects){
			effects.add(effect);
			logger.warning("KeyEffect added!");
			resumeNow();
		}
	}
	
	public Color[][] getPerKeyColor(){
		return getColorsFromAndClearEffects();
	}
	
	private Color[][] getColorsFromAndClearEffects(){
		synchronized (effects){
			effects.removeIf(KeyEffect::isDone);
			
			Color[][][] interpolationColors = new Color[effects.size()+1][columns][rows];
			Color[][] backgroundComp = new Color[columns][rows];
			Color[] backgroundRow = new Color[rows];
			Arrays.fill(backgroundRow, getGlobalColor().deriveColor(0,1,1,0.2));
			Arrays.fill(backgroundComp, backgroundRow);
			interpolationColors[0] = backgroundComp;
			
			for (int i = 1; i < effects.size() + 1; i++) {
				interpolationColors[i] = effects.get(i-1).getKeyColors(columns, rows);
			}
			return interpolate(interpolationColors);
		}
	}
	
	private Color[][] interpolate(Color[][][] colors){
		Color[][] result = new Color[columns][rows];
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				Color[] effectListAtKey = new Color[colors.length];
				for (int effect = 0; effect < colors.length; effect++) {
					effectListAtKey[effect] = colors[effect][column][row];
				}
				result[column][row] = blend(effectListAtKey);
			}
		}
		return result;
	}
	
	private Color blend(Color[] colors){
		double r, g, b, a;
		r = g = b = a = 0;
		for (Color color : colors) {
			r += color.getRed() * color.getOpacity();
			g += color.getGreen() * color.getOpacity();
			b += color.getBlue() * color.getOpacity();
			a += color.getOpacity();
		}
		return Color.color(Math.min(r / a, 1.0), Math.min(g / a, 1.0), Math.min(b / a, 1.0));
	}
	
	public static void main(String[] args) {
		int rows = 2;
		int columns = 8;
		
		Color[][][] interpolationColors = new Color[1][columns][rows];
		Color[][] backgroundComp = new Color[columns][rows];
		Color[] backgroundRow = new Color[rows];
		Arrays.fill(backgroundRow, Color.RED);
		Arrays.fill(backgroundComp, backgroundRow);
		System.out.println(backgroundComp[0][1]);
		System.out.println(backgroundComp.length);
		System.out.println(backgroundComp[0].length);
		interpolationColors[0] = backgroundComp;
		
		System.out.println(interpolationColors.length);
	}
	
	@Override
	public void resumeRendering() {
		resumeNow();
	}
}
