/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.driver.configuration.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;


import org.junit.Before;
import org.junit.Test;

import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.bier.driver.configuration.channel.ChannelConfigWriterImpl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;


import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;


import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;

import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.bier.node.EgressNodes;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.bier.node.EgressNodesKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticast;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.pure.multicast.MulticastOverlay;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.overlay.BierInformation;




import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;






public class ChannelConfigWriterImplTest  extends AbstractConcurrentDataBrokerTest {

    private MountPoint mountPoint;
    private DataBroker dataBroker ;
    private BindingAwareBroker bindingAwareBroker;
    private BindingAwareBroker.ConsumerContext consumerContext;
    private MountPointService mountPointService;
    // Optionals

    private Optional<MountPoint> optionalMountPointObject;

    private Optional<DataBroker> optionalDataBrokerObject;
    private ChannelConfigWriterImpl channelConfigWriter ;
    private NetconfDataOperator netconfDataOperator;
    private ConfigurationResult result =
            new ConfigurationResult(ConfigurationResult.Result.FAILED);


    //data
    private static final String NODE_ID = "nodeId";
    private static final IpAddress SRC_IP = new IpAddress(new Ipv4Address("10.41.42.60"));
    private static final short SRC_WILDCARD = 8;
    private static final IpAddress DEST_GROUP = new IpAddress(new Ipv4Address("224.0.0.5"));
    private static final short DEST_WILDCARD = 4;

    @Before
    public void before() throws Exception {

        dataBroker = getDataBroker();

    }


    private void buildMock() {
        mountPoint = mock(MountPoint.class);
        bindingAwareBroker = mock(BindingAwareBroker.class);
        consumerContext = mock(BindingAwareBroker.ConsumerContext.class);
        mountPointService = mock(MountPointService.class);
        optionalMountPointObject = mock(Optional.class);
        optionalDataBrokerObject = mock(Optional.class);

        when(bindingAwareBroker.registerConsumer(any(BindingAwareConsumer.class))).thenReturn(consumerContext);
        when(consumerContext.getSALService(any())).thenReturn(mountPointService);
        when(mountPointService.getMountPoint(any(InstanceIdentifier.class))).thenReturn(optionalMountPointObject);
        when(mountPoint.getService(eq(DataBroker.class))).thenReturn(optionalDataBrokerObject);
        // Mock getting mountpoint
        when(optionalMountPointObject.isPresent()).thenReturn(true);
        // OptionalGetWithoutIsPresent
        when(optionalMountPointObject.get()).thenReturn(mountPoint);
        when(optionalDataBrokerObject.isPresent()).thenReturn(true);
        // OptionalGetWithoutIsPresent
        when(optionalDataBrokerObject.get()).thenReturn(dataBroker);

    }

    private void buildInstance() {
        netconfDataOperator = new NetconfDataOperator(bindingAwareBroker);
        channelConfigWriter = new ChannelConfigWriterImpl(netconfDataOperator);
        netconfDataOperator.onSessionInitialized(consumerContext);
    }


    private Channel buildChannelAddData() {
        ArrayList<EgressNode> egressNodeArrayList = new ArrayList<EgressNode>();
        egressNodeArrayList.add(new EgressNodeBuilder().setEgressBfrId(new BfrId(5)).build());
        egressNodeArrayList.add(new EgressNodeBuilder().setEgressBfrId(new BfrId(10)).build());
        return new ChannelBuilder()
                .setIngressNode(NODE_ID)
                .setSrcIp(SRC_IP)
                .setSourceWildcard(SRC_WILDCARD)
                .setDstGroup(DEST_GROUP)
                .setGroupWildcard(DEST_WILDCARD)
                .setIngressBfrId(new BfrId(100))
                .setEgressNode(egressNodeArrayList)
                .build();

    }

