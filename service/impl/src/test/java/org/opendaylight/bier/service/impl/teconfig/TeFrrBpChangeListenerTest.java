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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import junit.framework.Assert;
import org.junit.Test;

import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBtaftWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.service.impl.allocatebp.TopoBasedBpAllocateStrategy;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BpAssignmentStrategy;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPceService;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathOutputBuilder;
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
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveTeFrrPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.TeFrrPathUpdate;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.TeFrrPathUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.backup.path.Path;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.backup.path.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.TeFrrKey;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.TeFrrKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.te.frr.key.ProtectedLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.FrrPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.ExcludingLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.ExcludingLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.NextHopPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.NextNextHopPath;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.NextNextHopPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
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
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBpBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.TeInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.frr.Btaft;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TeFrrBpChangeListenerTest extends AbstractDataBrokerTest {

    private static final Logger LOG = LoggerFactory.getLogger(TeFrrBpChangeListenerTest.class);
    private static final String TOPOLOGY_ID = "example-linkstate-topology";
    private BierTeBiftWriterMock bierTeBiftWriterMock;
    private BierTeBtaftWriterMock bierTeBtaftWriterMock;
    private AddResetBitMaskProcess addResetBitMaskProcess;


    public void setUp() {
        BierPceServiceMock bierPceServiceMock;
        RpcConsumerRegistry rpcConsumerRegistry;
        rpcConsumerRegistry = mock(RpcConsumerRegistry.class);
        bierPceServiceMock = new BierPceServiceMock();
        when(rpcConsumerRegistry.getRpcService(BierPceService.class))
                .thenReturn(bierPceServiceMock);
        TopoBasedBpAllocateStrategy bpAllocateStrategy;

        bpAllocateStrategy = TopoBasedBpAllocateStrategy.getInstance();
        bpAllocateStrategy.setDataBroker(getDataBroker());
        bpAllocateStrategy.setRpcConsumerRegistry(rpcConsumerRegistry);

        bierTeBiftWriterMock = new BierTeBiftWriterMock();
        bierTeBtaftWriterMock = new BierTeBtaftWriterMock();

        addResetBitMaskProcess = new AddResetBitMaskProcess(getDataBroker(), rpcConsumerRegistry, null,
                bierTeBiftWriterMock, bierTeBtaftWriterMock);

        constructBierTopology();
    }

    @Test
    public void teFrrBpChangeListenerTest() {

        setUp();

        TeBpBuilder teBpBuilder = new TeBpBuilder();
        teBpBuilder.setBitposition(4);
        TeSiBuilder teSiBuilder = new TeSiBuilder();
        teSiBuilder.setSi(new Si(1));
        TeBslBuilder teBslBuilder = new TeBslBuilder();
        teBslBuilder.setBitstringlength(Bsl._64Bit);
        TeSubDomainBuilder teSubDomainBuilder = new TeSubDomainBuilder();
        teSubDomainBuilder.setSubDomainId(new SubDomainId(1));
        TeDomainBuilder teDomainBuilder = new TeDomainBuilder();
        teDomainBuilder.setDomainId(new DomainId(1));
        BierLink bierLink = constructBierLink("001","001","002","002","004");
        BpAssignmentStrategy bpAssignmentStrategy = BpAssignmentStrategy.Automatic;
        addResetBitMaskProcess.processFrrInfo(bierLink, bpAssignmentStrategy, teDomainBuilder.build(),
                teSubDomainBuilder.build(), teBslBuilder.build(), teSiBuilder.build(), teBpBuilder.build(),
                null, AddResetBitMaskProcess.FRR_BP_ADD);

        BierLink bierLink1 = constructBierLink("005", "001", "003", "004", "010");
        TeBpBuilder teBpBuilder1 = new TeBpBuilder();
        teBpBuilder1.setBitposition(10);
        addResetBitMaskProcess.processFrrInfo(bierLink1, bpAssignmentStrategy, teDomainBuilder.build(),
                teSubDomainBuilder.build(), teBslBuilder.build(), teSiBuilder.build(), teBpBuilder1.build(),
                null, AddResetBitMaskProcess.FRR_BP_ADD);

        Assert.assertEquals(bierTeBiftWriterMock.teInfoAddedList.size(), 2);
        LOG.info(bierTeBiftWriterMock.teInfoAddedList.get(0).toString());
        Assert.assertEquals(bierTeBtaftWriterMock.btaftAddedList.size(), 2);
        LOG.info(bierTeBtaftWriterMock.btaftAddedList.get(0).toString());

        addResetBitMaskProcess.processFrrInfo(bierLink1, bpAssignmentStrategy, teDomainBuilder.build(),
                teSubDomainBuilder.build(), teBslBuilder.build(), teSiBuilder.build(), teBpBuilder1.build(),
                null, AddResetBitMaskProcess.FRR_BP_DELETE);

        Assert.assertEquals(bierTeBiftWriterMock.teInfoModifiedList.size(),1);
        LOG.info(bierTeBiftWriterMock.teInfoModifiedList.get(0).toString());
        Assert.assertEquals(bierTeBtaftWriterMock.btaftDeletedList.size(),1);
        LOG.info(bierTeBtaftWriterMock.btaftDeletedList.get(0).toString());

        TeFrrPathUpdate teFrrPathUpdate = constructTeFrrPath();
        addResetBitMaskProcess.processTeFrrPathUpdate(teFrrPathUpdate);
        Assert.assertEquals(bierTeBiftWriterMock.teInfoAddedList.size(), 3);
        LOG.info(bierTeBiftWriterMock.teInfoAddedList.get(2).toString());
        Assert.assertEquals(bierTeBtaftWriterMock.btaftDeletedList.size(),2);
        LOG.info(bierTeBtaftWriterMock.btaftDeletedList.get(1).toString());

        addResetBitMaskProcess.processFrrInfo(bierLink, bpAssignmentStrategy, teDomainBuilder.build(),
                teSubDomainBuilder.build(), teBslBuilder.build(), teSiBuilder.build(), teBpBuilder.build(),
                null, AddResetBitMaskProcess.FRR_BP_DELETE);
        Assert.assertEquals(bierTeBiftWriterMock.teInfoModifiedList.size(),2);
        LOG.info(bierTeBiftWriterMock.teInfoModifiedList.get(1).toString());
        Assert.assertEquals(bierTeBtaftWriterMock.btaftDeletedList.size(),3);
        LOG.info(bierTeBtaftWriterMock.btaftDeletedList.get(2).toString());
    }

    private void constructBierTopology() {
        BierTopologyBuilder topologyBuilder = new BierTopologyBuilder();
        topologyBuilder.setTopologyId("example-linkstate-topology");
        List<BierNode> bierNodeList = constructBierNodeList();
        List<BierLink> bierLinkList = constructBierLinkList();
        topologyBuilder.setBierNode(bierNodeList);
        topologyBuilder.setBierLink(bierLinkList);
        addTopologyToDataStore(topologyBuilder.build());
    }

    private List<BierNode> constructBierNodeList() {
        List<Integer> bpList1 = Arrays.asList((Integer)12, 2, 3, 19);
        List<String> tpIdList1 = Arrays.asList("001","002","003", "019");
        TeDomain teDomain1 = constructTeDomain(new DomainId(1), new SubDomainId(1), 1, bpList1, tpIdList1);
        BierNode bierNode1 = constructBierNode("001", "Node1", "001", 35, 38,
                constructBierTerminationPointList("001", 1L, "002", 2L, "003", 3L),
                constructBierTeNodeParams(teDomain1));

        List<Integer> bpList2 = Arrays.asList((Integer)13, 4, 5, 6, 20);
        List<String> tpIdList2 = Arrays.asList("007","004","005","006", "020");
        TeDomain teDomain2 = constructTeDomain(new DomainId(1), new SubDomainId(1), 1, bpList2, tpIdList2);
        BierNode bierNode2 = constructBierNode("002", "Node2", "002", 35, 38,
                constructBierTerminationPointList("004", 4L, "005", 5L, "006", 6L,"007", 7L),
                constructBierTeNodeParams(teDomain2));

        List<Integer> bpList3 = Arrays.asList((Integer)14, 8);
        List<String> tpIdList3 = Arrays.asList("009", "008");
        TeDomain teDomain3 = constructTeDomain(new DomainId(1), new SubDomainId(1), 1, bpList3, tpIdList3);
        BierNode bierNode3 = constructBierNode("003", "Node3", "003", 35, 38,
                constructBierTerminationPointList("008", 8L, "009", 9L),
                constructBierTeNodeParams(teDomain3));

        List<Integer> bpList4 = Arrays.asList((Integer)15, 10, 11);
        List<String> tpIdList4 = Arrays.asList("012", "010", "011");
        TeDomain teDomain4 = constructTeDomain(new DomainId(001), new SubDomainId(001), 1, bpList4, tpIdList4);
        BierNode bierNode4 = constructBierNode("004", "Node4", "004", 35, 38,
                constructBierTerminationPointList("010", 10L, "011", 11L, "012", 12L),
                constructBierTeNodeParams(teDomain4));

        List<Integer> bpList5 = Arrays.asList((Integer)16, 17, 18);
        List<String> tpIdList5 = Arrays.asList("013", "017", "018");
        TeDomain teDomain5 = constructTeDomain(new DomainId(1), new SubDomainId(1), 1, bpList5, tpIdList5);
        BierNode bierNode5 = constructBierNode("005", "Node5", "005", 35, 38,
                constructBierTerminationPointList("013", 13L, "017", 17L, "018", 18L),
                constructBierTeNodeParams(teDomain5));

        List<BierNode> bierNodeList = new ArrayList<>();
        bierNodeList.add(bierNode1);
        bierNodeList.add(bierNode2);
        bierNodeList.add(bierNode3);
        bierNodeList.add(bierNode4);
        bierNodeList.add(bierNode5);
        return bierNodeList;
    }

    private List<BierLink> constructBierLinkList() {
        BierLink bierLink1 = constructBierLink("001","001","002","002","004");
        BierLink bierLink2 = constructBierLink("002","002","004","001","002");
        BierLink bierLink3 = constructBierLink("003","002","005","003","008");
        BierLink bierLink4 = constructBierLink("004","003","008","002","005");
        BierLink bierLink5 = constructBierLink("005","001","003","004","010");
        BierLink bierLink6 = constructBierLink("006","004","010","001","003");
        BierLink bierLink7 = constructBierLink("007","004","011","002","006");
        BierLink bierLink8 = constructBierLink("008","002","006","004","011");
        BierLink bierLink9 = constructBierLink("009","001","019","005","017");
        BierLink bierLink10 = constructBierLink("010","005","017","001","019");
        BierLink bierLink11 = constructBierLink("011","005","018","002","020");
        BierLink bierLink12 = constructBierLink("012","002","020","005","018");
        List<BierLink> bierLinkList = new ArrayList<>();
        bierLinkList.add(bierLink1);
        bierLinkList.add(bierLink2);
        bierLinkList.add(bierLink3);
        bierLinkList.add(bierLink4);
        bierLinkList.add(bierLink5);
        bierLinkList.add(bierLink6);
        bierLinkList.add(bierLink7);
        bierLinkList.add(bierLink8);
        bierLinkList.add(bierLink9);
        bierLinkList.add(bierLink10);
        bierLinkList.add(bierLink11);
        bierLinkList.add(bierLink12);
        return bierLinkList;
    }

    private TeFrrPathUpdate constructTeFrrPath() {
        TeFrrKeyBuilder teFrrKeyBuilder = new TeFrrKeyBuilder();
        teFrrKeyBuilder.setSubDomainId(new SubDomainId(1));
        teFrrKeyBuilder.setProtectedLink(new ProtectedLinkBuilder(constructBierLink("001","001","002","002","004"))
                .build());
        NextHopPathBuilder nextHopPathBuilder = new NextHopPathBuilder();
        nextHopPathBuilder.setDestinationNode("002");
        List<Path> nhPathList = new ArrayList<>();
        nhPathList.add(new PathBuilder(constructBierLink("009","001","019","005","017")).build());
        nhPathList.add(new PathBuilder(constructBierLink("011","005","018","002","020")).build());
        nextHopPathBuilder.setPath(nhPathList);

        NextNextHopPathBuilder nextNextHopPathBuilder1 = new NextNextHopPathBuilder();
        nextNextHopPathBuilder1.setDestinationNode("005");
        List<Path> nnhPathList1 = new ArrayList<>();
        nnhPathList1.add(new PathBuilder(constructBierLink("009","001","019","005","017")).build());
        nextNextHopPathBuilder1.setPath(nnhPathList1);
        NextNextHopPathBuilder nextNextHopPathBuilder2 = new NextNextHopPathBuilder();
        nextNextHopPathBuilder2.setDestinationNode("003");
        List<Path> nnhPathList2 = new ArrayList<>();
        nnhPathList2.add(new PathBuilder(constructBierLink("009","001","019","005","017")).build());
        nnhPathList2.add(new PathBuilder(constructBierLink("011","005","018","002","020")).build());
        nnhPathList2.add(new PathBuilder(constructBierLink("003","002","005","003","008")).build());
        nextNextHopPathBuilder2.setPath(nnhPathList2);
        NextNextHopPathBuilder nextNextHopPathBuilder3 = new NextNextHopPathBuilder();
        nextNextHopPathBuilder3.setDestinationNode("004");
        List<Path> nnhPathList3 = new ArrayList<>();
        nnhPathList3.add(new PathBuilder(constructBierLink("009","001","019","005","017")).build());
        nnhPathList3.add(new PathBuilder(constructBierLink("011","005","018","002","020")).build());
        nnhPathList3.add(new PathBuilder(constructBierLink("008","002","006","004","011")).build());
        nextNextHopPathBuilder3.setPath(nnhPathList3);
        List<NextNextHopPath> nextNextHopPathList = new ArrayList<>();
        nextNextHopPathList.add(nextNextHopPathBuilder1.build());
        nextNextHopPathList.add(nextNextHopPathBuilder2.build());
        nextNextHopPathList.add(nextNextHopPathBuilder3.build());

        List<ExcludingLink> excludingLinkList = new ArrayList<>();
        excludingLinkList.add(new ExcludingLinkBuilder(constructBierLink("001","001","002","002","004"))
                .build());
        excludingLinkList.add(new ExcludingLinkBuilder(constructBierLink("008","002","006","004","011"))
                .build());
        excludingLinkList.add(new ExcludingLinkBuilder(constructBierLink("003","002","005","003","008"))
                .build());
        excludingLinkList.add(new ExcludingLinkBuilder(constructBierLink("012","002","020","005","018"))
                .build());
        FrrPathBuilder frrPathBuilder = new FrrPathBuilder();
        frrPathBuilder.setNextHopPath(nextHopPathBuilder.build());
        frrPathBuilder.setNextNextHopPath(nextNextHopPathList);
        frrPathBuilder.setExcludingLink(excludingLinkList);
        TeFrrPathUpdateBuilder builder = new TeFrrPathUpdateBuilder();
        builder.setTeFrrKey(teFrrKeyBuilder.build());
        builder.setFrrPath(frrPathBuilder.build());
        return builder.build();
    }

    private void addTopologyToDataStore(BierTopology bierTopology) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<BierTopology> topologyPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(TOPOLOGY_ID));
        tx.put(LogicalDatastoreType.CONFIGURATION, topologyPath, bierTopology, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    private BierLink constructBierLink(String linkId, String srcNode, String srcTp, String dstNode, String dstTp) {
        BierLinkBuilder bierLinkBuilder = new BierLinkBuilder();
        bierLinkBuilder.setKey(new BierLinkKey(linkId));
        LinkSourceBuilder linkSourceBuilder = new LinkSourceBuilder();
        linkSourceBuilder.setSourceNode(srcNode);
        linkSourceBuilder.setSourceTp(srcTp);
        bierLinkBuilder.setLinkSource(linkSourceBuilder.build());
        LinkDestBuilder linkDestBuilder = new LinkDestBuilder();
        linkDestBuilder.setDestNode(dstNode);
        linkDestBuilder.setDestTp(dstTp);
        bierLinkBuilder.setLinkDest(linkDestBuilder.build());
        bierLinkBuilder.setLinkId(linkId);
        return bierLinkBuilder.build();
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
        bierNodeBuilder.setKey(new BierNodeKey(nodeId));
        return bierNodeBuilder.build();
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

    private List<BierTerminationPoint> constructBierTerminationPointList(String tpId1, Long tpIndex1, String tpId2,
                                                                         Long tpIndex2, String tpId3, Long tpIndex3) {
        List<BierTerminationPoint> bierTerminationPointList = constructBierTerminationPointList(tpId1, tpIndex1,
                tpId2, tpIndex2);
        BierTerminationPointBuilder bierTerminationPointBuilder = new BierTerminationPointBuilder();
        bierTerminationPointBuilder.setTpId(tpId3);
        bierTerminationPointBuilder.setTpIndex(tpIndex3);
        bierTerminationPointList.add(bierTerminationPointBuilder.build());
        return bierTerminationPointList;
    }

    private List<BierTerminationPoint> constructBierTerminationPointList(String tpId1, Long tpIndex1, String tpId2,
                                                                         Long tpIndex2, String tpId3, Long tpIndex3,
                                                                         String tpId4, Long tpIndex4) {
        List<BierTerminationPoint> bierTerminationPointList = constructBierTerminationPointList(tpId1, tpIndex1,
                tpId2, tpIndex2, tpId3, tpIndex3);
        BierTerminationPointBuilder bierTerminationPointBuilder = new BierTerminationPointBuilder();
        bierTerminationPointBuilder.setTpId(tpId4);
        bierTerminationPointBuilder.setTpIndex(tpIndex4);
        bierTerminationPointList.add(bierTerminationPointBuilder.build());
        return bierTerminationPointList;
    }

    private TeDomain constructTeDomain(DomainId domainId, SubDomainId subDomainId, Integer si, List<Integer> bpList,
                                       List<String> tpIdList) {
        TeBpBuilder teBpBuilder = new TeBpBuilder();
        List<TeBp> teBpList = new ArrayList<>();
        for (int i = 0;i < bpList.size();i++) {
            teBpBuilder.setBitposition(bpList.get(i));
            teBpBuilder.setTpId(tpIdList.get(i));
            teBpList.add(teBpBuilder.build());
        }

        TeSiBuilder teSiBuilder = new TeSiBuilder();
        teSiBuilder.setSi(new Si(si));
        teSiBuilder.setTeBp(teBpList);
        List<TeSi> teSiList = new ArrayList<>();
        teSiList.add(teSiBuilder.build());

        TeBslBuilder teBslBuilder = new TeBslBuilder();
        teBslBuilder.setBitstringlength(Bsl._64Bit);
        teBslBuilder.setTeSi(teSiList);
        List<TeBsl> teBslList = new ArrayList<>();
        teBslList.add(teBslBuilder.build());

        TeSubDomainBuilder teSubDomainBuilder = new TeSubDomainBuilder();
        teSubDomainBuilder.setSubDomainId(subDomainId);
        teSubDomainBuilder.setTeBsl(teBslList);
        List<TeSubDomain> teSubDomainList = new ArrayList<>();
        teSubDomainList.add(teSubDomainBuilder.build());

        TeDomainBuilder teDomainBuilder = new TeDomainBuilder();
        teDomainBuilder.setDomainId(domainId);
        teDomainBuilder.setTeSubDomain(teSubDomainList);
        return teDomainBuilder.build();
    }

    private BierTeNodeParams constructBierTeNodeParams(TeDomain teDomain) {
        List<TeDomain> teDomainList = new ArrayList<>();
        teDomainList.add(teDomain);
        BierTeNodeParamsBuilder bierTeNodeParamsBuilder = new BierTeNodeParamsBuilder();
        bierTeNodeParamsBuilder.setTeDomain(teDomainList);
        return bierTeNodeParamsBuilder.build();
    }

    private class BierTeBiftWriterMock implements BierTeBiftWriter {

        private List<TeInfo> teInfoAddedList = new ArrayList<>();
        private List<TeInfo> teInfoModifiedList = new ArrayList<>();

        @Override
        public ConfigurationResult writeTeBift(ConfigurationType type, String nodeId, TeInfo teInfo) {
            switch (type) {
                case ADD:
                    teInfoAddedList.add(teInfo);
                    break;
                case MODIFY:
                    teInfoModifiedList.add(teInfo);
                    break;
                case DELETE:
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }
    }

    private class BierTeBtaftWriterMock implements BierTeBtaftWriter {

        List<Btaft> btaftAddedList = new ArrayList<>();
        List<Btaft> btaftDeletedList = new ArrayList<>();

        @Override
        public ConfigurationResult writeBierTeBtaft(ConfigurationType type, String nodeId, SubDomainId subDomainId,
                                                    Btaft btaft) {
            switch (type) {
                case ADD:
                    btaftAddedList.add(btaft);
                    break;
                case MODIFY:
                    break;
                case DELETE:
                    btaftDeletedList.add(btaft);
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }
    }

    private class BierPceServiceMock implements BierPceService {

        @Override
        public Future<RpcResult<QueryChannelThroughPortOutput>> queryChannelThroughPort(
                QueryChannelThroughPortInput queryChannelThroughPortInput) {
            return null;
        }

        @Override
        public Future<RpcResult<QueryTeFrrPathOutput>> queryTeFrrPath(QueryTeFrrPathInput queryTeFrrPathInput) {
            return null;
        }

        @Override
        public Future<RpcResult<CreateBierPathOutput>> createBierPath(CreateBierPathInput createBierPathInput) {
            return null;
        }

        @Override
        public Future<RpcResult<RemoveBierPathOutput>> removeBierPath(RemoveBierPathInput removeBierPathInput) {
            return null;
        }

        @Override
        public Future<RpcResult<Void>> removeTeFrrPath(RemoveTeFrrPathInput removeTeFrrPathInput) {
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        @Override
        public Future<RpcResult<QueryBierPathOutput>> queryBierPath(QueryBierPathInput queryBierPathInput) {
            return null;
        }

        @Override
        public Future<RpcResult<QueryBierInstancePathOutput>> queryBierInstancePath(
                QueryBierInstancePathInput queryBierInstancePathInput) {
            return null;
        }

        @Override
        public Future<RpcResult<CreateTeFrrPathOutput>> createTeFrrPath(CreateTeFrrPathInput createTeFrrPathInput) {
            TeFrrKey teFrrKey = createTeFrrPathInput.getTeFrrKey();
            if (teFrrKey.getProtectedLink().getLinkId().equals("001")) {
                NextHopPathBuilder nextHopPathBuilder = new NextHopPathBuilder();
                nextHopPathBuilder.setDestinationNode("002");
                List<Path> nhPathList = new ArrayList<>();
                nhPathList.add(new PathBuilder(constructBierLink("005","001","003","004","010")).build());
                nhPathList.add(new PathBuilder(constructBierLink("007","004","011","002","006")).build());
                nextHopPathBuilder.setPath(nhPathList);

                NextNextHopPathBuilder nextNextHopPathBuilder1 = new NextNextHopPathBuilder();
                nextNextHopPathBuilder1.setDestinationNode("004");
                List<Path> nnhPathList1 = new ArrayList<>();
                nnhPathList1.add(new PathBuilder(constructBierLink("005","001","003","004","010")).build());
                nextNextHopPathBuilder1.setPath(nnhPathList1);
                NextNextHopPathBuilder nextNextHopPathBuilder2 = new NextNextHopPathBuilder();
                nextNextHopPathBuilder2.setDestinationNode("003");
                List<Path> nnhPathList2 = new ArrayList<>();
                nnhPathList2.add(new PathBuilder(constructBierLink("005","001","003","004","010")).build());
                nnhPathList2.add(new PathBuilder(constructBierLink("007","004","011","002","006")).build());
                nnhPathList2.add(new PathBuilder(constructBierLink("003","002","005","003","008")).build());
                nextNextHopPathBuilder2.setPath(nnhPathList2);
                List<Path> nnhPathList3 = new ArrayList<>();
                nnhPathList3.add(new PathBuilder(constructBierLink("005","001","003","004","010")).build());
                nnhPathList3.add(new PathBuilder(constructBierLink("007","004","011","002","006")).build());
                nnhPathList3.add(new PathBuilder(constructBierLink("012","002","020","005","018")).build());
                NextNextHopPathBuilder nextNextHopPathBuilder3 = new NextNextHopPathBuilder();
                nextNextHopPathBuilder3.setPath(nnhPathList3);
                List<NextNextHopPath> nextNextHopPathList = new ArrayList<>();
                nextNextHopPathList.add(nextNextHopPathBuilder1.build());
                nextNextHopPathList.add(nextNextHopPathBuilder2.build());
                nextNextHopPathList.add(nextNextHopPathBuilder3.build());

                List<ExcludingLink> excludingLinkList = new ArrayList<>();
                excludingLinkList.add(new ExcludingLinkBuilder(constructBierLink("001","001","002","002","004"))
                        .build());
                excludingLinkList.add(new ExcludingLinkBuilder(constructBierLink("008","002","006","004","011"))
                        .build());
                excludingLinkList.add(new ExcludingLinkBuilder(constructBierLink("003","002","005","003","008"))
                        .build());
                FrrPathBuilder frrPathBuilder = new FrrPathBuilder();
                frrPathBuilder.setNextHopPath(nextHopPathBuilder.build());
                frrPathBuilder.setNextNextHopPath(nextNextHopPathList);
                frrPathBuilder.setExcludingLink(excludingLinkList);
                CreateTeFrrPathOutputBuilder builder = new CreateTeFrrPathOutputBuilder();
                builder.setFrrPath(frrPathBuilder.build());
                return RpcResultBuilder.success(builder.build()).buildFuture();
            } else if (teFrrKey.getProtectedLink().getLinkId().equals("005")) {
                NextHopPathBuilder nextHopPathBuilder = new NextHopPathBuilder();
                nextHopPathBuilder.setDestinationNode("004");
                List<Path> nhPathList = new ArrayList<>();
                nhPathList.add(new PathBuilder(constructBierLink("001","001","002","002","004")).build());
                nhPathList.add(new PathBuilder(constructBierLink("008","002","006","004","011")).build());
                nextHopPathBuilder.setPath(nhPathList);

                NextNextHopPathBuilder nextNextHopPathBuilder1 = new NextNextHopPathBuilder();
                nextNextHopPathBuilder1.setDestinationNode("002");
                List<Path> nnhPathList1 = new ArrayList<>();
                nnhPathList1.add(new PathBuilder(constructBierLink("001","001","002","002","004")).build());
                nextNextHopPathBuilder1.setPath(nnhPathList1);
                List<NextNextHopPath> nextNextHopPathList = new ArrayList<>();
                nextNextHopPathList.add(nextNextHopPathBuilder1.build());

                List<ExcludingLink> excludingLinkList = new ArrayList<>();
                excludingLinkList.add(new ExcludingLinkBuilder(constructBierLink("005","001","003","004","010"))
                        .build());
                excludingLinkList.add(new ExcludingLinkBuilder(constructBierLink("007","004","011","002","006"))
                        .build());
                FrrPathBuilder frrPathBuilder = new FrrPathBuilder();
                frrPathBuilder.setNextHopPath(nextHopPathBuilder.build());
                frrPathBuilder.setNextNextHopPath(nextNextHopPathList);
                frrPathBuilder.setExcludingLink(excludingLinkList);
                CreateTeFrrPathOutputBuilder builder = new CreateTeFrrPathOutputBuilder();
                builder.setFrrPath(frrPathBuilder.build());
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
            return null;
        }
    }
}
