package net.ximias.gui.tabs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import net.ximias.effect.producers.FadingEffectProducer;
import net.ximias.gui.MainController;
import net.ximias.gui.ResizableCanvas;
import net.ximias.effect.views.scenes.SceneConstants;
import net.ximias.network.CurrentPlayer;
import net.ximias.persistence.Persisted;

import java.util.Map;
import java.util.TreeMap;

public class Properties {
	
	
	private MainController mainController;
	private int oldZone;
	private boolean worldWasModified = false;
	private TreeMap<String, Integer> selections;
	private static final double DEFAULT_EFFECT_INTENSITY = 1; // TODO Move to scene constants.
	private static final double DEFAULT_BACKGROUND_INTENSITY = 0.1;
	private static final double DEFAULT_BACKGROUND_BRIGHTENS = 0.5;
	private FadingEffectProducer exampleEffect = new FadingEffectProducer(Color.LIME,1500);
	private FadingEffectProducer exampleDarkEffect = new FadingEffectProducer(Color.BLACK,1500);
	
	@FXML
	private Canvas propertiesPreview = new ResizableCanvas();
	@FXML
	private ChoiceBox<String> previewBackgroundSelector;
	@FXML
	private ToggleButton debugTextToggle;
	@FXML
	private AnchorPane propertiesPreviewContainer;
	@FXML
	private Slider backgroundBrightnessSlider;
	@FXML
	private Slider backgroundIntensitySlider;
	@FXML
	private Slider effectIntensitySlider;
	
	
	@FXML
	private void addExampleEffect(ActionEvent e){
		mainController.getEffectContainer().addEffect(exampleEffect.build());
	}
	
	@FXML
	private void addExampleDarkEffect(ActionEvent e){
		mainController.getEffectContainer().addEffect(exampleDarkEffect.build());
	}
	
	@FXML
	public void restoreDefaults(ActionEvent actionEvent) {
		effectIntensitySlider.setValue(DEFAULT_EFFECT_INTENSITY);
		backgroundIntensitySlider.setValue(DEFAULT_BACKGROUND_INTENSITY);
		backgroundBrightnessSlider.setValue(DEFAULT_BACKGROUND_BRIGHTENS);
		propertiesChanged();
	}
	
	@FXML
	private void initialize(){
		propertiesPreview.widthProperty().bind(propertiesPreviewContainer.widthProperty());
		propertiesPreview.heightProperty().bind(propertiesPreviewContainer.heightProperty());
	}
	
	public void injectMainController(MainController mainController) {
		this.mainController = mainController;
		Platform.runLater(this::setupPreviewTab);
	}
	
	private void setupPreviewTab() {
		propertiesPreviewContainer.getChildren().add(propertiesPreview);
		selections = new TreeMap<>();
		selections.put("Not ingame", -1);
		selections.put("Amerish", SceneConstants.AMERISH_ID);
		selections.put("Esamir", SceneConstants.ESAMIR_ID);
		selections.put("Indar", SceneConstants.INDAR_ID);
		selections.put("Hossin", SceneConstants.HOSSIN_ID);
		previewBackgroundSelector.setItems(FXCollections.observableArrayList(selections.keySet()));
		setSelectionToCurrentZone(selections);
		previewBackgroundSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (!worldWasModified){
				worldWasModified = true;
				oldZone = Integer.valueOf(CurrentPlayer.getInstance().getValue("zone_id"));
			}
			CurrentPlayer.getInstance().setZoneId(selections.get(newValue));
			mainController.getEffectScene().updateBackground();
		});
		
		backgroundIntensitySlider.setValue(Persisted.getInstance().BACKGROUND_TRANSPARENCY_SLIDER);
		backgroundBrightnessSlider.setValue(Persisted.getInstance().BACKGROUND_BRIGHTNESS_SLIDER);
		effectIntensitySlider.setValue(Persisted.getInstance().EFFECT_TRANSPARENCY_SLIDER);
		propertiesChanged();
		addPropertyChangeListener(backgroundBrightnessSlider);
		addPropertyChangeListener(backgroundIntensitySlider);
		addPropertyChangeListener(effectIntensitySlider);
	}
	
	private void propertiesChanged(){
		Persisted persisted = Persisted.getInstance();
		persisted.EFFECT_TRANSPARENCY_SLIDER = effectIntensitySlider.getValue();
		persisted.BACKGROUND_TRANSPARENCY_SLIDER = backgroundIntensitySlider.getValue();
		persisted.BACKGROUND_BRIGHTNESS_SLIDER = backgroundBrightnessSlider.getValue();
		mainController.getEffectContainer().setEffectIntensity(effectIntensitySlider.getValue());
		mainController.getEffectScene().intensityChanged(backgroundBrightnessSlider.getValue(),backgroundIntensitySlider.getValue());
	}
	
	public void onTabChange(int newValue){
		if (newValue != 1 && worldWasModified) {
			worldWasModified = false;
			CurrentPlayer.getInstance().setZoneId(oldZone);
			mainController.getEffectScene().updateBackground();
			setSelectionToCurrentZone(selections);
		}
	}
	
	private void addPropertyChangeListener(Slider slider){
		slider.valueProperty().addListener(observable -> propertiesChanged());
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
	
	public boolean drawDebug(){
		return debugTextToggle.isSelected();
	}
	
	public Canvas getCanvas() {
		return propertiesPreview;
	}
}
