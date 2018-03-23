package net.ximias.effects.EffectViews;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectView;
import net.ximias.effects.EffectViews.Scenes.*;
import net.ximias.effects.impl.FadingEffectProducer;
import net.ximias.logging.WebLogAppender;
import net.ximias.network.CurrentPlayer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
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
	private static final WebLogAppender webLogAppender = new WebLogAppender();
	private int oldZone;
	private boolean worldWasModified = false;
	@FXML
	public WebView logView;
	public ChoiceBox<String> previewBackgroundSelector;
	@FXML
	private ToggleButton debugTextToggle;
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
	private final PlayStateScene scene = new PlayStateScene(this);
	private FadingEffectProducer exampleEffect = new FadingEffectProducer(Color.LIME,1500);
	
	public static void main(String[] args) {
		initLogger();
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws IOException {
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
			PROJECT_LEVEL_LOGGER.warning("GUI initializing...");
			setupWebLogger();
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
			setupPreviewTab();
		});
	}
	
	private void setupPreviewTab() {
		TreeMap<String, Integer> selections = new TreeMap<>();
		selections.put("None", -1);
		selections.put("Amerish", SceneConstants.AMERISH_ID);
		selections.put("Esamir", SceneConstants.ESAMIR_ID);
		selections.put("Indar", SceneConstants.INDAR_ID);
		selections.put("Hossin", SceneConstants.HOSSIN_ID);
		previewBackgroundSelector.setItems(FXCollections.observableArrayList(selections.keySet()));
		setSelectionToCurrentZone(selections);
		previewBackgroundSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (!worldWasModified){
				oldZone = Integer.valueOf(CurrentPlayer.getInstance().getValue("zone_id"));
			}
			CurrentPlayer.getInstance().setZoneId(selections.get(newValue));
			scene.updateBackground();
		});
		
		tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Tab changed to: "+newValue);
			if (newValue.intValue() != 1) {
				worldWasModified = false;
				CurrentPlayer.getInstance().setZoneId(oldZone);
				scene.updateBackground();
				setSelectionToCurrentZone(selections);
			}
		});
	}
	
	private void setSelectionToCurrentZone(TreeMap<String, Integer> selections) {
		for (Map.Entry<String, Integer> stringIntegerEntry : selections.entrySet()) {
			if (stringIntegerEntry.getValue().equals(Integer.valueOf(CurrentPlayer.getInstance().getValue("zone_id")))){
				previewBackgroundSelector.getSelectionModel().select(stringIntegerEntry.getKey());
				return;
			}
		}
		
		previewBackgroundSelector.getSelectionModel().select("none");
	}
	
	private void setupWebLogger() {
		webLogAppender.addEngine(logView.getEngine());
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
		if(debugTextToggle.isSelected()){
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
		effectContainer.setEffectIntensity(effectIntensitySlider.getValue());
		(scene).intensityChanged(backgroundBrightnessSlider.getValue(),backgroundIntensitySlider.getValue());
	}
	
	@FXML
	private void addExampleEffect(ActionEvent e){
		addEffect(exampleEffect.build());
	}
	
	private static void initLogger() {
		try(FileInputStream fis = new FileInputStream("logging.properties")) {
			LogManager logManager = LogManager.getLogManager();
			logManager.readConfiguration(fis);
			PROJECT_LEVEL_LOGGER.addHandler(webLogAppender);
		} catch (IOException e) {
			System.err.println("Logging config file not readable: "+e);
		}
	}
}
