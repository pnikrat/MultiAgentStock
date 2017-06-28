package behaviours;

import agents.StockTrader;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import models.Asset;
import models.Order;
import models.TrendQuery;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Przemek on 2017-06-26.
 */
public class TraderReasoning extends ParallelBehaviour {
    private boolean senderFinished = false;
    private boolean receiverFinished = false;
    private ContractNetResponder parentBehaviour;
    private StockTrader myAgentConcrete;
    private ACLMessage cfpMessage;
    private ACLMessage messageToHistorian;
    private ACLMessage priceCheckMessage;
    private final int NUMBER_OF_TREND_CHECKS = 1;
    private List<TrendQuery> checkedTrends = new ArrayList<TrendQuery>();

    public TraderReasoning(Agent a, ContractNetResponder parentBehaviour) {
        super(a, WHEN_ALL); //end this master behaviour when ALL subbehaviours have completed at least once
        this.parentBehaviour = parentBehaviour;
        this.myAgentConcrete = (StockTrader) a;
        initSubBehaviours();
    }

    @Override
    public void onStart() {
        senderFinished = false;
        receiverFinished = false;
        checkedTrends.clear();
        cfpMessage = (ACLMessage) this.getDataStore().get(parentBehaviour.CFP_KEY);
        myAgentConcrete.setTradingStatus(true);
        System.out.println("Starting...");
    }

    private void initSubBehaviours() {
        //Sender to historian
        addSubBehaviour(new SimpleBehaviour() {
            private int counterOfHistorianRequests;
            @Override
            public void onStart() {
                counterOfHistorianRequests = 0;
            }

            @Override
            public void action() {
                setPriceCheckMessageAttributes();
                List<Asset> assetsToCheck = myAgentConcrete.getAvailableAssets();
                List<TrendQuery> historianQuery = new ArrayList<TrendQuery>();
                for (Asset a : assetsToCheck) {
                    Random r = new Random();
                    Integer timePeriod = r.nextInt(20 - 5) + 5;
                    TrendQuery t = new TrendQuery(a, timePeriod);
                    historianQuery.add(t);
                }
                try {
                    priceCheckMessage.setContentObject((Serializable) historianQuery);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (messageToHistorian == null) {
                    messageToHistorian = priceCheckMessage;
                    myAgentConcrete.send(messageToHistorian);
                    messageToHistorian = null; //clear buffer message
                    priceCheckMessage = null;
                    ++counterOfHistorianRequests;
                }
            }

            @Override
            public boolean done() {
                if (counterOfHistorianRequests >= NUMBER_OF_TREND_CHECKS) {
                    senderFinished = true;
                    System.out.println("Sender finished!" + myAgentConcrete.getLocalName());
                    return true;
                }
                else
                    return false;
            }
        });
        //Receiver from historian
        addSubBehaviour(new SimpleBehaviour() {
            private int counterOfHistorianResponds;
            @Override
            public void onStart() {
                counterOfHistorianResponds = 0;
            }

            @Override
            public void action() {
                MessageTemplate mt =
                        MessageTemplate.MatchConversationId("price-check" + myAgentConcrete.getLocalName());
                ACLMessage response = myAgentConcrete.receive(mt);
                if (response != null) {
                    System.out.println("Historian responded " + myAgentConcrete.getLocalName());
                    List<TrendQuery> receivedTrends = null;
                    try {
                        receivedTrends = (List<TrendQuery>) response.getContentObject();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    if (receivedTrends != null) {
                        checkedTrends.addAll(receivedTrends);
                        counterOfHistorianResponds++;
                    }
                }
            }

            @Override
            public boolean done() {
                if (counterOfHistorianResponds >= NUMBER_OF_TREND_CHECKS) {
                    receiverFinished = true;
                    System.out.println("Receiver finished!" + myAgentConcrete.getLocalName());
                    System.out.println("Trends list length: " + checkedTrends.size());
                    return true;
                }
                else
                    return false;
            }
        });
        //REASONER - here we will decide whether sell/buy etc..
        addSubBehaviour(new SimpleBehaviour() {
            private boolean isDone = false;
            @Override
            public void action() {
                //do sth with trendQueries
                if (receiverFinished && senderFinished)
                    isDone = true;
            }

            @Override
            public boolean done() {
                //System.out.println(senderFinished + " " + receiverFinished + " " + myAgentConcrete.getLocalName());
                return receiverFinished && senderFinished && isDone;
            }
        });
    }

    @Override
    public int onEnd() {
        Asset tradedAsset = new Asset("BZW");
        Order order = new Order(tradedAsset, false, 10);
        ACLMessage reply = cfpMessage.createReply();
        reply.setPerformative(ACLMessage.PROPOSE);
        try {
            reply.setContentObject(order);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.getDataStore().put(parentBehaviour.REPLY_KEY, reply);
        System.out.println("Parallel behaviour finished.");
        return super.onEnd();
    }

    private void setPriceCheckMessageAttributes() {
        priceCheckMessage = new ACLMessage(ACLMessage.REQUEST);
        priceCheckMessage.addReceiver(myAgentConcrete.getHistorian());
        priceCheckMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        priceCheckMessage.setConversationId("price-check" + myAgentConcrete.getLocalName());
    }
}

