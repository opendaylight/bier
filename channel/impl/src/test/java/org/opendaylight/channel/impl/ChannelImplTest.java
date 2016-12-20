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
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
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
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.ModifyChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.ModifyChannelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.ModifyChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.RemoveChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.RemoveChannelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.RemoveChannelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.deploy.channel.input.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.deploy.channel.input.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.get.channel.output.ChannelName;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.get.channel.output.ChannelNameBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.yang.common.RpcResult;


public class ChannelImplTest extends AbstractDataBrokerTest {
    private ChannelImpl channelImpl;
    private ChannelDBContext context;

    @Before
    public void setUp() throws Exception {
        context = new ChannelDBContext(getDataBroker());
        channelImpl = new ChannelImpl(context);
        channelImpl.start();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void addChannelBasicTest() throws Exception {
        RpcResult<AddChannelOutput> result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        QueryChannelOutput actulChannel = queryChannel("channel-1");
        QueryChannelOutput expectChannel = buildExpectChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",null,
                new ArrayList<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel
                        .EgressNode>());
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
    }

    @Test
    public void modifyChannelBasictest() throws Exception {
        RpcResult<AddChannelOutput> result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        RpcResult<ModifyChannelOutput> modifyResult = modifyChannel("channel-1",2,22,"2.2.2.2","225.1.1.1",
                (short)24,(short)30);
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        QueryChannelOutput actulChannel = queryChannel("channel-1");
        QueryChannelOutput expectChannel = buildExpectChannel("channel-1",2,22,"2.2.2.2","225.1.1.1",null,
                new ArrayList<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel
                        .EgressNode>());
        assertChannelData(expectChannel.getChannel(),actulChannel.getChannel());
    }

    @Test
    public void deployChannelBasicTest() throws Exception {
        RpcResult<AddChannelOutput> result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        RpcResult<DeployChannelOutput> deployResult = deployChannel("channel-1");
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel
                channelData = ChannelDBUtil.getInstance().readChannel("channel-1","flow:1").get();
        org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel
                expectChannel = buildChannelData("channel-1",1,11,"1.1.1.1","224.1.1.1",false);
        assertChannelDbData(expectChannel,channelData);
    }

