package de.uib.configed.dashboard;

import java.awt.Color;
import java.util.Set;

import javax.swing.UIManager;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.text.TextFlow;

public class ComponentStyler {
	private ComponentStyler() {
	}

	public static <T> void styleTableViewComponent(TableView<T> view) {
		String tableViewBackgroundColor = getHexColor(UIManager.getColor("Table.background"));
		String tableViewForegroundColor = getHexColor(UIManager.getColor("Table.foreground"));
		String tableViewSelectionBackgroundColor = getHexColor(UIManager.getColor("Table.selectionBackground"));
		String tableViewSelectionInactiveForegroundColor = getHexColor(
				UIManager.getColor("Table.selectionInactiveBackground"));
		String tableViewHeaderBackgroundColor = getHexColor(UIManager.getColor("TableHeader.background"));
		String tableViewHeaderForegroundColor = getHexColor(UIManager.getColor("TableHeader.foreground"));

		view.setStyle("-fx-background-color: #" + tableViewBackgroundColor + "; -fx-selection-bar: #"
				+ tableViewSelectionBackgroundColor + "; -fx-selection-bar-non-focused: #"
				+ tableViewSelectionInactiveForegroundColor + "; -fx-control-inner-background: #"
				+ tableViewBackgroundColor + "; -fx-text-background-color: #" + tableViewForegroundColor);

		Node columnHeaderBackground = view.lookup(".column-header-background");

		if (columnHeaderBackground != null) {
			columnHeaderBackground.setStyle("-fx-background-color: #" + tableViewHeaderBackgroundColor
					+ "; -fx-text-fill: #" + tableViewHeaderForegroundColor);
		}

		Node columnHeader = view.lookup(".column-header");

		if (columnHeader != null) {
			columnHeader.setStyle("-fx-background-color: #" + tableViewHeaderBackgroundColor + "; -fx-text-fill: #"
					+ tableViewHeaderForegroundColor);
		}

		Set<Node> scrollBars = view.lookupAll(".scroll-bar");

		if (!scrollBars.isEmpty()) {
			for (Node scrollBar : scrollBars) {
				styleScrollBarComponent((ScrollBar) scrollBar);
			}
		}
	}

	public static <T> void styleListViewComponent(ListView<T> view) {
		String listBackgroundColor = getHexColor(UIManager.getColor("List.background"));
		String listForegroundColor = getHexColor(UIManager.getColor("List.foreground"));
		String listSelectionBackgroundColor = getHexColor(UIManager.getColor("List.selectionBackground"));
		String listSelectionInactiveBackgroundColor = getHexColor(
				UIManager.getColor("List.selectionInactiveBackground"));

		view.setStyle("-fx-background-color: #" + listBackgroundColor + "; -fx-text-fill: #" + listForegroundColor
				+ "; -fx-selection-bar: #" + listSelectionBackgroundColor + "; -fx-selection-bar-non-focused: #"
				+ listSelectionInactiveBackgroundColor + "; -fx-control-inner-background: #" + listBackgroundColor);

		Node cellSelected = view.lookup(".list-cell:filled:selected");

		if (cellSelected != null) {
			cellSelected.setStyle("-fx-background-color: #" + listSelectionBackgroundColor);
		}

		Set<Node> scrollBars = view.lookupAll(".scroll-bar");

		if (!scrollBars.isEmpty()) {
			for (Node scrollBar : scrollBars) {
				styleScrollBarComponent((ScrollBar) scrollBar);
			}
		}
	}

	public static void styleScrollBarComponent(ScrollBar scrollBar) {
		String scrollbarThumbColor = getHexColor(UIManager.getColor("ScrollBar.thumb"));
		String scrollbarBackground = getHexColor(UIManager.getColor("ScrollBar.background"));
		String scrollbarTrackColor = getHexColor(UIManager.getColor("ScrollBar.track"));

		if (!scrollbarBackground.equals("") || !scrollbarThumbColor.equals("") || !scrollbarTrackColor.equals("")) {

			scrollBar.setStyle("-fx-background-color: #" + scrollbarBackground);
			scrollBar.lookup(".track").setStyle("-fx-background-color: #" + scrollbarTrackColor);
			scrollBar.lookup(".track-background").setStyle("-fx-background-color: #" + scrollbarTrackColor);
		}

		Node thumb = scrollBar.lookup(".thumb");

		if (thumb != null && !scrollbarThumbColor.equals("")) {
			thumb.setStyle("-fx-background-color: #" + scrollbarThumbColor);
		}
	}

	public static void styleTextFieldComponent(TextField textField) {
		String textFieldBackgroundColor = getHexColor(UIManager.getColor("TextField.background"));
		String textFieldForegroundColor = getHexColor(UIManager.getColor("TextField.foreground"));

		textField.setStyle(
				"-fx-background-color: #" + textFieldBackgroundColor + "; -fx-text-fill: #" + textFieldForegroundColor);
	}

