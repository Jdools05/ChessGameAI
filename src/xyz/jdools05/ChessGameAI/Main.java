package xyz.jdools05.ChessGameAI;

// entrance point of the NEAT implementation for the chess game
public class Main {

    // store the population
    private static Population population;

    public static void main(String[] args) {

        // create a new population
        population = new Population();

        // iterate a number of times
        for (int i = 0; i < 100; i++) {
            // if the population is not finished
            if (!population.isFinished()) {
                // update the population
                population.update();
            } else {
                // utilize the genetic algorithm to create a new population
                population.naturalSelection();
            }
        }
    }

}
