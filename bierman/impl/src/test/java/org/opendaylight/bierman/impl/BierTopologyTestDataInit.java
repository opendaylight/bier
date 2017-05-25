/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

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
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.nt.l3.unicast.igp.topology.rev131021.Link1;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.nt.l3.unicast.igp.topology.rev131021.Link1Builder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.nt.l3.unicast.igp.topology.rev131021.igp.link.attributes.IgpLinkAttributesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BierTopologyTestDataInit {
    public static String TopologyId = "example-linkstate-topology";

    public static void initTopo(DataBroker dataBroker) {
        Topology topology = constructTopology();

        // write to datastore
        final TopologyKey key = new TopologyKey(new TopologyId(TopologyId));
        final InstanceIdentifier<Topology> path = InstanceIdentifier.create(
                NetworkTopology.class).child(Topology.class, key);

        final ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, path, topology, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            // LOG.warn("Initial topology export failed, continuing anyway", e);
        }
    }

    public static Topology constructTopology() {
        TopologyBuilder topoBuilder = new TopologyBuilder();

        TopologyId topologyId = new TopologyId(TopologyId);
        TopologyKey topoKey = new TopologyKey(topologyId);
        topoBuilder.setKey(topoKey);
        topoBuilder.setTopologyId(topologyId);

        NodeBuilder nodeBuilder = new NodeBuilder();
        NodeId nodeId = new NodeId("bgpls://IsisLevel1:0/type=node&as=1&domain=0&router=1");
        NodeKey nodeKey = new NodeKey(nodeId);
        nodeBuilder.setNodeId(nodeId);
        nodeBuilder.setKey(nodeKey);
        List<TerminationPoint> tpList = new ArrayList<TerminationPoint>();
        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        TpId tpId = new TpId("bgpls://IsisLevel1:0/type=tp&ipv4=192.168.54.11");
        TerminationPointKey tpKey = new TerminationPointKey(tpId);
        tpBuilder.setKey(tpKey);
        tpBuilder.setTpId(tpId);
        tpList.add(tpBuilder.build());
        nodeBuilder.setTerminationPoint(tpList);
        List<Node> nodeList = new ArrayList<Node>();
        nodeList.add(nodeBuilder.build());

        NodeBuilder nodeBuilder2 = new NodeBuilder();
        NodeId nodeId2 = new NodeId("bgpls://IsisLevel1:0/type=node&as=1&domain=0&router=2");
        NodeKey nodeKey2 = new NodeKey(nodeId2);
        nodeBuilder2.setNodeId(nodeId2);
        nodeBuilder2.setKey(nodeKey2);
        List<TerminationPoint> tpList2 = new ArrayList<TerminationPoint>();
        TerminationPointBuilder tpBuilder2 = new TerminationPointBuilder();
        TpId tpId2 = new TpId("bgpls://IsisLevel1:0/type=tp&ipv4=192.168.54.13");
        TerminationPointKey tpKey2 = new TerminationPointKey(tpId2);
        tpBuilder2.setKey(tpKey2);
        tpBuilder2.setTpId(tpId2);
        tpList2.add(tpBuilder2.build());
        nodeBuilder2.setTerminationPoint(tpList2);
        nodeList.add(nodeBuilder2.build());
        topoBuilder.setNode(nodeList);

        LinkBuilder linkBuilder = new LinkBuilder();
        LinkId linkId = new LinkId("bgpls://IsisLevel1:0/type=link&local-as=1&local-domain=0&local-router=2"
                + "&remote-as=1&remote-domain=0&remote-router=1&ipv4-iface=192.168.54.13&ipv4-neigh=192.168.54.11");
        LinkKey linkKey = new LinkKey(linkId);
        linkBuilder.setKey(linkKey);
        linkBuilder.setLinkId(linkId);
        SourceBuilder sourceBuilder = new SourceBuilder();
        sourceBuilder.setSourceNode(new NodeId("bgpls://IsisLevel1:0/type=node&as=1&domain=0&router=2"));
        sourceBuilder.setSourceTp(new TpId("bgpls://IsisLevel1:0/type=tp&ipv4=192.168.54.13"));
        linkBuilder.setSource(sourceBuilder.build());
        DestinationBuilder destBuilder = new DestinationBuilder();
        destBuilder.setDestNode(new NodeId("bgpls://IsisLevel1:0/type=node&as=1&domain=0&router=1"));
        destBuilder.setDestTp(new TpId("bgpls://IsisLevel1:0/type=tp&ipv4=192.168.54.11"));
        linkBuilder.setDestination(destBuilder.build());
        final IgpLinkAttributesBuilder ilab = new IgpLinkAttributesBuilder();
        ilab.setMetric(new Long(10));
        linkBuilder.addAugmentation(Link1.class, new Link1Builder().setIgpLinkAttributes(ilab.build()).build());
        List<Link> linkList = new ArrayList<Link>();
        linkList.add(linkBuilder.build());
        topoBuilder.setLink(linkList);

        return topoBuilder.build();
    }
}