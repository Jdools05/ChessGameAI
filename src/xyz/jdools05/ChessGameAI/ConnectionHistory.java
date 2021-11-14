package xyz.jdools05.ChessGameAI;

import java.util.ArrayList;
import java.util.List;

public class ConnectionHistory {
    public int fromNode;
    public int toNode;
    public int innovationNumber;
    public List<Integer> innovationNumbers = new ArrayList<>();

    public ConnectionHistory(int fromNode, int toNode, int innovationNumber, List<Integer> innovationNumbers) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.innovationNumber = innovationNumber;
        this.innovationNumbers = innovationNumbers;
    }

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
