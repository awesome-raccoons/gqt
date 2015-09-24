import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Johannes on 10.09.2015.
 */

public class GisVisualization {

    private static final double POINT_WIDTH = 5;    //Defines the width of Points
    private static final double POINT_HEIGHT = 5;   //Defines the height of Points

    private int idCounter = 0;       //Static counter for IDs
    private Pane group;         //Root node all canvases will be drawn to

    private int id;
    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private Geometry geometry;
    private ArrayList<Circle> tooltips;

    public GisVisualization(final double canvasWidth,
                            final double canvasHeight,
                            final Geometry geometry,
                            final Pane group) {
        this.id = idCounter++;
        this.group = group;
        this.canvas = new Canvas(canvasWidth, canvasHeight);
        this.graphicsContext = canvas.getGraphicsContext2D();
      //  graphicsContext.setFill(Color.WHITE);
      //  graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);
        this.geometry = geometry;
        this.tooltips = new ArrayList<>();
    }

    public final Pane getGroup() {
        return this.group;
    }

    /**
     * Creates a polygon from the given points and draw it on the canvas.
     * Also creates tooltips for each point in the polygon.
     *
     * @param canvasWidth  Width of the canvas the GIS-visualization will drawn to.
     * @param canvasHeight Height of the canvas the GIS-visualization will drawn to.
     * @param geometry      The geometry object to visualize.
     * @param group         The group the polygon will be drawn at.
     * @return a GisVisualization object.
     */
    public static GisVisualization createVisualization(final double canvasWidth,
                                                       final double canvasHeight,
                                                       final Geometry geometry,
                                                       final Pane group) {
        GisVisualization gisVis = new GisVisualization(canvasWidth, canvasHeight, geometry, group);
        gisVis.create2DShape(getRandomColor(1.0f));
        group.getChildren().add(gisVis.canvas);

        return gisVis;
    }


    /**
     * Redraws this GisVisualization object and tooltips to its given group.
     */
    public final void redraw(final boolean displayTooltips) {
        group.getChildren().add(this.canvas);
        if (displayTooltips) {
            for (Circle c : tooltips) {
                group.getChildren().add(c);
            }
        }
    }

    /**
     * Creates a polygon using this GisVisualization object's graphicsContext.
     * @param color The color of the polygon
     */
    private void create2DShape(final Color color) {

        this.graphicsContext.setFill(color);
        this.graphicsContext.setStroke(color);

        //This GisVisualization object can contain any geometry
        //There check its instance and draw accordingly
        if (this.geometry instanceof Polygon) {
            drawPolygon((Polygon) this.geometry);
        } else if (this.geometry instanceof Point) {
            drawPoint((Point) this.geometry);
        } else if (this.geometry instanceof LineString) {
            drawLineString((LineString) this.geometry);
        } else if (this.geometry instanceof MultiPoint) {
            Coordinate[] coords = this.geometry.getCoordinates();
            for (Coordinate c : coords) {
                drawPoint(c.x, c.y);
            }
        } else if (this.geometry instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon) this.geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                drawPolygon((Polygon) mp.getGeometryN(i));
            }
        } else if (this.geometry instanceof MultiLineString) {
            MultiLineString mls = (MultiLineString) this.geometry;
            for (int i = 0; i < mls.getNumGeometries(); i++) {
                drawLineString((LineString) mls.getGeometryN(i));
            }
        }
    }


    /**
     * Draws a WKT polygon to screen.
     *
     * @param polygon The Polygon object to be drawn
     */
    private void drawPolygon(final Polygon polygon) {
        //TODO
        // Why does this draw the intended drawing (with hole)
        //      MULTIPOLYGON(((28 26,28 0,84 0,84 42,28 26),
        //                    (52 18,66 23,73 9,48 6,52 18)),
        //                   ((59 18,67 18,67 13,59 13,59 18)))
        // while this doesn't:
        //      POLYGON((0 0,50 0,50 50,0 50,0 0),
        //              (10 10,20 10,20 20,10 20,10 10)) ???

        Coordinate[] coords = polygon.getCoordinates();
        double[] xCoords = new double[coords.length];
        double[] yCoords = new double[coords.length];
        for (int i = 0; i < coords.length; i++) {
            xCoords[i] = coords[i].x;
            yCoords[i] = coords[i].y;
            createTooltip(this.group, xCoords[i], yCoords[i], Color.BLACK);
        }
        this.graphicsContext.fillPolygon(xCoords, yCoords, coords.length);
    }

    private static final double ADJUSTMENT_FACTOR = 0.5;

    /**
     * Draws a WKT Point to screen.
     *
     * @param point The Points to be drawn.
     */
    private void drawPoint(final Point point) {
        this.graphicsContext.fillOval(
                point.getX() - POINT_WIDTH * ADJUSTMENT_FACTOR,
                point.getY() - POINT_HEIGHT * ADJUSTMENT_FACTOR,
                POINT_WIDTH, POINT_HEIGHT);

    }

    /**
     * Draws a point to the screen.
     *
     * @param xCoordinate x coordinate of the point.
     * @param yCoordinate y coordinate of the point.
     */

    private void drawPoint(final double xCoordinate, final double yCoordinate) {
        this.graphicsContext.fillOval(
                xCoordinate - POINT_WIDTH * ADJUSTMENT_FACTOR,
                yCoordinate - POINT_HEIGHT * ADJUSTMENT_FACTOR,
                POINT_WIDTH,
                POINT_HEIGHT);
    }

    /**
     * Draws a WKT LineString to screen.
     *
     * @param lineString the LineString to be drawn.
     */
    private void drawLineString(final LineString lineString) {
        Coordinate[] coords = lineString.getCoordinates();
        this.graphicsContext.moveTo(coords[0].x, coords[0].y);
        for (int i = 1; i < coords.length; i++) {
            this.graphicsContext.lineTo(coords[i].x, coords[i].y);
        }
        this.graphicsContext.stroke();
    }

    private static final double TOOLTIP_ADJUSTOR = 2.5;

    /**
     * Creates tooltips for each of the points involved in this GisVisualization.
     *
     * @param group The group the tooltips will be added to.
     */
    private void createTooltip(final Pane group,
                               final double xCoord,
                               final double yCoord,
                               final Paint color) {

        Circle c = new Circle(xCoord, yCoord, TOOLTIP_ADJUSTOR, color);
        Tooltip t = new Tooltip(xCoord + " , " + yCoord);
        Tooltip.install(c, t);
        tooltips.add(c);
        group.getChildren().add(c);
    }

    /**
     * Toggles the visibility of this GisVisualization object.
     */
    public final void toggleVisibility() {
        this.canvas.setVisible(!canvas.isVisible());
        for (Circle c : this.tooltips) {
            c.setVisible(canvas.isVisible());
        }
    }

    private static final int RGB_PARAM = 255;

    /**
     * Returns a random rgb color with provided opacity.
     *
     * @param opacity transparent component, in range 0.0f-1.0f.
     * @return the color.
     */
    private static Color getRandomColor(final float opacity) {
        Random r = new Random();
        return Color.rgb(r.nextInt(RGB_PARAM),
                r.nextInt(RGB_PARAM),
                r.nextInt(RGB_PARAM),
                opacity);
    }


    /**
     * Get the ID for this GisVisualization object.
     *
     * @return The ID.
     */
    public final int getID() {
        return this.id;
    }

}
