/*
 * Copyright © 2016 www.bupt.edu.cn and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topomanager.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Destination;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Source;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTopologyAdapter {
    private static final Logger LOG =  LoggerFactory.getLogger(BierTopologyAdapter.class);
    private final ExecutorService executor = Executors.newFixedThreadPool(7);

    public BierTopologyAdapter() {
    }

    public BierTopology getBierTopology(DataBroker dataBroker,String topologyId) {
        Topology topo = getInitTopology(dataBroker,topologyId);
        BierTopology bierTopo = null;
        if (topo != null) {
            bierTopo = toBierTopology(topo);
        }

        return bierTopo;
    }

    private boolean isTopologyExist(DataBroker dataBroker,final InstanceIdentifier<Topology> path) {
        final ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        try {
            Optional<Topology> bierTopology = tx.read(LogicalDatastoreType.OPERATIONAL, path).checkedGet();
            LOG.debug("openflow topology exist in the operational data store at {}",path);
            if (bierTopology.isPresent()) {
                return true;
            }
        } catch (ReadFailedException e) {
            LOG.warn("openflow topology read operation failed!", e);
        }
        return false;
    }

    public Topology getInitTopology(DataBroker dataBroker,String topologyId) {
        BierTopologyProcess<Topology> processor =  new BierTopologyProcess<Topology>(dataBroker,
                BierTopologyProcess.FLAG_READ,(new TopologyBuilder()).build());

        final InstanceIdentifier<Topology> path = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(topologyId)));
        if (!isTopologyExist(dataBroker,path)) {
            return null;
        }

        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
              // Auto-generated method stub
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<Topology>> readOperation(ReadWriteTransaction transaction) {

                ListenableFuture<Optional<Topology>> listenableFuture =
                        transaction.read(LogicalDatastoreType.OPERATIONAL, path);

                return listenableFuture;
            }
        });

        Future<ListenableFuture<Topology>> future = executor.submit(processor);

        try {
            ListenableFuture<Topology> result = future.get();
            Topology topology = result.get();
            if ( null == topology || null == topology.getTopologyId()) {
                LOG.error("ZTE:get topology is faild!");
                return null;
            }
            return topology;
        } catch (InterruptedException e) {
            LOG.error("ZTE:Get topology is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("ZTE:Get topology is faild cause by", e);
        }
        LOG.error("ZTE:get topology is faild!");
        return null;
    }

    public BierTopology toBierTopology(Topology topo) {
        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder();
        TopologyBuilder topoBuilder = new TopologyBuilder(topo);
        bierTopoBuilder.setTopologyId(topoBuilder.getTopologyId().getValue());
        BierTopologyKey bierTopoKey = new BierTopologyKey(topoBuilder.getTopologyId().getValue());
        bierTopoBuilder.setKey(bierTopoKey);

        List<BierNode> bierNodeList = new ArrayList<BierNode>();
        List<Node> nodeList = topoBuilder.getNode();
        if (nodeList != null) {
            int nodeSize = nodeList.size();
            for (int iloop = 0; iloop < nodeSize; ++iloop) {
                Node node = nodeList.get(iloop);
                BierNode bierNode = toBierNode(node);
                bierNodeList.add(bierNode);
            }
        }
        bierTopoBuilder.setBierNode(bierNodeList);

        List<BierLink> bierLinkList = new ArrayList<BierLink>();
        List<Link> linkList = topoBuilder.getLink();
        if (linkList != null) {
            int linkSize = linkList.size();
            for (int iloop = 0; iloop < linkSize; ++iloop) {
                Link link = linkList.get(iloop);
                BierLink bierLink = toBierLink(link);
                bierLinkList.add(bierLink);
            }
        }
        bierTopoBuilder.setBierLink(bierLinkList);
        return bierTopoBuilder.build();
    }

    public BierNode toBierNode(Node node) {
        NodeBuilder nodeBuilder = new NodeBuilder(node);
        BierNodeBuilder bierNodeBuilder = new BierNodeBuilder();
        String nodeId = nodeBuilder.getNodeId().getValue();
        String bierNodeId = toBierId(nodeId);
        bierNodeBuilder.setNodeId(bierNodeId);
        BierNodeKey bierNodeKey = new BierNodeKey(bierNodeId);
        bierNodeBuilder.setKey(bierNodeKey);

        List<BierTerminationPoint> bierTpList = new ArrayList<BierTerminationPoint>();
        List<TerminationPoint> tpList = nodeBuilder.getTerminationPoint();
        if (tpList != null) {
            int tpSize = tpList.size();
            for (int iloop = 0; iloop < tpSize; ++iloop) {
                TerminationPoint tp = tpList.get(iloop);
                BierTerminationPoint bierTp = toBierTp(tp);

                bierTpList.add(bierTp);
            }
        }
        bierNodeBuilder.setBierTerminationPoint(bierTpList);

        BierNodeParamsBuilder nodeParamsBuilder = new BierNodeParamsBuilder();
        List<Domain> domainList = new ArrayList<Domain>();
        nodeParamsBuilder.setDomain(domainList);
        bierNodeBuilder.setBierNodeParams(nodeParamsBuilder.build());
        bierNodeBuilder.setRouterId(bierNodeId);
        bierNodeBuilder.setName(bierNodeId);

        return bierNodeBuilder.build();
    }

    public String toBierId(String id) {
        String bierId = id;
        int index = id.indexOf("openflow:");
        if (index != -1) {
            bierId = id.substring(index + 9);
        }

        return bierId;
    }

    public BierTerminationPoint toBierTp(TerminationPoint tp) {
        TerminationPointBuilder tpBuilder = new TerminationPointBuilder(tp);

        BierTerminationPointBuilder bierTpBuilder = new BierTerminationPointBuilder();
        String tpId = toBierId(tpBuilder.getTpId().getValue());
        bierTpBuilder.setTpId(tpId);
        BierTerminationPointKey bierTpKey = new BierTerminationPointKey(tpId);
        bierTpBuilder.setKey(bierTpKey);

        return bierTpBuilder.build();
    }

    public BierLink toBierLink(Link link) {
        LinkBuilder linkBuilder = new LinkBuilder(link);

        BierLinkBuilder bierLinkBuilder = new BierLinkBuilder();
        String linkId = toBierId(linkBuilder.getLinkId().getValue());
        bierLinkBuilder.setLinkId(linkId);
        BierLinkKey bierLinkKey = new BierLinkKey(linkId);
        bierLinkBuilder.setKey(bierLinkKey);

        Source source = linkBuilder.getSource();
        SourceBuilder sourceBuilder = new SourceBuilder(source);
        LinkSourceBuilder bierSource = new LinkSourceBuilder();
        String sourceNodeId = sourceBuilder.getSourceNode().getValue();
        bierSource.setSourceNode(toBierId(sourceNodeId));
        bierSource.setSourceTp(toBierId(sourceBuilder.getSourceTp().getValue()));
        bierLinkBuilder.setLinkSource(bierSource.build());

        Destination dest = linkBuilder.getDestination();
        DestinationBuilder destBuilder = new DestinationBuilder(dest);
        LinkDestBuilder bierDest = new LinkDestBuilder();
        String destNodeId = destBuilder.getDestNode().getValue();
        bierDest.setDestNode(toBierId(destNodeId));
        bierDest.setDestTp(toBierId(destBuilder.getDestTp().getValue()));
        bierLinkBuilder.setLinkDest(bierDest.build());

        return bierLinkBuilder.build();
    }
}