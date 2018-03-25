package net.ximias.gui.tabs;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import net.ximias.gui.MainController;
import net.ximias.logging.WebLogAppender;

public class Log {
	private WebLogAppender webLogAppender;
	
	private MainController mainController;
	@FXML
	private WebView logView;
	
	
	public void injectMainController(MainController controller, WebLogAppender appender){
		this.mainController = controller;
		webLogAppender = appender;
		setupWebLogger();
	}
	
	private void setupWebLogger() {
		webLogAppender.addEngine(logView.getEngine());
	}
}
