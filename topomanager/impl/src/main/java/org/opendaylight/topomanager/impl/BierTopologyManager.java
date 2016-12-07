/*
 * Copyright © 2016 www.bupt.edu.cn and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topomanager.impl;


import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.BindingService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;



import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;


public class BierTopologyManager {
	private static final Logger LOG =  LoggerFactory.getLogger(BierTopologyManager.class);    // 日志记录
	private BierTopologyProvider bierTopologyProvider;
	private final DataBroker dataBroker;
    private BierTopologyAdapter topoAdapter = new BierTopologyAdapter();
    
    
    public static final String TOPOLOGY_ID = "flow:1";
    private static final ExecutorService m_cExecutor = Executors.newFixedThreadPool(7);          // 操作执行器

	public BierTopologyManager(BierTopologyProvider bierTopologyProvider) {
		this.bierTopologyProvider = bierTopologyProvider;
		dataBroker = bierTopologyProvider.getDataBroker();
	}
	
	public DataBroker getDataBroker(){
		return dataBroker;
	}
    
    
    public void start(){
    	final BierTopologyKey key = new BierTopologyKey(TOPOLOGY_ID);
        final InstanceIdentifier<BierTopology> path = InstanceIdentifier
                .create(BierNetworkTopology.class)
                .child(BierTopology.class, key);
        
        if(!isBierTopologyExist(path)){
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
        
        //监听openflow topo datastore
        
        //启动websocket服务
    }
    
    private boolean isBierTopologyExist(final InstanceIdentifier<BierTopology> path) {
        final ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        try {
            Optional<BierTopology> bierTopology = tx.read(LogicalDatastoreType.OPERATIONAL, path).checkedGet();
            LOG.debug("Bier topology exist in the operational data store at {}",path);
            if(bierTopology.isPresent()){
                return true;
            }
        } catch (ReadFailedException e) {
            LOG.warn("Bier topology read operation failed!", e);
        }
        return false;
    }
    
    
    public void setTopologyData(final BierTopology bierTopology)
    {
        // 参数检查
        if ( null == dataBroker || null == bierTopology ) {
        	 LOG.error("ZTE:Set Bier Topology input is error!");
        	 return;
        }
        
        // 创建执行器，用来执行数据
        BierTopologyProcess<BierTopology> processor =  new BierTopologyProcess<BierTopology>(dataBroker,
        		BierTopologyProcess.FLAG_WRITE,(new BierTopologyBuilder()).build());

        final InstanceIdentifier<BierTopology> path = InstanceIdentifier.create(BierNetworkTopology.class).child(BierTopology.class, bierTopology.getKey());
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
            public ListenableFuture<Optional<BierTopology>> ReadOperation(ReadWriteTransaction transaction) {
              return null;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<BierTopology>> future = m_cExecutor.submit(processor);

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
    
    public static BierTopology getTopologyData(DataBroker dataBroker, String topologyId)
    {
        // 创建执行器，用来执行数据
        BierTopologyProcess<BierTopology> processor =  new BierTopologyProcess<BierTopology>(dataBroker,
        		BierTopologyProcess.FLAG_READ,(new BierTopologyBuilder()).build());

        final InstanceIdentifier<BierTopology> path = InstanceIdentifier.create(BierNetworkTopology.class).child(BierTopology.class, new BierTopologyKey(topologyId));
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
            public ListenableFuture<Optional<BierTopology>> ReadOperation(ReadWriteTransaction transaction) {

                ListenableFuture<Optional<BierTopology>> listenableFuture = transaction.read(LogicalDatastoreType.OPERATIONAL, path);  // 读取数据

              return listenableFuture;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<BierTopology>> future = m_cExecutor.submit(processor);
        
        try {
            ListenableFuture<BierTopology> result = future.get();         // 获取执行后的结果
            BierTopology topology = result.get();                   
            if ( null == topology || null == topology.getTopologyId())
            {
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
    
    
    
}
