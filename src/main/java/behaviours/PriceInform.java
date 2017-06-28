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
import models.TrendQuery;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
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
        List<TrendQuery> trendsToCheck = null;
        try {
            trendsToCheck = (List<TrendQuery>) request.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        ACLMessage reply = request.createReply();
        if (trendsToCheck != null) {
            reply.setPerformative(ACLMessage.INFORM);
            List<TrendQuery> queryResult = new ArrayList<TrendQuery>();
            for (TrendQuery t : trendsToCheck) {
                TrendQuery singleQueryResult = myAgentConcrete.getTrend(t);
                queryResult.add(singleQueryResult);
            }
            try {
                reply.setContentObject((Serializable) queryResult);
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
