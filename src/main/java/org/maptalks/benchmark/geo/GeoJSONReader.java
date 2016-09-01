package org.maptalks.benchmark.geo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.maptalks.geojson.*;
import org.maptalks.geojson.json.GeoJSONFactory;

public class GeoJSONReader {
    private final static GeometryFactory geometryFactory = new GeometryFactory(
        new PrecisionModel(PrecisionModel.FLOATING));

    public static com.vividsolutions.jts.geom.Geometry read(String json) {
        GeoJSON geoJSON = GeoJSONFactory.create(json);
        return read(geoJSON);
    }

    public static com.vividsolutions.jts.geom.Geometry read(GeoJSON geoJSON) {
        if (geoJSON instanceof Point) {
            return convert((Point) geoJSON);
        } else if (geoJSON instanceof LineString) {
            return convert((LineString) geoJSON);
        } else if (geoJSON instanceof Polygon) {
            return convert((Polygon) geoJSON);
        } else if (geoJSON instanceof MultiPoint) {
            return convert((MultiPoint) geoJSON);
        } else if (geoJSON instanceof MultiLineString) {
            return convert((MultiLineString) geoJSON);
        } else if (geoJSON instanceof MultiPolygon) {
            return convert((MultiPolygon) geoJSON);
        } else if (geoJSON instanceof GeometryCollection) {
            return convert((GeometryCollection) geoJSON);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static com.vividsolutions.jts.geom.Geometry convert(GeometryCollection geoJSON) {
        Geometry[] geometries = geoJSON.getGeometries();
        com.vividsolutions.jts.geom.Geometry[] collection = new com.vividsolutions.jts.geom.Geometry[geometries.length];
        for (int i = 0; i < geometries.length; i++) {
            collection[i] = read(geometries[i]);
        }
        return geometryFactory.createGeometryCollection(collection);
    }

    private static com.vividsolutions.jts.geom.Geometry convert(Point point) {
        return geometryFactory.createPoint(convert(point.getCoordinates()));
    }

    private static com.vividsolutions.jts.geom.Geometry convert(MultiPoint multiPoint) {
        return geometryFactory.createMultiPoint(convert(multiPoint.getCoordinates()));
    }

    private static com.vividsolutions.jts.geom.Geometry convert(LineString lineString) {
        return geometryFactory.createLineString(convert(lineString.getCoordinates()));
    }

    private static com.vividsolutions.jts.geom.Geometry convert(MultiLineString multiLineString) {
        int size = multiLineString.getCoordinates().length;
        com.vividsolutions.jts.geom.LineString[] lineStrings = new com.vividsolutions.jts.geom.LineString[size];
        for (int i = 0; i < size; i++) {
            lineStrings[i] = geometryFactory.createLineString(convert(multiLineString.getCoordinates()[i]));
        }
        return geometryFactory.createMultiLineString(lineStrings);
    }

    private static com.vividsolutions.jts.geom.Geometry convert(Polygon polygon) {
        return convertToPolygon(polygon.getCoordinates());
    }

    private static com.vividsolutions.jts.geom.Polygon convertToPolygon(double[][][] coordinates) {
        LinearRing shell = geometryFactory.createLinearRing(convert(coordinates[0]));

        if (coordinates.length > 1) {
            int size = coordinates.length - 1;
            LinearRing[] holes = new LinearRing[size];
            for (int i = 0; i < size; i++) {
                holes[i] = geometryFactory.createLinearRing(convert(coordinates[i + 1]));
            }
            return geometryFactory.createPolygon(shell, holes);
        } else {
            return geometryFactory.createPolygon(shell);
        }
    }

    private static com.vividsolutions.jts.geom.Geometry convert(MultiPolygon multiPolygon) {
        int size = multiPolygon.getCoordinates().length;
        com.vividsolutions.jts.geom.Polygon[] polygons = new com.vividsolutions.jts.geom.Polygon[size];
        for (int i = 0; i < size; i++) {
            polygons[i] = convertToPolygon(multiPolygon.getCoordinates()[i]);
        }
        return geometryFactory.createMultiPolygon(polygons);
    }

    private static Coordinate convert(double[] c) {
        return new Coordinate(c[0], c[1]);
    }

    private static Coordinate[] convert(double[][] ca) {
        Coordinate[] coordinates = new Coordinate[ca.length];
        for (int i = 0; i < ca.length; i++) {
            coordinates[i] = convert(ca[i]);
        }
        return coordinates;
    }

}
