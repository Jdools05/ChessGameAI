package xyz.jdools05.ChessGameAI;

import java.util.ArrayList;
import java.util.List;

public class Species {
    public List<Agent> agents = new ArrayList<>();
    public double bestFitness = 0;
    public Agent champion;
    public double averageFitness = 0;
    public int staleness = 0;
    Genome rep;

    public double excessCoefficient = 1;
    public double weightDiffCoefficient = 0.5;
    public double compatibilityThreshold = 55;

    Species(){}

    Species(Agent agent){
        agents.add(agent);
        bestFitness = agent.fitness;
        rep = agent.brain;
        champion = agent;
    }

    public boolean sameSpecies(Genome g){
        double compatibility;
        double excessAndDisjoint = getExcessDisjoint(g, rep);
        double averageWeightDiff = getAverageWeightDiff(g, rep);

        double largeGenomeNormalizer = g.genes.size() - 20;
        if (largeGenomeNormalizer < 1) largeGenomeNormalizer = 1;

        compatibility = (excessCoefficient * excessAndDisjoint / largeGenomeNormalizer) + (weightDiffCoefficient * averageWeightDiff);
        return compatibility < compatibilityThreshold;
    }

    public void addToSpecies(Agent agent){
        agents.add(agent);
    }

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
        return (g1.genes.size() + g2.genes.size()) - 2 * matching;
    }

    public double getAverageWeightDiff(Genome g1, Genome g2){
        if (g1.genes.size() == 0 || g2.genes.size() == 0) return 0;

        double matching = 0;
        double totalDiff = 0;
        for (int i = 0; i < g1.genes.size(); i++){
            for (int j = 0; j < g2.genes.size(); j++){
                if (g1.genes.get(i).innovationNumber == g2.genes.get(j).innovationNumber){
                    totalDiff += Math.abs(g1.genes.get(i).weight - g2.genes.get(j).weight);
                    matching++;
                    break;
                }
            }
        }
        if (matching == 0) return 100;
        return totalDiff / matching;
    }

    public void sortSpecies() {
        agents.sort((a1, a2) -> Double.compare(a2.fitness, a1.fitness));
        if (agents.size() == 0) {
            staleness = 200;
            return;
        }

        if (agents.get(0).fitness > bestFitness) {
            bestFitness = agents.get(0).fitness;
            champion = agents.get(0);
            rep = champion.brain;
            staleness = 0;
        } else {
            staleness++;
        }
    }

    public void setAverageFitness(){
        double total = 0;
        for (Agent a : agents) total += a.fitness;
        averageFitness = total / agents.size();
    }

    public Agent giveChild(List<ConnectionHistory> innovationHistory){
        Agent child;
        if (Math.random() < 0.25) child = selectPlayer();
        else {
            Agent parent1 = selectPlayer();
            Agent parent2 = selectPlayer();

            if (parent1.fitness < parent2.fitness) child = parent2.crossover(parent1);
            else child = parent1.crossover(parent2);
        }
        child.brain.mutate(innovationHistory);
        return child;
    }

    public Agent selectPlayer(){
        double r = Math.random() * bestFitness;
        for (Agent a : agents){
            r -= a.fitness;
            if (r <= 0) return a;
        }
        return agents.get(0);
    }

    public void cull() {
        if (agents.size() > Math.ceil(agents.size() / 2.0)) {
            agents.subList(agents.size() / 2, agents.size()).clear();
        }
    }

    public void fitnessSharing() {
        for (Agent a : agents) a.fitness /= agents.size();
    }
}
