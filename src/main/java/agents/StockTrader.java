package agents;

import jade.core.Agent;
import jade.core.AID;
import utils.DfAgentUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Przemek on 2017-06-20.
 */
public class StockTrader extends Agent {

    private List<String> startupArguments = new ArrayList<String>();
    private DfAgentUtils utils;
    private AID sessionManager;
    private AID historian;

    @Override
    protected void setup() {
        System.out.println("Stock trader agent " + getAID().getName() + " is ready");
        try {
            collectStartupArguments();
        }
        catch (NullPointerException e) {
            System.out.println("No trading goals specified, terminating...");
            doDelete();
        }
        printStartupArguments();
        utils = new DfAgentUtils(this);
        utils.registerService("trader", "tradingAgent");
        sessionManager = utils.searchForService("sessionManager", "sessionAgent")[0];
        historian = utils.searchForService("historian", "historianAgent")[0];
    }

    @Override
    protected void takeDown() {
        System.out.println("Stock trader agent going down");
        utils.deregisterService();
    }

    private void collectStartupArguments() {
        Object[] args = getArguments();
        for (Object x : args)
            startupArguments.add((String) x);
    }

    private boolean checkIfArgumentsEmpty() {
        return startupArguments.size() == 0;
    }

    private void printStartupArguments() {
        for (String x : startupArguments)
            System.out.println(x + " ");
    }
}
