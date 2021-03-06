package xyz.jdools05.ChessGameAI;

import java.util.ArrayList;
import java.util.List;

public class Genome {
    // The list of genes
    public List<ConnectionGene> genes = new ArrayList<>();
    // the list of nodes the genes are attached to
    public List<Node> nodes = new ArrayList<>();
    // number of inputs
    public int inputs;
    // number of outputs
    public int outputs;
    // number of layers in the network
    public int layers = 2;
    // counter to keep track of the id of new nodes
    public int nextNode = 0;
    // store the id of the bias node
    public int biasNode;
    // store the nodes together in a network
    public List<Node> network = new ArrayList<>();

    // constructor to forego initializing
    @SuppressWarnings("unused")
    public Genome (int inputs, int outputs, boolean crossover) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    // main constructor
    public Genome(int inputs, int outputs) {
        this.inputs = inputs;
        this.outputs = outputs;

        // create input nodes
        for (int i = 0; i < inputs; i++) {
            nodes.add(new Node(i));
            nextNode++;
            nodes.get(i).layer = 0;
        }

        // create output nodes
        for (int i = 0; i < outputs; i++) {
            nodes.add(new Node(i + inputs));
            nextNode++;
            nodes.get(i + inputs).layer = 1;
        }

        // add bias node
        nodes.add(new Node(nextNode));
        biasNode = nextNode;
        nextNode++;
        nodes.get(biasNode).layer = 0;
    }

    // return the node with the given id
    public Node getNode(int nodeNumber) {
        for (Node node : nodes) {
            if (node.number == nodeNumber) {
                return node;
            }
        }
        return null;
    }

    // add the genes to connect the nodes together
    public void connectNodes() {
        // clear connections
        for (Node node : nodes) {
            node.outputConnections.clear();
        }

        // connect nodes
        for (ConnectionGene gene : genes) {
            gene.fromNode.outputConnections.add(gene);
        }
    }

    // calculate the outputs of the network
    public double[] feedForward(double[] in) {
        // set input as output of input nodes
        for (int i = 0; i < inputs; i++) {
            nodes.get(i).outputValue = in[i];
        }
        nodes.get(biasNode).outputValue = 1;

        // engage nodes
        for (Node node : nodes) {
            node.engage();
        }

        // outputs are nodes[inputs] to node[inputs + outputs -1]
        double[] out = new double[outputs];
        for (int i = 0; i < outputs; i++) {
            out[i] = nodes.get(inputs + i).outputValue;
        }

        // reset nodes
        for (Node node : nodes) {
            node.inputSum = 0;
        }

        return out;
    }

    // puts together the network from genome
    public void generateNetwork() {
        connectNodes();
        network = new ArrayList<>();
        // for each layer, add the node in that layer, since layers cannot connect to themselves
        // there is no need to order the nodes within a layer
        for (int i = 0; i < layers; i++) {
            for (Node node : nodes) {
                if (node.layer == i) {
                    network.add(node);
                }
            }
        }
    }

    // adds a node on a gene
    public void addNode(List<ConnectionHistory> innovationHistory) {
        // pick a random connection to create a node between
        if (genes.size() == 0) {
            addConnection(innovationHistory);
            return;
        }
        int randomConnection = (int) Math.floor(Math.random() * genes.size());

        // dont disconnect the bias node
        while (genes.get(randomConnection).fromNode == nodes.get(biasNode) && genes.size() > 1) {
            randomConnection = (int) Math.floor(Math.random() * genes.size());
        }

        // disable the connection
        genes.get(randomConnection).enabled = false;

        // add the node
        int newNodeNumber = nextNode;
        nodes.add(new Node(newNodeNumber));
        nextNode++;

        // add a new connection to the new node with a weight of 1
        int connectionInnovationNumber = getInnovationNumber(innovationHistory, genes.get(randomConnection).fromNode, getNode(newNodeNumber));
        genes.add(new ConnectionGene(getNode(newNodeNumber), genes.get(randomConnection).toNode, genes.get(randomConnection).weight, connectionInnovationNumber));
        getNode(newNodeNumber).layer = genes.get(randomConnection).fromNode.layer + 1;

        // add a new connection from the new node to the old node with a weight of 1
        connectionInnovationNumber = getInnovationNumber(innovationHistory, nodes.get(biasNode), getNode(newNodeNumber));
        genes.add(new ConnectionGene(nodes.get(biasNode), getNode(newNodeNumber), 1, connectionInnovationNumber));

        //if the layer of the new node is equal to the layer of the output node of the old connection then a new layer needs to be created
        //more accurately the layer numbers of all layers equal to or greater than this new node need to be incremented
        if (getNode(newNodeNumber).layer == genes.get(randomConnection).toNode.layer) {
            for (int i = 0; i < nodes.size() - 1; i++) {
                if (nodes.get(i).layer >= getNode(newNodeNumber).layer) {
                    nodes.get(i).layer++;
                }
            }
            layers++;
        }
        connectNodes();
    }

    // adds a connection between two nodes
    public void addConnection(List<ConnectionHistory> innovationHistory) {
        // cannot add a connection to a fully connected network
        if (fullyConnected()) {
            return;
        }

        // get random nodes
        int randomNode1 = (int) Math.floor(Math.random() * nodes.size());
        int randomNode2 = (int) Math.floor(Math.random() * nodes.size());
        while (randomConnectionNodesDontWork(randomNode1, randomNode2)) {
            // get new random nodes
            randomNode1 = (int) Math.floor(Math.random() * nodes.size());
            randomNode2 = (int) Math.floor(Math.random() * nodes.size());
        }
        int temp;
        if (nodes.get(randomNode1).layer > nodes.get(randomNode2).layer) { // swap nodes if the first node is in a higher layer
            temp = randomNode1;
            randomNode1 = randomNode2;
            randomNode2 = temp;
        }

        // add the connection
        int connectionInnovationNumber = getInnovationNumber(innovationHistory, nodes.get(randomNode1), nodes.get(randomNode2));
        genes.add(new ConnectionGene(nodes.get(randomNode1), nodes.get(randomNode2), Math.random() * 2 - 1, connectionInnovationNumber));
        connectNodes();
    }

