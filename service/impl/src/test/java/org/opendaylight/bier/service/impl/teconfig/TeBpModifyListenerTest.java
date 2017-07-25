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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.AddChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.AddChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.BierChannelApiService;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.DeployChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.DeployChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.GetChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.GetChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.ModifyChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.ModifyChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelWithPortInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelWithPortOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelWithPortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.RemoveChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.RemoveChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.with.port.output.QueryChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.with.port.output.QueryChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPceService;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryChannelThroughPortInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryChannelThroughPortOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryChannelThroughPortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.channel.through.port.output.RelatedChannel;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.channel.through.port.output.RelatedChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.TePath;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.bier.te.path.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.te.path.Bitstring;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.te.path.BitstringBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.te.path.BitstringKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBpKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.TeInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TeBpModifyListenerTest extends AbstractDataBrokerTest {

    private static final Logger LOG = LoggerFactory.getLogger(TeBpModifyListenerTest.class);
    private BierTeBiftWriterMock bierTeBiftWriterMock;
    private BierTeBitstringWriterMock bierTeBitstringWriterMock;
    private RpcConsumerRegistry rpcConsumerRegistry;
    private BierNodeTeBpChangeListener bierNodeTeBpChangeListener;
    private BierPceServiceMock bierPceServiceMock;
    private BierChannelApiServiceMock bierChannelApiServiceMock;
    private BitStringDB bitStringDB;
    private Channel channel1;
    private Channel channel2;
    private Channel channel3;

    public void setUp() {
        LOG.info("Init environment for delete and add BP test!");
        bierTeBiftWriterMock = new BierTeBiftWriterMock();
        bierTeBitstringWriterMock = new BierTeBitstringWriterMock();
        bierPceServiceMock = new BierPceServiceMock();
        bierChannelApiServiceMock = new BierChannelApiServiceMock();
        rpcConsumerRegistry = mock(RpcConsumerRegistry.class);
        when(rpcConsumerRegistry.getRpcService(BierPceService.class)).thenReturn(bierPceServiceMock);
        when(rpcConsumerRegistry.getRpcService(BierChannelApiService.class)).thenReturn(bierChannelApiServiceMock);
        bierNodeTeBpChangeListener = new BierNodeTeBpChangeListener(getDataBroker(), rpcConsumerRegistry,
                bierTeBiftWriterMock, bierTeBitstringWriterMock);
        getDataBroker().registerDataTreeChangeListener(new DataTreeIdentifier<TeBp>(
                        LogicalDatastoreType.CONFIGURATION, bierNodeTeBpChangeListener.getTeBpIid()),
                bierNodeTeBpChangeListener);
        bitStringDB = new BitStringDB(getDataBroker());

        writeBierLinkToDataStore();
        writeBierNodeToDataStore();

        channel1 = initChannel("channel1","001","018","003","011","004","014");
        channel2 = initChannel("channel2","001","018","002","015","003","011");
        channel3 = initChannel("channel3","001","018","002","015","004","014");

        writeBitStringToDataStore();

    }





    @Test
    public void localDecapBPModifyTest() {
        setUp();

        TeBpBuilder teBpBuilder = new TeBpBuilder();
        teBpBuilder.setBitposition(11).setTpId("011").setKey(new TeBpKey("011"));
        String bierNodeId = "003";
        deleteBP(bierNodeId,teBpBuilder.build());


        List<TeInfo> teInfoDeletedList = bierTeBiftWriterMock.getTeInfoDeletedList();
        assertBiftBPChanged(teInfoDeletedList, 11);

        List<TePath> tePathList = bierTeBitstringWriterMock.getTePathList();
        assertBitStringLocalDecapDeleted(tePathList);

        clearList();

        teBpBuilder.setTpId("011").setKey(new TeBpKey("011")).setBitposition(110);
        addBP(bierNodeId, teBpBuilder.build());

        List<TeInfo> teInfoAddedList = bierTeBiftWriterMock.getTeInfoAddedList();
        assertBiftBPChanged(teInfoAddedList, 110);
        tePathList = bierTeBitstringWriterMock.getTePathList();
        assertBitStringLocalDecapAdded(tePathList);

        clearList();
    }

    @Test
    public void connectedBPModifyTest() {
        setUp();

        TeBpBuilder teBpBuilder = new TeBpBuilder();
        teBpBuilder.setBitposition(10).setTpId("010").setKey(new TeBpKey("010"));
        String bierNodeId = "005";
        deleteBP(bierNodeId,teBpBuilder.build());

        List<TeInfo> teInfoDeletedList = bierTeBiftWriterMock.getTeInfoDeletedList();
        assertBiftBPChanged(teInfoDeletedList,10,1);

        List<TePath> tePathList = bierTeBitstringWriterMock.getTePathList();
        assertBitStringConnectedDeleted(tePathList);

        clearList();

        teBpBuilder.setBitposition(100).setTpId("010").setKey(new TeBpKey("010"));
        addBP(bierNodeId,teBpBuilder.build());

        List<TeInfo> teInfoAddedList = bierTeBiftWriterMock.getTeInfoAddedList();
        assertBiftBPChanged(teInfoAddedList,100,1);
        tePathList = bierTeBitstringWriterMock.getTePathList();
        assertBitStringConnectedAdded(tePathList);

        clearList();
    }

    public void clearList() {
        bierTeBiftWriterMock.getTeInfoAddedList().clear();
        bierTeBiftWriterMock.getTeInfoDeletedList().clear();
        bierTeBitstringWriterMock.getTePathList().clear();
    }

    public void assertBiftBPChanged(List<TeInfo> teInfoDeletedList,int... bps) {
        for (int i = 0;i < bps.length;i++) {
            BitString bitStringFound = teInfoDeletedList.get(i).getTeSubdomain().get(0)
                    .getTeBsl().get(0).getTeSi().get(0).getTeFIndex().get(0).getTeFIndex();
            BitString bitStringDesired = new BitString(bps[i]);
            Assert.assertEquals(bitStringFound,bitStringDesired);
        }
    }

    public void assertBitStringLocalDecapDeleted(List<TePath> tePathList) {


        for (TePath tePath : tePathList) {
            Long pathId = tePath.getPathId();
            List<Bitstring> bitStringList = tePath.getBitstring();

            if (pathId == 1L) {
                assertEquals(bitStringList,10,8,13,14);
            }
            if (pathId == 2L) {
                assertEquals(bitStringList,3,5,15);
            }

            if (pathId == 3L) {
                assertEquals(bitStringList,10,8,17,14,15);
            }
        }
    }

    public void assertBitStringLocalDecapAdded(List<TePath> tePathList) {
        for (TePath tePath : tePathList) {
            Long pathId = tePath.getPathId();
            List<Bitstring> bitStringList = tePath.getBitstring();

            if (pathId == 1L) {
                assertEquals(bitStringList,10,8,13,14,110);
            }

            if (pathId == 2L) {
                assertEquals(bitStringList,3,5,110,15);
            }

            if (pathId == 3L) {
                assertEquals(bitStringList,10,8,17,14,15);
            }
        }
    }

    public void assertBitStringConnectedDeleted(List<TePath> tePathList) {
        for (TePath tePath : tePathList) {
            Long pathId = tePath.getPathId();
            List<Bitstring> bitStringList = tePath.getBitstring();

            if (pathId == 1L) {
                assertEquals(bitStringList,8,13,11,14);
            }

            if (pathId == 2L) {
                assertEquals(bitStringList,3,5,11,15);
            }

            if (pathId == 3L) {
                assertEquals(bitStringList,8,17,14,15);
            }
        }
    }

    public void assertBitStringConnectedAdded(List<TePath> tePathList) {
        for (TePath tePath : tePathList) {
            Long pathId = tePath.getPathId();
            List<Bitstring> bitStringList = tePath.getBitstring();

            if (pathId == 1L) {
                assertEquals(bitStringList,100,8,13,11,14);
            }

            if (pathId == 2L) {
                assertEquals(bitStringList,3,5,11,15);
            }

            if (pathId == 3L) {
                assertEquals(bitStringList,100,8,17,14,15);
            }
        }
    }

    public void assertEquals(List<Bitstring> bitStringList, int...bps) {
        Set<Integer> bitStrings = new HashSet<>();
        for (int i = 0;i < bitStringList.size();i++) {
            bitStrings.add(bitStringList.get(i).getBitposition().getValue().intValue());
        }

        boolean flag = checkSetEquals(bitStrings,bps);
        Assert.assertTrue(flag);


    }

    public boolean checkSetEquals(Set<Integer> bitStrings, int...bps) {
        Set<Integer> bpSet = new HashSet<>();
        for (int bp:bps) {
            bpSet.add(bp);
        }
        if (bitStrings.containsAll(bpSet) && bpSet.containsAll(bitStrings)) {
            return true;
        } else {
            return false;
        }
    }

    public InstanceIdentifier<TeBp> getTeBPPath(String bierNodeId,String bp) {
        final InstanceIdentifier<TeBp> bierNodePath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey("example-linkstate-topology"))
                .child(BierNode.class, new BierNodeKey(bierNodeId)).child(BierTeNodeParams.class)
                .child(TeDomain.class, new TeDomainKey(new DomainId(001)))
                .child(TeSubDomain.class, new TeSubDomainKey(new SubDomainId(001)))
                .child(TeBsl.class, new TeBslKey(Bsl._64Bit)).child(TeSi.class,new TeSiKey(new Si(0001)))
                .child(TeBp.class, new TeBpKey(bp));
        return bierNodePath;
    }

    public void deleteBP(String bierNodeId,TeBp teBp) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<TeBp> teBPPath = getTeBPPath(bierNodeId,teBp.getTpId());
        tx.delete(LogicalDatastoreType.CONFIGURATION, teBPPath);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    public void addBP(String bierNodeId, TeBp teBp) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<TeBp> teBPPath = getTeBPPath(bierNodeId,teBp.getTpId());
        tx.put(LogicalDatastoreType.CONFIGURATION, teBPPath, teBp, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    public Channel initChannel(String channelName,String ingress,String srcTp, String egress1,
                               String rcvTp1, String egress2, String rcvTp2) {
        ChannelBuilder channelBuidler = new ChannelBuilder();

        channelBuidler.setKey(new ChannelKey(channelName));
        channelBuidler.setName(channelName);
        channelBuidler.setIngressNode(ingress);
        channelBuidler.setIngressBfrId(new BfrId(Integer.parseInt(ingress)));
        channelBuidler.setSrcTp(srcTp);
        EgressNodeBuilder egressNodeBuilder1 = new EgressNodeBuilder();
        egressNodeBuilder1.setNodeId(egress1);
        RcvTpBuilder rcvTpBuilder = new RcvTpBuilder();
        rcvTpBuilder.setTp(rcvTp1);
        List<RcvTp> rcvTpList = new ArrayList<>();
        rcvTpList.add(rcvTpBuilder.build());
        egressNodeBuilder1.setRcvTp(rcvTpList);
        EgressNodeBuilder egressNodeBuilder2 = new EgressNodeBuilder();
        egressNodeBuilder2.setNodeId(egress2);
        RcvTpBuilder rcvTpBuilder2 = new RcvTpBuilder();
        rcvTpBuilder2.setTp(rcvTp2);
        List<RcvTp> rcvTpList2 = new ArrayList<>();
        rcvTpList2.add(rcvTpBuilder.build());
        egressNodeBuilder2.setRcvTp(rcvTpList2);
        List<EgressNode> egressNodeList = new ArrayList<>();
        egressNodeList.add(egressNodeBuilder1.build());
        egressNodeList.add(egressNodeBuilder2.build());
        channelBuidler.setEgressNode(egressNodeList);
        return channelBuidler.build();
    }

    public void writeBitStringToDataStore() {
        TePath tePath1 = constructTePath(1L,10,8,13,11,14);
        TePath tePath2 = constructTePath(2L,3,5,11,15);
        TePath tePath3 = constructTePath(3L,10,8,17,14,15);

        bitStringDB.setBitStringToDataStore(channel1,tePath1);
        bitStringDB.setBitStringToDataStore(channel2,tePath2);
        bitStringDB.setBitStringToDataStore(channel3,tePath3);
    }


    public TePath constructTePath(long pathId, Integer...bps) {
        List<org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.te.path.Bitstring> bitStringList =
                new ArrayList<>();
        for (Integer bp : bps) {
            BitstringBuilder bitstringBuilder = new BitstringBuilder();
            BitString bitString = new BitString(bp);
            bitstringBuilder.setBitposition(bitString);
            bitstringBuilder.setKey(new BitstringKey(bitString));
            bitStringList.add(bitstringBuilder.build());
        }

        PathBuilder pathBuilder = new PathBuilder();
        pathBuilder.setSubdomainId(new SubDomainId(1));
        pathBuilder.setBitstringlength(Bsl._64Bit);
        pathBuilder.setSi(new Si(1));
        pathBuilder.setPathId(pathId);
        pathBuilder.setBitstring(bitStringList);

        return pathBuilder.build();

    }

    public void writeBierNodeToDataStore() {
        Map<String,Long> tpIdToTpIndex1 = new HashMap<>();
        tpIdToTpIndex1.put("001",1L);
        tpIdToTpIndex1.put("002",2L);
        tpIdToTpIndex1.put("018",18L);
        List<BierTerminationPoint> bierTerminationPointList1 = constructBierTerminationPointList(tpIdToTpIndex1);
        BierNode bierNode1 = constructBierNode("001", "Node1", "001", 35, 38,
                bierTerminationPointList1,
                constructBierTeNodeParams1(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(0001), 1L, tpIdToTpIndex1));
        addBierNodeToDataStore(bierNode1);

        Map<String,Long> tpIdToTpIndex2 = new HashMap<>();
        tpIdToTpIndex2.put("003",3L);
        tpIdToTpIndex2.put("004",4L);
        tpIdToTpIndex2.put("015",15L);
        tpIdToTpIndex2.put("017",17L);
        List<BierTerminationPoint> bierTerminationPointList2 = constructBierTerminationPointList(tpIdToTpIndex2);
        BierNode bierNode2 = constructBierNode("002", "Node2", "001", 35, 38,
                bierTerminationPointList2,
                constructBierTeNodeParams1(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(0001), 2L, tpIdToTpIndex2));
        addBierNodeToDataStore(bierNode2);

        Map<String,Long> tpIdToTpIndex3 = new HashMap<>();
        tpIdToTpIndex3.put("005",5L);
        tpIdToTpIndex3.put("006",6L);
        tpIdToTpIndex3.put("011",11L);
        tpIdToTpIndex3.put("013",13L);
        List<BierTerminationPoint> bierTerminationPointList3 = constructBierTerminationPointList(tpIdToTpIndex3);
        BierNode bierNode3 = constructBierNode("003", "Node3", "001", 35, 38,
                bierTerminationPointList3,
                constructBierTeNodeParams1(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(0001), 3L, tpIdToTpIndex3));
        addBierNodeToDataStore(bierNode3);

        Map<String,Long> tpIdToTpIndex4 = new HashMap<>();
        tpIdToTpIndex4.put("007",7L);
        tpIdToTpIndex4.put("008",8L);
        tpIdToTpIndex4.put("014",14L);
        List<BierTerminationPoint> bierTerminationPointList4 = constructBierTerminationPointList(tpIdToTpIndex4);
        BierNode bierNode4 = constructBierNode("004", "Node4", "001", 35, 38,
                bierTerminationPointList4,
                constructBierTeNodeParams1(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(0001), 4L, tpIdToTpIndex4));
        addBierNodeToDataStore(bierNode4);

        Map<String,Long> tpIdToTpIndex5 = new HashMap<>();
        tpIdToTpIndex5.put("009",9L);
        tpIdToTpIndex5.put("010",10L);
        tpIdToTpIndex5.put("012",12L);
        tpIdToTpIndex5.put("016",16L);
        List<BierTerminationPoint> bierTerminationPointList5 = constructBierTerminationPointList(tpIdToTpIndex5);
        BierNode bierNode5 = constructBierNode("005", "Node5", "001", 35, 38,
                bierTerminationPointList5,
                constructBierTeNodeParams1(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(0001), 5L, tpIdToTpIndex5));
        addBierNodeToDataStore(bierNode5);
    }

    public void writeBierLinkToDataStore() {
        BierLink bierLink1 = constructBierLink("001", "001", "002", "002", "003");
        addBierLinkToDataStore(bierLink1);

        BierLink bierLink2 = constructBierLink("002", "002", "004", "003", "005");
        addBierLinkToDataStore(bierLink2);

        BierLink bierLink3 = constructBierLink("003", "003", "006", "004", "007");
        addBierLinkToDataStore(bierLink3);

        BierLink bierLink4 = constructBierLink("004", "004", "008", "005", "009");
        addBierLinkToDataStore(bierLink4);

        BierLink bierLink5 = constructBierLink("005", "005", "010", "001", "001");
        addBierLinkToDataStore(bierLink5);

        BierLink bierLink6 = constructBierLink("006", "002", "003", "001", "002");
        addBierLinkToDataStore(bierLink6);

        BierLink bierLink7 = constructBierLink("007", "003", "005", "002", "004");
        addBierLinkToDataStore(bierLink7);

        BierLink bierLink8 = constructBierLink("008", "004", "007", "003", "006");
        addBierLinkToDataStore(bierLink8);

        BierLink bierLink9 = constructBierLink("009", "005", "009", "004", "008");
        addBierLinkToDataStore(bierLink9);

        BierLink bierLink10 = constructBierLink("010", "001", "001", "005", "010");
        addBierLinkToDataStore(bierLink10);

        BierLink bierLink11 = constructBierLink("011", "005", "012", "003", "013");
        addBierLinkToDataStore(bierLink11);

        BierLink bierLink12 = constructBierLink("012", "003", "013", "005", "012");
        addBierLinkToDataStore(bierLink12);

        BierLink bierLink13 = constructBierLink("013", "005", "016", "002", "017");
        addBierLinkToDataStore(bierLink13);

        BierLink bierLink14 = constructBierLink("014", "002", "017", "005", "016");
        addBierLinkToDataStore(bierLink14);
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

    private List<BierTerminationPoint> constructBierTerminationPointList(Map<String,Long> tpIDtoTpIndex) {
        List<BierTerminationPoint> bierTerminationPointList = new ArrayList<>();
        Set<String> tpIdSet = tpIDtoTpIndex.keySet();
        for (String tpId : tpIdSet) {
            Long tpIndex = tpIDtoTpIndex.get(tpId);
            BierTerminationPointBuilder bierTerminationPointBuilder = new BierTerminationPointBuilder();
            bierTerminationPointBuilder.setTpId(tpId);
            bierTerminationPointBuilder.setTpIndex(tpIndex);
            bierTerminationPointList.add(bierTerminationPointBuilder.build());
        }
        return  bierTerminationPointList;
    }


    private BierTeNodeParams constructBierTeNodeParams1(DomainId domainId, SubDomainId subDomainId, Bsl bsl, Si si,
                                                        long ftlabel, Map<String,Long> tpIdToTp) {
        List<TeBp> teBpList = new ArrayList<>();
        Set<String> tpIdSet = tpIdToTp.keySet();
        for (String tpId : tpIdSet) {
            long tp = tpIdToTp.get(tpId);
            teBpList.add(constructTeBp(tpId,(int)tp));
        }

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

    private void addBierNodeToDataStore(BierNode bierNode) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<BierNode> bierNodePath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey("example-linkstate-topology"))
                .child(BierNode.class, new BierNodeKey(bierNode.getNodeId()));
        tx.put(LogicalDatastoreType.CONFIGURATION, bierNodePath, bierNode, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }



    private BierLink constructBierLink(String linkId, String sourceNode, String sourceTp,
                                       String destNode, String destTp) {
        BierLinkBuilder bierLinkBuilder = new BierLinkBuilder();
        bierLinkBuilder.setLinkId(linkId);
        LinkSourceBuilder linkSourceBuilder = new LinkSourceBuilder();
        linkSourceBuilder.setSourceNode(sourceNode);
        linkSourceBuilder.setSourceTp(sourceTp);
        bierLinkBuilder.setLinkSource(linkSourceBuilder.build());
        LinkDestBuilder linkDestBuilder = new LinkDestBuilder();
        linkDestBuilder.setDestNode(destNode);
        linkDestBuilder.setDestTp(destTp);
        bierLinkBuilder.setLinkDest(linkDestBuilder.build());
        return bierLinkBuilder.build();
    }

    private void addBierLinkToDataStore(BierLink bierLink) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<BierLink> bierLinkPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey("example-linkstate-topology"))
                .child(BierLink.class, new BierLinkKey(bierLink.getLinkId()));
        tx.put(LogicalDatastoreType.CONFIGURATION, bierLinkPath, bierLink, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    private static class BierTeBiftWriterMock implements BierTeBiftWriter {

        private List<TeInfo> teInfoAddedList = new ArrayList<>();
        private List<TeInfo> teInfoDeletedList = new ArrayList<>();

        @Override
        public ConfigurationResult writeTeBift(ConfigurationType type, String nodeId, TeInfo teInfo) {
            switch (type) {
                case ADD:
                    teInfoAddedList.add(teInfo);
                    break;
                case MODIFY:
                    break;
                case DELETE:
                    teInfoDeletedList.add(teInfo);
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }

        private List<TeInfo> getTeInfoAddedList() {
            return teInfoAddedList;
        }

        private List<TeInfo> getTeInfoDeletedList() {
            return teInfoDeletedList;
        }
    }

    public static class BierTeBitstringWriterMock implements BierTeBitstringWriter {

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

        public List<TePath> getTePathList() {
            return tePathList;
        }

    }

    private static class BierPceServiceMock implements BierPceService {

        @Override
        public Future<RpcResult<QueryChannelThroughPortOutput>> queryChannelThroughPort(QueryChannelThroughPortInput
                                                                                                input) {
            String nodeId = input.getNodeId();
            String tpId = input.getTpId();

            QueryChannelThroughPortOutputBuilder queryChannelThroughPortOutputBuilder =
                    new QueryChannelThroughPortOutputBuilder();
            List<RelatedChannel> relatedChannelList = new ArrayList<>();

            if (nodeId.equals("001") && tpId.equals("001")) {
                RelatedChannelBuilder relatedChannelBuilder1 = new RelatedChannelBuilder();
                relatedChannelBuilder1.setChannelName("channel1");
                relatedChannelBuilder1.setBfir("001");
                relatedChannelList.add(relatedChannelBuilder1.build());

                RelatedChannelBuilder relatedChannelBuilder2 = new RelatedChannelBuilder();
                relatedChannelBuilder2.setChannelName("channel3");
                relatedChannelBuilder2.setBfir("001");
                relatedChannelList.add(relatedChannelBuilder2.build());
            }

            queryChannelThroughPortOutputBuilder.setRelatedChannel(relatedChannelList);
            RpcResultBuilder<QueryChannelThroughPortOutput> rpcResultBuilder = RpcResultBuilder.success();
            rpcResultBuilder.withResult(queryChannelThroughPortOutputBuilder.build());
            SettableFuture<RpcResult<QueryChannelThroughPortOutput>> future = SettableFuture.create();
            future.set(rpcResultBuilder.build());
            return future;
        }

        @Override
        public Future<RpcResult<CreateBierPathOutput>> createBierPath(CreateBierPathInput input) {

            return null;
        }

        @Override
        public Future<RpcResult<RemoveBierPathOutput>> removeBierPath(RemoveBierPathInput input) {

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
    }

    private static class BierChannelApiServiceMock implements BierChannelApiService {

        @Override
        public Future<RpcResult<DeployChannelOutput>> deployChannel(DeployChannelInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<ModifyChannelOutput>> modifyChannel(ModifyChannelInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<QueryChannelWithPortOutput>> queryChannelWithPort(QueryChannelWithPortInput input) {
            String nodeId = input.getNodeId();
            String tpId = input.getTpId();
            QueryChannelWithPortOutputBuilder queryChannelWithPortOutputBuilder =
                    new QueryChannelWithPortOutputBuilder();
            List<QueryChannel> queryChannelList = new ArrayList<>();
            if (nodeId.equals("003") && tpId.equals("011")) {
                QueryChannelBuilder queryChannelBuilder1 = new QueryChannelBuilder();
                queryChannelBuilder1.setChannelName("channel1");
                queryChannelBuilder1.setBfir("001");
                queryChannelBuilder1.setIsRcvTp(true);
                queryChannelList.add(queryChannelBuilder1.build());

                QueryChannelBuilder queryChannelBuilder2 = new QueryChannelBuilder();
                queryChannelBuilder2.setChannelName("channel2");
                queryChannelBuilder2.setBfir("001");
                queryChannelBuilder2.setIsRcvTp(true);
                queryChannelList.add(queryChannelBuilder2.build());
            }
            queryChannelWithPortOutputBuilder.setQueryChannel(queryChannelList);
            RpcResultBuilder<QueryChannelWithPortOutput> rpcResultBuilder = RpcResultBuilder.success();
            rpcResultBuilder.withResult(queryChannelWithPortOutputBuilder.build());
            SettableFuture<RpcResult<QueryChannelWithPortOutput>> future = SettableFuture.create();
            future.set(rpcResultBuilder.build());
            return future;
        }

        @Override
        public Future<RpcResult<RemoveChannelOutput>> removeChannel(RemoveChannelInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<AddChannelOutput>> addChannel(AddChannelInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<GetChannelOutput>> getChannel(GetChannelInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<QueryChannelOutput>> queryChannel(QueryChannelInput input) {
            return null;
        }
    }
}