	@SuppressWarnings("unchecked")
	public static <T> void styleComboBoxComponent(ComboBox<T> comboBox) {
		String comboBoxBackgroundColor = getHexColor(UIManager.getColor("ComboBox.background"));
		String comboBoxForegroundColor = getHexColor(UIManager.getColor("ComboBox.foreground"));
		String comboBoxSelectionBackgroundColor = getHexColor(UIManager.getColor("ComboBox.selectionBackground"));
		String comboBoxArrowColor = getHexColor(UIManager.getColor("ComboBox.buttonArrowColor"));

		comboBox.setStyle("-fx-background-color: #" + comboBoxBackgroundColor + "; -fx-selection-bar: #"
				+ comboBoxSelectionBackgroundColor + "; -fx-selection-bar-non-focused: #"
				+ comboBoxSelectionBackgroundColor + "; -fx-control-inner-background: #" + comboBoxBackgroundColor);

		Node listCell = comboBox.lookup(".list-cell:selected:empty");

		if (listCell != null) {
			listCell.setStyle("-fx-background-color: #" + comboBoxBackgroundColor + "; -fx-text-fill: #"
					+ comboBoxForegroundColor);
		}

		Node listView = comboBox.lookup(".list-view:vertical");

		if (listView != null) {
			styleListViewComponent((ListView<T>) listView);
		}

		Node arrowButton = comboBox.lookup(".arrow-button");

		if (arrowButton != null) {
			arrowButton.setStyle("-fx-background-color: #" + comboBoxBackgroundColor);
		}

		Node arrow = comboBox.lookup(".arrow");

		if (arrow != null) {
			arrow.setStyle("-fx-background-color: #" + comboBoxArrowColor);
		}

		// There is a bug in ComboBox, where when ListCell's style is changed,
		// the ComboBox isn't updated with a new style. It only updates itself
		// with a new style, when a popup in ComboBox is opened and closed. To
		// update ComboBox with a new style right after it is applied, we simply
		// open and close popup in ComboBox programmatically.
		comboBox.show();
		comboBox.hide();
	}

	public static void styleProgressBarComponent(ProgressBar progressBar) {
		String progressBarBackgroundColor = getHexColor(UIManager.getColor("ProgressBar.background"));
		String progressBarForegroundColor = getHexColor(UIManager.getColor("ProgressBar.foreground"));

		Node bar = progressBar.lookup(".bar");

		if (bar != null) {
			bar.setStyle("-fx-background-color: #" + progressBarForegroundColor);
		}

		Node track = progressBar.lookup(".track");

		if (track != null) {
			track.setStyle("-fx-background-color: #" + progressBarBackgroundColor);
		}
	}

	public static void styleTextFlowComponent(TextFlow textFlow) {
		String textFlowBackgroundColor = getHexColor(UIManager.getColor("TextArea.background"));
		String textFlowForegroundColor = getHexColor(UIManager.getColor("TextArea.foreground"));

		textFlow.setStyle("-fx-background-color: #" + textFlowBackgroundColor);

		ObservableList<Node> children = textFlow.getChildren();
		children.forEach(child -> child.setStyle("-fx-fill: #" + textFlowForegroundColor));
	}

	public static void styleButtonComponent(Button button) {
		String buttonBackgroundColor = getHexColor(UIManager.getColor("Button.background"));
		String buttonForegroundColor = getHexColor(UIManager.getColor("Button.foreground"));

		button.setStyle(
				"-fx-background-color: #" + buttonBackgroundColor + "; -fx-text-fill: #" + buttonForegroundColor);
	}

	public static void stylePieChartComponent(PieChart pieChart) {
		String foregroundColor = getHexColor(UIManager.getColor("Label.foreground"));

		Set<Node> pieLabelLines = pieChart.lookupAll(".chart-pie-label-line");

		for (Node pieLabelLine : pieLabelLines) {
			pieLabelLine.setStyle("-fx-stroke: #" + foregroundColor + "; -fx-fill: #" + foregroundColor + ";");
		}

		Set<Node> pieLabels = pieChart.lookupAll(".chart-pie-label");

		for (Node pieLabel : pieLabels) {
			pieLabel.setStyle("-fx-fill: #" + foregroundColor);
		}

		Node chartPieTitle = pieChart.lookup(".chart-title");
		chartPieTitle.setStyle("-fx-text-fill: #" + foregroundColor);
	}

	public static <T, V> void styleBarChartComponent(BarChart<T, V> barChart) {
		String foregroundColor = getHexColor(UIManager.getColor("Label.foreground"));
		String lighterBackgroundColor = getHexColor(
				Helper.adjustColorBrightness(UIManager.getColor("Panel.background")));

		Node chartBar = barChart.lookup(".chart-plot-background");
		Node chartBarVerticalLine = barChart.lookup(".chart-vertical-zero-line");
		Node chartBarHorizontalLine = barChart.lookup(".chart-horizontal-zero-line");
		Node chartBarTitle = barChart.lookup(".chart-title");
		chartBar.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		chartBarVerticalLine.setStyle("-fx-stroke: #" + foregroundColor);
		chartBarHorizontalLine.setStyle("-fx-stroke: #" + foregroundColor);
		chartBarTitle.setStyle("-fx-text-fill: #" + foregroundColor);

		Set<Node> chartBarAxises = barChart.lookupAll(".axis");

		for (Node chartBarAxis : chartBarAxises) {
			chartBarAxis.setStyle("-fx-tick-label-fill: #" + foregroundColor);
		}
	}

	public static String getHexColor(Color color) {
		if (color == null) {
			return "";
		}

		return Integer.toHexString(color.getRGB()).substring(2);
	}
}
