import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.geotools.geometry.jts.JTSFactoryFinder;


public class Controller {

    private final double zoomFactor = 1.4;

    @FXML
    private Button submit;
    @FXML
    private TextArea queryInput;
    @FXML
    private AnchorPane upperPane;
    @FXML
    private VBox vboxLayers;

    private double dragBaseX, dragBaseY;
    private double dragBase2X, dragBase2Y;

    @FXML
    public final void pressed() {
        drawPolygon(queryInput.getText());

        vboxLayers.getChildren().add(new HBox());
    }

    public final void drawPolygon(final String poly) {
        try {
            //Create a WKT parser for reading WKT input
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            WKTReader reader = new WKTReader(geometryFactory);
            Geometry p = reader.read(poly);

            if (p instanceof GeometryCollection) {
                for (int i = 0; i < p.getNumGeometries(); i++) {
                    refineGeometryClass(p.getGeometryN(i));
                }
            } else {
                refineGeometryClass(p);
            }
        } catch (com.vividsolutions.jts.io.ParseException e) {
            e.printStackTrace();
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
                        Main.stage.getWidth(),
                        Main.stage.getHeight(),
                        geometry,
                        upperPane);

        Layer hb = new Layer(gv, vboxLayers, geometry.getGeometryType());
        Layer.getLayers().add(hb);
        hb.reorderLayers();
        upperPane.requestFocus();
    }

    private void zoom(final double d) {
        upperPane.scaleXProperty().set(upperPane.scaleXProperty().get() * d);
        upperPane.scaleYProperty().set(upperPane.scaleYProperty().get() * d);
    }


    public final void handleUpperPaneKeyPresses(final KeyEvent event) {
        if (event.getText().equals('+')) {
            zoom(zoomFactor);
        } else if (event.getText().equals('-')) {
            zoom(1 / zoomFactor);
        }
    }

    // TODO
    // Concerns: dragging only works when clicking the canvas,
    // areas of pane not filled with canvas does not react
    // Possible solutions: Make a really huge canvas and translate
    // 0,0 to middle of screen. Or find another node and listener to move canvas


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
    }

    /**
     * Called when upperPane is being dragged and drag it accordingly.
     * Also sets cursor to Cursor.Move.
     * @param event MouseEvent to react to.
     */
    public final void upperPaneMouseDragged(final MouseEvent event) {
        Main.stage.getScene().setCursor(Cursor.MOVE);
        upperPane.setTranslateX(dragBaseX + (event.getSceneX() - dragBase2X));
        upperPane.setTranslateY(dragBaseY + (event.getSceneY() - dragBase2Y));
    }

    /**
     * Called when mouse releases on upperPane. Makes sure cursor goes back to normal.
     */
    public final void upperPaneMouseReleased() {
        Main.stage.getScene().setCursor(Cursor.DEFAULT);
    }

}