    // checks if the nodes will cause errors in the network
    public boolean randomConnectionNodesDontWork(int randomNode1, int randomNode2) {
        // if the nodes are in the same layer or if they are already connected, return true
        return nodes.get(randomNode1).layer == nodes.get(randomNode2).layer || nodes.get(randomNode1).isConnectedTo(nodes.get(randomNode2));
    }

    //returns the innovation number for the new mutation
    //if this mutation has never been seen before then it will be given a new unique innovation number
    //if this mutation matches a previous mutation then it will be given the same innovation number as the previous one
    public int getInnovationNumber(List<ConnectionHistory> innovationHistory, Node fromNode, Node toNode) {
        boolean isNew = true;
        int connectionInnovationNumber = Main.nextConnectionNo;
        for (ConnectionHistory connectionHistory : innovationHistory) {
            if (connectionHistory.matches(this, fromNode, toNode)) {
                isNew = false;
                connectionInnovationNumber = connectionHistory.innovationNumber;
                break;
            }
        }

        // if this is a new mutation then give it a new innovation number
        if (isNew) {
            List<Integer> innovationNumbers = new ArrayList<>();
            for (ConnectionGene gene : genes) {
                innovationNumbers.add(gene.innovationNumber);
            }

            innovationHistory.add(new ConnectionHistory(fromNode.number, toNode.number, connectionInnovationNumber, innovationNumbers));
            Main.nextConnectionNo++;
        }
        return connectionInnovationNumber;
    }

    // returns whether the network is fully connected
    public boolean fullyConnected() {
        int maxConnections = 0;
        // set layers to match the highest layer in the nodes
        for (Node node : nodes) {
            if (node.layer > layers-1) {
                layers = node.layer+1;
            }
        }

        int[] nodesInLayers = new int[layers];

        // count the number of nodes in each layer
        for (Node node : nodes) {
            nodesInLayers[node.layer]++;
        }

        // find the maximum number of connections
        for (int i = 0; i < layers-1; i++) {
            int nodesInFront = 0;
            for (int j = i+1; j < layers; j++) {
                nodesInFront += nodesInLayers[j];
            }

            maxConnections += nodesInLayers[i] * nodesInFront;
        }
        // if the number of connections is the same as the maximum number of connections then the network is fully connected
        return maxConnections == genes.size();
    }

    // changes the genome slightly
    public void mutate(List<ConnectionHistory> innovationHistory) {
        // if there is no genes then add a random connection
        if (genes.size() == 0) {
            addConnection(innovationHistory);
        }

        // 80% chance of changing a connection weight
        if (Math.random() < 0.8) {
            for (ConnectionGene gene : genes) {
                gene.mutateWeight();
            }
        }

        // 5% chance of adding a new connection
        if (Math.random() < 0.05) {
            addConnection(innovationHistory);
        }

        // 2% chance of adding a new node
        if (Math.random() < 0.02) {
            addNode(innovationHistory);
        }
    }

    // crossover two genomes to give a new genome
    // refer to biological evolution theory for more information on crossover
    public Genome crossover(Genome mate) {
        // initialise a new genome
        Genome child = new Genome(inputs, outputs, true);
        child.genes.clear();
        child.nodes.clear();
        child.layers = layers;
        child.nextNode = nextNode;
        child.biasNode = biasNode;
        List<ConnectionGene> childGenes = new ArrayList<>();
        List<Boolean> isEnabled = new ArrayList<>();
        // all the genes from the parent are copied to the child
        for (ConnectionGene gene : genes) {
            boolean setEnabled = true;

            // if the gene is in the mate then copy it from the mate
            int mateGene = matchingGene(mate, gene.innovationNumber);
            // if the gene is not in the mate then there is a chance to disable it
            if (mateGene != -1) {
                if (!gene.enabled || !mate.genes.get(mateGene).enabled) {
                    if (Math.random() < 0.75) {
                        setEnabled = false;
                    }
                }
                // chance to copy the gene from the mate
                if (Math.random() < 0.5) {
                    childGenes.add(gene);
                } else {
                    childGenes.add(mate.genes.get(mateGene));
                }
            } else {
                childGenes.add(gene);
                setEnabled = gene.enabled;
            }
            isEnabled.add(setEnabled);
        }

        child.nodes.addAll(nodes);

        // add the child's genes to the child
        for (int i = 0; i < childGenes.size(); i++) {
            child.genes.add(childGenes.get(i).duplicate(child.getNode(childGenes.get(i).fromNode.number), child.getNode(childGenes.get(i).toNode.number)));
            child.genes.get(i).enabled = isEnabled.get(i);
        }

        // generate the child's nodes
        child.connectNodes();
        return child;
    }

    //returns whether there is a gene matching the input innovation number  in the input genome
    int matchingGene(Genome mate, int innovationNumber) {
        for (int i =0; i < mate.genes.size(); i++) {
            if (mate.genes.get(i).innovationNumber == innovationNumber) {
                return i;
            }
        }
        return -1; //no matching gene found
    }
}
