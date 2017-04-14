/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topomanager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BierChangeListenerTest extends AbstractDataBrokerTest {
    private BierTopologyManager topoManager;
    private BierNodeChangeListenerImpl bierNodeChangeListener;
    private BierLinkChangeListenerImpl bierLinkChangeLintener;
    private BierTpChangeListenerImpl bierTpChangeListener;

    @Before
    public void setUp() throws Exception {

        TopologyBuilder topoBuilder = new TopologyBuilder();
        String topoId = "flow:1";
        TopologyId topologyId = new TopologyId(topoId);
        TopologyKey topoKey = new TopologyKey(topologyId);
        topoBuilder.setKey(topoKey);
        topoBuilder.setTopologyId(topologyId);

        NodeBuilder nodeBuilder = new NodeBuilder();
        NodeId nodeId = new NodeId("1");
        NodeKey nodeKey = new NodeKey(nodeId);
        nodeBuilder.setNodeId(nodeId);
        nodeBuilder.setKey(nodeKey);
        List<TerminationPoint> tpList = new ArrayList<TerminationPoint>();
        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        TpId tpId = new TpId("1-1");
        TerminationPointKey tpKey = new TerminationPointKey(tpId);
        tpBuilder.setKey(tpKey);
        tpBuilder.setTpId(tpId);
        tpList.add(tpBuilder.build());
        nodeBuilder.setTerminationPoint(tpList);
        List<Node> nodeList = new ArrayList<Node>();
        nodeList.add(nodeBuilder.build());

        NodeBuilder nodeBuilder2 = new NodeBuilder();
        NodeId nodeId2 = new NodeId("2");
        NodeKey nodeKey2 = new NodeKey(nodeId2);
        nodeBuilder2.setNodeId(nodeId2);
        nodeBuilder2.setKey(nodeKey2);
        List<TerminationPoint> tpList2 = new ArrayList<TerminationPoint>();
        TerminationPointBuilder tpBuilder2 = new TerminationPointBuilder();
        TpId tpId2 = new TpId("2-1");
        TerminationPointKey tpKey2 = new TerminationPointKey(tpId2);
        tpBuilder2.setKey(tpKey2);
        tpBuilder2.setTpId(tpId2);
        tpList2.add(tpBuilder2.build());
        nodeBuilder2.setTerminationPoint(tpList2);
        nodeList.add(nodeBuilder2.build());
        topoBuilder.setNode(nodeList);

        LinkBuilder linkBuilder = new LinkBuilder();
        LinkId linkId = new LinkId("1");
        LinkKey linkKey = new LinkKey(linkId);
        linkBuilder.setKey(linkKey);
        linkBuilder.setLinkId(linkId);
        SourceBuilder sourceBuilder = new SourceBuilder();
        sourceBuilder.setSourceNode(new NodeId("1"));
        sourceBuilder.setSourceTp(new TpId("1-1"));
        linkBuilder.setSource(sourceBuilder.build());
        DestinationBuilder destBuilder = new DestinationBuilder();
        destBuilder.setDestNode(new NodeId("2"));
        destBuilder.setDestTp(new TpId("2-1"));
        linkBuilder.setDestination(destBuilder.build());
        List<Link> linkList = new ArrayList<Link>();
        linkList.add(linkBuilder.build());
        topoBuilder.setLink(linkList);
        Topology topology = topoBuilder.build();
        // write to openflow datastore
        final TopologyKey key = new TopologyKey(new TopologyId("flow:1"));
        final InstanceIdentifier<Topology> path = InstanceIdentifier.create(
                NetworkTopology.class).child(Topology.class, key);
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, path, topology, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            // LOG.warn("Initial topology export failed, continuing anyway", e);
        }
        topoManager = new BierTopologyManager(getDataBroker());
        topoManager.start();
        bierNodeChangeListener = new BierNodeChangeListenerImpl(getDataBroker());
        bierLinkChangeLintener = new BierLinkChangeListenerImpl(getDataBroker());
        bierTpChangeListener = new BierTpChangeListenerImpl(getDataBroker());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void addedNodeTest() throws ExecutionException, InterruptedException {
        addNodeToTopology("3");
        BierNode bierNode = topoManager.getNodeData("flow:1","3");
        Assert.assertTrue(bierNode.getNodeId().equals("3"));
    }

    @Test
    public void removedNodeTest() {
        removeNodeToTopology("1");
        BierNode bierNode = topoManager.getNodeData("flow:1","1");
        Assert.assertTrue(bierNode.getNodeId().equals("1"));
    }

    @Test
    public void addedLinkTest() {
        addLinkToTopology("3");
        BierLink bierLink = topoManager.getLinkData("flow:1", "3");
        Assert.assertTrue(bierLink.getLinkId().equals("3"));
    }

    @Test
    public void removedLinkTest() {
        removeLinkToTopology("1");
        BierLink bierLink = topoManager.getLinkData("flow:1", "1");
        Assert.assertTrue(bierLink == null);
    }

    @Test
    public void addedTpTest() {
        addTpToTopology("1-2");
        List<BierTerminationPoint> bierTp = topoManager.getNodeData("flow:1", "1").getBierTerminationPoint();
        Assert.assertTrue(bierTp.get(1).getTpId().equals("1-2"));
    }

    @Test
    public void removedTpTest() {
        removeTpToTopology("1-1");
        List<BierTerminationPoint> bierTp = topoManager.getNodeData("flow:1", "1").getBierTerminationPoint();
        Assert.assertTrue(bierTp.size() == 0);
    }

    public void addNodeToTopology(String strNodeId) {
        NodeBuilder nodeBuilder = new NodeBuilder();
        NodeId nodeId = new NodeId(strNodeId);
        NodeKey nodeKey = new NodeKey(nodeId);
        nodeBuilder.setNodeId(nodeId);
        nodeBuilder.setKey(nodeKey);
        Node node = nodeBuilder.build();
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<Node> path = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1")))
                .child(Node.class,new NodeKey(new NodeId(strNodeId)));
        tx.put(LogicalDatastoreType.OPERATIONAL,path, node, true);
        try {
            tx.submit().get();
            Thread.sleep(1500);
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    public void removeNodeToTopology(String strNodeId) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<Node> path = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1")))
                .child(Node.class,new NodeKey(new NodeId(strNodeId)));
        tx.delete(LogicalDatastoreType.OPERATIONAL,path);
        try {
            tx.submit().get();
            Thread.sleep(1500);
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    public void addLinkToTopology(String strLinkId) {
        LinkBuilder linkBuilder = new LinkBuilder();
        LinkId linkId = new LinkId(strLinkId);
        LinkKey linkKey = new LinkKey(linkId);
        linkBuilder.setKey(linkKey);
        linkBuilder.setLinkId(linkId);
        SourceBuilder sourceBuilder = new SourceBuilder();
        final String strSourceNodeId = "1";
        sourceBuilder.setSourceNode(new NodeId(strSourceNodeId));
        final String strSourceTpId = "1-1";
        sourceBuilder.setSourceTp(new TpId(strSourceTpId));
        linkBuilder.setSource(sourceBuilder.build());
        DestinationBuilder destBuilder = new DestinationBuilder();
        final String strDestNodeId = "2";
        destBuilder.setDestNode(new NodeId(strDestNodeId));
        final String strDestTpId = "2-1";
        destBuilder.setDestTp(new TpId(strDestTpId));
        linkBuilder.setDestination(destBuilder.build());
        Link link = linkBuilder.build();

        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<Link> path = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1")))
                .child(Link.class,new LinkKey(new LinkId(strLinkId)));
        tx.put(LogicalDatastoreType.OPERATIONAL,path, link, true);
        try {
            tx.submit().get();
            Thread.sleep(1500);
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    public void removeLinkToTopology(String strLinkId) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<Link> path = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1")))
                .child(Link.class,new LinkKey(new LinkId(strLinkId)));
        tx.delete(LogicalDatastoreType.OPERATIONAL, path);
        try {
            tx.submit().get();
            Thread.sleep(1500);
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    public void addTpToTopology(String strTpId) {
        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        TpId tpId = new TpId(strTpId);
        TerminationPointKey terminationPointKey = new TerminationPointKey(tpId);
        tpBuilder.setKey(terminationPointKey);
        tpBuilder.setTpId(tpId);
        TerminationPoint tp = tpBuilder.build();
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<TerminationPoint> path = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1")))
                .child(Node.class,new NodeKey(new NodeId("1")))
                .child(TerminationPoint.class,new TerminationPointKey(new TpId(strTpId)));
        tx.put(LogicalDatastoreType.OPERATIONAL,path, tp, true);
        try {
            tx.submit().get();
            Thread.sleep(1500);
        } catch (InterruptedException | ExecutionException e) {
            return;
        }

    }

    public void removeTpToTopology(String strTpId) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<TerminationPoint> path = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1")))
                .child(Node.class,new NodeKey(new NodeId("1")))
                .child(TerminationPoint.class,new TerminationPointKey(new TpId(strTpId)));
        tx.delete(LogicalDatastoreType.OPERATIONAL, path);
        try {
            tx.submit().get();
            Thread.sleep(1500);
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }
}
