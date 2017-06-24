package behaviours;

import agents.SessionManager;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

/**
 * Created by Przemek on 2017-06-24.
 */
public class TradingPeriodBehaviour extends TickerBehaviour {

    private SessionManager myAgentConcrete;
    private ACLMessage cfp;

    public TradingPeriodBehaviour(Agent a, int tickTime) {
        super(a, tickTime);
        myAgentConcrete = (SessionManager) a;
    }

    protected void onTick() {
        if (!myAgentConcrete.getTradingStatus()) {
            myAgentConcrete.setTradingStatus(true);
            System.out.println("Trading point started");
            createCallForProposals("stock-trading", "TradingOpen");
            addCfpReceivers();
            myAgent.addBehaviour(new CollectOrdersBehaviour(myAgent, cfp));
        }
    }

    private void createCallForProposals(String conversationId, String content) {
        cfp = new ACLMessage(ACLMessage.CFP);
        cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        cfp.setConversationId(conversationId);
        cfp.setContent(content);
    }

    private void addCfpReceivers() {
        myAgentConcrete.scanForStockTraders();
        for (AID receiver : myAgentConcrete.getStockTraders()) {
            cfp.addReceiver(receiver);
        }
    }
}
