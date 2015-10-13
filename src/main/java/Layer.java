
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Johannes on 10.09.2015.
 */

public class Layer extends HBox {

    private final VBox parentContainer;        //Container where layers are put
    private GisVisualization gisVis;            //The drawing model
    private String name;                       //Name of the layers
    private String wktString;                  //Original WKT string entered for this layer
    private TextArea textArea;
    private LayerSelectedProperty isSelected;
    private CheckBox showOrHideCheckbox;
    private TextField layerName;
    private Image validWkt;
    private Image invalidWkt;
    private Tooltip validTooltip;
    private Tooltip invalidTooltip;
    private ImageView validityView;
    private ColorPicker colorPicker;
    private static ArrayList<Layer> layers = new ArrayList<>();

    public Layer(final GisVisualization gisVis, final VBox parentContainer, final String name,
                 final String wktString, final TextArea textArea) {
        this.gisVis = gisVis;
        this.parentContainer = parentContainer;
        this.name = name;
        this.wktString = wktString;
        this.textArea = textArea;
        this.validWkt = new Image(Main.class.getResourceAsStream("valid.png"));
        this.invalidWkt = new Image(Main.class.getResourceAsStream("invalid.png"));
        this.validTooltip = new Tooltip("All geometries in layer are valid");
        this.invalidTooltip = new Tooltip("Layer contains invalid geometries");
        this.isSelected = new LayerSelectedProperty();
        EventHandler<MouseEvent> mouseClickedHandler = event -> handleLayerMousePress();
        this.setOnMouseClicked(mouseClickedHandler);
        EventHandler<KeyEvent> keyReleasedHandler = this::handleLayerKeyPresses;
        this.setOnKeyReleased(keyReleasedHandler);

        createLayer();
    }


    /**
     * Creates a layer for this Layer object.
     */
    public final void createLayer() {
        //Create show/hide checkbox
        showOrHideCheckbox = new CheckBox();
        showOrHideCheckbox.setDisable(gisVis == null);
        showOrHideCheckbox.setOnAction(event -> {
            checkShowOrHideCheckboxes();
            redrawAll();
        });
        showOrHideCheckbox.setSelected(true);

        //Create and update layer name field
        layerName = new TextField();
        updateLayerName();

        //Create delete button
        Button deleteButton = new Button();
        Tooltip.install(deleteButton, new Tooltip("Delete layer"));
        deleteButton.setOnAction(event -> {
            if (isSelected.get()) {
                getAllSelectedLayers(false).forEach(Layer::deleteLayer);
            } else {
                deleteLayer();
            }
        });

        //Create imageView for displaying the validity of layers geometries
        validityView = new ImageView();
        validityView.setImage(validWkt);
        Tooltip.install(validityView, validTooltip);

        //Create a color picker to selected this layers color
        colorPicker = new ColorPicker();
        colorPicker.setDisable(gisVis == null);
        colorPicker.setOnAction(event -> changeColor(colorPicker.getValue()));

        addLayerStyle(showOrHideCheckbox, "layerStyles.css");
        addLayerStyle(layerName, "layerStyles.css");
        addLayerStyle(deleteButton, "layerStyles.css");

        this.setAlignment(Pos.CENTER_LEFT);
        this.getChildren().add(showOrHideCheckbox);
        this.getChildren().add(layerName);
        this.getChildren().add(colorPicker);
        this.getChildren().add(validityView);
        this.getChildren().add(deleteButton);
    }

    private void addLayerStyle(Control node, String style) {
        node.getStylesheets().add(this.getClass().getResource(
                style
        ).toExternalForm());
    }

    private void changeColor(Color value) {
        gisVis.setColor(value);
        reorderLayers();
    }

    private void updateLayerName() {
        if (gisVis != null) {
            this.name += " " + gisVis.getID();
        }
        layerName.setText(this.name);
    }

    public final void handleLayerKeyPresses(final KeyEvent event) {
        if (event.getCode() == KeyCode.DOWN) {
            //Move selected layers down
            moveSelectedLayers(1);
        } else if (event.getCode() == KeyCode.UP) {
            //Move selected layers up
            moveSelectedLayers(-1);
        }
    }

