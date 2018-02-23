/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.oam.impl;

import com.google.common.util.concurrent.Futures;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierForwardingType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.start.echo.request.input.TargetNodeIds;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringInput;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringOutput;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.BitstringInfo;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.BitstringInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.PathInfo;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.PathInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDest;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSource;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobalBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.ReplyMode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.BierTeBpInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.BierTeBpInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.Bitstring;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.BitstringBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.FecStackType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.FecStackTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.bitstring.Bitpositions;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.bitstring.BitpositionsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.fec.stack.type.FecStackInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.fec.stack.type.FecStackInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.fec.stack.type.fec.stack.info.fec.stack.type.ConnectedBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.fec.stack.type.fec.stack.info.fec.stack.type.LocalDecapBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class BierBasicMockUtils extends AbstractConcurrentDataBrokerTest {
    public static final String DEFAULT_TOPO = "example-linkstate-topology";

    public static void buildBierTopoAndChannel() {
        buildBierTopo();
        buildChannels();
    }

    private static void buildChannels() {
        List<String> egressNodes1 = new ArrayList<>();
        egressNodes1.add("node2");
        egressNodes1.add("node3");
        List<Integer> bfers1 = new ArrayList<>();
        bfers1.add(2);
        bfers1.add(33);
        List<Channel> channels = new ArrayList<>();
        channels.add(buildChannel("c1",1,1,"10.1.1.1","224.1.1.1","node1",egressNodes1,11,bfers1,
                BierForwardingType.Bier));
        channels.add(buildChannel("c2",1,1,"20.1.1.1","225.1.1.1","node4",egressNodes1,44,bfers1,
                BierForwardingType.Bier));
        channels.add(buildChannel("c5",1,1,"50.1.1.1","224.1.1.5","node4",egressNodes1,44,bfers1,
                BierForwardingType.BierTe));
        List<String> egressNodes2 = new ArrayList<>(egressNodes1);
        egressNodes2.add("node5");
        List<Integer> bfers2 = new ArrayList<>(bfers1);
        bfers2.add(55);
        channels.add(buildChannel("c3",1,1,"30.1.1.1","226.1.1.1","node1",egressNodes2,11,bfers2,
                BierForwardingType.Bier));
        channels.add(buildChannel("c4",1,1,"40.1.1.1","227.1.1.1","node1",egressNodes2,11,bfers2,
                BierForwardingType.Bier));
        channels.add(buildChannel("c6",1,1,"60.1.1.1","224.1.1.6","node1",egressNodes2,11,bfers2,
                BierForwardingType.BierTe));
        BierChannel bierChannel = new BierChannelBuilder()
                .setTopologyId(DEFAULT_TOPO)
                .setChannel(channels)
                .build();
        DbProvider.getInstance().mergeData(LogicalDatastoreType.CONFIGURATION,buildBierChannelPath(),bierChannel);
    }

    private static void buildBierTopo() {
        List<BierNode> bierNodeList = new ArrayList<>();
        BierNode bierNode1 = buildBierNodeInfo("node1",1,1,new BfrId(1),new BfrId(11),"1.1.1.1/32",null);
        BierNode bierNode2 = buildBierNodeInfo("node2",1,1,new BfrId(2),null,null,"fe80::7009:fe25:8170:36af/64");
        BierNode bierNode3 = buildBierNodeInfo("node3",1,1,null,new BfrId(33),"3.3.3.3/32",null);
        BierNode bierNode4 = buildBierNodeInfo("node4",1,1,new BfrId(4),new BfrId(44),"4.4.4.4/32",null);
        BierNode bierNode5 = buildBierNodeInfo("node5",1,1,new BfrId(5),new BfrId(55),"5.5.5.5/32",null);
        BierNode bierNode6 = buildBierNodeInfo("node6",1,1,new BfrId(6),new BfrId(66),"6.6.6.6/32",null);
        BierNode bierNode7 = buildBierNodeInfo("node7",1,1,new BfrId(7),new BfrId(77),"7.7.7.7/32",null);
        bierNodeList.add(bierNode1);
        bierNodeList.add(bierNode2);
        bierNodeList.add(bierNode3);
        bierNodeList.add(bierNode4);
        bierNodeList.add(bierNode5);
        bierNodeList.add(bierNode6);
        bierNodeList.add(bierNode7);
        BierTopology bierTopology = new BierTopologyBuilder()
                .setTopologyId(DEFAULT_TOPO)
                .setBierNode(bierNodeList)
                .build();
        DbProvider.getInstance().mergeData(LogicalDatastoreType.CONFIGURATION,buildBierTopologyPath(),bierTopology);
    }

    private static BierNode buildBierNodeInfo(String node, Integer domainId, Integer subDomainID,
                                              BfrId globalBfrId, BfrId subDomainBfrId, String ipv4Prefix,
                                              String ipv6Prefix) {
        List<Domain> domainList = new ArrayList<>();
        List<SubDomain> subDomainList = new ArrayList<>();
        subDomainList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(subDomainID))
                .setBfrId(subDomainBfrId)
                .build());

        domainList.add(new DomainBuilder()
                .setDomainId(new DomainId(domainId))
                .setBierGlobal(new BierGlobalBuilder()
                        .setSubDomain(subDomainList)
                        .setBfrId(globalBfrId)
                        .setIpv4BfrPrefix(ipv4Prefix == null ? null : new Ipv4Prefix(ipv4Prefix))
                        .setIpv6BfrPrefix(ipv6Prefix == null ? null : new Ipv6Prefix(ipv6Prefix))
                        .build())
                .build());
        return new BierNodeBuilder()
                .setNodeId(node)
                .setBierNodeParams(new BierNodeParamsBuilder()
                        .setDomain(domainList)
                        .build())
                .build();
    }

    private static InstanceIdentifier<BierTopology> buildBierTopologyPath() {
        return InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(DEFAULT_TOPO));
    }

    private static InstanceIdentifier<BierChannel> buildBierChannelPath() {
        return InstanceIdentifier.create(BierNetworkChannel.class)
                .child(BierChannel.class, new BierChannelKey(DEFAULT_TOPO));
    }

    private static Channel buildChannel(String channelName, Integer domainId, Integer subDomainId, String srcIp,
                                        String groupIp,String ingressNode,List<String> egressNodes,Integer bfirId,
                                        List<Integer> bferIds, BierForwardingType type) {
        List<EgressNode> egressNodeList = new ArrayList<>();
        List<RcvTp> rcvTps = new ArrayList<>();
        rcvTps.add(new RcvTpBuilder().setTp("tp2").build());
        rcvTps.add(new RcvTpBuilder().setTp("tp3").build());
        for (String egressNode : egressNodes) {
            egressNodeList.add(new EgressNodeBuilder()
                    .setNodeId(egressNode)
                    .setRcvTp(rcvTps)
                    .setEgressBfrId(new BfrId(bferIds.get(egressNodes.indexOf(egressNode))))
                    .build());
        }
        ChannelBuilder channelBuilder = new ChannelBuilder();
        channelBuilder.setIngressNode(ingressNode);
        channelBuilder.setIngressBfrId(new BfrId(bfirId));
        channelBuilder.setName(channelName);
        channelBuilder.setBierForwardingType(type);
        channelBuilder.setSrcTp("tp1");
        channelBuilder.setDomainId(new DomainId(domainId));
        channelBuilder.setSubDomainId(new SubDomainId(subDomainId));
        channelBuilder.setSrcIp(new IpAddress(new Ipv4Address(srcIp)));
        channelBuilder.setDstGroup(new IpAddress(new Ipv4Address(groupIp)));
        channelBuilder.setSourceWildcard((short) 24);
        channelBuilder.setGroupWildcard((short) 30);
        channelBuilder.setEgressNode(egressNodeList);
        return channelBuilder.build();
    }

    public static List<String> getEgressNodes(String channelname) {
        List<String> egressNodes1 = new ArrayList<>();
        egressNodes1.add("node2");
        egressNodes1.add("node3");
        List<String> egressNodes2 = new ArrayList<>(egressNodes1);
        egressNodes2.add("node5");
        if (channelname.equals("c3") || channelname.equals("c4") || channelname.equals("c6")) {
            return egressNodes2;
        }
        if (channelname.equals("c1") || channelname.equals("c2") || channelname.equals("c5")) {
            return egressNodes1;
        }
        return null;
    }

    public static List<BfrId> getBfers(String channelname) {
        List<BfrId> bfers1 = new ArrayList<>();
        bfers1.add(new BfrId(2));
        bfers1.add(new BfrId(33));
        List<BfrId> bfers2 = new ArrayList<>(bfers1);
        bfers2.add(new BfrId(55));
        if (channelname.equals("c3") || channelname.equals("c4") || channelname.equals("c6")) {
            return bfers2;
        }
        if (channelname.equals("c1") || channelname.equals("c2") || channelname.equals("c5")) {
            return bfers1;
        }
        return null;
    }

    public static IpAddress getIpAddress(BfrId bierBfrid) {
        if (bierBfrid.getValue() == 11) {
            return new IpAddress(new Ipv4Address("1.1.1.1"));
        }
        if (bierBfrid.getValue() == 2) {
            return new IpAddress(new Ipv6Address("fe80::7009:fe25:8170:36af"));
        }
        if (bierBfrid.getValue() == 33) {
            return new IpAddress(new Ipv4Address("3.3.3.3"));
        }
        if (bierBfrid.getValue() == 44) {
            return new IpAddress(new Ipv4Address("4.4.4.4"));
        }
        if (bierBfrid.getValue() == 55) {
            return new IpAddress(new Ipv4Address("5.5.5.5"));
        }
        if (bierBfrid.getValue() == 66) {
            return new IpAddress(new Ipv4Address("6.6.6.6"));
        }
        if (bierBfrid.getValue() == 77) {
            return new IpAddress(new Ipv4Address("7.7.7.7"));
        }
        return null;
    }


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


}
