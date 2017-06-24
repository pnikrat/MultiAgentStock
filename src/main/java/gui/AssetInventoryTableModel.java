package gui;

import models.Asset;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
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

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Asset singleRow = getStock(rowIndex);

        switch (columnIndex) {
            case 1:
                if (aValue instanceof Integer) {
                    Integer value = (Integer) aValue;
                    singleRow.setNumberOfUnits(value);
                }
                break;
            case 2:
                if (aValue instanceof BigDecimal) {
                    BigDecimal value = (BigDecimal) aValue;
                    singleRow.setUnitValue(value);
                }
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void addRow(Asset assetRow) {
        if (assetRow == null)
            throw new IllegalArgumentException("Row data cannot be null");
        int currentSize = assets.size();
        assets.add(assetRow);
        fireTableRowsInserted(currentSize, currentSize);
    }

    public int removeAssetUnits(Asset assetToSell, int units) {
        int soldUnits = units;
        for (Asset a : assets) {
            if (a.equals(assetToSell)) {
                Integer rowIndex = getRowByShortName(a.getShortName());
                if (a.getNumberOfUnits() - units < 0) {
                    soldUnits = a.getNumberOfUnits();
                    if (rowIndex != null)
                        setValueAt(0, rowIndex, 1);
                }
                else {
                    if (rowIndex != null)
                        setValueAt(a.getNumberOfUnits() - units, rowIndex, 1);
                }
            }
        }
        return soldUnits;
    }

    public void addAssetUnits(Asset assetToBuy, int units) {
        for (Asset a : assets) {
            if (a.equals(assetToBuy)) {
                a.setNumberOfUnits(a.getNumberOfUnits() + units);
            }
        }
    }

    public BigDecimal getCurrentAssetValue(Asset assetToCheck) {
        for (Asset a : assets) {
            if (a.equals(assetToCheck)) {
                return a.getUnitValue();
            }
        }
        return null;
    }

    private Asset getStock(int rowIndex) {
        return assets.get(rowIndex);
    }

    private Integer getRowByShortName(String name) {
        for (int i = 0; i <= getRowCount() - 1; i++) {
            if (getValueAt(i, 0).equals(name)) {
                return i;
            }
        }
        return null;
    }
}
