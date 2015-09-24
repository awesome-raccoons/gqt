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
public class LineStringModel extends GeometryModel {
    public LineStringModel(final Geometry geometry, final AnchorPane group) {
        super(geometry, group);
    }

    public final ArrayList<Circle> drawAndCreateToolTips(final GraphicsContext graphicsContext) {
        ArrayList<Circle> tooltips = new ArrayList<>();
        Coordinate[] coordinates = this.getGeometry().getCoordinates();
        graphicsContext.moveTo(coordinates[0].x, coordinates[0].y);
        graphicsContext.setLineWidth(2.0);

        graphicsContext.setFill(Color.BLACK);

        for (int i = 1; i < coordinates.length; i++) {
            graphicsContext.strokeLine(coordinates[i-1].x, coordinates[i-1].y, coordinates[i].x, coordinates[i].y);
            Circle tooltip = createToolTip(coordinates[i].x, coordinates[i].y, Color.BLACK);
            tooltips.add(tooltip);
        }

        return tooltips;
    }
}
