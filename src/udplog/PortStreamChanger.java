package udplog;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class PortStreamChanger implements ChangeListener {
    public PortStreamChanger(Receiver r) {
        rec = r;
    }

    private final Receiver rec;

    @Override
    public void stateChanged(ChangeEvent e) {
        int t = ((SpinnerNumberModel) ((JSpinner) e.getSource()).getModel())
                .getNumber().intValue();
        rec.setNewPort(t);
    }
}