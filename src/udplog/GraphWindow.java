package udplog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

public class GraphWindow extends JFrame {
    private static final long serialVersionUID = -8729853562187473829L;
    private final String key;
    private final Graph g;
    private boolean paused;

    public GraphWindow(String key, ArrayList<GraphWindow> us) {
        super();
        this.key = key;
        g = new Graph();

        JButton killButton = new JButton("KILL ME");
        killButton.setBackground(new Color(255, 255, 255));
        GraphExiter exit = new GraphExiter(this, us);
        killButton.addActionListener(exit);
        killButton.setToolTipText("Slaughters this poor window");

        final JButton pauseButton = new JButton("PAUSE");
        pauseButton.setBackground(new Color(255, 195, 40));
        pauseButton.setToolTipText("Ignore incoming data");

        Dimension pbsize = new Dimension(99, 25);
        pauseButton.setMinimumSize(pbsize);
        pauseButton.setPreferredSize(pbsize);
        pauseButton.setMaximumSize(pbsize);

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Boolean b = (pauseButton.getText().equals("UNPAUSE"));
                setPause(!b);
                if (b) {
                    pauseButton.setText("PAUSE");
                    pauseButton.setBackground(new Color(255, 195, 40));
                    pauseButton.setToolTipText("Ignore incoming data");
                } else {
                    pauseButton.setText("UNPAUSE");
                    pauseButton.setBackground(new Color(40, 255, 195));
                    pauseButton.setToolTipText("Begin receiving data");
                }
            }
        });

        JSpinner maxChanger = new JSpinner();
        maxChanger.setModel(new SpinnerNumberModel(Graph.DEFAULT_MAX_VALUE,
                null, null, 1.0f));
        ((DefaultFormatter) ((JFormattedTextField) maxChanger.getEditor()
                .getComponent(0)).getFormatter()).setCommitsOnValidEdit(true);
        maxChanger.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                g.setMaximum((float) ((SpinnerNumberModel) ((JSpinner) e
                        .getSource()).getModel()).getNumber().doubleValue());
            }
        });
        maxChanger.setPreferredSize(new Dimension(80, 20));
        maxChanger.setMaximumSize(new Dimension(150, 30));
        maxChanger
                .setToolTipText("Add or remove space near the top of the graph");

        JSpinner minChanger = new JSpinner();
        minChanger.setModel(new SpinnerNumberModel(Graph.DEFAULT_MIN_VALUE,
                null, null, 1.0f));
        ((DefaultFormatter) ((JFormattedTextField) minChanger.getEditor()
                .getComponent(0)).getFormatter()).setCommitsOnValidEdit(true);
        minChanger.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                g.setMinimum((float) ((SpinnerNumberModel) ((JSpinner) e
                        .getSource()).getModel()).getNumber().doubleValue());
            }
        });
        minChanger.setPreferredSize(new Dimension(80, 20));
        minChanger.setMaximumSize(new Dimension(150, 30));
        maxChanger
                .setToolTipText("Add or remove space near the bottom of the graph");

        JPanel optionsPain = new JPanel();
        optionsPain.setLayout(new BoxLayout(optionsPain, BoxLayout.X_AXIS));
        optionsPain.add(Box.createHorizontalStrut(2));
        optionsPain.add(pauseButton);

        optionsPain.add(Box.createHorizontalStrut(6));
        optionsPain.add(new JLabel("Min Y"));
        optionsPain.add(Box.createHorizontalStrut(2));
        optionsPain.add(minChanger);

        optionsPain.add(Box.createHorizontalStrut(6));
        optionsPain.add(new JLabel("Max Y"));
        optionsPain.add(Box.createHorizontalStrut(2));
        optionsPain.add(maxChanger);

        optionsPain.add(Box.createHorizontalGlue());
        optionsPain.add(killButton);
        optionsPain.add(Box.createHorizontalStrut(2));

        JPanel w = new JPanel();
        w.setLayout(new BorderLayout());
        w.add(g, BorderLayout.CENTER);
        w.add(optionsPain, BorderLayout.SOUTH);

        paused = false;

        this.setContentPane(w);
        this.addWindowListener(exit);
        this.setSize(500, 300);
        this.setTitle("Graph: " + key);
        this.setLocationByPlatform(true);
        this.setVisible(true);
    }

    protected void setPause(Boolean b) {
        paused = b;
    }

    public void sendPair(String key, String value) {
        if (!paused) {
            if (this.key.equals(key)) {
                g.sendValue(value);
            }
        }
    }
}
