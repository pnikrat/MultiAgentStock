package gui;

import agents.StockTrader;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Przemek on 2017-06-20.
 */
public class StockTraderGui extends JFrame {
    private StockTrader guiAgent;

    private JLabel maximumLoss, desiredGain, currentMoney, tradingStatus, inventory;
    private String maximumLossBase, desiredGainBase, currentMoneyBase, tradingStatusBase, inventoryBase;

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

    private void initLabelsBaseTexts() {
        maximumLossBase = "Maximum loss: ";
        desiredGainBase = "Desired gain: ";
        currentMoneyBase = "Current money: ";
        tradingStatusBase = "Trading: ";
        inventoryBase = "Stock in inventory: ";
    }

    private void initPanel() {
        Container pane = getContentPane();
        Box verticalBox = Box.createVerticalBox();
        verticalBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pane.add(verticalBox);
        defineComponents();
        addComponents(verticalBox);
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
