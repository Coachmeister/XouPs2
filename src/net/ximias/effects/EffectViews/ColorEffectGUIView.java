package net.ximias.effects.EffectViews;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectView;
import net.ximias.effects.EffectViews.Scenes.EffectScene;
import net.ximias.effects.EffectViews.Scenes.LoginEventsScene;
import net.ximias.effects.EffectViews.Scenes.PlayStateScene;
import net.ximias.effects.EffectViews.Scenes.SceneConstants;

import java.io.IOException;

/**
 * Debug gui view
 * Displays the overall effect color
 */
public class ColorEffectGUIView extends Application implements EffectView {
	
	public Canvas canvas;
	public AnchorPane root;
	private EffectContainer effectContainer = new EffectContainer();
	private AnimationTimer animationTimer;
	private EffectScene scene = new PlayStateScene(this);
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws IOException {
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
			root.widthProperty().addListener((observable, oldValue, newValue) -> resize());
			root.heightProperty().addListener((observable, oldValue, newValue) -> resize());
		});
	}
	
	private void resize() {
		canvas.setWidth(root.getWidth());
		canvas.setHeight(root.getHeight());
		System.out.println(canvas.getWidth());
		System.out.println(canvas.getHeight());
	}
	
	private void animateFrame() {
		GraphicsContext ctx = canvas.getGraphicsContext2D();
		ctx.setFill(effectContainer.getColor());
		ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	@Override
	public void addEffect(Effect effect) {
		effectContainer.addEffect(effect);
	}
}
