package utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MyUtills {
    public static double[] lngLat2Mercator(double lng, double lat) {
        double[] xy = new double[2];
        double x = lng * 20037508.342789 / 180;
        double y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
        y = y * 20037508.34789 / 180;
        xy[0] = x;
        xy[1] = y;
        return xy;
    }

    public static String readStream(InputStream in) throws Exception{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = -1;
        byte[] buffer = new byte[1024];//1kb
        while((len=in.read(buffer))!=-1) {
            baos.write(buffer, 0, len);
        }
        in.close();
        String content = new String(baos.toByteArray());
        return content;
    }
}
