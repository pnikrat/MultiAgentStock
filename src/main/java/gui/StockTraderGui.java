package gui;

import agents.StockTrader;
import models.Asset;
import models.TrendQuery;

import javax.swing.*;
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

    private JLabel maximumLoss, desiredGain, currentMoney, tradingStatus, inventory, logLabel;
    private String maximumLossBase, desiredGainBase, currentMoneyBase, tradingStatusBase, inventoryBase, logLabelBase;
    private JTable inventoryTable;
    private JTextArea logArea;
    private AssetInventoryTableModel inventoryTableModel;
    private JScrollPane scrollPaneForTable, scrollPaneForLog;
    private int tradingSessionsCounter = 1;

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
        setLocationByPlatform(true);
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

    public void appendToLog(final String logMessage) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                logArea.append("(" + tradingSessionsCounter + ") " + logMessage + "\n");
                tradingSessionsCounter++;
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
        inventoryTableModel.removeAssetUnits(asset, asset.getNumberOfUnits(), true);
    }

    public void updatePrices(Asset assetToUpdatePrice, BigDecimal difference) {
        inventoryTableModel.changeAssetPrice(assetToUpdatePrice, difference);
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
        logLabelBase = "Previous actions: ";
    }

    private void initPanel() {
        Container pane = getContentPane();
        Box verticalBox = Box.createVerticalBox();
        verticalBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pane.add(verticalBox);
        defineComponents();
        addComponents(verticalBox);

        defineTable();
        verticalBox.add(scrollPaneForTable);
        verticalBox.add(logLabel);

        defineLog();
        verticalBox.add(scrollPaneForLog);
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

        logLabel = new JLabel(logLabelBase);
        configLabel(logLabel);
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
        inventoryTable.setPreferredScrollableViewportSize(new Dimension(500,100));
        scrollPaneForTable = new JScrollPane(inventoryTable);
    }

    private void defineLog() {
        logArea = new JTextArea(6, 20);
        logArea.setEditable(false);
        scrollPaneForLog = new JScrollPane(logArea);
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
