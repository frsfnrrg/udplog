package udplog;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

class MModel extends AbstractTableModel {
    public MModel() {
        super();
    }

    private static final long serialVersionUID = 1339564197013013517L;
    private final String[] columnNames = { "Key", "Value" };
    private final List<String[]> data = new ArrayList<String[]>();

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        return data.get(row)[col];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        data.get(row)[col] = value.toString();
        super.fireTableCellUpdated(row, col);
    }

    public void postLine(String k, String v) {
        int ii;
        for (ii = 0; ii < data.size(); ii++) {
            if (k.equals(data.get(ii)[0])) {

                data.get(ii)[1] = v;
                super.fireTableCellUpdated(ii, 1);
                return;
            }
        }
        // new line
        data.add(new String[] { k, v });
        super.fireTableCellUpdated(data.size() - 1, 0);
        super.fireTableCellUpdated(data.size() - 1, 1);
    }

    public void empty() {
        data.clear();
        super.fireTableDataChanged();
    }

}