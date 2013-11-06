/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.sdp.graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A semantic dependency graph.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class Graph {

    private final List<Node> nodes;
    private final List<Edge> edges;
    //
    public final String id;

    public Graph(String id) {
        this.id = id;
        this.nodes = new ArrayList<Node>();
        this.edges = new ArrayList<Edge>();
    }

    public Node addNode(String form, String lemma, String pos, boolean isRoot, boolean isPred) {
        Node node = new Node(nodes.size(), form, lemma, pos, isRoot, isPred);
        nodes.add(node);
        return node;
    }

    public Edge addEdge(int source, int target, String label) {
        assert 0 <= source && source < nodes.size();
        assert 0 <= target && target < nodes.size();
        Edge edge = new Edge(edges.size(), source, target, label);
        edges.add(edge);
        nodes.get(source).addOutgoingEdge(edge);
        nodes.get(target).addIncomingEdge(edge);
        return edge;
    }

    public int getNNodes() {
        return nodes.size();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Node getNode(int node) {
        assert 0 <= node && node < nodes.size();
        return nodes.get(node);
    }

    public int getNEdges() {
        return edges.size();
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Edge getEdge(int edge) {
        assert 0 <= edge && edge < edges.size();
        return edges.get(edge);
    }

    public List<Node> getRoots() {
        List<Node> roots = new LinkedList<Node>();
        for (Node node : nodes) {
            if (node.isRoot) {
                roots.add(node);
            }
        }
        return roots;
    }

    public List<Node> getPreds() {
        List<Node> roots = new LinkedList<Node>();
        for (Node node : nodes) {
            if (node.isPred) {
                roots.add(node);
            }
        }
        return roots;
    }
}