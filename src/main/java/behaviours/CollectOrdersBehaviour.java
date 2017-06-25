package behaviours;

import agents.SessionManager;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import models.Asset;
import models.Order;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by Przemek on 2017-06-24.
 */
public class CollectOrdersBehaviour extends ContractNetInitiator {

    private SessionManager myAgentConcrete;
    private Order singleOrderTradeResult;

    public CollectOrdersBehaviour(Agent a, ACLMessage cfp) {
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
        return super.onEnd();
    }
}
