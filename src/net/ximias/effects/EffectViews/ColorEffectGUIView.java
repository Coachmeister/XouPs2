package net.ximias.effects.EffectViews;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectView;
import net.ximias.effects.EffectViews.Scenes.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Debug gui view
 * Displays the overall effect color
 */
public class ColorEffectGUIView extends Application implements EffectView {
	
	private static final double DEFAULT_EFFECT_INTESITY = 1;
	private static final double DEFAULT_BACKGROUND_INTESITY = 0.1;
	private static final double DEFAULT_BACKGROUND_BRIGHTNES = 0.5;
	private static final Logger PROJECT_LEVEL_LOGGER = Logger.getLogger("net.ximias");
	
	public Canvas canvas;
	public Slider backgroundBrightnessSlider;
	public Slider backgroundIntensitySlider;
	public Slider effectIntensitySlider;
	public AnchorPane effectViewRoot;
	private EffectContainer effectContainer = new EffectContainer(DEFAULT_EFFECT_INTESITY);
	private AnimationTimer animationTimer;
	private EffectScene scene = new PlayStateScene(this);
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		initLogger();
		Parent hue = FXMLLoader.load(getClass().getResource("monogui.fxml"));
		primaryStage.setTitle("Xou "+ SceneConstants.VERSION_NAME +" v"+SceneConstants.VERSION);
		primaryStage.setScene(new Scene(hue));
		primaryStage.show();
	}
	
	@FXML
	public void initialize() {
		Platform.runLater(() -> {
			resize();
			animationTimer = new AnimationTimer() {
				@Override
				public void handle(long now) {
					animateFrame();
				}
			};
			animationTimer.start();
			effectViewRoot.widthProperty().addListener((observable, oldValue, newValue) -> resize());
			effectViewRoot.heightProperty().addListener((observable, oldValue, newValue) -> resize());
			/*addPropertyChangeListener(backgroundBrightnessSlider);
			addPropertyChangeListener(backgroundIntensitySlider);
			addPropertyChangeListener(effectIntensitySlider);*/
			propertiesChanged();
		});
	}
	
	private void addPropertyChangeListener(Slider slider){
		slider.valueProperty().addListener(observable -> propertiesChanged());
	}
	
	private void resize() {
		canvas.setWidth(effectViewRoot.getWidth());
		canvas.setHeight(effectViewRoot.getHeight());
	}
	
	private void animateFrame() {
		GraphicsContext ctx = canvas.getGraphicsContext2D();
		ctx.setFill(effectContainer.getColor());
		ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		String effects = effectContainer.toString();
		ctx.setFill(Color.MAGENTA);
		ctx.setFont(Font.font("monospaced",12));
		ctx.setTextAlign(TextAlignment.LEFT);
		String[] split = effects.split("\n");
		for (int i = 0; i < split.length; i++) {
			String s = split[i];
			ctx.fillText(s, 20, (i+1)*20);
		}
		ctx.setFill(effectContainer.getColor().invert());
		ctx.setFont(Font.font("sans-serif",20));
		ctx.setTextAlign(TextAlignment.RIGHT);
		
		ctx.fillText("App created by Ximias",canvas.getWidth()-20,canvas.getHeight()-25);
	}
	
	@Override
	public synchronized void addEffect(Effect effect) {
		effectContainer.addEffect(effect);
	}
	
	@Override
	public double getEffectIntensity() {
		return effectContainer.getEffectIntensity();
	}
	
	public void restoreDefaults(ActionEvent actionEvent) {
		effectIntensitySlider.setValue(DEFAULT_EFFECT_INTESITY);
		backgroundIntensitySlider.setValue(DEFAULT_BACKGROUND_INTESITY);
		backgroundBrightnessSlider.setValue(DEFAULT_BACKGROUND_BRIGHTNES);
	}
	
	@FXML
	public void applyChanges(ActionEvent event){
		propertiesChanged();
	}
	
	public void propertiesChanged(){
		if (scene instanceof PlayStateScene){
			PlayStateScene playState = ((PlayStateScene) scene);
			effectContainer.setEffectIntensity(effectIntensitySlider.getValue());
			playState.intensityChanged(backgroundBrightnessSlider.getValue(),backgroundIntensitySlider.getValue());
		}
	}
	
	private void initLogger() {
		try(FileInputStream fis = new FileInputStream("logging.properties")) {
			LogManager logManager = LogManager.getLogManager();
			logManager.readConfiguration(fis);
		} catch (IOException e) {
			System.err.println("Logging config file not readable: "+e);
		}
	}
}
