package behaviours;

import agents.StockTrader;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import models.Asset;
import models.Order;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by Przemek on 2017-06-24.
 */
public class SubmitOrdersBehaviour extends ContractNetResponder {

    private StockTrader myAgentConcrete;

    public SubmitOrdersBehaviour(Agent a, MessageTemplate mt) {
        super(a, mt);
        myAgentConcrete = (StockTrader) a;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
        String title = cfp.getContent();
        if (title.equals("TradingOpen"))
            myAgentConcrete.setTradingStatus(true);
        boolean isBuy = true;
        Asset stock = new Asset("PKN");
        Order order = new Order(stock, isBuy, 10);
        ACLMessage reply = cfp.createReply();
        reply.setPerformative(ACLMessage.PROPOSE);
        try {
            reply.setContentObject(order);
        } catch (IOException e) {}
        return reply;
    }
}
