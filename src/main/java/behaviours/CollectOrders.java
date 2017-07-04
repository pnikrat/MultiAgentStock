package behaviours;

import agents.SessionManager;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;
import models.Asset;
import models.Order;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Przemek on 2017-06-24.
 */
public class CollectOrders extends ContractNetInitiator {

    private SessionManager myAgentConcrete;
    private Order singleOrderTradeResult;
    private ACLMessage archiveStockDataMessage;
    private List<Order> ordersFromTraders;
    private List<ACLMessage> messagesFromTraders;
    private Map<Asset, Integer> ordersCounted;

    public CollectOrders(Agent a, ACLMessage cfp) {
        super(a, cfp);
        myAgentConcrete = (SessionManager) a;
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        collectNewOrders(responses);
        countOrders();
        setNewPrices();
        int index = 0;
        for (Order o : ordersFromTraders) {
            boolean isBuy = o.isBuy();
            boolean tradeSuccessful = true;
            if (isBuy)
                tradeSuccessful = handleBuyOrder(o);
            else
                handleSellOrder(o);
            ACLMessage reply = messagesFromTraders.get(index).createReply();
            if (tradeSuccessful) {
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                try {
                    reply.setContentObject(singleOrderTradeResult);
                } catch (IOException e){}
            }
            else {
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            }
            acceptances.add(reply);
            index++;
        }
    }

    private boolean handleBuyOrder(Order buyOrder) {
        Asset tradedAsset = buyOrder.getAssetToTrade();
        int orderedUnits = buyOrder.getUnitsToTrade();
        int soldUnits = myAgentConcrete.sellStock(tradedAsset, orderedUnits);
        tradedAsset.setUnitValue(myAgentConcrete.getCurrentAssetValue(tradedAsset));
        if (soldUnits != 0 && tradedAsset.getUnitValue() != null) {
            tradedAsset.setNumberOfUnits(soldUnits);
            singleOrderTradeResult = new Order(tradedAsset, buyOrder.isBuy(), soldUnits);
            return true;
        }
        return false;
    }

    private void handleSellOrder(Order sellOrder) {
        Asset tradedAsset = sellOrder.getAssetToTrade();
        tradedAsset.setNumberOfUnits(sellOrder.getUnitsToTrade());
        myAgentConcrete.buyStock(tradedAsset);
        tradedAsset.setUnitValue(myAgentConcrete.getCurrentAssetValue(tradedAsset));
        if (tradedAsset.getUnitValue() != null) {
            singleOrderTradeResult = new Order(tradedAsset, sellOrder.isBuy(), tradedAsset.getNumberOfUnits());
        }
    }

    @Override
    public int onEnd() {
        System.out.println("TRADING POINT FINISHED");
        myAgentConcrete.setTradingStatus(false);
        myAgentConcrete.addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                setArchiveStockDataMessageAttributes();
                List<Asset> newPricesToArchive = myAgentConcrete.getAssets();
                try {
                    archiveStockDataMessage.setContentObject((Serializable) newPricesToArchive);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                myAgentConcrete.addBehaviour(new AchieveREInitiator(myAgentConcrete, archiveStockDataMessage));
            }
        });
        return super.onEnd();
    }

    private void setArchiveStockDataMessageAttributes() {
        archiveStockDataMessage = new ACLMessage(ACLMessage.REQUEST);
        archiveStockDataMessage.addReceiver(myAgentConcrete.getHistorian());
        archiveStockDataMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        archiveStockDataMessage.setConversationId("archive-prices");
    }

    private void collectNewOrders(Vector responses) {
        ordersFromTraders = new ArrayList<Order>();
        messagesFromTraders = new ArrayList<ACLMessage>();
        for (Object response : responses) {
            ACLMessage propose = (ACLMessage) response;
            if (propose.getPerformative() == ACLMessage.PROPOSE) {
                Order orderFromProposal = null;
                try {
                    orderFromProposal = (Order) propose.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                if (orderFromProposal != null) {
                    ordersFromTraders.add(orderFromProposal);
                    messagesFromTraders.add(propose);
                }
            }
        }
    }

    private void countOrders() {
        ordersCounted = new HashMap<Asset, Integer>();
        initOrdersCountedMap();
        for (Order o : ordersFromTraders) {
            Integer orderModifier = o.isBuy() ? 1 : -1;
            ordersCounted.put(o.getAssetToTrade(), ordersCounted.get(o.getAssetToTrade()) + orderModifier);
        }
    }

    private void initOrdersCountedMap() {
        for (Asset a : myAgentConcrete.getAssets()) {
            ordersCounted.put(a, 0);
        }
    }

    private void setNewPrices() {
        for (Asset a : myAgentConcrete.getAssets()) {
            Random r = new Random();
            double minChange = -2.0f;
            double maxChange = 2.0f;
            double modifier = ordersCounted.get(a) * 0.05f;
            minChange += modifier;
            maxChange += modifier;
            double priceChange = r.nextFloat() * (maxChange - minChange) + minChange;
            myAgentConcrete.changeAssetPrice(a, new BigDecimal(priceChange)
                    .setScale(2, BigDecimal.ROUND_HALF_UP));
        }
    }
}
