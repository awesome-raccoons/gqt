import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.GeometryModel;
import models.ModelBoundaries;

import java.util.ArrayList;

/**
 * Created by Johannes on 19.10.2015.
 */
public class DisplayController implements ChangeListener {

    /**
     * Identifies value of zooming (e.g 140% -> 1.4).
     */
    private static final double ZOOM_FACTOR = 1.4;
    /**
     * Sets minimum number of pixels moves needed to drag objects.
     */
    private static final int DRAG_SENSITIVITY = 3;
    private static final int PERCENT = 100;
    /**
     * Sets maximum level of zooming (ZOOM_FACTOR ^ currentZoomLevel).
     */
    private static final int MAX_ZOOM_LEVEL = 25;
    /**
     * Sets minimum level of zooming (ZOOM_FACTOR ^ currentZoomLevel).
     */
    private static final int MIN_ZOOM_LEVEL = -25;

    private static final int ZOOM_TO_SCALE_MULTIPLIER = 10;
    /**
     * Current level of zooming (0 -> default).
     */
    private int currentZoomLevel = 0;

    /**
     * current exact value of zooming used to save calculations in
     * repeating actions and for exact zoom from input field.
     */
    private double currentZoom = 1;

    /**
     * offset of X axis based on dragging.
     */
    private double currentOffsetX = 0;
    /**
     * offset of Y axis based on dragging.
     */
    private double currentOffsetY = 0;
    /**
     * used to calculate how many pixels were dragged before redrawing objects.
     */
    private double mouseMoveOffsetX = 0;
    /**
     * used to calculate how many pixels were dragged before redrawing objects.
     */
    private double mouseMoveOffsetY = 0;

    /**
     * saves position of mouse coordinates from last handler.
     */
    private double dragBaseX, dragBaseY;
    /**
     * saves position of mouse coordinates from beginning of dragging.
     */
    private double dragBeginX, dragBeginY;


    private AnchorPane parentPane;
    private TextField zoomTextField;
    private BackgroundGrid backgroundGrid;
    private Text positionX;
    private Text positionY;
    private Text zoomTextError;
    private Stage stage;

    public DisplayController(final AnchorPane parentPane,
                             final TextField zoomTextField,
                             final BackgroundGrid backgroundGrid,
                             final Text positionX, final Text positionY,
                             final Text zoomTextError, final Stage stage) {
        this.parentPane = parentPane;
        this.zoomTextField = zoomTextField;
        this.zoomTextError = zoomTextError;
        this.backgroundGrid = backgroundGrid;
        this.positionX = positionX;
        this.positionY = positionY;
        this.stage = stage;
    }


    /**
     * Calculates double value which is used for multiplying with coordinates.
     * @param zoomFactor base
     * @param zoomLevel exponent
     * @return zoomFactor^zoomLevel
     */
    private double getZoomScale(final double zoomFactor, final int zoomLevel) {
        return Math.pow(zoomFactor, zoomLevel);
    }

    /**
     * Rewrite text field with current zoom level in % .
     */
    private void updateZoomText() {
        zoomTextField.setText("" + String.format("%.2f", currentZoom * PERCENT));
    }

