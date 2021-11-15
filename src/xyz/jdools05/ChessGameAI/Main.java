package xyz.jdools05.ChessGameAI;

import java.util.logging.Logger;

// entrance point of the NEAT implementation for the chess game
public class Main {

    static int nextConnectionNo = 1000;

    public static void main(String[] args) {

        // create a new population
        // store the population
        Population population = new Population(100);

        // iterate a number of times
        for (int i = 0; i < 1000; i++) {
            // if the population is not finished
            int turnCount = 0;
            while (!population.isFinished()) {
                turnCount++;
                // update the population
                population.updateAlive();
                if (turnCount > 500) {
                    // force the population to end
                    population.forceEnd();
                }
            }
                // utilize the genetic algorithm to create a new population
            population.naturalSelection();
        }
    }

}
