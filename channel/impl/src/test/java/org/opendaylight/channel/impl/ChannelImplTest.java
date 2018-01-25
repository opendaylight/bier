/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.channel.util.ChannelDBContext;
import org.opendaylight.channel.util.ChannelDBUtil;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.AddChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.AddChannelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.AddChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.DeployChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.DeployChannelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.DeployChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.GetChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.GetChannelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.GetChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.GetChannelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.ModifyChannelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.ModifyChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelWithPortInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelWithPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelWithPortOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.RemoveChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.RemoveChannelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.RemoveChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.deploy.channel.input.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.deploy.channel.input.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.deploy.channel.input.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.deploy.channel.input.egress.node.RcvTpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.get.channel.output.ChannelName;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.get.channel.output.ChannelNameBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.with.port.output.QueryChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.with.port.output.QueryChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierForwardingType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BpAssignmentStrategy;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainBuilder;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobalBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;


public class ChannelImplTest extends AbstractConcurrentDataBrokerTest {
    private static final String DEFAULT_TOPO = "example-linkstate-topology";
    private ChannelImpl channelImpl;
    private ChannelDBContext context;

    @Before
    public void setUp() throws Exception {
        context = new ChannelDBContext(getDataBroker());
        channelImpl = new ChannelImpl(context);
        channelImpl.start();
        configBierNetworkTopology();
    }

    @After
    public void tearDown() throws Exception {

    }

    private void configBierNetworkTopology() {
        List<BierNode> bierNodeList = new ArrayList<>();
        BierNode bierNode1 = buildBierNodeInfo("node1",1,11,new BfrId(1),new BfrId(11));
        BierNode bierNode2 = buildBierNodeInfo("node2",1,11,new BfrId(2),null);
        BierNode bierNode3 = buildBierNodeInfo("node3",1,11,null,new BfrId(33));
        BierNode bierNode4 = buildBierNodeInfo("node4",1,11,new BfrId(4),new BfrId(0));
        BierNode bierNode5 = buildBierNodeInfo("node5",1,11,new BfrId(0),new BfrId(55));
        bierNodeList.add(bierNode1);
        bierNodeList.add(bierNode2);
        bierNodeList.add(bierNode3);
        bierNodeList.add(bierNode4);
        bierNodeList.add(bierNode5);
        BierTopology bierTopology = new BierTopologyBuilder()
                .setTopologyId(DEFAULT_TOPO)
                .setBierNode(bierNodeList)
                .build();

        WriteTransaction wtx = context.newWriteOnlyTransaction();
        wtx.put(LogicalDatastoreType.CONFIGURATION,buildBierTopologyPath(),bierTopology);
        wtx.submit();
    }

