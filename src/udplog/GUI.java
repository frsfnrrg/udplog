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
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultFormatter;

class GUI {
    public static final int MIN_MAX_LINES = 20;
    public static final int DEFAULT_MAX_LINES = 2000;
    public static final int MAX_MAX_LINES = 10000;

    /**
     * Makes a GUI for the UDP logger.
     */
    public GUI(Receiver r) {
        PortStreamChanger psc = new PortStreamChanger(r);

        // STOP - orange; START - green
        // CLEAR - blue
        graphingWindows = new ArrayList<GraphWindow>();

        final JButton stopper = new JButton("STOP");
        stopper.setBackground(new Color(255, 125, 20));
        stopper.setToolTipText("Ignore incoming packets");

        stopper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Boolean b = (stopper.getText() == "START");
                toggleReceiver(b);
                if (b) {
                    stopper.setText("STOP");
                    stopper.setBackground(new Color(255, 195, 40));
                    stopper.setToolTipText("Ignore incoming packets");
                } else {
                    stopper.setText("START");
                    stopper.setBackground(new Color(40, 255, 195));
                    stopper.setToolTipText("Receive incoming packets"
                            + " and log to a new file");
                }
            }
        });

        JButton clear = new JButton("CLEAR");
        clear.setBackground(new Color(40, 195, 255));
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearOutputFeed();
            }
        });
        clear.setToolTipText("Clear the stream and the table of values.");

        JButton quit = new JButton("QUIT");
        quit.setBackground(new Color(255, 255, 255));
        quit.addActionListener(new Exiter());
        quit.setToolTipText("Exit this program.");

        JPanel stopPanel = new JPanel();
        stopPanel.setLayout(new BoxLayout(stopPanel, BoxLayout.X_AXIS));
        int vs = 70;
        stopPanel.add(Box.createVerticalStrut(vs));
        stopPanel.add(Box.createHorizontalGlue());
        stopPanel.add(stopper);
        stopPanel.add(Box.createHorizontalGlue());
        stopPanel.add(clear);
        stopPanel.add(Box.createHorizontalGlue());
        stopPanel.add(quit);
        stopPanel.add(Box.createHorizontalGlue());
        stopPanel.add(Box.createVerticalStrut(vs));

        // If we are ever really, really bored, fill out the sub-documents to be
        // simpler and more efficient.
        outputFeed = new JTextArea();
        outputFeed.setEditable(false);

        JScrollPane feedScroller = new JScrollPane(outputFeed);

        JLabel feedDesc = new JLabel("Expects: \"KEY:data!\\n\"");

        JSpinner maxChanger = new JSpinner();
        maxChanger.setModel(new SpinnerNumberModel(DEFAULT_MAX_LINES,
                MIN_MAX_LINES, MAX_MAX_LINES, 1));
        ((DefaultFormatter) ((JFormattedTextField) maxChanger.getEditor()
                .getComponent(0)).getFormatter()).setCommitsOnValidEdit(true);
        maxChanger.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setMaxLines(((SpinnerNumberModel) ((JSpinner) e.getSource())
                        .getModel()).getNumber().intValue());
            }
        });
        maxChanger.setMaximumSize(new Dimension(100, 30));
        maxChanger.setToolTipText("Change the cap on the maximum number"
                + " of rows for the port stream");

        JPanel subfpanel = new JPanel();
        subfpanel.setLayout(new BoxLayout(subfpanel, BoxLayout.X_AXIS));
        subfpanel.add(Box.createHorizontalStrut(5));
        subfpanel.add(feedDesc);
        subfpanel.add(Box.createHorizontalStrut(5));
        subfpanel.add(Box.createHorizontalGlue());
        subfpanel.add(new JLabel("Max Rows:"));
        subfpanel.add(Box.createHorizontalStrut(5));
        subfpanel.add(maxChanger);
        subfpanel.add(Box.createHorizontalStrut(5));

        JPanel feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.add(feedScroller);
        feedPanel.add(Box.createVerticalStrut(2));
        feedPanel.add(subfpanel);

        outputTable = new MModel();
        final JTable outputJTable = new JTable(outputTable);
        outputJTable.setFillsViewportHeight(true);
        TableColumnModel c = outputJTable.getColumnModel();
        c.getColumn(0).setPreferredWidth(50);
        c.getColumn(1).setPreferredWidth(400);
        c.getColumn(0).setResizable(true);
        outputJTable.setCellSelectionEnabled(true);
        outputJTable.getSelectedRow();

        JButton makeGraphButton = new JButton("Graph Selected Set");
        makeGraphButton.setBackground(new Color(255, 255, 255));
        makeGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = outputJTable.getSelectedRow();
                if (choice == -1)
                    return;
                System.out.println("Spawning a new graph.");
                // make a graph window...; pump the feeds into it..
                graphingWindows.add(new GraphWindow((String) outputTable
                        .getValueAt(choice, 0), graphingWindows));
            }
        });
        makeGraphButton.setToolTipText("Open a new window that"
                + " graphs the values from this feed");

        JSpinner portChanger = new JSpinner();
        portChanger.setModel(new SpinnerNumberModel(udplog.DEFAULT_PORT, 1,
                65536, 1));
        ((DefaultFormatter) ((JFormattedTextField) portChanger.getEditor()
                .getComponent(0)).getFormatter()).setCommitsOnValidEdit(true);
        portChanger.addChangeListener(psc);
        portChanger.setMaximumSize(new Dimension(100, 30));
        portChanger.setToolTipText("Change the UDP port to"
                + " listen for packets from");

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.X_AXIS));
        settingsPanel.add(Box.createHorizontalStrut(5));
        settingsPanel.add(new JLabel("UDP Port:"));
        settingsPanel.add(Box.createHorizontalStrut(5));
        settingsPanel.add(portChanger);
        settingsPanel.add(Box.createHorizontalStrut(5));
        settingsPanel.add(psc.getStatusIndicator());
        settingsPanel.add(Box.createHorizontalStrut(5));
        settingsPanel.add(Box.createHorizontalGlue());
        settingsPanel.add(Box.createHorizontalStrut(5));

        JScrollPane tableScroller = new JScrollPane(outputJTable);
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.add(tableScroller);
        tablePanel.add(Box.createHorizontalStrut(2));
        tablePanel.add(makeGraphButton);

        JPanel blankPanel = new JPanel(new BorderLayout());
        JLabel note = new JLabel("This tab requires almost no CPU.");
        note.setHorizontalAlignment(SwingConstants.CENTER);
        blankPanel.add(note, BorderLayout.CENTER);

        JTabbedPane outputPain = new JTabbedPane();
        outputPain.addTab("Blank", null, blankPanel, "Empty panel");
        outputPain.addTab("Port Stream", null, feedPanel,
                "Text stream from the incoming packets");
        outputPain.addTab("Tables", null, tablePanel,
                "A table displaying the last value for each substream");

        JPanel main = new JPanel();
        main.setLayout(new BorderLayout());
        main.add(stopPanel, BorderLayout.NORTH);
        main.add(outputPain, BorderLayout.CENTER);
        main.add(settingsPanel, BorderLayout.SOUTH);

        window = new JFrame();
        window.setVisible(false);
        window.setContentPane(main);
        window.addWindowListener(new Exiter());
        window.setSize(500, 300);
        window.setTitle("UDPLog");

        rec = r;
        maxLines = DEFAULT_MAX_LINES;
    }

    public void show() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // newlines should be sent with it
                window.setVisible(true);
            }
        });
    }

    private void toggleReceiver(Boolean on) {
        // called by the SwingUtilities thread
        rec.setEnabled(on);

    }

    private void setMaxLines(int v) {
        maxLines = v;
    }

    private final JTextArea outputFeed;
    private final Receiver rec;
    private final MModel outputTable;
    private final JFrame window;
    private int maxLines;
    private ArrayList<GraphWindow> graphingWindows;

    // @SuppressWarnings("unused")
    public void showNewPacket(final String val) {
        if (true) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // newlines should be sent with it
                    outputFeed.append(val);
                    int k = outputFeed.getLineCount() - maxLines;
                    if (k > 0) {
                        outputFeed.setText("");
                    }
                }
            });
        }
    }

    private void clearOutputFeed() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                outputFeed.setText("");
                outputTable.empty();
            }
        });
    }

    public void showNewPair(final String prefix, final String values) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                outputTable.postLine(prefix, values);
                for (GraphWindow g : graphingWindows) {
                    g.sendPair(prefix, values);
                }
            }
        });
    }
}