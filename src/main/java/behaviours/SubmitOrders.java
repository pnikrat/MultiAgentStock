package behaviours;

import agents.StockTrader;
import jade.core.AID;
import jade.core.Agent;
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
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
        savedCfp = cfp;
        String title = cfp.getContent();
        CfpSender = cfp.getSender();
        if (title.equals("TradingOpen"))
            myAgentConcrete.setTradingStatus(true);

        tradedAsset = new Asset("BZW");
//        priceCheckMessage = setPriceCheckMessageAttributes(tradedAsset);
//        myAgent.addBehaviour(new OneShotBehaviour() {
//            @Override
//            public void action() {
//                myAgent.addBehaviour(new PriceCheck(myAgentConcrete, priceCheckMessage, parentBehaviour));
//                System.out.println("One shot done");
//            }
//        });
        //buying
//        boolean isBuy = true;
//        Order order = new Order(stock, isBuy, 10);
        //selling
        boolean isBuy = false;
        Order order = new Order(tradedAsset, isBuy, 10);
        ACLMessage reply = cfp.createReply();
        reply.setPerformative(ACLMessage.PROPOSE);
        try {
            reply.setContentObject(order);
        } catch (IOException e) {}
        return reply;
    }

//    public ACLMessage myHandleCfp(ACLMessage cfp) {
//        Asset stock = tradedAsset;
//        System.out.println(myAgentConcrete.getCheckedPrice());
//        //buying
////        boolean isBuy = true;
////        Order order = new Order(stock, isBuy, 10);
//        //selling
//        boolean isBuy = false;
//        Order order = new Order(stock, isBuy, 10);
//        ACLMessage reply = cfp.createReply();
//        reply.setPerformative(ACLMessage.PROPOSE);
//        try {
//            reply.setContentObject(order);
//        } catch (IOException e) {}
//        return reply;
//    }

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
            ACLMessage reply = accept.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            return reply;
        }
        throw new FailureException("WrongObject");
    }

//    private ACLMessage setPriceCheckMessageAttributes(Asset assetToCheckPrice) {
//        ACLMessage priceCheckMessage = new ACLMessage(ACLMessage.REQUEST);
//        priceCheckMessage.addReceiver(myAgentConcrete.getHistorian());
//        priceCheckMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
//        try {
//            priceCheckMessage.setContentObject(assetToCheckPrice);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return priceCheckMessage;
//    }
}
