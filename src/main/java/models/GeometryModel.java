package models;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.CoordinateSequences;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.MultiPoint;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.util.ArrayList;

/**
 * Created by thea on 22/09/15.
 */
public abstract class GeometryModel {

    private final Geometry geometry;
    private final Geometry originalGeometry;
    private final AnchorPane group;

    private static final double TOOLTIP_SIZE = 2.5;

    public GeometryModel(final Geometry geometry, final AnchorPane group) {
        this.geometry = geometry;
        this.group = group;
        this.originalGeometry = (Geometry) geometry.clone();
    }

    public final Geometry getGeometry() {
        return this.geometry;
    }

    public final AnchorPane getGroup() {
        return this.group;
    }

    /**
     * Reverses hole orientation if needed. All geometries with holes go through here.
     * @param geometry
     * @return
     */
    public static final Geometry holeFunction(final Geometry geometry) {

        //Number of interior rings
        int interiorRings = ((Polygon) geometry).getNumInteriorRing();

        //Outer ring
        LineString boundary = ((Polygon) geometry).getExteriorRing();
        //LineStrings and CoordinateSequence arrays initializing, one entry per interior ring
        CoordinateSequence[] holeSeqs = new CoordinateSequence[interiorRings];
        CGAlgorithms clock = new CGAlgorithms();
        CoordinateSequence outer = boundary.getCoordinateSequence();

        //Checking if the outer ring is clockwise or not
        boolean isCW = !clock.isCCW(outer.toCoordinateArray());


        //If outer ring is clockwise, inner ring should be counterclockwise. If it isn't, change it
        //If outer ring is CCW, make inner ring CW.
        //Puts each ring into the LineString and CoordinateSequence arrays.
        for (int x = 0; x < interiorRings; x++) {
            LineString hole = ((Polygon) geometry).getInteriorRingN(x);

            holeSeqs[x] = hole.getCoordinateSequence();


            if (isCW) {
                if (clock.isCCW(holeSeqs[x].toCoordinateArray())) {
                    continue;
                } else {
                    CoordinateSequences.reverse(holeSeqs[x]);
                }
            } else {
                if (!clock.isCCW(holeSeqs[x].toCoordinateArray())) {
                    continue;
                } else {
                    CoordinateSequences.reverse(holeSeqs[x]);
                }
            }

        }
        CoordinateSequence shell = boundary.getCoordinateSequence();

        LinearRing[] holes = new LinearRing[interiorRings];
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

        //Makes one new LinearRing for each interior ring
        for (int x = 0; x < interiorRings; x++) {
            LinearRing linear = new GeometryFactory().createLinearRing(holeSeqs[x]);
            holes[x] = linear;

        }
        //One LinearRing for the outer ring
        LinearRing shellR = new GeometryFactory().createLinearRing(shell);

        //Making the new polygon
        return new Polygon(shellR, holes, geometryFactory);
    }

    public final Geometry getOriginalGeometry() {
        return this.originalGeometry;
    }

    public static GeometryModel getModel(final Geometry geometry, final AnchorPane group) {
        if (geometry instanceof Polygon) {
            boolean hasHoles = ((Polygon) geometry).getNumInteriorRing() > 0;
            if (hasHoles) {
                Geometry g = holeFunction(geometry);
                return new PolygonModel(g, group);
            }
            return new PolygonModel(geometry, group);
        } else if (geometry instanceof Point) {
            return new PointModel(geometry, group);
        } else if (geometry instanceof LineString) {
            return new LineStringModel(geometry, group);
        } else if (geometry instanceof MultiPoint) {
            return new MultiPointModel(geometry, group);
        } else if (geometry instanceof MultiPolygon) {
            return new MultiPolygonModel(geometry, group);
        } else if (geometry instanceof MultiLineString) {
            return new MultiLineStringModel(geometry, group);
        }
        return null;
    }

    public final Circle createToolTip(final double x, final double y, final Paint color) {
        Circle circle = new Circle(x, y, TOOLTIP_SIZE, color);
        Tooltip tooltip = new Tooltip(x + ", " + y);
        Tooltip.install(circle, tooltip);
        return circle;
    }


    /**
     * Draws a geometry-object with to the screen.
     * @param graphicsContext The graphics to which the object should be drawn.
     * @return A list of tooltips.
     */
    public abstract ArrayList<Circle> drawAndCreateToolTips(final GraphicsContext graphicsContext);
}

