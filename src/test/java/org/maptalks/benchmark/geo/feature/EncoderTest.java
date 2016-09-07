package org.maptalks.benchmark.geo.feature;

import org.junit.*;
import org.junit.rules.TestName;
import org.maptalks.geojson.Feature;
import org.maptalks.geojson.FeatureCollection;
import org.maptalks.geojson.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncoderTest {

    private static final int COUNT = 100000;
    private static List<Feature> features;
    private static FeatureCollection collection;

    @BeforeClass
    public static void setup() throws Exception {
        features = new ArrayList<Feature>();
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
        collection = new FeatureCollection(features.toArray(new Feature[features.size()]));
    }

    @Rule
    public TestName name = new TestName();

    private long start;

    @Before
    public void before() throws Exception {
        start = System.currentTimeMillis();
    }

    @After
    public void after() throws Exception {
        System.out.println("Test " + name.getMethodName() + " took " + (System.currentTimeMillis() - start) + " ms");
    }

    @Test
    public void encodeToSimple() throws Exception {
        byte[] bytes = Encoder.encodeToSimple(collection);
        // System.out.println("simple: " + bytes.length);
    }

    //@Test
    public void encodeToGeoBuf() throws Exception {
        byte[] bytes = Encoder.encodeToGeoBuf(collection);
        // System.out.println("geobuf: " + bytes.length);
    }

    @Test
    public void encodeToBson() throws Exception {
        byte[] bytes = Encoder.encodeToBson(collection);
        // System.out.println("bson: " + bytes.length);
    }

    @Test
    public void toArrayFull() throws Exception {
        Feature[] array = features.toArray(new Feature[features.size()]);
        Assert.assertEquals(COUNT, array.length);
    }

    @Test
    public void toArrayEmpty() throws Exception {
        Feature[] array = features.toArray(new Feature[0]);
        Assert.assertEquals(COUNT, array.length);
    }

}

