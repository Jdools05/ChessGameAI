package xyz.jdools05.ChessGameAI;

import java.util.ArrayList;
import java.util.List;

public class ConnectionHistory {
    // stores the input node's id
    public int fromNode;
    // stores the output node's id
    public int toNode;
    // store the innovation number 
    public int innovationNumber;
    // stores all the innovation numbers used to seach history
    public List<Integer> innovationNumbers = new ArrayList<>();

    // constructor
    public ConnectionHistory(int fromNode, int toNode, int innovationNumber, List<Integer> innovationNumbers) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.innovationNumber = innovationNumber;
        this.innovationNumbers = innovationNumbers;
    }

    // returns if a two Genomes are matching
    public boolean matches(Genome genome, Node from, Node to) {
        // if the number of connections are different, it's not a match
        if (genome.genes.size() == innovationNumbers.size()) {
            if (from.number == fromNode && to.number == toNode) {
                // check if all the innovation numbers are the same
                for (int i = 0; i < genome.genes.size(); i++) {
                    if (!innovationNumbers.contains(genome.genes.get(i).innovationNumber)) {
                        return false;
                    }
                }

                return true;
            }
        }

        return false;
    }
}
