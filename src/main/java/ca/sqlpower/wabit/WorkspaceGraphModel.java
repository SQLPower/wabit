package ca.sqlpower.wabit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import ca.sqlpower.graph.BreadthFirstSearch;
import ca.sqlpower.graph.GraphModel;

/**
 * This graph takes a {@link WabitObject} for its root and makes a graph
 * model that represents all of the root's dependencies. The root is
 * included in the dependencies. The children can be included or the graph
 * can just be of the dependencies. If the children are not included in the
 * graph then the parents will replace their children when adding edges.
 * <p>
 * Example:<br>
 * If we have a workspace with a report, chart, query, and 2 data sources 
 * the parent tree would look like the following
 * <pre>
 *                       Workspace
 *                           |
 *    ---------------------------------------------
 *   /     |       |             |                 \
 * DS1    DS2    Query         Chart             Report
 *                               |                 |
 *                           ---------             |
 *                          /    |    \            |
 *                       Col1  Col2  Col3         Page
 *                                                 |
 *                                            -----------
 *                                           /     |     \
 *                                          CB1   CB2   CB3
 *                                           |     |
 *                                          CR1   CR2
 * </pre>
 * Where a DS is a data source, a CB is a content box and a CR is a content renderer.<br>
 * If we said the Query was based on DS1, the Chart was based on the Query, and the Report
 * was displaying the Chart in CB1 and the Query in CB2 we would have dependencies between
 * the WabitObjects. With dependencies the tree would look like the following<br>
 * <img src="doc-files/ProjectGraphModel-1.JPG"><br>
 * If a graph is made with the report node as the root you will have a graph of the following
 * <pre>
 *             Report
 *               |
 *               |
 *               |
 *              Page
 *               |
 *          ----------------------------
 *         /                      |     \
 *        CB1                    CB2   CB3
 *         |                      |
 *        CR1                    CR2
 *         |                      |
 *       Chart----------------->Query
 *         |                      |
 *      ---------                DS1
 *     /    |    \
 *   Col1  Col2  Col3
 * </pre>
 * If a graph is made without the children of objects being included in the graph
 * the result will look like the following
 * <pre>
 *            Report
 *              |
 *         -----------
 *        /           \
 *     Chart-------->Query
 *                     |
 *                    DS1
 * </pre>
 * If we look at a graph without the objects children, just the dependencies, with
 * the query as the root we will get the graph
 * <pre>
 *    Query
 *      |
 *     DS1
 * </pre>
 * If we then reverse the polarity on the graph above we get
 * <pre>
 *              Query
 *                |
 *           -----------
 *          /           \
 *        Chart         CR2
 *          |
 *         CR1
 * </pre>
 */
public class WorkspaceGraphModel implements GraphModel<WabitObject, WorkspaceGraphModelEdge> {
    
    /**
     * This list will contain all of the nodes in the entire graph.
     */
    private final Set<WabitObject> nodes = new HashSet<WabitObject>();
    
    /**
     * This list will map each node to a list of inbound edges on the node.
     */
    private final Map<WabitObject, Set<WorkspaceGraphModelEdge>> inboundEdges = 
        new HashMap<WabitObject, Set<WorkspaceGraphModelEdge>>();
    
    /**
     * This list will map each node to a list of outbound edges on the node.
     */
    private final Map<WabitObject, Set<WorkspaceGraphModelEdge>> outboundEdges =
        new HashMap<WabitObject, Set<WorkspaceGraphModelEdge>>();

    /**
     * This node is the starting point when creating the graph.
     * @see #WorkspaceGraphModel(WabitObject, WabitObject, boolean, boolean)
     */
    private final WabitObject graphStartNode;

