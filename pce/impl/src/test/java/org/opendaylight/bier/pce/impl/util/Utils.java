/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opendaylight.bier.pce.impl.biertepath.BierPathUnifyKey;
import org.opendaylight.bier.pce.impl.provider.DbProvider;
import org.opendaylight.bier.pce.impl.topology.PathsRecordPerSubDomain;
import org.opendaylight.bier.pce.impl.topology.TopologyProvider;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.backup.path.Path;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferKey;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPath;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.BferBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.bier.instance.path.output.Link;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.bier.instance.path.output.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.channel.through.port.output.RelatedChannel;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.channel.through.port.output.RelatedChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.BierTEData;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstance;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstanceKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
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

    public static void checkPath(List<PathLink> actualPath, String node1, String node2, String node3,
                                 String node4, String node5) {
        assertEquals(4, actualPath.size());
        assertEquals(actualPath.get(0).getLinkSource().getSourceNode(), node1);
        assertEquals(actualPath.get(0).getLinkDest().getDestNode(), node2);
        assertEquals(actualPath.get(1).getLinkSource().getSourceNode(), node2);
        assertEquals(actualPath.get(1).getLinkDest().getDestNode(), node3);
        assertEquals(actualPath.get(2).getLinkSource().getSourceNode(), node3);
        assertEquals(actualPath.get(2).getLinkDest().getDestNode(), node4);
        assertEquals(actualPath.get(3).getLinkSource().getSourceNode(), node4);
        assertEquals(actualPath.get(3).getLinkDest().getDestNode(), node5);
    }

    public static void checkLinkId(List<Link> actualLink, BierLink link1, BierLink link2, BierLink link3) {
        assertEquals(3, actualLink.size());
        assertTrue(actualLink.contains(new LinkBuilder().setLinkId(link1.getLinkId()).build()));
        assertTrue(actualLink.contains(new LinkBuilder().setLinkId(link2.getLinkId()).build()));
        assertTrue(actualLink.contains(new LinkBuilder().setLinkId(link3.getLinkId()).build()));
    }

    public static void checkLinkId(List<Link> actualLink, BierLink link1) {
        assertEquals(1, actualLink.size());
        assertTrue(actualLink.contains(new LinkBuilder().setLinkId(link1.getLinkId()).build()));
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

    public static void deleteLinkInDB(BierLink link) {
        InstanceIdentifier<BierLink> path = InstanceIdentifier.builder(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(TopologyProvider.DEFAULT_TOPO_ID_STRING))
                .child(BierLink.class, new BierLinkKey(link.getLinkId()))
                .build();
        DbProvider.getInstance().deleteData(LogicalDatastoreType.CONFIGURATION,path);
    }

    public static BierPath readBierPath(String channelName, String bferNode) {

        InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328
                .bierpath.Bfer> path = InstanceIdentifier.builder(BierTEData.class)
                .child(BierTEInstance.class, new BierTEInstanceKey(channelName))
                .child(org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328
                        .bierpath.Bfer.class, new BferKey(bferNode))
                .build();
        org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328
                .bierpath.Bfer bfer = DbProvider.getInstance().readData(LogicalDatastoreType.OPERATIONAL,path);
        return bfer.getBierPath();

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


    public static void assertBierPath(List<PathLink> pathList, BierPath bierPathData) {
        assertEquals(pathList.size(),bierPathData.getPathLink().size());
        for (PathLink pathLink : bierPathData.getPathLink()) {
            PathLink linkTemp = new PathLinkBuilder(pathLink).build();
            pathList.contains(linkTemp);
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

    public static void assertQueryLinks(String channelName, List<Link> links, boolean isUpdate) {
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
            if (isUpdate) {
                assertEquals(4, links.size());
                links.contains(new LinkBuilder()
                        .setLinkId(buildLinkId("1.1.1.1", "14.14.14.14", "41.41.41.41", "4.4.4.4")).build());
                links.contains(new LinkBuilder()
                        .setLinkId(buildLinkId("4.4.4.4", "45.45.45.45", "54.54.54.54", "5.5.5.5")).build());
                links.contains(new LinkBuilder()
                        .setLinkId(buildLinkId("5.5.5.5", "56.56.56.56", "65.65.65.65", "6.6.6.6")).build());
                links.contains(new LinkBuilder()
                        .setLinkId(buildLinkId("6.6.6.6", "63.63.63.63", "36.36.36.36", "3.3.3.3")).build());
            } else {
                assertEquals(4, links.size());
                links.contains(new LinkBuilder()
                        .setLinkId(buildLinkId("1.1.1.1", "12.12.12.12", "21.21.21.21", "2.2.2.2")).build());
                links.contains(new LinkBuilder()
                        .setLinkId(buildLinkId("2.2.2.2", "23.23.23.23", "32.32.32.32", "3.3.3.3")).build());
                links.contains(new LinkBuilder()
                        .setLinkId(buildLinkId("3.3.3.3", "36.36.36.36", "63.63.63.63", "6.6.6.6")).build());
                links.contains(new LinkBuilder()
                        .setLinkId(buildLinkId("2.2.2.2", "25.25.25.25", "52.52.52.52", "5.5.5.5")).build());
            }
        }
        if (channelName.equals("channel-3")) {
            assertEquals(2,links.size());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("2.2.2.2","25.25.25.25","52.52.52.52","5.5.5.5")).build());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("5.5.5.5","54.54.54.54","45.45.45.45","4.4.4.4")).build());
        }
        if (channelName.equals("channel-4")) {
            assertEquals(4,links.size());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("1.1.1.1","14.14.14.14","41.41.41.41","4.4.4.4")).build());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("4.4.4.4","45.45.45.45","54.54.54.54","5.5.5.5")).build());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("5.5.5.5","56.56.56.56","65.65.65.65","6.6.6.6")).build());
            links.contains(new LinkBuilder()
                    .setLinkId(buildLinkId("6.6.6.6","63.63.63.63","36.36.36.36","3.3.3.3")).build());
        }
    }

    public static String buildLinkId(String srcNode, String srcPort, String destPort, String destNode) {
        return srcNode + srcPort + destPort + destNode;
    }

    public static void assertChannelInfo(String channelName, String bfirNode, List<RelatedChannel> channels) {
        assertEquals(1,channels.size());
        assertTrue(channels.contains(new RelatedChannelBuilder().setChannelName(channelName)
                .setBfir(bfirNode).build()));
    }


    public static void assertPathsPerSubDomain(SubDomainId subDomainId, int channelNum) {
        Set<BierPathUnifyKey> pathSet = PathsRecordPerSubDomain.getInstance()
                .getBierPathSetBySubDomainId(subDomainId);

        if (subDomainId.getValue() == 1) {
            BierPathUnifyKey pathKey114 = new BierPathUnifyKey("channel-1",new SubDomainId(1),"1.1.1.1","4.4.4.4");
            BierPathUnifyKey pathKey115 = new BierPathUnifyKey("channel-1",new SubDomainId(1),"1.1.1.1","5.5.5.5");
            BierPathUnifyKey pathKey116 = new BierPathUnifyKey("channel-1",new SubDomainId(1),"1.1.1.1","6.6.6.6");
            BierPathUnifyKey pathKey213 = new BierPathUnifyKey("channel-2",new SubDomainId(1),"1.1.1.1","3.3.3.3");
            BierPathUnifyKey pathKey215 = new BierPathUnifyKey("channel-2",new SubDomainId(1),"1.1.1.1","5.5.5.5");
            BierPathUnifyKey pathKey216 = new BierPathUnifyKey("channel-2",new SubDomainId(1),"1.1.1.1","6.6.6.6");
            BierPathUnifyKey pathKey324 = new BierPathUnifyKey("channel-3",new SubDomainId(1),"2.2.2.2","4.4.4.4");
            BierPathUnifyKey pathKey325 = new BierPathUnifyKey("channel-3",new SubDomainId(1),"2.2.2.2","5.5.5.5");

            if (channelNum == 1) {
                assertEquals(3,pathSet.size());
                assertTrue(pathSet.contains(pathKey213));
                assertTrue(pathSet.contains(pathKey215));
                assertTrue(pathSet.contains(pathKey216));
            } else {
                assertEquals(8,pathSet.size());
                assertTrue(pathSet.contains(pathKey114));
                assertTrue(pathSet.contains(pathKey115));
                assertTrue(pathSet.contains(pathKey116));
                assertTrue(pathSet.contains(pathKey213));
                assertTrue(pathSet.contains(pathKey215));
                assertTrue(pathSet.contains(pathKey216));
                assertTrue(pathSet.contains(pathKey324));
                assertTrue(pathSet.contains(pathKey325));
            }
        } else {
            assertEquals(3,pathSet.size());
            BierPathUnifyKey pathKey13 = new BierPathUnifyKey("channel-4",new SubDomainId(2),"1.1.1.1","3.3.3.3");
            BierPathUnifyKey pathKey15 = new BierPathUnifyKey("channel-4",new SubDomainId(2),"1.1.1.1","5.5.5.5");
            BierPathUnifyKey pathKey16 = new BierPathUnifyKey("channel-4",new SubDomainId(2),"1.1.1.1","6.6.6.6");
            assertTrue(pathSet.contains(pathKey13));
            assertTrue(pathSet.contains(pathKey15));
            assertTrue(pathSet.contains(pathKey16));
        }
    }

    public static List<PathLink> transPath(List<Path> paths) {
        List<PathLink> pathLinks = new ArrayList<>();
        for (Path path : paths) {
            pathLinks.add(new PathLinkBuilder(path).build());
        }

        return pathLinks;
    }

    public static List<PathLink> transPath(LinkedList<BierLink> paths) {
        List<PathLink> pathLinks = new ArrayList<>();
        for (BierLink path : paths) {
            pathLinks.add(new PathLinkBuilder(path).build());
        }

        return pathLinks;
    }
}
