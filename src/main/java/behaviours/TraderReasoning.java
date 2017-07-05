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
    private Order orderToSend;
    private boolean traderConditionsFulfilled;

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
        orderToSend = null;
        checkedTrends.clear();
        cfpMessage = (ACLMessage) this.getDataStore().get(parentBehaviour.CFP_KEY);
        traderConditionsFulfilled = myAgentConcrete.checkSelfState();
        if (!traderConditionsFulfilled) {
            myAgentConcrete.setTradingStatus(true);
        }
        else {
            myAgentConcrete.doDelete();
        }
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
                if (!traderConditionsFulfilled) {
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
            }

            @Override
            public boolean done() {
                if (counterOfHistorianRequests >= NUMBER_OF_TREND_CHECKS) {
                    senderFinished = true;
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
                if (!traderConditionsFulfilled) {
                    MessageTemplate mt =
                            MessageTemplate.MatchConversationId("price-check" + myAgentConcrete.getLocalName());
                    ACLMessage response = myAgentConcrete.receive(mt);
                    if (response != null) {
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
            }

            @Override
            public boolean done() {
                if (counterOfHistorianResponds >= NUMBER_OF_TREND_CHECKS) {
                    receiverFinished = true;
                    return true;
                }
                else
                    return false;
            }
        });
        //REASONER - here we will decide whether sell/buy etc..
        addSubBehaviour(new SimpleBehaviour() {
            private boolean isDone;

            @Override
            public void onStart() {
                isDone = false;
            }

            @Override
            public void action() {
                //evaluate profitability in agent class to keep behaviour code clean
                if (receiverFinished && senderFinished && !isDone && !traderConditionsFulfilled) {
                    orderToSend = myAgentConcrete.checkProfitCreateOrder(checkedTrends);
                    isDone = true;
                }
            }

            @Override
            public boolean done() {
                return receiverFinished && senderFinished && isDone;
            }
        });
    }

    @Override
    public int onEnd() {
        prepareReply();
        return super.onEnd();
    }

    private void prepareReply() {
        // if order to send is null agent resigns from trade
        ACLMessage reply = cfpMessage.createReply();
        if (orderToSend != null) {
            reply.setPerformative(ACLMessage.PROPOSE);
            try {
                reply.setContentObject(orderToSend);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            reply.setPerformative(ACLMessage.REFUSE);
            myAgentConcrete.appendLogMessage("Resigned from trade");
            myAgentConcrete.setTradingStatus(false);
        }
        this.getDataStore().put(parentBehaviour.REPLY_KEY, reply);
    }

    private void setPriceCheckMessageAttributes() {
        priceCheckMessage = new ACLMessage(ACLMessage.REQUEST);
        priceCheckMessage.addReceiver(myAgentConcrete.getHistorian());
        priceCheckMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        priceCheckMessage.setConversationId("price-check" + myAgentConcrete.getLocalName());
    }
}

