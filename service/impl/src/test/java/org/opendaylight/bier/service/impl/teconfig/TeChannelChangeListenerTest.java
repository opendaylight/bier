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

import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.BierTeChannelWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.service.impl.ChannelChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierForwardingType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BpAssignmentStrategy;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPceService;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryChannelThroughPortInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryChannelThroughPortOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryTeFrrPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryTeFrrPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveTeFrrPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.TePath;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBpBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.TeInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TeChannelChangeListenerTest extends AbstractDataBrokerTest {

    private static final Logger LOG = LoggerFactory.getLogger(TeChannelChangeListenerTest.class);

    private BierTeChannelWriterMock bierTeChannelWriterMock;
    private ChannelChangeListener channelChangeListener;
    private BierTeBiftWriterMock bierTeBiftWriterMock;
    private BierPceServiceMock bierPceServiceMock;
    private BierTeBitstringWriterMock bierTeBitstringWriterMock;
    private RpcConsumerRegistry rpcConsumerRegistry;
    @Mock
    private ChannelConfigWriter channelConfigWriter;


    public void setUp() {

        bierTeBiftWriterMock = new BierTeBiftWriterMock();
        bierTeChannelWriterMock = new BierTeChannelWriterMock();
        bierPceServiceMock = new BierPceServiceMock();
        bierTeBitstringWriterMock = new BierTeBitstringWriterMock();
        rpcConsumerRegistry = mock(RpcConsumerRegistry.class);
        channelChangeListener = new ChannelChangeListener(getDataBroker(), rpcConsumerRegistry, channelConfigWriter,
                bierTeChannelWriterMock, bierTeBiftWriterMock, bierTeBitstringWriterMock);
        when(rpcConsumerRegistry.getRpcService(BierPceService.class)).thenReturn(bierPceServiceMock);

        BierTopologyBuilder topologyBuilder = new BierTopologyBuilder();
        List<BierNode> bierNodeList = new ArrayList<>();
        topologyBuilder.setTopologyId("example-linkstate-topology");

        BierNode bierNode1 = constructBierNode("001", "Node1", "001", 35, 38,
                constructBierTerminationPointList("001", 1L, "002", 2L, "003", 3L),
                constructBierTeNodeParams(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(001), 1L, "001", new Integer(1), "002", new Integer(2),
                        "003", new Integer(3)));

        BierNode bierNode2 = constructBierNode("002", "Node2", "001", 35, 38,
                constructBierTerminationPointList("004", 4L, "005", 5L, "006", 6L),
                constructBierTeNodeParams(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(001), 2L, "004", new Integer(4), "005", new Integer(5),
                        "006", new Integer(6)));

        BierNode bierNode3 = constructBierNode("003", "Node3", "001", 35, 38,
                constructBierTerminationPointList("007", 7L, "008", 8L, "009", 9L),
                constructBierTeNodeParams(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(001), 3L, "007", new Integer(7), "008", new Integer(8),
                        "009", new Integer(9)));

        BierNode bierNode4 = constructBierNode("004", "Node4", "001", 35, 38,
                constructBierTerminationPointList("010", 10L, "011", 11L, "012", 12L),
                constructBierTeNodeParams(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(001), 4L, "010", new Integer(10), "011", new Integer(11),
                        "012", new Integer(12)));

        BierNode bierNode5 = constructBierNode("005", "Node5", "001", 35, 38,
                constructBierTerminationPointList("013", 13L, "014", 14L, "015", 15L),
                constructBierTeNodeParams(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(001), 5L, "013", new Integer(13), "014", new Integer(14),
                        "015", new Integer(15)));

        bierNodeList.add(bierNode1);
        bierNodeList.add(bierNode2);
        bierNodeList.add(bierNode3);
        bierNodeList.add(bierNode4);
        bierNodeList.add(bierNode5);
        topologyBuilder.setBierNode(bierNodeList);

        addTopologyToDataStore(topologyBuilder.build());
    }


    @Test
    public void teChannelChangeListenerTest() {

        setUp();

        //Test add channel with no ingressNode and egressNode
        Channel channelAdd = constructTeChannel("channel-1","10.84.220.5","102.112.20.40",1,1,
                (short)30,(short)40,null, 1, null, null, null, BpAssignmentStrategy.Manual);
        channelChangeListener.onDataTreeChanged(setTeChannelData(null, channelAdd, ModificationType.WRITE));
        Assert.assertNull(bierTeChannelWriterMock.getChannelFromList(channelAdd.getName()));

        //Test modify channel with no ingressNode and egressNode
        Channel channelModify = constructTeChannel("channel-1","10.84.220.5","102.112.20.40",1,1,
                (short)30,(short)40,null, 2, null, null, null, BpAssignmentStrategy.Manual);
        channelChangeListener.onDataTreeChanged(setTeChannelData(channelAdd, channelModify,
                ModificationType.SUBTREE_MODIFIED));
        Assert.assertNull(bierTeChannelWriterMock.getChannelFromList(channelModify.getName()));

        //Test add channel with ingressNode and egressNode
        List<EgressNode> egressNodeList1 = new ArrayList<>();
        egressNodeList1.add(constructEgressNode("003", 4, "009"));
        egressNodeList1.add(constructEgressNode("004", 5, "012"));
        Channel channelDeploy = constructTeChannel("channel-1","10.84.220.5","102.112.20.40",1,1,
                (short)30,(short)40,"001", 2, "003", egressNodeList1, BierForwardingType.BierTe,
                BpAssignmentStrategy.Manual);
        channelChangeListener.onDataTreeChanged(setTeChannelData(channelModify, channelDeploy,
                ModificationType.SUBTREE_MODIFIED));
        assertAddedChannelTeBitString(bierTeBitstringWriterMock.getTePathList());
        assertAddedTeChannel(bierTeChannelWriterMock.getChannelFromList("channel-1"));

        //Test modify egressNode from two to one
        List<EgressNode> egressNodeList2 = new ArrayList<>();
        egressNodeList2.add(constructEgressNode("003", 4, "009"));
        Channel channelDeploy2 = constructTeChannel("channel-1","10.84.220.5","102.112.20.40",1,1,
                (short)30,(short)40,"001", 2, "003", egressNodeList2, BierForwardingType.BierTe,
                BpAssignmentStrategy.Manual);
        channelChangeListener.onDataTreeChanged(setTeChannelData(channelDeploy, channelDeploy2,
                ModificationType.SUBTREE_MODIFIED));
        assertModifiedChannelTeBitString1(bierTeBitstringWriterMock.getTePathList());
        assertModifiedTeChannel1(bierTeChannelWriterMock.getChannelFromList("channel-1"));

        //Test modify egressNode from one to two
        List<EgressNode> egressNodeList3 = new ArrayList<>();
        egressNodeList3.add(constructEgressNode("003", 4, "009"));
        egressNodeList3.add(constructEgressNode("004", 5, "012"));
        Channel channelDeploy3 = constructTeChannel("channel-1","10.84.220.5","102.112.20.40",1,1,
                (short)30,(short)40,"001", 2, "003", egressNodeList3, BierForwardingType.BierTe,
                BpAssignmentStrategy.Manual);
        channelChangeListener.onDataTreeChanged(setTeChannelData(channelDeploy2, channelDeploy3,
                ModificationType.SUBTREE_MODIFIED));
        assertModifiedChannelTeBitString2(bierTeBitstringWriterMock.getTePathList());
        assertModifiedTeChannel2(bierTeChannelWriterMock.getChannelFromList("channel-1"));

        //Test modify ingressNode
        Channel channelDeploy4 = constructTeChannel("channel-1","10.84.220.5","102.112.20.40",1,1,
                (short)30,(short)40,"002", 2, "006", egressNodeList3, BierForwardingType.BierTe,
                BpAssignmentStrategy.Manual);
        channelChangeListener.onDataTreeChanged(setTeChannelData(channelDeploy3, channelDeploy4,
                ModificationType.SUBTREE_MODIFIED));
        assertModifiedChannelTeBitString3(bierTeBitstringWriterMock.getTePathList());
        assertModifiedTeChannel3(bierTeChannelWriterMock.getChannelFromList("channel-1"));

        //Test remove one egressNode and then add one
        List<EgressNode> egressNodeList4 = new ArrayList<>();
        egressNodeList4.add(constructEgressNode("001", 1, "003"));
        egressNodeList4.add(constructEgressNode("003", 4, "009"));
        Channel channelDeploy5 = constructTeChannel("channel-1","10.84.220.5","102.112.20.40",1,1,
                (short)30,(short)40,"002", 2, "006", egressNodeList4, BierForwardingType.BierTe,
                BpAssignmentStrategy.Manual);
        channelChangeListener.onDataTreeChanged(setTeChannelData(channelDeploy4, channelDeploy5,
                ModificationType.SUBTREE_MODIFIED));
        assertModifiedChannelTeBitString4(bierTeBitstringWriterMock.getTePathList());
        assertModifiedTeChannel4(bierTeChannelWriterMock.getChannelFromList("channel-1"));

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

    private TeBp constructTeBp(String tpId, Integer bp) {
        TeBpBuilder teBpBuilder = new TeBpBuilder();
        teBpBuilder.setTpId(tpId);
        teBpBuilder.setBitposition(bp);
        return teBpBuilder.build();
    }

    private TeSi constructTeSi(Si si, long ftLabel, List<TeBp> teBpList) {
        TeSiBuilder teSiBuilder = new TeSiBuilder();
        teSiBuilder.setSi(si);
        teSiBuilder.setFtLabel(new MplsLabel(new Long(ftLabel)));
        teSiBuilder.setTeBp(teBpList);
        return teSiBuilder.build();
    }

    private TeBsl constructTeBsl(Bsl bsl, List<TeSi> teSiList) {
        TeBslBuilder teBslBuilder = new TeBslBuilder();
        teBslBuilder.setBitstringlength(bsl);
        teBslBuilder.setTeSi(teSiList);
        return teBslBuilder.build();
    }

    private TeSubDomain constructTeSubDomain(SubDomainId subDomainId, List<TeBsl> teBslList) {
        TeSubDomainBuilder teSubDomainBuilder = new TeSubDomainBuilder();
        teSubDomainBuilder.setSubDomainId(subDomainId);
        teSubDomainBuilder.setTeBsl(teBslList);
        return teSubDomainBuilder.build();
    }

    private TeDomain constructTeDomain(DomainId domainId, List<TeSubDomain> teSubDomainList) {
        TeDomainBuilder teDomainBuilder = new TeDomainBuilder();
        teDomainBuilder.setDomainId(domainId);
        teDomainBuilder.setTeSubDomain(teSubDomainList);
        return teDomainBuilder.build();
    }

    private BierTeNodeParams constructBierTeNodeParams(DomainId domainId, SubDomainId subDomainId, Bsl bsl, Si si,
                                                       long ftlabel, String tpId1, Integer bp1, String tpId2,
                                                       Integer bp2, String tpId3, Integer bp3) {
        List<TeBp> teBpList = new ArrayList<>();
        teBpList.add(constructTeBp(tpId1, bp1));
        teBpList.add(constructTeBp(tpId2, bp2));
        teBpList.add(constructTeBp(tpId3, bp3));

        List<TeSi> teSiList = new ArrayList<>();
        teSiList.add(constructTeSi(si, ftlabel, teBpList));

        List<TeBsl> teBslList = new ArrayList<>();
        teBslList.add(constructTeBsl(bsl, teSiList));

        List<TeSubDomain> teSubDomainList = new ArrayList<>();
        teSubDomainList.add(constructTeSubDomain(subDomainId, teBslList));

        List<TeDomain> teDomainList = new ArrayList<>();
        teDomainList.add(constructTeDomain(domainId, teSubDomainList));

        BierTeNodeParamsBuilder bierTeNodeParamsBuilder = new BierTeNodeParamsBuilder();
        bierTeNodeParamsBuilder.setTeDomain(teDomainList);
        return bierTeNodeParamsBuilder.build();
    }

    private BierNode constructBierNode(String nodeId, String name, String routerId, int latitude,
                                       int longitude, List<BierTerminationPoint> bierTerminationPointList,
                                       BierTeNodeParams bierTeNodeParams) {
        BierNodeBuilder bierNodeBuilder = new BierNodeBuilder();
        bierNodeBuilder.setNodeId(nodeId);
        bierNodeBuilder.setName(name);
        bierNodeBuilder.setRouterId(routerId);
        bierNodeBuilder.setLatitude(BigInteger.valueOf(latitude));
        bierNodeBuilder.setLongitude(BigInteger.valueOf(longitude));
        bierNodeBuilder.setBierTerminationPoint(bierTerminationPointList);
        bierNodeBuilder.setBierTeNodeParams(bierTeNodeParams);
        return bierNodeBuilder.build();
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
                                       List<EgressNode> egressList, BierForwardingType type,
                                       BpAssignmentStrategy bpAssignmentStrategy) {
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
        builder.setBpAssignmentStrategy(bpAssignmentStrategy);
        return builder.build();
    }


    private void assertAddedChannelTeBitString(List<TePath> list) {
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0).getBitstring().size(), 6);
        Assert.assertEquals(list.get(0).getBitstring().get(0).getBitposition(), new BitString(new Integer(4)));
        Assert.assertEquals(list.get(0).getBitstring().get(1).getBitposition(), new BitString(new Integer(7)));
        Assert.assertEquals(list.get(0).getBitstring().get(2).getBitposition(), new BitString(new Integer(14)));
        Assert.assertEquals(list.get(0).getBitstring().get(3).getBitposition(), new BitString(new Integer(11)));
        Assert.assertEquals(list.get(0).getBitstring().get(4).getBitposition(), new BitString(new Integer(9)));
        Assert.assertEquals(list.get(0).getBitstring().get(5).getBitposition(), new BitString(new Integer(12)));
    }

    private void assertModifiedChannelTeBitString1(List<TePath> list) {
        Assert.assertEquals(list.size(), 2);
        Assert.assertEquals(list.get(1).getBitstring().size(), 3);
        Assert.assertEquals(list.get(1).getBitstring().get(0).getBitposition(), new BitString(new Integer(4)));
        Assert.assertEquals(list.get(1).getBitstring().get(1).getBitposition(), new BitString(new Integer(7)));
        Assert.assertEquals(list.get(1).getBitstring().get(2).getBitposition(), new BitString(new Integer(9)));
    }

    private void assertModifiedChannelTeBitString2(List<TePath> list) {
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.get(2).getBitstring().size(), 6);
        Assert.assertEquals(list.get(2).getBitstring().get(0).getBitposition(), new BitString(new Integer(14)));
        Assert.assertEquals(list.get(2).getBitstring().get(1).getBitposition(), new BitString(new Integer(11)));
        Assert.assertEquals(list.get(2).getBitstring().get(2).getBitposition(), new BitString(new Integer(4)));
        Assert.assertEquals(list.get(2).getBitstring().get(3).getBitposition(), new BitString(new Integer(7)));
        Assert.assertEquals(list.get(2).getBitstring().get(4).getBitposition(), new BitString(new Integer(9)));
        Assert.assertEquals(list.get(2).getBitstring().get(5).getBitposition(), new BitString(new Integer(12)));
    }

    private void assertModifiedChannelTeBitString3(List<TePath> list) {
        Assert.assertEquals(list.size(), 5);
        Assert.assertEquals(list.get(3).getBitstring().size(), 6);
        Assert.assertEquals(list.get(3).getBitstring().get(0).getBitposition(), new BitString(new Integer(14)));
        Assert.assertEquals(list.get(3).getBitstring().get(1).getBitposition(), new BitString(new Integer(11)));
        Assert.assertEquals(list.get(3).getBitstring().get(2).getBitposition(), new BitString(new Integer(4)));
        Assert.assertEquals(list.get(3).getBitstring().get(3).getBitposition(), new BitString(new Integer(7)));
        Assert.assertEquals(list.get(3).getBitstring().get(4).getBitposition(), new BitString(new Integer(9)));
        Assert.assertEquals(list.get(3).getBitstring().get(5).getBitposition(), new BitString(new Integer(12)));
        Assert.assertEquals(list.get(4).getBitstring().size(), 4);
        Assert.assertEquals(list.get(4).getBitstring().get(0).getBitposition(), new BitString(new Integer(7)));
        Assert.assertEquals(list.get(4).getBitstring().get(1).getBitposition(), new BitString(new Integer(10)));
        Assert.assertEquals(list.get(4).getBitstring().get(2).getBitposition(), new BitString(new Integer(9)));
        Assert.assertEquals(list.get(4).getBitstring().get(3).getBitposition(), new BitString(new Integer(12)));
    }

    private void assertModifiedChannelTeBitString4(List<TePath> list) {
        Assert.assertEquals(list.size(), 7);
        Assert.assertEquals(list.get(5).getBitstring().size(), 2);
        Assert.assertEquals(list.get(5).getBitstring().get(0).getBitposition(), new BitString(new Integer(7)));
        Assert.assertEquals(list.get(5).getBitstring().get(1).getBitposition(), new BitString(new Integer(9)));
        Assert.assertEquals(list.get(6).getBitstring().size(), 4);
        Assert.assertEquals(list.get(6).getBitstring().get(0).getBitposition(), new BitString(new Integer(7)));
        Assert.assertEquals(list.get(6).getBitstring().get(1).getBitposition(), new BitString(new Integer(2)));
        Assert.assertEquals(list.get(6).getBitstring().get(2).getBitposition(), new BitString(new Integer(9)));
        Assert.assertEquals(list.get(6).getBitstring().get(3).getBitposition(), new BitString(new Integer(3)));
    }

    private void assertAddedTeChannel(Channel channel) {
        Assert.assertEquals(channel.getName(), "channel-1");
        Assert.assertEquals(channel.getDomainId(), new DomainId(1));
        Assert.assertEquals(channel.getSubDomainId(), new SubDomainId(1));
        Assert.assertEquals(channel.getIngressNode(), "001");
        Assert.assertEquals(channel.getSrcTp(), "003");
        Assert.assertEquals(channel.getEgressNode().size(), 2);
        Assert.assertEquals(channel.getEgressNode().get(0).getNodeId(), "003");
        Assert.assertEquals(channel.getEgressNode().get(0).getRcvTp().size(), 1);
        Assert.assertEquals(channel.getEgressNode().get(0).getRcvTp().get(0).getTp(), "009");
        Assert.assertEquals(channel.getEgressNode().get(1).getNodeId(), "004");
        Assert.assertEquals(channel.getEgressNode().get(1).getRcvTp().size(), 1);
        Assert.assertEquals(channel.getEgressNode().get(1).getRcvTp().get(0).getTp(), "012");
    }

    private void assertModifiedTeChannel1(Channel channel) {
        Assert.assertEquals(channel.getName(), "channel-1");
        Assert.assertEquals(channel.getDomainId(), new DomainId(1));
        Assert.assertEquals(channel.getSubDomainId(), new SubDomainId(1));
        Assert.assertEquals(channel.getIngressNode(), "001");
        Assert.assertEquals(channel.getSrcTp(), "003");
        Assert.assertEquals(channel.getEgressNode().size(), 1);
        Assert.assertEquals(channel.getEgressNode().get(0).getNodeId(), "003");
        Assert.assertEquals(channel.getEgressNode().get(0).getRcvTp().size(), 1);
        Assert.assertEquals(channel.getEgressNode().get(0).getRcvTp().get(0).getTp(), "009");
    }

    private void assertModifiedTeChannel2(Channel channel) {
        assertAddedTeChannel(channel);
    }

    private void assertModifiedTeChannel3(Channel channel) {
        Assert.assertEquals(channel.getName(), "channel-1");
        Assert.assertEquals(channel.getDomainId(), new DomainId(1));
        Assert.assertEquals(channel.getSubDomainId(), new SubDomainId(1));
        Assert.assertEquals(channel.getIngressNode(), "002");
        Assert.assertEquals(channel.getSrcTp(), "006");
        Assert.assertEquals(channel.getEgressNode().size(), 2);
        Assert.assertEquals(channel.getEgressNode().get(0).getNodeId(), "003");
        Assert.assertEquals(channel.getEgressNode().get(0).getRcvTp().size(), 1);
        Assert.assertEquals(channel.getEgressNode().get(0).getRcvTp().get(0).getTp(), "009");
        Assert.assertEquals(channel.getEgressNode().get(1).getNodeId(), "004");
        Assert.assertEquals(channel.getEgressNode().get(1).getRcvTp().size(), 1);
        Assert.assertEquals(channel.getEgressNode().get(1).getRcvTp().get(0).getTp(), "012");
    }

    private void assertModifiedTeChannel4(Channel channel) {
        Assert.assertEquals(channel.getName(), "channel-1");
        Assert.assertEquals(channel.getDomainId(), new DomainId(1));
        Assert.assertEquals(channel.getSubDomainId(), new SubDomainId(1));
        Assert.assertEquals(channel.getIngressNode(), "002");
        Assert.assertEquals(channel.getSrcTp(), "006");
        Assert.assertEquals(channel.getEgressNode().size(), 2);
        Assert.assertEquals(channel.getEgressNode().get(0).getNodeId(), "001");
        Assert.assertEquals(channel.getEgressNode().get(0).getRcvTp().size(), 1);
        Assert.assertEquals(channel.getEgressNode().get(0).getRcvTp().get(0).getTp(), "003");
        Assert.assertEquals(channel.getEgressNode().get(1).getNodeId(), "003");
        Assert.assertEquals(channel.getEgressNode().get(1).getRcvTp().size(), 1);
        Assert.assertEquals(channel.getEgressNode().get(1).getRcvTp().get(0).getTp(), "009");
    }

    private static class DataTreeModificationMock implements DataTreeModification<Channel> {
        private Channel before;
        private Channel after;
        private ModificationType type;

        public void setTeChannelData(Channel before,Channel after,ModificationType type) {
            this.before = before;
            this.after = after;
            this.type = type;
        }

        @Override
        public DataTreeIdentifier<Channel> getRootPath() {
            InstanceIdentifier<Channel> teChannelId = InstanceIdentifier.create(BierNetworkChannel.class)
                    .child(BierChannel.class, new BierChannelKey("example-linkstate-topology"))
                    .child(Channel.class);
            return new DataTreeIdentifier<Channel>(LogicalDatastoreType.CONFIGURATION, teChannelId);
        }

        @Override
        public DataObjectModification<Channel> getRootNode() {
            DataObjectModificationMock mock = new DataObjectModificationMock();
            mock.setTeChannelData(before, after, type);
            return mock;
        }
    }

    private static class DataObjectModificationMock implements DataObjectModification<Channel> {
        private Channel before;
        private Channel after;
        private ModificationType type;

        public void setTeChannelData(Channel before,Channel after,ModificationType type) {
            this.before = before;
            this.after = after;
            this.type = type;
        }

        @Override
        public ModificationType getModificationType() {
            return type;
        }

        @Override
        public Channel getDataBefore() {
            return before;
        }

        @Override
        public Channel getDataAfter() {
            return after;
        }

        @Override
        public PathArgument getIdentifier() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Class<Channel> getDataType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection<DataObjectModification<? extends DataObject>> getModifiedChildren() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <C extends ChildOf<? super Channel>> DataObjectModification<C> getModifiedChildContainer(
                Class<C> child) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <C extends Augmentation<Channel> & DataObject> DataObjectModification<C> getModifiedAugmentation(
                Class<C> augmentation) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <C extends Identifiable<K> & ChildOf<? super Channel>,
                K extends Identifier<C>> DataObjectModification<C> getModifiedChildListItem(
                        Class<C> listItem, K listKey) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public DataObjectModification<? extends DataObject> getModifiedChild(PathArgument childArgument) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private Collection<DataTreeModification<Channel>> setTeChannelData(Channel before, Channel after,
                                                                       ModificationType type) {
        Collection<DataTreeModification<Channel>> collection = new ArrayList<>();
        DataTreeModificationMock mock = new DataTreeModificationMock();
        mock.setTeChannelData(before, after, type);
        collection.add(mock);
        return collection;
    }

    private static class BierTeBiftWriterMock implements BierTeBiftWriter {

        private List<TeInfo> teInfoAddedList = new ArrayList<>();

        @Override
        public ConfigurationResult writeTeBift(ConfigurationType type, String nodeId, TeInfo teInfo) {
            switch (type) {
                case ADD:
                    teInfoAddedList.add(teInfo);
                    break;
                case MODIFY:
                    break;
                case DELETE:
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }

        private List<TeInfo> getTeInfoAddedList() {
            return teInfoAddedList;
        }

    }

    private static class BierPceServiceMock implements BierPceService {

        @Override
        public Future<RpcResult<QueryChannelThroughPortOutput>> queryChannelThroughPort(QueryChannelThroughPortInput
                                                                                                        input) {
            return null;
        }

        @Override
        public Future<RpcResult<QueryTeFrrPathOutput>> queryTeFrrPath(QueryTeFrrPathInput queryTeFrrPathInput) {
            return null;
        }

        @Override
        public Future<RpcResult<CreateBierPathOutput>> createBierPath(CreateBierPathInput input) {
            CreateBierPathOutputBuilder builder = new CreateBierPathOutputBuilder();
            if (input.getBfirNodeId().equals("001")) {
                if (2 == input.getBfer().size()) {
                    builder.setChannelName(input.getChannelName());
                    builder.setBfirNodeId(input.getBfirNodeId());
                    builder.setBfer(getCreatBferListFromInput(input.getBfirNodeId(), input.getBfer()));
                }
                if (1 == input.getBfer().size()) {
                    builder.setChannelName(input.getChannelName());
                    builder.setBfirNodeId(input.getBfirNodeId());
                    List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.Bfer> bferList
                            = new ArrayList<>();
                    org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.BferBuilder bferBuilder1
                            = new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input
                            .BferBuilder();
                    bferBuilder1.setBferNodeId("004");
                    bferList.add(bferBuilder1.build());
                    org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.BferBuilder bferBuilder2
                            = new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input
                            .BferBuilder();
                    bferBuilder2.setBferNodeId("003");
                    bferList.add(bferBuilder2.build());
                    builder.setBfer(getCreatBferListFromInput(input.getBfirNodeId(), bferList));
                }
            }
            if (input.getBfirNodeId().equals("002")) {
                builder.setChannelName(input.getChannelName());
                builder.setBfirNodeId(input.getBfirNodeId());
                LOG.info("++++++ " + input.getBfer());
                builder.setBfer(getCreatBferListFromInput(input.getBfirNodeId(), input.getBfer()));
            }

            RpcResultBuilder<CreateBierPathOutput> rpcResultBuilder = RpcResultBuilder.success();
            rpcResultBuilder.withResult(builder.build());

            SettableFuture<RpcResult<CreateBierPathOutput>> future = SettableFuture.create();
            future.set(rpcResultBuilder.build());
            return future;
        }

        @Override
        public Future<RpcResult<RemoveBierPathOutput>> removeBierPath(RemoveBierPathInput input) {
            RemoveBierPathOutputBuilder builder = new RemoveBierPathOutputBuilder();
            if (null == input.getBfer()) {
                builder.setChannelName(input.getChannelName());
                builder.setBfirNodeId(input.getBfirNodeId());
                builder.setBfer(null);
            } else {
                builder.setChannelName(input.getChannelName());
                builder.setBfirNodeId(input.getBfirNodeId());
                LOG.info("remove bier path bfer: " + input.getBfer());
                List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input.Bfer> bferList
                        = new ArrayList<>();
                org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input.BferBuilder bferBuilder = new
                        org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input.BferBuilder();
                bferBuilder.setBferNodeId("003");
                bferList.add(bferBuilder.build());
                builder.setBfer(getRemoveBferListFromInput(input.getBfirNodeId(), bferList));
            }

            RpcResultBuilder<RemoveBierPathOutput> rpcResultBuilder = RpcResultBuilder.success();
            rpcResultBuilder.withResult(builder.build());

            SettableFuture<RpcResult<RemoveBierPathOutput>> future = SettableFuture.create();
            future.set(rpcResultBuilder.build());
            return future;
        }

        @Override
        public Future<RpcResult<Void>> removeTeFrrPath(RemoveTeFrrPathInput removeTeFrrPathInput) {
            return null;
        }

        @Override
        public Future<RpcResult<QueryBierPathOutput>> queryBierPath(QueryBierPathInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<QueryBierInstancePathOutput>> queryBierInstancePath(QueryBierInstancePathInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<CreateTeFrrPathOutput>> createTeFrrPath(CreateTeFrrPathInput createTeFrrPathInput) {
            return null;
        }

        private List<Bfer> getCreatBferListFromInput(String bfirId,
                List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.Bfer> list) {
            List<Bfer> bferList = new ArrayList<>();
            for (org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.Bfer bfer : list) {
                BferBuilder bferBuilder = new BferBuilder();
                bferBuilder.setBferNodeId(bfer.getBferNodeId());
                bferBuilder.setBierPath(setBierPath(bfirId, bfer.getBferNodeId()));
                bferList.add(bferBuilder.build());
            }
            return bferList;
        }

        private List<Bfer> getRemoveBferListFromInput(String bfirId,
                List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input.Bfer> list) {
            List<Bfer> bferList = new ArrayList<>();
            for (org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input.Bfer bfer : list) {
                BferBuilder bferBuilder = new BferBuilder();
                bferBuilder.setBferNodeId(bfer.getBferNodeId());
                bferBuilder.setBierPath(setBierPath(bfirId, bfer.getBferNodeId()));
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
            if (bfirId.equals("002")) {
                builder.setPathLink(constructPathLink2(bferId));
            }
            return builder.build();
        }

        private List<PathLink> constructPathLink1(String nodeId) {
            if (nodeId.equals("003")) {
                return constructLink("001", "002", "001", "004", "002", "002", "005", "002", "007", "003");
            }
            if (nodeId.equals("004")) {
                return constructLink("005", "001", "001", "014", "005", "004", "013", "005", "011", "004");
            }
            return null;
        }

        private List<PathLink> constructPathLink2(String nodeId) {
            if (nodeId.equals("001")) {
                return constructLink("002", "005", "002", "007", "003", "001", "004", "002", "002", "001");
            }
            if (nodeId.equals("003")) {
                return constructLink2("002", "005", "002", "007", "003");
            }
            if (nodeId.equals("004")) {
                return constructLink("002", "005", "002", "007", "003", "003", "008", "003", "010", "004");
            }
            return null;
        }

        private List<PathLink> constructLink(String linkId1, String sourceTp1, String sourceNode1, String destTp1,
                                             String destNode1, String linkId2, String sourceTp2, String sourceNode2,
                                             String destTp2, String destNode2) {
            PathLinkBuilder builder1 = new PathLinkBuilder();
            builder1.setLinkId(linkId1);
            LinkSourceBuilder sourceBuilder1 = new LinkSourceBuilder();
            sourceBuilder1.setSourceTp(sourceTp1);
            sourceBuilder1.setSourceNode(sourceNode1);
            builder1.setLinkSource(sourceBuilder1.build());
            LinkDestBuilder destBuilder1 = new LinkDestBuilder();
            destBuilder1.setDestTp(destTp1);
            destBuilder1.setDestNode(destNode1);
            builder1.setLinkDest(destBuilder1.build());
            List<PathLink> links = new ArrayList<>();
            links.add(builder1.build());
            PathLinkBuilder builder2 = new PathLinkBuilder();
            builder2.setLinkId(linkId2);
            LinkSourceBuilder sourceBuilder2 = new LinkSourceBuilder();
            sourceBuilder2.setSourceTp(sourceTp2);
            sourceBuilder2.setSourceNode(sourceNode2);
            builder2.setLinkSource(sourceBuilder2.build());
            LinkDestBuilder destBuilder2 = new LinkDestBuilder();
            destBuilder2.setDestTp(destTp2);
            destBuilder2.setDestNode(destNode2);
            builder2.setLinkDest(destBuilder2.build());
            links.add(builder2.build());
            return links;
        }

        private List<PathLink> constructLink2(String linkId1, String sourceTp1, String sourceNode1, String destTp1,
                                              String destNode1) {
            PathLinkBuilder builder1 = new PathLinkBuilder();
            builder1.setLinkId(linkId1);
            LinkSourceBuilder sourceBuilder1 = new LinkSourceBuilder();
            sourceBuilder1.setSourceTp(sourceTp1);
            sourceBuilder1.setSourceNode(sourceNode1);
            builder1.setLinkSource(sourceBuilder1.build());
            LinkDestBuilder destBuilder1 = new LinkDestBuilder();
            destBuilder1.setDestTp(destTp1);
            destBuilder1.setDestNode(destNode1);
            builder1.setLinkDest(destBuilder1.build());
            List<PathLink> links = new ArrayList<>();
            links.add(builder1.build());
            return links;
        }
    }

    private static class BierTeBitstringWriterMock implements BierTeBitstringWriter {

        private List<TePath> tePathList = new ArrayList<>();

        public ConfigurationResult writeBierTeBitstring(ConfigurationType type, String nodeId, TePath tePath) {
            switch (type) {
                case ADD:
                    tePathList.add(tePath);
                    break;
                case MODIFY:
                    tePathList.add(tePath);
                    break;
                case DELETE:
                    tePathList.add(tePath);
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }

        private List<TePath> getTePathList() {
            return tePathList;
        }

    }

    private static class BierTeChannelWriterMock implements BierTeChannelWriter {

        private List<Channel> channelList = new ArrayList<>();

        @Override
        public ConfigurationResult writeBierTeChannel(ConfigurationType type, String nodeId, Channel channel,
                                                      List<Long> pathId) {
            switch (type) {
                case ADD:
                    channelList.add(channel);
                    break;
                case MODIFY:
                    deleteChannelFromList(channel.getName());
                    channelList.add(channel);
                    break;
                case DELETE:
                    deleteChannelFromList(channel.getName());
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }

        private void deleteChannelFromList(String name) {
            if (null == name) {
                return;
            }
            for (Channel channel : channelList) {
                if (channel.getName().equals(name)) {
                    channelList.remove(channel);
                    return;
                }
            }
        }

        private Channel getChannelFromList(String name) {
            if (null == name) {
                return null;
            }
            for (Channel channel : channelList) {
                if (channel.getName().equals(name)) {
                    return channel;
                }
            }
            return null;
        }
    }
}
