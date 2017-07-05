package gui;

import models.Asset;
import models.MarketOfAssets;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

/**
 * Created by Przemek on 2017-06-24.
 */
public class SessionManagerGui extends JFrame {

    private JLabel titleOfTable;
    private JTable inventoryTable;
    private AssetInventoryTableModel inventoryTableModel;
    private JScrollPane scrollPane;

    public SessionManagerGui() {
        super("SessionManager");
        initPanel();
        makeUnresizable();
        fillMarketWithStock();
    }

    public void showGui() {
        pack();
        setLocationByPlatform(true);
        setVisible(true);
    }

    public void addAsset(Asset asset) {
        inventoryTableModel.addRow(asset);
    }

    public int sellAsset(Asset asset, int units) {
        return inventoryTableModel.removeAssetUnits(asset, units, false);
    }

    public void buyAsset(Asset asset) {
        inventoryTableModel.addAssetUnits(asset);
    }

    public BigDecimal getCurrentAssetValue(Asset assetToCheck) {
        return inventoryTableModel.getCurrentAssetValue(assetToCheck);
    }

    public List<Asset> getAssets() {
        return inventoryTableModel.getAssets();
    }

    public void changeAssetPrice(Asset asset, BigDecimal amount) {
        inventoryTableModel.changeAssetPrice(asset, amount);
    }

    private void fillMarketWithStock() {
        MarketOfAssets market = new MarketOfAssets();
        List<Asset> assets = market.getAssetsOnMarket();
        for (Asset x : assets)
            addAsset(x);
    }

    private void initPanel() {
        Container pane = getContentPane();
        Box verticalBox = Box.createVerticalBox();
        verticalBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pane.add(verticalBox);
        defineTableTitle();
        verticalBox.add(titleOfTable);
        defineTable();
        verticalBox.add(scrollPane);
    }

    private void defineTableTitle() {
        titleOfTable = new JLabel("Assets available on market: ");
        titleOfTable.setPreferredSize(new Dimension(200, 30));
        titleOfTable.setAlignmentX(LEFT_ALIGNMENT);
        titleOfTable.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        titleOfTable.setFont(new Font("sans-serif", Font.PLAIN, 18));
    }

    private void defineTable() {
        inventoryTableModel = new AssetInventoryTableModel();
        inventoryTable = new JTable(inventoryTableModel);
        inventoryTable.setPreferredScrollableViewportSize(new Dimension(500,100));
        scrollPane = new JScrollPane(inventoryTable);
    }

    private void makeUnresizable() {
        setResizable(false);
    }
}
