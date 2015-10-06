import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import models.GeometryModel;
import models.ModelBoundaries;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

//import javafx.scene.Node;
//import javafx.scene.Parent;

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
    private ModelBoundaries modelBoundaries;

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
    public final void pressed() {
        drawPolygonFromWKT(queryInput.getText());

        vboxLayers.getChildren().add(new HBox());
    }

    public final void setStage(final Stage stage) {
        this.stage = stage;
    }

    /**
     * Creates and saves geometry. Calls methods to create new layer and visualization.
     * @param poly Well Known Text from user input
     */
    public final void drawPolygonFromWKT(final String poly) {
        try {
            //Create a WKT parser for reading WKT input
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            WKTReader reader = new WKTReader(geometryFactory);
            Geometry geom = reader.read(poly);

            drawPolygon(geom);

            rescaleAllGeometries();
        } catch (com.vividsolutions.jts.io.ParseException e) {
            e.printStackTrace();
        }
    }

    public final void drawPolygon(final Geometry geom) {
        if (geom instanceof GeometryCollection) {
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                refineGeometryClass(geom.getGeometryN(i));
            }
        } else {
            refineGeometryClass(geom);
        }
    }
    /**
     * Delegates the task of creating the layer for this geometry. Whether it is a plain WKT object,
     * or a composite such as a MultiPolygon.
     * @param geometry geometry to consider.
     */
    private void refineGeometryClass(final Geometry geometry) {
        if (geometry instanceof GeometryCollection) {
            createLayersFromMultiples(geometry);
        } else {
            createLayer(geometry);
        }
    }

    /**
     * Assumes the given geometry is of a multiple type, and creates a layer for each.
     * @param geometry geometry to consider.
     */
    private void createLayersFromMultiples(final Geometry geometry) {
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            createLayer(geometry.getGeometryN(i));
        }
    }

    /**
     * Creates a layer for the given geometry.
     * @param geometry geometry to draw to a layer.
     */
    private void createLayer(final Geometry geometry) {
        GisVisualization gv = GisVisualization.createVisualization(
                this.stage.getWidth(),
                this.stage.getHeight(),
                geometry,
                upperPane);
        gisVisualizations.add(gv);
        Layer hb = new Layer(gv, vboxLayers, geometry.getGeometryType());
        Layer.getLayers().add(hb);
        hb.reorderLayers();
        upperPane.requestFocus();
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
            default:
                break;
        }
    }

    // TODO
    // Concerns: dragging only works when clicking the canvas,
    // areas of pane not filled with canvas does not react
    // Possible solutions: Make a really huge canvas and translate
    // 0,0 to middle of screen. Or find another node and listener to move canvas

    /**
     * Change coordinates of all geometries by scale of current zoom and move center of view from
     * top-left corner to the middle. (0, 0) coordinate is in the middle
     */
    public final void rescaleAllGeometries() {
        double currentZoom = Math.pow(ZOOM_FACTOR, currentZoomLevel); // ZOOM_FACTOR ^ ZOOM_LEVEL;
        double centerX = upperPane.getWidth() / 2;
        double centerY = upperPane.getHeight() / 2;
        GisVisualization gv;
        GeometryModel gm;

        modelBoundaries.clear();
        // resize and redraw all geometries
        for (int i = 0; i < gisVisualizations.size(); i++) {
            gv = (GisVisualization) gisVisualizations.get(i);
            gm = gv.getGeometryModel();

            gm.transformGeometry(currentZoom, this.currentOffsetX + centerX, this.currentOffsetY + centerY);
            modelBoundaries.includeGeometry(gm.getGeometry());
            // redraw
            gisVisualizations.get(i).reDraw();
        }
        // reorder layers to maintain tooltips display correctly
        if (!Layer.getLayers().isEmpty()) {
            Layer.getLayers().get(0).reorderLayers();
        }
    }

    /**
     * Changes coordinates of all geometries.
     * @param offsetX change of X coordinates
     * @param offsetY change of Y coordinates
     */
    public final void moveAllGeometries(double offsetX, double offsetY) {
        GisVisualization gv;

        modelBoundaries.clear();
        // resize and redraw all geometries
        for (int i = 0; i < gisVisualizations.size(); i++) {
            gv = (GisVisualization) gisVisualizations.get(i);
            GeometryModel gm = gv.getGeometryModel();
            gm.moveGeometry(offsetX, offsetY);
            modelBoundaries.includeGeometry(gm.getGeometry());
            // redraw
            gisVisualizations.get(i).reDraw();
        }
        // reorder layers to maintain tooltips display correctly
        if (!Layer.getLayers().isEmpty()) {
            Layer.getLayers().get(0).reorderLayers();
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
    //TODO Concerns: dragging only works when clicking the canvas,
        //areas of pane not filled with canvas does not react
    //TODO possible solutions: Make a really huge canvas and translate 0,0 to middle of screen.
        // Or find another node and listener to move canvas

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
    }



    /*private Node isChildFocused(final Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node.isFocused()) {
                return node;
            } else if (node instanceof Parent) {
                if (isChildFocused((Parent) node) != null) {
                    return node;
                }
            }
        }
        return null;
    }*/



    public final void wktAreaKeyPressed(final KeyEvent event) {
        if (event.isAltDown() && event.getCode() == KeyCode.ENTER) {
            pressed();
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
