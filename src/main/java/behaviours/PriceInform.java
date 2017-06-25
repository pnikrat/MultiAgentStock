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
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Przemek on 2017-06-25.
 */
public class PriceInform extends AchieveREResponder {
    private Historian myAgentConcrete;

    public PriceInform(Agent a, MessageTemplate mt) {
        super(a, mt);
        myAgentConcrete = (Historian) a;
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
        List<Asset> assetsToPriceCheck = null;
        try {
            assetsToPriceCheck = (List<Asset>) request.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        ACLMessage reply = request.createReply();
        if (assetsToPriceCheck != null) {
            reply.setPerformative(ACLMessage.INFORM);
            for (Asset a : assetsToPriceCheck) {
                a.setUnitValue(myAgentConcrete.getAssetCurrentPrice(a));
            }
            try {
                reply.setContentObject((Serializable) assetsToPriceCheck);
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
