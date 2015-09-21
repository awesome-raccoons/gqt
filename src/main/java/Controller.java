
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;
import javafx.event.Event;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.geotools.geometry.jts.JTSFactoryFinder;


public class Controller {

    @FXML
    private Button submit;
    @FXML
    private TextArea queryInput;
    @FXML
    private AnchorPane upperPane;
    @FXML
    public VBox vboxLayers;

    private double dragBaseX, dragBaseY;
    private double dragBase2X, dragBase2Y;

    @FXML
    public void pressed() {
        draw_polygon(queryInput.getText());

        vboxLayers.getChildren().add(new HBox());
    }


    public void draw_polygon(String poly) {
        try {

            //Create a WKT parser for reading WKT input
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            WKTReader reader = new WKTReader(geometryFactory);
            Geometry p = reader.read(poly);


            if (p instanceof  GeometryCollection)
            {
                for(int i = 0; i < p.getNumGeometries(); i++)
                {
                    refineGeometryClass(p.getGeometryN(i));
                }
            }
            else
            {
                refineGeometryClass(p);
            }

        } catch(com.vividsolutions.jts.io.ParseException e) {}



    }

    /**
     * Delegates the task of creating the layer for this geometry. Whether it is a plain WKT object,
     * or a composite such as a MultiPolygon
     * @param geometry  geometry to consider
     */
    private void refineGeometryClass(Geometry geometry)
    {
        if (geometry instanceof MultiPolygon || geometry instanceof  MultiLineString || geometry instanceof  MultiPoint)
        {
            createrLayersFromMultiples(geometry);
        }
        else {
            createLayer(geometry);
        }
    }

    /**
     * Assumes the given geomtry is of a multiple type, and creates a layer for each
     * @param geometry geometry to consider
     */
    private void createrLayersFromMultiples(Geometry geometry)
    {
        for(int i = 0; i < geometry.getNumGeometries(); i++)
        {
            createLayer(geometry.getGeometryN(i));
        }
    }

    /**
     * Creates a layer for the given geometry
     * @param geometry  geometry to draw to a layer
     */
    private void createLayer(Geometry geometry)
    {
        GisVisualization gv = GisVisualization.createVisualization(Main.stage.getWidth(),Main.stage.getHeight(), geometry, upperPane);
        Layer hb = new Layer(gv, vboxLayers, geometry.getGeometryType());
        Layer.layers.add(hb);
        hb.reorderLayers();
        upperPane.requestFocus();
    }

    private void zoom(double d)
    {
        upperPane.scaleXProperty().set(upperPane.scaleXProperty().get() * d);
        upperPane.scaleYProperty().set(upperPane.scaleYProperty().get() * d);
    }

    public void handleUpperPaneKeyPresses(KeyEvent event) {
        if (event.getText().equals("+"))
        {
            zoom(1.4);
        }
        else if (event.getText().equals("-"))
        {
            zoom(1/1.4);
        }
    }

    //TODO Concerns: dragging only works when clicking the canvas, areas of pane not filled with canvas does not react
    //TODO possible solutions: Make a really huge canvas and translate 0,0 to middle of screen. Or find another node and listener to move canvas


    /**
     * Called when the mouse press the upperPane.
     * This pane will receive focus and save the coordinates of the press to be used for dragging
     * @param event MouseEvent to react to
     */
    public void upperPaneMousePressed(MouseEvent event) {
        upperPane.requestFocus();

        dragBaseX = upperPane.translateXProperty().get();
        dragBaseY = upperPane.translateYProperty().get();
        dragBase2X = event.getSceneX();
        dragBase2Y = event.getSceneY();
    }

    /**
     * Called when upperPane is being dragged and drag it accordingly. Also sets cursor to Cursor.Move
     * @param event MouseEvent to react to
     */
    public void upperPaneMouseDragged(MouseEvent event) {
        Main.stage.getScene().setCursor(Cursor.MOVE);
        upperPane.setTranslateX(dragBaseX + (event.getSceneX()-dragBase2X));
        upperPane.setTranslateY(dragBaseY + (event.getSceneY()-dragBase2Y));
    }

    /**
     * Called when mouse releases on upperPane. Makes sure cursor goes back to normal
     */
    public void upperPaneMouseReleased() {
        Main.stage.getScene().setCursor(Cursor.DEFAULT);
    }

}
