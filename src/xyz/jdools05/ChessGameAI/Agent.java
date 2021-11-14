package xyz.jdools05.ChessGameAI;

import xyz.jdools05.chess.Game;

import java.util.Random;

public class Agent {
    public double fitness;
    public Genome brain;
    // input of the neural network
    public double[] vision = new double[64];
    // output of the neural network
    public double[] decision = new double[64];
    int turnCount = 0;
    int bestScore = 0;
    boolean dead = false;
    boolean won = false;
    int score = 0;
    int gen = 0;

    // max number of attempts the agent can make to find a valid move
    public static int MAX_ATTEMPTS = 10;

    int genomeInputs = 64;
    int genomeOutputs = 64;

    boolean isWhite = true;
    int game;

    // Constructor
    public Agent() {
        brain = new Genome(genomeInputs, genomeOutputs);
    }

    public void setGame(int id) {
        this.game = id;
    }

    public void setIsWhite(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public Game look(Game game) {
        // get the input of the neural network
        // translate the pieces into a 64-element array
        // set the input of the neural network to the array
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (game.getTile(i, j).piece != null) {
                    vision[i * 8 + j] = game.getTile(i, j).piece.value * (game.getTile(i, j).piece.white == isWhite ? 1 : -1);
                } else {
                    vision[i * 8 + j] = 0;
                }
            }
        }
        return game;
    }

    public void think() {
        decision = brain.feedForward(vision);
    }

    public Game move(Game game) {
        if (dead) return game;
        if (game.isGameOver()) {
            dead = true;
            return game;
        }

        // get the output of the neural network
        // find the highest value output and translate the index to a tile on the board
        // this will be the starting tile
        int highest = 0;
        for (int i = 0; i < decision.length; i++) {
            if (decision[i] > decision[highest]) {
                highest = i;
            }
        }
        // translate the index to a tile on the board
        int startTileX = highest / 8;
        int startTileY = highest % 8;

        // find the second-highest value output and translate the index to a tile on the board
        // this will be the ending tile
        int secondHighest = 0;
        for (int i = 0; i < decision.length; i++) {
            if (decision[i] > decision[secondHighest] && i != highest) {
                secondHighest = i;
            }
        }

        // translate the index to a tile on the board
        int endTileX = secondHighest / 8;
        int endTileY = secondHighest % 8;

        // move the piece to the tile
        if (game.isWhiteTurn() == isWhite) {
            // parse the indices into proper coordinates
            StringBuilder sb = new StringBuilder();
            sb.append((char) (startTileX + 'a'));
            sb.append(startTileY + 1);
            sb.append("-");
            sb.append((char) (endTileX + 'a'));
            sb.append(endTileY + 1);

            Random random = new Random();

            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    game.makeMove(sb.toString());
                    turnCount++;
                } catch (Exception e) {
                    // make random move
                    sb.append((char) (random.nextInt(7) + 'a'));
                    sb.append(random.nextInt(7) + 1);
                    sb.append("-");
                    sb.append((char) (random.nextInt(7) + 'a'));
                    sb.append(random.nextInt(7) + 1);
                }
                if ( i == MAX_ATTEMPTS - 1) {
                    dead = true;
                    game.setGameOver(true);
                }
            }
        }
        return game;
    }

    public void calculateFitness() {
        // calculate the fitness
        // if the agent is dead, fitness is negative turn count
        // if the agent is alive, fitness is inverse of turn count
        if (!won) {
            fitness = -turnCount;
        } else {
            fitness = 500 / (double) turnCount;
        }
    }

    public Agent crossover(Agent partner) {
        Agent child = new Agent();
        child.brain = brain.crossover(partner.brain);
        child.brain.generateNetwork();
        return child;
    }

}
