package agents;

import behaviours.SubmitOrdersBehaviour;
import gui.StockTraderGui;
import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import models.Asset;
import models.MarketOfAssets;
import models.TradingResult;
import utils.DfAgentUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Przemek on 2017-06-20.
 */
public class StockTrader extends Agent {

    private List<String> startupArguments = new ArrayList<String>();
    private DfAgentUtils utils;
    private AID sessionManager;
    private AID historian;
    private StockTraderGui gui;
    private BigDecimal maximumLoss;
    private BigDecimal desiredGain;
    private BigDecimal currentMoney;
    private boolean tradingStatus;
    private MessageTemplate tradingTemplate;

    @Override
    protected void setup() {
        System.out.println("Asset trader agent " + getAID().getName() + " is ready");
        try {
            collectStartupArguments();
        }
        catch (NullPointerException e) {
            System.out.println("No trading goals specified, terminating...");
            doDelete();
        }
        gui = new StockTraderGui(this);
        gui.showGui();

        utils = new DfAgentUtils(this);
        utils.registerService("trader", "tradingAgent");
        sessionManager = utils.searchForService("sessionManager", "sessionAgent")[0];
        historian = utils.searchForService("historian", "historianAgent")[0];

        setMaximumLoss(startupArguments.get(0));
        setDesiredGain(startupArguments.get(1));
        setCurrentMoney(startupArguments.get(2));
        setTradingStatus(false);

        setTradingTemplateAttributes();
        addBehaviour(new SubmitOrdersBehaviour(this, tradingTemplate));
    }

    @Override
    protected void takeDown() {
        System.out.println("Asset trader agent going down");
        utils.deregisterService();
        gui.dispose();
    }

    public void setTradingStatus(boolean tradingStatus) {
        this.tradingStatus = tradingStatus;
        gui.setTradingStatus(tradingStatus);
    }

    public void addBoughtStock(TradingResult result) {
        Asset boughtAsset = result.getAssetTraded();
        gui.addBoughtAsset(boughtAsset);
        deduceMoney(boughtAsset);
    }

    private void collectStartupArguments() {
        Object[] args = getArguments();
        for (Object x : args)
            startupArguments.add((String) x);
    }

    private boolean checkIfArgumentsEmpty() {
        return startupArguments.size() == 0;
    }

    private void printStartupArguments() {
        for (String x : startupArguments)
            System.out.println(x + " ");
    }

    private void setMaximumLoss(String maximumLoss) {
        this.maximumLoss = BigDecimal.valueOf(Double.parseDouble(maximumLoss));
        gui.setMaximumLoss(maximumLoss);
    }

    private void setDesiredGain(String desiredGain) {
        this.desiredGain = BigDecimal.valueOf(Double.parseDouble(desiredGain));
        gui.setDesiredGain(desiredGain);
    }

    private void setCurrentMoney(String currentMoney) {
        this.currentMoney = BigDecimal.valueOf(Double.parseDouble(currentMoney));
        gui.setCurrentMoney(currentMoney);
    }

    private void setTradingTemplateAttributes() {
        tradingTemplate = MessageTemplate.and(
                MessageTemplate.and(
                        MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                        MessageTemplate.MatchPerformative(ACLMessage.CFP)),
                MessageTemplate.MatchConversationId("stock-trading"));
    }

    private void deduceMoney(Asset boughtAsset) {
        BigDecimal valueOfBoughtAsset = boughtAsset.getUnitValue();
        int units = boughtAsset.getNumberOfUnits();
        BigDecimal deductedMoney = valueOfBoughtAsset.multiply(new BigDecimal(units));
        setCurrentMoney(getCurrentMoney().subtract(deductedMoney).toString());
    }

    private BigDecimal getCurrentMoney() {
        return currentMoney;
    }
}
