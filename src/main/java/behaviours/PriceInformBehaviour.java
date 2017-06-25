package behaviours;

import agents.Historian;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;
import models.Asset;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by Przemek on 2017-06-25.
 */
public class PriceInformBehaviour extends AchieveREResponder {
    private Historian myAgentConcrete;

    public PriceInformBehaviour(Agent a, MessageTemplate mt) {
        super(a, mt);
        myAgentConcrete = (Historian) a;
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
        Asset assetToPriceCheck = null;
        try {
            assetToPriceCheck = (Asset) request.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        ACLMessage reply = request.createReply();
        if (assetToPriceCheck != null) {
            reply.setPerformative(ACLMessage.INFORM);
            BigDecimal currentPrice = myAgentConcrete.getAssetCurrentPrice(assetToPriceCheck);
            try {
                reply.setContentObject(currentPrice);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return reply;
        }
        else {
            throw new NotUnderstoodException("Could not serialize Asset from StockTrader request");
        }
    }
}
