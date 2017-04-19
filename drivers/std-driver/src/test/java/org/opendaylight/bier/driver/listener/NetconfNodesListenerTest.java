/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.driver.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

import static org.mockito.Mockito.mock;

import static org.mockito.Mockito.when;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.bier.driver.NetconfDataOperator;

import org.opendaylight.bier.driver.listener.NetconfNodesListener;


import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.NotificationsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;

import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus;

import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.AvailableCapabilitiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.available.capabilities.AvailableCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.available.capabilities.AvailableCapabilityBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


@RunWith(MockitoJUnitRunner.class)
public class NetconfNodesListenerTest {
    @Mock
    DataBroker dataBroker;
    @Mock
    DataTreeModification<Node> dataTreeModification;
    @Mock
    DataObjectModification<Node> dataObjectModification;
    Collection<DataTreeModification<Node>> modifications;



    private MountPoint mountPoint;
    private MountPointService mountPointService;
    private Optional<MountPoint> optionalMountPointObject;
    private Optional<NotificationService> optionalNotificationObject;
    private NotificationService notificationService;
    private RpcConsumerRegistry rpcConsumerRegistry;
    private NotificationsService rpcService;
    private Optional<RpcConsumerRegistry> optionalRegistryObject;
    private BindingAwareBroker.ConsumerContext consumerContext;
    private BindingAwareBroker bindingAwareBroker;
    private NetconfDataOperator netconfDataOperator;


    private static final NodeId NODE_ID = new NodeId("device_1");


    private Node buildNodeAfter() {

        ArrayList<AvailableCapability> availableCapabilities = new ArrayList<>();
        AvailableCapability availableCapability = new AvailableCapabilityBuilder()
                .setCapability("(urn:ietf:params:xml:ns:yang:ietf-bier?revision=2016-07-23)ietf-bier").build();
        availableCapabilities.add(availableCapability);

        return new NodeBuilder()
                .setNodeId(NODE_ID)
                .addAugmentation(NetconfNode.class, new NetconfNodeBuilder()
                        .setConnectionStatus(NetconfNodeConnectionStatus.ConnectionStatus.Connected)
                        .setAvailableCapabilities(new AvailableCapabilitiesBuilder()
                                .setAvailableCapability(availableCapabilities)
                                .build())
                        .build())
                .build();

    }

    private Node buildNodeBefore() {

        return new NodeBuilder()
                .setNodeId(NODE_ID)
                .addAugmentation(NetconfNode.class, new NetconfNodeBuilder()
                        .setConnectionStatus(NetconfNodeConnectionStatus.ConnectionStatus.Connecting)
                        .build())
                .build();

    }


    private void buildMock() {
        modifications = Collections.singletonList(dataTreeModification);
        when(dataTreeModification.getRootNode()).thenReturn(dataObjectModification);

        mountPoint = mock(MountPoint.class);
        mountPointService = mock(MountPointService.class);
        optionalMountPointObject = mock(Optional.class);
        consumerContext = mock(BindingAwareBroker.ConsumerContext.class);
        bindingAwareBroker = mock(BindingAwareBroker.class);
        notificationService = mock(NotificationService.class);
        optionalNotificationObject = mock(Optional.class);

        rpcConsumerRegistry = mock(RpcConsumerRegistry.class);
        optionalRegistryObject = mock(Optional.class);
        NotificationsService rpcService = mock(NotificationsService.class);

        final Optional<RpcConsumerRegistry> service = mountPoint.getService(RpcConsumerRegistry.class);

        when(bindingAwareBroker.registerConsumer(any(BindingAwareConsumer.class))).thenReturn(consumerContext);
        when(consumerContext.getSALService(any())).thenReturn(mountPointService);
        when(mountPointService.getMountPoint(any(InstanceIdentifier.class))).thenReturn(optionalMountPointObject);
        when(optionalMountPointObject.isPresent()).thenReturn(true);
        when(optionalMountPointObject.get()).thenReturn(mountPoint);
        when(mountPoint.getService(NotificationService.class)).thenReturn(optionalNotificationObject);
        when(optionalNotificationObject.get()).thenReturn(notificationService);
        when(mountPoint.getService(RpcConsumerRegistry.class)).thenReturn(optionalRegistryObject);
        when(optionalRegistryObject.get()).thenReturn(rpcConsumerRegistry);
        when(rpcConsumerRegistry.getRpcService(NotificationsService.class)).thenReturn(rpcService);

        netconfDataOperator = new NetconfDataOperator();
        netconfDataOperator.onSessionInitialized(consumerContext);


    }




    @Test
    public void testOnDataTreeChangedAddNode() throws Exception {

        buildMock();
        NetconfNodesListener netconfNodesListener = new NetconfNodesListener(dataBroker, netconfDataOperator);

        when(dataObjectModification.getModificationType()).thenReturn(DataObjectModification.ModificationType.WRITE);
        when(dataObjectModification.getDataAfter()).thenReturn(buildNodeAfter());
        netconfNodesListener.onDataTreeChanged(modifications);
        assertTrue(netconfNodesListener.getMapNodeListenerReg().containsKey(NODE_ID));
    }

    @Test
    public void testOnDataTreeChangedModifyNode() throws Exception {

        buildMock();

        when(dataObjectModification.getModificationType())
                .thenReturn(DataObjectModification.ModificationType.SUBTREE_MODIFIED);
        when(dataObjectModification.getDataAfter()).thenReturn(buildNodeAfter());
        when(dataObjectModification.getDataBefore()).thenReturn(buildNodeBefore());
        NetconfNodesListener netconfNodesListener = new NetconfNodesListener(dataBroker, netconfDataOperator);
        netconfNodesListener.onDataTreeChanged(modifications);
        assertTrue(netconfNodesListener.getMapNodeListenerReg().containsKey(NODE_ID));
    }

    @Test
    public void testOnDataTreeChangedDeleteNode() throws Exception {

        buildMock();
        NetconfNodesListener netconfNodesListener = new NetconfNodesListener(dataBroker, netconfDataOperator);
        when(dataObjectModification.getModificationType())
                .thenReturn(DataObjectModification.ModificationType.DELETE);
        when(dataObjectModification.getDataBefore()).thenReturn(buildNodeBefore());
        netconfNodesListener.onDataTreeChanged(modifications);
        assertFalse(netconfNodesListener.getMapNodeListenerReg().containsKey(NODE_ID));
    }

    @Test
    public void testUnregisterListener() throws Exception {

        buildMock();
        NetconfNodesListener netconfNodesListener = new NetconfNodesListener(dataBroker, netconfDataOperator);

        when(dataObjectModification.getModificationType()).thenReturn(DataObjectModification.ModificationType.WRITE);
        when(dataObjectModification.getDataAfter()).thenReturn(buildNodeAfter());
        netconfNodesListener.onDataTreeChanged(modifications);
        assertTrue(netconfNodesListener.getMapNodeListenerReg().containsKey(NODE_ID));
        netconfNodesListener.unregisterListener();
        assertEquals(netconfNodesListener.getMapNodeListenerReg().size(), 0);
    }
}