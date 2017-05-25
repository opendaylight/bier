/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.bier.pce.impl.provider.DbProvider;
import org.opendaylight.bier.pce.impl.topology.TopologyProvider;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPath;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.BferBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.bier.instance.path.output.Link;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.bier.instance.path.output.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstance;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Utils {
    public static void checkPath(List<PathLink> actualPath, String node1, String node2, String node3) {
        assertEquals(2, actualPath.size());
        assertEquals(actualPath.get(0).getLinkSource().getSourceNode(), node1);
        assertEquals(actualPath.get(0).getLinkDest().getDestNode(), node2);
        assertEquals(actualPath.get(1).getLinkSource().getSourceNode(), node2);
        assertEquals(actualPath.get(1).getLinkDest().getDestNode(), node3);
    }

    public static void checkPath(List<PathLink> actualPath, String node1, String node2) {
        assertEquals(1, actualPath.size());
        assertEquals(actualPath.get(0).getLinkSource().getSourceNode(), node1);
        assertEquals(actualPath.get(0).getLinkDest().getDestNode(), node2);
    }

    public static void checkPath(List<PathLink> actualPath, String node1, String node2, String node3, String node4) {
        assertEquals(3, actualPath.size());
        assertEquals(actualPath.get(0).getLinkSource().getSourceNode(), node1);
        assertEquals(actualPath.get(0).getLinkDest().getDestNode(), node2);
        assertEquals(actualPath.get(1).getLinkSource().getSourceNode(), node2);
        assertEquals(actualPath.get(1).getLinkDest().getDestNode(), node3);
        assertEquals(actualPath.get(2).getLinkSource().getSourceNode(), node3);
        assertEquals(actualPath.get(2).getLinkDest().getDestNode(), node4);
    }

    public static void checkPathNull(List<PathLink> actualPath) {
        assertEquals(actualPath.size(),0);
    }



    public static void writeLinkToDB(BierLink link) {
        InstanceIdentifier<BierLink> path = InstanceIdentifier.builder(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(TopologyProvider.DEFAULT_TOPO_ID_STRING))
                .child(BierLink.class, new BierLinkKey(link.getLinkId()))
                .build();
        DbProvider.getInstance().mergeData(LogicalDatastoreType.CONFIGURATION,path, link);
    }

    public static void writeLinksToDB(List<BierLink> links) {
        for (BierLink link:links) {
            writeLinkToDB(link);
        }
    }

    public static List<Bfer> build2BferInfo(String nodeId1, String nodeId2) {
        List<Bfer> bferList = new ArrayList<>();
        bferList.add(new BferBuilder().setBferNodeId(nodeId1).build());
        bferList.add(new BferBuilder().setBferNodeId(nodeId2).build());
        return bferList;
    }

    public static List<Bfer> build3BferInfo(String nodeId1, String nodeId2, String nodeId3) {
        List<Bfer> bferList = new ArrayList<>();
        bferList.add(new BferBuilder().setBferNodeId(nodeId1).build());
        bferList.add(new BferBuilder().setBferNodeId(nodeId2).build());
        bferList.add(new BferBuilder().setBferNodeId(nodeId3).build());
        return bferList;
    }

    public static void assertBierPathData(BierPath path, BierPath bierPathData) {
        assertEquals(path.getPathLink().size(),bierPathData.getPathLink().size());
        assertEquals(path.getPathMetric(),bierPathData.getPathMetric());
        for (PathLink pathLink : bierPathData.getPathLink()) {
            PathLink linkTemp = new PathLinkBuilder(pathLink).build();
            path.getPathLink().contains(linkTemp);
        }
    }


    public static BierTEInstance buildBierTeInstanceData(String channelName, String bfirNode, String bferNode1,
                                                         String bferNode2) {
        List<PathLink> pathList14 = new ArrayList<>();
        pathList14.add(buildPathLink("1.1.1.1","14.14.14.14","41.41.41.41","4.4.4.4",10));

        List<PathLink> pathList16 = new ArrayList<>();
        pathList16.add(buildPathLink("1.1.1.1","14.14.14.14","41.41.41.41","4.4.4.4",10));
        pathList16.add(buildPathLink("4.4.4.4","45.45.45.45","54.54.54.54","5.5.5.5",10));
        pathList16.add(buildPathLink("5.5.5.5","56.56.56.56","65.65.65.65","6.6.6.6",10));

        List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer> bferList = new ArrayList<>();

        bferList.add(new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferBuilder()
                .setBferNodeId(bferNode1)
                .setBierPath(new BierPathBuilder()
                        .setPathLink(pathList14)
                        .setPathMetric(Long.valueOf(10))
                        .build())
                .build());

        bferList.add(new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferBuilder()
                .setBferNodeId(bferNode2)
                .setBierPath(new BierPathBuilder()
                        .setPathLink(pathList16)
                        .setPathMetric(Long.valueOf(30))
                        .build())
                .build());

        return new BierTEInstanceBuilder()
                .setChannelName(channelName)
                .setBfirNodeId(bfirNode)
                .setBfer(bferList)
                .build();
    }

    public static PathLink buildPathLink(String sourceNode, String srcTp, String destNode, String destTp, long metric) {
        return new PathLinkBuilder(TopoMockUtils.buildLinkEx(sourceNode,srcTp,destNode,destTp,metric)).build();
    }

    public static void assertQueryLinks(String channelName, List<Link> links) {
        if (channelName.equals("channel-1")) {
            assertEquals(3,links.size());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("1.1.1.1","14.14.14.14","41.41.41.41","4.4.4.4")).build());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("4.4.4.4","45.45.45.45","54.54.54.54","5.5.5.5")).build());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("5.5.5.5","56.56.56.56","65.65.65.65","6.6.6.6")).build());
        }
        if (channelName.equals("channel-2")) {
            assertEquals(4,links.size());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("1.1.1.1","12.12.12.12","21.21.21.21","2.2.2.2")).build());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("2.2.2.2","23.23.23.23","32.32.32.32","3.3.3.3")).build());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("3.3.3.3","36.36.36.36","63.63.63.63","6.6.6.6")).build());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("2.2.2.2","25.25.25.25","52.52.52.52","5.5.5.5")).build());
        }
        if (channelName.equals("channel-3")) {
            assertEquals(2,links.size());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("2.2.2.2","25.25.25.25","52.52.52.52","5.5.5.5")).build());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("5.5.5.5","54.54.54.54","45.45.45.45","4.4.4.4")).build());
        }

    }

    private static String buildLinkId(String srcNode, String srcPort, String destPort, String destNode) {
        return srcNode + srcPort + destPort + destNode;
    }
}
