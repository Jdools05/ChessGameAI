package xyz.jdools05.ChessGameAI;

import java.util.ArrayList;

public class Population {
    // store all the agents in the population
    private Agent[] agents;
    private Agent bestAgent;
    // best score of the best ever agent
    private double bestScore;
    int generation;
    // list of the connection history
    private ArrayList<ConnectionHistory> connectionHistory;
    private ArrayList<Agent> genAgents = new ArrayList<Agent>();
    private List<Species> species = new ArrayList<Species>();

    // constructor
    public Population(int size) {
        agents = new Agent[size];
        connectionHistory = new ArrayList<ConnectionHistory>();
        // initialize the agents
        for (int i = 0; i < size; i++) {
            agents[i] = new Agent();
            agents[i].brain.generateNetwork();
            agents[i].brain.mutate(innovationHistory);
        }
    }

    public void updateAllive() {
        for (Agent agent : agents) {
            if (!agent.isDead) {
                agent.look();
                agent.think();
                agent.update();
            }
        }
    }

    public boolean isFinished() {
        for (Agent agent : agents) {
            if (!agent.isDead) {
                return false;
            }
        }
        return true;
    }

    //TODO: finish this class
}
