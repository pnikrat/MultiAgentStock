package behaviours;

import agents.SessionManager;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import models.Asset;
import models.Order;
import models.TradingResult;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by Przemek on 2017-06-24.
 */
public class CollectOrdersBehaviour extends ContractNetInitiator {

    private SessionManager myAgentConcrete;
    private TradingResult singleOrderTradeResult;

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
            Asset tradedAsset = orderFromProposal.getAssetToTrade();
            int orderedUnits = orderFromProposal.getUnitsToTrade();
            boolean isBuy = orderFromProposal.isBuy();
            boolean tradeSuccessful = false;
            if (isBuy) {
                tradeSuccessful = handleBuyOrder(tradedAsset, orderedUnits);
            }
            else {
                handleSellOrder(tradedAsset, orderedUnits);
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

    private boolean handleBuyOrder(Asset tradedAsset, int orderedUnits) {
        int soldUnits = myAgentConcrete.sellStock(tradedAsset, orderedUnits);
        tradedAsset.setUnitValue(myAgentConcrete.getCurrentAssetValue(tradedAsset));
        if (soldUnits != 0 && tradedAsset.getUnitValue() != null) {
            tradedAsset.setNumberOfUnits(soldUnits);
            singleOrderTradeResult = new TradingResult(tradedAsset, soldUnits);
            return true;
        }
        return false;
    }

    private void handleSellOrder(Asset tradedAsset, int orderedUnits) {

    }
}
