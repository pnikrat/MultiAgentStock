package utils;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

/**
 * Created by Przemek on 2017-06-20.
 */
public class DfAgentUtils {
    private Agent agent;

    public DfAgentUtils(Agent agent) {
        this.agent = agent;
    }

    public void registerService(String serviceType, String serviceName) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        sd.setName(serviceName);
        dfd.addServices(sd);
        try {
            DFService.register(agent, dfd);
        }
        catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    public void deregisterService() {
        try {
            DFService.deregister(agent);
        }
        catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    public AID[] searchForService(String serviceType, String serviceName) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName(serviceName);
        sd.setType(serviceType);
        template.addServices(sd);
        AID[] searchedAgents = new AID[0];
        try {
            DFAgentDescription[] result = DFService.search(agent, template);
            searchedAgents = new AID[result.length];
            for (int i = 0; i < result.length; i++) {
                searchedAgents[i] = result[i].getName();
            }
        }
        catch (FIPAException e) {
            e.printStackTrace();
        }
        return searchedAgents;
    }


}
