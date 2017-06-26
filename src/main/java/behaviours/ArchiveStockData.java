package behaviours;

import agents.Historian;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;
import models.Asset;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Przemek on 2017-06-26.
 */
public class ArchiveStockData extends AchieveREResponder {
    private Historian myAgentConcrete;

    public ArchiveStockData(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.myAgentConcrete = (Historian) a;
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
        List<Asset> newPricesToArchive = new ArrayList<Asset>();
        try {
            newPricesToArchive = (List<Asset>) request.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        myAgentConcrete.archiveData(newPricesToArchive);
        ACLMessage reply = request.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        return reply;
    }
}