    private BierNode buildBierNodeInfo(String node, Integer domainId, Integer subDomainID,
                                       BfrId globalBfrId, BfrId subDomainBfrId) {
        List<Domain> domainList = new ArrayList<>();
        List<SubDomain> subDomainList = new ArrayList<>();
        subDomainList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(subDomainID))
                .setBfrId(subDomainBfrId)
                .build());

        domainList.add(new DomainBuilder()
                .setDomainId(new DomainId(domainId))
                .setBierGlobal(new BierGlobalBuilder().setSubDomain(subDomainList).setBfrId(globalBfrId).build())
                .build());

        List<TeBp> teBpList = new ArrayList<>();
        teBpList.add(new TeBpBuilder().setTpId("tp1").build());
        teBpList.add(new TeBpBuilder().setTpId("tp2").build());
        teBpList.add(new TeBpBuilder().setTpId("tp3").build());
        teBpList.add(new TeBpBuilder().setTpId("tp4").build());
        List<TeSi> teSiList = new ArrayList<>();
        teSiList.add(new TeSiBuilder().setSi(Si.getDefaultInstance("1")).setTeBp(teBpList).build());
        List<TeBsl> teBslList = new ArrayList<>();
        teBslList.add(new TeBslBuilder().setBitstringlength(Bsl._64Bit).setTeSi(teSiList).build());
        List<TeSubDomain> teSubdomainList = new ArrayList<>();
        teSubdomainList.add(new TeSubDomainBuilder()
                .setSubDomainId(new SubDomainId(subDomainID))
                .setTeBsl(teBslList)
                .build());
        List<TeDomain> teDomainList = new ArrayList<>();
        teDomainList.add(new TeDomainBuilder()
                .setDomainId(new DomainId(domainId))
                .setTeSubDomain(teSubdomainList)
                .build());
        return new BierNodeBuilder()
                .setNodeId(node)
                .setBierNodeParams(new BierNodeParamsBuilder()
                        .setDomain(domainList)
                        .build())
                .setBierTeNodeParams(new BierTeNodeParamsBuilder()
                        .setTeDomain(teDomainList)
                        .build())
                .build();
    }

    private InstanceIdentifier<BierTopology> buildBierTopologyPath() {
        return InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(DEFAULT_TOPO));
    }

    @Test
    public void addChannelBasicTest() throws Exception {
        RpcResult<AddChannelOutput> result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        QueryChannelOutput actulChannel = queryChannel("channel-1");
        QueryChannelOutput expectChannel = buildExpectChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short) 24,
                (short) 30,null,null,null, new ArrayList<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102
                        .query.channel.output.channel.EgressNode>());
        assertChannelData(expectChannel.getChannel(),actulChannel.getChannel());
    }

    @Test
    public void removeChannelBasicTest() throws Exception {
        RpcResult<AddChannelOutput> result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        RpcResult<RemoveChannelOutput> removeResult = removeChannel("channel-1");
        Assert.assertTrue(removeResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        QueryChannelOutput actulChannel = queryChannel("channel-1");
        Assert.assertTrue(actulChannel.getChannel().isEmpty());
        removeResult = removeChannel("channel-1");
        Assert.assertTrue(removeResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
    }

    @Test
    public void modifyChannelBasictest() throws Exception {
        RpcResult<AddChannelOutput> result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        RpcResult<ModifyChannelOutput> modifyResult = modifyChannel("channel-1",null,22,null,"225.1.1.1",
                (short)24,(short)24);
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        QueryChannelOutput actulChannel = queryChannel("channel-1");
        QueryChannelOutput expectChannel = buildExpectChannel("channel-1",1,22,"1.1.1.1","225.1.1.1",(short) 24,
                (short) 24, null,null,null,new ArrayList<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102
                        .query.channel.output.channel.EgressNode>());
        assertChannelData(expectChannel.getChannel(),actulChannel.getChannel());

        modifyResult = modifyChannel("channel-1",2,null,"2.2.2.2",null,(short) 16,(short)24);
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        actulChannel = queryChannel("channel-1");
        expectChannel = buildExpectChannel("channel-1",2,22,"2.2.2.2","225.1.1.1",(short) 16,
                (short) 24, null,null,null, new ArrayList<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102
                        .query.channel.output.channel.EgressNode>());
        assertChannelData(expectChannel.getChannel(),actulChannel.getChannel());
    }

    @Test
    public void deployChannelBasicTest() throws Exception {
        RpcResult<AddChannelOutput> result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        RpcResult<DeployChannelOutput> deployResult = deployChannel("channel-1","tp1","tp2","tp3",
                BierForwardingType.BierTe);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel
                channelData = ChannelDBUtil.getInstance().readChannel("channel-1",DEFAULT_TOPO).get();
        org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel
                expectChannel = buildChannelData("channel-1",1,11,"1.1.1.1","224.1.1.1",false,true);
        assertChannelDbData(expectChannel,channelData);
    }

    @Test
    public void modifyDeployChannelBasicTest() throws Exception {
        RpcResult<AddChannelOutput> result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        RpcResult<DeployChannelOutput> deployResult = deployChannel("channel-1","tp1","tp2","tp3",
                BierForwardingType.BierTe);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        RpcResult<DeployChannelOutput> modifyResult = channelImpl.deployChannel(
                buildModifyDeployChannelInput("channel-1",BierForwardingType.Bier)).get();
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("forwarding-type conflict! can not change forwarding-type,"
                + " when update deploy-channel info.", modifyResult.getResult().getConfigureResult().getErrorCause());

        modifyResult = channelImpl.deployChannel(buildModifyDeployChannelInput("channel-1",BierForwardingType.BierTe))
                .get();
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel
                channelData = ChannelDBUtil.getInstance().readChannel("channel-1",DEFAULT_TOPO).get();
        org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel
                expectChannel = buildChannelData("channel-1",1,11,"1.1.1.1","224.1.1.1",true,true);
        assertChannelDbData(expectChannel,channelData);
    }

    @Test
    public void getChannelTest() throws Exception {
        RpcResult<GetChannelOutput> outputRpcResult = channelImpl.getChannel(null).get();
        Assert.assertFalse(outputRpcResult.isSuccessful());
        Assert.assertEquals("input is null!",outputRpcResult.getErrors().iterator().next().getMessage());

        GetChannelInput input = new GetChannelInputBuilder().build();
        outputRpcResult = channelImpl.getChannel(input).get();
        Assert.assertTrue(outputRpcResult.isSuccessful());
        GetChannelOutput expect = new GetChannelOutputBuilder().setChannelName(new ArrayList<ChannelName>()).build();
        assertgetChannelData(expect,outputRpcResult.getResult());
        RpcResult<AddChannelOutput> result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        result = addChannel("channel-2",1,11,"2.2.2.2","225.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        result = addChannel("channel-3",2,22,"3.3.3.3","226.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        outputRpcResult = channelImpl.getChannel(input).get();

        Assert.assertTrue(outputRpcResult.isSuccessful());
        List<ChannelName> channelNames = new ArrayList<>();
        channelNames.add(new ChannelNameBuilder().setName("channel-1").build());
        channelNames.add(new ChannelNameBuilder().setName("channel-2").build());
        channelNames.add(new ChannelNameBuilder().setName("channel-3").build());
        expect = new GetChannelOutputBuilder().setChannelName(channelNames).build();
        assertgetChannelData(expect,outputRpcResult.getResult());


    }

    @Test
    public void queryChannelTest() throws Exception {
        RpcResult<AddChannelOutput> result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        RpcResult<DeployChannelOutput> deployResult = deployChannel("channel-1","tp1","tp2","tp3",
                BierForwardingType.Bier);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        List<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel.EgressNode>
                egressList = new ArrayList<>();
        List<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel.egress.node
                .RcvTp> rcvTps = new ArrayList<>();
        rcvTps.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel.egress
                .node.RcvTpBuilder().setTp("tp2").build());
        rcvTps.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel.egress
                .node.RcvTpBuilder().setTp("tp3").build());
        egressList.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel
                .EgressNodeBuilder().setNodeId("node2").setRcvTp(rcvTps).build());
        egressList.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel
                .EgressNodeBuilder().setNodeId("node3").setRcvTp(rcvTps).build());

        QueryChannelOutput expectChannel = buildExpectChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short) 24,
                (short) 30, "node1","tp1",BierForwardingType.Bier,egressList);
        QueryChannelOutput actulChannel = queryChannel("channel-1");
        assertQueryChannelData(expectChannel.getChannel().get(0),actulChannel.getChannel().get(0));
    }

    @Test
    public void checkAddChannelInputTest() throws Exception {
        RpcResult<AddChannelOutput> result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,null);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("group-wildcard is null!",result.getResult().getConfigureResult().getErrorCause());

        result = addChannel("channel-1",1,11,"239.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("src-ip is illegal!",result.getResult().getConfigureResult().getErrorCause());

        result = addChannel("channel-1",1,11,"1.1.1.1","220.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("dest-group is not multicast ipaddress!",
                result.getResult().getConfigureResult().getErrorCause());

        result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)33);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("wildcard is invalid!it must be in the range [1,32].",
                result.getResult().getConfigureResult().getErrorCause());

        result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("The Channel already exists!",result.getResult().getConfigureResult().getErrorCause());
    }

    @Test
    public void checkModifyChannelInputTest() throws Exception {
        RpcResult<ModifyChannelOutput> modifyResult = modifyChannel("channel-1",2,22,"2.2.2.2","225.1.1.1",
                (short)24,(short)30);
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("The Channel does not exists!",
                modifyResult.getResult().getConfigureResult().getErrorCause());

        addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        modifyResult = modifyChannel(null,2,22,"2.2.2.2","225.1.1.1",(short)24,(short)30);
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("channel-name is null!",modifyResult.getResult().getConfigureResult().getErrorCause());

        modifyResult = modifyChannel("channel-1",null,11,"239.1.1.1",null,(short)24,(short)30);
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("src-ip is illegal!",modifyResult.getResult().getConfigureResult().getErrorCause());

        modifyResult = modifyChannel("channel-1",null,null,null,"220.1.1.1",null,(short)30);
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("dest-group is not multicast ipaddress!",
                modifyResult.getResult().getConfigureResult().getErrorCause());

        modifyResult = modifyChannel("channel-1",null,null,null,null,(short)35,null);
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("wildcard is invalid!it must be in the range [1,32].",
                modifyResult.getResult().getConfigureResult().getErrorCause());

        deployChannel("channel-1","tp1","tp2","tp3",BierForwardingType.BierTe);
        modifyResult = modifyChannel("channel-1",2,22,"2.2.2.2","225.1.1.1",(short)32,(short)32);
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("The Channel has deployed,can not modify",
                modifyResult.getResult().getConfigureResult().getErrorCause());
    }

    @Test
    public void checkDeployChannelInputTest() throws Exception {
        RpcResult<DeployChannelOutput> deployResult = deployChannel("channel-1","tp1","tp2","tp3",
                BierForwardingType.Bier);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("The Channel does not exists!",
                deployResult.getResult().getConfigureResult().getErrorCause());

        addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        deployResult = deployChannel(null,"tp1","tp2","tp3",BierForwardingType.Bier);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("channel-name is null!",deployResult.getResult().getConfigureResult().getErrorCause());

        deployResult = deployChannel("channel-1",null,"tp2","tp3",BierForwardingType.BierTe);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("src-tp is null!",deployResult.getResult().getConfigureResult().getErrorCause());

        deployResult = deployChannel("channel-1","tp1","tp2","tp3",null);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("bier forwarding type is null!",
                deployResult.getResult().getConfigureResult().getErrorCause());

        List<EgressNode> egressNodes = new ArrayList<>();
        List<RcvTp> rcvTpList = new ArrayList<>();
        rcvTpList.add(new RcvTpBuilder().setTp("tp2").build());
        egressNodes.add(new EgressNodeBuilder().setNodeId("node2").setRcvTp(rcvTpList).build());
        egressNodes.add(new EgressNodeBuilder().setNodeId("node3").setRcvTp(rcvTpList).build());
        deployResult = deployChannelWithBfr("channel-1","node6",egressNodes,"tp1",BierForwardingType.Bier,
                BpAssignmentStrategy.Manual);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("ingress-node is not in this sub-domain!",
                deployResult.getResult().getConfigureResult().getErrorCause());

        egressNodes.add(new EgressNodeBuilder().setNodeId("node7").setRcvTp(rcvTpList).build());
        deployResult = deployChannelWithBfr("channel-1","node1",egressNodes,"tp1",BierForwardingType.BierTe,
                BpAssignmentStrategy.Automatic);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("egress-node is not in this sub-domain!",
                deployResult.getResult().getConfigureResult().getErrorCause());
        egressNodes.remove(2);

        deployResult = deployChannelWithBfr("channel-1","node1",egressNodes,"tp5",BierForwardingType.BierTe,
                BpAssignmentStrategy.Manual);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("src-tp is not in this sub-domain!",
                deployResult.getResult().getConfigureResult().getErrorCause());

        rcvTpList.add(new RcvTpBuilder().setTp("tp6").build());
        deployResult = deployChannelWithBfr("channel-1","node1",egressNodes,"tp1",BierForwardingType.BierTe,
                BpAssignmentStrategy.Manual);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("rcv-tp is not in this sub-domain!",
                deployResult.getResult().getConfigureResult().getErrorCause());
        rcvTpList.remove(1);

        deployResult = deployChannelWithBfr("channel-1","node2",egressNodes,"tp1",BierForwardingType.Bier,
                BpAssignmentStrategy.Automatic);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("ingress-node and egress-nodes conflict!the node must not be both ingress and egress.",
                deployResult.getResult().getConfigureResult().getErrorCause());

        deployResult = deployChannelWithBfr("channel-1","node1",egressNodes,"tp1",BierForwardingType.BierTe,
                BpAssignmentStrategy.Manual);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        deployResult = deployChannelWithBfr("channel-1","node1",egressNodes,"tp1",BierForwardingType.BierTe,
                BpAssignmentStrategy.Automatic);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("assignment-strategy conflict! can not change strategy, when update deploy-channel info.",
                deployResult.getResult().getConfigureResult().getErrorCause());
    }


    @Test
    public void checkRemoveChannelInputTest() throws Exception {
        RpcResult<RemoveChannelOutput> removeResult = channelImpl.removeChannel(null).get();
        Assert.assertTrue(removeResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("Input is null!",removeResult.getResult().getConfigureResult().getErrorCause());

        removeResult = removeChannel(null);
        Assert.assertTrue(removeResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("channel-name is null!",removeResult.getResult().getConfigureResult().getErrorCause());
    }

    @Test
    public void checkQueryChannelInputTest() throws Exception {
        RpcResult<QueryChannelOutput> result = channelImpl.queryChannel(new QueryChannelInputBuilder().build()).get();
        Assert.assertFalse(result.isSuccessful());
        Assert.assertEquals("input or channel-name is null!",result.getErrors().iterator().next().getMessage());
    }


    @Test
    public void queryChannelWithPortTest() throws Exception {
        addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        addChannel("channel-2",1,11,"2.2.2.2","225.1.1.1",(short)24,(short)30);
        addChannel("channel-3",1,11,"1.1.1.1","226.1.1.1",(short)24,(short)30);

        RpcResult<QueryChannelWithPortOutput> output = queryChannelWithPort(1,11,null,"tp1");
        Assert.assertTrue(!output.isSuccessful());
        Assert.assertEquals("input is null, or domain, sub-domain, node-id, tp-id is null!",
                output.getErrors().iterator().next().getMessage());

        output = queryChannelWithPort(1,11,"node1","tp1");
        Assert.assertTrue(output.isSuccessful());
        Assert.assertTrue(output.getResult().getQueryChannel().isEmpty());


        deployChannel("channel-1","tp1","tp2","tp3", BierForwardingType.BierTe);
        deployChannel("channel-2","tp2","tp3","tp4", BierForwardingType.BierTe);
        deployChannel("channel-3","tp3","tp1",null, BierForwardingType.BierTe);

        output = queryChannelWithPort(1,11,"node1","tp1");
        Assert.assertTrue(output.isSuccessful());
        Assert.assertEquals(1,output.getResult().getQueryChannel().size());
        QueryChannel exChannel = new QueryChannelBuilder()
                .setChannelName("channel-1").setBfir("node1").setIsRcvTp(false).build();
        Assert.assertTrue(output.getResult().getQueryChannel().contains(exChannel));

        output = queryChannelWithPort(1,11,"node3","tp4");
        Assert.assertTrue(output.isSuccessful());
        Assert.assertEquals(1,output.getResult().getQueryChannel().size());
        exChannel = new QueryChannelBuilder()
                .setChannelName("channel-2").setBfir("node1").setIsRcvTp(true).build();
        Assert.assertTrue(output.getResult().getQueryChannel().contains(exChannel));

        output = queryChannelWithPort(1,11,"node3","tp3");
        Assert.assertTrue(output.isSuccessful());
        Assert.assertEquals(2,output.getResult().getQueryChannel().size());
        exChannel = new QueryChannelBuilder()
                .setChannelName("channel-1").setBfir("node1").setIsRcvTp(true).build();
        Assert.assertTrue(output.getResult().getQueryChannel().contains(exChannel));
        exChannel = new QueryChannelBuilder()
                .setChannelName("channel-2").setBfir("node1").setIsRcvTp(true).build();
        Assert.assertTrue(output.getResult().getQueryChannel().contains(exChannel));

        output = queryChannelWithPort(2,11,"node3","tp3");
        Assert.assertTrue(output.isSuccessful());
        Assert.assertTrue(output.getResult().getQueryChannel().isEmpty());

    }

    private RpcResult<QueryChannelWithPortOutput> queryChannelWithPort(Integer domainId, Integer subDomainId,
                                                                      String node, String tp)
            throws ExecutionException, InterruptedException {
        QueryChannelWithPortInput input = new QueryChannelWithPortInputBuilder()
                .setDomainId(new DomainId(domainId))
                .setSubDomainId(new SubDomainId(subDomainId))
                .setNodeId(node)
                .setTpId(tp)
                .build();
        return channelImpl.queryChannelWithPort(input).get();
    }

    private void assertQueryChannelData(Channel expectChannel, Channel actualChannel) {
        Assert.assertEquals(expectChannel.getEgressNode(), actualChannel.getEgressNode());
        Assert.assertEquals(expectChannel.getIngressNode(),actualChannel.getIngressNode());
        Assert.assertEquals(expectChannel.getDomainId(),actualChannel.getDomainId());
        Assert.assertEquals(expectChannel.getDstGroup(),actualChannel.getDstGroup());
        Assert.assertEquals(expectChannel.getGroupWildcard(),actualChannel.getGroupWildcard());
        Assert.assertEquals(expectChannel.getSourceWildcard(),actualChannel.getSourceWildcard());
        Assert.assertEquals(expectChannel.getSrcIp(),actualChannel.getSrcIp());
        Assert.assertEquals(expectChannel.getSubDomainId(),actualChannel.getSubDomainId());
    }

    private void assertgetChannelData(GetChannelOutput expect, GetChannelOutput actual) {
        Assert.assertEquals(expect.getChannelName().size(),actual.getChannelName().size());
        Assert.assertEquals(expect.getChannelName(), actual.getChannelName());
    }

    private DeployChannelInput buildModifyDeployChannelInput(String channelName,BierForwardingType type) {
        List<RcvTp> rcvTps = new ArrayList<>();
        rcvTps.add(new RcvTpBuilder().setTp("tp2").build());
        rcvTps.add(new RcvTpBuilder().setTp("tp3").build());
        rcvTps.add(new RcvTpBuilder().setTp("tp4").build());
        List<EgressNode> egresNodes = new ArrayList<>();
        egresNodes.add(new EgressNodeBuilder().setNodeId("node3").setRcvTp(rcvTps).build());
        egresNodes.add(new EgressNodeBuilder().setNodeId("node4").setRcvTp(rcvTps).build());
        egresNodes.add(new EgressNodeBuilder().setNodeId("node5").setRcvTp(rcvTps).build());
        return new DeployChannelInputBuilder()
                .setChannelName(channelName)
                .setIngressNode("node2")
                .setEgressNode(egresNodes)
                .setBierForwardingType(type)
                .setSrcTp("tp1")
                .build();
    }

    private void assertChannelDbData(org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                                             .channel.Channel expectChannel,
                                     org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                                             .channel.Channel channelData) {
        Assert.assertEquals(expectChannel.getEgressNode(), channelData.getEgressNode());
        Assert.assertEquals(expectChannel.getIngressNode(),channelData.getIngressNode());
        Assert.assertEquals(expectChannel.getBierForwardingType(),channelData.getBierForwardingType());
        Assert.assertEquals(expectChannel.getSrcTp(),channelData.getSrcTp());
        Assert.assertEquals(expectChannel.getDomainId(),channelData.getDomainId());
        Assert.assertEquals(expectChannel.getDstGroup(),channelData.getDstGroup());
        Assert.assertEquals(expectChannel.getGroupWildcard(),channelData.getGroupWildcard());
        Assert.assertEquals(expectChannel.getSourceWildcard(),channelData.getSourceWildcard());
        Assert.assertEquals(expectChannel.getSrcIp(),channelData.getSrcIp());
        Assert.assertEquals(expectChannel.getSubDomainId(),channelData.getSubDomainId());
    }


    private org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel
            .Channel buildChannelData(String channelName, Integer domainId, Integer subDomainId, String srcIp,
                                     String groupIp, boolean modify, boolean isBierTe) {
        org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel
                .ChannelBuilder channelBuilder = new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier
                .network.channel.bier.channel.ChannelBuilder();
        List<org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel
                .EgressNode> egressNodes = new ArrayList<>();
        List<org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel
                .egress.node.RcvTp> rcvTps = new ArrayList<>();
        if (modify) {
            channelBuilder.setIngressNode("node2");
            channelBuilder.setIngressBfrId(new BfrId(11));
            rcvTps.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel
                    .channel.egress.node.RcvTpBuilder().setTp("tp2").build());
            rcvTps.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel
                    .channel.egress.node.RcvTpBuilder().setTp("tp3").build());
            rcvTps.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel
                    .channel.egress.node.RcvTpBuilder().setTp("tp4").build());
            channelBuilder.setIngressBfrId(new BfrId(2));
            egressNodes.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                    .channel.channel.EgressNodeBuilder().setNodeId("node3").setRcvTp(rcvTps)
                    .setEgressBfrId( new BfrId(33)).build());
            egressNodes.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                    .channel.channel.EgressNodeBuilder().setNodeId("node4").setRcvTp(rcvTps)
                    .setEgressBfrId(new BfrId(4)).build());
            egressNodes.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                    .channel.channel.EgressNodeBuilder().setNodeId("node5").setRcvTp(rcvTps)
                    .setEgressBfrId(new BfrId(55)).build());
        } else {

            rcvTps.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel
                    .channel.egress.node.RcvTpBuilder().setTp("tp2").build());
            rcvTps.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel
                    .channel.egress.node.RcvTpBuilder().setTp("tp3").build());
            channelBuilder.setIngressNode("node1");
            channelBuilder.setIngressBfrId(new BfrId(11));
            egressNodes.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                    .channel.channel.EgressNodeBuilder().setNodeId("node2").setRcvTp(rcvTps)
                    .setEgressBfrId(new BfrId(2)).build());
            egressNodes.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                    .channel.channel.EgressNodeBuilder().setNodeId("node3").setRcvTp(rcvTps)
                    .setEgressBfrId(new BfrId(33)).build());

        }
        channelBuilder.setName(channelName);
        channelBuilder.setBierForwardingType(BierForwardingType.BierTe);
        channelBuilder.setSrcTp("tp1");
        channelBuilder.setDomainId(new DomainId(domainId));
        channelBuilder.setSubDomainId(new SubDomainId(subDomainId));
        channelBuilder.setSrcIp(new IpAddress(new Ipv4Address(srcIp)));
        channelBuilder.setDstGroup(new IpAddress(new Ipv4Address(groupIp)));
        channelBuilder.setSourceWildcard((short) 24);
        channelBuilder.setGroupWildcard((short) 30);
        channelBuilder.setEgressNode(egressNodes);
        return channelBuilder.build();
    }

    private RpcResult<DeployChannelOutput> deployChannel(String channelName, String srcTp, String rcvTp1, String rcvTp2,
                                                         BierForwardingType forwardingType)
            throws ExecutionException, InterruptedException {
        List<EgressNode> egressNodes = new ArrayList<>();
        List<RcvTp> rcvTps = new ArrayList<>();
        rcvTps.add(new RcvTpBuilder().setTp(rcvTp1).build());
        rcvTps.add(new RcvTpBuilder().setTp(rcvTp2).build());
        egressNodes.add(new EgressNodeBuilder()
                .setNodeId("node2")
                .setRcvTp((rcvTp1 == null && rcvTp2 == null) ? null : rcvTps)
                .build());
        egressNodes.add(new EgressNodeBuilder()
                .setNodeId("node3")
                .setRcvTp((rcvTp1 == null && rcvTp2 == null) ? null : rcvTps)
                .build());
        DeployChannelInput input = new DeployChannelInputBuilder()
                .setChannelName(channelName)
                .setBierForwardingType(forwardingType)
                .setBpAssignmentStrategy(BpAssignmentStrategy.Manual)
                .setIngressNode("node1")
                .setSrcTp(srcTp)
                .setEgressNode(egressNodes)
                .build();
        return channelImpl.deployChannel(input).get();
    }

    private RpcResult<DeployChannelOutput> deployChannelWithBfr(String channelName, String ingressNode,
                                                                List<EgressNode> egressNodes, String srcTp,
                                                                BierForwardingType type,
                                                                BpAssignmentStrategy bpStrategy)
            throws ExecutionException, InterruptedException {
        DeployChannelInput input = new DeployChannelInputBuilder()
                .setChannelName(channelName)
                .setBierForwardingType(type)
                .setBpAssignmentStrategy(bpStrategy)
                .setSrcTp(srcTp)
                .setIngressNode(ingressNode)
                .setEgressNode(egressNodes)
                .build();
        return channelImpl.deployChannel(input).get();
    }

    private RpcResult<ModifyChannelOutput> modifyChannel(String channelName, Integer domainId, Integer subDomainId,
                                                         String srcIp, String groupIp, Short srcWildcard,
                                                         Short groupWildcard)
            throws ExecutionException, InterruptedException {
        ModifyChannelInputBuilder input = new ModifyChannelInputBuilder();
        input.setName(channelName);
        if (domainId != null) {
            input.setDomainId(new DomainId(domainId));
        }
        if (subDomainId != null) {
            input.setSubDomainId(new SubDomainId(subDomainId));
        }
        if (srcIp != null) {
            input.setSrcIp(new IpAddress(new Ipv4Address(srcIp)));
        }
        if (srcWildcard != null) {
            input.setSourceWildcard(srcWildcard);
        }
        if (groupIp != null) {
            input.setDstGroup(new IpAddress(new Ipv4Address(groupIp)));
        }
        if (groupWildcard != null) {
            input.setGroupWildcard(groupWildcard);
        }
        return channelImpl.modifyChannel(input.build()).get();
    }

    private void assertChannelData(List<Channel> expectChannel, List<Channel> actulChannel) {
        Assert.assertEquals(expectChannel.size(),actulChannel.size());
        for (Channel channel : actulChannel) {
            Assert.assertTrue(expectChannel.contains(channel));
        }
    }

    private QueryChannelOutput buildExpectChannel(String channelName, Integer domainId, Integer subDomainID,
                                                  String srcIp, String groupIp, Short srcWildcard, Short groupWildcard,
                                                  String ingressNode,String srcTp,BierForwardingType type,
                                                  List<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102
                                                          .query.channel.output.channel.EgressNode> egressList) {
        List<Channel> channels = new ArrayList<>();
        Channel channel = new ChannelBuilder()
                .setName(channelName)
                .setBierForwardingType(type)
                .setSrcTp(srcTp)
                .setDomainId(new DomainId(domainId))
                .setSubDomainId(new SubDomainId(subDomainID))
                .setSrcIp(new IpAddress(new Ipv4Address(srcIp)))
                .setDstGroup(new IpAddress(new Ipv4Address(groupIp)))
                .setSourceWildcard(srcWildcard)
                .setGroupWildcard(groupWildcard)
                .setIngressNode(ingressNode)
                .setEgressNode(egressList)
                .build();
        channels.add(channel);
        return new QueryChannelOutputBuilder().setChannel(channels).build();
    }


    private QueryChannelOutput queryChannel(String channelName) throws Exception {
        List<String> channels = new ArrayList<>();
        channels.add(channelName);
        QueryChannelInput input = new QueryChannelInputBuilder().setChannelName(channels).build();
        RpcResult<QueryChannelOutput> result = channelImpl.queryChannel(input).get();
        Assert.assertTrue(result.isSuccessful());
        return result.getResult();
    }

    private RpcResult<AddChannelOutput> addChannel(String channelName, Integer domainId, Integer subDomainID,
                                                   String srcIp, String groupIp,
                                                   Short srcWildcard, Short groupWildcard) throws Exception {
        AddChannelInput input = buildAddChannelInput(channelName,domainId,subDomainID,srcIp,groupIp,
                srcWildcard,groupWildcard);
        return channelImpl.addChannel(input).get();
    }

    private AddChannelInput buildAddChannelInput(String channelName, Integer domainId, Integer subDomainID,
                                                 String srcIp, String groupIp, Short srcWildcard, Short groupWildcard) {
        return new AddChannelInputBuilder()
                .setName(channelName)
                .setDomainId(new DomainId(domainId))
                .setSubDomainId(new SubDomainId(subDomainID))
                .setSrcIp(new IpAddress(new Ipv4Address(srcIp)))
                .setDstGroup(new IpAddress(new Ipv4Address(groupIp)))
                .setSourceWildcard(srcWildcard)
                .setGroupWildcard(groupWildcard)
                .build();
    }

    private  RpcResult<RemoveChannelOutput> removeChannel(String channelName) throws Exception {
        RemoveChannelInput input = buildRemoveChannelInput(channelName);
        return channelImpl.removeChannel(input).get();
    }

    private RemoveChannelInput buildRemoveChannelInput(String channelName) {
        return new RemoveChannelInputBuilder().setChannelName(channelName).build();
    }
}