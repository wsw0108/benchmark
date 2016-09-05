package org.maptalks.benchmark.geo.feature;

import org.junit.Before;
import org.junit.Test;
import org.maptalks.geojson.Feature;
import org.maptalks.geojson.FeatureCollection;
import org.maptalks.geojson.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecoderTest {

    private static final int COUNT = 100000;
    private byte[] simpleData;
    private byte[] bsonData;

    @Before
    public void setUp() throws Exception {
        List<Feature> features = new ArrayList<Feature>();
        for (int i = 0; i < COUNT; i++) {
            int id = i + 1;
            String fid = String.valueOf(id);
            Map<String, Object> props = new HashMap<String, Object>();
            props.put("str", fid);
            props.put("int", id);
            double x = Math.random() * 180;
            double y = Math.random() * 180;
            Point point = new Point(new double[]{x, y});
            Feature feature = new Feature(fid, point, props);
            features.add(feature);
        }
        FeatureCollection collection = new FeatureCollection(features.toArray(new Feature[features.size()]));
        simpleData = Encoder.encodeToSimple(collection);
        bsonData = Encoder.encodeToBson(collection);
    }

    @Test
    public void decodeFromSimple() throws Exception {
        List<Feature> features = Decoder.decodeFromSimple(simpleData);
        System.out.println(features.size());
    }

    @Test
    public void decodeFromBson() throws Exception {
        List<Feature> features = Decoder.decodeFromBson(bsonData);
        System.out.println(features.size());
    }
}
