package xyz.jdools05.ChessGameAI;

// entrance point of the NEAT implementation for the chess game
public class Main {

    static int nextConnectionNo = 1000;

    public static void main(String[] args) {

        // create a new population
        // store the population
        Population population = new Population(100);

        // iterate a number of times
        for (int i = 0; i < 100; i++) {
            // if the population is not finished
            while (!population.isFinished()) {
                // update the population
                population.updateAlive();
            }
                // utilize the genetic algorithm to create a new population
            population.naturalSelection();
        }
    }

}
