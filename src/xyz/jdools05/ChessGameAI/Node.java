package xyz.jdools05.ChessGameAI;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public int number;
    public double inputSum = 0;
    public double outputValue = 0;
    public List<ConnectionGene> outputConnections = new ArrayList<ConnectionGene>();
    public int layer = 0;

    public Node(int number) {
        this.number = number;
    }

    public void engage() {
        // no activation for input nodes
        if (layer != 0) {
            outputValue = 1 / (1 + Math.exp(-inputSum * 5));
        }

        // for each connection
        for (ConnectionGene connection : outputConnections) {
            if (connection.enabled) {
                connection.toNode.inputSum += connection.weight * outputValue;
            }
        }
    }

    public boolean isConnectedTo(Node node) {
        if (node.layer == layer) {
            return false;
        }

        if (node.layer < layer) {
            for (ConnectionGene connection : outputConnections) {
                if (connection.toNode == this) {
                    return true;
                }
            }
        } else {
            for (ConnectionGene connection : node.outputConnections) {
                if (connection.toNode == node) {
                    return true;
                }
            }
        }
        return false;
    }
}
