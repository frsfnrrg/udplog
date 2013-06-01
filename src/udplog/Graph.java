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
        // to optimize still further, change this into a circular buffer.
        // this would allow faster access & easier removal of first k/iteration
        // over last k
        sets = new ArrayDeque<float[]>();
    }

    /*
     * Send a value to the Graph object to be graphed; This must be done on the
     * Swing Thread.
     */
    public void sendValue(String value) {
        String[] ns = value.split(" *, *");
        float[] currentValues = new float[ns.length];
        for (int ii = 0; ii < ns.length; ii++) {
            currentValues[ii] = Float.valueOf(ns[ii]);
        }
        sets.addLast(currentValues);

        this.repaint(500);
        // repaint auto clips: therefore, to send a message of new fields, stuff
        // must be done.
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D gg = (Graphics2D) g;
        int height = this.getHeight();
        int width = this.getWidth();
        gg.setBackground(Color.WHITE);
        gg.clearRect(0, 0, width, height);
        gg.setStroke(new BasicStroke(1.5f));

        // the sector to be redrawn is specified by gg.getClipBounds
        //
        // There are several cases:
        // - redraw entire window or portion in response to hiding; redraw that
        // X portion
        // - redraw entire window in response to scaling; redraw all
        // - redraw all in response to new data: eqv. shift left & add to tail

        if (width <= 1) {
            return;
        }

        float[] lvs = {};
        // or should I just use a circular buffer, with end cap? Rescale on
        // rescale?
        while (sets.size() > width) {
            tick -= 1;
            lvs = sets.removeFirst();
        }

        // implicit k < size <= width
        gg.setColor(Color.BLACK);
        for (int k = (tick % VERTICAL_LINE_SPACING_PX); k < sets.size(); k += VERTICAL_LINE_SPACING_PX) {
            gg.drawLine(k, 0, k, height);
        }

        // fundamental algo problem - it redraws material that it should not
        // need to

        // also, it could help to flatten the loop still further; precache the
        // colors (maybe on recieving...). We could end up with overflow due to
        // colors,
        // but that assumes someone sends 2 GB single UDP packets :-O
        // and make a new one on demand. Then iterate through each set, and for
        // each color deal with the polyline adding/drawing.

        for (int c = 0;; c++) {
            gg.setColor(Color.getHSBColor(c * 0.17f, 1.0f, 0.7f));

            float last;
            if (lvs.length <= c) {
                last = Float.NaN;
            } else {
                last = lvs[c];
            }

            int x = -1;
            int[] xp = new int[sets.size() + 2];
            int[] yp = new int[sets.size() + 2];
            int cutoff = -1;
            for (float[] cvs : sets) {
                x++;

                if (cvs.length <= c) {
                    if (cutoff > 0) {
                        gg.drawPolyline(xp, yp, cutoff + 1);
                    }
                    cutoff = -1;
                    continue;
                }

                float curr = cvs[c];

                cutoff += 1;

                xp[cutoff] = x;
                yp[cutoff] = Util.fastround(Util.linearRangeScale(curr,
                        minValue, maxValue, height, 0.0));

                last = curr;

            }
            if (cutoff > 0) {
                gg.drawPolyline(xp, yp, cutoff + 1);
            }

            // i.e., last was never set
            if (Float.isNaN(last)) {
                break;
            }
        }
    }

    public void setMaximum(float doubleValue) {
        maxValue = doubleValue;
    }

    public void setMinimum(float doubleValue) {
        minValue = doubleValue;
    }
}
