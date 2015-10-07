import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.GeometryModel;
import models.ModelBoundaries;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.rmi.MarshalException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Controller {
    /**
     * Identifies value of zooming (e.g 140% -> 1.4).
     */
    private static final double ZOOM_FACTOR = 1.4;
    /**
     * Current level of zooming (0 -> default).
     */
    private int currentZoomLevel = 0;

    private double currentOffsetX = 0;
    private double currentOffsetY = 0;

    private ModelBoundaries modelBoundaries;

    @FXML
    private TextArea queryInput;
    @FXML
    private AnchorPane upperPane;
    @FXML
    private VBox vboxLayers;

    private double dragBaseX, dragBaseY;
    private double dragBase2X, dragBase2Y;
    private double dragBeginX, dragBeginY;
    private Stage stage;

    /**
     * Stored all GisVisualizations.
     */
    private final Vector<GisVisualization> gisVisualizations;
    private static List<KeyCode> heldDownKeys = new ArrayList<>();

    public Controller() {
        gisVisualizations = new Vector(1, 1);
        modelBoundaries = new ModelBoundaries();

    }

    @FXML
    public final void updateLayer() {
        drawPolygonFromWKT(queryInput.getText());
    }

    public final void createEmptyLayer() {
        Layer l = new Layer(null, vboxLayers, null, "", queryInput);
        Layer.getLayers(false).add(l);
        l.addLayerToView();
        //To ensure the latest new layer will be selected.
        l.handleLayerMousePress();
    }

    public final void setStage(final Stage stage) {
        this.stage = stage;
    }

    /**
     * Creates and saves geometry. Calls methods to create new layer and visualization.
     * @param poly Well Known Text from user input
     */
    public final void drawPolygonFromWKT(final String poly) {
        if (poly.equals("")) {
            showWKTParseErrorMessage();
        } else {
            try {
                //Create a WKT parser for reading WKT input
                GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
                WKTReader reader = new WKTReader(geometryFactory);
                Geometry geom = reader.read(poly);

                drawGeometry(geom, poly);
                modelBoundaries.includeGeometry(geom);
                rescaleAllGeometries();
            } catch (com.vividsolutions.jts.io.ParseException e) {
                showWKTParseErrorMessage();
            }
        }
    }

    public final void drawGeometry(final Geometry geom, final String poly) {
        if (geom instanceof GeometryCollection) {
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                refineGeometryClass(geom.getGeometryN(i), poly);
            }
        } else {
            refineGeometryClass(geom, poly);
        }
    }

    /**
     * Displays an alert dialog when trying to draw an invalid WKT string.
     */
    public final void showWKTParseErrorMessage() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error parsing WKT");
        alert.setHeaderText("Invalid WKT");
        String s = "The WKT string entered is of unknown geometry type ";
        alert.setContentText(s);
        alert.show();
    }

    /**
     * Delegates the task of creating the layer for this geometry. Whether it is a plain WKT object,
     * or a composite such as a MultiPolygon.
     * @param geometry geometry to consider.
     */
    private void refineGeometryClass(final Geometry geometry, final String poly) {
        if (geometry instanceof GeometryCollection) {
            createLayersFromMultiples(geometry, poly);
        } else {
            updateLayerFigure(geometry, poly);
        }
    }

    /**
     * Assumes the given geometry is of a multiple type, and creates a layer for each.
     * @param geometry geometry to consider.
     */
    private void createLayersFromMultiples(final Geometry geometry, final String poly) {
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            updateLayerFigure(geometry.getGeometryN(i), poly);
        }
    }

    /**
     * Creates a layer for the given geometry.
     * @param geometry geometry to draw to a layer.
     */
    private void updateLayerFigure(final Geometry geometry, final String poly) {
        Layer l = Layer.getSelectedLayer();
        if (l != null) {
            if (l.getGisVis() == null) {
                l.setGisVis(GisVisualization.createVisualization(
                        geometry,
                        upperPane));
            } else {
                l.getGisVis().setGeometryModel(geometry);
            }
            l.setWKTString(poly);
            l.setName(geometry.getGeometryType());

            l.reorderLayers();
            upperPane.requestFocus();
        }
    }


    // TODO
    // Concerns: dragging only works when clicking the canvas,
    // areas of pane not filled with canvas does not react
    // Possible solutions: Make a really huge canvas and translate
    // 0,0 to middle of screen. Or find another node and listener to move canvas


    public final double getZoomScale(double zoomFactor, int zoomLevel) {
        return Math.pow(zoomFactor, zoomLevel);
    }
    /**
     * Change coordinates of all geometries by scale of current zoom and move center of view from
     * top-left corner to the middle. (0, 0) coordinate is in the middle
     */
    public final void rescaleAllGeometries() {
        double currentZoom = getZoomScale(ZOOM_FACTOR, currentZoomLevel);  // ZOOM_FACTOR ^ ZOOM_LEVEL;
        double centerX = upperPane.getWidth() / 2;
        double centerY = upperPane.getHeight() / 2;
        GisVisualization gv;
        AnchorPane plotViewGroup = GisVisualization.getGroup();
        //Make sure to reset the GisVisualization, this empties the canvas and tooltips
        GisVisualization.reset();

        // resize and redraw all geometries
        for (int i = Layer.getLayers(true).size() - 1; i >= 0; i--) {
            Layer layer = Layer.getLayers(true).get(i);
            GisVisualization gisVisualization = layer.getGisVis();
            GeometryModel gm = gisVisualization.getGeometryModel();
            gm.transformGeometry(currentZoom, this.currentOffsetX + centerX, this.currentOffsetY + centerY);
            // redraw
            layer.redraw2DShape();
            //Move and add all tooltips
            gisVisualization.moveTooltips();
            //Only add them if the corresponding layer is checked and selected.
            if (layer.getIfTooltipsShouldBeDisplayed()) {
                plotViewGroup.getChildren().addAll(gisVisualization.getTooltips());
            }
        }
    }

    /**
     * Changes coordinates of all geometries.
     * @param offsetX change of X coordinates
     * @param offsetY change of Y coordinates
     */
    public final void moveAllGeometries(double offsetX, double offsetY) {
        GisVisualization gv;
        AnchorPane plotViewGroup = GisVisualization.getGroup();

        //Make sure to reset the GisVisualization, this empties the canvas and tooltips
        GisVisualization.reset();
        // resize and redraw all geometries
        for (int i = Layer.getLayers(true).size() - 1; i >= 0; i--) {
            Layer layer = Layer.getLayers(true).get(i);
            GisVisualization gisVisualization = layer.getGisVis();
            GeometryModel gm = gisVisualization.getGeometryModel();
            gm.moveGeometry(offsetX, offsetY);

            // redraw
            layer.redraw2DShape();
            //Move and add all tooltips
            gisVisualization.moveTooltips();
            //Only add them if the corresponding layer is checked and selected.
            if (layer.getIfTooltipsShouldBeDisplayed()) {
                plotViewGroup.getChildren().addAll(gisVisualization.getTooltips());
            }
        }
    }

    private final double logZoomFactor(double x) {
        return Math.log(x)/Math.log(ZOOM_FACTOR);
    }

    /**
     * Scales geometries to fit to current view.
     * @param fitBoundaries ModelBoundaries of objects that has to be included in a view
     */
    public final void zoomToFit() {
        double scaleX = upperPane.getWidth() / this.modelBoundaries.getWidth();
        double scaleY = upperPane.getHeight() / this.modelBoundaries.getHeight();
        double scaleMin = Math.min(scaleX, scaleY);
        double zoomLevel;

        zoomLevel = (int) logZoomFactor(scaleMin);
        // correction because of truncating negative numbers in a way that doesn't fit this purpose
        if (zoomLevel < 0) {
            zoomLevel--;
        }
        currentZoomLevel = (int)zoomLevel;

        double zoomScale = getZoomScale(ZOOM_FACTOR, currentZoomLevel);
        this.currentOffsetX = - (this.modelBoundaries.getMiddleX() * zoomScale);
        this.currentOffsetY = - (this.modelBoundaries.getMiddleY() * zoomScale);

        rescaleAllGeometries();
    }

    public final void resetView() {
        currentZoomLevel = 0;
        this.currentOffsetX = 0;
        this.currentOffsetY = 0;

        rescaleAllGeometries();
    }
    public final void zoomIn() {
        currentZoomLevel++;
        rescaleAllGeometries();
    }

    public final void zoomOut() {
        currentZoomLevel--;
        rescaleAllGeometries();
    }

    public final void handleUpperPaneKeyPresses(final KeyEvent event) {
        switch (event.getText()) {
            case "+":
                zoomIn();

                break;
            case "-":
                zoomOut();
                break;
            case "*":
                zoomToFit();
                break;
            case "/":
                resetView();
            default:
                break;
        }
    }


    public final void mouseScrollEvent(final ScrollEvent event) {
        // scroll down
        if (event.getDeltaY() < 0) {
            zoomOut();
        } else { // scroll up
            zoomIn();
        }
    }

    /**
     * Called when the mouse press the upperPane.
     * This pane will receive focus and save the coordinates of the press to be used for dragging.
     * @param event MouseEvent to react to.
     */
    public final void upperPaneMousePressed(final MouseEvent event) {
        upperPane.requestFocus();

        dragBaseX = upperPane.translateXProperty().get();
        dragBaseY = upperPane.translateYProperty().get();
        dragBase2X = event.getSceneX();
        dragBase2Y = event.getSceneY();
        dragBeginX = dragBase2X;
        dragBeginY = dragBase2Y;
    }

    /**
     * Called when upperPane is being dragged and drag it accordingly.
     * Also sets cursor to Cursor.Move.
     * @param event MouseEvent to react to.
     */
    public final void upperPaneMouseDragged(final MouseEvent event) {
        this.stage.getScene().setCursor(Cursor.MOVE);
        double offsetX = (event.getSceneX() - dragBase2X);
        dragBase2X = event.getSceneX();
        double offsetY = (event.getSceneY() - dragBase2Y);
        dragBase2Y = event.getSceneY();
        moveAllGeometries(offsetX, offsetY);
    }

    /**
     * Called when mouse releases on upperPane. Makes sure cursor goes back to normal.
     */
    public final void upperPaneMouseReleased(final MouseEvent event) {
        this.stage.getScene().setCursor(Cursor.DEFAULT);
        this.currentOffsetX += event.getSceneX() - dragBeginX;
        this.currentOffsetY += event.getSceneY() - dragBeginY;
        //calculateBoundaries();
    }

    public final void wktAreaKeyPressed(final KeyEvent event) {
        if (event.isAltDown() && event.getCode() == KeyCode.ENTER) {
            updateLayer();
        }
    }

    public final void onAnyKeyPressed(final KeyEvent event) {
        if (!heldDownKeys.contains(event.getCode())) {
            heldDownKeys.add(event.getCode());
        }
    }

    public final void onAnyKeyReleased(final KeyEvent event) {
        heldDownKeys.remove(event.getCode());
    }

    public static boolean isKeyHeldDown(final KeyCode code) {
        return heldDownKeys.contains(code);
    }
}
