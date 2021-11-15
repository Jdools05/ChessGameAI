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
    private double bestScore = Double.NEGATIVE_INFINITY;
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

    // forces the game to end and makes the agents lose
    public void forceEnd() {
        for (Agent agent : agents) {
            if (!agent.dead) {
                agent.won = false;
            }
            agent.dead = true;
        }
    }

    // if the agent is still alive allow the game to continue
    public void updateAlive() {
        for (Agent agent : agents) {
            if (!agent.dead) {
                agent.look(getGame(agent.game));
                agent.think();
                // update the game to prevent errors
                games.set(agent.game, agent.move(getGame(agent.game)));
            }
        }
    }

    // boolean that determines if the there are any agents alive
    public boolean isFinished() {
        for (Agent agent : agents) {
            if (!agent.dead) {
                return false;
            }
        }
        return true;
    }

    // gets the global best agent and saves it
    public void setBestAgent() {
        Agent best = species.get(0).agents.get(0);
        best.gen = generation;

        if (best.fitness > bestScore) {
            genAgents.add(best);
            bestScore = best.fitness;
            bestAgent = best;
        }
        Logger.getGlobal().info("Generation: " + generation + " Best score: " + bestScore);
    }

    // assigns game id to each agent
    public void assignIds() {
        for (int i = 0; i < agents.size() / 2; i ++) {
            agents.get(i).game = i;
            agents.get(i + 1).game = i;
            agents.get(i+1).isWhite = false;
        }
    }

    // returns the game for the agent
    public Game getGame(int id) {
        return games.get(id);
    }

    // very complicated method that makes the agents reproduce
    public void naturalSelection() {
        // sort the agents by genome
        speciate();
        // calculate the fitness of each agent
        calculateFitness();
        // sort the species by fitness
        sortSpecies();
        // remove the bottom half of the species
        cullSpecies();
        // get the best agent in that species
        setBestAgent();
        // if no improvements have been made, kill the species
        killStaleSpecies();
        // if the species is horrible, kill that species
        killBadSpecies();

        // creates new children using a weighted random selection based on fitness
        double averageSum = getAvgFitnessSum();
        List<Agent> children = new ArrayList<>();
        for (Species species : this.species) {
            children.add(species.champion);
            int numChildren = (int) (Math.floor(species.averageFitness / averageSum * agents.size()) - 1);
            for (int i = 0; i < numChildren; i++) {
                children.add(species.giveChild(innovationHistory));
            }
        }

        // make sure that the population is always equal to the previous generation
        while (children.size() < agents.size()) {
            children.add(species.get(0).giveChild(innovationHistory));
        }

        // clear the old population and replace it with the new one
        agents.clear();
        agents = children;
        generation++;
        // generate the agents brains
        for (Agent agent : agents) {
            agent.brain.generateNetwork();
        }

        // assign the game ids to the agents
        assignIds();
        // reset the games
        games = new ArrayList<>();
        for (int i = 0; i < agents.size() / 2; i++) {
            games.add(new Game());
        }
    }

    // sorts the Agents by brain structure
    public void speciate() {
        // clear the species
        for (Species s : species) {
            s.agents.clear();
        }
        // sort the agents by genome
        for (Agent agent : agents) {
            boolean foundSpecies = false;
            for (Species s : species) {
                // if the agents' genome fits the species genome
                if (s.sameSpecies(agent.brain)) {
                    s.addToSpecies(agent);
                    foundSpecies = true;
                    break;
                }
            }
            // if the agent doesn't fit any species, create a new one
            if (!foundSpecies) {
                species.add(new Species(agent));
            }
        }
    }

    // calculates the fitness of each agent
    public void calculateFitness() {
        for (Agent agent : agents) {
            agent.calculateFitness();
        }
    }

    // sorts the species by fitness
    public void sortSpecies() {
        for (Species s : this.species) {
            s.sortSpecies();
        }
        // sort the species by fitness
        species.sort((Species s1, Species s2) -> s1.bestFitness < s2.bestFitness ? 1 : -1);
    }

    // kills the species that have not improved in a while
    public void killStaleSpecies() {
        // prevent max extinction
        int maxKill = (int) Math.floor(species.size() * 0.1);
        int counter = 0;
        for (int i = 0; i < species.size(); i++) {
            if (species.get(i).staleness >= 15 && counter < maxKill) {
                species.remove(i);
                counter++;
                i--;
            }
        }
    }

    // kills the species that are too bad
    public void killBadSpecies() {
        // prevent max extinction
        int maxKill = (int) Math.floor(species.size() * 0.1);
        int counter = 0;
        double averageSum = getAvgFitnessSum();
        for (int i = 0; i < species.size(); i++) {
            if (species.get(i).averageFitness / averageSum * agents.size() < 1 && counter < maxKill) {
                species.remove(i);
                counter++;
                i--;
            }
        }
    }

    // get the average fitness sum of a species
    public double getAvgFitnessSum() {
        double sum = 0;
        for (Species species : this.species) {
            sum += species.averageFitness;
        }
        return sum;
    }

    // kill the bottom half of the population of the species
    public void cullSpecies() {
        for (Species species : this.species) {
            species.cull();
            // change the fitness to be proportional to the number of agents of the species
            species.fitnessSharing();
            species.setAverageFitness();
        }
    }
}
