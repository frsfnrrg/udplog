package udplog;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class PortStreamChanger implements ChangeListener {
    public PortStreamChanger(Receiver r) {
        rec = r;
        if (r.isConnected()) {
            status = new JLabel("Connected.");
        } else {
            status = new JLabel("Failed.");
        }

    }

    private final Receiver rec;
    private final JLabel status;

    /*
     * Changes the port of the Receiver; also updates the label displaying the
     * Receiver state.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        int t = ((SpinnerNumberModel) ((JSpinner) e.getSource()).getModel())
                .getNumber().intValue();

        boolean success = rec.setNewPort(t);

        if (success) {
            status.setText("Connected.");
        } else {
            status.setText("Failed.");
        }
    }

    public JLabel getStatusIndicator() {
        return status;
    }
}