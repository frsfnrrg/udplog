package udplog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class GraphExiter extends WindowAdapter implements ActionListener {
    private final ArrayList<GraphWindow> us;
    private final GraphWindow me;

    public GraphExiter(GraphWindow me, ArrayList<GraphWindow> us) {
        super();
        this.us = us;
        this.me = me;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        us.remove(me);
        me.setVisible(false);
        me.removeWindowListener(this);
        me.dispose();
    }

    @Override
    public void windowClosing(WindowEvent we) {
        us.remove(me);
        me.setVisible(false);
        me.removeWindowListener(this);
        me.dispose();
    }
}