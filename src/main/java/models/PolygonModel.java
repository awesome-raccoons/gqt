package models;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;

/**
 * Created by thea on 22/09/15.
 */
public class PolygonModel extends GeometryModel {

    public PolygonModel(final Geometry geometry, final AnchorPane group){
        super(geometry, group);
    }

    public final ArrayList<Circle> drawAndCreateToolTips(final GraphicsContext graphicsContext){
        ArrayList<Circle> tooltips = new ArrayList<>();

        Coordinate[] coordinates = this.geometry.getCoordinates();
        double[] xCoordinates = new double[coordinates.length];
        double[] yCoordinates = new double[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            xCoordinates[i] = coordinates[i].x;
            yCoordinates[i] = coordinates[i].y;
            Circle tooltip = createToolTip(coordinates[i].x, coordinates[i].y, Color.BLACK);
            tooltips.add(tooltip);
        }

        graphicsContext.fillPolygon(xCoordinates, yCoordinates, coordinates.length);

        return tooltips;
    }

}