package org.maptalks.benchmark.geo.feature;

import com.alibaba.fastjson.JSON;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ByteOrderValues;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBWriter;
import org.maptalks.benchmark.geo.GeoJSONReader;
import org.maptalks.benchmark.geo.Geobuf;
import org.maptalks.geojson.Feature;
import org.maptalks.geojson.FeatureCollection;

import java.io.ByteArrayOutputStream;

public class Encoder {
    public static byte[] encodeToSimple(FeatureCollection collection) throws Exception {
        Feature[] features = collection.getFeatures();
        WKBWriter wkbWriter = new WKBWriter();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        OutputStreamOutStream os = new OutputStreamOutStream(baos);

        byte[] buf = new byte[4];

        for (Feature feature : features) {
            Geometry geom = GeoJSONReader.read(feature.getGeometry());
            wkbWriter.write(geom, os);

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

        Geobuf.Data.FeatureCollection.Builder featureCollectionBuilder = Geobuf.Data.FeatureCollection.newBuilder();
        for (Feature feature : features) {
            Geobuf.Data.Feature.Builder featureBuilder = Geobuf.Data.Feature.newBuilder();

            Object fid = feature.getId();
            if (fid != null) {
                featureBuilder.setId(fid.toString());
            }

            // TODO: set properties

            Geobuf.Data.Geometry.Builder geometryBuilder = Geobuf.Data.Geometry.newBuilder();
            geometryBuilder.setType(Geobuf.Data.Geometry.Type.valueOf(feature.getGeometry().getType()));
            // TODO: set coords

            featureBuilder.setGeometry(geometryBuilder);

            featureCollectionBuilder.addFeatures(featureBuilder);
        }

        return featureCollectionBuilder.build().toByteArray();
    }
}
