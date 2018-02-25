/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.pce.impl.pathcore;
import edu.uci.ics.jung.graph.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opendaylight.bier.pce.impl.biertepath.LspGetPath;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;

public class ContrainedOptimalPath extends OptimalPath<String,BierLink> {
    private String tailNode;
    private List<BierLink> excludePath;

    public ContrainedOptimalPath(String bfirNodeId, String tailNode, Graph<String, BierLink> topoGraph,
                                 ICalcStrategy<String, BierLink> strategy) {
        super(bfirNodeId,topoGraph,strategy);
        this.tailNode = tailNode;
        setSourceData(new SourceDataExImpl(bfirNodeId));
    }

/*    public void setEdgeMetric(ITransformer<BierLink> edgeMetric) {
        this.edgeMetric = edgeMetric;
    }*/

    public void setExcludePath(List<BierLink> excludePath) {
        this.excludePath = excludePath;
    }

    public List<BierLink> calcCspf(String sourceNode) {
        calcSpt();
        Map<String, List<BierLink>> incomingMap = getIncomingEdgeMap();
        if (!incomingMap.containsKey(tailNode)) {
            return new ArrayList<>();
        }
        return LspGetPath.getPath(incomingMap, sourceNode, tailNode);
    }

    protected class SourceDataExImpl extends SourceDataImpl {

        public SourceDataExImpl(String sourceNode) {
            super(sourceNode);
        }

        public void add2TentList(String localNode, String neighborNode, BierLink incomingEdge) {
            if (isLinkExcluded(incomingEdge)) {
                return;
            }
/*
            if ((destNodeInport != null)
                    && (neighborNode.equals(destNodeInport.getNode()))
                    && (!isMatchDestInport(incomingEdge))) {
                return;
            }*/

            super.add2TentList(localNode, neighborNode, incomingEdge);
            //addBestEdge2TentDelay(localNode,neighborNode,incomingEdge);

        }

        private boolean isLinkExcluded(BierLink link) {
            for (BierLink excludeLink : excludePath) {
                if (excludeLink.getLinkDest().equals(link.getLinkDest())
                        && excludeLink.getLinkSource().equals(link.getLinkSource())) {
                    return true;
                }
            }
            return false;
        }
/*
        private void addBestEdge2TentDelay(NodeId localNode, NodeId neighborNode, Link incomingEdge) {
            if ((maxDelay == 4294967295L) && (reverseMaxDelay == 4294967295L )) {
                return;
            }

            //we choose positive least delay link to TentDelayMap
            long lessDelay = maxDelay;
            List<Link> temList = new LinkedList<>();
            List<Link> edgeList = tentIncomingEdgesMap.get(neighborNode);
            if (edgeList.contains(incomingEdge)) {
                for (Link edge : edgeList) {
                    long incomingEdgeDelay = ComUtility.getLinkDelay(edge);
                    NodeId preNode = edge.getSource().getSourceNode();

                    PathDelay preLocNodeDelay = (PathDelay) pathDelayMap.get(preNode);
                    long preNodePositiveDelay = preLocNodeDelay.getPostiveDelay();
                    if (incomingEdgeDelay + preNodePositiveDelay < lessDelay) {
                        lessDelay = incomingEdgeDelay + preNodePositiveDelay;
                        temList.clear();
                        temList.add(edge);
                    } else if (incomingEdgeDelay + preNodePositiveDelay == lessDelay) {
                        temList.add(edge);
                    }
                }
                if (!temList.isEmpty()) {
                    edgeList.clear();
                    edgeList.addAll(temList);
                }
                if (edgeList.contains(incomingEdge)) {
                    PathDelay locNodeDelay = (PathDelay) pathDelayMap.get(localNode);
                    long reverseLinkDelay = ComUtility.getReverseLinkDelay(graph, edgeList.get(0));
                    addTentDelay(neighborNode, lessDelay, locNodeDelay.getReverseDelay() + reverseLinkDelay);
                }
            }
        }

        private void addTentDelay(NodeId node, long delay, long reverseDelay) {
            PathDelay pathTentDelay = new PathDelay(delay,reverseDelay);

            tentDelayMap.put(node,pathTentDelay);
        }*/

    }
}
