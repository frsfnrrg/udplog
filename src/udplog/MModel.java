package udplog;

import javax.swing.table.AbstractTableModel;

/**
 * An abstract table model implementation that holds a small set of key/value
 * pairs of Strings. Not editable.
 * 
 */
class MModel extends AbstractTableModel {
    public MModel() {
        super();
        size = 8;
        data = new String[size][2];
        length = 0;
    }

    private static final long serialVersionUID = 1339564197013013517L;
    private final int size;
    private int length;
    private String[][] data;

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        return length;
    }

    @Override
    public String getColumnName(int col) {
        if (col == 0) {
            return "Key";
        } else {
            return "Value";
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        // notice: this can harvest unused data
        return data[row][col];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value.toString();
        super.fireTableCellUpdated(row, col);
    }

    public void postLine(String k, String v) {
        int ii;
        for (ii = 0; ii < length; ii++) {
            if (k.equals(data[ii][0])) {

                data[ii][1] = v;
                super.fireTableCellUpdated(ii, 1);
                return;
            }
        }

        length++;
        if (length >= size) {
            String[][] n = data;
            int ns = 2 * size;
            data = new String[ns][2];
            System.arraycopy(n, 0, data, 0, size);
        }
        data[length - 1][0] = k;
        data[length - 1][1] = v;

        super.fireTableCellUpdated(length - 1, 0);
        super.fireTableCellUpdated(length - 1, 1);
    }

    public void empty() {
        length = 0;
        super.fireTableDataChanged();
    }

}