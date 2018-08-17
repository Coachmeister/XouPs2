package net.ximias.datastructures.gui.nodes;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import net.ximias.gui.MainController;
import net.ximias.gui.StatusSeverity;

import java.util.HashMap;
import java.util.Map;

public class StatusIndicator {
	MainController mainController;
	Circle statusCircle;
	Rectangle tooltipRectangle = new Rectangle();
	Text tooltipText;
	String text;
	HashMap<String, StatusSeverity> statuses = new HashMap<>();
	private static StatusIndicator instance;
	
	private StatusIndicator(){
	
	}
	
	public void injectMainController(MainController controller){
		instance = this;
		mainController = controller;
	}
	
	public static StatusIndicator getInstance(){
		if (instance == null) {
			instance = new StatusIndicator();
		}
		return instance;
	}
	
	public void injectComponents(Circle indicator, Rectangle rectangle, Text text){
		statusCircle = indicator;
		tooltipRectangle = rectangle;
		tooltipText = text;
	}
	
	public void addStatus(String text, StatusSeverity severity){
		statuses.put(text,severity);
		showMostSevereStatus();
	}
	
	public void setStatus(String text, StatusSeverity severity){
		statuses.clear();
		statuses.put(text,severity);
	}
	
	private void showMostSevereStatus(){
		int severity = -1;
		for (Map.Entry<String, StatusSeverity> stringStatusSeverityEntry : statuses.entrySet()) {
			if (stringStatusSeverityEntry.getValue().getSeverityValue() > severity) {
				text = stringStatusSeverityEntry.getKey();
				severity = stringStatusSeverityEntry.getValue().getSeverityValue();
			}
		}
		updateStatusIndicatorColor(statuses.get(text).getSeverityValue());
	}
	
	private void updateStatusIndicatorColor(int severity){
		int severityLevel = severity/2;
		Color innerColor;
		if (severityLevel <= 0) innerColor =  Color.LIME;
		else if (severityLevel == 1) innerColor = Color.YELLOW;
		else if (severityLevel == 2) innerColor = Color.ORANGERED;
		else innerColor = Color.MAROON;
		Stop innerStop = new Stop(0,innerColor);
		Stop outerStop = new Stop(1,Color.TRANSPARENT);
		RadialGradient circleFill = new RadialGradient(0,0,0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE, innerStop, outerStop);
		statusCircle.setFill(circleFill);
	}
	
	public void removeStatus(String text){
		statuses.remove(text);
	}
	
	public void showTooltip(){
		tooltipText.setText(text);
		tooltipText.setTextAlignment(TextAlignment.RIGHT);
		tooltipText.setStroke(Color.TRANSPARENT);
		tooltipRectangle.setWidth(statusCircle.getLayoutX() - tooltipText.getLayoutX());
		tooltipRectangle.setHeight(20);
		tooltipText.setVisible(true);
		tooltipRectangle.setVisible(true);
	}
	
	public void hideToolTip(){
		tooltipText.setVisible(false);
		tooltipRectangle.setVisible(false);
	}
	
}
