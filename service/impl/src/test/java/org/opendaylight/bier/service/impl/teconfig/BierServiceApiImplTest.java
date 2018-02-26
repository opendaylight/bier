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

import org.junit.Assert;
import org.junit.Test;
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
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelKey;
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
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveTeFrrPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferKey;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringInput;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringOutput;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.input.TargetNodeIds;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.input.TargetNodeIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.input.TargetNodeIdsKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.bier.domain.BierSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.bier.domain.BierSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.bier.domain.BierSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainKey;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobalBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BierServiceApiImplTest extends AbstractDataBrokerTest {
    private static final Logger LOG = LoggerFactory.getLogger(BierServiceApiImplTest.class);
    private static final String TOPOLOGY_ID = "example-linkstate-topology";
    private BierServiceApiImpl bierServiceApiImpl;
    private BierPceServiceMock bierPceServiceMock;

    private void setUp() {
        RpcConsumerRegistry rpcConsumerRegistry;
        rpcConsumerRegistry = mock(RpcConsumerRegistry.class);
        bierPceServiceMock = new BierPceServiceMock();
        when(rpcConsumerRegistry.getRpcService(BierPceService.class))
                .thenReturn(bierPceServiceMock);


        bierServiceApiImpl = new BierServiceApiImpl(getDataBroker(), rpcConsumerRegistry);
    }

    @Test
    public void bierServiceApiImplTest() {

        setUp();

        //BIER topology
        constructBierTopologyAndPutToDataStore();

        //BIER-TE Channel
        Channel channel = constructChannel();
        putChannelToDataStore(channel);

        GetTargetBitstringInput input = getGetTargetBitstringInputBuilder(channel);
        Future<RpcResult<GetTargetBitstringOutput>> future = bierServiceApiImpl.getTargetBitstring(
                input);
        try {
            GetTargetBitstringOutput output = future.get().getResult();
            Assert.assertNotNull(output);
            LOG.info(output.toString());
        } catch (InterruptedException | ExecutionException e) {
            LOG.info(Arrays.toString(e.getStackTrace()));
        }
    }

    private GetTargetBitstringInput getGetTargetBitstringInputBuilder(Channel channel) {
        GetTargetBitstringInputBuilder inputBuilder = new GetTargetBitstringInputBuilder();
        inputBuilder.setChannelName(channel.getName());
        inputBuilder.setTopologyId(TOPOLOGY_ID);
        List<TargetNodeIds> targetNodeIdsList = new ArrayList<>();
        for (EgressNode egressNode : channel.getEgressNode()) {
            TargetNodeIdsBuilder targetNodeIdsBuilder = new TargetNodeIdsBuilder();
            targetNodeIdsBuilder.setTargetNodeId(egressNode.getNodeId());
            targetNodeIdsBuilder.setKey(new TargetNodeIdsKey(egressNode.getNodeId()));
            targetNodeIdsList.add(targetNodeIdsBuilder.build());
        }
        inputBuilder.setTargetNodeIds(targetNodeIdsList);
        return inputBuilder.build();
    }

    private void constructBierTopologyAndPutToDataStore() {
        BierTopologyBuilder bierTopologyBuilder = new BierTopologyBuilder();
        bierTopologyBuilder.setBierNode(constructBierNodeList());
        bierTopologyBuilder.setBierLink(constructBierLinkList());
        bierTopologyBuilder.setBierDomain(constructBierDomain(1,1));
        bierTopologyBuilder.setTopologyId(TOPOLOGY_ID);
        bierTopologyBuilder.setKey(new BierTopologyKey(TOPOLOGY_ID));
        addTopologyToDataStore(bierTopologyBuilder.build());

    }

    private List<BierNode> constructBierNodeList() {
        List<Integer> bpList1 = Arrays.asList((Integer)10,2);
        List<String> tpIdList1 = Arrays.asList("001", "002");
        TeDomain teDomain1 = constructTeDomain(new DomainId(1), new SubDomainId(1), 1, bpList1, tpIdList1);
        BierNode bierNode1 = constructBierNode("001", "Node1", "001", 35, 38,
                constructBierTerminationPointList("001", "fei-0/1/0/1", "002", "fei-0/1/0/2"),
                constructBierNodeParams(1,"1.1.1.1/32",null),
                constructBierTeNodeParams(teDomain1));

        List<Integer> bpList2 = Arrays.asList((Integer)11,3,4,5);
        List<String> tpIdList2 = Arrays.asList("010","003","004","005");
        TeDomain teDomain2 = constructTeDomain(new DomainId(1), new SubDomainId(1), 1, bpList2, tpIdList2);
        BierNode bierNode2 = constructBierNode("002", "Node2", "002", 35, 38,
                constructBierTerminationPointList("003", "fei-0/1/0/3", "004", "fei-0/1/0/4", "005", "fei-0/1/0/5",
                        "010", "fei-0/1/0/10"),
                constructBierNodeParams(1,"2.2.2.2/32",null),
                constructBierTeNodeParams(teDomain2));

        List<Integer> bpList3 = Arrays.asList((Integer)12,6);
        List<String> tpIdList3 = Arrays.asList("007", "006");
        TeDomain teDomain3 = constructTeDomain(new DomainId(1), new SubDomainId(1), 1, bpList3, tpIdList3);
        BierNode bierNode3 = constructBierNode("003", "Node3", "003", 35, 38,
                constructBierTerminationPointList("006", "fei-0/1/0/6", "007", "fei-0/1/0/7"),
                constructBierNodeParams(1,"3.3.3.3/32",null),
                constructBierTeNodeParams(teDomain3));

        List<Integer> bpList4 = Arrays.asList((Integer)13,8);
        List<String> tpIdList4 = Arrays.asList("009", "008");
        TeDomain teDomain4 = constructTeDomain(new DomainId(001), new SubDomainId(001), 1, bpList4, tpIdList4);
        BierNode bierNode4 = constructBierNode("004", "Node4", "004", 35, 38,
                constructBierTerminationPointList("008", "fei-0/1/0/8", "009", "fei-0/1/0/9"),
                constructBierNodeParams(1,"4.4.4.4/32",null),
                constructBierTeNodeParams(teDomain4));

        List<BierNode> bierNodeList = new ArrayList<>();
        bierNodeList.add(bierNode1);
        bierNodeList.add(bierNode2);
        bierNodeList.add(bierNode3);
        bierNodeList.add(bierNode4);
        return bierNodeList;
    }

    private List<BierLink> constructBierLinkList() {
        BierLink bierLink1 = constructBierLink("001","001","002","002","003");
        BierLink bierLink2 = constructBierLink("002","002","003","001","002");
        BierLink bierLink3 = constructBierLink("003","002","004","003","006");
        BierLink bierLink4 = constructBierLink("004","003","006","002","004");
        BierLink bierLink5 = constructBierLink("005","002","005","004","008");
        BierLink bierLink6 = constructBierLink("006","004","008","002","005");
        List<BierLink> bierLinkList = new ArrayList<>();
        bierLinkList.add(bierLink1);
        bierLinkList.add(bierLink2);
        bierLinkList.add(bierLink3);
        bierLinkList.add(bierLink4);
        bierLinkList.add(bierLink5);
        bierLinkList.add(bierLink6);
        return bierLinkList;
    }

    private BierNode constructBierNode(String nodeId, String name, String routerId, int latitude,
                                       int longitude, List<BierTerminationPoint> bierTerminationPointList,
                                       BierNodeParams bierNodeParams, BierTeNodeParams bierTeNodeParams) {
        BierNodeBuilder bierNodeBuilder = new BierNodeBuilder();

        bierNodeBuilder.setNodeId(nodeId);
        bierNodeBuilder.setName(name);
        bierNodeBuilder.setRouterId(routerId);
        bierNodeBuilder.setLatitude(BigInteger.valueOf(latitude));
        bierNodeBuilder.setLongitude(BigInteger.valueOf(longitude));
        bierNodeBuilder.setBierTerminationPoint(bierTerminationPointList);
        bierNodeBuilder.setBierNodeParams(bierNodeParams);
        bierNodeBuilder.setBierTeNodeParams(bierTeNodeParams);

        bierNodeBuilder.setKey(new BierNodeKey(nodeId));
        return bierNodeBuilder.build();
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

    private Channel constructChannel() {
        ChannelBuilder channelBuilder = new ChannelBuilder();
        channelBuilder.setName("test-channel");
        channelBuilder.setBpAssignmentStrategy(BpAssignmentStrategy.Manual);
        channelBuilder.setBierForwardingType(BierForwardingType.BierTe);
        channelBuilder.setDomainId(new DomainId(1));
        channelBuilder.setSubDomainId(new SubDomainId(1));

        channelBuilder.setIngressNode("001");
        channelBuilder.setSrcTp("001");

        EgressNodeBuilder egressNodeBuilder1 = new EgressNodeBuilder();
        egressNodeBuilder1.setNodeId("003");
        RcvTpBuilder rcvTpBuilder1 = new RcvTpBuilder();
        rcvTpBuilder1.setTp("007");
        List<RcvTp> rcvTpList1 = new ArrayList<>();
        rcvTpList1.add(rcvTpBuilder1.build());
        egressNodeBuilder1.setRcvTp(rcvTpList1);
        List<EgressNode> egressNodeList = new ArrayList<>();
        egressNodeList.add(egressNodeBuilder1.build());
        EgressNodeBuilder egressNodeBuilder2 = new EgressNodeBuilder();
        egressNodeBuilder2.setNodeId("004");
        RcvTpBuilder rcvTpBuilder2 = new RcvTpBuilder();
        rcvTpBuilder2.setTp("009");
        List<RcvTp> rcvTpList2 = new ArrayList<>();
        rcvTpList2.add(rcvTpBuilder2.build());
        egressNodeBuilder2.setRcvTp(rcvTpList2);
        egressNodeList.add(egressNodeBuilder2.build());

        channelBuilder.setEgressNode(egressNodeList);

        return channelBuilder.build();
    }

    private BierTeNodeParams constructBierTeNodeParams(TeDomain teDomain) {
        List<TeDomain> teDomainList = new ArrayList<>();
        teDomainList.add(teDomain);
        BierTeNodeParamsBuilder bierTeNodeParamsBuilder = new BierTeNodeParamsBuilder();
        bierTeNodeParamsBuilder.setTeDomain(teDomainList);
        return bierTeNodeParamsBuilder.build();
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


    private List<BierTerminationPoint> constructBierTerminationPointList(String tpId1, String ifName1, String tpId2,
                                                                         String ifName2) {
        BierTerminationPointBuilder bierTerminationPointBuilder1 = new BierTerminationPointBuilder();
        bierTerminationPointBuilder1.setTpId(tpId1);
        bierTerminationPointBuilder1.setIfName(ifName1);
        BierTerminationPointBuilder bierTerminationPointBuilder2 = new BierTerminationPointBuilder();
        bierTerminationPointBuilder2.setTpId(tpId2);
        bierTerminationPointBuilder2.setIfName(ifName2);
        List<BierTerminationPoint> bierTerminationPointList = new ArrayList<>();
        bierTerminationPointList.add(bierTerminationPointBuilder1.build());
        bierTerminationPointList.add(bierTerminationPointBuilder2.build());
        return bierTerminationPointList;
    }

    private List<BierTerminationPoint> constructBierTerminationPointList(String tpId1, String ifName1, String tpId2,
                                                                         String ifName2, String tpId3, String ifName3) {
        List<BierTerminationPoint> bierTerminationPointList = constructBierTerminationPointList(tpId1, ifName1,
                tpId2, ifName2);
        BierTerminationPointBuilder bierTerminationPointBuilder = new BierTerminationPointBuilder();
        bierTerminationPointBuilder.setTpId(tpId3);
        bierTerminationPointBuilder.setIfName(ifName3);
        bierTerminationPointList.add(bierTerminationPointBuilder.build());
        return bierTerminationPointList;
    }

    private List<BierTerminationPoint> constructBierTerminationPointList(String tpId1, String ifName1, String tpId2,
                                                                         String ifName2, String tpId3, String ifName3,
                                                                         String tpId4, String ifName4) {
        List<BierTerminationPoint> bierTerminationPointList = constructBierTerminationPointList(tpId1, ifName1,
                tpId2, ifName2, tpId3, ifName3);
        BierTerminationPointBuilder bierTerminationPointBuilder = new BierTerminationPointBuilder();
        bierTerminationPointBuilder.setTpId(tpId4);
        bierTerminationPointBuilder.setIfName(ifName4);
        bierTerminationPointList.add(bierTerminationPointBuilder.build());
        return bierTerminationPointList;
    }

    private BierNodeParams constructBierNodeParams(Integer domainId, String ipv4Address, String ipv6Address) {
        DomainBuilder domainBuilder = new DomainBuilder();
        BierGlobalBuilder bierGlobalBuilder = new BierGlobalBuilder();
        bierGlobalBuilder.setIpv4BfrPrefix(new Ipv4Prefix(ipv4Address));
        if (null != ipv6Address) {
            bierGlobalBuilder.setIpv6BfrPrefix(new Ipv6Prefix(ipv6Address));
        }
        domainBuilder.setBierGlobal(bierGlobalBuilder.build());
        domainBuilder.setDomainId(new DomainId(domainId));
        domainBuilder.setKey(new DomainKey(new DomainId(domainId)));
        List<Domain> domainList = new ArrayList<>();
        domainList.add(domainBuilder.build());
        BierNodeParamsBuilder bierNodeParamsBuilder = new BierNodeParamsBuilder();
        bierNodeParamsBuilder.setDomain(domainList);
        return bierNodeParamsBuilder.build();
    }

    private void putChannelToDataStore(Channel channel) {
        InstanceIdentifier<Channel> identifier = InstanceIdentifier.create(BierNetworkChannel.class)
                .child(BierChannel.class, new BierChannelKey(TOPOLOGY_ID))
                .child(Channel.class, new ChannelKey(channel.getName()));
        ReadWriteTransaction transaction = getDataBroker().newReadWriteTransaction();
        transaction.put(LogicalDatastoreType.CONFIGURATION, identifier, channel, true);
        try {
            transaction.submit().get();
            LOG.info("Put test channel to data store success!");
        } catch (InterruptedException | ExecutionException e) {
            return;
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
        public Future<RpcResult<CreateBierPathOutput>> createBierPath(CreateBierPathInput input) {

            if (input.getChannelName().equals("test-channel")) {
                BferBuilder bferBuilder1 = new BferBuilder();
                bferBuilder1.setBferNodeId("003");
                bferBuilder1.setKey(new BferKey("003"));
                BierPathBuilder bierPathBuilder1 = new BierPathBuilder();
                List<PathLink> pathLinkList1 = new ArrayList<>();
                pathLinkList1.add(new PathLinkBuilder(constructBierLink("001","001","002","002","003")).build());
                pathLinkList1.add(new PathLinkBuilder(constructBierLink("003","002","004","003","006")).build());
                bierPathBuilder1.setPathLink(pathLinkList1);
                bferBuilder1.setBierPath(bierPathBuilder1.build());

                BferBuilder bferBuilder2 = new BferBuilder();
                bferBuilder2.setBferNodeId("004");
                bferBuilder2.setKey(new BferKey("004"));
                BierPathBuilder bierPathBuilder2 = new BierPathBuilder();
                List<PathLink> pathLinkList2 = new ArrayList<>();
                pathLinkList2.add(new PathLinkBuilder(constructBierLink("001","001","002","002","003")).build());
                pathLinkList2.add(new PathLinkBuilder(constructBierLink("005","002","005","004","008")).build());
                bierPathBuilder2.setPathLink(pathLinkList2);
                bferBuilder2.setBierPath(bierPathBuilder2.build());

                List<Bfer> bferList = new ArrayList<>();
                bferList.add(bferBuilder1.build());
                bferList.add(bferBuilder2.build());

                CreateBierPathOutputBuilder outputBuilder = new CreateBierPathOutputBuilder();
                outputBuilder.setChannelName(input.getChannelName());
                outputBuilder.setBfirNodeId(input.getBfirNodeId());
                outputBuilder.setBfer(bferList);

                return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
            }

            LOG.info("Create bier path error!");
            return null;
        }

        @Override
        public Future<RpcResult<RemoveBierPathOutput>> removeBierPath(RemoveBierPathInput removeBierPathInput) {
            return null;
        }

        @Override
        public Future<RpcResult<Void>> removeTeFrrPath(RemoveTeFrrPathInput removeTeFrrPathInput) {
            return null;
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
            return null;
        }
    }




}