    /**
     * Change coordinates of all geometries by scale of current zoom and move center of view from
     * top-left corner to the middle. (0, 0) coordinate is in the middle
     */
    public final void rescaleAllGeometries() {
        double centerX = parentPane.getWidth() / 2;
        double centerY = parentPane.getHeight() / 2;
        ArrayList<GeometryModel> geometryModelList;
        AnchorPane plotViewGroup = GisVisualization.getGroup();
        //Make sure to reset the GisVisualization, this empties the canvas and tooltips
        GisVisualization.reset();
        updateZoomText();

        // resize and redraw all geometries
        for (int i = Layer.getLayers(true).size() - 1; i >= 0; i--) {
            Layer layer = Layer.getLayers(true).get(i);
            GisVisualization gisVisualization = layer.getGisVis();
            geometryModelList = gisVisualization.getGeometryModelList();
            for (GeometryModel gm : geometryModelList) {
                gm.transformGeometry(currentZoom,
                        this.currentOffsetX + centerX,
                        this.currentOffsetY + centerY);
            }
            // redraw
            layer.redraw2DShape();
            //Move and add all tooltips
            gisVisualization.moveTooltips();
            //Only add them if the corresponding layer is checked and selected.
            if (layer.getIfTooltipsShouldBeDisplayed()) {
                plotViewGroup.getChildren().addAll(gisVisualization.getTooltips());
            }
        }

        backgroundGrid.setCurrentWidth(centerX);
        backgroundGrid.setCurrentHeight(centerY);
        //Redraw the grid...
        int scaling = (int) (currentZoom * ZOOM_TO_SCALE_MULTIPLIER);
        backgroundGrid.scaleGrid(scaling, scaling,
                currentOffsetX + centerX, currentOffsetY + centerY);
    }

    /**
     * Changes X coordinates of all geometries by offsetX.
     * Changes Y coordinates of all geometries by offsetY.
     * @param offsetX change of X coordinates
     * @param offsetY change of Y coordinates
     */
    private void moveAllGeometries(final double offsetX, final double offsetY) {
        AnchorPane plotViewGroup = GisVisualization.getGroup();

        //Make sure to reset the GisVisualization, this empties the canvas and tooltips
        GisVisualization.reset();
        // resize and redraw all geometries
        for (int i = Layer.getLayers(true).size() - 1; i >= 0; i--) {
            Layer layer = Layer.getLayers(true).get(i);
            GisVisualization gisVisualization = layer.getGisVis();

            // moving geometries to a List decreased dragging performance
            for (GeometryModel gm : gisVisualization.getGeometryModelList()) {
                gm.moveGeometry(offsetX, offsetY);
            }
            // redraw
            layer.redraw2DShape();
            //Move and add all tooltips
            gisVisualization.moveTooltips();
            //Only add them if the corresponding layer is checked and selected.
            if (layer.getIfTooltipsShouldBeDisplayed()) {
                plotViewGroup.getChildren().addAll(gisVisualization.getTooltips());
            }
        }

        //Redraw grid
        backgroundGrid.moveGrid((int) (currentZoom * ZOOM_TO_SCALE_MULTIPLIER),
                (int) (currentZoom * ZOOM_TO_SCALE_MULTIPLIER), offsetX, offsetY);
    }

    /**
     * Gets value of zoom.
     * ZOOM_FACTOR ^ result = x
     * @param x number to calculate value for
     * @return log_zoomfactor(x) which is equal to log(x)/log(ZOOM_FACTOR)
     */
    private double logZoomFactor(final double x) {
        return Math.log(x) / Math.log(ZOOM_FACTOR);
    }

