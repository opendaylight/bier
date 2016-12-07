/*
 * Copyright © 2016 www.bupt.edu.cn and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topomanager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Destination;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Source;
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
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSource;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDest;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDestBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;


public class BierTopologyAdapter {
	private static final Logger LOG =  LoggerFactory.getLogger(BierTopologyAdapter.class);    // 日志记录
	private final ExecutorService m_cExecutor = Executors.newFixedThreadPool(7);          // 操作执行器
    
	    
	public BierTopologyAdapter() {
	}
	
	public BierTopology getBierTopology(DataBroker dataBroker,String topologyId)
	{
		Topology topo = getInitTopology(dataBroker,topologyId);
		BierTopology bierTopo = null;
		if(topo != null)
		{
		    bierTopo = toBierTopology(topo);
		}
		
		return bierTopo;
	}
	public Topology getInitTopology(DataBroker dataBroker,String topologyId) {
		
		// 创建执行器，用来执行数据
        BierTopologyProcess<Topology> processor =  new BierTopologyProcess<Topology>(dataBroker,
        		BierTopologyProcess.FLAG_READ,(new TopologyBuilder()).build());

        final InstanceIdentifier<Topology> path = InstanceIdentifier.create(NetworkTopology.class).child(Topology.class, new TopologyKey(new TopologyId(topologyId)));
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
            public ListenableFuture<Optional<Topology>> ReadOperation(ReadWriteTransaction transaction) {

                ListenableFuture<Optional<Topology>> listenableFuture = transaction.read(LogicalDatastoreType.OPERATIONAL, path);  // 读取数据

              return listenableFuture;
            }
        });

        // 启动一个执行器执行该操作
        Future<ListenableFuture<Topology>> future = m_cExecutor.submit(processor);
        
        try {
            ListenableFuture<Topology> result = future.get();         // 获取执行后的结果
            Topology topology = result.get();                   
            if ( null == topology || null == topology.getTopologyId())
            {
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

    public BierTopology toBierTopology(Topology topo)
    {
    	BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder();
    	TopologyBuilder topoBuilder = new TopologyBuilder(topo);
    	bierTopoBuilder.setTopologyId(topoBuilder.getTopologyId().toString());
    	BierTopologyKey bierTopoKey = new BierTopologyKey(topoBuilder.getTopologyId().toString());
    	bierTopoBuilder.setKey(bierTopoKey);
    	
    	List<BierNode> bierNodeList = new ArrayList<BierNode>();
    	List<Node> nodeList = topoBuilder.getNode();
    	int nNode = nodeList.size();
    	for(int i = 0; i < nNode; ++i)
    	{
    		Node node = nodeList.get(i);
    		BierNode bierNode = toBierNode(node);
    		bierNodeList.add(bierNode);
    	}
    	bierTopoBuilder.setBierNode(bierNodeList);
    	
    	List<BierLink> bierLinkList = new ArrayList<BierLink>();
    	List<Link> linkList = topoBuilder.getLink();
    	int nLink = linkList.size();
    	for(int i = 0; i < nLink; ++i)
    	{
    		Link link = linkList.get(i);
    		BierLink bierLink = toBierLink(link);
    		bierLinkList.add(bierLink);
    	}
    	bierTopoBuilder.setBierLink(bierLinkList);
    	
    	
    	return bierTopoBuilder.build();
    }
    
    public BierNode toBierNode(Node node)
    {
    	NodeBuilder nodeBuilder = new NodeBuilder(node);
		
		BierNodeBuilder bierNodeBuilder = new BierNodeBuilder();
		bierNodeBuilder.setNodeId(nodeBuilder.getNodeId().toString());
		BierNodeKey bierNodeKey = new BierNodeKey(nodeBuilder.getNodeId().toString());
		bierNodeBuilder.setKey(bierNodeKey);
		
		List<BierTerminationPoint> bierTpList = new ArrayList<BierTerminationPoint>();
		List<TerminationPoint> tpList = nodeBuilder.getTerminationPoint();
		int nTp = tpList.size();
		for(int i = 0; i < nTp; ++i)
		{
			TerminationPoint tp = tpList.get(i);
			BierTerminationPoint bierTp = toBierTp(tp);
			
			bierTpList.add(bierTp);
		}
		bierNodeBuilder.setBierTerminationPoint(bierTpList);
		
		return bierNodeBuilder.build();
    }
    
    public BierTerminationPoint toBierTp(TerminationPoint tp)
    {
    	TerminationPointBuilder tpBuilder = new TerminationPointBuilder(tp);
		
		BierTerminationPointBuilder bierTpBuilder = new BierTerminationPointBuilder();
		bierTpBuilder.setTpId(tpBuilder.getTpId().toString());
		BierTerminationPointKey bierTpKey = new BierTerminationPointKey(tpBuilder.getTpId().toString());
		bierTpBuilder.setKey(bierTpKey);
		
		return bierTpBuilder.build();
    }
    
    public BierLink toBierLink(Link link)
    {
    	LinkBuilder linkBuilder = new LinkBuilder(link);
		
		BierLinkBuilder bierLinkBuilder = new BierLinkBuilder();
		bierLinkBuilder.setLinkId(linkBuilder.getLinkId().toString());
		BierLinkKey bierLinkKey = new BierLinkKey(linkBuilder.getLinkId().toString());
		bierLinkBuilder.setKey(bierLinkKey);
		
		Source source = linkBuilder.getSource();
		SourceBuilder sourceBuilder = new SourceBuilder(source);
		LinkSourceBuilder bierSource = new LinkSourceBuilder();
		bierSource.setSourceNode(sourceBuilder.getSourceNode().toString());
		bierSource.setSourceTp(sourceBuilder.getSourceTp().toString());
		bierLinkBuilder.setLinkSource(bierSource.build());
		
		Destination dest = linkBuilder.getDestination();
		DestinationBuilder destBuilder = new DestinationBuilder(dest);
		LinkDestBuilder bierDest = new LinkDestBuilder();
		bierDest.setDestNode(destBuilder.getDestNode().toString());
		bierDest.setDestTp(destBuilder.getDestTp().toString());
		bierLinkBuilder.setLinkDest(bierDest.build());
		
		return bierLinkBuilder.build();
    }
    
    
    
    
}