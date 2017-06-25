package behaviours;

import agents.StockTrader;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;

import java.math.BigDecimal;

/**
 * Created by Przemek on 2017-06-25.
 */
public class PriceCheckBehaviour extends AchieveREInitiator {
    private StockTrader myAgentConcrete;
    private SubmitOrdersBehaviour parentBehaviour;

    public PriceCheckBehaviour(Agent a, ACLMessage msg, SubmitOrdersBehaviour parentBehaviour) {
        super(a, msg);
        myAgentConcrete = (StockTrader) a;
        this.parentBehaviour = parentBehaviour;
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        System.out.println("Received Inform");
        BigDecimal currentPrice = null;
        try {
            currentPrice = (BigDecimal) inform.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        myAgentConcrete.setCheckedPrice(currentPrice);
    }
}
