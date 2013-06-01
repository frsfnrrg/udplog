package udplog;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.imageio.ImageIO;

final class Util {
    public static String timestamp() {
        // alas, Windoze does not allow /\|"": etc.
        // The file name may not include it
        SimpleDateFormat sdfDate = new SimpleDateFormat(
                "yyyy.MM.dd-HH.mm.ss.SSS");
        return sdfDate.format(new Date());
    }

    private final static Random r = new Random();

    public final static boolean randomBoolean() {
        return r.nextBoolean();
    }

    public synchronized static int randomInteger() {
        return r.nextInt();
    }

    public synchronized static double randomDouble() {
        return r.nextDouble();
    }

    public static BufferedImage loadImage(String path, Dimension defaultsize) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.format("Reading image \"%s\" failed.\n", path);
            return new BufferedImage(defaultsize.width, defaultsize.height,
                    BufferedImage.TYPE_3BYTE_BGR);
        }
    }

    public static Object print(Object o) {
        System.out.println(o);
        return o;
    }

    public static Object[] print(Object... l) {
        StringBuilder b = new StringBuilder();
        for (Object o : l) {
            b.append(o);
            b.append(' ');
        }

        System.out.println(b.substring(0));
        return l;
    }

    private static double l = 0;

    public synchronized static double nextSine(double k) {
        l += 0.01;
        return Math.sin(k * l);
    }

    public static Boolean print(Boolean o) {
        System.out.println(o);
        return o;
    }

    public static int print(int i) {
        System.out.println(i);
        return i;
    }

    public static double linearRangeScale(double value, double in_min,
            double in_max, double out_min, double out_max) {
        return (value - in_min) / (in_max - in_min) * (out_max - out_min)
                + out_min;
    }

    /*
     * A very fast rounding function.
     * 
     * Warning: there is no guarantee of accuracy.
     * 
     * It is sometimes one off from IEEE rounding when the double is around 10^8
     */
    public static int fastround(double v) {
        if (v > 0) {
            int k = (int) v;
            if ((v - k) > 0.5) {
                return k + 1;
            } else {
                return k;
            }
        } else {
            int k = (int) -v;
            if ((-v - k) > 0.5) {
                return -k - 1;
            } else {
                return -k;
            }
        }
    }

}