    @Test
    public void modifyDeployChannelBasicTest() throws Exception {
        RpcResult<AddChannelOutput> result = addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        RpcResult<DeployChannelOutput> deployResult = deployChannel("channel-1");
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        RpcResult<DeployChannelOutput> modifyResult = channelImpl.deployChannel(
                buildModifyDeployChannelInput("channel-1")).get();
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel
                channelData = ChannelDBUtil.getInstance().readChannel("channel-1","flow:1").get();
        org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel
                expectChannel = buildChannelData("channel-1",1,11,"1.1.1.1","224.1.1.1",true);
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
        RpcResult<DeployChannelOutput> deployResult = deployChannel("channel-1");
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        QueryChannelOutput actulChannel = queryChannel("channel-1");
        List<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel.EgressNode>
                egressList = new ArrayList<>();
        egressList.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel
                .EgressNodeBuilder().setNodeId("node2").build());
        egressList.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.output.channel
                .EgressNodeBuilder().setNodeId("node3").build());

        QueryChannelOutput expectChannel = buildExpectChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",
                "node1",egressList);
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
        Assert.assertEquals("wildcard is invalid!",result.getResult().getConfigureResult().getErrorCause());

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

        modifyResult = modifyChannel("channel-1",1,11,"239.1.1.1","224.1.1.1",(short)24,(short)30);
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("src-ip is illegal!",modifyResult.getResult().getConfigureResult().getErrorCause());

        modifyResult = modifyChannel("channel-1",1,11,"1.1.1.1","220.1.1.1",(short)24,(short)30);
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("dest-group is not multicast ipaddress!",
                modifyResult.getResult().getConfigureResult().getErrorCause());

        deployChannel("channel-1");
        modifyResult = modifyChannel("channel-1",2,22,"2.2.2.2","225.1.1.1",(short)24,(short)30);
        Assert.assertTrue(modifyResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("The Channel has deployed,can not modify",
                modifyResult.getResult().getConfigureResult().getErrorCause());
    }

    @Test
    public void checkDeployChannelInputTest() throws Exception {
        RpcResult<DeployChannelOutput> deployResult = deployChannel("channel-1");
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("The Channel does not exists!",
                deployResult.getResult().getConfigureResult().getErrorCause());

        addChannel("channel-1",1,11,"1.1.1.1","224.1.1.1",(short)24,(short)30);
        deployResult = deployChannel(null);
        Assert.assertTrue(deployResult.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertEquals("channel-name is null!",deployResult.getResult().getConfigureResult().getErrorCause());

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

    private DeployChannelInput buildModifyDeployChannelInput(String channelName) {
        List<EgressNode> egresNodes = new ArrayList<>();
        egresNodes.add(new EgressNodeBuilder().setNodeId("node3").build());
        egresNodes.add(new EgressNodeBuilder().setNodeId("node4").build());
        egresNodes.add(new EgressNodeBuilder().setNodeId("node5").build());
        return new DeployChannelInputBuilder()
                .setChannelName(channelName)
                .setIngressNode("node2")
                .setEgressNode(egresNodes)
                .build();
    }

    private void assertChannelDbData(org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                                             .channel.Channel expectChannel,
                                     org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                                             .channel.Channel channelData) {
        Assert.assertEquals(expectChannel.getEgressNode(), channelData.getEgressNode());
        Assert.assertEquals(expectChannel.getIngressNode(),channelData.getIngressNode());
        Assert.assertEquals(expectChannel.getDomainId(),channelData.getDomainId());
        Assert.assertEquals(expectChannel.getDstGroup(),channelData.getDstGroup());
        Assert.assertEquals(expectChannel.getGroupWildcard(),channelData.getGroupWildcard());
        Assert.assertEquals(expectChannel.getSourceWildcard(),channelData.getSourceWildcard());
        Assert.assertEquals(expectChannel.getSrcIp(),channelData.getSrcIp());
        Assert.assertEquals(expectChannel.getSubDomainId(),channelData.getSubDomainId());
    }


    private org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel
            .Channel buildChannelData(String channelName, Integer domainId, Integer subDomainId, String srcIp,
                                     String groupIp, boolean modify) {
        org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel
                .ChannelBuilder channelBuilder = new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier
                .network.channel.bier.channel.ChannelBuilder();
        List<org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel
                .EgressNode> egressNodes = new ArrayList<>();
        if (modify) {
            channelBuilder.setIngressNode("node2");
            egressNodes.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                    .channel.channel.EgressNodeBuilder().setNodeId("node3").build());
            egressNodes.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                    .channel.channel.EgressNodeBuilder().setNodeId("node4").build());
            egressNodes.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                    .channel.channel.EgressNodeBuilder().setNodeId("node5").build());
        } else {

            channelBuilder.setIngressNode("node1");
            egressNodes.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                    .channel.channel.EgressNodeBuilder().setNodeId("node2").build());
            egressNodes.add(new org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier
                    .channel.channel.EgressNodeBuilder().setNodeId("node3").build());

        }
        channelBuilder.setName(channelName);
        channelBuilder.setDomainId(new DomainId(domainId));
        channelBuilder.setSubDomainId(new SubDomainId(subDomainId));
        channelBuilder.setSrcIp(new IpAddress(new Ipv4Address(srcIp)));
        channelBuilder.setDstGroup(new IpAddress(new Ipv4Address(groupIp)));
        channelBuilder.setSourceWildcard((short) 24);
        channelBuilder.setGroupWildcard((short) 30);
        channelBuilder.setEgressNode(egressNodes);
        return channelBuilder.build();
    }

    private RpcResult<DeployChannelOutput> deployChannel(String channelName)
            throws ExecutionException, InterruptedException {
        List<EgressNode> egressNodes = new ArrayList<>();
        egressNodes.add(new EgressNodeBuilder().setNodeId("node2").build());
        egressNodes.add(new EgressNodeBuilder().setNodeId("node3").build());
        DeployChannelInput input = new DeployChannelInputBuilder()
                .setChannelName(channelName)
                .setIngressNode("node1")
                .setEgressNode(egressNodes)
                .build();
        return channelImpl.deployChannel(input).get();
    }

    private RpcResult<ModifyChannelOutput> modifyChannel(String channelName, Integer domainId, Integer subDomainId,
                                                         String srcIp, String groupIp, Short srcWildcard,
                                                         Short groupWildcard)
            throws ExecutionException, InterruptedException {
        ModifyChannelInput input = new ModifyChannelInputBuilder()
                .setName(channelName)
                .setDomainId(new DomainId(domainId))
                .setSubDomainId(new SubDomainId(subDomainId))
                .setSrcIp(new IpAddress(new Ipv4Address(srcIp)))
                .setSourceWildcard(srcWildcard)
                .setDstGroup(new IpAddress(new Ipv4Address(groupIp)))
                .setGroupWildcard(groupWildcard)
                .build();
        return channelImpl.modifyChannel(input).get();
    }

    private void assertChannelData(List<Channel> expectChannel, List<Channel> actulChannel) {
        Assert.assertEquals(expectChannel.size(),actulChannel.size());
        for (Channel channel : actulChannel) {
            Assert.assertTrue(expectChannel.contains(channel));
        }
    }

    private QueryChannelOutput buildExpectChannel(String channelName, Integer domainId, Integer subDomainID,
                                                  String srcIp, String groupIp, String ingressNode,
                                                  List<org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102
                                                          .query.channel.output.channel.EgressNode> egressList) {
        List<Channel> channels = new ArrayList<>();
        Channel channel = new ChannelBuilder()
                .setName(channelName)
                .setDomainId(new DomainId(domainId))
                .setSubDomainId(new SubDomainId(subDomainID))
                .setSrcIp(new IpAddress(new Ipv4Address(srcIp)))
                .setDstGroup(new IpAddress(new Ipv4Address(groupIp)))
                .setSourceWildcard((short) 24)
                .setGroupWildcard((short) 30)
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