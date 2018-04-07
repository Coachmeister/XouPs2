package net.ximias.gui.guiElements;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

public class SortedStringColorTable<S extends Map.Entry<String, Color>> extends javafx.scene.control.TableView<S> {
	
	private final ObservableList<S> data;
	private final HashSet<String> selectedActions = new HashSet<>(32);
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	public SortedStringColorTable() {
		data = FXCollections.observableArrayList();
		SortedList<S> sortedData = new SortedList<>(data);
		setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
		
		sortedData.comparatorProperty().bind(comparatorProperty());
		setItems(sortedData);
		setupTableView();
	}
	
	/**
	 * Saves the current selection in the table.
	 * This operation overwrites any previous save operations with the current selection.
	 */
	public void saveSelection(){
		getSelectionModel().getSelectedItems().forEach(it-> selectedActions.add(it.getKey()));
	}
	
	/**
	 * Restores previously saved selection.
	 * Multiple restores without save will not change the selected indices.
	 */
	public void restoreSelection(){
		if (selectedActions.isEmpty()) {
			logger.info("Could not restore selection. Selected actions is empty.");
			return;
		}
		Platform.runLater(() -> {
			
			requestFocus();
			getSelectionModel().clearSelection();
			for (int i = 0; i < data.size(); i++) {
				S s = data.get(i);
				if (selectedActions.contains(s.getKey())) {
					logger.info("Selected "+s.getKey());
					getSelectionModel().select(s);
				}
			}
			selectedActions.clear();
		});
	}
	
	/**
	 * Used to set the items contained within the table.
	 * @param items a Collection of string color map entries.
	 */
	public void setItems(Collection<S> items){
		clear();
		data.addAll(items);
	}
	
	public void clear(){
		data.clear();
	}
	
	@Override
	protected double computePrefWidth(double height) {
		return 0;
	}
	
	@Override
	protected double computePrefHeight(double width) {
		return 0;
	}
	
	private void setupTableView() {
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		TableColumn<S, String> actionColumn = new TableColumn<>();
		actionColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getKey()));
		actionColumn.setText("Game action");
		actionColumn.setMinWidth(150);
		
		TableColumn<S, String> colorColumn = new TableColumn<>();
		colorColumn.setMaxWidth(150);
		colorColumn.setMinWidth(100);
		colorColumn.setText("Color");
		colorColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().toString()));
		colorColumn.setCellFactory(new Callback<TableColumn<S, String>, TableCell<S, String>>() {
			@Override
			public TableCell<S, String> call(TableColumn<S, String> param) {
				return new TableCell<S, String>(){
					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						
						if (item == null || empty) {
							setText(null);
							setStyle("");
						} else {
							setText(item);
							Color color = Color.valueOf(item);
							setBackground(new Background(new BackgroundFill(color,null, null)));
							setTextFill(color.getBrightness() > 0.5 ? Color.BLACK : Color.WHITE);
						}
					}
				};
			}
		});
		getColumns().clear();
		getColumns().addAll(actionColumn, colorColumn);
		getSortOrder().clear();
		getSortOrder().setAll(actionColumn);
	}
}
