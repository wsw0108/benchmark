package org.maptalks.benchmark.geo.feature;

import com.alibaba.fastjson.JSON;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ByteOrderValues;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBWriter;
import io.jeo.geobuf.GeobufWriter;
import io.jeo.vector.MapFeature;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonWriter;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.OutputBuffer;
import org.maptalks.benchmark.geo.GeoJSONReader;
import org.maptalks.geojson.Feature;
import org.maptalks.geojson.FeatureCollection;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class Encoder {
    public static byte[] encodeToSimple(FeatureCollection collection) throws Exception {
        Feature[] features = collection.getFeatures();
        WKBWriter wkbWriter = new WKBWriter();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        OutputStreamOutStream os = new OutputStreamOutStream(baos);

        byte[] buf = new byte[4];

        for (Feature feature : features) {
            // id, not handled

            // geometry
            Geometry geom = GeoJSONReader.read(feature.getGeometry());
            wkbWriter.write(geom, os);

            // properties
            byte[] propsBytes = JSON.toJSONBytes(feature.getProperties());
            int propsBytesLen = propsBytes.length;
            ByteOrderValues.putInt(propsBytesLen, buf, ByteOrderValues.BIG_ENDIAN);
            os.write(buf, buf.length);
            os.write(propsBytes, propsBytesLen);
        }

        return baos.toByteArray();
    }

    public static byte[] encodeToGeoBuf(FeatureCollection collection) throws Exception {
        Feature[] features = collection.getFeatures();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        GeobufWriter geobufWriter = new GeobufWriter(baos);

        for (Feature feature : features) {
            // id
            MapFeature feat = new MapFeature(feature.getId().toString());

            // geometry
            Geometry geom = GeoJSONReader.read(feature.getGeometry());
            // FIXIT: bug on put
            feat.put(null, geom);

            // properties
            Map<String, Object> props = feature.getProperties();
            for (Map.Entry<String, Object> entry : props.entrySet()) {
                feat.put(entry.getKey(), entry.getValue());
            }

            geobufWriter.write(feat);
            // geobufWriter.append(feat);
        }

        return baos.toByteArray();
    }

    public static byte[] encodeToBson(FeatureCollection collection) throws Exception {
        Feature[] features= collection.getFeatures();
        WKBWriter wkbWriter = new WKBWriter();

        OutputBuffer buffer = new BasicOutputBuffer(8192);
        BsonWriter writer = new BsonBinaryWriter(buffer);

        writer.writeStartDocument();
        writer.writeString("type", collection.getType());
        writer.writeStartArray("features");
        for (Feature feature : features) {
            writer.writeStartDocument();

            // id
            writer.writeString("id", feature.getId().toString());

            // geometry
            Geometry geom = GeoJSONReader.read(feature.getGeometry());
            byte[] bytes = wkbWriter.write(geom);
            writer.writeBinaryData("geometry", new BsonBinary(bytes));

            // properties
            byte[] propsBytes = JSON.toJSONBytes(feature.getProperties());
            writer.writeBinaryData("properties", new BsonBinary(propsBytes));

            writer.writeEndDocument();
        }
        writer.writeEndArray();
        writer.writeEndDocument();

        writer.flush();

        return buffer.toByteArray();
    }

}