    /**
     * Creates new ModelBoundaries object which includes geometries from list of layers.
     * @param layers list of layers that have to be included in ModelBoundaries
     * @return ModelBoundaries class updated to reflect all geometries boundaries
     */
    private ModelBoundaries fillBoundariesWithLayers(final ArrayList<Layer> layers) {
        ModelBoundaries boundaries = new ModelBoundaries();
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer layer = layers.get(i);
            GisVisualization gisVisualization = layer.getGisVis();

            for (GeometryModel gm : gisVisualization.getGeometryModelList()) {
                boundaries.includeGeometry(gm.getOriginalGeometry());
            }
        }
        return boundaries;
    }

    /**
     * Gets boundaries for all layers and find best zoom and position for it.
     */
    public final void zoomToFitAll() {
        ModelBoundaries modelBoundaries = fillBoundariesWithLayers(Layer.getLayers(true));
        zoomToFit(modelBoundaries);
    }

    /**
     * Gets boundaries for all selected layers and find best zoom and position for it.
     */
    public final void zoomToFitSelected() {
        ModelBoundaries modelBoundaries =
                fillBoundariesWithLayers(Layer.getAllSelectedLayers(true));
        zoomToFit(modelBoundaries);
    }

    /**
     * Gets boundaries for all visible layers and find best zoom and position for it.
     */
    public final void zoomToFitVisible() {
        ModelBoundaries modelBoundaries =
                fillBoundariesWithLayers(Layer.getAllVisibleLayers(true));
        zoomToFit(modelBoundaries);
    }

    /**
     * Calculates best zoom level for current modelBoundaries and upperPane size.
     * @param modelBoundaries used to get size and position of geometries
     * @return integer value representing zoom level that all geometries fits into it
     */
    private int getBestZoomLevel(final ModelBoundaries modelBoundaries) {
        double scaleX = parentPane.getWidth() / modelBoundaries.getWidth();
        double scaleY = parentPane.getHeight() / modelBoundaries.getHeight();
        if (modelBoundaries.getWidth() == 0) {
            scaleX = 1;
        }
        if (modelBoundaries.getHeight() == 0) {
            scaleY = 1;
        }
        double scaleMin = Math.min(scaleX, scaleY);
        double zoomLevel;
        int bestZoomLevel;

        zoomLevel = logZoomFactor(scaleMin);
        // correction because of truncating negative numbers in a way that doesn't fit this purpose
        if (zoomLevel < 0) {
            zoomLevel--;
        }
        bestZoomLevel = (int) zoomLevel;
        bestZoomLevel = applyLimits(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL, bestZoomLevel);
        return bestZoomLevel;
    }

    /**
     * Scales geometries to fit to current view.
     */
    public final void zoomToFit(final ModelBoundaries modelBoundaries) {
        if (modelBoundaries.isNull()) {
            return;
        }
        currentZoomLevel = getBestZoomLevel(modelBoundaries);

        double zoomScale = getZoomScale(ZOOM_FACTOR, currentZoomLevel);
        this.currentOffsetX = -(modelBoundaries.getMiddleX() * zoomScale);
        this.currentOffsetY = -(modelBoundaries.getMiddleY() * zoomScale);


        //Set values in backgroundgrid
        backgroundGrid.resetOffsets();
        backgroundGrid.addXOffset(currentOffsetX);
        backgroundGrid.addYOffset(currentOffsetY);

        setZoomLevel();
        rescaleAllGeometries();
    }

    /**
     * Check zoom limits and calculate actual zoom factor.
     */
    private void setZoomLevel() {
        this.currentZoomLevel = applyLimits(MIN_ZOOM_LEVEL,
                MAX_ZOOM_LEVEL,
                this.currentZoomLevel);
        currentZoom = getZoomScale(ZOOM_FACTOR, this.currentZoomLevel);

    }

    /**
     * Controls if value is within limits.
     * @param min lower limit
     * @param max upper limit
     * @param value current value to compare
     * @return true if value is in interval, otherwise false
     */
    private boolean isInLimits(final int min, final int max, final int value) {
        if (value > max) {
            return false;
        } else if (value < min) {
            return false;
        }
        return true;
    }

    /**
     * Controls and updates value if necessary.
     * @param min lower limit
     * @param max upper limit
     * @param value current value to compare
     * @return value or appropriate limit value if value is out of interval
     */
    private int applyLimits(final int min, final int max, final int value) {
        int correctValue = value;
        if (value > max) {
            correctValue = max;
        } else if (value < min) {
            correctValue = min;
        }
        return correctValue;
    }



    /**
     * reset view to default position centered around (0 0) with 100% zoom.
     */
    public final void resetView() {
        currentZoomLevel = 0;
        this.currentOffsetX = 0;
        this.currentOffsetY = 0;

        backgroundGrid.resetOffsets();
        setZoomLevel();
        rescaleAllGeometries();
    }

    /**
     * increase zoom level, calculate zoom factor and rescale all geometries with this factor.
     */
    public final void zoomIn() {
        currentZoomLevel++;
        setZoomLevel();
        rescaleAllGeometries();
    }

    /**
     * decrease zoom level, calculate zoom factor and rescale all geometries with this factor.
     */
    public final void zoomOut() {
        currentZoomLevel--;
        setZoomLevel();
        rescaleAllGeometries();
    }

    /**
     * Calculates and updates coordinates displayed to match coordinates of geometries.
     * @param sceneX mouse pointer x coordinate
     * @param sceneY mouse pointer y coordinate
     */
    public final void updateCoordinatesText(final double sceneX, final double sceneY) {
        // calculate
        double centerX = parentPane.getWidth() / 2;
        double centerY = parentPane.getHeight() / 2;
        double positionX = (sceneX - currentOffsetX - centerX) / currentZoom;
        double positionY = -(sceneY - currentOffsetY - centerY) / currentZoom;
        // update text
        this.positionX.setText("X: " + (int) positionX);
        this.positionY.setText("Y: " + (int) positionY);
    }

    public final void upperPaneMousePressed(final MouseEvent event) {
        backgroundGrid.hideContextMenu();

        parentPane.requestFocus();
        dragBaseX = event.getSceneX();
        dragBaseY = event.getSceneY();
        dragBeginX = dragBaseX;
        dragBeginY = dragBaseY;
    }

    /**
     * Called when upperPane is being dragged and drag it accordingly.
     * Also sets cursor to Cursor.Move.
     * @param event MouseEvent to react to.
     */
    public final void upperPaneMouseDragged(final MouseEvent event) {
        this.stage.getScene().setCursor(Cursor.MOVE);
        mouseMoveOffsetX += (event.getSceneX() - dragBaseX);
        dragBaseX = event.getSceneX();
        mouseMoveOffsetY += (event.getSceneY() - dragBaseY);
        dragBaseY = event.getSceneY();
        // performance improvement
        if (Math.abs(mouseMoveOffsetX) > DRAG_SENSITIVITY
                || Math.abs(mouseMoveOffsetY) > DRAG_SENSITIVITY) {
            moveAllGeometries(mouseMoveOffsetX, mouseMoveOffsetY);
            mouseMoveOffsetX = 0;
            mouseMoveOffsetY = 0;
        }
    }

    /**
     * Called when mouse releases on upperPane. Makes sure cursor goes back to normal.
     */
    public final void upperPaneMouseReleased(final MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            backgroundGrid.showContextMenu(event.getScreenX(), event.getScreenY());
        }

        this.stage.getScene().setCursor(Cursor.DEFAULT);
        this.currentOffsetX += event.getSceneX() - dragBeginX;
        this.currentOffsetY += event.getSceneY() - dragBeginY;
        moveAllGeometries(mouseMoveOffsetX, mouseMoveOffsetY);
        mouseMoveOffsetX = 0;
        mouseMoveOffsetY = 0;
        //calculateBoundaries();
    }

    /**
     * Handler for applying exact zoom factor received from user.
     * Zoom factor is read from zoomText TextField
     * @param event key event
     */
    public final void zoomTextKeyPressed(final KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            // set only when valid input is read
            try {
                double zoomFactor = Double.parseDouble(zoomTextField.getText());
                zoomTextError.setVisible(false);
                zoomFactor /= PERCENT;
                if (isInLimits(MIN_ZOOM_LEVEL,
                        MAX_ZOOM_LEVEL,
                        (int) logZoomFactor(zoomFactor))) {
                    currentZoom = zoomFactor;
                    rescaleAllGeometries();
                    currentZoomLevel = (int) logZoomFactor(zoomFactor);
                }
            } catch (NumberFormatException e) {
                // display error message when input is invalid
                zoomTextError.setVisible(true);
            }

        }
    }


    @Override
    public final void changed(final ObservableValue observable,
                              final Object oldValue,
                              final Object newValue) {
        rescaleAllGeometries();
    }
}
