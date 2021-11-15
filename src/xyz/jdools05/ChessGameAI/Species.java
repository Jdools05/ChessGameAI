package xyz.jdools05.ChessGameAI;

import java.util.ArrayList;
import java.util.List;

public class Species {
    // store the agents that belong to this species
    public List<Agent> agents = new ArrayList<>();
    // information about the best of the species
    public double bestFitness = 0;
    public Agent champion;

    // average fitness of the species
    public double averageFitness = 0;
    // number of generations since the last improvement
    public int staleness = 0;
    Genome rep;

    // values used to calculate if an agent belongs to this species
    public double excessCoefficient = 1;
    public double weightDiffCoefficient = 0.5;
    public double compatibilityThreshold = 55;

    Species(){}

    // initialize the species with a single agent
    Species(Agent agent){
        agents.add(agent);
        bestFitness = agent.fitness;
        rep = agent.brain;
        champion = agent;
    }

    // check if two agents belong to the same species
    public boolean sameSpecies(Genome g){
        // compare the average excess and weight difference
        double compatibility;
        double excessAndDisjoint = getExcessDisjoint(g, rep);
        double averageWeightDiff = getAverageWeightDiff(g, rep);

        double largeGenomeNormalizer = g.genes.size() - 20;
        if (largeGenomeNormalizer < 1) largeGenomeNormalizer = 1;

        // calculate the compatibility
        compatibility = (excessCoefficient * excessAndDisjoint / largeGenomeNormalizer) + (weightDiffCoefficient * averageWeightDiff);
        // if the difference is less than the threshold, the two agents are in the same species
        return compatibility < compatibilityThreshold;
    }

    public void addToSpecies(Agent agent){
        agents.add(agent);
    }

    // calculates the excess and disjoint genes between two genomes
    public double getExcessDisjoint(Genome g1, Genome g2){
        double matching = 0;
        for (int i = 0; i < g1.genes.size(); i++){
            for (int j = 0; j < g2.genes.size(); j++){
                if (g1.genes.get(i).innovationNumber == g2.genes.get(j).innovationNumber){
                    matching++;
                    break;
                }
            }
        }
        // calculate the final value
        return (g1.genes.size() + g2.genes.size()) - 2 * matching;
    }

    // calculates the average weight difference between two genomes
    public double getAverageWeightDiff(Genome g1, Genome g2){
        if (g1.genes.size() == 0 || g2.genes.size() == 0) return 0;

        double matching = 0;
        double totalDiff = 0;
        for (int i = 0; i < g1.genes.size(); i++){
            for (int j = 0; j < g2.genes.size(); j++){
                // check if the innovation number is a match
                // meaning this is the same gene
                if (g1.genes.get(i).innovationNumber == g2.genes.get(j).innovationNumber){
                    totalDiff += Math.abs(g1.genes.get(i).weight - g2.genes.get(j).weight);
                    matching++;
                    break;
                }
            }
        }
        // very different genomes and prevents divide by zero
        if (matching == 0) return 100;
        return totalDiff / matching;
    }

    // sort the agents by fitness
    public void sortSpecies() {
        agents.sort((a1, a2) -> Double.compare(a2.fitness, a1.fitness));
        // self-destruct if there is no agents
        if (agents.size() == 0) {
            staleness = 200;
            return;
        }

        // check for new champion
        if (agents.get(0).fitness > bestFitness) {
            bestFitness = agents.get(0).fitness;
            champion = agents.get(0);
            rep = champion.brain;
            staleness = 0;
        } else {
            staleness++;
        }
    }

    // calculate the average fitness of the species
    public void setAverageFitness(){
        double total = 0;
        for (Agent a : agents) total += a.fitness;
        averageFitness = total / agents.size();
    }

    // generate a new child based off a weighted random selection of the agents
    public Agent giveChild(List<ConnectionHistory> innovationHistory){
        Agent child;
        // 25% chance of cloning one of the agents
        if (Math.random() < 0.25) child = selectPlayer();
        else {
            // else select two random agents to breed
            Agent parent1 = selectPlayer();
            Agent parent2 = selectPlayer();

            // select the most fit to be the dominant parent
            if (parent1.fitness < parent2.fitness) child = parent2.crossover(parent1);
            else child = parent1.crossover(parent2);
        }
        // mutate the child
        child.brain.mutate(innovationHistory);
        return child;
    }

    // function to select a weighted random agent
    // the higher the fitness, the more likely they are to be selected
    public Agent selectPlayer(){
        double r = Math.random() * bestFitness;
        for (Agent a : agents){
            r -= a.fitness;
            if (r <= 0) return a;
        }
        return agents.get(0);
    }

    // kill off the bottom half of the species
    public void cull() {
        if (agents.size() > Math.ceil(agents.size() / 2.0)) {
            agents.subList(agents.size() / 2, agents.size()).clear();
        }
    }

    // recalculate the fitness of the species with respect to the current species size
    public void fitnessSharing() {
        for (Agent a : agents) a.fitness /= agents.size();
    }
}
