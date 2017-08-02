/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl;

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
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierTeLabelRangeSize;
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
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeLableRange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeLableRangeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierEncapsulationMpls;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BierDataManager {
    private static final Logger LOG =  LoggerFactory.getLogger(BierDataManager.class);
    private final DataBroker dataBroker;
    private BierDataAdapter topoAdapter = new BierDataAdapter();
    public static final String TOPOLOGY_ID = "example-linkstate-topology";
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(7);
    private final LogicalDatastoreType datastoreType = LogicalDatastoreType.CONFIGURATION;

    public BierDataManager(DataBroker dataBroker) {
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
        if (null == dataBroker || null == bierTopology) {
            LOG.error("Set Bier Topology input is error!");
            return;
        }

        BierDataProcess<BierTopology> processor =  new BierDataProcess<BierTopology>(dataBroker,
                BierDataProcess.FLAG_WRITE,(new BierTopologyBuilder()).build());

        final InstanceIdentifier<BierTopology> path = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, bierTopology.getKey());
        processor.enqueueOperation(new BierDataOperation() {
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
            if (null == result.get()) {
                LOG.error("Set bier topology failed!");
                return;
            }
            LOG.info("Set bier topology succeed!");
            return;
        } catch (InterruptedException e) {
            LOG.error("Set bier topology is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Set bier topology is faild cause by", e);
        }

        LOG.error("Set bier topology failed!");
        return;
    }

    public BierTopology getTopologyData(String topologyId) {
        BierDataProcess<BierTopology> processor =  new BierDataProcess<BierTopology>(dataBroker,
                BierDataProcess.FLAG_READ,(new BierTopologyBuilder()).build());
        final InstanceIdentifier<BierTopology> path = getTopoPath(topologyId);
        processor.enqueueOperation(new BierDataOperation() {
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
            if (null == topology || null == topology.getTopologyId()) {
                LOG.error("Get bier topology is faild!");
                return null;
            }
            return topology;
        } catch (InterruptedException e) {
            LOG.error("Get bier topology is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Get bier topology is faild cause by", e);
        }
        LOG.error("Get bier topology is faild!");
        return null;
    }

    public boolean setDomainData(final String topologyId,final List<BierDomain> domainList) {

        if (null == dataBroker || null == domainList || domainList.isEmpty()) {
            LOG.error("Set bier domain input is error!");
            return false;
        }

        BierDataProcess<BierDomain> processor =  new BierDataProcess<BierDomain>(dataBroker,
                BierDataProcess.FLAG_WRITE,(new BierDomainBuilder()).build());

        processor.enqueueOperation(new BierDataOperation() {
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
            if (null == result.get()) {
                LOG.error("Set bier domain failed!");
                return false;
            }

            LOG.info("Set bier domain succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("Set bier domain is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Set bier domain is faild cause by", e);
        }


        LOG.error("Set bier domain failed!");
        return false;
    }

    public BierDomain getDomainData(String topologyId,DomainId domainId) {

        BierDataProcess<BierDomain> processor =  new BierDataProcess<BierDomain>(dataBroker,
                BierDataProcess.FLAG_READ,(new BierDomainBuilder()).build());

        final InstanceIdentifier<BierDomain> path = getDomainPath(topologyId,domainId);

        processor.enqueueOperation(new BierDataOperation() {
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
            if (null == domain || null == domain.getDomainId()) {
                LOG.error("Get bier domain is faild!");
                return null;
            }
            return domain;
        } catch (InterruptedException e) {
            LOG.error("Get bier domain is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Get bier domain is faild cause by", e);
        }
        LOG.error("Get bier domain is faild!");
        return null;
    }

    public boolean setSubDomainData(final String topologyId,final DomainId domainId,
            final List<BierSubDomain> subDomainList) {
        if (null == dataBroker || null == subDomainList || subDomainList.isEmpty()) {
            LOG.error("Set bier sub-domai input is error!");
            return false;
        }

        BierDataProcess<BierSubDomain> processor =  new BierDataProcess<BierSubDomain>(dataBroker,
                BierDataProcess.FLAG_WRITE,(new BierSubDomainBuilder()).build());

        processor.enqueueOperation(new BierDataOperation() {
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
            if (null == result.get()) {
                LOG.error("Set bier sub-domain failed!");
                return false;
            }

            LOG.info("Set bier sub-domain succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("Set bier sub-domain is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Set bier sub-domain is faild cause by", e);
        }

        LOG.error("Set bier sub-domain failed!");
        return false;
    }

    public boolean delDomainData(final String topologyId,final DomainId domainId,final List<BierNode> nodeList) {
        if (null == dataBroker || null == domainId) {
            LOG.error("Del bier domain input is error!");
            return false;
        }

        BierDataProcess<BierDomain> processor =  new BierDataProcess<BierDomain>(dataBroker,
                BierDataProcess.FLAG_WRITE,(new BierDomainBuilder()).build());
        final InstanceIdentifier<BierDomain> path = getDomainPath(topologyId,domainId);

        processor.enqueueOperation(new BierDataOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                transaction.delete(datastoreType,path);
                updateAffectedDomainNode(transaction,topologyId,domainId,nodeList);
                updateAffectedTeDomainNode(transaction,topologyId,domainId,nodeList);
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
            if (null == result.get()) {
                LOG.error("Del bier domain failed!");
                return false;
            }

            LOG.info("Del bier domain succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("Del bier domain is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Del bier domain is faild cause by", e);
        }


        LOG.error("Del bier domain failed!");
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

    public void updateAffectedTeDomainNode(ReadWriteTransaction transaction,String topologyId,
                                           DomainId domainId,List<BierNode> nodeList) {
        int nodeSize = nodeList.size();
        for (int iloop = 0; iloop < nodeSize; ++iloop) {
            BierNode node = nodeList.get(iloop);
            int tedomainIndex = getTeDomainIndex(domainId,node);
            if (tedomainIndex != -1) {
                BierNodeBuilder newNodeBuilder = new BierNodeBuilder(node);
                List<TeDomain> newTeDomainList = newNodeBuilder.getBierTeNodeParams().getTeDomain();
                newTeDomainList.remove(tedomainIndex);
                InstanceIdentifier<BierNode> path = getNodePath(topologyId,node.getNodeId());
                transaction.put(datastoreType,path,node);
            }
        }
    }

    public boolean delSubDomainData(final String topologyId,final DomainId domainId,
            final SubDomainId subDomainId,final List<BierNode> nodeList) {

        if (null == dataBroker || null == domainId) {
            LOG.error("Del bier sub-domain input is error!");
            return false;
        }


        BierDataProcess<BierSubDomain> processor =  new BierDataProcess<BierSubDomain>(dataBroker,
                BierDataProcess.FLAG_WRITE,(new BierSubDomainBuilder()).build());
        final InstanceIdentifier<BierSubDomain> path = getSubDomainPath(topologyId,domainId,subDomainId);

        processor.enqueueOperation(new BierDataOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                transaction.delete(datastoreType,path);

                updateAffectedSubDomainNode(transaction,topologyId,domainId,subDomainId,nodeList);
                updateAffectedTeSubDomainNode(transaction,topologyId,domainId,subDomainId,nodeList);
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
            if (null == result.get()) {
                LOG.error("Del bier sub-domain failed!");
                return false;
            }

            LOG.info("Del bier sub-domain succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("Del bier sub-domain is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Del bier sub-domain is faild cause by", e);
        }

        LOG.error("Del bier sub-domain failed!");
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

    public void updateAffectedTeSubDomainNode(ReadWriteTransaction transaction,String topologyId,
                                              DomainId domainId,SubDomainId subDomainId,List<BierNode> nodeList) {
        if (nodeList == null) {
            return;
        }
        //List<BierNode> subDomainNodeList = new ArrayList<BierNode>();
        int nodeSize = nodeList.size();
        for (int iloop = 0; iloop < nodeSize; ++iloop) {
            BierNode node = nodeList.get(iloop);
            BierNodeBuilder newNodeBuilder = new BierNodeBuilder(node);
            int teDomainIndex = getTeDomainIndex(domainId,node);
            if (teDomainIndex != -1) {
                int teSubDomainIndex = getTeSubDomainIndex(domainId,subDomainId,node);
                if (teSubDomainIndex != -1) {
                    List<TeSubDomain> newTeSubDomainList = newNodeBuilder.getBierTeNodeParams().getTeDomain()
                            .get(teDomainIndex).getTeSubDomain();
                    newTeSubDomainList.remove(teSubDomainIndex);
                    if (newTeSubDomainList.isEmpty()) {
                        List<TeDomain> newTeDomainList = newNodeBuilder.getBierTeNodeParams().getTeDomain();
                        newTeDomainList.remove(teDomainIndex);
                    }
                    InstanceIdentifier<BierNode> path = getNodePath(topologyId,node.getNodeId());
                    transaction.put(datastoreType,path,newNodeBuilder.build());
                }
            }
        }
    }

    public boolean deleteTeLabel(final String topologyId,BierNode node) {
        if (null == dataBroker) {
            LOG.error("Del Te Label input is error!");
            return false;
        }
        BierDataProcess<BierTeLableRange> processor =  new BierDataProcess<BierTeLableRange>(dataBroker,
                BierDataProcess.FLAG_WRITE,(new BierTeLableRangeBuilder()).build());
        final InstanceIdentifier<BierTeLableRange> path = getLabelPath(topologyId,node.getNodeId());

        processor.enqueueOperation(new BierDataOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                transaction.delete(datastoreType,path);
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<Node>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        Future<ListenableFuture<BierTeLableRange>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierTeLableRange> result = future.get();
            if (null == result.get()) {
                LOG.error("Del Te Label failed!");
                return false;
            }

            LOG.info("Del Te Label succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("Del Te Label is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Del Te Label is faild cause by", e);
        }


        LOG.error("Del Te Label failed!");
        return false;
    }

    public boolean setNodeData(String topologyId, final BierNode node) {
        if (null == dataBroker || node == null) {
            LOG.error("Set bier node input is error!");
            return false;
        }

        BierDataProcess<BierNode> processor =  new BierDataProcess<BierNode>(dataBroker,
                BierDataProcess.FLAG_WRITE,(new BierNodeBuilder()).build());
        final InstanceIdentifier<BierNode> path = getNodePath(topologyId,node.getNodeId());

        processor.enqueueOperation(new BierDataOperation() {
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
            if (null == result.get()) {
                LOG.error("Set bier node failed!");
                return false;
            }

            LOG.info("Set bier node succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("Set bier node is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Set bier node is faild cause by", e);
        }

        LOG.error("Set bier node failed!");
        return false;
    }

    public BierNode getNodeData(String topologyId,String nodeId) {
        BierDataProcess<BierNode> processor =  new BierDataProcess<BierNode>(dataBroker,
                BierDataProcess.FLAG_READ,(new BierNodeBuilder()).build());
        final InstanceIdentifier<BierNode> path = getNodePath(topologyId,nodeId);

        processor.enqueueOperation(new BierDataOperation() {
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
            if (null == node || null == node.getNodeId()) {
                LOG.error("Get bier node is faild!");
                return null;
            }
            return node;
        } catch (InterruptedException e) {
            LOG.error("Get bier node is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Get bier node is faild cause by", e);
        }
        LOG.error("Get bier node is faild!");
        return null;
    }

    public boolean deleteBierTerminationPoint(final String topologyId,String nodeId,String tpId) {
        if (null == dataBroker || null == tpId) {
            LOG.error("Del bier Termination Point input is error!");
            return false;
        }

        BierDataProcess<BierTerminationPoint> processor =  new BierDataProcess<BierTerminationPoint>(dataBroker,
                BierDataProcess.FLAG_WRITE,(new BierTerminationPointBuilder()).build());
        final InstanceIdentifier<BierTerminationPoint> path = getBierTerminationPointPath(topologyId,nodeId,tpId);

        processor.enqueueOperation(new BierDataOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                transaction.delete(datastoreType,path);
            }

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<Node>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        Future<ListenableFuture<BierTerminationPoint>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierTerminationPoint> result = future.get();
            if (null == result.get()) {
                LOG.error("Del bier termination point failed!");
                return false;
            }

            LOG.info("Del bier termination point succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("Del bier termination point is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Del bier termination point is faild cause by", e);
        }


        LOG.error("Del bier termination point failed!");
        return false;
    }

    public boolean delTeBslFromNode(String topologyId,final DomainId domainId,
                                     final SubDomainId subDomainId,Bsl bitstringlength,final BierNode node) {
        BierDataProcess<BierNode> processor =  new BierDataProcess<BierNode>(dataBroker,
                BierDataProcess.FLAG_WRITE,(new BierNodeBuilder()).build());
        final InstanceIdentifier<BierNode> path = getNodePath(topologyId,node.getNodeId());

        processor.enqueueOperation(new BierDataOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                BierNodeBuilder newNodeBuilder = new BierNodeBuilder(node);
                int teBslDomainIndex = getTeDomainIndex(domainId,node);
                int teBslSubDomainIndex = getTeSubDomainIndex(domainId,subDomainId,node);
                int teBslIndex = getTeBslIndex(domainId,subDomainId,bitstringlength,node);
                List<TeBsl> newTeBslList = newNodeBuilder.getBierTeNodeParams().getTeDomain()
                        .get(teBslDomainIndex).getTeSubDomain().get(teBslSubDomainIndex).getTeBsl();
                newTeBslList.remove(teBslIndex);
                if (newTeBslList.isEmpty()) {
                    List<TeSubDomain> newTeSubdomainList = newNodeBuilder.getBierTeNodeParams()
                            .getTeDomain().get(teBslSubDomainIndex).getTeSubDomain();
                    newTeSubdomainList.remove(teBslSubDomainIndex);
                    if (newTeSubdomainList.isEmpty()) {
                        List<TeDomain> newTeDomainList = newNodeBuilder.getBierTeNodeParams().getTeDomain();
                        newTeDomainList.remove(teBslDomainIndex);
                    }
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
            if (null == result.get()) {
                LOG.error("Del Te-Bsl failed!");
                return false;
            }

            LOG.info("Del Te-Bsl succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("Del Te-Bsl is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Del Te-Bsl is faild cause by", e);
        }

        LOG.error("Del Te-Bsl failed!");
        return false;
    }

    public boolean delTeSiFromNode(String topologyId,final DomainId domainId,final SubDomainId subDomainId,
                                   final Bsl bitstringlength,final Si si ,final BierNode node) {
        BierDataProcess<BierNode> processor =  new BierDataProcess<BierNode>(dataBroker,
                BierDataProcess.FLAG_WRITE,(new BierNodeBuilder()).build());
        final InstanceIdentifier<BierNode> path = getNodePath(topologyId,node.getNodeId());

        processor.enqueueOperation(new BierDataOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                int teSiDomainIndex = getTeDomainIndex(domainId,node);
                int teSiSubDomainIndex = getTeSubDomainIndex(domainId,subDomainId,node);
                int teSiBslIndex = getTeBslIndex(domainId,subDomainId,bitstringlength,node);
                int teSiIndex = getTeSiIndex(domainId,subDomainId,bitstringlength,si,node);
                BierNodeBuilder newNodeBuilder = new BierNodeBuilder(node);
                List<TeSi> newTeSiList = newNodeBuilder.getBierTeNodeParams().getTeDomain().get(teSiDomainIndex)
                        .getTeSubDomain().get(teSiSubDomainIndex).getTeBsl().get(teSiBslIndex).getTeSi();
                newTeSiList.remove(teSiIndex);
                if (newTeSiList.isEmpty()) {
                    List<TeBsl> newTeBslList = newNodeBuilder.getBierTeNodeParams().getTeDomain()
                            .get(teSiDomainIndex).getTeSubDomain().get(teSiSubDomainIndex).getTeBsl();
                    newTeBslList.remove(teSiBslIndex);
                    if (newTeBslList.isEmpty()) {
                        List<TeSubDomain> newTeSubdomainList = newNodeBuilder.getBierTeNodeParams().getTeDomain()
                                .get(teSiDomainIndex).getTeSubDomain();
                        newTeSubdomainList.remove(teSiSubDomainIndex);
                        if (newTeSubdomainList.isEmpty()) {
                            List<TeDomain> newTeDomainList = newNodeBuilder.getBierTeNodeParams().getTeDomain();
                            newTeDomainList.remove(teSiDomainIndex);
                        }
                    }
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
            if (null == result.get()) {
                LOG.error("Del Te-Si failed!");
                return false;
            }

            LOG.info("Del Te-Si succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("Del Te-Si is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Del Te-Si is faild cause by", e);
        }

        LOG.error("Del Te-Si failed!");
        return false;
    }


    public boolean delTeBpFromNode(String topologyId,final DomainId domainId,final SubDomainId subDomainId,
                                   final Bsl bitstringlength,final Si si , final String tpId, final BierNode node) {
        BierDataProcess<BierNode> processor =  new BierDataProcess<BierNode>(dataBroker,
                BierDataProcess.FLAG_WRITE,(new BierNodeBuilder()).build());
        final InstanceIdentifier<BierNode> path = getNodePath(topologyId,node.getNodeId());

        processor.enqueueOperation(new BierDataOperation() {
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                BierNodeBuilder newNodeBuilder = new BierNodeBuilder(node);
                int teBpDomainIndex = getTeDomainIndex(domainId,node);
                int teBpSubDomainIndex = getTeSubDomainIndex(domainId,subDomainId,node);
                int teBpBslIndex = getTeBslIndex(domainId,subDomainId,bitstringlength,node);
                int teBpSiIndex = getTeSiIndex(domainId,subDomainId,bitstringlength,si,node);
                int teBpIndex = getTeBpIndex(domainId,subDomainId,bitstringlength,si,tpId,node);
                List<TeBp> newTeBpList = newNodeBuilder.getBierTeNodeParams().getTeDomain().get(teBpDomainIndex)
                        .getTeSubDomain().get(teBpSubDomainIndex).getTeBsl().get(teBpBslIndex).getTeSi()
                        .get(teBpSiIndex).getTeBp();
                newTeBpList.remove(teBpIndex);
                if (newTeBpList.isEmpty()) {
                    List<TeSi> newTeSiList = newNodeBuilder.getBierTeNodeParams().getTeDomain()
                            .get(teBpDomainIndex).getTeSubDomain().get(teBpSubDomainIndex).getTeBsl()
                            .get(teBpBslIndex).getTeSi();
                    newTeSiList.remove(teBpSiIndex);
                    if (newTeSiList.isEmpty()) {
                        List<TeBsl> newTeBslList = newNodeBuilder.getBierTeNodeParams()
                                .getTeDomain().get(teBpDomainIndex).getTeSubDomain().get(teBpSubDomainIndex)
                                .getTeBsl();
                        newTeBslList.remove(teBpBslIndex);
                        if (newTeBslList.isEmpty()) {
                            List<TeSubDomain> newTeSubdomainList = newNodeBuilder
                                    .getBierTeNodeParams().getTeDomain().get(teBpDomainIndex)
                                    .getTeSubDomain();
                            newTeSubdomainList.remove(teBpSubDomainIndex);
                            if (newTeSubdomainList.isEmpty()) {
                                List<TeDomain> newTeDomainList = newNodeBuilder.getBierTeNodeParams()
                                        .getTeDomain();
                                newTeDomainList.remove(teBpDomainIndex);
                            }
                        }
                    }
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
            if (null == result.get()) {
                LOG.error("Del Te-Bp failed!");
                return false;
            }

            LOG.info("Del Te-Bp succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("Del Te-Bp is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Del Te-Bp is faild cause by", e);
        }

        LOG.error("Del Te-Bp failed!");
        return false;
    }

    public boolean delNodeFromDomain(String topologyId,final DomainId domainId,
            final SubDomainId subDomainId,final BierNode node) {
        BierDataProcess<BierNode> processor =  new BierDataProcess<BierNode>(dataBroker,
                BierDataProcess.FLAG_WRITE,(new BierNodeBuilder()).build());
        final InstanceIdentifier<BierNode> path = getNodePath(topologyId,node.getNodeId());

        processor.enqueueOperation(new BierDataOperation() {
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
            if (null == result.get()) {
                LOG.error("Del bier node failed!");
                return false;
            }

            LOG.info("Del bier node succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("Del bier node is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Del bier node is faild cause by", e);
        }

        LOG.error("Del bier Node failed!");
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
        BierDataProcess<BierNode> processor =  new BierDataProcess<BierNode>(dataBroker,
                BierDataProcess.FLAG_WRITE,(new BierNodeBuilder()).build());

        final InstanceIdentifier<BierNode> path = getNodePath(topologyId,node.getNodeId());

        processor.enqueueOperation(new BierDataOperation() {
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
            if (null == result.get()) {
                LOG.error("Del bier node ipv4 or ipv6 failed!");
                return false;
            }

            LOG.info("Del bier node ipv4 or ipv6 succeed!");
            return true;
        } catch (InterruptedException e) {
            LOG.error("Del bier node ipv4 or ipv6 is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Del bier node ipv4 or ipv6 is faild cause by", e);
        }

        LOG.error("Del bier Node ipv4 or ipv6 failed!");
        return false;
    }

    public BierLink getLinkData(String topologyId,String linkId) {
        BierDataProcess<BierLink> processor =  new BierDataProcess<BierLink>(dataBroker,
                BierDataProcess.FLAG_READ,(new BierLinkBuilder()).build());

        final InstanceIdentifier<BierTopology> topoPath = getTopoPath(topologyId);
        final InstanceIdentifier<BierLink> path = topoPath.child(BierLink.class, new BierLinkKey(linkId));

        processor.enqueueOperation(new BierDataOperation() {

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
            if (null == link || null == link.getLinkId()) {
                LOG.error("Get bier link is faild!");
                return null;
            }
            return link;
        } catch (InterruptedException e) {
            LOG.error("Get bier link is Interrupted by", e);
        } catch (ExecutionException e) {
            LOG.error("Get bier link is faild cause by", e);
        }
        LOG.error("Get bier link is faild!");
        return null;
    }

    public List<BierNode> getSubDomainNode(String topologyId,DomainId domainId,
            SubDomainId subDomainId) {
        List<BierNode> nodeList = new ArrayList<BierNode>();
        BierTopology  topo = getTopologyData(topologyId);
        if (topo == null) {
            LOG.error("QuerySubdomainNode rpc topo is not exist!");
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

    public List<BierNode> getTeSubDomainNode(String topologyId,DomainId domainId,
                                           SubDomainId subDomainId) {
        List<BierNode> nodeList = new ArrayList<BierNode>();
        BierTopology  topo = getTopologyData(topologyId);
        if (topo == null) {
            LOG.error("QueryTeSubdomainNode rpc topo is not exist!");
            return nodeList;
        }

        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
        List<BierNode> allNodeList = bierTopoBuilder.getBierNode();
        int nodeSize = allNodeList.size();
        for (int iloop = 0; iloop < nodeSize; ++iloop) {
            BierNode node = allNodeList.get(iloop);
            boolean findFlag = isNodeBelongToTeSubDomain(domainId,subDomainId,node);
            if (findFlag) {
                nodeList.add(node);
            }
        }

        return nodeList;
    }

    public boolean isNodeBelongToSubDomain(DomainId domainId,SubDomainId subDomainId,BierNode node) {
        boolean findFlag = false;
        BierNodeBuilder nodeBuilder = new BierNodeBuilder(node);
        if (nodeBuilder.getBierNodeParams() == null) {
            return false;
        }
        List<Domain> domainList = nodeBuilder.getBierNodeParams().getDomain();
        if (domainList == null) {
            return false;
        }
        int domainSize = domainList.size();
        for (int jloop = 0; jloop < domainSize; ++jloop) {
            Domain domain = domainList.get(jloop);
            if (!domainId.equals(domain.getDomainId())) {
                continue;
            }

            List<SubDomain> subDomainList = domain.getBierGlobal().getSubDomain();
            if (subDomainList == null) {
                return false;
            }
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

    public boolean isNodeBelongToTeSubDomain(DomainId domainId,SubDomainId subDomainId,BierNode node) {
        boolean findFlag = false;
        BierNodeBuilder nodeBuilder = new BierNodeBuilder(node);
        if (nodeBuilder.getBierTeNodeParams() == null) {
            return false;
        }
        List<TeDomain> domainList = nodeBuilder.getBierTeNodeParams().getTeDomain();
        if (domainList == null) {
            return false;
        }
        int domainSize = domainList.size();
        for (int jloop = 0; jloop < domainSize; ++jloop) {
            TeDomain domain = domainList.get(jloop);
            if (!domainId.equals(domain.getDomainId())) {
                continue;
            }

            List<TeSubDomain> subDomainList = domain.getTeSubDomain();
            if (subDomainList == null) {
                return false;
            }
            int subDomainSize = subDomainList.size();
            for (int kloop = 0; kloop < subDomainSize; ++kloop) {
                TeSubDomain subDomain = subDomainList.get(kloop);
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
            LOG.error("QuerySubdomainLink rpc topo is not exist!");
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

    public List<BierLink> getTeSubDomainLink(String topologyId,DomainId domainId,SubDomainId subDomainId) {
        List<BierLink> teLinkList = new ArrayList<BierLink>();
        BierTopology  topo = getTopologyData(topologyId);
        if (topo == null) {
            LOG.error("QueryTeSubdomainLink rpc topo is not exist!");
            return teLinkList;
        }

        BierTopologyBuilder bierTeTopoBuilder = new BierTopologyBuilder(topo);
        List<BierLink> allTeLinkList = bierTeTopoBuilder.getBierLink();
        int linkSize = allTeLinkList.size();
        for (int iloop = 0; iloop < linkSize; ++iloop) {
            BierLink teLink = allTeLinkList.get(iloop);
            BierLinkBuilder teLinkBuilder = new BierLinkBuilder(teLink);
            String sourceNodeId = teLinkBuilder.getLinkSource().getSourceNode();
            String destNodeId = teLinkBuilder.getLinkDest().getDestNode();
            BierNode sourceNode = null;
            BierNode destNode = null;
            List<BierNode> allTeNodeList = bierTeTopoBuilder.getBierNode();
            int nodeSize = allTeNodeList.size();
            for (int jloop = 0; jloop < nodeSize; ++jloop) {
                BierNode node = allTeNodeList.get(jloop);
                String nodeId = node.getNodeId();
                if (nodeId.equals(sourceNodeId)) {
                    sourceNode = node;
                } else if (nodeId.equals(destNodeId)) {
                    destNode = node;
                }
            }

            if (sourceNode != null || destNode != null) {
                boolean findSourceFlag = isNodeBelongToTeSubDomain(domainId,subDomainId,sourceNode);
                boolean findDestFlag = isNodeBelongToTeSubDomain(domainId,subDomainId,destNode);
                if (findSourceFlag && findDestFlag) {
                    teLinkList.add(teLink);
                }
            }
        }

        return teLinkList;
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

    public boolean checkTeDomainExist(String topologyId,List<TeDomain> domainList) {
        if (domainList == null || domainList.isEmpty()) {
            return false;
        }

        TeDomain domain = domainList.get(0);
        DomainId domainId  = domain.getDomainId();
        BierDomain bierDomain = getDomainData(topologyId,domainId);
        if (bierDomain == null) {
            return false;
        }

        TeDomainBuilder teDomainBuilder = new TeDomainBuilder(domain);
        List<TeSubDomain> tesubDomainList = teDomainBuilder.getTeSubDomain();
        if (tesubDomainList == null || tesubDomainList.isEmpty()) {
            return true;
        }
        boolean tesubDomainExistFlag = false;
        SubDomainId subDomainId  = tesubDomainList.get(0).getSubDomainId();
        List<BierSubDomain> bierSubDomainList = bierDomain.getBierSubDomain();
        if (bierSubDomainList == null) {
            return false;
        }
        int subDomainSize = bierSubDomainList.size();
        for (int iloop = 0; iloop < subDomainSize; ++iloop) {
            BierSubDomain subDomain = bierSubDomainList.get(iloop);
            if (subDomainId.equals(subDomain.getSubDomainId())) {
                tesubDomainExistFlag = true;
                break;
            }
        }
        if (!tesubDomainExistFlag) {
            return false;
        }

        return true;
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

    public boolean checkNodeBelongToTeDomain(DomainId domainId,SubDomainId subDomainId,BierNode node) {
        if (getTeSubDomainIndex(domainId,subDomainId,node) == -1) {
            return false;
        }

        return true;
    }

    public boolean checkBitstringlengthExist(String topologyId,DomainId domainId,SubDomainId subDomainId,
                                  Bsl bitstringlength,BierNode node) {
        if (-1 == getTeBslIndex(domainId,subDomainId,bitstringlength,node)) {
            return false;
        }
        return true;
    }


    public boolean checkSiExist(String topologyId,DomainId domainId,SubDomainId subDomainId,
                                             Bsl bitstringlength,Si si,BierNode node) {
        if (-1 == getTeSiIndex(domainId,subDomainId,bitstringlength,si,node)) {
            return false;
        }
        return true;
    }


    public boolean checkTpIdExist(String topologyId,DomainId domainId,SubDomainId subDomainId,
                                Bsl bitstringlength,Si si,String tpid,BierNode node) {
        if (-1 == getTeBpIndex(domainId,subDomainId,bitstringlength,si,tpid,node)) {
            return false;
        }
        return true;
    }

    public boolean checkLabelExist(BierNode node) {
        if (node.getBierTeLableRange() == null) {
            return false;
        }
        return true;
    }

    public int getDomainIndex(DomainId domainId,BierNode node) {
        if (node.getBierNodeParams() == null) {
            return -1;
        }
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


    public int getTeDomainIndex(DomainId domainId,BierNode node) {
        if (node.getBierTeNodeParams() == null) {
            return -1;
        }
        List<TeDomain> domainList =  node.getBierTeNodeParams().getTeDomain();
        if (domainList == null) {
            return -1;
        }
        int domainSize = domainList.size();
        for (int iloop = 0; iloop < domainSize; ++iloop) {
            TeDomain domain = domainList.get(iloop);
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

    public int getTeSubDomainIndex(DomainId domainId,SubDomainId subDomainId,BierNode node) {
        int domainIndex = getTeDomainIndex(domainId,node);
        if (domainIndex == -1) {
            return -1;
        }

        TeDomain domain = node.getBierTeNodeParams().getTeDomain().get(domainIndex);
        List<TeSubDomain> subDomainList = domain.getTeSubDomain();
        if (subDomainList == null) {
            return -1;
        }
        int subDomainSize = subDomainList.size();
        for (int iloop = 0; iloop < subDomainSize; ++iloop) {
            TeSubDomain subDomain = subDomainList.get(iloop);
            if (subDomain.getSubDomainId().equals(subDomainId)) {
                return iloop;
            }
        }

        return -1;
    }

    public int getTeBslIndex(DomainId domainId, SubDomainId subDomainId, Bsl bitstringlength, BierNode node) {
        int domainIndex = getTeDomainIndex(domainId,node);
        if (domainIndex == -1) {
            return -1;
        }
        int subdomainIndex = getTeSubDomainIndex(domainId,subDomainId,node);
        if (subdomainIndex == -1) {
            return -1;
        }
        TeSubDomain subDomain = node.getBierTeNodeParams().getTeDomain().get(domainIndex)
                .getTeSubDomain().get(subdomainIndex);
        List<TeBsl> bslList = subDomain.getTeBsl();
        if (bslList == null) {
            return -1;
        }
        int bslSize = bslList.size();
        for (int iloop = 0; iloop < bslSize; ++iloop) {
            TeBsl bsl = bslList.get(iloop);
            if (bsl.getBitstringlength().equals(bitstringlength)) {
                return iloop;
            }
        }
        return -1;
    }

    public int getTeSiIndex(DomainId domainId, SubDomainId subDomainId, Bsl bitstringlength, Si si, BierNode node) {
        int domainIndex = getTeDomainIndex(domainId,node);
        if (domainIndex == -1) {
            return -1;
        }
        int subdomainIndex = getTeSubDomainIndex(domainId,subDomainId,node);
        if (subdomainIndex == -1) {
            return -1;
        }
        int bslIndex = getTeBslIndex(domainId,subDomainId,bitstringlength,node);
        if (bslIndex == -1) {
            return -1;
        }
        TeBsl bsl = node.getBierTeNodeParams().getTeDomain().get(domainIndex).getTeSubDomain()
                .get(subdomainIndex).getTeBsl().get(bslIndex);
        List<TeSi> siList = bsl.getTeSi();
        if (siList == null) {
            return -1;
        }
        int siSize = siList.size();
        for (int iloop = 0; iloop < siSize; ++iloop) {
            TeSi teSi = siList.get(iloop);
            if (teSi.getSi().equals(si)) {
                return iloop;
            }
        }

        return -1;
    }

    public int getTeBpIndex(DomainId domainId, SubDomainId subDomainId, Bsl bitstringlength,
                            Si si, String tpId,BierNode node) {
        int domainIndex = getTeDomainIndex(domainId,node);
        if (domainIndex == -1) {
            return -1;
        }
        int subdomainIndex = getTeSubDomainIndex(domainId,subDomainId,node);
        if (subdomainIndex == -1) {
            return -1;
        }
        int bslIndex = getTeBslIndex(domainId,subDomainId,bitstringlength,node);
        if (bslIndex == -1) {
            return -1;
        }
        int siIndex = getTeSiIndex(domainId,subDomainId,bitstringlength,si,node);
        if (siIndex == -1) {
            return -1;
        }
        TeSi tesi = node.getBierTeNodeParams().getTeDomain().get(domainIndex).getTeSubDomain()
                .get(subdomainIndex).getTeBsl().get(bslIndex).getTeSi().get(siIndex);
        List<TeBp> teBpList = tesi.getTeBp();
        if (teBpList == null) {
            return -1;
        }
        int teBpSize = teBpList.size();
        for (int iloop = 0; iloop < teBpSize; ++iloop) {
            TeBp teBp = teBpList.get(iloop);
            if (teBp.getTpId().equals(tpId)) {
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

    public InstanceIdentifier<BierTerminationPoint> getBierTerminationPointPath(String topologyId,String nodeId,
                                                                                String tpId) {
        InstanceIdentifier<BierNode> nodePath = getNodePath(topologyId,nodeId);
        InstanceIdentifier<BierTerminationPoint> path = nodePath.child(BierTerminationPoint.class,
                new BierTerminationPointKey(tpId));
        return path;
    }

    public InstanceIdentifier<BierTeLableRange> getLabelPath(String topologyId,String nodeId) {
        InstanceIdentifier<BierNode> nodePath = getNodePath(topologyId,nodeId);
        InstanceIdentifier<BierTeLableRange> path = nodePath.child(BierTeLableRange.class);
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

    public boolean checkFtLabel(String topologyId,BierNode node) {
        LOG.info("checkFtLabel..............................");
        BierNode existNode = getNodeData(topologyId, node.getNodeId());
        DomainId teDomainId = node.getBierTeNodeParams().getTeDomain().get(0).getDomainId();
        SubDomainId teSubDomainId = node.getBierTeNodeParams().getTeDomain().get(0).getTeSubDomain()
                .get(0).getSubDomainId();
        Bsl teBitStringLength = node.getBierTeNodeParams().getTeDomain().get(0).getTeSubDomain()
                .get(0).getTeBsl().get(0).getBitstringlength();
        Si teSi = node.getBierTeNodeParams().getTeDomain().get(0).getTeSubDomain()
                .get(0).getTeBsl().get(0).getTeSi().get(0).getSi();
        int teSiIndex = getTeSiIndex(teDomainId,teSubDomainId,teBitStringLength,teSi,existNode);
        LOG.info("teSiIndex........................." + teSiIndex);
        if (teSiIndex == -1) {
            return false;
        }
        return true;
    }

    public List<Long> checkFtLabel(String topologyId,String nodeId) {
        List<Long> labelList = new ArrayList<Long>();
        BierNode existNode = getNodeData(topologyId, nodeId);
        if (existNode.getBierTeNodeParams() == null) {
            return labelList;
        }
        List<TeDomain> teDomainList = existNode.getBierTeNodeParams().getTeDomain();
        if (teDomainList == null) {
            return labelList;
        }
        int teDomainSize = teDomainList.size();
        for (int iloop = 0; iloop < teDomainSize; ++iloop) {
            List<TeSubDomain> teSubDomainList = teDomainList.get(iloop).getTeSubDomain();
            if (teSubDomainList == null) {
                return labelList;
            }
            int teSubDomainSize = teSubDomainList.size();
            for (int jloop = 0; jloop < teSubDomainSize; ++jloop) {
                List<TeBsl> teBslList = teSubDomainList.get(jloop).getTeBsl();
                if (teBslList == null) {
                    return labelList;
                }
                labelList = checkFtLabel(teBslList,labelList);
            }
        }
        return labelList;
    }

    public List<Long> checkFtLabel(List<TeBsl> teBslList,List<Long> labelList) {
        int teBslSize = teBslList.size();
        for (int kloop = 0; kloop < teBslSize; ++kloop) {
            List<TeSi> teSiList = teBslList.get(kloop).getTeSi();
            if (teSiList == null) {
                return labelList;
            }
            int teSiSize = teSiList.size();
            for (int lloop = 0; lloop < teSiSize; ++lloop) {
                Long ftLabel = teSiList.get(lloop).getFtLabel().getValue();
                labelList.add(ftLabel);
            }
        }
        return labelList;
    }

    public Long buildFtLabel(String topologyId,BierNode node) {
        BierNode existNode = getNodeData(topologyId, node.getNodeId());
        MplsLabel labelBaseData = existNode.getBierTeLableRange().getLabelBase();
        BierTeLabelRangeSize labelRangeSizeData = existNode.getBierTeLableRange().getLabelRangeSize();
        Long labelBase = labelBaseData.getValue();
        Long labelRangeSize = labelRangeSizeData.getValue();
        Long labelMax = labelBase + labelRangeSize;
        Long buildLabel = labelBase;
        List<Long> exisitFtLabel = checkFtLabel(topologyId, node.getNodeId());
        LOG.info("exisitFtLabel....................." + exisitFtLabel);
        if (exisitFtLabel.size() > 0) {
            buildLabel = buildFtLabel(buildLabel,exisitFtLabel);
            Short checkNum = 0;
            while (!checkFtLabelExisit(buildLabel,exisitFtLabel)) {
                buildLabel++;
                checkNum++;
                if (buildLabel > labelMax) {
                    buildLabel = labelBase;
                }
                if (checkNum > labelRangeSize) {
                    buildLabel = -1L;
                }
            }

        }
        return buildLabel;
    }

    public Long buildFtLabel(Long buildLabel,List<Long> exisitFtLabel) {
        for (int iloop = 0; iloop < exisitFtLabel.size(); iloop++) {
            if (buildLabel < exisitFtLabel.get(iloop)) {
                buildLabel = exisitFtLabel.get(iloop);
            }
        }
        return buildLabel;
    }

    public boolean checkFtLabelExisit(Long buildLabel,List<Long> exisitFtLabel) {
        for (int iloop = 0; iloop < exisitFtLabel.size(); iloop++) {
            if (buildLabel.equals(exisitFtLabel.get(iloop))) {
                return false;
            }
        }
        return true;
    }


    public boolean checkTpId(String topologyId,BierNode node) {
        BierNode existNode = getNodeData(topologyId, node.getNodeId());
        if (node.getBierTeNodeParams() == null) {
            return true;
        }
        List<TeDomain> teDomainList = node.getBierTeNodeParams().getTeDomain();
        if (teDomainList == null) {
            return true;
        }
        int teDomainSize = teDomainList.size();
        for (int iloop = 0; iloop < teDomainSize; ++iloop) {
            DomainId teDomainId = teDomainList.get(iloop).getDomainId();
            List<TeSubDomain> teSubDomainList = teDomainList.get(iloop).getTeSubDomain();
            if (teSubDomainList == null) {
                return true;
            }
            int teSubDomainSize = teSubDomainList.size();
            for (int jloop = 0; jloop < teSubDomainSize; ++jloop) {
                SubDomainId teSubDomainId =  teSubDomainList.get(jloop).getSubDomainId();
                List<TeBsl> teBslList = teSubDomainList.get(jloop).getTeBsl();
                if (teBslList == null) {
                    return true;
                }
                if (!checkTpId(topologyId,teDomainId,teSubDomainId,teBslList,existNode)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkTpId(String topologyId,DomainId teDomainId,SubDomainId teSubDomainId,
                             List<TeBsl> teBslList,BierNode exisitNode) {
        int teBslSize = teBslList.size();
        for (int kloop = 0; kloop < teBslSize; ++kloop) {
            Bsl teBitStringLength = teBslList.get(kloop).getBitstringlength();
            List<TeSi> teSiList = teBslList.get(kloop).getTeSi();
            if (teSiList == null) {
                return true;
            }
            int teSiSize = teSiList.size();
            for (int lloop = 0; lloop < teSiSize; ++lloop) {
                Si teSi = teSiList.get(lloop).getSi();
                List<TeBp> teBpList = teSiList.get(lloop).getTeBp();
                if (teBpList == null) {
                    return true;
                }

                int teDomainIndex = getTeDomainIndex(teDomainId,exisitNode);
                int teSubDomainIndex = getTeSubDomainIndex(teDomainId,teSubDomainId,exisitNode);
                int teBslIndex = getTeBslIndex(teDomainId,teSubDomainId,teBitStringLength,exisitNode);
                int teSiIndex = getTeSiIndex(teDomainId,teSubDomainId,teBitStringLength,teSi,exisitNode);
                if (teSiIndex != -1) {
                    List<TeBp> exisitBpList = exisitNode.getBierTeNodeParams().getTeDomain().get(teDomainIndex)
                            .getTeSubDomain().get(teSubDomainIndex).getTeBsl().get(teBslIndex).getTeSi()
                            .get(teSiIndex).getTeBp();
                    if (!checkTpIdExisit(teBpList,exisitBpList)) {
                        return false;
                    }

                }
            }
        }
        return true;
    }

    public boolean checkTpIdExisit(List<TeBp> teBpList,List<TeBp> exisitBpList) {
        for (int iloop = 0; iloop < teBpList.size(); iloop++) {
            for (int jloop = 0; jloop < exisitBpList.size(); jloop++) {
                String tpId = teBpList.get(iloop).getTpId();
                int bitposition = teBpList.get(iloop).getBitposition();
                String exisitpId = exisitBpList.get(jloop).getTpId();
                int exisitBitposition = exisitBpList.get(jloop).getBitposition();

                if (tpId.equals(exisitpId) || (bitposition == exisitBitposition)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkNodeBfrId(String topologyId,BierNode node) {
        if (node.getBierNodeParams() == null) {
            return true;
        }
        List<Domain> domainList = node.getBierNodeParams().getDomain();
        if (domainList == null) {
            return true;
        }
        int domainSize = domainList.size();
        for (int iloop = 0; iloop < domainSize; ++iloop) {
            DomainId domainId = domainList.get(iloop).getDomainId();
            List<SubDomain> subDomainList = domainList.get(iloop).getBierGlobal().getSubDomain();
            if (subDomainList == null) {
                return true;
            }
            int subDomainSize = subDomainList.size();
            for (int jloop = 0; jloop < subDomainSize; ++jloop) {
                SubDomainId subDomainId =  subDomainList.get(jloop).getSubDomainId();
                if (!checkNodeBfrId(topologyId,domainId,subDomainId,node)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean checkNodeBfrId(String topologyId,DomainId domainId,
            SubDomainId subDomainId,BierNode node) {
        List<BierNode> nodeList = getSubDomainNode(topologyId,domainId,subDomainId);
        int nodeSize = nodeList.size();
        for (int i = 0; i < nodeSize; ++i) {
            BierNode node1 = nodeList.get(i);
            if (node1.getNodeId().equals(node.getNodeId())) {
                continue;
            }

            BfrId bfrId = getNodeBfrId(domainId,subDomainId,node);
            BfrId bfrId1 = getNodeBfrId(domainId,subDomainId,node1);
            if (bfrId.equals(bfrId1)) {
                return false;
            }
        }

        return true;
    }

    public BfrId getNodeBfrId(DomainId domainId,SubDomainId subDomainId,BierNode node) {
        BfrId bfrId = new BfrId(new Integer(0));
        int domainIndex = getDomainIndex(domainId,node);
        if (domainIndex != -1) {
            BfrId globalBfrId = node.getBierNodeParams().getDomain().get(domainIndex)
                    .getBierGlobal().getBfrId();
            int subDomainIndex = getSubDomainIndex(domainId,subDomainId,node);
            if (subDomainIndex != -1) {
                bfrId = node.getBierNodeParams().getDomain().get(domainIndex)
                        .getBierGlobal().getSubDomain().get(subDomainIndex).getBfrId();
                if (bfrId.getValue().intValue() == 0) {
                    bfrId = globalBfrId;
                }
            }
        }

        return bfrId;
    }

    public class BierMplsLabel {
        DomainId domainId;
        SubDomainId subDomainId;
        String type;
        int bitstringlength;
        long labelbase;
        long rangesize;
    }

    public boolean checkNodeLabel(BierNode node,BierNode newNode) {
        List<BierMplsLabel> labelList = getNodeLabel(node);
        List<BierMplsLabel> newLabelList = getNodeLabel(newNode);

        int newLabelSize = newLabelList.size();
        for (int iloop = 0; iloop < newLabelSize - 1; ++iloop) {
            BierMplsLabel label = newLabelList.get(iloop);

            for (int jloop = iloop + 1; jloop < newLabelSize; ++jloop) {
                BierMplsLabel label1 = newLabelList.get(jloop);

                if (!checkNodeLabel(label,label1)) {
                    return false;
                }
            }
        }

        int labelSize = labelList.size();
        if (labelSize == 0) {
            return true;
        }
        for (int iloop = 0; iloop < newLabelSize; ++iloop) {
            BierMplsLabel label = newLabelList.get(iloop);

            for (int jloop = 0; jloop < labelSize; ++jloop) {
                BierMplsLabel label1 = labelList.get(jloop);

                if (label.domainId.equals(label1.domainId) && label.subDomainId.equals(label1.subDomainId)
                        && label.type.equals(label1.type)
                        && label.bitstringlength == label1.bitstringlength
                        && label.labelbase == label1.labelbase) {
                    continue;
                }

                if (!checkNodeLabel(label,label1)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean checkNodeLabel(BierMplsLabel label,BierMplsLabel label1) {
        Long begin = label.labelbase;
        Long end = label.labelbase + label.rangesize - 1;
        Long begin1 = label1.labelbase;
        Long end1 = label1.labelbase + label1.rangesize - 1;

        if (begin >= begin1 && begin <= end1 || end >= begin1 && end <= end1) {
            return false;
        }

        return true;
    }

    public List<BierMplsLabel> getNodeLabel(BierNode node) {
        List<BierMplsLabel> labelList = new ArrayList<BierMplsLabel>();
        if (node.getBierNodeParams() == null) {
            return labelList;
        }
        List<Domain> domainList = node.getBierNodeParams().getDomain();
        if (domainList != null) {
            int domainSize = domainList.size();
            for (int iloop = 0; iloop < domainSize; ++iloop) {
                List<SubDomain> subDomainList = domainList.get(iloop).getBierGlobal().getSubDomain();
                if (subDomainList != null) {
                    int subDomainSize = subDomainList.size();
                    for (int jloop = 0; jloop < subDomainSize; ++jloop) {
                        if (subDomainList.get(jloop).getAf() != null) {
                            List<Ipv4> ipv4List = subDomainList.get(jloop).getAf().getIpv4();
                            if (ipv4List != null) {
                                int ipv4Size = ipv4List.size();
                                for (int kloop = 0; kloop < ipv4Size; ++kloop) {
                                    BierMplsLabel label = new BierMplsLabel();
                                    label.domainId = domainList.get(iloop).getDomainId();
                                    label.subDomainId = subDomainList.get(jloop).getSubDomainId();
                                    label.type = "ipv4";
                                    Ipv4 ipv4 = ipv4List.get(kloop);
                                    label.bitstringlength = ipv4.getBitstringlength();
                                    label.labelbase = ipv4.getBierMplsLabelBase().getValue();
                                    label.rangesize = ipv4.getBierMplsLabelRangeSize().getValue();
                                    labelList.add(label);
                                }
                            }

                            List<Ipv6> ipv6List = subDomainList.get(jloop).getAf().getIpv6();
                            if (ipv6List != null) {
                                int ipv6Size = ipv6List.size();
                                for (int kloop = 0; kloop < ipv6Size; ++kloop) {
                                    BierMplsLabel label = new BierMplsLabel();
                                    label.domainId = domainList.get(iloop).getDomainId();
                                    label.subDomainId = subDomainList.get(jloop).getSubDomainId();
                                    label.type = "ipv6";
                                    Ipv6 ipv6 = ipv6List.get(kloop);
                                    label.bitstringlength = ipv6.getBitstringlength();
                                    label.labelbase = ipv6.getBierMplsLabelBase().getValue();
                                    label.rangesize = ipv6.getBierMplsLabelRangeSize().getValue();
                                    labelList.add(label);
                                }
                            }
                        }
                    }
                }
            }
        }

        return labelList;
    }

    public String checkBierNodeParams(BierNode node,BierNodeParamsBuilder nodeParamsBuilder) {
        String errorMsg = "";

        List<Domain> domainList = nodeParamsBuilder.getDomain();
        if (domainList == null || domainList.isEmpty()) {
            errorMsg = " domain is null or empty!";
            return errorMsg;
        }

        int domainSize = domainList.size();
        for (int iloop = 0; iloop < domainSize; ++iloop) {
            Domain domain = domainList.get(iloop);
            DomainId domainId = domain.getDomainId();
            int domainIndex = getDomainIndex(domainId,node);
            if (domainIndex == -1) {
                if (domain.getBierGlobal().getBitstringlength() == null) {
                    errorMsg = "domain bitstringlength is null!";
                    return errorMsg;
                }

                if (domain.getBierGlobal().getBfrId() == null) {
                    errorMsg = "domain bfrid is null!";
                    return errorMsg;
                }

                if (domain.getBierGlobal().getIpv4BfrPrefix() == null
                        && domain.getBierGlobal().getIpv6BfrPrefix() == null) {
                    errorMsg = "domain ipv4-bfr-prefix and ipv6-bfr-prefix are null on the same time!";
                    return errorMsg;
                }
            }

            List<SubDomain> subDomainList = domain.getBierGlobal().getSubDomain();
            if (subDomainList == null || subDomainList.isEmpty()) {
                continue;
            }
            int subDomainSize = subDomainList.size();
            for (int jloop = 0; jloop < subDomainSize; ++jloop) {
                SubDomain subDomain = subDomainList.get(jloop);
                int subDomainIndex = getSubDomainIndex(domainId,subDomain.getSubDomainId(),node);
                if (subDomainIndex == -1) {
                    if (subDomain.getIgpType() == null) {
                        errorMsg = "subdomain igp-type is null!";
                        return errorMsg;
                    }
                    if (domain.getBierGlobal().getEncapsulationType() != null
                            && domain.getBierGlobal().getEncapsulationType().equals(BierEncapsulationMpls.class)) {
                        if (subDomain.getAf() == null) {
                            errorMsg = "subdomain af is null when encapsulation-type is mpls!";
                            return errorMsg;
                        }

                        if ((subDomain.getAf().getIpv4() == null || subDomain.getAf().getIpv4().isEmpty())
                            && (subDomain.getAf().getIpv6() == null || subDomain.getAf().getIpv6().isEmpty())) {
                            errorMsg = "subdomain ipv4 and ipv6 are null on the same time!";
                            return errorMsg;
                        }
                    }
                }
            }
        }

        return errorMsg;
    }

    public String checkBierTeNodeParams(BierNode node, BierTeNodeParamsBuilder teNodeParamsBuilder) {
        String errorMsg = "";

        List<TeDomain> teDomainList = teNodeParamsBuilder.getTeDomain();
        if (teDomainList == null || teDomainList.isEmpty()) {
            errorMsg = " te-domain is null or empty!";
            return errorMsg;
        }

        int teDomainSize = teDomainList.size();
        for (int iloop = 0; iloop < teDomainSize; ++iloop) {
            TeDomain teDomain = teDomainList.get(iloop);
            DomainId teDomainId = teDomain.getDomainId();
            int teDomainIndex = getTeDomainIndex(teDomainId,node);

            List<TeSubDomain> teSubDomainList = teDomain.getTeSubDomain();
            if (teSubDomainList == null || teSubDomainList.isEmpty()) {
                continue;
            }
            int teSubDomainSize = teSubDomainList.size();
            for (int jloop = 0; jloop < teSubDomainSize; ++jloop) {
                TeSubDomain teSubDomain = teSubDomainList.get(jloop);
                int teSubDomainIndex = getTeSubDomainIndex(teDomainId,teSubDomain.getSubDomainId(),node);
                List<TeBsl> teBslList = teSubDomain.getTeBsl();
                if (teBslList == null || teBslList.isEmpty()) {
                    continue;
                }
                int teBslSize = teBslList.size();
                for (int bslloop = 0; bslloop < teBslSize; ++bslloop) {
                    TeBsl teBsl = teBslList.get(bslloop);
                    int teBslIndex = getTeBslIndex(teDomainId,teSubDomain.getSubDomainId(),
                            teBsl.getBitstringlength(),node);
                    if (teBslIndex == -1) {
                        if (teBsl.getBitstringlength() == null) {
                            errorMsg = "bitstringlength is null!";
                            return errorMsg;
                        }
                    }
                    List<TeSi> teSiList = teBsl.getTeSi();
                    if (teSiList == null || teSiList.isEmpty()) {
                        continue;
                    }
                    int teSiSize = teSiList.size();
                    for (int siloop = 0; siloop < teSiSize; ++siloop) {
                        TeSi teSi = teSiList.get(siloop);
                        int teSiIndex = getTeSiIndex(teDomainId, teSubDomain.getSubDomainId(),
                                teBsl.getBitstringlength(), teSi.getSi(), node);
                        if (teSiIndex == -1) {
                            if (teSi.getSi() == null) {
                                errorMsg = "si is null!";
                                return errorMsg;
                            }
                        }

                        List<TeBp> teBpList = teSi.getTeBp();
                        if (teBpList == null || teSiList.isEmpty()) {
                            errorMsg = "te-bp is null or empty!";
                            return errorMsg;
                        }
                        int teBpSize = teBpList.size();
                        for (int bpLoop = 0; bpLoop < teBpSize; bpLoop++) {
                            TeBp teBp = teBpList.get(bpLoop);
                            if (teBp.getTpId() == null || teBp.getBitposition() == null) {
                                errorMsg = "tp-id or bitposition is null or empty!";
                                return errorMsg;
                            }
                        }
                    }
                }
            }
        }
        return errorMsg;
    }

    public String checkLableRangeParams(BierTeLableRangeBuilder teLableRangeBuilder) {
        String errorMsg = "";

        MplsLabel labelBase = teLableRangeBuilder.getLabelBase();
        if (labelBase == null) {
            errorMsg = "label-base is null!";
            return errorMsg;
        }
        BierTeLabelRangeSize labelRangeSize = teLableRangeBuilder.getLabelRangeSize();
        if (labelRangeSize == null) {
            errorMsg = "label-range-size is null!";
            return errorMsg;
        }

        Long plus = labelBase.getValue() + labelRangeSize.getValue();
        if (plus < 0L || plus > 1048575L) {
            errorMsg = "label-base plus label-range-size range [0‥1048575]";
            return errorMsg;
        }

        return errorMsg;
    }
}
