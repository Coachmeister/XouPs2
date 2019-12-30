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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import net.ximias.datastructures.gui.data.EffectData;
import net.ximias.datastructures.gui.nodes.StatusIndicator;
import net.ximias.effect.Renderer;
import net.ximias.effect.views.EffectContainer;
import net.ximias.gui.tabs.*;
import net.ximias.logging.FileLogAppender;
import net.ximias.persistence.ApplicationConstants;
import net.ximias.persistence.Persisted;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * Debug gui view
 * Displays the overall effect color
 */
public class MainController extends Application implements Renderer {
	
	private static final java.util.logging.Logger PROJECT_LEVEL_LOGGER = java.util.logging.Logger.getLogger("net.ximias");
	private final net.ximias.logging.Logger logger = net.ximias.logging.Logger.getLogger(getClass().getName());
	private static final FileLogAppender fileLogAppender = new FileLogAppender();
	private static boolean logDisabled = false;
	private final EffectContainer effectContainer = new EffectContainer(ApplicationConstants.DEFAULT_EFFECT_INTENSITY, this);
	private AnimationTimer animationTimer;
	private Rectangle statusArea;
	private final StatusIndicator statusIndicatorController = StatusIndicator.getInstance();
	
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
	private HueTab hueTabController;
	@FXML
	private EffectEditorTab effectEditorTabController;
	@FXML
	private LoginTab loginTabController;
	@FXML
	private TabPane tabPane;
	@FXML
	private Circle statusIndicator;
	@FXML
	private Rectangle statusRectangle;
	@FXML
	private Text statusText;
	@FXML
	private Tab effectViewT;
	@FXML
	private Tab propertiesT;
	@FXML
	private Tab logT;
	@FXML
	private Tab featuresT;
	@FXML
	private Tab keyboardT;
	@FXML
	private Tab hueT;
	@FXML
	private Tab editorT;
	
	private EffectData effectData;
	
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
		logger.application().fine("Stylesheets: " + scene.getStylesheets().size());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	private void onResizeHeight(int newValue) {
		logger.application().info("Height updated!");
		Persisted.getInstance().APPLICATION_HEIGHT = newValue;
	}
	
	private void onResizeWidth(int newValue) {
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
			setupLogTab();
			animationTimer = new AnimationTimer() {
				@Override
				public void handle(long now) {
					animateFrame();
				}
			};
			loginTabController.initProperty().addListener((observable, oldValue, newValue) -> {
				if (!newValue) {
					onLogin();
				}
			});
			bindTabToInit(effectViewT);
			bindTabToInit(propertiesT);
			bindTabToInit(editorT);
			
			setupTabListener();
			setupHueTab();
			setupKeyboardTab();
			setupFeaturesTab();
			setupLoginTab();
			statusIndicatorController.injectMainController(this);
			statusIndicatorController.injectComponents(statusIndicator, statusRectangle, statusText);
			statusIndicatorController.addStatus("No issues detected!", StatusSeverity.NOTHING);
			animationTimer.start();
		});
	}
	
	private void onLogin() {
		effectData = new EffectData(effectContainer, new File("test.json"));
		setupEffectViewTab();
		setupPropertiesTab();
		setupEffectEditorTab();
	}
	
	@FXML
	private void showStatus(MouseEvent event) {
		statusIndicatorController.showTooltip();
	}
	
	@FXML
	private void hideStatus(MouseEvent event) {
		statusIndicatorController.hideToolTip();
	}
	
	public void addStatus(String text, StatusSeverity severity) {
		statusIndicatorController.addStatus(text, severity);
	}
	
	public void removeStatus(String text) {
		statusIndicatorController.removeStatus(text);
	}
	
	private void bindTabToInit(Tab tab) {
		tab.disableProperty().bind(loginTabController.initProperty());
	}
	
	private void setupKeyboardTab() {
		logger.application().info("Keyboard tab init.");
		keyboardTabController.injectMainController(this);
	}
	
	private void setupHueTab() {
		logger.application().info("Hue tab init.");
		hueTabController.injectMainController(this);
	}
	
	private void setupLoginTab() {
		logger.application().info("Login tab init.");
		loginTabController.injectMainController(this);
		loginTabController.initProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) effectData = new EffectData(getEffectContainer(), new File("test.json"));
		});
	}
	
	private void setupEffectEditorTab() {
		logger.application().info("Effect edit tab init.");
		effectEditorTabController.injectMainController(this);
	}
	
	private void setupEffectViewTab() {
		logger.application().info("Effect view tab init.");
		effectViewController.injectMainController(this);
	}
	
	private void setupPropertiesTab() {
		logger.application().info("Features tab init.");
		propertiesTabController.injectMainController(this);
	}
	
	private void setupFeaturesTab() {
		logger.application().info("Features tab init.");
		featuresTabController.injectMainController(this);
	}
	
	private void setupLogTab() {
		logger.application().info("Log tab init.");
		logTabController.injectMainController(this, fileLogAppender.getLogFile());
	}
	
	private void setupTabListener() {
		tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
			logger.application().info("Tab changed to: " + newValue);
			propertiesTabController.onTabChange(newValue.intValue());
			resumeRendering();
		});
	}
	
	private void animateFrame() {
		if (effectContainer.isPausable()) {
			animationTimer.stop();
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
	
	public void addProjectLevelLoggerHandler(Handler handler) {
		PROJECT_LEVEL_LOGGER.addHandler(handler);
		if (logDisabled) {
			handler.publish(new LogRecord(Level.SEVERE, "Logging is disabled on account of missing logging.properties file."));
		}
	}
	
	private static void initLogger() {
		try (FileInputStream fis = new FileInputStream("logging.properties")) {
			LogManager logManager = LogManager.getLogManager();
			logManager.readConfiguration(fis);
			PROJECT_LEVEL_LOGGER.addHandler(fileLogAppender);
		} catch (IOException e) {
			logDisabled = true;
			System.err.println("Logging config file not readable: " + e +
			                   "\n Disabling output to Logging tab.");
			fileLogAppender.publish(new LogRecord(Level.SEVERE, "Logging is disabled on account of missing logging.properties file."));
		}
	}
	
	public EffectContainer getEffectContainer() {
		return effectContainer;
	}
	
	
	public EffectData getEffectData() {
		return effectData;
	}
	
	@Override
	public void resumeRendering() {
		if (animationTimer == null) return;
		animationTimer.start();
		logger.application().fine("Rendering resumed.");
	}
}
