/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDest;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSource;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;


public class TopoMockUtils extends AbstractConcurrentDataBrokerTest {
    public static BierLink buildLink(String srcNode, String srcPort, String destPort, String destNode, long metric) {
        return new BierLinkBuilder()
                .setLinkSource(buildSrc(srcNode, srcPort))
                .setLinkDest(buildDest(destNode, destPort))
                .setLinkId(srcNode + srcPort + destPort + destNode)
                .setMetric(BigInteger.valueOf(metric))
                .setDelay(BigInteger.valueOf(0))
                .setLoss(BigInteger.valueOf(0))
                .build();
    }

    public static BierLink buildLinkEx(String srcNode, String srcPort, String destPort, String destNode, long metric) {

        return new BierLinkBuilder()
                .setLinkSource(buildSrc(srcNode, srcPort))
                .setLinkDest(buildDest(destNode, destPort))
                .setLinkId(srcNode + srcPort + destPort + destNode)
                .setMetric(BigInteger.valueOf(metric))
                .setDelay(BigInteger.valueOf(0))
                .setLoss(BigInteger.valueOf(0))
                .build();
    }

    public static LinkDest buildDest(String destNode, String destPort) {
        LinkDestBuilder build = new LinkDestBuilder();

        if (destNode != null) {
            build.setDestNode(destNode);
        }
        if (destPort != null) {
            build.setDestTp(destPort);
        }
        return build.build();
    }

    public static LinkSource buildSrc(String srcNode, String srcPort) {
        LinkSourceBuilder build = new LinkSourceBuilder();

        if (srcNode != null) {
            build.setSourceNode(srcNode);
        }
        if (srcPort != null) {
            build.setSourceTp(srcPort);
        }
        return build.build();
    }

    public static List<BierLink> buildLinkPair(String node, String port, String oppesitPort,
                                               String oppesiteNode,long metric) {
        List<BierLink> list = new ArrayList<>();

        list.add(buildLink(node, port, oppesitPort, oppesiteNode,metric));
        list.add(buildLink(oppesiteNode, oppesitPort, port, node,metric));

        return list;
    }

    public static List<BierLink> buildLinkPairWithMetric(String node, String port, String oppesitPort,
                                                         String oppesiteNode, long metric) {
        List<BierLink> list = new ArrayList<>();
        BierLink link = null;

        link = buildLinkEx(node, port, oppesitPort, oppesiteNode, metric);
        list.add(link);

        link = buildLinkEx(oppesiteNode, oppesitPort, port, node, metric);
        list.add(link);
        return list;
    }


/*


    /*        10
    *       R1-----R2
    *       |      |
    *  10   |      | 10
    *       |      |
    *       R3-----R4
    *          10
    */

    public static List<BierLink> buildFourNodeTopo() throws ExecutionException, InterruptedException {
        List<BierLink> links = new ArrayList<BierLink>();
        links.addAll(TopoMockUtils.buildLinkPair("1.1.1.1", "12.12.12.12", "21.21.21.21", "2.2.2.2",10));
        links.addAll(TopoMockUtils.buildLinkPair("2.2.2.2", "24.24.24.24", "42.42.42.42", "4.4.4.4",10));
        links.addAll(TopoMockUtils.buildLinkPair("1.1.1.1", "13.13.13.13", "31.31.31.31", "3.3.3.3",10));
        links.addAll(TopoMockUtils.buildLinkPair("3.3.3.3", "34.34.34.34", "43.43.43.43", "4.4.4.4",10));
        return links;
    }



    /*   10        10
 *  R1--------R2--------R3
 *  |         |         |
 *  | 10      | 10      | 10
 *  |         |         |
 *  R4--------R5--------R6
 *      10        10
 */
    public static List<BierLink> getTopo6Node() {
        List<BierLink> links = new ArrayList<>();

        links.addAll(TopoMockUtils.buildLinkPairWithMetric("1.1.1.1", "12.12.12.12", "21.21.21.21", "2.2.2.2", 10));

        links.addAll(TopoMockUtils.buildLinkPairWithMetric("1.1.1.1", "14.14.14.14", "41.41.41.41", "4.4.4.4", 10));

        links.addAll(TopoMockUtils.buildLinkPairWithMetric("2.2.2.2", "23.23.23.23", "32.32.32.32", "3.3.3.3", 10));

        links.addAll(TopoMockUtils.buildLinkPairWithMetric("3.3.3.3", "36.36.36.36", "63.63.63.63", "6.6.6.6", 10));

        links.addAll(TopoMockUtils.buildLinkPairWithMetric("6.6.6.6", "65.65.65.65", "56.56.56.56", "5.5.5.5", 10));

        links.addAll(TopoMockUtils.buildLinkPairWithMetric("4.4.4.4", "45.45.45.45", "54.54.54.54", "5.5.5.5", 10));

        links.addAll(TopoMockUtils.buildLinkPairWithMetric("2.2.2.2", "25.25.25.25", "52.52.52.52", "5.5.5.5", 10));

        return links;
    }

}