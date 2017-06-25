package behaviours;

import agents.StockTrader;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import models.Asset;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Przemek on 2017-06-25.
 */
public class PriceCheckBehaviour extends AchieveREInitiator {
    private StockTrader myAgentConcrete;
    //private SubmitOrdersBehaviour parentBehaviour;

    public PriceCheckBehaviour(Agent a, ACLMessage msg) {
        super(a, msg);
        myAgentConcrete = (StockTrader) a;
        //this.parentBehaviour = parentBehaviour;
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        System.out.println("Received Inform");
        List<Asset> currentPrices = null;
        try {
            currentPrices = (List<Asset>) inform.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        for (Asset a : currentPrices) {
            myAgentConcrete.setCheckedPrice(a.getUnitValue(), a);
        }
    }
}
