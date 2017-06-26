package behaviours;

import agents.StockTrader;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import models.Asset;
import models.Order;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Przemek on 2017-06-26.
 */
public class TraderReasoning extends ParallelBehaviour {
    private boolean subOneFinished = false;
    private boolean subTwoFinished = false;
    private ContractNetResponder parentBehaviour;
    private StockTrader myAgentConcrete;
    private ACLMessage cfpMessage;
    private ACLMessage messageToHistorian;
    private ACLMessage priceCheckMessage;
    private final int NUMBER_OF_PRICE_CHECKS = 3;
    private List<List<Asset>> testAssetsList = new ArrayList<List<Asset>>();

    public TraderReasoning(Agent a, ContractNetResponder parentBehaviour) {
        super(a, WHEN_ALL); //end this master behaviour when ALL subbehaviours have completed at least once
        this.parentBehaviour = parentBehaviour;
        this.myAgentConcrete = (StockTrader) a;
    }

    @Override
    public void onStart() {
        subOneFinished = false;
        subTwoFinished = false;
//        testAssetsList.clear();
        initSubBehaviours();
        cfpMessage = (ACLMessage) this.getDataStore().get(parentBehaviour.CFP_KEY);
        myAgentConcrete.setTradingStatus(true);
        System.out.println("Starting...");
    }

    private void initSubBehaviours() {
        //Sender to historian
        addSubBehaviour(new SimpleBehaviour() {
            private boolean isDone = false;
            private int counterOfHistorianRequests = 0;
            @Override
            public void onStart() {
                isDone = false;
                counterOfHistorianRequests = 0;
            }

            @Override
            public void action() {
                if (messageToHistorian != null) {
                    myAgentConcrete.send(messageToHistorian);
                    messageToHistorian = null; //clear buffer message
                    counterOfHistorianRequests++;
                }
                if (counterOfHistorianRequests == NUMBER_OF_PRICE_CHECKS)
                    isDone = true;
            }

            @Override
            public boolean done() {
                if (isDone) {
                    subOneFinished = true;
                    System.out.println("Sub one finished!" + myAgentConcrete.getLocalName());
                }
                return isDone;
            }
        });
        //Receiver from historian
        addSubBehaviour(new SimpleBehaviour() {
            private boolean isDone = false;
            private int counterOfHistorianResponds = 0;

            @Override
            public void onStart() {
                isDone = false;
                counterOfHistorianResponds = 0;
            }

            @Override
            public void action() {
                MessageTemplate mt =
                        MessageTemplate.MatchConversationId("price-check" + myAgentConcrete.getLocalName());
                ACLMessage response = myAgentConcrete.receive(mt);
                if (response != null) {
                    System.out.println("Historian responded " + myAgentConcrete.getLocalName());
                    List<Asset> testAssets = null;
                    try {
                        testAssets = (List<Asset>) response.getContentObject();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    if (testAssets != null) {
                        testAssetsList.add(testAssets);
                        for (Asset a : testAssets)
                            myAgentConcrete.setCheckedPrice(a.getUnitValue(), a);
                    }
                    counterOfHistorianResponds++;
                }
                if (counterOfHistorianResponds == NUMBER_OF_PRICE_CHECKS) {
                    isDone = true;
                }
            }

            @Override
            public boolean done() {
                if (isDone) {
                    subTwoFinished = true;
                    System.out.println("Sub two finished!" + myAgentConcrete.getLocalName());
                }
                return isDone;
            }
        });
        //REASONER - here we will decide whether sell/buy etc..
        addSubBehaviour(new SimpleBehaviour() {
            @Override
            public void action() {
                setPriceCheckMessageAttributes();
                List<Asset> assetsToCheckPrice = myAgentConcrete.getAssetPrices();
                try {
                    priceCheckMessage.setContentObject((Serializable) assetsToCheckPrice);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                messageToHistorian = priceCheckMessage;
            }

            @Override
            public boolean done() {
                System.out.println(subOneFinished + " " + subTwoFinished + " " + myAgentConcrete.getLocalName());
                return subTwoFinished && subOneFinished;
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