    public final void handleLayerMousePress() {
        textArea.setDisable(false);
        //CTRL is pressed select additional, otherwise unselected previously selected
        boolean oldValue = isSelected.get();
        if (!Controller.isKeyHeldDown(KeyCode.CONTROL)) {
            deselectAllLayers();
        }

        //Toggle selection and display tooltips if it is selected
        isSelected.set(!oldValue);
        if (gisVis != null) {
            gisVis.setDisplayTooltips(getIfTooltipsShouldBeDisplayed());
        }
        if (isSelected.get()) {
            showWKTString();
            requestFocus();
        }

        int numberOfSelectedLayers = getNumberOfSelectedLayers();

        if (numberOfSelectedLayers == 0) {
            textArea.clear();
            textArea.setDisable(true);
        } else if (numberOfSelectedLayers == 1) {
            getAllSelectedLayers(false).get(0).showWKTString();
        } else if (numberOfSelectedLayers > 1) {
            textArea.setDisable(true);
        }


        toggleBackgroundColor(isSelected);
    }

    /**
     * Returns a list of selected layers.
     * @return the list.
     */
    public static ArrayList<Layer> getAllSelectedLayers(final boolean filterEmpty) {
        ArrayList<Layer> selectedLayers = new ArrayList<>();
        for (Layer l : getLayers(filterEmpty)) {
            if (l.isSelected.get()) {
                selectedLayers.add(l);
            }
        }

        return selectedLayers;
    }

    public static ArrayList<Layer> getAllVisibleLayers(final boolean filterEmpty) {
        ArrayList<Layer> selectedLayers = new ArrayList<>();
        for (Layer l : getLayers(filterEmpty)) {
            if (l.showOrHideCheckbox.isSelected()) {
                selectedLayers.add(l);
            }
        }

        return selectedLayers;
    }

    /**
     * Deselects all layers, disables their tooltip and returns background color to normal.
     */
    public final void deselectAllLayers() {
        for (Layer l : getLayers(false)) {
            l.isSelected.set(false);
            if (l.gisVis != null) {
                l.gisVis.setDisplayTooltips(false);
            }
            l.toggleBackgroundColor(l.isSelected);
        }
    }

    private void deleteLayer() {
        layers.remove(this);
        reorderLayers();
        textArea.setText("");
            textArea.setDisable(getNumberOfSelectedLayers() == 0);
    }

    /**
     * Clears the WKT input text area and displays the WKT string used to draw this layer.
     */
    private void showWKTString() {
        textArea.clear();
        textArea.setText(wktString);
    }

    /**
     * Toggles the background color depending on current selection.
     * @param val   BooleanProperty to evaluate.
     */
    private void toggleBackgroundColor(final BooleanProperty val) {
        backgroundProperty().bind(Bindings.when(val)
                .then(new Background(
                        new BackgroundFill(Color.CORNFLOWERBLUE,
                                CornerRadii.EMPTY, Insets.EMPTY)))
                .otherwise(new Background(
                        new BackgroundFill(Color.TRANSPARENT,
                                CornerRadii.EMPTY, Insets.EMPTY))));
    }

    /**
     * Move all selected layers the amount of places up or down the list according to the offset.
     * @param offset    The number of places to move selected layers.
     *                  Offset > 0 moves it down the stack.
     *                  Offset < 0 moves it up the stack.
     */
    public final void moveSelectedLayers(final int offset) {
        //Keep track of all the layers that have been checked
        ArrayList<Layer> checkedLayers = new ArrayList<>();

        //Create a list of numbers from 0...n where n is the amount of layers
        //Is there an easier more convenient way to do this?
        ArrayList<Integer> iteratorList = new ArrayList<>();
        for (int i = 0; i < Layer.getLayers(false).size(); i++) {
            iteratorList.add(i);
        }

        if (offset > 0) {
            Collections.reverse(iteratorList);
        }
        for (int i : iteratorList) {
            Layer currentLayer = Layer.getLayers(false).get(i);

            if (!checkedLayers.contains(currentLayer)) {
                checkedLayers.add(currentLayer);

                if (currentLayer.isSelected.get()) {
                    Layer l = Layer.getLayers(false).remove(i);
                    int newPos = i + offset;

                    if (newPos >= Layer.getLayers(false).size()) {
                        Layer.getLayers(false).add(l);
                    } else if (newPos < 0) {
                        Layer.getLayers(false).add(0, l);
                    } else {
                        Layer.getLayers(false).add(newPos, l);
                    }
                }
            }

        }
        reorderLayers();
    }

