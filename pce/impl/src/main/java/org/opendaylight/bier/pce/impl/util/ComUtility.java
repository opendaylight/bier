/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.util;

import edu.uci.ics.jung.graph.Graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComUtility {
    public static final long DEFAULT_METRIC = 0x0a;
    private static final Logger LOG = LoggerFactory.getLogger(ComUtility.class);

    private ComUtility() {
    }

    public static Double getLinkMetric(BierLink link) {

        if (link.getMetric() == null) {
            return Double.valueOf(DEFAULT_METRIC);
        } else {
            return link.getMetric().doubleValue();
        }
    }


    public static String getLinkString(BierLink link) {
        return link.getLinkSource().getSourceNode() + ":" + link.getLinkSource().getSourceTp()
                + "-----" + link.getLinkDest().getDestNode() + ":" + link.getLinkDest().getDestTp();
    }


    public static List<BierLink> getLinkInGraph(Graph<String, BierLink> graph, String sourceNode, String sourceTp,
                                            String destNode, String destTp) {
        List<BierLink> linksFound = new LinkedList<>();
        if ((!graph.containsVertex(sourceNode))
                || (!graph.containsVertex(destNode))) {
            LOG.error("source:" + sourceNode.toString() + " dest:" + destNode.toString());
            return linksFound;
        }

        Collection<BierLink> links = graph.findEdgeSet(sourceNode, destNode);
        if ((links == null) || (links.isEmpty())) {
            return linksFound;
        }

        for (BierLink linkTemp : links) {
            if (linkTemp.getLinkDest().getDestTp().equals(destTp)
                    && (linkTemp.getLinkSource().getSourceTp().equals(sourceTp))) {
                linksFound.add(linkTemp);
            }
        }

        return linksFound;
    }

    public static List<BierLink> getOtherLink(Graph<String, BierLink> graph, BierLink link) {
        List<BierLink> linksList = new LinkedList<>();
        if ((!graph.containsVertex(link.getLinkDest().getDestNode()))
                || (!graph.containsVertex(link.getLinkSource().getSourceNode()))) {
            LOG.error(link.toString());
            return linksList;
        }

        Collection<BierLink> links = graph.findEdgeSet(link.getLinkSource().getSourceNode(),
                link.getLinkDest().getDestNode());
        if ((links == null) || (links.isEmpty())) {
            return linksList;
        }

        for (BierLink otherLink : links) {
            if (otherLink.getLinkDest().getDestTp().equals(link.getLinkDest().getDestTp())
                    && (otherLink.getLinkSource().getSourceTp().equals(link.getLinkSource().getSourceTp()))) {
                linksList.add(otherLink);
            }
        }

        return linksList;
    }

    public static List<BierLink> getReverseLink(Graph<String, BierLink> graph, BierLink link) {
        List<BierLink> reverseLinks = getLinkInGraph(graph,
                link.getLinkDest().getDestNode(),
                link.getLinkDest().getDestTp(),
                link.getLinkSource().getSourceNode(),
                link.getLinkSource().getSourceTp());

        if ((reverseLinks != null) && (!reverseLinks.isEmpty())) {
            return reverseLinks;
        }

        Collection<BierLink> links = graph.findEdgeSet(link.getLinkDest().getDestNode(),
                link.getLinkSource().getSourceNode());
        if ((links == null) || (links.isEmpty())) {
            return null;
        }
        return null;
    }


    public static List<PathLink> transform2PathLink(List<BierLink> links) {
        LinkedList<PathLink> path = new LinkedList<>();
        if (links == null) {
            return path;
        }

        for (BierLink link : links) {
            path.addLast(new PathLinkBuilder(link).build());
        }

        return path;
    }


}
