package agents;

import jade.core.AID;
import jade.core.Agent;
import utils.DfAgentUtils;

/**
 * Created by Przemek on 2017-06-20.
 */
public class Historian extends Agent {
    private DfAgentUtils utils;

    @Override
    protected void setup() {
        System.out.println("Historian agent " + getAID().getName() + " is ready");

        utils = new DfAgentUtils(this);
        utils.registerService("historian", "historianAgent");
    }

    @Override
    protected void takeDown() {
        System.out.println("Historian agent going down");
        utils.deregisterService();
    }
}