    /**
     * Reorders the layers according to their position in the layers list.
     */
    public final void reorderLayers() {

        redrawAll();

        updateValidity();

        this.parentContainer.getChildren().remove(0, this.parentContainer.getChildren().size());

        layers.forEach(Layer::addLayerToView);
    }

    public final void setColorPickerValue(Color color) {
        colorPicker.setValue(color);
    }

    private void updateValidity() {
        Tooltip.uninstall(validityView, validTooltip);
        Tooltip.uninstall(validityView, invalidTooltip);

        if (gisVis != null && gisVis.containsInvalidGeometries()) {
            validityView.setImage(invalidWkt);
            Tooltip.install(validityView, invalidTooltip);
        } else {
            validityView.setImage(validWkt);
            Tooltip.install(validityView, validTooltip);
        }
    }

    public final void addLayerToView() {
        this.parentContainer.getChildren().add(this);
    }

    /**
     * Redraws all geometries to the canvas, in the same order as they appear in the layer view.
     * The bottom layer is drawn at the bottom of the drawing stack.
     */
    public static void redrawAll() {
        GisVisualization.reset();
        for (int i = getLayers(false).size() - 1; i >= 0; i--) {
            Layer layer = layers.get(i);
            if (layer.showOrHideCheckbox.isSelected() && layer.gisVis != null) {
                layer.gisVis.create2DShapeAndTooltips();
                layer.gisVis.setDisplayTooltips(layer.isSelected.get());
            }
        }
    }

    /**
     * Redraws the current GisVisualization if it is checked.
     * Delegates the tasks further to GisVisualization
     */
    public final void redraw2DShape() {
        if (showOrHideCheckbox.isSelected()) {
            this.gisVis.redraw2DShape();
        }
    }

    /**
     * Checks or unchecks all the selected layers.
     * This is useful for showing/hiding several layers at once.
     */
    private void checkShowOrHideCheckboxes() {
        if (isSelected.get()) {
            boolean checkedValue = showOrHideCheckbox.isSelected();
            for (Layer l : getAllSelectedLayers(true)) {
                l.showOrHideCheckbox.setSelected(checkedValue);
            }
        }
    }

    public final boolean getIfTooltipsShouldBeDisplayed() {
        return isSelected.get() && showOrHideCheckbox.isSelected();
    }

    /**
     * Returns the number of selected layers whose selected property is true.
     * @return number of selected layers.
     */
    private static int getNumberOfSelectedLayers() {
        int numberOfSelected = 0;
        for (Layer l : layers) {
            if (l.isSelected.get()) {
                numberOfSelected++;
            }
        }
        return numberOfSelected;
    }

    /**
     * Get a copy of a list of all current layers.
     * @param filterEmpty Whether to filter out the empty layers in the returned clone
     * @return a copy of the list of layers
     */
    public static ArrayList<Layer> getLayers(final boolean filterEmpty) {
        if (filterEmpty) {
            ArrayList<Layer> layerCopy = new ArrayList<>();
            for (Layer l : layers) {
                if (l.gisVis != null) {
                    layerCopy.add(l);
                }
            }

            return layerCopy;
        }
        return layers;
    }

    public final GisVisualization getGisVis() {
        return this.gisVis;
    }

    public final void setGisVis(final GisVisualization newGisVis) {
        if (newGisVis != null) {
            this.gisVis = newGisVis;
            showOrHideCheckbox.setDisable(false);
            colorPicker.setDisable(false);
        }
    }

    public final String getLayerName() {
        return layerName.getText();
    }



    public final void setName(final String name) {
        this.name = name;
        updateLayerName();
    }

    public final void setWKTString(final String wktString) {
        this.wktString = wktString;
    }

    public static Layer getSelectedLayer() {
        if (getNumberOfSelectedLayers() == 1) {
            return getAllSelectedLayers(false).get(0);
        }
        return null;
    }
}
