package gui;

import models.Asset;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Przemek on 2017-06-20.
 */
public class AssetInventoryTableModel extends AbstractTableModel {

    private List<Asset> assets = new ArrayList<Asset>();
    private String[] attributeNames = {
            "Short name",
            "Number of units",
            "Unit value"
    };

    public AssetInventoryTableModel() {

    }

    public AssetInventoryTableModel(List<Asset> assets) {
        this.assets = assets;
    }

    public int getRowCount() {
        return assets.size();
    }

    public int getColumnCount() {
        return attributeNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return attributeNames[column];
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Asset singleRow = getStock(rowIndex);

        switch (columnIndex) {
            case 0: return singleRow.getShortName();
            case 1: return singleRow.getNumberOfUnits();
            case 2: return singleRow.getUnitValueRepresentation();
            default: return null;
        }
    }

    public void addRow(Asset assetRow) {
        if (assetRow == null)
            throw new IllegalArgumentException("Row data cannot be null");
        int currentSize = assets.size();
        assets.add(assetRow);
        fireTableRowsInserted(currentSize, currentSize);
    }

    private Asset getStock(int rowIndex) {
        return assets.get(rowIndex);
    }
}
