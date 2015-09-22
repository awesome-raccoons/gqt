package models;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import javafx.scene.Group;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Circle;

import java.util.ArrayList;

/**
 * Created by thea on 22/09/15.
 */
public class MultiPolygonModel extends GeometryModel {
    public MultiPolygonModel(Geometry geometry, Group group) {
        super(geometry, group);
    }

    public final ArrayList<Circle> drawAndCreateToolTips(GraphicsContext graphicsContext) {
        ArrayList<Circle> tooltips = new ArrayList<>();
        MultiPolygon multiPolygon = (MultiPolygon) this.geometry;
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            PolygonModel polygonModel = new PolygonModel(multiPolygon.getGeometryN(i), this.group);
            tooltips.addAll(polygonModel.drawAndCreateToolTips(graphicsContext));
        }
        return tooltips;
    }
}
