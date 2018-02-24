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
import org.opendaylight.bier.driver.configuration.channel.ChannelTestDataBuilder;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.bier.node.EgressNodes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.bier.node.EgressNodesKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticast;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.pure.multicast.MulticastOverlay;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.overlay.BierInformation;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;



public class ChannelConfigWriterImplTest  extends AbstractConcurrentDataBrokerTest {

    private MountPoint mountPoint;
    private DataBroker dataBroker ;
    private MountPointService mountPointService;
    // Optionals

    private Optional<MountPoint> optionalMountPointObject;

    private Optional<DataBroker> optionalDataBrokerObject;
    private ChannelConfigWriterImpl channelConfigWriter ;
    private NetconfDataOperator netconfDataOperator;
    private ConfigurationResult result =
            new ConfigurationResult(ConfigurationResult.Result.FAILED);



    @Before
    public void before() throws Exception {

        dataBroker = getDataBroker();

    }


    private void buildMock() {
        mountPoint = mock(MountPoint.class);
        mountPointService = mock(MountPointService.class);
        optionalMountPointObject = mock(Optional.class);
        optionalDataBrokerObject = mock(Optional.class);

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
        netconfDataOperator = new NetconfDataOperator(mountPointService);
        channelConfigWriter = new ChannelConfigWriterImpl(netconfDataOperator,dataBroker);

    }


    private Channel buildChannelData(Integer ingressBfrId,Integer egressBfrId1,Integer egressBfrId2) {
        //interface to datastore
        ChannelTestDataBuilder.writeInterfaceInfo(ChannelTestDataBuilder.RETRY_WRITE_MAX,dataBroker);


        return ChannelTestDataBuilder.buildChannelData(new BfrId(ingressBfrId),
                new BfrId(egressBfrId1), new BfrId(egressBfrId2));
    }

    private Channel buildChannelAddData() {
        return buildChannelData(100,5,10);
    }


    private Channel buildChannelModifyData() {
        return buildChannelData(50,15,110);

    }

    private Channel buildChannelEgressData(Integer egressBfrId) {
        //interface to datastore
        ChannelTestDataBuilder.writeInterfaceInfo(ChannelTestDataBuilder.RETRY_WRITE_MAX,dataBroker);
        return ChannelTestDataBuilder.buildChannelEgressNodeData(new BfrId(egressBfrId));
    }

    private Channel buildChannelAddEgressNode() {
        return buildChannelEgressData(100);
    }

    private Channel buildChannelModEgressNode() {
        return buildChannelEgressData(101);
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
        Channel channel = buildChannelAddEgressNode();
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
        Channel channel = buildChannelAddEgressNode();
        channelConfigWriter.writeChannelEgressNode(ConfigurationType.ADD,channel,result).checkedGet();
        assertTrue(result.isSuccessful());
        Channel modifiedChannel = buildChannelModEgressNode();
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
        Channel channel = buildChannelAddEgressNode();
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