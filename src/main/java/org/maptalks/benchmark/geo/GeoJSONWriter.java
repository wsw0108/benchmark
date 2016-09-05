package org.maptalks.benchmark.geo;

import com.vividsolutions.jts.geom.*;
import org.maptalks.geojson.Feature;
import org.maptalks.geojson.Geometry;
import org.maptalks.geojson.GeometryCollection;

import java.util.List;

public class GeoJSONWriter {

    public static Geometry write(com.vividsolutions.jts.geom.Geometry geometry) {
        Class<? extends com.vividsolutions.jts.geom.Geometry> c = geometry.getClass();
        if (c.equals(Point.class)) {
            return convert((Point) geometry);
        } else if (c.equals(LineString.class)) {
            return convert((LineString) geometry);
        } else if (c.equals(Polygon.class)) {
            return convert((Polygon) geometry);
        } else if (c.equals(MultiPoint.class)) {
            return convert((MultiPoint) geometry);
        } else if (c.equals(MultiLineString.class)) {
            return convert((MultiLineString) geometry);
        } else if (c.equals(MultiPolygon.class)) {
            return convert((MultiPolygon) geometry);
        } else if (c.equals(com.vividsolutions.jts.geom.GeometryCollection.class)) {
            return convert((com.vividsolutions.jts.geom.GeometryCollection) geometry);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static Geometry convert(com.vividsolutions.jts.geom.GeometryCollection geometry) {
        int size = geometry.getNumGeometries();
        Geometry[] geometries = new Geometry[size];
        for (int i = 0; i < size; i++) {
            geometries[i] = write(geometry.getGeometryN(i));
        }
        return new GeometryCollection(geometries);
    }

    public static org.maptalks.geojson.FeatureCollection write(List<Feature> features) {
        int size = features.size();
        Feature[] featuresJson = new Feature[size];
        for (int i = 0; i < size; i++) {
            featuresJson[i] = features.get(i);
        }
        return new org.maptalks.geojson.FeatureCollection(featuresJson);
    }

    private static org.maptalks.geojson.Point convert(Point point) {
        return new org.maptalks.geojson.Point(
            convert(point.getCoordinate()));
    }

    private static org.maptalks.geojson.MultiPoint convert(MultiPoint multiPoint) {
        return new org.maptalks.geojson.MultiPoint(
            convert(multiPoint.getCoordinates()));
    }

    private static org.maptalks.geojson.LineString convert(LineString lineString) {
        return new org.maptalks.geojson.LineString(
            convert(lineString.getCoordinates()));
    }

    private static org.maptalks.geojson.MultiLineString convert(MultiLineString multiLineString) {
        int size = multiLineString.getNumGeometries();
        double[][][] lineStrings = new double[size][][];
        for (int i = 0; i < size; i++) {
            lineStrings[i] = convert(multiLineString.getGeometryN(i).getCoordinates());
        }
        return new org.maptalks.geojson.MultiLineString(lineStrings);
    }

    private static org.maptalks.geojson.Polygon convert(Polygon polygon) {
        int size = polygon.getNumInteriorRing() + 1;
        double[][][] rings = new double[size][][];
        rings[0] = convert(polygon.getExteriorRing().getCoordinates());
        for (int i = 0; i < size - 1; i++) {
            rings[i + 1] = convert(polygon.getInteriorRingN(i).getCoordinates());
        }
        return new org.maptalks.geojson.Polygon(rings);
    }

    private static org.maptalks.geojson.MultiPolygon convert(MultiPolygon multiPolygon) {
        int size = multiPolygon.getNumGeometries();
        double[][][][] polygons = new double[size][][][];
        for (int i = 0; i < size; i++) {
            polygons[i] = convert((Polygon) multiPolygon.getGeometryN(i)).getCoordinates();
        }
        return new org.maptalks.geojson.MultiPolygon(polygons);
    }

    private static double[] convert(Coordinate coordinate) {
        return new double[]{coordinate.x, coordinate.y};
    }

    private static double[][] convert(Coordinate[] coordinates) {
        double[][] array = new double[coordinates.length][];
        for (int i = 0; i < coordinates.length; i++) {
            array[i] = convert(coordinates[i]);
        }
        return array;
    }
}
