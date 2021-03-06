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

    private void removeRow(Asset assetRow) {
        int currentSize = assets.size();
        assets.remove(assetRow);
        fireTableRowsDeleted(currentSize, currentSize);
    }

    public int removeAssetUnits(Asset assetToRemove, int units, boolean isSimple) {
        return removeAssetUnitsCommon(assetToRemove, units, isSimple);
    }

    private int removeAssetUnitsCommon(Asset assetToRemove, int units, boolean isSimple) {
        int soldUnits = units;
        List<Asset> assetsCopy = new ArrayList<Asset>(assets);
        for (Asset a : assetsCopy) {
            if (a.equals(assetToRemove)) {
                Integer rowIndex = getRowByShortName(a.getShortName());
                if (rowIndex != null) {
                    if (a.getNumberOfUnits() - units <= 0) {
                        if (isSimple)
                            removeAssetUnitsSimpleVariant(assetToRemove);
                        else
                            soldUnits = removeAssetUnitsComplexVariant(a, rowIndex);
                    }
                    else {
                        setValueAt(a.getNumberOfUnits() - units, rowIndex, 1);
                    }
                    break;
                }
            }
        }
        return soldUnits;
    }

    private void removeAssetUnitsSimpleVariant(Asset assetToRemove) {
        removeRow(assetToRemove);
    }

    private int removeAssetUnitsComplexVariant(Asset a, Integer rowIndex) {
        int soldUnits = a.getNumberOfUnits() / 2;
        if (soldUnits == 0)
            soldUnits = a.getNumberOfUnits();
        setValueAt(a.getNumberOfUnits() - soldUnits, rowIndex, 1);
        return soldUnits;
    }

    public void addAssetUnits(Asset assetToAdd) {
        for (Asset a : assets) {
            if (a.equals(assetToAdd)) {
                Integer rowIndex = getRowByShortName(a.getShortName());
                if (rowIndex != null) {
                    setValueAt(a.getNumberOfUnits() + assetToAdd.getNumberOfUnits(), rowIndex, 1);
                    return;
                }
            }
        }
        addRow(assetToAdd);
    }

    public void changeAssetPrice(Asset asset, BigDecimal amount) {
        replaceAssetPrice(asset, getCurrentAssetValue(asset).add(amount));
    }

    public void replaceAssetPrice(Asset asset, BigDecimal amount) {
        Integer rowIndex = getRowByShortName(asset.getShortName());
        if (rowIndex != null)
            setValueAt(amount, rowIndex, 2);
    }

    public BigDecimal getCurrentAssetValue(Asset assetToCheck) {
        for (Asset a : assets) {
            if (a.equals(assetToCheck)) {
                return a.getUnitValue();
            }
        }
        return null;
    }

    public List<Asset> getAssets() {
        return this.assets;
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
