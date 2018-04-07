package net.ximias.gui;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import net.ximias.effect.Renderer;
import net.ximias.effect.views.EffectContainer;
import net.ximias.gui.tabs.*;
import net.ximias.effect.views.scenes.*;
import net.ximias.logging.WebLogAppender;
import net.ximias.persistence.ApplicationConstants;
import net.ximias.persistence.Persisted;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.*;

/**
 * Debug gui view
 * Displays the overall effect color
 */
public class MainController extends Application implements Renderer{
	
	private static final Logger PROJECT_LEVEL_LOGGER = Logger.getLogger("net.ximias");
	private static final WebLogAppender webLogAppender = new WebLogAppender();
	private final EffectContainer effectContainer = new EffectContainer(ApplicationConstants.DEFAULT_EFFECT_INTENSITY, this);
	private final PlayStateScene scene = new PlayStateScene(effectContainer);
	private AnimationTimer animationTimer;
	private Logger logger = Logger.getLogger(getClass().getName());
	
	@FXML
	private MainEffectView effectViewController;
	@FXML
	private Properties propertiesTabController;
	@FXML
	private Log logTabController;
	@FXML
	private Features featuresTabController;
	@FXML
	private KeyboardTab keyboardTabController;
	
	@FXML
	private TabPane tabPane;
	
	public static void main(String[] args) {
		initLogger();
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		displayWelcome();
		Parent gui = FXMLLoader.load(getClass().getResource("monogui.fxml"));
		primaryStage.setTitle("Xou " + ApplicationConstants.VERSION_NAME + " v" + ApplicationConstants.VERSION);
		primaryStage.setWidth(Persisted.getInstance().APPLICATION_WIDTH);
		primaryStage.setHeight(Persisted.getInstance().APPLICATION_HEIGHT);
		primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> onResizeHeight(newValue.intValue()));
		primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> onResizeWidth(newValue.intValue()));
		primaryStage.centerOnScreen();
		Scene scene = new Scene(gui);
		scene.getStylesheets().clear();
		scene.getStylesheets().add("style.css");
		logger.finer("Stylesheets: " + scene.getStylesheets().size());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	private void onResizeHeight(int newValue){
		logger.info("Height updated!");
		Persisted.getInstance().APPLICATION_HEIGHT = newValue;
	}
	private void onResizeWidth(int newValue){
		Persisted.getInstance().APPLICATION_WIDTH = newValue;
	}
	
	private static void displayWelcome() {
		Alert welcome = new Alert(Alert.AlertType.INFORMATION, System.currentTimeMillis() < ApplicationConstants.EXP_DATE ? ApplicationConstants.INTRO_TEXT : ApplicationConstants.EXP_MESSAGE, ButtonType.CLOSE);
		welcome.setTitle("Before we start:");
		welcome.setHeaderText("Some would call this a disclaimer");
		welcome.getDialogPane().getStylesheets().clear();
		welcome.getDialogPane().getStyleClass().add("dialog");
		welcome.getDialogPane().getStylesheets().add("style.css");
		welcome.showAndWait();
	}
	
	@FXML
	private void initialize() {
		Platform.runLater(() -> {
			PROJECT_LEVEL_LOGGER.warning("GUI initializing...");
			animationTimer = new AnimationTimer() {
				@Override
				public void handle(long now) {
					animateFrame();
				}
			};
			setupEffectViewTab();
			setupPreviewTab();
			setupFeaturesTab();
			setupLogTab();
			setupTabListener();
			setupKeyboardTab();
			animationTimer.start();
		});
	}
	
	private void setupKeyboardTab() {
		keyboardTabController.injectMainController(this);
	}
	
	private void setupEffectViewTab() {
		effectViewController.injectMainController(this);
	}
	
	private void setupPreviewTab() {
		propertiesTabController.injectMainController(this);
	}
	
	private void setupTabListener() {
		tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
			logger.info("Tab changed to: " + newValue);
			propertiesTabController.onTabChange(newValue.intValue());
			resumeRendering();
		});
	}
	
	private void setupFeaturesTab() {
		featuresTabController.injectMainController(this);
	}
	
	private void setupLogTab() {
		logTabController.injectMainController(this, webLogAppender);
	}
	
	private void animateFrame() {
		if (effectContainer.canPauseRendering()){
			animationTimer.stop();
			logger.fine("Rendering has been paused.");
		}
		Canvas activeCanvas = getActiveCanvas();
		GraphicsContext ctx = activeCanvas.getGraphicsContext2D();
		ctx.setFill(effectContainer.getColor());
		ctx.fillRect(0, 0, activeCanvas.getWidth(), activeCanvas.getHeight());
		if (propertiesTabController.drawDebug()) {
			String effects = effectContainer.toString();
			ctx.setFill(Color.MAGENTA);
			ctx.setFont(Font.font("monospaced", 12));
			ctx.setTextAlign(TextAlignment.LEFT);
			String[] split = effects.split("\n");
			for (int i = 0; i < split.length; i++) {
				String s = split[i];
				ctx.fillText(s, 20, (i + 1) * 20);
			}
		}
		ctx.setFill(effectContainer.getColor().invert());
		ctx.setFont(Font.font("sans-serif", 20));
		ctx.setTextAlign(TextAlignment.RIGHT);
		
		ctx.fillText("App created by Ximias", activeCanvas.getWidth() - 20, activeCanvas.getHeight() - 25);
	}
	
	private Canvas getActiveCanvas() {
		return tabPane.getSelectionModel().getSelectedIndex() == 0 ? effectViewController.getCanvas() : propertiesTabController.getCanvas();
	}
	
	private static void initLogger() {
		try (FileInputStream fis = new FileInputStream("logging.properties")) {
			LogManager logManager = LogManager.getLogManager();
			logManager.readConfiguration(fis);
			PROJECT_LEVEL_LOGGER.addHandler(webLogAppender);
		} catch (IOException e) {
			System.err.println("Logging config file not readable: " + e +
			                   "\n Disabling output to Logging tab.");
			webLogAppender.append(new LogRecord(Level.SEVERE, "Logging tab is disabled on account of missing logging.properties file."));
		}
	}
	
	public EffectContainer getEffectContainer() {
		return effectContainer;
	}
	
	public PlayStateScene getEffectScene() {
		return scene;
	}
	
	@Override
	public void resumeRendering() {
		animationTimer.start();
		logger.fine("Rendering resumed.");
	}
}
