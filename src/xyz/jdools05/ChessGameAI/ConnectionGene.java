package xyz.jdools05.ChessGameAI;

// stores a unique connection between nodes
public class ConnectionGene {
    // store the input node
    public Node fromNode;
    // stores the output node
    public Node toNode;
    // stores the weight of the connection
    public double weight;
    // if the connection is enabled
    public boolean enabled = true;
    // unique id given to this connection
    public int innovationNumber;

    // constructor 
    public ConnectionGene(Node fromNode, Node toNode, double weight, int innovationNumber) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.weight = weight;
        this.innovationNumber = innovationNumber;
    }

    // changes the weight of this connection
    public void mutateWeight() {
        // 10% of the time, completely randomize the weight
        // otherwise slightly change it
        if (Math.random() < 0.1) {
            weight = Math.random() * 2 - 1;
        } else {
            weight += (Math.random() * 0.2 - 0.1);
        }
    }

    // code to return a duplication of this gene (may be removed if no use)
    public ConnectionGene duplicate(Node from, Node to) {
        ConnectionGene clone = new ConnectionGene(from, to, weight, innovationNumber);
        clone.enabled = enabled;
        return clone;
    }
}
