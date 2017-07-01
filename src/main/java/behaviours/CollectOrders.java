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
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Created by Przemek on 2017-06-24.
 */
public class CollectOrders extends ContractNetInitiator {

    private SessionManager myAgentConcrete;
    private Order singleOrderTradeResult;
    private ACLMessage archiveStockDataMessage;

    public CollectOrders(Agent a, ACLMessage cfp) {
        super(a, cfp);
        myAgentConcrete = (SessionManager) a;
    }

    @Override
    protected void handlePropose(ACLMessage propose, Vector acceptances) {
        Order orderFromProposal = null;
        try {
            orderFromProposal = (Order) propose.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        if (orderFromProposal != null) {
            boolean isBuy = orderFromProposal.isBuy();
            boolean tradeSuccessful = true;
            if (isBuy) {
                tradeSuccessful = handleBuyOrder(orderFromProposal);
            }
            else {
                handleSellOrder(orderFromProposal);
            }
            ACLMessage reply = propose.createReply();
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
                //TODO:set new prices here. For now lower by 5 each time
                setNewPrices();
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

    private void setNewPrices() {
        for (int i = 0 ; i < 6 ; i++) {
            Random r = new Random();
            Integer priceChange = r.nextInt(8) - 4;
            myAgentConcrete.changeAssetPrice(myAgentConcrete.getAssets().get(i), new BigDecimal(priceChange));
        }
    }
}
