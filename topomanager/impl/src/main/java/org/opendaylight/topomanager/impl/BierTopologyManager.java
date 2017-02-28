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
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.bier.domain.BierSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.bier.domain.BierSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.bier.domain.BierSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTopologyManager {
    private static final Logger LOG =  LoggerFactory.getLogger(BierTopologyManager.class);
    private final DataBroker dataBroker;
    private BierTopologyAdapter topoAdapter = new BierTopologyAdapter();
    public static final String TOPOLOGY_ID = "flow:1";
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(7);
    private final LogicalDatastoreType datastoreType = LogicalDatastoreType.CONFIGURATION;

    public BierTopologyManager(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public DataBroker getDataBroker() {
        return dataBroker;
    }

    public void start() {
        final BierTopologyKey key = new BierTopologyKey(TOPOLOGY_ID);
        final InstanceIdentifier<BierTopology> path = InstanceIdentifier
                .create(BierNetworkTopology.class)
                .child(BierTopology.class, key);

        if (!isBierTopologyExist(path)) {
            final ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
            tx.put(datastoreType, path, new BierTopologyBuilder().setKey(key).build(), true);
            try {
                tx.submit().get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Initial topology export failed, continuing anyway", e);
            }

            BierTopology topo = topoAdapter.getBierTopology(dataBroker,TOPOLOGY_ID);
            setTopologyData(topo);
        }
    }

    private boolean isBierTopologyExist(final InstanceIdentifier<BierTopology> path) {
        final ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        try {
            Optional<BierTopology> bierTopology = tx.read(datastoreType, path).checkedGet();
            LOG.debug("Bier topology exist in the operational data store at {}",path);
            if (bierTopology.isPresent()) {
                return true;
            }
        } catch (ReadFailedException e) {
            LOG.warn("Bier topology read operation failed!", e);
        }
        return false;
    }

    public void setTopologyData(final BierTopology bierTopology) {
        if ( null == dataBroker || null == bierTopology ) {
            LOG.error("ZTE:Set Bier Topology input is error!");
            return;
        }

        BierTopologyProcess<BierTopology> processor =  new BierTopologyProcess<BierTopology>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierTopologyBuilder()).build());

        final InstanceIdentifier<BierTopology> path = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, bierTopology.getKey());
        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                transaction.put(datastoreType,path,bierTopology ,true);
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<BierTopology>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        Future<ListenableFuture<BierTopology>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierTopology> result = future.get();
            if ( null == result.get() ) {
                LOG.error("ZTE:Set Bier Topology failed!");
                return;
            }
            LOG.info("ZTE:Set Bier Topology succeed!");
            return;
        } catch (InterruptedException e) {
            LOG.error("ZTE:Set Bier Topology is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("ZTE:Set Bier Topology is faild cause by", e);
        }

        LOG.error("ZTE:Set Bier Topology failed!");
        return;
    }

    public BierTopology getTopologyData(String topologyId) {
        BierTopologyProcess<BierTopology> processor =  new BierTopologyProcess<BierTopology>(dataBroker,
                BierTopologyProcess.FLAG_READ,(new BierTopologyBuilder()).build());
        final InstanceIdentifier<BierTopology> path = getTopoPath(topologyId);
        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
              // Auto-generated method stub
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<BierTopology>> readOperation(ReadWriteTransaction transaction) {

                ListenableFuture<Optional<BierTopology>> listenableFuture =
                        transaction.read(datastoreType, path);

                return listenableFuture;
            }
        });

        Future<ListenableFuture<BierTopology>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierTopology> result = future.get();
            BierTopology topology = result.get();
            if ( null == topology || null == topology.getTopologyId()) {
                LOG.error("ZTE:get bier topology is faild!");
                return null;
            }
            return topology;
        } catch (InterruptedException e) {
            LOG.error("ZTE:Get bier topology is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("ZTE:Get bier topology is faild cause by", e);
        }
        LOG.error("ZTE:get bier topology is faild!");
        return null;
    }

    public boolean setDomainData(final String topologyId,final List<BierDomain> domainList) {

        if ( null == dataBroker || null == domainList || domainList.isEmpty() ) {
            LOG.error("ZTE:Set bier domain input is error!");
            return false;
        }

        BierTopologyProcess<BierDomain> processor =  new BierTopologyProcess<BierDomain>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierDomainBuilder()).build());

        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                int domainSize = domainList.size();
                for (int iloop = 0; iloop < domainSize; ++iloop) {
                    BierDomain domain = domainList.get(iloop);
                    final InstanceIdentifier<BierDomain> path = getDomainPath(topologyId,domain.getDomainId());
                    transaction.put(datastoreType,path, domain,true);
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<Node>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        Future<ListenableFuture<BierDomain>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierDomain> result = future.get();
            if ( null == result.get() ) {
                LOG.error("ZTE:Set bier domain failed!");
                return false;
            }

            LOG.info("ZTE:Set bier domain succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("ZTE:Set bier domain is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("ZTE:Set bier domain is faild cause by", e);
        }


        LOG.error("ZTE:Set bier domain failed!");
        return false;
    }

    public BierDomain getDomainData(String topologyId,DomainId domainId) {

        BierTopologyProcess<BierDomain> processor =  new BierTopologyProcess<BierDomain>(dataBroker,
                BierTopologyProcess.FLAG_READ,(new BierDomainBuilder()).build());

        final InstanceIdentifier<BierDomain> path = getDomainPath(topologyId,domainId);

        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
              // Auto-generated method stub
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<BierDomain>> readOperation(ReadWriteTransaction transaction) {

                ListenableFuture<Optional<BierDomain>> listenableFuture =
                        transaction.read(datastoreType, path);

                return listenableFuture;
            }
        });

        Future<ListenableFuture<BierDomain>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierDomain> result = future.get();
            BierDomain domain = result.get();
            if ( null == domain || null == domain.getDomainId()) {
                LOG.error("ZTE:get bier domain is faild!");
                return null;
            }
            return domain;
        } catch (InterruptedException e) {
            LOG.error("ZTE:Get bier domain is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("ZTE:Get bier domain is faild cause by", e);
        }
        LOG.error("ZTE:get bier domain is faild!");
        return null;
    }

    public boolean setSubDomainData(final String topologyId,final DomainId domainId,
            final List<BierSubDomain> subDomainList) {
        if ( null == dataBroker || null == subDomainList || subDomainList.isEmpty() ) {
            LOG.error("ZTE:Set bier sub-domai input is error!");
            return false;
        }

        BierTopologyProcess<BierSubDomain> processor =  new BierTopologyProcess<BierSubDomain>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierSubDomainBuilder()).build());

        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                int subDomainSize = subDomainList.size();
                for (int iloop = 0; iloop < subDomainSize; ++iloop) {
                    BierSubDomain subDomain = subDomainList.get(iloop);
                    final InstanceIdentifier<BierSubDomain> path = getSubDomainPath(topologyId,domainId,
                            subDomain.getSubDomainId());
                    transaction.put(datastoreType,path, subDomain,true);
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<Node>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        Future<ListenableFuture<BierSubDomain>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierSubDomain> result = future.get();
            if ( null == result.get() ) {
                LOG.error("ZTE:Set bier sub-domain failed!");
                return false;
            }

            LOG.info("ZTE:Set bier sub-domain succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("ZTE:Set bier sub-domain is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("ZTE:Set bier sub-domain is faild cause by", e);
        }

        LOG.error("ZTE:Set bier sub-domain failed!");
        return false;
    }

    public boolean delDomainData(final String topologyId,final DomainId domainId,final List<BierNode> nodeList) {
        if ( null == dataBroker || null == domainId ) {
            LOG.error("ZTE:Del bier domain input is error!");
            return false;
        }

        BierTopologyProcess<BierDomain> processor =  new BierTopologyProcess<BierDomain>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierDomainBuilder()).build());
        final InstanceIdentifier<BierDomain> path = getDomainPath(topologyId,domainId);

        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                transaction.delete(datastoreType,path);
                updateAffectedDomainNode(transaction,topologyId,domainId,nodeList);
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<Node>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        Future<ListenableFuture<BierDomain>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierDomain> result = future.get();
            if ( null == result.get() ) {
                LOG.error("ZTE:Del bier domain failed!");
                return false;
            }

            LOG.info("ZTE:Del domain bier domain succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("ZTE:Del bier domain is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("ZTE:Del bier domain is faild cause by", e);
        }


        LOG.error("ZTE:Del bier domain failed!");
        return false;
    }

    public void updateAffectedDomainNode(ReadWriteTransaction transaction,String topologyId,
            DomainId domainId,List<BierNode> nodeList) {
        int nodeSize = nodeList.size();
        for (int iloop = 0; iloop < nodeSize; ++iloop) {
            BierNode node = nodeList.get(iloop);
            int domainIndex = getDomainIndex(domainId,node);
            if (domainIndex != -1) {
                BierNodeBuilder newNodeBuilder = new BierNodeBuilder(node);
                List<Domain> newDomainList = newNodeBuilder.getBierNodeParams().getDomain();
                newDomainList.remove(domainIndex);
                InstanceIdentifier<BierNode> path = getNodePath(topologyId,node.getNodeId());
                transaction.put(datastoreType,path,node);
            }
        }
    }

    public boolean delSubDomainData(final String topologyId,final DomainId domainId,
            final SubDomainId subDomainId,final List<BierNode> nodeList) {

        if ( null == dataBroker || null == domainId ) {
            LOG.error("ZTE:Del bier sub-domain input is error!");
            return false;
        }


        BierTopologyProcess<BierSubDomain> processor =  new BierTopologyProcess<BierSubDomain>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierSubDomainBuilder()).build());
        final InstanceIdentifier<BierSubDomain> path = getSubDomainPath(topologyId,domainId,subDomainId);

        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                transaction.delete(datastoreType,path);

                updateAffectedSubDomainNode(transaction,topologyId,domainId,subDomainId,nodeList);
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<Node>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        Future<ListenableFuture<BierSubDomain>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierSubDomain> result = future.get();
            if ( null == result.get() ) {
                LOG.error("ZTE:Del sub-domain failed!");
                return false;
            }

            LOG.info("ZTE:Del sub-domain succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("ZTE:Del sub-domain is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("ZTE:Del sub-domain is faild cause by", e);
        }

        LOG.error("ZTE:Del Optical Node failed!");
        return false;
    }

    public void updateAffectedSubDomainNode(ReadWriteTransaction transaction,String topologyId,
            DomainId domainId,SubDomainId subDomainId,List<BierNode> nodeList) {
        if (nodeList == null) {
            return;
        }
        List<BierNode> subDomainNodeList = new ArrayList<BierNode>();
        int nodeSize = nodeList.size();
        for (int iloop = 0; iloop < nodeSize; ++iloop) {
            BierNode node = nodeList.get(iloop);
            BierNodeBuilder newNodeBuilder = new BierNodeBuilder(node);
            int domainIndex = getDomainIndex(domainId,node);
            if (domainIndex != -1) {
                int subDomainIndex = getSubDomainIndex(domainId,subDomainId,node);
                if (subDomainIndex != -1) {
                    List<SubDomain> newSubDomainList = newNodeBuilder.getBierNodeParams().getDomain()
                            .get(domainIndex).getBierGlobal().getSubDomain();
                    newSubDomainList.remove(subDomainIndex);
                    if (newSubDomainList.isEmpty()) {
                        List<Domain> newDomainList = newNodeBuilder.getBierNodeParams().getDomain();
                        newDomainList.remove(domainIndex);
                    }
                    InstanceIdentifier<BierNode> path = getNodePath(topologyId,node.getNodeId());
                    transaction.put(datastoreType,path,newNodeBuilder.build());
                }
            }
        }
    }

    public boolean setNodeData(String topologyId, final BierNode node) {
        if ( null == dataBroker || node == null ) {
            LOG.error("ZTE:Set bier node input is error!");
            return false;
        }

        BierTopologyProcess<BierNode> processor =  new BierTopologyProcess<BierNode>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierNodeBuilder()).build());
        final InstanceIdentifier<BierNode> path = getNodePath(topologyId,node.getNodeId());

        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                transaction.merge(datastoreType,path, node,true);
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<Node>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        Future<ListenableFuture<BierNode>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierNode> result = future.get();
            if ( null == result.get() ) {
                LOG.error("ZTE:Set bier node failed!");
                return false;
            }

            LOG.info("ZTE:Set bier node succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("ZTE:Set bier node is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("ZTE:Set bier node is faild cause by", e);
        }

        LOG.error("ZTE:Set bier domain failed!");
        return false;
    }

    public BierNode getNodeData(String topologyId,String nodeId) {
        BierTopologyProcess<BierNode> processor =  new BierTopologyProcess<BierNode>(dataBroker,
                BierTopologyProcess.FLAG_READ,(new BierNodeBuilder()).build());
        final InstanceIdentifier<BierNode> path = getNodePath(topologyId,nodeId);

        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
              // Auto-generated method stub
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<BierNode>> readOperation(ReadWriteTransaction transaction) {
                ListenableFuture<Optional<BierNode>> listenableFuture =
                        transaction.read(datastoreType, path);
                return listenableFuture;
            }
        });

        Future<ListenableFuture<BierNode>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierNode> result = future.get();
            BierNode node = result.get();
            if ( null == node || null == node.getNodeId()) {
                LOG.error("ZTE:get bier node is faild!");
                return null;
            }
            return node;
        } catch (InterruptedException e) {
            LOG.error("ZTE:Get bier node is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("ZTE:Get bier node is faild cause by", e);
        }
        LOG.error("ZTE:get bier node is faild!");
        return null;
    }

    public boolean delNodeFromDomain(String topologyId,final DomainId domainId,
            final SubDomainId subDomainId,final BierNode node) {
        BierTopologyProcess<BierNode> processor =  new BierTopologyProcess<BierNode>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierNodeBuilder()).build());
        final InstanceIdentifier<BierNode> path = getNodePath(topologyId,node.getNodeId());

        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                BierNodeBuilder newNodeBuilder = new BierNodeBuilder(node);
                int domainIndex = getDomainIndex(domainId,node);
                int subDomainIndex = getSubDomainIndex(domainId,subDomainId,node);
                List<SubDomain> newSubDomainList = newNodeBuilder.getBierNodeParams().getDomain()
                        .get(domainIndex).getBierGlobal().getSubDomain();
                newSubDomainList.remove(subDomainIndex);
                if (newSubDomainList.isEmpty()) {
                    List<Domain> newDomainList = newNodeBuilder.getBierNodeParams().getDomain();
                    newDomainList.remove(domainIndex);
                }
                transaction.put(datastoreType,path,newNodeBuilder.build());
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<BierNode>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        Future<ListenableFuture<BierNode>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierNode> result = future.get();
            if ( null == result.get() ) {
                LOG.error("ZTE:Del bier node failed!");
                return false;
            }

            LOG.info("ZTE:Del bier node succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("ZTE:Del bier node is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("ZTE:Del bier node is faild cause by", e);
        }

        LOG.error("ZTE:Del bier Node failed!");
        return false;
    }

    public boolean delIpv4FromNode(String topologyId,final DomainId domainId,final SubDomainId subDomainId,
            final int bitstringlength, final MplsLabel bierMplsLabelBase,final BierNode node) {
        final String type = "ipv4";
        return delIpFromNode(topologyId,domainId,subDomainId,
                bitstringlength, bierMplsLabelBase,node,type);
    }

    public boolean delIpv6FromNode(String topologyId,final DomainId domainId,final SubDomainId subDomainId,
            final int bitstringlength, final MplsLabel bierMplsLabelBase,final BierNode node) {
        final String type = "ipv6";
        return delIpFromNode(topologyId,domainId,subDomainId,
                bitstringlength, bierMplsLabelBase,node,type);
    }

    public boolean delIpFromNode(String topologyId,final DomainId domainId,final SubDomainId subDomainId,
            final int bitstringlength, final MplsLabel bierMplsLabelBase,final BierNode node,final String type) {
        BierTopologyProcess<BierNode> processor =  new BierTopologyProcess<BierNode>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierNodeBuilder()).build());

        final InstanceIdentifier<BierNode> path = getNodePath(topologyId,node.getNodeId());

        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                BierNodeBuilder newNodeBuilder = new BierNodeBuilder(node);
                int domainIndex = getDomainIndex(domainId,node);
                int subDomainIndex = getSubDomainIndex(domainId,subDomainId,node);
                int ipIndex = getIpIndex(domainId,subDomainId,bitstringlength,bierMplsLabelBase,node,type);
                if (type.equals("ipv4")) {
                    List<Ipv4> newIpv4List = newNodeBuilder.getBierNodeParams().getDomain()
                            .get(domainIndex).getBierGlobal().getSubDomain().get(subDomainIndex).getAf().getIpv4();
                    newIpv4List.remove(ipIndex);
                } else if (type.equals("ipv6")) {
                    List<Ipv6> newIpv6List = newNodeBuilder.getBierNodeParams().getDomain()
                            .get(domainIndex).getBierGlobal().getSubDomain().get(subDomainIndex).getAf().getIpv6();
                    newIpv6List.remove(ipIndex);
                }

                transaction.put(datastoreType,path,newNodeBuilder.build());
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<BierNode>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        Future<ListenableFuture<BierNode>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierNode> result = future.get();
            if ( null == result.get() ) {
                LOG.error("ZTE:Del bier node ipv6 failed!");
                return false;
            }

            LOG.info("ZTE:Del bier node ipv6 succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("ZTE:Del bier node ipv6 is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("ZTE:Del bier node ipv6 is faild cause by", e);
        }

        LOG.error("ZTE:Del bier Node ipv4 failed!");
        return false;
    }

    public BierLink getLinkData(String topologyId,String linkId) {
        BierTopologyProcess<BierLink> processor =  new BierTopologyProcess<BierLink>(dataBroker,
                BierTopologyProcess.FLAG_READ,(new BierLinkBuilder()).build());

        final InstanceIdentifier<BierTopology> topoPath = getTopoPath(topologyId);
        final InstanceIdentifier<BierLink> path = topoPath.child(BierLink.class, new BierLinkKey(linkId));

        processor.enqueueOperation(new BierTopologyOperation() {

            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
              // Auto-generated method stub
            }


            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<BierLink>> readOperation(ReadWriteTransaction transaction) {
                ListenableFuture<Optional<BierLink>> listenableFuture = transaction
                        .read(datastoreType, path);
                return listenableFuture;
            }
        });

        Future<ListenableFuture<BierLink>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierLink> result = future.get();
            BierLink link = result.get();
            if ( null == link || null == link.getLinkId()) {
                LOG.error("ZTE:get bier link is faild!");
                return null;
            }
            return link;
        } catch (InterruptedException e) {
            LOG.error("ZTE:Get bier link is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("ZTE:Get bier link is faild cause by", e);
        }
        LOG.error("ZTE:get bier link is faild!");
        return null;
    }

    public List<BierNode> getSubDomainNode(String topologyId,DomainId domainId,
            SubDomainId subDomainId) {
        List<BierNode> nodeList = new ArrayList<BierNode>();
        BierTopology  topo = getTopologyData(topologyId);
        if (topo == null) {
            LOG.error("querySubdomainNode rpc topo is not exist!");
            return nodeList;
        }

        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
        List<BierNode> allNodeList = bierTopoBuilder.getBierNode();
        int nodeSize = allNodeList.size();
        for (int iloop = 0; iloop < nodeSize; ++iloop) {
            BierNode node = allNodeList.get(iloop);
            boolean findFlag = isNodeBelongToSubDomain(domainId,subDomainId,node);
            if (findFlag) {
                nodeList.add(node);
            }
        }

        return nodeList;
    }

    public boolean isNodeBelongToSubDomain(DomainId domainId,SubDomainId subDomainId,BierNode node) {
        boolean findFlag = false;
        BierNodeBuilder nodeBuilder = new BierNodeBuilder(node);
        List<Domain> domainList = nodeBuilder.getBierNodeParams().getDomain();
        int domainSize = domainList.size();
        for (int jloop = 0; jloop < domainSize; ++jloop) {
            Domain domain = domainList.get(jloop);
            if (!domainId.equals(domain.getDomainId())) {
                continue;
            }

            List<SubDomain> subDomainList = domain.getBierGlobal().getSubDomain();
            int subDomainSize = subDomainList.size();
            for (int kloop = 0; kloop < subDomainSize; ++kloop) {
                SubDomain subDomain = subDomainList.get(kloop);
                if (!subDomain.getSubDomainId().equals(subDomainId)) {
                    continue;
                }

                findFlag = true;
            }

            if (findFlag) {
                break;
            }
        }

        return findFlag;
    }

    public List<BierLink> getSubDomainLink(String topologyId,DomainId domainId,SubDomainId subDomainId) {
        List<BierLink> linkList = new ArrayList<BierLink>();
        BierTopology  topo = getTopologyData(topologyId);
        if (topo == null) {
            LOG.error("querySubdomainLink rpc topo is not exist!");
            return linkList;
        }

        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
        List<BierLink> allLinkList = bierTopoBuilder.getBierLink();
        int linkSize = allLinkList.size();
        for (int iloop = 0; iloop < linkSize; ++iloop) {
            BierLink link = allLinkList.get(iloop);
            BierLinkBuilder linkBuilder = new BierLinkBuilder(link);
            String sourceNodeId = linkBuilder.getLinkSource().getSourceNode();
            String destNodeId = linkBuilder.getLinkDest().getDestNode();
            BierNode sourceNode = null;
            BierNode destNode = null;
            List<BierNode> allNodeList = bierTopoBuilder.getBierNode();
            int nodeSize = allNodeList.size();
            for (int jloop = 0; jloop < nodeSize; ++jloop) {
                BierNode node = allNodeList.get(jloop);
                String nodeId = node.getNodeId();
                if (nodeId.equals(sourceNodeId)) {
                    sourceNode = node;
                } else if (nodeId.equals(destNodeId)) {
                    destNode = node;
                }
            }

            if (sourceNode != null || destNode != null) {
                boolean findSourceFlag = isNodeBelongToSubDomain(domainId,subDomainId,sourceNode);
                boolean findDestFlag = isNodeBelongToSubDomain(domainId,subDomainId,destNode);
                if (findSourceFlag && findDestFlag) {
                    linkList.add(link);
                }
            }
        }

        return linkList;
    }

    public boolean checkDomainExist(String topologyId,List<Domain> domainList) {
        if (domainList == null || domainList.isEmpty()) {
            return false;
        }

        Domain domain = domainList.get(0);
        DomainId domainId  = domain.getDomainId();
        BierDomain bierDomain = getDomainData(topologyId,domainId);
        if (bierDomain == null) {
            return false;
        }

        DomainBuilder domainBuilder = new DomainBuilder(domain);
        List<SubDomain> subDomainList = domainBuilder.getBierGlobal().getSubDomain();
        if (subDomainList == null || subDomainList.isEmpty()) {
            return true;
        }
        boolean subDomainExistFlag = false;
        SubDomainId subDomainId  = subDomainList.get(0).getSubDomainId();
        List<BierSubDomain> bierSubDomainList = bierDomain.getBierSubDomain();
        if (bierSubDomainList == null) {
            return false;
        }
        int subDomainSize = bierSubDomainList.size();
        for (int iloop = 0; iloop < subDomainSize; ++iloop) {
            BierSubDomain subDomain = bierSubDomainList.get(iloop);
            if (subDomainId.equals(subDomain.getSubDomainId())) {
                subDomainExistFlag = true;
                break;
            }
        }
        if (!subDomainExistFlag) {
            return false;
        }

        return true;
    }

    public boolean checkDomainExist(String topologyId,DomainId domainId,
            List<BierDomain> domainList) {
        if (domainList == null || domainList.isEmpty()) {
            return false;
        }

        int domainSize = domainList.size();
        for (int iloop = 0; iloop < domainSize; ++iloop) {
            BierDomain domain = domainList.get(iloop);
            if (domain.getDomainId().equals(domainId)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkSubDomainExist(String topologyId,DomainId domainId,SubDomainId subDomainId,
            List<BierDomain> domainList) {
        if (domainList == null || domainList.isEmpty()) {
            return false;
        }

        int domainSize = domainList.size();
        for (int iloop = 0; iloop < domainSize; ++iloop) {
            BierDomain domain = domainList.get(iloop);
            if (!domain.getDomainId().equals(domainId)) {
                continue;
            }

            List<BierSubDomain> subDomainList = domain.getBierSubDomain();
            if (subDomainList == null || subDomainList.isEmpty()) {
                return false;
            }

            int subDomainSize = subDomainList.size();
            for (int jloop = 0; jloop < subDomainSize; ++jloop) {
                BierSubDomain subDomain = subDomainList.get(jloop);
                if (subDomain.getSubDomainId().equals(subDomainId)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean checkIpv4Exist(String topologyId,DomainId domainId,SubDomainId subDomainId,
            int bitstringlength, MplsLabel bierMplsLabelBase,BierNode node) {
        if (-1 == getIpIndex(domainId,subDomainId,bitstringlength,bierMplsLabelBase,node,"ipv4")) {
            return false;
        }
        return true;
    }

    public boolean checkIpv6Exist(String topologyId,DomainId domainId,SubDomainId subDomainId,
            int bitstringlength, MplsLabel bierMplsLabelBase,BierNode node) {
        if (-1 == getIpIndex(domainId,subDomainId,bitstringlength,bierMplsLabelBase,node,"ipv6")) {
            return false;
        }
        return true;
    }

    public boolean checkNodeBelongToDomain(DomainId domainId,SubDomainId subDomainId,BierNode node) {
        if (getSubDomainIndex(domainId,subDomainId,node) == -1) {
            return false;
        }

        return true;
    }

    public int getDomainIndex(DomainId domainId,BierNode node) {
        List<Domain> domainList =  node.getBierNodeParams().getDomain();
        if (domainList == null) {
            return -1;
        }
        int domainSize = domainList.size();
        for (int iloop = 0; iloop < domainSize; ++iloop) {
            Domain domain = domainList.get(iloop);
            if (domain.getDomainId().equals(domainId)) {
                return iloop;
            }
        }

        return -1;
    }

    public int getSubDomainIndex(DomainId domainId,SubDomainId subDomainId,BierNode node) {
        int domainIndex = getDomainIndex(domainId,node);
        if (domainIndex == -1) {
            return -1;
        }

        Domain domain = node.getBierNodeParams().getDomain().get(domainIndex);
        List<SubDomain> subDomainList = domain.getBierGlobal().getSubDomain();
        if (subDomainList == null) {
            return -1;
        }
        int subDomainSize = subDomainList.size();
        for (int iloop = 0; iloop < subDomainSize; ++iloop) {
            SubDomain subDomain = subDomainList.get(iloop);
            if (subDomain.getSubDomainId().equals(subDomainId)) {
                return iloop;
            }
        }

        return -1;
    }

    public int getIpIndex(DomainId domainId,SubDomainId subDomainId,int bitstringlength,
            MplsLabel bierMplsLabelBase,BierNode node,String type) {
        int domainIndex = getDomainIndex(domainId,node);
        if (domainIndex == -1) {
            return -1;
        }
        int subDomainIndex = getSubDomainIndex(domainId,subDomainId,node);
        if (subDomainIndex == -1) {
            return -1;
        }

        SubDomain subDomain = node.getBierNodeParams().getDomain().get(domainIndex).getBierGlobal()
                .getSubDomain().get(subDomainIndex);

        if (type.equals("ipv4")) {
            List<Ipv4> ipv4List = subDomain.getAf().getIpv4();
            int ipv4Size = ipv4List.size();
            for (int iloop = 0; iloop < ipv4Size; ++iloop) {
                Ipv4 ipv4 = ipv4List.get(iloop);
                if (ipv4.getBitstringlength().intValue() == bitstringlength
                        && ipv4.getBierMplsLabelBase().equals(bierMplsLabelBase)) {
                    return iloop;
                }
            }
        } else if (type.equals("ipv6")) {
            List<Ipv6> ipv6List = subDomain.getAf().getIpv6();
            int ipv6Size = ipv6List.size();
            for (int iloop = 0; iloop < ipv6Size; ++iloop) {
                Ipv6 ipv6 = ipv6List.get(iloop);
                if (ipv6.getBitstringlength().intValue() == bitstringlength
                        && ipv6.getBierMplsLabelBase().equals(bierMplsLabelBase)) {
                    return iloop;
                }
            }
        }

        return -1;
    }

    public InstanceIdentifier<BierTopology> getTopoPath(String topologyId) {
        return InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId));
    }

    public InstanceIdentifier<BierNode> getNodePath(String topologyId,String nodeId) {
        InstanceIdentifier<BierTopology> topoPath = getTopoPath(topologyId);
        InstanceIdentifier<BierNode> path = topoPath.child(BierNode.class, new BierNodeKey(nodeId));
        return path;
    }

    public InstanceIdentifier<BierDomain> getDomainPath(String topologyId,DomainId domainId) {
        InstanceIdentifier<BierTopology> topoPath = getTopoPath(topologyId);
        InstanceIdentifier<BierDomain> path = topoPath.child(BierDomain.class, new BierDomainKey(domainId));
        return path;
    }

    public InstanceIdentifier<BierSubDomain> getSubDomainPath(String topologyId,DomainId domainId,
            SubDomainId subDomainId) {
        InstanceIdentifier<BierDomain> domainPath = getDomainPath(topologyId,domainId);
        InstanceIdentifier<BierSubDomain> path = domainPath.child(BierSubDomain.class,
                new BierSubDomainKey(subDomainId));
        return path;
    }

    public  List<String> nodesOnline(String queryId) {
        final BierTopology topoOnline = topoAdapter.getBierTopology(dataBroker, queryId);
        final List<BierNode> bierNode = topoOnline.getBierNode();
        List<String> bierNodeId = new ArrayList<String>();
        for (int loopi = 0 ; loopi < bierNode.size() ; loopi++) {
            bierNodeId.add(bierNode.get(loopi).getNodeId());
        }
        return bierNodeId;
    }
}
