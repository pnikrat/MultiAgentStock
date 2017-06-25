package agents;


import behaviours.TradingPeriodBehaviour;
import gui.SessionManagerGui;
import jade.core.AID;
import jade.core.Agent;
import models.Asset;
import utils.DfAgentUtils;

import java.math.BigDecimal;

/**
 * Created by Przemek on 2017-06-20.
 */
public class SessionManager extends Agent {
    private DfAgentUtils utils;
    private AID[] stockTraders;
    private AID historian;
    private SessionManagerGui gui;
    private boolean tradingStatus;

    @Override
    protected void setup() {
        System.out.println("Session manager agent " + getAID().getName() + " is ready");

        tradingStatus = false;

        utils = new DfAgentUtils(this);
        utils.registerService("sessionManager", "sessionAgent");
        scanForStockTraders();
        historian = utils.searchForService("historian", "historianAgent")[0];

        gui = new SessionManagerGui();
        gui.showGui();

        addBehaviour(new TradingPeriodBehaviour(this, 30000));
    }

    @Override
    protected void takeDown() {
        System.out.println("Session manager agent going down");
        utils.deregisterService();
        gui.dispose();
    }

    public void setTradingStatus(boolean tradingStatus) {
        this.tradingStatus = tradingStatus;
    }

    public boolean getTradingStatus() {
        return tradingStatus;
    }

    public void scanForStockTraders() {
        stockTraders = utils.searchForService("trader", "tradingAgent");
    }

    public AID[] getStockTraders() {
        return stockTraders;
    }

    public int sellStock(Asset toSell, int units) {
        return gui.sellAsset(toSell, units);
    }

    public void buyStock(Asset toBuy) {
        gui.buyAsset(toBuy);
    }

    public BigDecimal getCurrentAssetValue(Asset assetToCheck) {
        return gui.getCurrentAssetValue(assetToCheck);
    }
}
