package org.realitix.dfilesearch.filesearch.util;

import org.realitix.dfilesearch.filesearch.beans.Node;

import java.util.HashMap;

public class NodeMap {

    private HashMap<Integer, Node> nodeMap = new HashMap<>();

    public NodeMap insertNode(Node node, int id) {
        nodeMap.put(id, node);
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
