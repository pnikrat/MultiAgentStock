package agents;

import jade.core.Agent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Przemek on 2017-06-20.
 */
public class StockTrader extends Agent {

    private List<String> startupArguments = new ArrayList<String>();

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
    }

    protected void takeDown() {
        System.out.println("Stock trader agent going down");
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
