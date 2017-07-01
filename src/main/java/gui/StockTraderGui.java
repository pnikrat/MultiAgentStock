package gui;

import agents.StockTrader;
import models.Asset;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Przemek on 2017-06-20.
 */
public class StockTraderGui extends JFrame {
    private StockTrader guiAgent;

    private JLabel maximumLoss, desiredGain, currentMoney, tradingStatus, inventory;
    private String maximumLossBase, desiredGainBase, currentMoneyBase, tradingStatusBase, inventoryBase;
    private JTable inventoryTable;
    private AssetInventoryTableModel inventoryTableModel;
    private JScrollPane scrollPane;

    public StockTraderGui(StockTrader guiAgent) {
        super(guiAgent.getLocalName());
        this.guiAgent = guiAgent;
        initLabelsBaseTexts();
        initPanel();
        addListeners();
        makeUnresizable();
    }

    public void showGui() {
        pack();
        setVisible(true);
    }

    public void setMaximumLoss(final String loss) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                maximumLoss.setText(maximumLossBase + loss);
            }
        });
    }

    public void setDesiredGain(final String gain) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                desiredGain.setText(desiredGainBase + gain);
            }
        });
    }

    public void setCurrentMoney(final String money) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                currentMoney.setText(currentMoneyBase + money);
            }
        });
    }

    public void setTradingStatus(final boolean status) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String statusDescription = status ? "Open" : "Closed";
                tradingStatus.setText(tradingStatusBase + statusDescription);
            }
        });
    }

    public void addAsset(Asset asset) {
        inventoryTableModel.addRow(asset);
    }

    public void addBoughtAsset(Asset asset) {
        inventoryTableModel.addAssetUnits(asset);
    }

    public void removeSoldAsset(Asset asset) {
        inventoryTableModel.removeAssetUnits(asset, asset.getNumberOfUnits());
    }

    public List<Asset> getAssets() {
        return inventoryTableModel.getAssets();
    }

    private void initLabelsBaseTexts() {
        maximumLossBase = "Maximum loss: ";
        desiredGainBase = "Desired gain: ";
        currentMoneyBase = "Current money: ";
        tradingStatusBase = "Trading: ";
        inventoryBase = "Assets in inventory: ";
    }

    private void initPanel() {
        Container pane = getContentPane();
        Box verticalBox = Box.createVerticalBox();
        verticalBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pane.add(verticalBox);
        defineComponents();
        addComponents(verticalBox);

        defineTable();
        verticalBox.add(scrollPane);
    }

    private void defineComponents() {
        maximumLoss = new JLabel(maximumLossBase);
        configLabel(maximumLoss);

        desiredGain = new JLabel(desiredGainBase);
        configLabel(desiredGain);

        currentMoney = new JLabel(currentMoneyBase);
        configLabel(currentMoney);

        tradingStatus = new JLabel(tradingStatusBase);
        configLabel(tradingStatus);

        inventory = new JLabel(inventoryBase);
        configLabel(inventory);
    }

    private void configLabel(JLabel label) {
        label.setPreferredSize(new Dimension(200, 30));
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        label.setFont(new Font("sans-serif", Font.PLAIN, 18));
    }

    private void addComponents(Container box) {
        box.add(maximumLoss);
        box.add(desiredGain);
        box.add(currentMoney);
        box.add(tradingStatus);
        box.add(inventory);
    }

    private void defineTable() {
        inventoryTableModel = new AssetInventoryTableModel();
        inventoryTable = new JTable(inventoryTableModel);
        inventoryTable.setPreferredScrollableViewportSize(new Dimension(500,200));
        scrollPane = new JScrollPane(inventoryTable);
    }

    private void addListeners() {
        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                guiAgent.doDelete();
            }
        } );
    }

    private void makeUnresizable() {
        setResizable(false);
    }
}
