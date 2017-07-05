package behaviours;

import agents.StockTrader;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import models.Asset;
import models.Order;

/**
 * Created by Przemek on 2017-06-24.
 */
public class SubmitOrders extends ContractNetResponder {

    private StockTrader myAgentConcrete;

    public SubmitOrders(Agent a, MessageTemplate mt) {
        super(a, mt);
        myAgentConcrete = (StockTrader) a;
        registerHandleCfp(new TraderReasoning(myAgentConcrete, this));
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        Order result = null;
        try {
            result = (Order) accept.getContentObject();
        }
        catch (UnreadableException e) {
            e.printStackTrace();
        }
        if (result != null) {
            boolean isBuy = result.isBuy();
            if (isBuy)
                myAgentConcrete.addBoughtStock(result);
            else
                myAgentConcrete.removeSoldStock(result);
            myAgentConcrete.appendLogMessage(result.getLogMessage());
            myAgentConcrete.setTradingStatus(false);
            ACLMessage reply = accept.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            return reply;
        }
        throw new FailureException("WrongObject");
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        Order failedPropose = null;
        try {
            failedPropose = (Order) propose.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        if (failedPropose != null) {
            if (failedPropose.isBuy())
                myAgentConcrete.appendLogMessage("Tried to buy " + failedPropose.getUnitsToTrade() + " units of "
                    + failedPropose.getAssetToTrade().getShortName() + " stock. Order failed");
        }
        else {
            myAgentConcrete.appendLogMessage("Order failed");
        }
        myAgentConcrete.setTradingStatus(false);
    }
}
