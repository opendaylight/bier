/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.teconfig;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.TeInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.adj.type.te.adj.type.ConnectedBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.TeSubdomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class TeBpChangeListenerTest extends AbstractConcurrentDataBrokerTest {

    private BierTeBiftWriterMock bierTeBiftWriterMock;
    private BierNodeTeBpChangeListener bierNodeTeBpChangeListener;
    @Mock
    private RpcConsumerRegistry rpcConsumerRegistry;
    @Mock
    private BierTeBitstringWriter bierTeBitstringWriter;

    public void setUp() {
        bierTeBiftWriterMock = new BierTeBiftWriterMock();
        bierNodeTeBpChangeListener = new BierNodeTeBpChangeListener(getDataBroker(), rpcConsumerRegistry,
                bierTeBiftWriterMock, bierTeBitstringWriter);
        getDataBroker().registerDataTreeChangeListener(new DataTreeIdentifier<TeBp>(
                LogicalDatastoreType.CONFIGURATION, bierNodeTeBpChangeListener.getTeBpIid()),
                bierNodeTeBpChangeListener);

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
    }


    @Test
    public void bierNodeTeBpChangeListenerTest() {
        setUp();

        //Test add five bier node Fwd Info
        BierNode bierNode1 = constructBierNode("001", "Node1", "001", 35, 38,
                constructBierTerminationPointList("001", 1L, "002", 2L),
                constructBierTeNodeParams1(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(0001), 1L, "001", new Integer(1), "002", new Integer(2)));
        addBierNodeToDataStore(bierNode1);

        BierNode bierNode2 = constructBierNode("002", "Node2", "001", 35, 38,
                constructBierTerminationPointList("003", 3L, "004", 4L),
                constructBierTeNodeParams1(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(0001), 2L, "003", new Integer(3), "004", new Integer(4)));
        addBierNodeToDataStore(bierNode2);

        BierNode bierNode3 = constructBierNode("003", "Node3", "001", 35, 38,
                constructBierTerminationPointList("005", 5L, "006", 6L),
                constructBierTeNodeParams1(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(0001), 3L, "005", new Integer(5), "006", new Integer(6)));
        addBierNodeToDataStore(bierNode3);

        BierNode bierNode4 = constructBierNode("004", "Node4", "001", 35, 38,
                constructBierTerminationPointList("007", 7L, "008", 8L),
                constructBierTeNodeParams1(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(0001), 4L, "007", new Integer(7), "008", new Integer(8)));
        addBierNodeToDataStore(bierNode4);

        BierNode bierNode5 = constructBierNode("005", "Node5", "001", 35, 38,
                constructBierTerminationPointList("009", 9L, "010", 10L),
                constructBierTeNodeParams1(new DomainId(001), new SubDomainId(001), Bsl._64Bit,
                        new Si(0001), 5L, "009", new Integer(9), "010", new Integer(10)));
        addBierNodeToDataStore(bierNode5);

        assertTestAddedFiveNodeFwdInfo(bierTeBiftWriterMock.getTeInfoAddedList());

        //Test delete five bier node Fwd Info
        TeBpBuilder teBpBuilder = new TeBpBuilder();
        teBpBuilder.setBitposition(2).setTpId("002").setKey(new TeBpKey("002"));
        deleteBP("001", teBpBuilder.build());
        teBpBuilder.setBitposition(4).setTpId("004").setKey(new TeBpKey("004"));
        deleteBP("002", teBpBuilder.build());
        teBpBuilder.setBitposition(6).setTpId("006").setKey(new TeBpKey("006"));
        deleteBP("003",teBpBuilder.build());
        teBpBuilder.setBitposition(8).setTpId("008").setKey(new TeBpKey("008"));
        deleteBP("004",teBpBuilder.build());
        teBpBuilder.setBitposition(10).setTpId("010").setKey(new TeBpKey("010"));
        deleteBP("005",teBpBuilder.build());
        assertTestDeletedFiveNodeFwdInfo(bierTeBiftWriterMock.getTeInfoDeletedList());
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

    private List<BierTerminationPoint> constructBierTerminationPointList(String tpId1, Long tpIndex1, String tpId2,
                                                                         Long tpIndex2) {
        BierTerminationPointBuilder bierTerminationPointBuilder1 = new BierTerminationPointBuilder();
        bierTerminationPointBuilder1.setTpId(tpId1);
        bierTerminationPointBuilder1.setTpIndex(tpIndex1);
        BierTerminationPointBuilder bierTerminationPointBuilder2 = new BierTerminationPointBuilder();
        bierTerminationPointBuilder2.setTpId(tpId2);
        bierTerminationPointBuilder2.setTpIndex(tpIndex2);
        List<BierTerminationPoint> bierTerminationPointList = new ArrayList<>();
        bierTerminationPointList.add(bierTerminationPointBuilder1.build());
        bierTerminationPointList.add(bierTerminationPointBuilder2.build());
        return bierTerminationPointList;
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

    private BierTeNodeParams constructBierTeNodeParams1(DomainId domainId, SubDomainId subDomainId, Bsl bsl, Si si,
                                                       long ftlabel, String tpId1, Integer bp1,
                                                       String tpId2, Integer bp2) {
        List<TeBp> teBpList = new ArrayList<>();
        teBpList.add(constructTeBp(tpId1, bp1));
        teBpList.add(constructTeBp(tpId2, bp2));

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

    private BierTeNodeParams constructBierTeNodeParams2(DomainId domainId, SubDomainId subDomainId, Bsl bsl, Si si,
                                                       long ftlabel, String tpId1, Integer bp1) {
        List<TeBp> teBpList = new ArrayList<>();
        teBpList.add(constructTeBp(tpId1, bp1));

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

    private void assertTestAddedFiveNodeFwdInfo(List<TeInfo> teInfoList) {
        Assert.assertEquals(teInfoList.size(), 10);
        assertAddedInfo(teInfoList.get(0), 3, 1L, 2L, 2L);
        assertAddedInfo(teInfoList.get(1), 2, 2L, 1L, 3L);
        assertAddedInfo(teInfoList.get(2), 5, 2L, 3L, 4L);
        assertAddedInfo(teInfoList.get(3), 4, 3L, 2L, 5L);
        assertAddedInfo(teInfoList.get(4), 7, 3L, 4L, 6L);
        assertAddedInfo(teInfoList.get(5), 6, 4L, 3L, 7L);
        assertAddedInfo(teInfoList.get(6), 9, 4L, 5L, 8L);
        assertAddedInfo(teInfoList.get(7), 8, 5L, 4L, 9L);
        assertAddedInfo(teInfoList.get(8), 10, 1L, 5L, 1L);
        assertAddedInfo(teInfoList.get(9), 1, 5L, 1L, 10L);
    }

    private void assertAddedInfo(TeInfo teInfo, int bp, long inLabel, long outLabel, long ffIntf) {
        List<TeSubdomain> teSubdomainList = teInfo.getTeSubdomain();
        Assert.assertEquals(teSubdomainList.size(), 1);
        Assert.assertEquals(teSubdomainList.get(0).getSubdomainId(), new SubDomainId(0001));
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().size(), 1);
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getFwdBsl().intValue(), 64);
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getTeSi().size(), 1);
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getTeSi().get(0).getSi(), new Si(0001));
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getTeSi().get(0).getTeFIndex().get(0)
                        .getTeFIndex(), new BitString(new Integer(bp)));
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getTeSi().get(0).getFtLabel(),
                new MplsLabel(new Long(inLabel)));
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getTeSi().get(0).getTeFIndex().size(), 1);
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getTeSi().get(0).getTeFIndex().get(0)
                .getTeAdjType(), new ConnectedBuilder().setConnected(true).build());
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getTeSi().get(0).getTeFIndex().get(0)
                .getOutLabel(), new MplsLabel(new Long(outLabel)));
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getTeSi().get(0).getTeFIndex().get(0).getFIntf(),
                new Long(ffIntf));
    }

    private void assertTestDeletedFiveNodeFwdInfo(List<TeInfo> teInfoList) {
        Assert.assertEquals(teInfoList.size(), 10);
        assertDeletedInfo(teInfoList.get(0), 2);
        assertDeletedInfo(teInfoList.get(1), 3);
        assertDeletedInfo(teInfoList.get(2), 4);
        assertDeletedInfo(teInfoList.get(3), 5);
        assertDeletedInfo(teInfoList.get(4), 6);
        assertDeletedInfo(teInfoList.get(5), 7);
        assertDeletedInfo(teInfoList.get(6), 8);
        assertDeletedInfo(teInfoList.get(7), 9);
        assertDeletedInfo(teInfoList.get(8), 10);
        assertDeletedInfo(teInfoList.get(9), 1);
    }

    private void assertDeletedInfo(TeInfo teInfo, int bp) {
        List<TeSubdomain> teSubdomainList = teInfo.getTeSubdomain();
        Assert.assertEquals(teSubdomainList.size(), 1);
        Assert.assertEquals(teSubdomainList.get(0).getSubdomainId(), new SubDomainId(0001));
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().size(), 1);
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getFwdBsl().intValue(), 64);
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getTeSi().size(), 1);
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getTeSi().get(0).getSi(), new Si(0001));
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getTeSi().get(0).getTeFIndex().get(0)
                .getTeFIndex(), new BitString(new Integer(bp)));
        Assert.assertEquals(teSubdomainList.get(0).getTeBsl().get(0).getTeSi().get(0).getTeFIndex().get(0)
                .getTeAdjType(), new ConnectedBuilder().setConnected(true).build());
    }
}
