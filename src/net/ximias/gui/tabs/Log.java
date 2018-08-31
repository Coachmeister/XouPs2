package net.ximias.gui.tabs;

import com.sun.javafx.scene.control.skin.ListViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.Callback;
import net.ximias.datastructures.RandomAccessFileTextReader;
import net.ximias.datastructures.collections.EvictingObservableList;
import net.ximias.gui.MainController;
import net.ximias.logging.Category;
import net.ximias.logging.CollectionLogAppender;
import net.ximias.logging.CollectionLogReciever;
import net.ximias.logging.FileLogAppender;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class Log implements CollectionLogReciever {
	@FXML
	public ListView<String> list;
	@FXML
	public ChoiceBox<String> categoryChoice;
	@FXML
	public ChoiceBox<String> levelChoice;
	@FXML
	public CheckBox filtersAnd;
	private static final int LINES_PR_SCREEN = 100;
	private static final int BUFFERED_SCREENS = 1;
	private static final int LINES_TO_READ = (BUFFERED_SCREENS*LINES_PR_SCREEN)/2;
	private static final char COMPARE_SUBSTRING_CHAR = '[';
	
	/*
	* 2 bytes pr char
	* 250 char pr line
	* 150 lines pr screen
	
	* 75000 bytes pr screen
	* 10% of screen pr scrollInAbove
	* */
	private VirtualFlow<IndexedCell<String>> visibleItems;
	private RandomAccessFileTextReader fileReader;
	private ScrollBar verticalScroll;
	private final EvictingObservableList<String> recentMessages = new EvictingObservableList<>(LINES_PR_SCREEN);
	private final EvictingObservableList<String> pastMessages = new EvictingObservableList<>(BUFFERED_SCREENS * LINES_PR_SCREEN);
	private boolean autoscroll = true;
	private boolean cached = true;
	private boolean topReached = false;
	private final String[] SEVERE_FILTER = {"SEVERE"};
	private final String[] WARNING_FILTER = {"SEVERE", "WARNING"};
	private final String[] INFO_FILTER = {"SEVERE", "WARNING", "INFO"};
	private String[] currentFilter = INFO_FILTER;
	LinkedList<String> toRemove = new LinkedList<>();
	
	public void injectMainController(MainController controller, RandomAccessFile logFile){
		fileReader = new RandomAccessFileTextReader(logFile, FileLogAppender.UTF16);
		recentMessages.add("Log tab initializing...");
		populateFilters();
		setupListView();
		
		
		controller.addProjectLevelLoggerHandler(new CollectionLogAppender(this));
	}
	
	private void populateFilters() {
		LinkedList<String> categories = new LinkedList<>();
		categories.add("All");
		Arrays.stream(Category.values()).forEach(it-> categories.add(it.getName()));
		categoryChoice.setItems(FXCollections.observableList(categories));
		categoryChoice.getSelectionModel().select("All");
		
		levelChoice.setItems(FXCollections.observableArrayList("SEVERE","WARNING", "INFO"));
		levelChoice.getSelectionModel().select("INFO");
		levelChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			switch (newValue) {
				case "SEVERE":
					currentFilter = SEVERE_FILTER;
					System.out.println("Severe");
					break;
				case "WARNING":
					System.out.println("Warning");
					currentFilter = WARNING_FILTER;
					break;
				default:
					System.out.println("Default");
					currentFilter = INFO_FILTER;
					break;
			}
			applyFilter();
		});
		categoryChoice.valueProperty().addListener(observable -> applyFilter());
		filtersAnd.selectedProperty().addListener(observable -> applyFilter());
		applyFilter();
	}
	
	private void applyFilter() {
			recentMessages.clear();
			pastMessages.clear();
			readPastLines();
			recentMessages.addAll(pastMessages);
	}
	
	private boolean containsFilter(String it) {
		boolean containsFilter = false;
		for (String s : currentFilter) {
			if (it.contains("[" + s + "]")){
				containsFilter = true;
				break;
			}
		}
		return containsFilter;
	}
	
	private void setupListView(){
		verticalScroll = getScrollbar();
		if (verticalScroll == null) {
			throw new Error("Scrollbar not found");
		}
		
		list.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> param) {
				return new ListCell<String>(){
					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (item == null || empty) {
							setText(null);
							setStyle("");
						} else {
							setText(item);
							getStyleClass().clear();
							getStyleClass().add("list-cell");
							if (item.contains("[INFO]")){
								getStyleClass().add("info");
							}
							if (item.contains("[WARNING]")) {
								getStyleClass().add("warning");
							}
							if (item.contains("[SEVERE]")) getStyleClass().add("severe");
						}
					}
				};
			}
		});
		
		recentMessages.addListener((ListChangeListener<? super String>) (c) -> {
			if (autoscroll) verticalScroll.setValue(verticalScroll.getMax());
		});
		
		verticalScroll.valueProperty().addListener((observable, oldValue, newValue) -> {
			double position = newValue.doubleValue();
			ScrollBar scrollBar = getScrollbar();
			if (position == scrollBar.getMax()){
				bottomReached();
			}else if (position == scrollBar.getMin()){
				topReached();
			}
		});
		// I left the warning until I know that this will always work.
		visibleItems = (VirtualFlow<IndexedCell<String>>) ((ListViewSkin<?>)list.getSkin()).getChildren().get(0);
		
		list.setItems(recentMessages);
	}
	
	private ScrollBar getScrollbar() {
		for (Node node : list.lookupAll(".scroll-bar")){
			if (node instanceof ScrollBar){
				if (((ScrollBar) node).getOrientation() == Orientation.VERTICAL) return (ScrollBar) node;
			}
		}
		return null;
	}
	
	private void topReached() {
		if (recentMessages.size() < LINES_PR_SCREEN) return;
		scrollUp();
	}
	
	private void bottomReached() {
		if (cached) return;
		if (pastMessages.getLast().equals(recentMessages.getLast()) ){
			autoscroll = true;
			cached = true;
			list.setItems(recentMessages);
		}else {
			scrollDown();
		}
	}
	
	private void scrollUp(){
		System.out.println("Scroll up");
		if (cached){
			list.setItems(pastMessages);
			pastMessages.addAll(recentMessages);
			cached = false;
			readPastLines();
		}else if (!topReached){
			readPastLines();
		}
	}
	
	private void scrollDown(){
		if (!cached){
			readFutureLines();
		}
	}
	
	private void readFutureLines() {
		String lastItem = visibleItems.getFirstVisibleCell().getItem();
		try {
			fileReader.binarySearchLine(pastMessages.getLast(),Comparator.comparing(s -> {
				System.out.println(s);
				if (!s.contains(COMPARE_SUBSTRING_CHAR+"")){
					return "0";
				}
				return s.substring(0, s.indexOf(COMPARE_SUBSTRING_CHAR));
			}));
			for (int i = 0; i < LINES_TO_READ; i++) {
				if (!fileReader.hasNext()) {
					bottomReached();
					break;
				}
				
				
				String line = fileReader.readNextLine();
				if (inFilter(line)) {
					pastMessages.addLast(line);
				}else {
					i--;
				}
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		verticalScroll.setValue((verticalScroll.getMax()-verticalScroll.getMin())/2+verticalScroll.getMin());
		list.scrollTo(lastItem);
	}
	
	
	private void readPastLines() {
		String target;
		String firstItem;
		if (pastMessages.isEmpty()) {
			target = null;
			firstItem = null;
		} else {
			target = pastMessages.getFirst();
			firstItem = visibleItems.getFirstVisibleCell().getItem();
		}
		try {
			if (target != null) {
				fileReader.binarySearchLine(target,Comparator.comparing(s -> {
					System.out.println(s);
					if (!s.contains(COMPARE_SUBSTRING_CHAR+"")){
						return "z";
					}
					return s.substring(0, s.indexOf(COMPARE_SUBSTRING_CHAR));
				}));
			}else {
				fileReader.seek(fileReader.endOfFile());
			}
			for (int i = 0; i < LINES_TO_READ; i++) {
				if (!fileReader.hasPrevious()) {
					System.out.println("Top reached");
					topReached = true;
					break;
				}
				String line = fileReader.readPreviousLine();
				if (inFilter(line)) {
					pastMessages.addFirst(line);
				}else {
					i--;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Platform.runLater(() -> {
			if (firstItem != null){
				list.scrollTo(firstItem);
			}
		});
	}
	
	@Override
	public void receiveMessage(String logMessage) {
		if (inFilter(logMessage)){
			Platform.runLater(()->recentMessages.addLast(logMessage));
		}
	}
	
	private boolean inFilter(String string){
		if (filtersAnd.isSelected()){
			return inLevelFilter(string) && inCategoryFilter(string);
		}else{
			return inLevelFilter(string) || inCategoryFilter(string);
		}
	}
	
	private boolean inLevelFilter(String string) {
		for (String s : currentFilter) {
			if (string.contains("["+s+"]")) {
				return true;
			}
		}
		return false;
	}
	
	private boolean inCategoryFilter(String string){
		if (categoryChoice.getValue().equals("All")) return true;
		return string.contains("["+categoryChoice.getValue().toUpperCase()+"]");
	}
}
