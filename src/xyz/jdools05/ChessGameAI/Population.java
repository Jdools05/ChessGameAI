package xyz.jdools05.ChessGameAI;

import xyz.jdools05.chess.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class Population {
    // store all the agents in the population
    private List<Agent> agents = new ArrayList<>();
    private List<Game> games = new ArrayList<>();
    private Agent bestAgent;
    // best score of the best ever agent
    private double bestScore;
    int generation;
    // list of the connection history
    private List<ConnectionHistory> innovationHistory = new ArrayList<>();
    private List<Agent> genAgents = new ArrayList<>();
    private List<Species> species = new ArrayList<>();

    // constructor
    public Population(int size) {
        // initialize the agents
        // make sure the population is always even
        if (size % 2 != 0) size++;
        for (int i = 0; i < size; i++) {
            agents.add(new Agent());
            agents.get(i).brain.generateNetwork();
            agents.get(i).brain.mutate(innovationHistory);
        }
        // initialize the games
        for (int i = 0; i < size / 2; i++) {
            games.add(new Game());
        }
    }

    public void updateAlive() {
        for (Agent agent : agents) {
            if (!agent.dead) {
                games.set(agent.game, agent.look(getGame(agent.game)));
                agent.think();
                games.set(agent.game, agent.move(getGame(agent.game)));
            }
        }
    }

    public boolean isFinished() {
        for (Agent agent : agents) {
            if (!agent.dead) {
                return false;
            }
        }
        return true;
    }

    public void setBestAgent() {
        Agent best = species.get(0).agents.get(0);
        best.gen = generation;

        if (best.score > bestScore) {
            genAgents.add(best);
            bestScore = best.score;
            bestAgent = best;
        }
        Logger.getGlobal().info("Generation: " + generation + " Best score: " + bestScore);
    }

    // assigns game id to each agent
    public void assignIds() {
        for (int i = 0; i < agents.size(); i += 2) {
            agents.get(i).game = i;
            agents.get(i + 1).game = i;
            agents.get(i+1).isWhite = false;
        }
    }

    public Game getGame(int id) {
        return games.get(id);
    }

    public void naturalSelection() {
        speciate();
        calculateFitness();
        sortSpecies();
        cullSpecies();
        setBestAgent();
        killStaleSpecies();
        killBadSpecies();

        double averageSum = getAvgFitnessSum();
        List<Agent> children = new ArrayList<>();
        for (Species species : this.species) {
            children.add(species.champion);
            int numChildren = (int) (Math.floor(species.averageFitness / averageSum * agents.size()) - 1);
            for (int i = 0; i < numChildren; i++) {
                children.add(species.giveChild(innovationHistory));
            }
        }

        while (children.size() < agents.size()) {
            children.add(species.get(0).giveChild(innovationHistory));
        }

        agents.clear();
        agents = children;
        generation++;
        for (Agent agent : agents) {
            agent.brain.generateNetwork();
        }

        assignIds();
        for (int i = 0; i < games.size(); i++) {
            games.set(i, new Game());
        }
    }

    public void speciate() {
        for (Species s : species) {
            s.agents.clear();
        }
        for (Agent agent : agents) {
            boolean foundSpecies = false;
            for (Species s : species) {
                if (s.sameSpecies(agent.brain)) {
                    s.addToSpecies(agent);
                    foundSpecies = true;
                    break;
                }
            }
            if (!foundSpecies) {
                species.add(new Species(agent));
            }
        }
    }

    public void calculateFitness() {
        for (Agent agent : agents) {
            agent.calculateFitness();
        }
    }

    public void sortSpecies() {
        for (Species s : this.species) {
            s.sortSpecies();
        }
        // sort the species by fitness
        species.sort((Species s1, Species s2) -> s1.bestFitness < s2.bestFitness ? 1 : -1);
    }

    public void killStaleSpecies() {
        for (int i = 0; i < species.size(); i++) {
            if (species.get(i).staleness >= 15) {
                species.remove(i);
                i--;
            }
        }
    }

    public void killBadSpecies() {
        double averageSum = getAvgFitnessSum();
        for (int i = 0; i < species.size(); i++) {
            if (species.get(i).averageFitness / averageSum * agents.size() < 1) {
                species.remove(i);
                i--;
            }
        }
    }

    public double getAvgFitnessSum() {
        double sum = 0;
        for (Species species : this.species) {
            sum += species.averageFitness;
        }
        return sum;
    }

    public void cullSpecies() {
        for (Species species : this.species) {
            species.cull();
            species.fitnessSharing();
            species.setAverageFitness();
        }
    }
}
