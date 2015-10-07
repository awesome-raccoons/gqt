package models;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Created by David on 2.10.2015.
 */
public class ModelBoundaries {
    /**
     *  the minimum x-coordinate.
     */
    private double minX;

    /**
     *  the maximum x-coordinate.
     */
    private double maxX;

    /**
     *  the minimum y-coordinate.
     */
    private double minY;

    /**
     *  the maximum y-coordinate.
     */
    private double maxY;

    private boolean defined = false;

    public ModelBoundaries() {
        clear();
        defined = false;
    }

    public final void clear() {
        minX = 0;
        minY = 0;
        maxX = 0;
        maxY = 0;
    }

    public final double getMinX() {
        return minX;
    }

    public final double getMinY() {
        return minY;
    }

    public final double getMaxX() {
        return maxX;
    }

    public final double getMaxY() {
        return maxY;
    }

    public final double getMiddleX() {
        return ((minX + maxX) / 2);
    }

    public final double getMiddleY() {
        return ((minY + maxY) / 2);
    }



    public final double getWidth() {
        return (maxX - minX);
    }

    public final double getHeight() {
        return (maxY - minY);
    }

    public final void include(final double x, final double y) {
        if ((this.isNull()) && (!defined)) {
            this.maxX = x;
            this.minX = x;
            this.maxY = y;
            this.minY = y;
            this.defined = true;
        } else {
            if (x < this.minX) {
                this.minX = x;
            }

            if (x > this.maxX) {
                this.maxX = x;
            }

            if (y < this.minY) {
                this.minY = y;
            }

            if (y > this.maxY) {
                this.maxY = y;
            }
        }
    }

    public final void include(final Coordinate coord) {
        include(coord.x, coord.y);
    }

    public final void include(final Coordinate[] coordinates) {
        for (int i = 0; i < coordinates.length; i++) {
            include(coordinates[i]);
        }
    }

    public final void includeGeometry(final Geometry geometry) {
        include(geometry.getCoordinates());
    }

    public final void update(final Geometry geometry) {
        clear();
        includeGeometry(geometry);
    }

    private boolean isNull() {
        return minX == 0.0D && minY == 0.0D && this.getWidth() <= 0.0D && this.getHeight() <= 0.0D;
    }
}
