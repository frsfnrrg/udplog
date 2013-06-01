package udplog;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayDeque;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Graph extends JPanel {
    public static float DEFAULT_MAX_VALUE = 1.25f;
    public static float DEFAULT_MIN_VALUE = -1.25f;

    private static final long serialVersionUID = -5351902697006157075L;
    private static final int VERTICAL_LINE_SPACING_PX = 100;

    private float maxValue;
    private float minValue;
    private volatile int tick;
    private final ArrayDeque<float[]> sets;

    private Color[] colors;
    private int bufferlength;

    public Graph() {
        super();
        maxValue = DEFAULT_MAX_VALUE;
        minValue = DEFAULT_MIN_VALUE;
        tick = 0;
        // to optimize still further, change this into a circular buffer.
        // this would allow faster access & easier removal of first k/iteration
        // over last k
        sets = new ArrayDeque<float[]>();

        // redrawing material
        size = 2;
        bufferlength = 500;

        colors = new Color[size]; // set of colors to use
        xps = new int[size][bufferlength]; // x pos buffer
        yps = new int[size][bufferlength]; // y pos buffer
        cutoffs = new int[size]; // how long the run has been
        for (int ii = 0; ii < size; ii++) {
            colors[ii] = Color.getHSBColor(ii * 0.17f, 1.0f, 0.7f);
        }
        repainter = null;
    }

    private Thread repainter;

    private void clearRepainter() {
        repainter = null;
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

        if (repainter == null) {
            repainter = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            repaint(500);
                            clearRepainter();
                        }
                    });
                }
            };
            repainter.start();
        }

        // TODO: limit the repaint frequency to 10 per second. In a non-hacky
        // way.
    }

    private int size;
    private int[][] xps;
    private int[][] yps;
    private int[] cutoffs;

    @Override
    public void paintComponent(Graphics gg) {
        int height = this.getHeight();
        int width = this.getWidth();

        ((Graphics2D) gg).setBackground(Color.WHITE);
        gg.clearRect(0, 0, width, height);

        while (sets.size() > width) {
            tick -= 1;
            sets.removeFirst();
        }

        // implicit k < size <= width
        gg.setColor(Color.BLACK);
        for (int k = (tick % VERTICAL_LINE_SPACING_PX); k < sets.size(); k += VERTICAL_LINE_SPACING_PX) {
            gg.drawLine(k, 0, k, height);
        }

        float[] lvs = {};

        if (bufferlength < sets.size() + 2) {
            // Expand the point buffer length if needed
            int newlength = sets.size() + 2;
            int[] cxp;
            int[] cyp;
            for (int ii = 0; ii < size; ii++) {
                cxp = xps[ii];
                cyp = yps[ii];

                xps[ii] = new int[newlength];
                yps[ii] = new int[newlength];

                System.arraycopy(cxp, 0, xps[ii], 0, bufferlength);
                System.arraycopy(cyp, 0, yps[ii], 0, bufferlength);
            }
            bufferlength = newlength;
        }

        // Reset cutoffs
        for (int ii = 0; ii < size; ii++) {
            cutoffs[ii] = -1;
        }

        int length = 0; // how many tracks there are
        int x = 0;
        for (float[] cvs : sets) {
            if (cvs.length > size) {
                // Make all buffers etc wider
                int newsize = cvs.length;

                Color[] ctemp = colors;
                int[][] cxps = xps;
                int[][] cyps = yps;
                int[] coffsets = cutoffs;

                colors = new Color[newsize];
                xps = new int[newsize][sets.size() + 2];
                yps = new int[newsize][sets.size() + 2];
                cutoffs = new int[newsize];

                System.arraycopy(ctemp, 0, colors, 0, size);
                System.arraycopy(cxps, 0, xps, 0, size);
                System.arraycopy(cyps, 0, yps, 0, size);
                System.arraycopy(coffsets, 0, cutoffs, 0, size);

                for (int ii = size; ii < newsize; ii++) {
                    colors[ii] = Color.getHSBColor(ii * 0.17f, 1.0f, 0.7f);
                    cutoffs[ii] = -1;
                }

                size = newsize;
            }

            if (cvs.length < length) {
                // finalize tracks between cvs.length and length ,<=,<,
                for (int track = cvs.length; track < length; track++) {
                    int cutoff = cutoffs[track];
                    if (cutoff > 0) {
                        gg.setColor(colors[track]);
                        gg.drawPolyline(xps[track], yps[track], cutoff + 1);
                    }
                    cutoffs[track] = -1;
                }
            }

            for (int track = 0; track < cvs.length; track++) {
                // add new points
                int cutoff = (++cutoffs[track]);
                int[] xp = xps[track];
                int[] yp = yps[track];
                float curr = cvs[track];

                xp[cutoff] = x;
                yp[cutoff] = Util.fastround(Util.linearRangeScale(curr,
                        minValue, maxValue, height, 0.0));
            }

            lvs = cvs;
            length = cvs.length;
            x++;
        }

        for (int track = 0; track < lvs.length; track++) {
            // finalize all tracks making it to the end
            gg.setColor(colors[track]);
            gg.drawPolyline(xps[track], yps[track], cutoffs[track] + 1);
        }
    }

    public void setMaximum(float doubleValue) {
        maxValue = doubleValue;
    }

    public void setMinimum(float doubleValue) {
        minValue = doubleValue;
    }
}
