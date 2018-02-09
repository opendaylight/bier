/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.bierman.impl.topo.BierLinkChangeListenerImpl;
import org.opendaylight.bierman.impl.topo.BierNodeChangeListenerImpl;
import org.opendaylight.bierman.impl.topo.BierTpChangeListenerImpl;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
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
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.nt.l3.unicast.igp.topology.rev131021.Link1;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.nt.l3.unicast.igp.topology.rev131021.Link1Builder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.nt.l3.unicast.igp.topology.rev131021.igp.link.attributes.IgpLinkAttributesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BierChangeListenerTest extends AbstractConcurrentDataBrokerTest {
    private BierDataManager topoManager;
    private BierNodeChangeListenerImpl bierNodeChangeListener;
    private BierLinkChangeListenerImpl bierLinkChangeLintener;
    private BierTpChangeListenerImpl bierTpChangeListener;
    private final Object timerLock = new Object();

    @Before
    public void setUp() throws Exception {
        BierTopologyTestDataInit.initTopo(getDataBroker());

        topoManager = new BierDataManager(getDataBroker());
        topoManager.start();
        bierNodeChangeListener = new BierNodeChangeListenerImpl(getDataBroker());
        bierLinkChangeLintener = new BierLinkChangeListenerImpl(getDataBroker());
        bierTpChangeListener = new BierTpChangeListenerImpl(getDataBroker());
    }

    @After
    public void tearDown() throws Exception {

    }

    private void lockTimerStart() throws InterruptedException {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (timerLock) {
                    timerLock.notify();
                }
            }
        }, 1500);

        synchronized (timerLock) {
            timerLock.wait();
        }
    }

    @Test
    public void addedNodeTest() throws ExecutionException, InterruptedException {
        String nodeId = "bgpls://IsisLevel1:0/type=node&as=1&domain=0&router=3";
        addNodeToTopology(nodeId);
        lockTimerStart();
        BierNode bierNode = topoManager.getNodeData(BierTopologyTestDataInit.TopologyId,"3");
        Assert.assertTrue(bierNode.getNodeId().equals("3"));
    }

    @Test
    public void removedNodeTest() throws InterruptedException {
        removeNodeToTopology("bgpls://IsisLevel1:0/type=node&as=1&domain=0&router=1");
        lockTimerStart();
        BierNode bierNode = topoManager.getNodeData(BierTopologyTestDataInit.TopologyId,"1");
        Assert.assertTrue(bierNode.getNodeId().equals("1"));

    }

    @Test
    public void addedLinkTest() throws InterruptedException {
        addLinkToTopology("bgpls://IsisLevel1:0/type=link&local-as=1&local-domain=0&local-router=1"
                + "&remote-as=1&remote-domain=0&remote-router=2&ipv4-iface=192.168.54.11&ipv4-neigh=192.168.54.13");
        lockTimerStart();
        BierLink bierLink = topoManager.getLinkData(BierTopologyTestDataInit.TopologyId,
                "1,192.168.54.11-2,192.168.54.13");
        Assert.assertTrue(bierLink.getLinkId().equals("1,192.168.54.11-2,192.168.54.13"));
    }

    @Test
    public void removedLinkTest() throws InterruptedException {
        removeLinkToTopology("bgpls://IsisLevel1:0/type=link&local-as=1&local-domain=0&local-router=2"
                + "&remote-as=1&remote-domain=0&remote-router=1&ipv4-iface=192.168.54.13&ipv4-neigh=192.168.54.11");
        lockTimerStart();
        BierLink bierLink = topoManager.getLinkData(BierTopologyTestDataInit.TopologyId,
                "2,192.168.54.13-1,192.168.54.11");
        Assert.assertTrue(bierLink == null);
    }

    @Test
    public void addedTpTest() throws InterruptedException {
        addTpToTopology("bgpls://IsisLevel1:0/type=node&as=1&domain=0&router=1",
                "bgpls://IsisLevel1:0/type=tp&ipv4=192.168.54.12");
        lockTimerStart();
        List<BierTerminationPoint> bierTp = topoManager.getNodeData(BierTopologyTestDataInit.TopologyId,
                "1").getBierTerminationPoint();
        Assert.assertTrue(bierTp.get(1).getTpId().equals("192.168.54.12"));
    }

    @Test
    public void removedTpTest() throws InterruptedException {
        removeTpToTopology("bgpls://IsisLevel1:0/type=node&as=1&domain=0&router=1",
                "bgpls://IsisLevel1:0/type=tp&ipv4=192.168.54.11");
        lockTimerStart();
        List<BierTerminationPoint> bierTp = topoManager.getNodeData(BierTopologyTestDataInit.TopologyId, "1")
                .getBierTerminationPoint();
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
                .child(Topology.class, new TopologyKey(new TopologyId(BierTopologyTestDataInit.TopologyId)))
                .child(Node.class,new NodeKey(new NodeId(strNodeId)));
        tx.put(LogicalDatastoreType.OPERATIONAL,path, node, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    public void removeNodeToTopology(String strNodeId) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<Node> path = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(BierTopologyTestDataInit.TopologyId)))
                .child(Node.class,new NodeKey(new NodeId(strNodeId)));
        tx.delete(LogicalDatastoreType.OPERATIONAL,path);
        try {
            tx.submit().get();
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
        final String strSourceTpId = "192.168.54.11";
        sourceBuilder.setSourceTp(new TpId(strSourceTpId));
        linkBuilder.setSource(sourceBuilder.build());
        DestinationBuilder destBuilder = new DestinationBuilder();
        final String strDestNodeId = "2";
        destBuilder.setDestNode(new NodeId(strDestNodeId));
        final String strDestTpId = "192.168.54.13";
        destBuilder.setDestTp(new TpId(strDestTpId));
        linkBuilder.setDestination(destBuilder.build());
        final IgpLinkAttributesBuilder ilab = new IgpLinkAttributesBuilder();
        ilab.setMetric(new Long(10));
        linkBuilder.addAugmentation(Link1.class, new Link1Builder().setIgpLinkAttributes(ilab.build()).build());
        Link link = linkBuilder.build();

        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<Link> path = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(BierTopologyTestDataInit.TopologyId)))
                .child(Link.class,new LinkKey(new LinkId(strLinkId)));
        tx.put(LogicalDatastoreType.OPERATIONAL,path, link, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    public void removeLinkToTopology(String strLinkId) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<Link> path = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(BierTopologyTestDataInit.TopologyId)))
                .child(Link.class,new LinkKey(new LinkId(strLinkId)));
        tx.delete(LogicalDatastoreType.OPERATIONAL, path);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    public void addTpToTopology(String nodeId,String strTpId) {
        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        TpId tpId = new TpId(strTpId);
        TerminationPointKey terminationPointKey = new TerminationPointKey(tpId);
        tpBuilder.setKey(terminationPointKey);
        tpBuilder.setTpId(tpId);
        TerminationPoint tp = tpBuilder.build();
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<TerminationPoint> path = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(BierTopologyTestDataInit.TopologyId)))
                .child(Node.class,new NodeKey(new NodeId(nodeId)))
                .child(TerminationPoint.class,new TerminationPointKey(new TpId(strTpId)));
        tx.put(LogicalDatastoreType.OPERATIONAL,path, tp, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }

    }

    public void removeTpToTopology(String nodeId,String strTpId) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<TerminationPoint> path = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(BierTopologyTestDataInit.TopologyId)))
                .child(Node.class,new NodeKey(new NodeId(nodeId)))
                .child(TerminationPoint.class,new TerminationPointKey(new TpId(strTpId)));
        tx.delete(LogicalDatastoreType.OPERATIONAL, path);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

}
