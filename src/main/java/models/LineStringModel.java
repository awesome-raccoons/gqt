package models;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import javafx.scene.Group;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;

/**
 * Created by thea on 22/09/15.
 */
public class LineStringModel extends GeometryModel {
    public LineStringModel(Geometry geometry, Group group) {
        super(geometry, group);
    }

    public final ArrayList<Circle> drawAndCreateToolTips(GraphicsContext graphicsContext) {
        ArrayList<Circle> tooltips = new ArrayList<Circle>();
        Coordinate[] coordinates = this.geometry.getCoordinates();
        graphicsContext.moveTo(coordinates[0].x, coordinates[0].y);

        for (int i = 1; i < coordinates.length; i++) {
            graphicsContext.lineTo(coordinates[i].x, coordinates[i].y);
            Circle tooltip = createToolTip(coordinates[i].x, coordinates[i].y, Color.BLACK);
            tooltips.add(tooltip);
        }

        return tooltips;
    }
}
