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
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectView;
import net.ximias.effects.EffectViews.Scenes.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Debug gui view
 * Displays the overall effect color
 */
public class ColorEffectGUIView extends Application implements EffectView {
	
	private static final double DEFAULT_EFFECT_INTENSITY = 1;
	private static final double DEFAULT_BACKGROUND_INTENSITY = 0.1;
	private static final double DEFAULT_BACKGROUND_BRIGHTENS = 0.5;
	private static final Logger PROJECT_LEVEL_LOGGER = Logger.getLogger("net.ximias");
	
	@FXML
	private TabPane tabPane;
	@FXML
	private Canvas propertiesPreview;
	@FXML
	private HBox propertiesPreviewContainer;
	@FXML
	private Canvas canvas;
	@FXML
	private Slider backgroundBrightnessSlider;
	@FXML
	private Slider backgroundIntensitySlider;
	@FXML
	private Slider effectIntensitySlider;
	@FXML
	private AnchorPane effectViewRoot;
	private final EffectContainer effectContainer = new EffectContainer(DEFAULT_EFFECT_INTENSITY);
	private AnimationTimer animationTimer;
	private final EffectScene scene = new PlayStateScene(this);
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		initLogger();
		Parent hue = FXMLLoader.load(getClass().getResource("monogui.fxml"));
		primaryStage.setTitle("Xou "+ SceneConstants.VERSION_NAME +" v"+SceneConstants.VERSION);
		Scene scene = new Scene(hue);
		scene.getStylesheets().clear();
		scene.getStylesheets().add("style.css");
		System.out.println(scene.getStylesheets().size());
		primaryStage.setScene(scene);
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
	
	@FXML
	private void resize() {
		propertiesPreview.setWidth(propertiesPreviewContainer.getWidth());
		propertiesPreview.setHeight(propertiesPreviewContainer.getHeight());
		canvas.setWidth(effectViewRoot.getWidth());
		canvas.setHeight(effectViewRoot.getHeight());
	}
	
	private void animateFrame() {
		Canvas activeCanvas = getActiveCanvas();
		GraphicsContext ctx = activeCanvas.getGraphicsContext2D();
		ctx.setFill(effectContainer.getColor());
		ctx.fillRect(0, 0, activeCanvas.getWidth(), activeCanvas.getHeight());
		if(activeCanvas == canvas){
			String effects = effectContainer.toString();
			ctx.setFill(Color.MAGENTA);
			ctx.setFont(Font.font("monospaced",12));
			ctx.setTextAlign(TextAlignment.LEFT);
			String[] split = effects.split("\n");
			for (int i = 0; i < split.length; i++) {
				String s = split[i];
				ctx.fillText(s, 20, (i+1)*20);
			}
		}
		ctx.setFill(effectContainer.getColor().invert());
		ctx.setFont(Font.font("sans-serif",20));
		ctx.setTextAlign(TextAlignment.RIGHT);
		
		ctx.fillText("App created by Ximias",activeCanvas.getWidth()-20,activeCanvas.getHeight()-25);
	}
	
	private Canvas getActiveCanvas(){
		return tabPane.getSelectionModel().getSelectedIndex() == 0 ? canvas : propertiesPreview;
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
		effectIntensitySlider.setValue(DEFAULT_EFFECT_INTENSITY);
		backgroundIntensitySlider.setValue(DEFAULT_BACKGROUND_INTENSITY);
		backgroundBrightnessSlider.setValue(DEFAULT_BACKGROUND_BRIGHTENS);
	}
	
	@FXML
	public void applyChanges(ActionEvent event){
		propertiesChanged();
	}
	
	@FXML
	private void propertiesChanged(){
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
