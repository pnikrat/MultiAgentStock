package agents;


import gui.SessionManagerGui;
import jade.core.AID;
import jade.core.Agent;
import utils.DfAgentUtils;

/**
 * Created by Przemek on 2017-06-20.
 */
public class SessionManager extends Agent {
    private DfAgentUtils utils;
    private AID[] stockTraders;
    private AID historian;
    private SessionManagerGui gui;

    @Override
    protected void setup() {
        System.out.println("Session manager agent " + getAID().getName() + " is ready");

        utils = new DfAgentUtils(this);
        utils.registerService("sessionManager", "sessionAgent");
        stockTraders = utils.searchForService("trader", "tradingAgent");
        historian = utils.searchForService("historian", "historianAgent")[0];

        gui = new SessionManagerGui();
        gui.showGui();
    }

    @Override
    protected void takeDown() {
        System.out.println("Session manager agent going down");
        utils.deregisterService();
    }
}
