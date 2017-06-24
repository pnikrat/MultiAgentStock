package behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

/**
 * Created by Przemek on 2017-06-24.
 */
public class CollectOrdersBehaviour extends ContractNetInitiator {
    public CollectOrdersBehaviour(Agent a, ACLMessage cfp) {
        super(a, cfp);
    }

}
