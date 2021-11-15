package xyz.jdools05.ChessGameAI;

import xyz.jdools05.chess.Game;

import java.util.Random;

public class Agent {
    // how good the agent is
    public double fitness;
    // the brain of the agent
    public Genome brain;
    // input of the neural network
    public double[] vision = new double[64];
    // output of the neural network
    public double[] decision = new double[64];

    // specifics of the game
    int turnCount = 0;
    boolean dead = false;
    boolean won = false;
    double score = 0;

    // generation of the agent
    int gen = 0;

    // max number of attempts the agent can make to find a valid move
    public static int MAX_ATTEMPTS = 10;

    // 8x8 board
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

    // allows the agent to look at the board and set the input values
    public void look(Game game) {
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
    }

    // run the agents neural network
    public void think() {
        decision = brain.feedForward(vision);
    }

    // calculate the output and translate it into a move
    public Game move(Game game) {
        // if dead, don't make moves
        if (dead) return game;
        // if the game is over, don't make moves
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

            // attempt the move
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                // if the move is invalid, this will catch
                try {
                    game.makeMove(sb.toString());
                } catch (Exception e) {
                    // make random move
                    // prevents the agent from getting stuck
                    sb.append((char) (random.nextInt(7) + 'a'));
                    sb.append(random.nextInt(7) + 1);
                    sb.append("-");
                    sb.append((char) (random.nextInt(7) + 'a'));
                    sb.append(random.nextInt(7) + 1);
                }
                // if exceeded the max number of attempts, break and forfeit the game
                if ( i == MAX_ATTEMPTS - 1) {
                    dead = true;
                    game.setGameOver(true);
                }
            }
            turnCount++;
        }
        // update the game
        return game;
    }

    // the hardest part of the project
    public void calculateFitness() {
        // calculate the fitness
        // if the agent lost, fitness is turnCount
        // if the agent won, fitness is inverse of turnCount scaled by a large number
        fitness = (dead ? turnCount : 10000000 / (double) turnCount);
        score = fitness;
    }

    // crossover the genes of two agents
    public Agent crossover(Agent partner) {
        Agent child = new Agent();
        child.brain = brain.crossover(partner.brain);
        // rebuild the brain
        child.brain.generateNetwork();
        return child;
    }

}
