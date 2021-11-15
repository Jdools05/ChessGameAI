package xyz.jdools05.ChessGameAI;

import java.util.ArrayList;
import java.util.List;

// Neuron in the neural network
public class Node {
    // unique id assigned to a number
    public int number;
    // sum of the inputs of the input connections
    public double inputSum = 0;
    // output value after activation
    public double outputValue = 0;
    // list of connections to the next layer(s)
    public List<ConnectionGene> outputConnections = new ArrayList<ConnectionGene>();
    // layer the node is in
    public int layer = 0;

    // constructor
    public Node(int number) {
        this.number = number;
    }

    // run the activation function
    public void engage() {
        // no activation for input nodes
        if (layer != 0) {
            outputValue = 1 / (1 + Math.exp(-inputSum * 5));
        }

        // for each connection
        for (ConnectionGene connection : outputConnections) {
            // send the output to the next nodes.
            if (connection.enabled) {
                connection.toNode.inputSum += connection.weight * outputValue;
            }
        }
    }

    // boolean that returns if the two nodes are connect by a gene
    public boolean isConnectedTo(Node node) {
        // connected nodes cannot be in the same layer
        if (node.layer == layer) {
            return false;
        }

        // check if the node is connected as an input
        if (node.layer < layer) {
            for (ConnectionGene connection : outputConnections) {
                if (connection.toNode == this) {
                    return true;
                }
            }
            // check if the node is connected as an output
        } else {
            for (ConnectionGene connection : node.outputConnections) {
                if (connection.toNode == node) {
                    return true;
                }
            }
        }
        // else they are not connected
        return false;
    }
}
