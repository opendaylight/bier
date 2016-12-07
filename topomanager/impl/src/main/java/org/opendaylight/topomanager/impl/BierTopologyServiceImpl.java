/*
 * Copyright © 2016 www.bupt.edu.cn and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topomanager.impl;

import java.util.concurrent.Future;
import java.util.List;
import java.util.ArrayList;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.BierTopologyApiService;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryLinkInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryLinkOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryLinkOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryNodeInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureDomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureDomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureDomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteSubdomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteSubdomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteSubdomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteDomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteDomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteDomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LoadTopologyInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LoadTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LoadTopologyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteNodeInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryDomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryDomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryDomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureSubdomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureSubdomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureSubdomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureNodeInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureNodeOutputBuilder;

import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.load.topology.output.Topology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.load.topology.output.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.topology.NodeId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.topology.NodeIdBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.topology.LinkId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.topology.LinkIdBuilder;

import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.node.output.Node;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.node.output.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.link.output.Link;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.link.output.LinkBuilder;




import com.google.common.util.concurrent.Futures;

public class BierTopologyServiceImpl implements BierTopologyApiService{

    private static final Logger LOG = LoggerFactory.getLogger(BierTopologyServiceImpl.class);
    
    public DataBroker dataBroker;
    
    public BierTopologyServiceImpl(DataBroker dataBroker){
    	this.dataBroker = dataBroker;
    }
   
    
    public Future<RpcResult<LoadTopologyOutput>> loadTopology(LoadTopologyInput input)
    {
    	if ( null == input )
        {
        	LOG.error("loadTopology rpc input is null!");
            return null;
        }
    	
    	LoadTopologyOutputBuilder builder = new LoadTopologyOutputBuilder();
    	
    	String topologyId = input.getTopologyId();
    	if(topologyId.equals(""))
    	{
    		topologyId = BierTopologyManager.TOPOLOGY_ID;
    	}
    	
    	BierTopology  topo = BierTopologyManager.getTopologyData(dataBroker,topologyId);
    	BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
    	
    	List<Topology> topoList = new ArrayList<Topology>();
    	TopologyBuilder topoBuilder = new TopologyBuilder();
    	topoBuilder.setTopologyId(bierTopoBuilder.getTopologyId());
    	topoList.add(topoBuilder.build());
    	builder.setTopology(topoList);
    	
    	return RpcResultBuilder.success(builder.build()).buildFuture();
    }
    
    
    public Future<RpcResult<QueryTopologyOutput>> queryTopology(QueryTopologyInput input)
    {
    	QueryTopologyOutputBuilder builder = new QueryTopologyOutputBuilder();
    	
    	 if ( null == input )
         {
         	LOG.error("queryTopology rpc input is null!");
             return null;
         }
    	 
    	 String topologyId = input.getTopologyId();
    	 if(null == topologyId || topologyId.equals(""))
    	 {
    		 LOG.error("queryTopology rpc input topologyId is error!");
             return null; 
    	 }

    	
    	BierTopology  topo = BierTopologyManager.getTopologyData(dataBroker,topologyId);
    	BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
    	builder.setTopologyId(bierTopoBuilder.getTopologyId());
    	
    	List<BierNode> bierNodeList = bierTopoBuilder.getBierNode();
    	List<NodeId> nodeList = new ArrayList<NodeId>();
    	int nNode = bierNodeList.size();
    	for(int i = 0; i < nNode; ++i )
    	{
    		BierNode bierNode = bierNodeList.get(i);
    		BierNodeBuilder bierNodeBuilder = new BierNodeBuilder(bierNode);
    		
    		NodeIdBuilder nodeBuilder = new NodeIdBuilder();
    		nodeBuilder.setNodeId(bierNodeBuilder.getNodeId());
    		nodeList.add(nodeBuilder.build());
    	}
    	builder.setNodeId(nodeList);
    	
    	List<BierLink> bierLinkList = bierTopoBuilder.getBierLink();
    	List<LinkId> linkList = new ArrayList<LinkId>();
    	int nLink = bierLinkList.size();
    	for(int i = 0; i < nLink; ++i)
    	{
    		BierLink bierLink = bierLinkList.get(i);
    		BierLinkBuilder bierLinkBuilder = new BierLinkBuilder(bierLink);
    		
    		LinkIdBuilder linkBuilder = new LinkIdBuilder();
    		linkBuilder.setLinkId(bierLinkBuilder.getLinkId());
    		linkList.add(linkBuilder.build());
    	}
    	builder.setLinkId(linkList);
    	
    	return RpcResultBuilder.success(builder.build()).buildFuture();
    }
    
    
    public Future<RpcResult<QueryNodeOutput>> queryNode(QueryNodeInput input)
    {
    	QueryNodeOutputBuilder builder = new QueryNodeOutputBuilder();
    	if ( null == input )
        {
        	LOG.error("queryNode rpc input is null!");
            return null;
        }
   	 
   	    String topologyId = input.getTopologyId();
   	    if(null == topologyId || topologyId.equals(""))
   	    {
   		    LOG.error("queryNode rpc input topologyId is error!");
            return null; 
   	    }
   	    
   	    List<String> nodeIdList = input.getNode();
   	    if(nodeIdList == null || nodeIdList.isEmpty())
   	    {
   	    	LOG.error("queryNode rpc input nodeId is error!");
            return null; 
   	    }
   	    
   	    BierTopology  topo = BierTopologyManager.getTopologyData(dataBroker,topologyId);
 	    BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
 	    
 	    List<Node> nodeList = new ArrayList<Node>();
 	    List<BierNode> bierNodeList = bierTopoBuilder.getBierNode();
   	    int nNode = bierNodeList.size();
   	    for(int i = 0; i < nNode; ++i )
   	    { 
   	        BierNode bierNode = bierNodeList.get(i);
   		    BierNodeBuilder bierNodeBuilder = new BierNodeBuilder(bierNode);
   		    String bierNodeId = bierNodeBuilder.getNodeId();
   		
   		    int nNodeId = nodeIdList.size();
   		    int j = 0;
   		    for(; j < nNodeId; ++j)
   		    {
   			    if(bierNodeId == nodeIdList.get(j))
   			    {
   				    break;
   			    }
   		    }
   		   
   		    if(j < nNodeId)
   		    {
   			    NodeBuilder nodeBuilder = new NodeBuilder(bierNode);
   			    nodeList.add(nodeBuilder.build());
   		    }
   	    }
   	   
   	    builder.setNode(nodeList);
        
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }
    
    
    public Future<RpcResult<QueryLinkOutput>> queryLink(QueryLinkInput input)
    {
    	if ( null == input )
        {
        	LOG.error("queryLink rpc input is null!");
            return null;
        }
   	 
   	    String topologyId = input.getTopologyId();
   	    if(null == topologyId || topologyId.equals(""))
   	    {
   		    LOG.error("queryLink rpc input topologyId is error!");
            return null; 
   	    }
   	    
   	    List<String> linkIdList = input.getLink();
   	    if(linkIdList == null || linkIdList.isEmpty())
   	    {
   	    	LOG.error("queryLink rpc input nodeId is error!");
            return null; 
   	    }
   	    
   	    BierTopology  topo = BierTopologyManager.getTopologyData(dataBroker,topologyId);
 	    BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
    	
    	QueryLinkOutputBuilder builder = new QueryLinkOutputBuilder();
    	
    	List<Link> linkList = new ArrayList<Link>();
 	    List<BierLink> bierLinkList = bierTopoBuilder.getBierLink();
   	    int nLink = bierLinkList.size();
   	    for(int i = 0; i < nLink; ++i )
   	    { 
   	        BierLink bierLink = bierLinkList.get(i);
   		    BierLinkBuilder bierLinkBuilder = new BierLinkBuilder(bierLink);
   		    String bierLinkId = bierLinkBuilder.getLinkId();
   		
   		    int nLinkId = linkIdList.size();
   		    int j = 0;
   		    for(; j < nLinkId; ++j)
   		    {
   			    if(bierLinkId == linkIdList.get(j))
   			    {
   				    break;
   			    }
   		    }
   		   
   		    if(j < nLinkId)
   		    {
   			    LinkBuilder linkBuilder = new LinkBuilder(bierLink);
   			    linkList.add(linkBuilder.build());
   		    }
   	    }
   	   
   	    builder.setLink(linkList);
        
        
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }
    
    
    
    public Future<RpcResult<ConfigureDomainOutput>> configureDomain(ConfigureDomainInput input)
    {
    	ConfigureDomainOutputBuilder builder = new ConfigureDomainOutputBuilder();
        
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }
    
    
    public Future<RpcResult<DeleteSubdomainOutput>> deleteSubdomain(DeleteSubdomainInput input)
    {
    	return null;
    }
    
   
    public Future<RpcResult<QuerySubdomainOutput>> querySubdomain(QuerySubdomainInput input)
    {
    	return null;
    }
    
    
    public Future<RpcResult<DeleteDomainOutput>> deleteDomain(DeleteDomainInput input)
    {
    	return null;
    }
    
    
    
    public Future<RpcResult<DeleteNodeOutput>> deleteNode(DeleteNodeInput input)
    {
    	return null;
    }
    
    
    public Future<RpcResult<QueryDomainOutput>> queryDomain(QueryDomainInput input)
    {
    	return null;
    }
    
    
    public Future<RpcResult<ConfigureSubdomainOutput>> configureSubdomain(ConfigureSubdomainInput input)
    {
    	return null;
    }
    
    
    public Future<RpcResult<ConfigureNodeOutput>> configureNode(ConfigureNodeInput input)
    {
    	return null;
    }
    
}

