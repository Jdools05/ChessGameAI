package xyz.jdools05.ChessGameAI;

public class ConnectionGene {
    public Node fromNode;
    public Node toNode;
    public double weight;
    public boolean enabled = true;
    public int innovationNumber;

    public ConnectionGene(Node fromNode, Node toNode, double weight, int innovationNumber) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.weight = weight;
        this.innovationNumber = innovationNumber;
    }

    public void mutateWeight() {
        // 10% of the time, completely randomize the weight
        // otherwise slightly change it
        if (Math.random() < 0.1) {
            weight = Math.random() * 2 - 1;
        } else {
            weight += (Math.random() * 0.2 - 0.1);
        }
    }

    public ConnectionGene duplicate(Node from, Node to) {
        ConnectionGene clone = new ConnectionGene(from, to, weight, innovationNumber);
        clone.enabled = enabled;
        return clone;
    }
}
