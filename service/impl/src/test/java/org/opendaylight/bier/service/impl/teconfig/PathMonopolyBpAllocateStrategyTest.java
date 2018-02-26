/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.service.impl.teconfig;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.bier.service.impl.allocatebp.PathMonopolyBPAllocateStrategy;
import org.opendaylight.bierman.impl.RpcUtil;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.AddSubdomainBslSiInput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.AddSubdomainBslSiOutput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.AddSubdomainBslSiOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.BierBpAllocateParamsConfigService;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.DeleteSubdomainBslSiInput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.DeleteSubdomainBslSiOutput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.DeleteSubdomainBslSiOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.QuerySubdomainBslSiInput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.QuerySubdomainBslSiOutput;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.QuerySubdomainBslSiOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.query.subdomain.bsl.si.output.SiOfModel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierForwardingType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierTeLabelRangeSize;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.bier.domain.BierSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.bier.domain.BierSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.bier.domain.BierSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeLableRange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeLableRangeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBpKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathMonopolyBpAllocateStrategyTest extends AbstractDataBrokerTest {
    private static final Logger LOG = LoggerFactory.getLogger(TeChannelChangeListenerTest.class);
    private PathMonopolyBPAllocateStrategy bpAllocateStrategy;
    private BierBpAllocateServiceMock bierBpAllocateServiceMock;
    private RpcConsumerRegistry rpcConsumerRegistry;

    public void setUp() {

        rpcConsumerRegistry = mock(RpcConsumerRegistry.class);
        bierBpAllocateServiceMock = new BierBpAllocateServiceMock();
        when(rpcConsumerRegistry.getRpcService(BierBpAllocateParamsConfigService.class))
                .thenReturn(bierBpAllocateServiceMock);

        bpAllocateStrategy = PathMonopolyBPAllocateStrategy.getInstance();
        bpAllocateStrategy.setDataBroker(getDataBroker());
        bpAllocateStrategy.setRpcConsumerRegistry(rpcConsumerRegistry);

        BierTopologyBuilder topologyBuilder = new BierTopologyBuilder();
        List<BierNode> bierNodeList = new ArrayList<>();
        topologyBuilder.setTopologyId("example-linkstate-topology");

        BierNode bierNode1 = constructBierNode("001", "Node1", "001", 35, 38,
                constructBierTerminationPointList("001", 1L, "002", 2L, "003", 3L),
                null,constructBierTeLableRange(100,100));

        BierNode bierNode2 = constructBierNode("002", "Node2", "001", 35, 38,
                constructBierTerminationPointList("004", 4L, "005", 5L, "006", 6L),
                null,constructBierTeLableRange(200,100));

        BierNode bierNode3 = constructBierNode("003", "Node3", "001", 35, 38,
                constructBierTerminationPointList("007", 7L, "008", 8L, "009", 9L),
                null,constructBierTeLableRange(300,100));

        BierNode bierNode4 = constructBierNode("004", "Node4", "001", 35, 38,
                constructBierTerminationPointList("010", 10L, "011", 11L, "012", 12L),
                null,constructBierTeLableRange(400,100));

        BierNode bierNode5 = constructBierNode("005", "Node5", "001", 35, 38,
                constructBierTerminationPointList("013", 13L, "014", 14L, "015", 15L),
                null,constructBierTeLableRange(500,100));

        BierNode bierNode6 = constructBierNode("006", "Node6", "001", 35, 38,
                constructBierTerminationPointList("016", 16L, "017", 17L, "018", 18L),
                null,constructBierTeLableRange(600,100));

        bierNodeList.add(bierNode1);
        bierNodeList.add(bierNode2);
        bierNodeList.add(bierNode3);
        bierNodeList.add(bierNode4);
        bierNodeList.add(bierNode5);
        bierNodeList.add(bierNode6);
        topologyBuilder.setBierNode(bierNodeList);
        topologyBuilder.setBierDomain(constructBierDomain(1,1));

        addTopologyToDataStore(topologyBuilder.build());
    }

    @Test
    public void simpleBpAllocateAndRecycleTest() {
        setUp();

        //Allocate Bp Test
        List<EgressNode> egressNodeList1 = new ArrayList<>();
        egressNodeList1.add(constructEgressNode("003", 4, "009"));
        egressNodeList1.add(constructEgressNode("004", 5, "012"));
        Channel channel = constructTeChannel("channel-1","10.84.220.5","102.112.20.40",1,1,
                (short)12,(short)12,"001", 2, "003", egressNodeList1, BierForwardingType.BierTe);
        List<String> bferNodeIdList = new ArrayList<>();
        bferNodeIdList.add("003");
        bferNodeIdList.add("004");
        List<Bfer> bferList = getCreatBferListFromInput("001", bferNodeIdList);
        bpAllocateStrategy.allocateBPs(channel, bferList);
        assertAllocateBpResult();

        //Recycle Bp Test
        List<EgressNode> egressNodeList2 = new ArrayList<>();
        egressNodeList2.add(constructEgressNode("003", 4, "009"));
        channel = constructTeChannel("channel-1","10.84.220.5","102.112.20.40",1,1,
                (short)12,(short)12,"001", 2, "003", egressNodeList2, BierForwardingType.BierTe);
        bferNodeIdList.remove("003");
        bferList = getCreatBferListFromInput("001", bferNodeIdList);
        bpAllocateStrategy.recycleBPs(channel, bferList);
        assertRecycleBpResult();

    }

    private void assertAllocateBpResult() {
        TeBp bp11 = getTeBp("001",1 ,"003");
        Assert.assertEquals(bp11.getBitposition().intValue(), 1);
        TeBp bp12 = getTeBp("001",2 ,"003");
        Assert.assertEquals(bp12.getBitposition().intValue(), 1);
        TeBp bp13 = getTeBp("001",1,"002");
        Assert.assertEquals(bp13.getBitposition().intValue(), 2);
        TeBp bp14 = getTeBp("001",2,"001");
        Assert.assertEquals(bp14.getBitposition().intValue(), 2);

        TeBp bp21 = getTeBp("002",1 ,"004");
        Assert.assertEquals(bp21.getBitposition().intValue(), 3);
        TeBp bp22 = getTeBp("002",1 ,"005");
        Assert.assertEquals(bp22.getBitposition().intValue(), 4);

        TeBp bp31 = getTeBp("003",1 ,"007");
        Assert.assertEquals(bp31.getBitposition().intValue(), 5);
        TeBp bp32 = getTeBp("003",1 ,"009");
        Assert.assertEquals(bp32.getBitposition().intValue(), 6);

        TeBp bp61 = getTeBp("006",2 ,"017");
        Assert.assertEquals(bp61.getBitposition().intValue(), 3);
        TeBp bp62 = getTeBp("006",2 ,"016");
        Assert.assertEquals(bp62.getBitposition().intValue(), 4);

        TeBp bp51 = getTeBp("005",2 ,"014");
        Assert.assertEquals(bp51.getBitposition().intValue(), 5);
        TeBp bp52 = getTeBp("005",2 ,"013");
        Assert.assertEquals(bp52.getBitposition().intValue(), 6);

        TeBp bp41 = getTeBp("004",2 ,"011");
        Assert.assertEquals(bp41.getBitposition().intValue(), 7);
        TeBp bp42 = getTeBp("004",2 ,"012");
        Assert.assertEquals(bp42.getBitposition().intValue(), 8);
    }

    private void assertRecycleBpResult() {
        TeBp bp12 = getTeBp("001",2 ,"003");
        Assert.assertNull(bp12);
        TeBp bp14 = getTeBp("001",2,"001");
        Assert.assertNull(bp14);
        TeBp bp61 = getTeBp("006",2 ,"017");
        Assert.assertNull(bp61);
        TeBp bp62 = getTeBp("006",2 ,"016");
        Assert.assertNull(bp62);
        TeBp bp51 = getTeBp("005",2 ,"014");
        Assert.assertNull(bp51);
        TeBp bp52 = getTeBp("005",2 ,"013");
        Assert.assertNull(bp52);
        TeBp bp41 = getTeBp("004",2 ,"011");
        Assert.assertNull(bp41);
        TeBp bp42 = getTeBp("004",2 ,"012");
        Assert.assertNull(bp42);
    }

    private List<BierTerminationPoint> constructBierTerminationPointList(String tpId1, Long tpIndex1, String tpId2,
                                                                         Long tpIndex2, String tpId3, Long tpIndex3) {
        BierTerminationPointBuilder bierTerminationPointBuilder1 = new BierTerminationPointBuilder();
        bierTerminationPointBuilder1.setTpId(tpId1);
        bierTerminationPointBuilder1.setTpIndex(tpIndex1);
        BierTerminationPointBuilder bierTerminationPointBuilder2 = new BierTerminationPointBuilder();
        bierTerminationPointBuilder2.setTpId(tpId2);
        bierTerminationPointBuilder2.setTpIndex(tpIndex2);
        BierTerminationPointBuilder bierTerminationPointBuilder3 = new BierTerminationPointBuilder();
        bierTerminationPointBuilder3.setTpId(tpId3);
        bierTerminationPointBuilder3.setTpIndex(tpIndex3);
        List<BierTerminationPoint> bierTerminationPointList = new ArrayList<>();
        bierTerminationPointList.add(bierTerminationPointBuilder1.build());
        bierTerminationPointList.add(bierTerminationPointBuilder2.build());
        bierTerminationPointList.add(bierTerminationPointBuilder3.build());
        return bierTerminationPointList;
    }

    private List<BierDomain> constructBierDomain(int domainId, int subdomainId) {
        BierSubDomainBuilder bierSubDomainBuilder = new BierSubDomainBuilder();
        bierSubDomainBuilder.setSubDomainId(new SubDomainId(subdomainId));
        bierSubDomainBuilder.setKey(new BierSubDomainKey(new SubDomainId(subdomainId)));
        BierDomainBuilder bierDomainBuilder = new BierDomainBuilder();
        List<BierSubDomain> subDomains = new ArrayList<>();
        subDomains.add(bierSubDomainBuilder.build());
        bierDomainBuilder.setBierSubDomain(subDomains);
        bierDomainBuilder.setDomainId(new DomainId(domainId));
        bierDomainBuilder.setKey(new BierDomainKey(new DomainId(domainId)));
        List<BierDomain> domains = new ArrayList<>();
        domains.add(bierDomainBuilder.build());
        return domains;
    }

    private BierNode constructBierNode(String nodeId, String name, String routerId, int latitude,
                                       int longitude, List<BierTerminationPoint> bierTerminationPointList,
                                       BierTeNodeParams bierTeNodeParams,BierTeLableRange bierTeLabelRange) {
        BierNodeBuilder bierNodeBuilder = new BierNodeBuilder();

        bierNodeBuilder.setNodeId(nodeId);
        bierNodeBuilder.setName(name);
        bierNodeBuilder.setRouterId(routerId);
        bierNodeBuilder.setLatitude(BigInteger.valueOf(latitude));
        bierNodeBuilder.setLongitude(BigInteger.valueOf(longitude));
        bierNodeBuilder.setBierTerminationPoint(bierTerminationPointList);
        bierNodeBuilder.setBierTeNodeParams(bierTeNodeParams);
        bierNodeBuilder.setBierTeLableRange(bierTeLabelRange);
        return bierNodeBuilder.build();
    }

    private BierTeLableRange constructBierTeLableRange(long labelBase, long labelRangeSize) {
        BierTeLableRangeBuilder teLableRangeBuilder = new BierTeLableRangeBuilder();
        MplsLabel mplsLabelBase = new MplsLabel(labelBase);
        teLableRangeBuilder.setLabelBase(mplsLabelBase);
        BierTeLabelRangeSize bierTeLabelRangeSize = new BierTeLabelRangeSize(labelRangeSize);
        teLableRangeBuilder.setLabelRangeSize(bierTeLabelRangeSize);
        return teLableRangeBuilder.build();
    }


    private void addTopologyToDataStore(BierTopology bierTopology) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<BierTopology> topologyPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey("example-linkstate-topology"));
        tx.put(LogicalDatastoreType.CONFIGURATION, topologyPath, bierTopology, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    private EgressNode constructEgressNode(String nodeId, int bfrId, String rcvTp) {
        EgressNodeBuilder builder = new EgressNodeBuilder();
        builder.setNodeId(nodeId);
        builder.setEgressBfrId(new BfrId(bfrId));
        RcvTpBuilder tpBuilder = new RcvTpBuilder();
        tpBuilder.setTp(rcvTp);
        List<RcvTp> list = new ArrayList<>();
        list.add(tpBuilder.build());
        builder.setRcvTp(list);
        return builder.build();
    }

    private Channel constructTeChannel(String name, String srcIp, String dstGroup, int domainId, int subDomainId,
                                       short srcWild, short groupWild, String ingress, int bfrId, String srcTp,
                                       List<EgressNode> egressList, BierForwardingType type) {
        ChannelBuilder builder = new ChannelBuilder();
        builder.setName(name);
        builder.setSrcIp(new IpAddress(new Ipv4Address(srcIp)));
        builder.setDstGroup(new IpAddress(new Ipv4Address(dstGroup)));
        builder.setDomainId(new DomainId(domainId));
        builder.setSubDomainId(new SubDomainId(subDomainId));
        builder.setSourceWildcard(srcWild);
        builder.setGroupWildcard(groupWild);
        builder.setIngressNode(ingress);
        builder.setIngressBfrId(new BfrId(bfrId));
        builder.setSrcTp(srcTp);
        builder.setEgressNode(egressList);
        builder.setBierForwardingType(type);
        return builder.build();
    }


    private List<Bfer> getCreatBferListFromInput(String bfirId,
                                                 List<String> bferNodeIdList) {
        List<Bfer> bferList = new ArrayList<>();
        for (String bferNodeId : bferNodeIdList) {
            BferBuilder bferBuilder = new BferBuilder();
            bferBuilder.setBferNodeId(bferNodeId);
            bferBuilder.setBierPath(setBierPath(bfirId, bferNodeId));
            bferList.add(bferBuilder.build());
        }
        return bferList;
    }

    private org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPath setBierPath(String bfirId,
                                                                                                   String bferId) {
        BierPathBuilder builder = new BierPathBuilder();
        if (bfirId.equals("001")) {
            builder.setPathLink(constructPathLink1(bferId));
        }
        return builder.build();
    }

    private List<PathLink> constructPathLink1(String nodeId) {
        if (nodeId.equals("003")) {
            AllocateTestLink link1 = new AllocateTestLink("001", "002", "001", "004", "002");
            AllocateTestLink link2 = new AllocateTestLink("002", "005", "002", "007", "003");
            return constructLink(link1,link2);
        }
        if (nodeId.equals("004")) {
            AllocateTestLink link1 = new AllocateTestLink("003","001","001","017","006");
            AllocateTestLink link2 = new AllocateTestLink("004","016","006","014","005");
            AllocateTestLink link3 = new AllocateTestLink("005", "013", "005", "011", "004");
            return constructLink(link1,link2,link3);
        }
        return null;
    }

    private class AllocateTestLink {
        String linkId;
        String sourceTp;
        String sourceNode;
        String destTp;
        String destNode;

        AllocateTestLink(String linkId, String sourceTp, String sourceNode, String destTp, String destNode) {
            this.linkId = linkId;
            this.sourceTp = sourceTp;
            this.sourceNode = sourceNode;
            this.destTp = destTp;
            this.destNode = destNode;
        }

        public String getLinkId() {
            return linkId;
        }

        public String getSourceTp() {
            return sourceTp;
        }

        public String getSourceNode() {
            return sourceNode;
        }

        public String getDestTp() {
            return destTp;
        }

        public String getDestNode() {
            return destNode;
        }
    }

    private List<PathLink> constructLink(AllocateTestLink... links) {
        List<PathLink> linkList = new ArrayList<>();
        for (AllocateTestLink link : links) {
            PathLinkBuilder builder = new PathLinkBuilder();
            builder.setLinkId(link.getLinkId());
            LinkSourceBuilder sourceBuilder = new LinkSourceBuilder();
            sourceBuilder.setSourceTp(link.getSourceTp());
            sourceBuilder.setSourceNode(link.getSourceNode());
            builder.setLinkSource(sourceBuilder.build());
            LinkDestBuilder destBuilder = new LinkDestBuilder();
            destBuilder.setDestTp(link.getDestTp());
            destBuilder.setDestNode(link.getDestNode());
            builder.setLinkDest(destBuilder.build());
            linkList.add(builder.build());
        }
        return linkList;
    }

    private TeBp getTeBp(String nodeId, int siVlaue, String tpId) {
        DomainId domainId = new DomainId(1);
        SubDomainId subDomainId = new SubDomainId(1);
        Si si = new Si(siVlaue);
        InstanceIdentifier<TeBp> teBpPath = getTeBpPath(nodeId, domainId, subDomainId, Bsl._64Bit, si, tpId);
        ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        try {
            Optional<TeBp> optional = tx.read(LogicalDatastoreType.CONFIGURATION, teBpPath).checkedGet();
            if (optional.isPresent()) {
                return optional.get();
            } else {
                return null;
            }
        } catch (ReadFailedException e) {
            LOG.info(e.getStackTrace().toString());
        }
        return null;
    }


    private InstanceIdentifier<TeSi> getTeSiPath(String nodeId,DomainId domainId, SubDomainId subDomainId,
                                                 Bsl bitstringlength,Si si) {
        InstanceIdentifier<TeSi> path = InstanceIdentifier.create(BierNetworkTopology.class).child(BierTopology.class,
                new BierTopologyKey("example-linkstate-topology")).child(BierNode.class, new BierNodeKey(nodeId))
                .child(BierTeNodeParams.class).child(TeDomain.class, new TeDomainKey(domainId))
                .child(TeSubDomain.class, new TeSubDomainKey(subDomainId)).child(TeBsl.class,
                        new TeBslKey(bitstringlength)).child(TeSi.class, new TeSiKey(si));
        return path;
    }

    private InstanceIdentifier<TeBp> getTeBpPath(String nodeId,DomainId domainId, SubDomainId subDomainId,
                                                 Bsl bitstringlength,Si si,String tpId) {
        InstanceIdentifier<TeBp> path = getTeSiPath(nodeId, domainId, subDomainId, bitstringlength, si)
                .child(TeBp.class, new TeBpKey(tpId));
        return path;
    }

    private class BierBpAllocateServiceMock implements BierBpAllocateParamsConfigService {

        @Override
        public Future<RpcResult<QuerySubdomainBslSiOutput>> querySubdomainBslSi(QuerySubdomainBslSiInput input) {
            List<SiOfModel> siOfModelList = new ArrayList<>();
            QuerySubdomainBslSiOutputBuilder builder = new QuerySubdomainBslSiOutputBuilder();
            builder.setSiOfModel(siOfModelList);
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        @Override
        public Future<RpcResult<DeleteSubdomainBslSiOutput>> deleteSubdomainBslSi(DeleteSubdomainBslSiInput input) {
            DeleteSubdomainBslSiOutputBuilder builder = new DeleteSubdomainBslSiOutputBuilder();
            builder.setConfigureResult(RpcUtil.getConfigResult(true, ""));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        @Override
        public Future<RpcResult<AddSubdomainBslSiOutput>> addSubdomainBslSi(AddSubdomainBslSiInput input) {
            AddSubdomainBslSiOutputBuilder builder = new AddSubdomainBslSiOutputBuilder();
            builder.setConfigureResult(RpcUtil.getConfigResult(true, ""));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
    }

}
