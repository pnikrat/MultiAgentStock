package agents;

import behaviours.SubmitOrders;
import gui.StockTraderGui;
import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import models.Asset;
import models.MarketOfAssets;
import models.Order;
import models.TrendQuery;
import utils.DfAgentUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Przemek on 2017-06-20.
 */
public class StockTrader extends Agent {

    private List<String> startupArguments = new ArrayList<String>();
    private DfAgentUtils utils;
    private AID historian;
    private StockTraderGui gui;
    private BigDecimal maximumLoss;
    private BigDecimal desiredGain;
    private BigDecimal currentMoney;
    private BigDecimal checkedPrice;
    private boolean tradingStatus;
    private MessageTemplate tradingTemplate;
    private List<Asset> availableAssets;
    private List<Asset> assetsInInventory;
    private boolean success;

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
        historian = utils.searchForService("historian", "historianAgent")[0];

        setMaximumLoss(startupArguments.get(0));
        setDesiredGain(startupArguments.get(1));
        setCurrentMoney(startupArguments.get(2));
        setTradingStatus(false);

        MarketOfAssets market = new MarketOfAssets();
        availableAssets = market.getAssetsOnMarket();

        setTradingTemplateAttributes();
        addBehaviour(new SubmitOrders(this, tradingTemplate));
    }

    @Override
    protected void takeDown() {
        if (success)
            System.out.println("StockTrader agent going down - success - earned enough money");
        else
            System.out.println("StockTrader agent going down - failure - lost too much money");
        utils.deregisterService();
        gui.dispose();
    }

    public AID getHistorian() {
        return historian;
    }

    public List<Asset> getAvailableAssets() {
        return availableAssets;
    }

    public void setCheckedPrice(BigDecimal checkedPrice, Asset assetChecked) {
        for (Asset a : availableAssets) {
            if (a.equals(assetChecked))
                a.setUnitValue(checkedPrice);
        }
    }

    public boolean checkSelfState() {
        if (currentMoney.compareTo(maximumLoss) == -1 || currentMoney.compareTo(desiredGain) == 1) {
            success = currentMoney.compareTo(maximumLoss) != -1;
            return true;
        }
        else
            return false;
    }

    public void setTradingStatus(boolean tradingStatus) {
        this.tradingStatus = tradingStatus;
        gui.setTradingStatus(tradingStatus);
    }

    public void addBoughtStock(Order result) {
        Asset boughtAsset = result.getAssetToTrade();
        gui.addBoughtAsset(boughtAsset);
        deduceMoney(boughtAsset);
    }

    public void removeSoldStock(Order result) {
        Asset soldAsset = result.getAssetToTrade();
        gui.removeSoldAsset(soldAsset);
        addMoney(soldAsset);
    }

    public void appendLogMessage(String logMessage) {
        gui.appendToLog(logMessage);
    }

    public Order checkProfitCreateOrder(List<TrendQuery> checkedTrends) {
        BigDecimal cheapestAssetOnMarket = findCheapestPrice(checkedTrends); //used to check if trader can afford cheapest asset
        assetsInInventory = gui.getAssets();
        BigDecimal lowerBound = calculatePercentage("0.1");
        BigDecimal sellingBound = calculateSpreadPercentage("0.1");
        if (lowerBound.compareTo(cheapestAssetOnMarket) == -1 && assetsInInventory.size() == 0) {
            return null; //not enough money and no assets - resign from trade
        }
        if (assetsInInventory.size() < 3 && currentMoney.subtract(maximumLoss).compareTo(sellingBound) >= 0) {
            return createBuyOrder(checkedTrends);
        }
        else if (assetsInInventory.size() < 3 && currentMoney.subtract(maximumLoss).compareTo(sellingBound) == -1) {
            return createSellOrder(checkedTrends);
        }
        else {
            BigDecimal buyingBound = calculateSpreadPercentage("0.3");
            if (currentMoney.compareTo(buyingBound) == 1) {
                return createBuyOrder(checkedTrends);
            }
            else {
                return createSellOrder(checkedTrends);
            }
        }
    }

    private Order createBuyOrder(List<TrendQuery> checkedTrends) {
        TrendQuery bestAssetToBuy = findHighestDerivative(checkedTrends);
        if (bestAssetToBuy == null)
            return null;
        BigDecimal latestPriceOfBestAsset = bestAssetToBuy.getCurrentPrice();
        BigDecimal lowerBound;
        if (Math.random() < 0.5)
            lowerBound = calculatePercentage("0.1");
        else
            lowerBound = calculatePercentage("0.2");
        int numberOfUnits = 0;
        while (latestPriceOfBestAsset.multiply(new BigDecimal(numberOfUnits)).compareTo(lowerBound) < 1) {
            numberOfUnits++;
        }
        if (numberOfUnits == 0)
            return createSellOrder(checkedTrends);
        return new Order(bestAssetToBuy.getAsset(), true, numberOfUnits);
    }

    private Order createSellOrder(List<TrendQuery> checkedTrends) {
        for (Asset a : assetsInInventory) {
            BigDecimal lossBoundary = calculateFlatPercentage("0.2", a.getUnitValue());
            TrendQuery specificTrend = findSpecificTrend(a, checkedTrends);
            BigDecimal currentPrice = specificTrend.getCurrentPrice();
            if (currentPrice.subtract(a.getUnitValue()).abs().compareTo(lossBoundary) == 1) {
                return new Order(a, false, a.getNumberOfUnits());
            }
        }
        Asset bestAssetToSell = findHighestDerivativeAmongInventory(checkedTrends);
        if (bestAssetToSell == null)
            return null;
        else
            return new Order(bestAssetToSell, false, bestAssetToSell.getNumberOfUnits());
    }

    private TrendQuery findSpecificTrend (Asset assetTrendToFind, List<TrendQuery> checkedTrends) {
        for (TrendQuery t: checkedTrends) {
            if (t.getAsset().equals(assetTrendToFind))
                return t;
        }
        return null;
    }

    private BigDecimal findCheapestPrice(List<TrendQuery> checkedTrends) {
        BigDecimal min = new BigDecimal("9999.99");
        for (TrendQuery t: checkedTrends) {
            BigDecimal currentPrice = t.getCurrentPrice();
            if (currentPrice.compareTo(min) == -1) {
                min = currentPrice;
            }
        }
        return min;
    }

    private TrendQuery findHighestDerivative(List<TrendQuery> checkedTrends) {
        BigDecimal highestDerivative = new BigDecimal(BigInteger.ZERO);
        TrendQuery trendWithHighestDerivative = null;
        for (TrendQuery t: checkedTrends) {
            List<BigDecimal> trend = t.getTrend();
            if (trend.size() < 2) { // not enough data from historian -> choose randomly (have to buy sth at beginning)
                Random r = new Random();
                Integer randomTrend = r.nextInt(6);
                trendWithHighestDerivative = checkedTrends.get(randomTrend);
                break;
            }
            BigDecimal derivative = t.getCurrentPrice().subtract(trend.get(0));
            if (derivative.compareTo(highestDerivative) == 1) {
                highestDerivative = derivative;
                trendWithHighestDerivative = t;
            }
        }
        return trendWithHighestDerivative;
    }

    private Asset findHighestDerivativeAmongInventory(List<TrendQuery> checkedTrends) {
        BigDecimal highestDerivative = new BigDecimal(BigInteger.ZERO);
        Asset assetWithHighestDerivative = null;
        for (Asset a : assetsInInventory) {
            TrendQuery specificTrend = findSpecificTrend(a, checkedTrends);
            List<BigDecimal> trend = specificTrend.getTrend();
            if (trend.size() < 2) { //not enough data from historian -> better not to sell anything
                return null;
            }
            BigDecimal derivative = specificTrend.getCurrentPrice().subtract(a.getUnitValue());
            if (derivative.compareTo(highestDerivative) == 1) {
                highestDerivative = derivative;
                assetWithHighestDerivative = specificTrend.getAsset();
            }
        }
        return assetWithHighestDerivative;
    }

    private BigDecimal calculateFlatPercentage(String percentage, BigDecimal value) {
        return value.multiply(new BigDecimal(percentage));
    }

    private BigDecimal calculatePercentage(String percentage) {
        return currentMoney.subtract(maximumLoss).abs().multiply(new BigDecimal(percentage));
    }

    private BigDecimal calculateSpreadPercentage(String percentage) {
        return desiredGain.subtract(maximumLoss).abs().multiply(new BigDecimal(percentage));
    }

    private void collectStartupArguments() {
        Object[] args = getArguments();
        for (Object x : args)
            startupArguments.add((String) x);
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

    private void addMoney(Asset soldAsset) {
        BigDecimal valueOfSoldAsset = soldAsset.getUnitValue();
        int units = soldAsset.getNumberOfUnits();
        BigDecimal addedMoney = valueOfSoldAsset.multiply(new BigDecimal(units));
        setCurrentMoney(getCurrentMoney().add(addedMoney).toString());
    }

    private BigDecimal getCurrentMoney() {
        return currentMoney;
    }
}