    /**
     * @param root
     *            The Wabit object that is the highest ancestor of all of the
     *            objects in the graph. This node is used to create an initial
     *            graph ensuring no nodes are missed when reversing polarity.
     *            This will normally be the WabitWorkspace.
     * @param graphStartNode
     *            The Wabit object to start creating the graph model from. This
     *            will be the starting point for finding edges and nodes. Only
     *            objects that are dependencies or children, if the child flag
     *            is set, recursively will be included in the graph. If the
     *            entire workspace is desired to be made into a graph this
     *            should be the workspace object. This must be a
     *            child/grandchild/etc of the root.
     * @param showOnlyDependencies
     *            If false the children of objects will be included in the graph
     *            and their parent/child relationship will be included as an
     *            edge. If true only the dependency lines will be stored when
     *            creating the graph not the parent/child edges. The children
     *            will still be traversed if this is true but the edge created
     *            for the dependency will link the child traversed from's
     *            ancestor to the object the child depends on directly not its
     *            ancestor. The ancestor to link to will always be a child of
     *            the root object given.
     * @param reversePolarity
     *            If false the edges in the graph will go from the parent object
     *            to the child object for parent/child edges, and dependencies
     *            edges will go from the object that is dependent on an object
     *            to the object being depended on. If this is true then the
     *            edges in the graph will be reversed so the children of objects
     *            will have an edge pointing to their parent and objects being
     *            depended on will point to the objects that depend on them.
     */
    public WorkspaceGraphModel(WabitObject root, WabitObject graphStartNode, 
            boolean showOnlyDependencies, boolean reversePolarity) {
        
        //The graph made by this constructor is done in two steps:
        //1) create an initial graph of the whole workspace and if necessary reverse 
        //   polarity. Child nodes will be hidden if we are showing only dependencies.
        //2) do a BFS on the graph starting at the start node to find what nodes should 
        //   stay in the graph, ie connected, and remove the rest.
        
        this.graphStartNode = graphStartNode;
        Queue<WabitObject> adjacentNodes = new LinkedList<WabitObject>();
        Map<WabitObject, WabitObject> childParentMap = new HashMap<WabitObject, WabitObject>();
        adjacentNodes.add(root);
        
        while (!adjacentNodes.isEmpty()) {
            WabitObject node = adjacentNodes.remove();
            if (nodes.contains(node)) continue;
            
            if (node != root) {
                for (WabitObject child : node.getChildren()) {
                    childParentMap.put(child, node);
                }
            }
            
            List<WabitObject> edgeChildren = new ArrayList<WabitObject>();
            edgeChildren.addAll(node.getDependencies());
            edgeChildren.addAll(node.getChildren());
            for (WabitObject child : edgeChildren) {
                adjacentNodes.add(child);

                if (showOnlyDependencies && child.getParent() != null && 
                        child.getParent().equals(node)) continue;
                
                WabitObject childNode = child;
                WabitObject parentNode = node;
                if (reversePolarity) {
                    WabitObject temp = parentNode;
                    parentNode = childNode;
                    childNode = temp;
                }
                
                if (showOnlyDependencies) {
                    while (childParentMap.get(parentNode) != null) {
                        parentNode = childParentMap.get(parentNode);
                    }
                }
                
                WorkspaceGraphModelEdge edge = new WorkspaceGraphModelEdge(parentNode, childNode);
                Set<WorkspaceGraphModelEdge> childInboundList = inboundEdges.get(childNode);
                if (childInboundList == null) {
                    childInboundList = new HashSet<WorkspaceGraphModelEdge>();
                    inboundEdges.put(childNode, childInboundList);
                }
                childInboundList.add(edge);
                Set<WorkspaceGraphModelEdge> parentOutboundList = outboundEdges.get(parentNode);
                if (parentOutboundList == null) {
                    parentOutboundList = new HashSet<WorkspaceGraphModelEdge>();
                    outboundEdges.put(parentNode, parentOutboundList);
                }
                parentOutboundList.add(edge);
            }
            
            if (!showOnlyDependencies) {
                nodes.add(node);
            } else {
                if (!childParentMap.containsKey(node) 
                        || inboundEdges.get(node) != null 
                        || outboundEdges.get(node) != null) {
                    nodes.add(node);
                }
            }
        }

        BreadthFirstSearch<WabitObject, WorkspaceGraphModelEdge> bfs = 
            new BreadthFirstSearch<WabitObject, WorkspaceGraphModelEdge>();
        List<WabitObject> connectedObjects = bfs.performSearch(this, graphStartNode);

        List<WabitObject> removedObjects = new ArrayList<WabitObject>();
        for (WabitObject node : nodes) {
            if (!connectedObjects.contains(node)) {
                removedObjects.add(node);
            }
        }
        nodes.removeAll(removedObjects);
        for (WabitObject removedNode : removedObjects) {
            inboundEdges.remove(removedNode);
            if (outboundEdges.get(removedNode) != null) {
                for (WorkspaceGraphModelEdge edge : outboundEdges.get(removedNode)) {
                    if (inboundEdges.get(edge.getChild()) != null) {
                        inboundEdges.get(edge.getChild()).remove(edge);
                    }
                }
            }
            outboundEdges.remove(removedNode);
        }
        
    }

    public Collection<WabitObject> getAdjacentNodes(WabitObject node) {
        Collection<WabitObject> adjacentNodes = new HashSet<WabitObject>();
        Set<WorkspaceGraphModelEdge> edges = outboundEdges.get(node);
        if (edges == null) return Collections.emptySet();
        for (WorkspaceGraphModelEdge edge : edges) {
            adjacentNodes.add(edge.getChild());
        }
        return adjacentNodes;
    }

    public Collection<WorkspaceGraphModelEdge> getEdges() {
        Set<WorkspaceGraphModelEdge> allEdges = new HashSet<WorkspaceGraphModelEdge>();
        
        //All of the edged in the inbound map should also be in the outbound map 
        //so only add one of the two
        for (Set<WorkspaceGraphModelEdge> edges : inboundEdges.values()) {
            if (edges == null) continue;
            allEdges.addAll(edges);
        }
        return allEdges;
    }

    public Collection<WorkspaceGraphModelEdge> getInboundEdges(
            WabitObject node) {
        Set<WorkspaceGraphModelEdge> edges = inboundEdges.get(node);
        if (edges == null) return Collections.emptyList();
        return edges;
    }

    public Collection<WabitObject> getNodes() {
        return nodes;
    }

    public Collection<WorkspaceGraphModelEdge> getOutboundEdges(
            WabitObject node) {
        Set<WorkspaceGraphModelEdge> edges = outboundEdges.get(node);
        if (edges == null) return Collections.emptyList();
        return edges;
    }

    public WabitObject getGraphStartNode() {
        return graphStartNode;
    }
    
}