package models;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;

import java.util.ArrayList;

/**
 * Created by thea on 22/09/15.
 */
public class MultiPolygonModel extends GeometryModel {
    public MultiPolygonModel(final Geometry geometry, final AnchorPane group) {
        super(geometry, group);
    }

    public final ArrayList<Circle> drawAndCreateToolTips(final GraphicsContext graphicsContext) {
        ArrayList<Circle> tooltips = new ArrayList<>();
        MultiPolygon multiPolygon = (MultiPolygon) this.getGeometry();
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            PolygonModel polygonModel =
                    new PolygonModel(multiPolygon.getGeometryN(i), this.getGroup());
            tooltips.addAll(polygonModel.drawAndCreateToolTips(graphicsContext));
        }
        return tooltips;
    }
}
