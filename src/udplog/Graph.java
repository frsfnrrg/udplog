package udplog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayDeque;

import javax.swing.JPanel;

public class Graph extends JPanel {
    public static float DEFAULT_MAX_VALUE = 1.25f;
    public static float DEFAULT_MIN_VALUE = -1.25f;

    private static final long serialVersionUID = -5351902697006157075L;
    private static final int VERTICAL_LINE_SPACING_PX = 100;

    private float maxValue;
    private float minValue;
    private volatile int tick;
    private final ArrayDeque<float[]> sets;

    public Graph() {
        super();
        maxValue = DEFAULT_MAX_VALUE;
        minValue = DEFAULT_MIN_VALUE;
        tick = 0;
        // I don't think we'll accumulate 1000 points before a repaint

        // todo = new ArrayBlockingQueue<float[]>(1000);
        sets = new ArrayDeque<float[]>();

    }

    public void sendValue(String value) {
        String[] ns = value.split("[ ]*,[ ]*");
        float[] currentValues = new float[ns.length];
        for (int ii = 0; ii < ns.length; ii++) {
            currentValues[ii] = Float.valueOf(ns[ii]);
        }
        try {
            // todo.add(currentValues);
            sets.addLast(currentValues);
            tick++;
        } catch (IllegalStateException e) {
            System.out.println("Queue too full..");
        }

        this.repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D gg = (Graphics2D) g;
        int height = this.getHeight();
        int width = this.getWidth();
        gg.clearRect(0, 0, width, height);
        gg.setStroke(new BasicStroke(1.5f));

        if (width <= 1) {
            return;
        }

        float[] lvs = {};
        while (sets.size() > width) {
            lvs = sets.removeFirst();
        }

        int ls;
        if (tick > width) {
            ls = -(tick % VERTICAL_LINE_SPACING_PX);
        } else {
            ls = 0;
        }

        // implict k < size <= width
        gg.setColor(Color.BLACK);
        for (int k = ls; k < sets.size(); k += VERTICAL_LINE_SPACING_PX) {
            gg.drawLine(k, 0, k, height);
        }

        for (int c = 0;; c++) {
            gg.setColor(Color.getHSBColor(c * 0.17f, 1.0f, 1.0f));

            float last;
            if (lvs.length <= c) {
                last = Float.NaN;
            } else {
                last = lvs[c];
            }

            int x = 0;
            for (float[] cvs : sets) {
                if (cvs.length <= c) {
                    continue;
                }

                float curr = cvs[c];

                gg.drawLine(x - 1, (int) Math.round(Util.linearRangeScale(last,
                        minValue, maxValue, height, 0.0)), x, (int) Math
                        .round(Util.linearRangeScale(curr, minValue, maxValue,
                                height, 0.0)));

                last = curr;
                x++;
            }

            // ie, last was never set
            if (Float.isNaN(last)) {
                break;
            }
        }

        /* @formatter:off
        int i = 0;
        for (float[] cvs : sets) {

            int minlength = Math.min(lvs.length, cvs.length);

            gg.setStroke(new BasicStroke(1.5f));
            for (int ii = 0; ii < minlength; ii++) {
                gg.setColor(Color.getHSBColor(ii * 0.17f, 1.0f, 1.0f));

                gg.drawLine(i - 1, (int) Math.round(Util.linearRangeScale(
                        lvs[ii], minValue, maxValue, height, 0.0)), i,
                        (int) Math.round(Util.linearRangeScale(cvs[ii],
                                minValue, maxValue, height, 0.0)));
            }

            lvs = cvs;

            i++;
        }*/
//      @formatter:on
    }

    public void setMaximum(double doubleValue) {
        maxValue = (float) doubleValue;
    }

    public void setMinimum(double doubleValue) {
        minValue = (float) doubleValue;
    }
}