    private Channel buildChannelModifyData() {
        ArrayList<EgressNode> egressNodeArrayList = new ArrayList<EgressNode>();
        egressNodeArrayList.add(new EgressNodeBuilder().setEgressBfrId(new BfrId(15)).build());
        egressNodeArrayList.add(new EgressNodeBuilder().setEgressBfrId(new BfrId(110)).build());
        return new ChannelBuilder().setIngressNode(NODE_ID)
                .setSrcIp(SRC_IP)
                .setSourceWildcard(SRC_WILDCARD)
                .setDstGroup(DEST_GROUP)
                .setGroupWildcard(DEST_WILDCARD)
                .setIngressBfrId(new BfrId(50))
                .setEgressNode(egressNodeArrayList)
                .build();

    }


    private boolean compareEgressNodesEqual(List<EgressNode> egressNodeList,List<EgressNodes> egressNodesList) {
        if (egressNodesList.isEmpty()) {
            return false;
        }
        if (egressNodesList.size() != egressNodeList.size()) {
            return false;
        }
        for (EgressNodes egressNodes:egressNodesList) {
            boolean hasSameBrfId = false;
            for (EgressNode egressNode:egressNodeList) {
                if (egressNode.getEgressBfrId().getValue().equals(egressNodes.getEgressNode().getValue())) {
                    hasSameBrfId = true;
                }
            }
            if (!hasSameBrfId) {
                return hasSameBrfId;
            }
        }
        return true;

    }

    @Test
    public void testWriteChannelAdd() throws Exception {
        buildMock();
        buildInstance();
        Channel channel = buildChannelAddData();
        channelConfigWriter.writeChannel(ConfigurationType.ADD,channel,result).checkedGet();
        assertTrue(result.isSuccessful());
        // "checkedGet" must be invoked to waits for the result of configuration,
        // or the result of "read" below may be null.
        PureMulticast pureMulticast = netconfDataOperator.read(dataBroker,
                channelConfigWriter.buildPureMulticastIId(channel));
        assertEquals(pureMulticast.getMulticastOverlay().getBierInformation().getIngressNode().getValue(),
                channel.getIngressBfrId().getValue());
        assertTrue(compareEgressNodesEqual(channel.getEgressNode(),
                pureMulticast.getMulticastOverlay().getBierInformation().getEgressNodes()));
    }

    @Test
    public void testWriteChannelModify() throws Exception {
        buildMock();
        buildInstance();
        Channel channel = buildChannelAddData();
        channelConfigWriter.writeChannel(ConfigurationType.ADD,channel,result).checkedGet();
        assertTrue(result.isSuccessful());
        Channel modifiedChannel = buildChannelModifyData();
        channelConfigWriter.writeChannel(ConfigurationType.MODIFY,modifiedChannel,result).checkedGet();
        assertTrue(result.isSuccessful());

        PureMulticast pureMulticast = netconfDataOperator.read(dataBroker,
                channelConfigWriter.buildPureMulticastIId(channel));
        assertEquals(modifiedChannel.getIngressBfrId().getValue(),
                pureMulticast.getMulticastOverlay().getBierInformation().getIngressNode().getValue());
        List<EgressNode> egressNodeListExpected = new ArrayList<EgressNode>();
        egressNodeListExpected.addAll(channel.getEgressNode());
        egressNodeListExpected.addAll(modifiedChannel.getEgressNode());
        assertTrue(compareEgressNodesEqual(egressNodeListExpected,
                pureMulticast.getMulticastOverlay().getBierInformation().getEgressNodes()));
    }

    @Test
    public void testWriteChannelDelete() throws Exception {
        buildMock();
        buildInstance();
        Channel channel = buildChannelAddData();
        channelConfigWriter.writeChannel(ConfigurationType.ADD,channel,result).checkedGet();
        assertTrue(result.isSuccessful());
        channelConfigWriter.writeChannel(ConfigurationType.DELETE,channel,result).checkedGet();
        assertTrue(result.isSuccessful());
        PureMulticast pureMulticast = netconfDataOperator.read(dataBroker,
                channelConfigWriter.buildPureMulticastIId(channel));
        assertNull(pureMulticast);

    }


