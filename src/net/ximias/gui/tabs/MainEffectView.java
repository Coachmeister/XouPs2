package net.ximias.gui.tabs;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import net.ximias.gui.MainController;

public class MainEffectView {
	@FXML
	private Canvas canvas;
	@FXML
	private AnchorPane effectViewRoot;
	
	private MainController mainController;
	
	@FXML
	private void initialize(){
	
	}
	
	private void setupResize(){
		canvas.widthProperty().bind(effectViewRoot.widthProperty());
		canvas.heightProperty().bind(effectViewRoot.heightProperty());
		effectViewRoot.widthProperty().addListener(observable -> mainController.resumeRendering());
		effectViewRoot.heightProperty().addListener(observable -> mainController.resumeRendering());
	}
	
	public void injectMainController(MainController mainController) {
		this.mainController = mainController;
		setupResize();
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
}
