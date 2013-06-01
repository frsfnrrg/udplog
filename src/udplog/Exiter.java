package udplog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Great at exiting things!
 */
class Exiter extends WindowAdapter implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.exit(0);
    }

    @Override
    public void windowClosing(WindowEvent we) {
        System.exit(0);
    }
}