package org.realitix.dfilesearch.filesearch.util;

import org.realitix.dfilesearch.filesearch.beans.Node;

import java.util.HashMap;

public class NodeMap {

    private HashMap<Integer, Node> nodeMap = new HashMap<Integer, Node>();

    public NodeMap insertNode(Node node) {
        // Insert node
        return this;
    }

    public NodeMap removeNode(int id) {
        // Remove node
        return this;
    }

    public HashMap<Integer, Node> getNodeMap() {
        return nodeMap;
    }

    public void setNodeMap(HashMap<Integer, Node> nodeMap) {
        this.nodeMap = nodeMap;
    }
}
