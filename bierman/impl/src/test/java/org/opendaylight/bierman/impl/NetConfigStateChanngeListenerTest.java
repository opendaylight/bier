/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.bier.adapter.api.DeviceInterfaceReader;
import org.opendaylight.bierman.impl.topo.NetConfigStateChangeListenerImpl;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.AvailableCapabilitiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.available.capabilities.AvailableCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.available.capabilities.AvailableCapabilityBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;

@RunWith(MockitoJUnitRunner.class)
public class NetConfigStateChanngeListenerTest extends AbstractDataBrokerTest {

    @Mock
    DataTreeModification<Node> dataTreeModification;
    @Mock
    DataObjectModification<Node> dataObjectModification;
    @Mock
    Collection<DataTreeModification<Node>> modifications;
    @Mock
    DeviceInterfaceReader deviceInterfaceReader;

    private NetConfigStateChangeListenerImpl netconfStateChangeListener;
    private BierDataManager topoManager;

    private static final NodeId NODE_ID = new NodeId("1");

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
        BierTerminationPointBuilder devTp = new BierTerminationPointBuilder();
        devTp.setIfName("fei-0/1/0/4");
        devTp.setTpIndex(1L);
        devTp.setTpIpPrefix(new IpAddress(Ipv4Address.getDefaultInstance("192.168.54.11")));
        List<BierTerminationPoint> devTpList = new ArrayList<BierTerminationPoint>();
        devTpList.add(devTp.build());
        buildTopology();

        modifications = Collections.singletonList(dataTreeModification);
        when(dataTreeModification.getRootNode()).thenReturn(dataObjectModification);
        when(deviceInterfaceReader.readDeviceInterface("1")).thenReturn(devTpList);
    }


    @Test
    public void testOnDataTreeChangedModifyNode() throws Exception {
        topoManager = new BierDataManager(getDataBroker());
        netconfStateChangeListener = new NetConfigStateChangeListenerImpl(getDataBroker(),
                topoManager, deviceInterfaceReader);
        buildMock();
        when(dataObjectModification.getModificationType())
                .thenReturn(DataObjectModification.ModificationType.SUBTREE_MODIFIED);
        when(dataObjectModification.getDataAfter()).thenReturn(buildNodeAfter());
        when(dataObjectModification.getDataBefore()).thenReturn(buildNodeBefore());
        netconfStateChangeListener.onDataTreeChanged(modifications);
        BierNode bierNode = topoManager.getNodeData("example-linkstate-topology","1");
        BierLink bierLink = topoManager.getLinkData("example-linkstate-topology","13");
        assertTrue(bierNode.getBierTerminationPoint().get(0).getTpId().equals("fei-0/1/0/4"));
        assertTrue(bierLink.getLinkSource().getSourceTp().equals("fei-0/1/0/4"));
    }

    @Test
    public void testOnDataTreeChangedAddNode() throws Exception {
        topoManager = new BierDataManager(getDataBroker());
        netconfStateChangeListener = new NetConfigStateChangeListenerImpl(getDataBroker(),
                topoManager, deviceInterfaceReader);
        buildMock();
        when(dataObjectModification.getModificationType())
                .thenReturn(DataObjectModification.ModificationType.WRITE);
        when(dataObjectModification.getDataAfter()).thenReturn(buildNodeAfter());
        netconfStateChangeListener.onDataTreeChanged(modifications);
        BierNode bierNode = topoManager.getNodeData("example-linkstate-topology","1");
        BierLink bierLink = topoManager.getLinkData("example-linkstate-topology","13");
        assertTrue(bierNode.getBierTerminationPoint().get(0).getTpId().equals("fei-0/1/0/4"));
        assertTrue(bierLink.getLinkSource().getSourceTp().equals("fei-0/1/0/4"));
    }

    private void buildTopology() {
        BierNode bierNode1 = buildBierNode("1", "192.168.54.11");
        BierNode bierNode2 = buildBierNode("3", "192.168.54.13");
        List<BierNode> bierNodeList = new ArrayList<BierNode>();
        bierNodeList.add(bierNode1);
        bierNodeList.add(bierNode2);
        List<BierLink> bierLinkList = new ArrayList<BierLink>();
        BierLink bierLink1 = buildBierLink("13", "1", "192.168.54.11", "3", "192.168.54.13");
        bierLinkList.add(bierLink1);
        BierTopologyBuilder bierTopo = new BierTopologyBuilder();
        bierTopo.setBierNode(bierNodeList);
        bierTopo.setBierLink(bierLinkList);
        bierTopo.setTopologyId("example-linkstate-topology");
        bierTopo.setKey(new BierTopologyKey("example-linkstate-topology"));
        topoManager.setTopologyData(bierTopo.build());
    }

    private BierNode buildBierNode(String nodeId, String tpId) {
        List<BierTerminationPoint> bierTerminationPointList = new ArrayList<BierTerminationPoint>();
        BierTerminationPointBuilder bierTerminationPointBuilder = new BierTerminationPointBuilder();
        bierTerminationPointBuilder.setTpId(tpId);
        bierTerminationPointBuilder.setKey(new BierTerminationPointKey(tpId));
        bierTerminationPointList.add(bierTerminationPointBuilder.build());
        BierNodeBuilder bierNode = new BierNodeBuilder();
        bierNode.setNodeId(nodeId);
        bierNode.setKey(new BierNodeKey(nodeId));
        bierNode.setBierTerminationPoint(bierTerminationPointList);
        return bierNode.build();
    }

    private BierLink buildBierLink(String linkId,String sourceNode, String sourceTp, String destNode, String destTp) {
        LinkSourceBuilder linkSourceBuilder = new LinkSourceBuilder();
        linkSourceBuilder.setSourceNode(sourceNode);
        linkSourceBuilder.setSourceTp(sourceTp);
        LinkDestBuilder linkDestBuilder = new LinkDestBuilder();
        linkDestBuilder.setDestTp(destNode);
        linkDestBuilder.setDestNode(destTp);
        BierLinkBuilder bierLink = new BierLinkBuilder();
        bierLink.setLinkSource(linkSourceBuilder.build());
        bierLink.setLinkDest(linkDestBuilder.build());
        bierLink.setLinkId(linkId);
        bierLink.setKey(new BierLinkKey(linkId));
        return bierLink.build();
    }
}