    @Test
    public void testWriteChannelEgressNodeAdd() throws Exception {
        buildMock();
        buildInstance();
        Channel channel = buildChannelAddData();
        channelConfigWriter.writeChannelEgressNode(ConfigurationType.ADD,channel,result).checkedGet();
        assertTrue(result.isSuccessful());

        for (EgressNode egressNode:channel.getEgressNode()) {

            EgressNodes egressNodes = netconfDataOperator.read(dataBroker,
                    channelConfigWriter.buildPureMulticastIId(channel)
                            .child(MulticastOverlay.class)
                            .child(BierInformation.class)
                            .child(EgressNodes.class,
                                    new EgressNodesKey(new org.opendaylight.yang.gen.v1.urn
                                            .ietf.params.xml.ns.yang.ietf.multicast.information
                                            .rev161028.BfrId(egressNode.getEgressBfrId().getValue()))));
            assertNotNull(egressNodes);


        }
    }


    @Test
    public void testWriteChannelEgressNodeModify() throws Exception {
        buildMock();
        buildInstance();
        Channel channel = buildChannelAddData();
        channelConfigWriter.writeChannelEgressNode(ConfigurationType.ADD,channel,result).checkedGet();
        assertTrue(result.isSuccessful());
        Channel modifiedChannel = buildChannelModifyData();
        channelConfigWriter.writeChannelEgressNode(ConfigurationType.MODIFY,modifiedChannel,result).checkedGet();
        assertTrue(result.isSuccessful());

        PureMulticast pureMulticast = netconfDataOperator.read(dataBroker,
                channelConfigWriter.buildPureMulticastIId(channel));

        List<EgressNode> egressNodeListExpected = new ArrayList<EgressNode>();
        egressNodeListExpected.addAll(channel.getEgressNode());
        egressNodeListExpected.addAll(modifiedChannel.getEgressNode());
        for (EgressNode egressNode:egressNodeListExpected) {

            EgressNodes egressNodes = netconfDataOperator.read(dataBroker,
                    channelConfigWriter.buildPureMulticastIId(channel)
                            .child(MulticastOverlay.class)
                            .child(BierInformation.class)
                            .child(EgressNodes.class,
                                    new EgressNodesKey(new org.opendaylight.yang.gen.v1.urn
                                            .ietf.params.xml.ns.yang.ietf.multicast.information
                                            .rev161028.BfrId(egressNode.getEgressBfrId().getValue()))));
            assertNotNull(egressNodes);


        }
    }

    @Test
    public void testWriteChannelEgressNodeDelete() throws Exception {
        buildMock();
        buildInstance();
        Channel channel = buildChannelAddData();
        channelConfigWriter.writeChannel(ConfigurationType.ADD, channel,result).checkedGet();
        assertTrue(result.isSuccessful());

        channelConfigWriter.writeChannelEgressNode(ConfigurationType.DELETE, channel,result).checkedGet();
        assertTrue(result.isSuccessful());

        for (EgressNode egressNode:channel.getEgressNode()) {

            EgressNodes egressNodes = netconfDataOperator.read(dataBroker,
                    channelConfigWriter.buildPureMulticastIId(channel)
                            .child(MulticastOverlay.class)
                            .child(BierInformation.class)
                            .child(EgressNodes.class,
                                    new EgressNodesKey(new org.opendaylight.yang.gen.v1.urn
                                            .ietf.params.xml.ns.yang.ietf.multicast.information
                                            .rev161028.BfrId(egressNode.getEgressBfrId().getValue()))));
            assertNull(egressNodes);


        }
    }

    @Test
    public void testWriteChannel() throws Exception {
        buildMock();
        buildInstance();
        Channel channel = buildChannelAddData();
        ConfigurationResult writeResult = channelConfigWriter.writeChannel(ConfigurationType.ADD,channel);
        assertTrue(writeResult.isSuccessful());
    }

    @Test
    public void testWriteChannelEgressNode() throws Exception {
        buildMock();
        buildInstance();
        Channel channel = buildChannelAddData();
        ConfigurationResult writeResult = channelConfigWriter.writeChannelEgressNode(ConfigurationType.ADD,channel);
        assertTrue(writeResult.isSuccessful());
    }
}