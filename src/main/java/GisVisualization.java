

import com.vividsolutions.jts.geom.*;
import javafx.event.EventHandler;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Johannes on 10.09.2015.
 */
public class GisVisualization {

    private double pointWidth = 5;          //Defines the width of Points
    private double pointHeight = 5;         //Defines the height of Points

    private int id;                         //A unique ID assigned to each GisVisualization object
    private static int id_counter = 0;      //Static counter for IDs
    private Canvas canvas;                  //The canvas associated with this GisVisualization object
    private GraphicsContext graphicsContext;//GraphicsContext associated with this GisVisualization object
    public static AnchorPane group;         //Root node all canvases will be drawn to
    private Geometry geometry;              //Geometry this GisVisualization object represents
    private ArrayList<Circle> tooltips;  //A rectangle of points representing tooltips that highlights

    public GisVisualization(double canvas_width, double canvas_height, Geometry geometry, AnchorPane group) {
        this.id = id_counter++;
        GisVisualization.group = group;
        this.canvas = new Canvas(canvas_width, canvas_height);
        this.graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setFill(Color.rgb(0,100,0,0.1f));
        graphicsContext.fillRect(0, 0, canvas_width, canvas_height);
        this.geometry = geometry;
        this.tooltips = new ArrayList<>();
    }


    /**
     * Creates a polygon from the given points and draw it on the canvas.
     * Also creates tooltips for each point in the polygon
     *
     * @param canvas_width  Width of the canvas the GIS-visualization will drawn to
     * @param canvas_height Height of the canvas the GIS-visualization will drawn to
     * @param geometry      The geometry object to visualize
     * @param group         The group the polygon will be drawn at
     * @return a GisVisualization object
     */
    public static GisVisualization createVisualization(double canvas_width, double canvas_height, Geometry geometry, AnchorPane group) {
        GisVisualization gisVis = new GisVisualization(canvas_width, canvas_height, geometry, group);
        gisVis.create2DShape(getRandomColor(1.0f));
        group.getChildren().add(gisVis.canvas);

        return gisVis;
    }


    /**
     * Redraws this GisVisualization object and tooltips to its given group
     */
    public void redraw(boolean displayTooltips) {
        group.getChildren().add(this.canvas);
        if (displayTooltips) {
            for (Circle c : tooltips) {
                group.getChildren().add(c);
            }
        }
    }

    /**
     * Creates a polygon using this GisVisualization object's graphicsContext
     *
     * @param color The color of the polygon
     */
    private void create2DShape(Color color) {

        this.graphicsContext.setFill(color);
        this.graphicsContext.setStroke(color);

        //This GisVisualization object can contain any geometry
        //There check its instance and draw accordingly
        if (this.geometry instanceof Polygon)
        {
            drawPolygon((Polygon) this.geometry);
        }
        else if (this.geometry instanceof Point)
        {
            drawPoint((Point)this.geometry);
        }
        else if (this.geometry instanceof  LineString)
        {
           drawLineString((LineString)this.geometry);
        }
        else if (this.geometry instanceof MultiPoint)
        {
            Coordinate[] coords = this.geometry.getCoordinates();
            for(Coordinate c : coords)
            {
                drawPoint(c.x, c.y);
            }
        }
        else if (this.geometry instanceof  MultiPolygon)
        {
            MultiPolygon mp = (MultiPolygon)this.geometry;
            for(int i = 0; i < mp.getNumGeometries(); i++)
            {
                drawPolygon((Polygon)mp.getGeometryN(i));
            }
        }
        else if (this.geometry instanceof MultiLineString)
        {
            MultiLineString mls = (MultiLineString)this.geometry;
            for(int i = 0; i < mls.getNumGeometries(); i++)
            {
                drawLineString((LineString) mls.getGeometryN(i));
            }
        }
    }


    /**
     * Draws a WKT polygon to screen
     * @param polygon   The Polygon object to be drawn
     */
    private void drawPolygon(Polygon polygon)
    {
        //TODO What's the deal with: POLYGON((0 0,50 0,50 50,0 50,0 0),(10 10,20 10,20 20,10 20,10 10))
        //TODO Found the deal: the second parenthesis creates a "hole" in the first polygon, I think
        //TODO How to deal with this. and why does this draw the intended drawing MULTIPOLYGON(((28 26,28 0,84 0,84 42,28 26),(52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))
        //TODO while this doesn't: POLYGON((0 0,50 0,50 50,0 50,0 0),(10 10,20 10,20 20,10 20,10 10)) ???
        Coordinate[] coords = polygon.getCoordinates();
        double[] x_coords = new double[coords.length];
        double[] y_coords = new double[coords.length];
        for(int i = 0; i < coords.length; i++)
        {
            x_coords[i] = coords[i].x;
            y_coords[i] = coords[i].y;
            createTooltip(GisVisualization.group, x_coords[i], y_coords[i], Color.BLACK);
        }
        this.graphicsContext.fillPolygon(x_coords, y_coords, coords.length);
    }

    /**
     * Draws a WKT Point to screen
     * @param point The Points to be drawn
     */
    private void drawPoint(Point point)
    {
        this.graphicsContext.fillOval(point.getX() + pointWidth * 0.5f, point.getY() + pointHeight * 0.5f, pointWidth, pointHeight);
    }

    /**
     * Draws a point to the screen
     * @param x_coordinate  x coordinate of the point
     * @param y_coordinate y coordinate of the point
     */
    private void drawPoint(double x_coordinate, double y_coordinate)
    {
        this.graphicsContext.fillOval(x_coordinate + pointWidth * 0.5, y_coordinate + pointHeight * 0.5, pointWidth, pointHeight);
    }

    /**
     * Draws a WKT LineString to screen
     * @param lineString    the LineString to be drawn
     */
    private void drawLineString(LineString lineString)
    {
        Coordinate[] coords = lineString.getCoordinates();
        this.graphicsContext.moveTo(coords[0].x, coords[0].y);
        for (int i=1;i<coords.length;i++) {
            this.graphicsContext.lineTo(coords[i].x, coords[i].y);
        }
        this.graphicsContext.stroke();
    }




    /**
     * Creates tooltips for each of the points involved in this GisVisualization
     *
     * @param group The group the tooltips will be added to
     */
    private void createTooltip(AnchorPane group, double x_coord, double y_coord, Paint color) {

        Rectangle r = new Rectangle(x_coord - 2.5, y_coord - 2.5, 5.0, 5.0);
        Circle c = new Circle(x_coord, y_coord,2.5, color);
        Tooltip t = new Tooltip(x_coord + " , " + y_coord);
        Tooltip.install(c, t);
        tooltips.add(c);
        group.getChildren().add(c);
    }

    /**
     * Toggles the visibility of this GisVisualization object
     */
    public void toggleVisibility() {
        this.canvas.setVisible(!canvas.isVisible());
        for (Circle c : this.tooltips) {
            c.setVisible(canvas.isVisible());
        }
    }

    /**
     * Returns a random rgb color with provided opacity
     * @param opacity   transparent component, in range 0.0f-1.0f
     * @return the color
     */
    private static Color getRandomColor(float opacity)
    {
        Random r = new Random();
        return Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255), opacity);
    }


    /**
     * Get the ID for this GisVisualization object
     *
     * @return The ID
     */
    public int getID() {
        return this.id;
    }

}
