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
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTopologyManager {
    private static final Logger LOG =  LoggerFactory.getLogger(BierTopologyManager.class);    // 日志记录
    private BierTopologyProvider bierTopologyProvider;
    private final DataBroker dataBroker;
    private BierTopologyAdapter topoAdapter = new BierTopologyAdapter();
    public static final String TOPOLOGY_ID = "flow:1";
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(7);          // 操作执行器

    public BierTopologyManager(BierTopologyProvider bierTopologyProvider) {
        this.bierTopologyProvider = bierTopologyProvider;
        dataBroker = bierTopologyProvider.getDataBroker();
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
            tx.put(LogicalDatastoreType.OPERATIONAL, path, new BierTopologyBuilder().setKey(key).build(), true);
            try {
                tx.submit().get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Initial topology export failed, continuing anyway", e);
            }
        }

        BierTopology topo = topoAdapter.getBierTopology(dataBroker,TOPOLOGY_ID);

        //写bier拓扑的datastore
        setTopologyData(topo);
    }

    private boolean isBierTopologyExist(final InstanceIdentifier<BierTopology> path) {
        final ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        try {
            Optional<BierTopology> bierTopology = tx.read(LogicalDatastoreType.OPERATIONAL, path).checkedGet();
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
        // 参数检查
        if ( null == dataBroker || null == bierTopology ) {
            LOG.error("ZTE:Set Bier Topology input is error!");
            return;
        }

        // 创建执行器，用来执行数据
        BierTopologyProcess<BierTopology> processor =  new BierTopologyProcess<BierTopology>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierTopologyBuilder()).build());

        final InstanceIdentifier<BierTopology> path = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, bierTopology.getKey());
        // 构造一个从数据区读取的操作类
        processor.enqueueOperation(new BierTopologyOperation() {
            // 重载空的写操作函数
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                transaction.put(LogicalDatastoreType.OPERATIONAL,path,bierTopology ,true);
            }

            // 重载读操作函数
            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<BierTopology>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<BierTopology>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierTopology> result = future.get();         // 获取执行后的结果
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

        // 数据错误
        LOG.error("ZTE:Set Bier Topology failed!");
        return;
    }

    public static BierTopology getTopologyData(DataBroker dataBroker, String topologyId) {
        // 创建执行器，用来执行数据
        BierTopologyProcess<BierTopology> processor =  new BierTopologyProcess<BierTopology>(dataBroker,
                BierTopologyProcess.FLAG_READ,(new BierTopologyBuilder()).build());

        final InstanceIdentifier<BierTopology> path = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId));
        // 构造一个从数据区读取的操作类
        processor.enqueueOperation(new BierTopologyOperation() {
            // 重载空的写操作函数
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
              // Auto-generated method stub
            }

            // 重载读操作函数
            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<BierTopology>> readOperation(ReadWriteTransaction transaction) {

                ListenableFuture<Optional<BierTopology>> listenableFuture =
                        transaction.read(LogicalDatastoreType.OPERATIONAL, path);  // 读取数据

                return listenableFuture;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<BierTopology>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierTopology> result = future.get();
            // 获取执行后的结果
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

    public static boolean setDomainData(final DataBroker dataBroker,String topologyId,
            final List<BierDomain> domainList) {
        // 参数检查
        if ( null == dataBroker || null == domainList || domainList.isEmpty() ) {
            LOG.error("ZTE:Set bier domain input is error!");
            return false;
        }

        // 创建执行器，用来执行数据
        BierTopologyProcess<BierDomain> processor =  new BierTopologyProcess<BierDomain>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierDomainBuilder()).build());

        final InstanceIdentifier<BierTopology> topoPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId));

        // 构造一个从数据区读取的操作类
        processor.enqueueOperation(new BierTopologyOperation() {
            // 重载空的写操作函数
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                int domainSize = domainList.size();
                for (int iloop = 0; iloop < domainSize; ++iloop) {
                    BierDomain domain = domainList.get(iloop);
                    final InstanceIdentifier<BierDomain> path = topoPath.child(BierDomain.class,domain.getKey());
                    transaction.put(LogicalDatastoreType.OPERATIONAL,path, domain,true);
                }
            }

            // 重载读操作函数
            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<Node>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<BierDomain>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierDomain> result = future.get();         // 获取执行后的结果
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

        // 数据错误
        LOG.error("ZTE:Set bier domain failed!");
        return false;
    }

    public static BierDomain getDomainData(DataBroker dataBroker, String topologyId,DomainId domainId) {
        // 创建执行器，用来执行数据
        BierTopologyProcess<BierDomain> processor =  new BierTopologyProcess<BierDomain>(dataBroker,
                BierTopologyProcess.FLAG_READ,(new BierDomainBuilder()).build());

        final InstanceIdentifier<BierTopology> topoPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId));
        final InstanceIdentifier<BierDomain> path = topoPath.child(BierDomain.class, new BierDomainKey(domainId));
        // 构造一个从数据区读取的操作P
        processor.enqueueOperation(new BierTopologyOperation() {
            // 重载空的写操作函数
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
              // Auto-generated method stub
            }

            // 重载读操作函数
            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<BierDomain>> readOperation(ReadWriteTransaction transaction) {

                ListenableFuture<Optional<BierDomain>> listenableFuture =
                        transaction.read(LogicalDatastoreType.OPERATIONAL, path);  // 读取数据

                return listenableFuture;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<BierDomain>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierDomain> result = future.get();         // 获取执行后的结果
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

    public static boolean setSubDomainData(final DataBroker dataBroker,String topologyId,
            DomainId domainId,final List<BierSubDomain> subDomainList) {
        // 参数检查
        if ( null == dataBroker || null == subDomainList || subDomainList.isEmpty() ) {
            LOG.error("ZTE:Set bier sub-domai input is error!");
            return false;
        }

        // 创建执行器，用来执行数据
        BierTopologyProcess<BierSubDomain> processor =  new BierTopologyProcess<BierSubDomain>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierSubDomainBuilder()).build());

        InstanceIdentifier<BierTopology> topoPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId));
        final InstanceIdentifier<BierDomain> domainPath = topoPath.child(BierDomain.class, new BierDomainKey(domainId));

        // 构造一个从数据区读取的操作类
        processor.enqueueOperation(new BierTopologyOperation() {
            // 重载空的写操作函数
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                int subDomainSize = subDomainList.size();
                for (int iloop = 0; iloop < subDomainSize; ++iloop) {
                    BierSubDomain subDomain = subDomainList.get(iloop);
                    final InstanceIdentifier<BierSubDomain> path
                            = domainPath.child(BierSubDomain.class, subDomain.getKey());
                    transaction.put(LogicalDatastoreType.OPERATIONAL,path, subDomain,true);
                }
            }

            // 重载读操作函数
            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<Node>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<BierSubDomain>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierSubDomain> result = future.get();         // 获取执行后的结果
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

        // 数据错误
        LOG.error("ZTE:Set bier sub-domain failed!");
        return false;
    }

    public static boolean delDomainData(final DataBroker dataBroker,final String topologyId,
            final DomainId domainId,final List<BierNode> nodeList) {
        // 参数检查
        if ( null == dataBroker || null == domainId ) {
            LOG.error("ZTE:Del bier domain input is error!");
            return false;
        }

        // 创建执行器，用来执行数据
        BierTopologyProcess<BierDomain> processor =  new BierTopologyProcess<BierDomain>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierDomainBuilder()).build());
        final InstanceIdentifier<BierTopology> topoPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId));
        final InstanceIdentifier<BierDomain> path = topoPath.child(BierDomain.class, new BierDomainKey(domainId));

        // 构造一个从数据区读取的操作类
        processor.enqueueOperation(new BierTopologyOperation() {
            // 重载空的写操作函数
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                transaction.delete(LogicalDatastoreType.OPERATIONAL,path);

                //修改影响的节点
                updateAffectedDomainNode(transaction,topologyId,domainId,nodeList);
            }

            // 重载读操作函数
            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<Node>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<BierDomain>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierDomain> result = future.get();         // 获取执行后的结果
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

        // 数据错误
        LOG.error("ZTE:Del bier domain failed!");
        return false;
    }

    public static void updateAffectedDomainNode(ReadWriteTransaction transaction,String topologyId,
            DomainId domainId,List<BierNode> nodeList) {
        List<BierNode> domainNodeList = new ArrayList<BierNode>();
        int nodeSize = nodeList.size();
        for (int iloop = 0; iloop < nodeSize; ++iloop) {
            BierNode node = nodeList.get(iloop);
            List<Domain> domainList =  node.getBierNodeParams().getDomain();
            int domainSize = domainList.size();
            for (int jloop = 0; jloop < domainSize; ++jloop) {
                Domain domain = domainList.get(jloop);
                if (domain.getDomainId().equals(domainId)) {
                    BierNodeBuilder newNodeBuilder = new BierNodeBuilder(node);
                    List<Domain> newDomainList = newNodeBuilder.getBierNodeParams().getDomain();
                    newDomainList.remove(jloop);
                    domainNodeList.add(newNodeBuilder.build());
                    break;
                }
            }
        }

        InstanceIdentifier<BierTopology> topoPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId));

        int domainNodeSize = domainNodeList.size();
        for (int iloop = 0; iloop < domainNodeSize; ++iloop) {
            BierNode domainNode = domainNodeList.get(iloop);
            final InstanceIdentifier<BierNode> path = topoPath.child(BierNode.class, domainNode.getKey());
            //transaction.delete(LogicalDatastoreType.OPERATIONAL,path);
            transaction.put(LogicalDatastoreType.OPERATIONAL,path,domainNode);
        }
    }

    public static boolean delSubDomainData(final DataBroker dataBroker,final String topologyId,final DomainId domainId,
            final SubDomainId subDomainId,final List<BierNode> nodeList) {
        // 参数检查
        if ( null == dataBroker || null == domainId ) {
            LOG.error("ZTE:Del bier sub-domain input is error!");
            return false;
        }

        // 创建执行器，用来执行数据
        BierTopologyProcess<BierSubDomain> processor =  new BierTopologyProcess<BierSubDomain>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierSubDomainBuilder()).build());
        final InstanceIdentifier<BierTopology> topoPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId));
        final InstanceIdentifier<BierDomain> domainPath = topoPath.child(BierDomain.class, new BierDomainKey(domainId));
        final InstanceIdentifier<BierSubDomain> path = domainPath.child(BierSubDomain.class,
                new BierSubDomainKey(subDomainId));
        // 构造一个从数据区读取的操作类
        processor.enqueueOperation(new BierTopologyOperation() {
            // 重载空的写操作函数
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                transaction.delete(LogicalDatastoreType.OPERATIONAL,path);

                //修改影响的节点
                updateAffectedSubDomainNode(transaction,topologyId,domainId,subDomainId,nodeList);
            }

            // 重载读操作函数
            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<Node>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<BierSubDomain>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierSubDomain> result = future.get();         // 获取执行后的结果
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

        // 数据错误
        LOG.error("ZTE:Del Optical Node failed!");
        return false;
    }

    public static void updateAffectedSubDomainNode(ReadWriteTransaction transaction,String topologyId,
            DomainId domainId,SubDomainId subDomainId,List<BierNode> nodeList) {
        if (nodeList == null) {
            return;
        }
        List<BierNode> subDomainNodeList = new ArrayList<BierNode>();
        int nodeSize = nodeList.size();
        for (int iloop = 0; iloop < nodeSize; ++iloop) {
            BierNode node = nodeList.get(iloop);
            List<Domain> domainList =  node.getBierNodeParams().getDomain();
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
                    if (subDomain.getSubDomainId().equals(subDomainId)) {
                        BierNodeBuilder newNodeBuilder = new BierNodeBuilder(node);
                        List<SubDomain> newSubDomainList = newNodeBuilder.getBierNodeParams()
                                .getDomain().get(jloop).getBierGlobal().getSubDomain();
                        newSubDomainList.remove(kloop);

                        if (newSubDomainList.isEmpty()) {
                            List<Domain> newDomainList = newNodeBuilder.getBierNodeParams()
                                    .getDomain();
                            newDomainList.remove(jloop);
                        }
                        subDomainNodeList.add(newNodeBuilder.build());
                        break;
                    }
                }
            }
        }

        InstanceIdentifier<BierTopology> topoPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId));

        int subDomainNodeSize = subDomainNodeList.size();
        for (int iloop = 0; iloop < subDomainNodeSize; ++iloop) {
            BierNode subDomainNode = subDomainNodeList.get(iloop);
            final InstanceIdentifier<BierNode> path = topoPath.child(BierNode.class, subDomainNode.getKey());
            transaction.put(LogicalDatastoreType.OPERATIONAL,path,subDomainNode);
        }
    }

    public static boolean setNodeData(final DataBroker dataBroker,String topologyId, final BierNode node) {
        // 参数检查
        if ( null == dataBroker || node == null ) {
            LOG.error("ZTE:Set bier node input is error!");
            return false;
        }

        // 创建执行器，用来执行数据
        BierTopologyProcess<BierNode> processor =  new BierTopologyProcess<BierNode>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierNodeBuilder()).build());

        final InstanceIdentifier<BierTopology> topoPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId));
        final InstanceIdentifier<BierNode> path = topoPath.child(BierNode.class, node.getKey());

        // 构造一个从数据区读取的操作类
        processor.enqueueOperation(new BierTopologyOperation() {
            // 重载空的写操作函数
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                transaction.merge(LogicalDatastoreType.OPERATIONAL,path, node,true);
            }

            // 重载读操作函数
            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<Node>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<BierNode>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierNode> result = future.get();         // 获取执行后的结果
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

        // 数据错误
        LOG.error("ZTE:Set bier domain failed!");
        return false;
    }

    public static BierNode getNodeData(DataBroker dataBroker, String topologyId,String nodeId) {
        // 创建执行器，用来执行数据
        BierTopologyProcess<BierNode> processor =  new BierTopologyProcess<BierNode>(dataBroker,
                BierTopologyProcess.FLAG_READ,(new BierNodeBuilder()).build());

        final InstanceIdentifier<BierTopology> topoPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId));
        final InstanceIdentifier<BierNode> path = topoPath.child(BierNode.class, new BierNodeKey(nodeId));
        // 构造一个从数据区读取的操作P
        processor.enqueueOperation(new BierTopologyOperation() {
            // 重载空的写操作函数
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
              // Auto-generated method stub
            }

            // 重载读操作函数
            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<BierNode>> readOperation(ReadWriteTransaction transaction) {
                ListenableFuture<Optional<BierNode>> listenableFuture =
                        transaction.read(LogicalDatastoreType.OPERATIONAL, path);// 读取数据
                return listenableFuture;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<BierNode>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierNode> result = future.get();         // 获取执行后的结果
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

    public static boolean delNodeFromDomain(DataBroker dataBroker, String topologyId,final DomainId domainId,
            final SubDomainId subDomainId,final BierNode node) {
        // 创建执行器，用来执行数据
        BierTopologyProcess<BierNode> processor =  new BierTopologyProcess<BierNode>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierNodeBuilder()).build());

        final InstanceIdentifier<BierTopology> topoPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId));
        final InstanceIdentifier<BierNode> path = topoPath.child(BierNode.class, new BierNodeKey(node.getKey()));
        // 构造一个从数据区读取的操作P
        processor.enqueueOperation(new BierTopologyOperation() {
            // 重载空的写操作函数
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
                BierNodeBuilder newNodeBuilder = new BierNodeBuilder(node);
                List<Domain> domainList =  node.getBierNodeParams().getDomain();
                int domainSize = domainList.size();
                for (int jloop = 0; jloop < domainSize; ++jloop) {
                    Domain domain = domainList.get(jloop);
                    List<SubDomain> subDomainList = domain.getBierGlobal().getSubDomain();
                    int subDomainSize = subDomainList.size();
                    for (int kloop = 0; kloop < subDomainSize; ++kloop) {
                        SubDomain subDomain = subDomainList.get(kloop);
                        if (subDomain.getSubDomainId().equals(subDomainId)) {
                            List<SubDomain> newSubDomainList = newNodeBuilder.getBierNodeParams().getDomain()
                                    .get(jloop).getBierGlobal().getSubDomain();
                            newSubDomainList.remove(kloop);

                            if (newSubDomainList.isEmpty()) {
                                List<Domain> newDomainList = newNodeBuilder.getBierNodeParams().getDomain();
                                newDomainList.remove(jloop);
                            }
                            break;
                        }
                    }
                }

                transaction.put(LogicalDatastoreType.OPERATIONAL,path,newNodeBuilder.build());
                // Auto-generated method stub
            }

            // 重载读操作函数
            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<BierNode>> readOperation(ReadWriteTransaction transaction) {
                return null;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<BierNode>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierNode> result = future.get();         // 获取执行后的结果
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

        // 数据错误
        LOG.error("ZTE:Del bier Node failed!");
        return false;
    }

    public static BierLink getLinkData(DataBroker dataBroker, String topologyId,String linkId) {
        // 创建执行器，用来执行数据
        BierTopologyProcess<BierLink> processor =  new BierTopologyProcess<BierLink>(dataBroker,
                BierTopologyProcess.FLAG_READ,(new BierLinkBuilder()).build());

        final InstanceIdentifier<BierTopology> topoPath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topologyId));
        final InstanceIdentifier<BierLink> path = topoPath.child(BierLink.class, new BierLinkKey(linkId));
        // 构造一个从数据区读取的操作P
        processor.enqueueOperation(new BierTopologyOperation() {
            // 重载空的写操作函数
            @Override
            public void writeOperation(ReadWriteTransaction transaction) {
              // Auto-generated method stub
            }

            // 重载读操作函数
            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<Optional<BierLink>> readOperation(ReadWriteTransaction transaction) {
                ListenableFuture<Optional<BierLink>> listenableFuture = transaction
                        .read(LogicalDatastoreType.OPERATIONAL, path);  // 读取数据

                return listenableFuture;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<BierLink>> future = EXECUTOR.submit(processor);

        try {
            ListenableFuture<BierLink> result = future.get();         // 获取执行后的结果
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

    public static List<BierNode> getSubDomainNode(DataBroker dataBroker,String topologyId,DomainId domainId,
            SubDomainId subDomainId) {
        List<BierNode> nodeList = new ArrayList<BierNode>();
        BierTopology  topo = BierTopologyManager.getTopologyData(dataBroker,topologyId);
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

    public static boolean isNodeBelongToSubDomain(DomainId domainId,SubDomainId subDomainId,BierNode node) {
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

    public static List<BierLink> getSubDomainLink(DataBroker dataBroker,String topologyId,
            DomainId domainId,SubDomainId subDomainId) {
        List<BierLink> linkList = new ArrayList<BierLink>();
        BierTopology  topo = BierTopologyManager.getTopologyData(dataBroker,topologyId);
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

    public static boolean checkDomainExist(DataBroker dataBroker,String topologyId,List<Domain> domainList) {
        if (domainList == null || domainList.isEmpty()) {
            return false;
        }

        Domain domain = domainList.get(0);
        DomainId domainId  = domain.getDomainId();
        BierDomain bierDomain = getDomainData(dataBroker, topologyId,domainId);
        if (bierDomain == null) {
            return false;
        }

        DomainBuilder domainBuilder = new DomainBuilder(domain);
        List<SubDomain> subDomainList = domainBuilder.getBierGlobal().getSubDomain();
        if (subDomainList == null || subDomainList.isEmpty()) {
            return false;
        }
        boolean subDomainExistFlag = false;
        SubDomainId subDomainId  = subDomainList.get(0).getSubDomainId();
        List<BierSubDomain> bierSubDomainList = bierDomain.getBierSubDomain();
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

    public static boolean checkDomainExist(DataBroker dataBroker,String topologyId,DomainId domainId,
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

    public static boolean checkSubDomainExist(DataBroker dataBroker,String topologyId,
            DomainId domainId,SubDomainId subDomainId,List<BierDomain> domainList) {
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
}
