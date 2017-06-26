package behaviours;

import agents.StockTrader;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import models.Asset;
import models.Order;

import java.io.IOException;

/**
 * Created by Przemek on 2017-06-24.
 */
public class SubmitOrders extends ContractNetResponder {

    private StockTrader myAgentConcrete;
    private ACLMessage priceCheckMessage;
    private ACLMessage savedCfp;
    private Asset tradedAsset;
    private AID CfpSender;
    private SubmitOrders parentBehaviour = this;

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
            myAgentConcrete.setTradingStatus(false);
            ACLMessage reply = accept.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            return reply;
        }
        throw new FailureException("WrongObject");
    }
}
