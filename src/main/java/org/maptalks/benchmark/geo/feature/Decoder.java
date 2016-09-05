package org.maptalks.benchmark.geo.feature;

import com.alibaba.fastjson.JSON;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ByteOrderValues;
import com.vividsolutions.jts.io.WKBReader;
import org.bson.BsonBinary;
import org.bson.BsonBinaryReader;
import org.bson.BsonType;
import org.maptalks.benchmark.geo.GeoJSONWriter;
import org.maptalks.geojson.Feature;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Decoder {
    public static List<Feature> decodeFromSimple(byte[] data) throws Exception {
        List<Feature> features = new ArrayList<Feature>();
        WKBReader wkbReader = new WKBReader();

        ByteArrayInputStream is = new ByteArrayInputStream(data);
        byte[] buf = new byte[4];

        is.read(buf);
        int len = ByteOrderValues.getInt(buf, ByteOrderValues.BIG_ENDIAN);

        for (int i = 0; i < len; i++) {
            Feature feature = new Feature();

            int bytesLen;
            byte[] bytes;

            is.read(buf);
            bytesLen = ByteOrderValues.getInt(buf, ByteOrderValues.BIG_ENDIAN);

            // id
            is.read(buf);
            bytesLen = ByteOrderValues.getInt(buf, ByteOrderValues.BIG_ENDIAN);
            bytes = new byte[bytesLen];
            is.read(bytes);
            feature.setId(new String(bytes, "UTF-8"));

            // geometry
            is.read(buf);
            bytesLen = ByteOrderValues.getInt(buf, ByteOrderValues.BIG_ENDIAN);
            bytes = new byte[bytesLen];
            is.read(bytes);
            Geometry geom = wkbReader.read(bytes);
            feature.setGeometry(GeoJSONWriter.write(geom));

            // properties
            is.read(buf);
            bytesLen = ByteOrderValues.getInt(buf, ByteOrderValues.BIG_ENDIAN);
            bytes = new byte[bytesLen];
            is.read(bytes);
            Map<String, Object> props = (Map<String, Object>) JSON.parse(bytes);
            feature.setProperties(props);

            features.add(feature);
        }

        return features;
    }

    public static List<Feature> decodeFromBson(byte[] data) throws Exception {
        List<Feature> features = new ArrayList<Feature>();
        WKBReader wkbReader = new WKBReader();

        ByteBuffer buffer = ByteBuffer.wrap(data);
        BsonBinaryReader reader = new BsonBinaryReader(buffer);

        reader.readStartDocument();

        String type = reader.readString("type");
        System.out.println(type);

        reader.readStartArray();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            Feature feature = new Feature();

            reader.readStartDocument();

            // id
            String id = reader.readString();
            feature.setId(id);

            // geometry
            BsonBinary geoBinary = reader.readBinaryData("geometry");
            Geometry geom = wkbReader.read(geoBinary.getData());
            feature.setGeometry(GeoJSONWriter.write(geom));

            // properties
            BsonBinary propsBinary = reader.readBinaryData("properties");
            Map<String, Object> props = (Map<String, Object>) JSON.parse(propsBinary.getData());
            feature.setProperties(props);

            reader.readEndDocument();

            features.add(feature);
        }
        reader.readEndArray();

        reader.readEndDocument();

        return features;
    }
}
