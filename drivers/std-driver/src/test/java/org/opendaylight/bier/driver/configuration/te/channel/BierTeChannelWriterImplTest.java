/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.driver.configuration.te.channel;

import static org.junit.Assert.assertFalse;
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
import org.opendaylight.bier.driver.common.util.IidBuilder;
import org.opendaylight.bier.driver.configuration.te.channel.BierTeChannelWriterImpl;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticast;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.transport.bier.te.Path;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.transport.bier.te.PathBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class BierTeChannelWriterImplTest  extends AbstractConcurrentDataBrokerTest {



    private MountPoint mountPoint;
    private DataBroker dataBroker ;
    private MountPointService mountPointService;
    // Optionals
    private Optional<MountPoint> optionalMountPointObject;
    private Optional<DataBroker> optionalDataBrokerObject;
    private BierTeChannelWriterImpl bierTeChannelWriter ;
    private NetconfDataOperator netconfDataOperator;

    private ConfigurationResult result =
            new ConfigurationResult(ConfigurationResult.Result.FAILED);

    private static final String NODE_ID = "nodeId";
    private static final String NODE_ID_E1 = "nodeId1";
    private static final String NODE_ID_E2 = "nodeId2";
    private static final IpAddress SRC_IP = new IpAddress(new Ipv4Address("10.41.42.60"));
    private static final short SRC_WILDCARD = 8;
    private static final IpAddress DEST_GROUP = new IpAddress(new Ipv4Address("224.0.0.5"));
    private static final short DEST_WILDCARD = 4;
    private static final Long PATH_ID_1 = 1L;
    private static final Long PATH_ID_2 = 2L;
    private static final Long PATH_ID_3 = 3L;

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
        bierTeChannelWriter = new BierTeChannelWriterImpl(netconfDataOperator);
    }

    private Channel buildChannelData() {
        ArrayList<EgressNode> egressNodeArrayList = new ArrayList<EgressNode>();
        egressNodeArrayList.add(new EgressNodeBuilder().setNodeId(NODE_ID_E1).build());
        egressNodeArrayList.add(new EgressNodeBuilder().setNodeId(NODE_ID_E2).build());
        return new ChannelBuilder()
                .setIngressNode(NODE_ID)
                .setSrcIp(SRC_IP)
                .setSourceWildcard(SRC_WILDCARD)
                .setDstGroup(DEST_GROUP)
                .setGroupWildcard(DEST_WILDCARD)
                .setEgressNode(egressNodeArrayList)
                .build();

    }

    private List<Long> buildPathIdAdd() {
        ArrayList<Long> pathIdlist = new ArrayList<>();
        pathIdlist.add(PATH_ID_1);
        pathIdlist.add(PATH_ID_2);
        return pathIdlist;
    }

    private List<Path> buildPathListAdd() {
        ArrayList<Path> pathIdlist = new ArrayList<>();
        pathIdlist.add(new PathBuilder().setPathId(PATH_ID_1).build());
        pathIdlist.add(new PathBuilder().setPathId(PATH_ID_2).build());
        return pathIdlist;
    }

    private List<Long> buildPathIdModify() {
        ArrayList<Long> pathIdlist = new ArrayList<>();
        pathIdlist.add(PATH_ID_1);
        pathIdlist.add(PATH_ID_3);
        return pathIdlist;
    }

    private List<Path> buildPathListModify() {
        ArrayList<Path> pathIdlist = new ArrayList<>();
        pathIdlist.add(new PathBuilder().setPathId(PATH_ID_1).build());
        pathIdlist.add(new PathBuilder().setPathId(PATH_ID_3).build());
        return pathIdlist;
    }

    private List<Long> getPathIdFromPureMulticast(PureMulticast pureMulticast) {
        ArrayList<Long> pathIdlist = new ArrayList<>();
        List<Path> pathList = pureMulticast.getMulticastTransport().getBierTe().getPath();
        for (Path path : pathList) {
            pathIdlist.add(path.getPathId().longValue());
        }

        return pathIdlist;
    }

    @Test
    public void testWriteTeChannelAdd() throws Exception {
        buildMock();
        buildInstance();
        Channel channel = buildChannelData();
        List<Long> pathIdExpected = buildPathIdAdd();

        bierTeChannelWriter.writeBierTeChannel(ConfigurationType.ADD, NODE_ID,
                channel,buildPathListAdd(),result).checkedGet();
        assertTrue(result.isSuccessful());
        PureMulticast pureMulticast = netconfDataOperator.read(dataBroker,
                IidBuilder.buildPureMulticastIId(channel));
        List<Long> pathIdActual = getPathIdFromPureMulticast(pureMulticast);
        assertTrue(pathIdActual.containsAll(pathIdExpected));
        assertTrue(pathIdExpected.containsAll(pathIdActual));
    }

    @Test
    public void testWriteTeChannelModify() throws Exception {
        buildMock();
        buildInstance();
        Channel channel = buildChannelData();
        bierTeChannelWriter.writeBierTeChannel(ConfigurationType.ADD, NODE_ID,
                channel,buildPathListAdd(),result).checkedGet();
        assertTrue(result.isSuccessful());
        bierTeChannelWriter.writeBierTeChannel(ConfigurationType.MODIFY, NODE_ID,
                channel,buildPathListModify(),result).checkedGet();
        PureMulticast pureMulticast = netconfDataOperator.read(dataBroker,
                IidBuilder.buildPureMulticastIId(channel));
        List<Long> pathIdActual = getPathIdFromPureMulticast(pureMulticast);
        List<Long> pathIdExpected = buildPathIdModify();
        assertTrue(pathIdActual.containsAll(pathIdExpected));
        assertTrue(pathIdExpected.containsAll(pathIdActual));

    }

    @Test
    public void testWriteTeChannelDelete() throws Exception {
        buildMock();
        buildInstance();
        Channel channel = buildChannelData();
        bierTeChannelWriter.writeBierTeChannel(ConfigurationType.ADD, NODE_ID,
                channel,buildPathListAdd(),result).checkedGet();
        assertTrue(result.isSuccessful());
        bierTeChannelWriter.writeBierTeChannel(ConfigurationType.DELETE, NODE_ID,
                channel,null,result).checkedGet();
        PureMulticast pureMulticast = netconfDataOperator.read(dataBroker,
                IidBuilder.buildPureMulticastIId(channel));
        assertNull(pureMulticast);
    }

    @Test
    public void testWriteTeChannelSuccess() throws Exception {
        buildMock();
        buildInstance();
        ConfigurationResult writeResult = bierTeChannelWriter.writeBierTeChannel(ConfigurationType.ADD, NODE_ID,
                buildChannelData(),buildPathIdAdd());
        assertTrue(writeResult.isSuccessful());
    }

    @Test
    public void testWriteTeChannelFailed() throws Exception {
        buildMock();
        buildInstance();
        ConfigurationResult writeResult = bierTeChannelWriter.writeBierTeChannel(ConfigurationType.ADD, NODE_ID,
                buildChannelData(),null);
        assertFalse(writeResult.isSuccessful());
    }



}