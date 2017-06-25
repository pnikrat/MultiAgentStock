package behaviours;

import agents.StockTrader;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import models.Asset;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Przemek on 2017-06-25.
 */
public class SchedulePriceCheck extends TickerBehaviour {
    private StockTrader myAgentConcrete;
    private ACLMessage priceCheckMessage;

    public SchedulePriceCheck(Agent a, long period) {
        super(a, period);
        myAgentConcrete = (StockTrader) a;
    }

    protected void onTick() {
        setPriceCheckMessageAttributes();
        List<Asset> assetToCheck = myAgentConcrete.getAssetPrices();
        try {
            priceCheckMessage.setContentObject((Serializable) assetToCheck);
        } catch (IOException e) {
            e.printStackTrace();
        }
        myAgentConcrete.addBehaviour(new PriceCheckBehaviour(myAgentConcrete, priceCheckMessage));
    }

    private void setPriceCheckMessageAttributes() {
        priceCheckMessage = new ACLMessage(ACLMessage.REQUEST);
        priceCheckMessage.addReceiver(myAgentConcrete.getHistorian());
        priceCheckMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
    }
}
