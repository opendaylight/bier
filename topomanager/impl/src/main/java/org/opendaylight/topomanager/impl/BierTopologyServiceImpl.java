/*
 * Copyright © 2016 www.bupt.edu.cn and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topomanager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResultBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.BierTopologyApiService;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureDomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureDomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureDomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureNodeInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureSubdomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureSubdomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureSubdomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteDomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteDomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteDomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteNodeInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteSubdomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteSubdomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteSubdomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LoadTopologyInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LoadTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LoadTopologyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryDomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryDomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryDomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryLinkInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryLinkOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryLinkOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryNodeInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainLinkInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainLinkOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainLinkOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainNodeInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.load.topology.output.Topology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.load.topology.output.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.domain.output.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.domain.output.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.link.output.Link;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.link.output.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.node.output.Node;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.node.output.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.subdomain.link.output.SubdomainLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.subdomain.link.output.SubdomainLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.subdomain.node.output.SubdomainNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.subdomain.node.output.SubdomainNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.subdomain.output.Subdomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.subdomain.output.SubdomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.topology.LinkId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.topology.LinkIdBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.topology.NodeId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.topology.NodeIdBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.bier.domain.BierSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.bier.domain.BierSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.bier.domain.BierSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTopologyServiceImpl implements BierTopologyApiService {
    private static final Logger LOG = LoggerFactory.getLogger(BierTopologyServiceImpl.class);
    public DataBroker dataBroker;

    public BierTopologyServiceImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public Future<RpcResult<LoadTopologyOutput>> loadTopology(LoadTopologyInput input) {
        if (null == input ) {
            return returnRpcErr("input is null!");
        }

        String topologyId = input.getTopologyId();
        if (topologyId.equals("")) {
            topologyId = BierTopologyManager.TOPOLOGY_ID;
        }

        BierTopology  topo = BierTopologyManager.getTopologyData(dataBroker,topologyId);
        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);

        List<Topology> topoList = new ArrayList<Topology>();
        TopologyBuilder topoBuilder = new TopologyBuilder();
        topoBuilder.setTopologyId(bierTopoBuilder.getTopologyId());
        topoList.add(topoBuilder.build());
        LoadTopologyOutputBuilder builder = new LoadTopologyOutputBuilder();
        builder.setTopology(topoList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<QueryTopologyOutput>> queryTopology(QueryTopologyInput input) {
        QueryTopologyOutputBuilder builder = new QueryTopologyOutputBuilder();
        if (null == input ) {
            return returnRpcErr("input is null!");
        }

        String topologyId = input.getTopologyId();
        if (null == topologyId || topologyId.equals("")) {
            return returnRpcErr("input param is error!");
        }

        BierTopology  topo = BierTopologyManager.getTopologyData(dataBroker,topologyId);
        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
        builder.setTopologyId(bierTopoBuilder.getTopologyId());
        List<BierNode> bierNodeList = bierTopoBuilder.getBierNode();
        List<NodeId> nodeList = new ArrayList<NodeId>();
        if (bierNodeList != null) {
            int nodeSize = bierNodeList.size();
            for (int loopi = 0; loopi < nodeSize; ++loopi ) {
                BierNode bierNode = bierNodeList.get(loopi);
                BierNodeBuilder bierNodeBuilder = new BierNodeBuilder(bierNode);
                NodeIdBuilder nodeBuilder = new NodeIdBuilder();
                nodeBuilder.setNodeId(bierNodeBuilder.getNodeId());
                nodeList.add(nodeBuilder.build());
            }
        }
        builder.setNodeId(nodeList);

        List<BierLink> bierLinkList = bierTopoBuilder.getBierLink();
        List<LinkId> linkList = new ArrayList<LinkId>();
        if (bierLinkList != null) {
            int linkSize = bierLinkList.size();
            for (int loopi = 0; loopi < linkSize; ++loopi) {
                BierLink bierLink = bierLinkList.get(loopi);
                BierLinkBuilder bierLinkBuilder = new BierLinkBuilder(bierLink);

                LinkIdBuilder linkBuilder = new LinkIdBuilder();
                linkBuilder.setLinkId(bierLinkBuilder.getLinkId());
                linkList.add(linkBuilder.build());
            }
        }
        builder.setLinkId(linkList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<QueryNodeOutput>> queryNode(QueryNodeInput input) {
        if ( null == input ) {
            return returnRpcErr("input is null!");
        }
        String topologyId = input.getTopologyId();
        List<String> nodeIdList = input.getNode();
        if (null == topologyId || topologyId.equals("") || nodeIdList == null || nodeIdList.isEmpty()) {
            return returnRpcErr("input param is error!");
        }

        BierTopology  topo = BierTopologyManager.getTopologyData(dataBroker,topologyId);
        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
        List<Node> nodeList = new ArrayList<Node>();
        List<BierNode> bierNodeList = bierTopoBuilder.getBierNode();
        if (bierNodeList != null) {
            int nodeSize = bierNodeList.size();
            for (int loopi = 0; loopi < nodeSize; ++loopi ) {
                BierNode bierNode = bierNodeList.get(loopi);
                BierNodeBuilder bierNodeBuilder = new BierNodeBuilder(bierNode);
                String bierNodeId = bierNodeBuilder.getNodeId();

                int nodeIdSize = nodeIdList.size();
                int loopj = 0;
                for (; loopj < nodeIdSize; ++loopj) {
                    if (bierNodeId.equals(nodeIdList.get(loopj))) {
                        break;
                    }
                }
                if (loopj < nodeIdSize) {
                    NodeBuilder nodeBuilder = new NodeBuilder(bierNode);
                    nodeList.add(nodeBuilder.build());
                }
            }
        }
        QueryNodeOutputBuilder builder = new QueryNodeOutputBuilder();
        builder.setNode(nodeList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<QueryLinkOutput>> queryLink(QueryLinkInput input) {
        if (null == input ) {
            return returnRpcErr("input is null!");
        }
        String topologyId = input.getTopologyId();
        List<String> linkIdList = input.getLink();
        if (null == topologyId || topologyId.equals("") || linkIdList == null || linkIdList.isEmpty()) {
            return returnRpcErr("input param is error!");
        }

        BierTopology  topo = BierTopologyManager.getTopologyData(dataBroker,topologyId);
        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
        QueryLinkOutputBuilder builder = new QueryLinkOutputBuilder();
        List<Link> linkList = new ArrayList<Link>();
        List<BierLink> bierLinkList = bierTopoBuilder.getBierLink();
        if (bierLinkList != null) {
            int linkSize = bierLinkList.size();
            for (int loopi = 0; loopi < linkSize; ++loopi ) {
                BierLink bierLink = bierLinkList.get(loopi);
                BierLinkBuilder bierLinkBuilder = new BierLinkBuilder(bierLink);
                String bierLinkId = bierLinkBuilder.getLinkId();
                int linkIdSize = linkIdList.size();
                int loopj = 0;
                for (; loopj < linkIdSize; ++loopj) {
                    if (bierLinkId.equals(linkIdList.get(loopj))) {
                        break;
                    }
                }

                if (loopj < linkIdSize) {
                    LinkBuilder linkBuilder = new LinkBuilder(bierLink);
                    linkList.add(linkBuilder.build());
                }
            }
        }

        builder.setLink(linkList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<ConfigureDomainOutput>> configureDomain(ConfigureDomainInput input) {
        ConfigureDomainOutputBuilder builder = new ConfigureDomainOutputBuilder();

        if ( null == input ) {
            builder.setConfigureResult(getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        String topologyId = input.getTopologyId();
        List<DomainId> domainList = input.getDomain();
        if (topologyId == null || topologyId.equals("") || domainList == null || domainList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        List<BierDomain> addDomainList = new ArrayList<BierDomain>();
        int domainSize = domainList.size();
        for (int iloop = 0; iloop < domainSize; ++iloop) {
            DomainId domainId = domainList.get(iloop);
            boolean existFlag = false;
            /*if (bierDomainList != null) {
                int nBierDomain = bierDomainList.size();
                for(int j = 0; j < nBierDomain; ++j )
                {
                    BierDomain bierDomain = bierDomainList.get(j);
                    BierDomainBuilder bierDomainBuilder = new BierDomainBuilder(bierDomain);
                    if(domainId == bierDomainBuilder.getDomainId())
                    {
                        bExist = true;
                        break;
                    }
                }
            }*/
            if (!existFlag) {
                BierDomainBuilder bierDomainBuilder = new BierDomainBuilder();
                bierDomainBuilder.setDomainId(domainId);
                BierDomainKey domainKey = new BierDomainKey(domainId);
                bierDomainBuilder.setKey(domainKey);
                addDomainList.add(bierDomainBuilder.build());
            }
        }

        if (!BierTopologyManager.setDomainData(dataBroker,topologyId,addDomainList)) {
            builder.setConfigureResult(getConfigResult(false,"write domain to datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<ConfigureSubdomainOutput>> configureSubdomain(ConfigureSubdomainInput input) {
        ConfigureSubdomainOutputBuilder builder = new ConfigureSubdomainOutputBuilder();
        if ( null == input ) {
            builder.setConfigureResult(getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        String topologyId = input.getTopologyId();
        DomainId domainId = input.getDomainId();
        List<SubDomainId> subDomainIdList = input.getSubDomain();

        if (topologyId == null || topologyId.equals("") || subDomainIdList == null || subDomainIdList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        BierDomain domain = BierTopologyManager.getDomainData(dataBroker,topologyId,domainId);
        if (domain == null ) {
            builder.setConfigureResult(getConfigResult(false,"domain is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        //不需要比较，APP只下发新增的，不是全量下发
        List<BierSubDomain> addSubDomainList = new ArrayList<BierSubDomain>();
        int subDomainSize = subDomainIdList.size();
        for (int iloop = 0; iloop < subDomainSize; ++iloop) {
            SubDomainId subDomainId = subDomainIdList.get(iloop);
            boolean existFlag = false;
            /*int nExistSubDomain = subDomainList.size();
            for (int j = 0; j < nExistSubDomain; ++j) {
                if (subDomainList.get(i).getSubDomainId() == subDomainId) {
                    bExist = true;
                    break;
                }
            }*/

            if (!existFlag) {
                BierSubDomainBuilder subDomainBuilder = new BierSubDomainBuilder();
                subDomainBuilder.setSubDomainId(subDomainId);
                BierSubDomainKey subDomainKey = new BierSubDomainKey(subDomainId);
                subDomainBuilder.setKey(subDomainKey);
                addSubDomainList.add(subDomainBuilder.build());
            }
        }

        if (!BierTopologyManager.setSubDomainData(dataBroker,topologyId,domainId,addSubDomainList)) {
            builder.setConfigureResult(getConfigResult(false,"write subdomain to datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<ConfigureNodeOutput>> configureNode(ConfigureNodeInput input) {
        ConfigureNodeOutputBuilder builder = new ConfigureNodeOutputBuilder();
        if ( null == input ) {
            builder.setConfigureResult(getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        String topologyId = input.getTopologyId();
        String nodeId = input.getNodeId();
        if (topologyId == null || topologyId.equals("") || nodeId == null || nodeId.equals("")) {
            builder.setConfigureResult(getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!BierTopologyManager.checkDomainExist(dataBroker,topologyId,input.getDomain())) {
            builder.setConfigureResult(getConfigResult(false,"domain or subdomain is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNode node = BierTopologyManager.getNodeData(dataBroker, topologyId, nodeId);
        BierNodeBuilder nodeBuilder = new BierNodeBuilder(node);
        BierNodeParamsBuilder nodeParamsBuilder = new BierNodeParamsBuilder();
        nodeParamsBuilder.setDomain(input.getDomain());
        nodeBuilder.setBierNodeParams(nodeParamsBuilder.build());

        //可能是新增或者修改节点的bier配置，需要先比较
        if (!BierTopologyManager.setNodeData(dataBroker, topologyId, nodeBuilder.build())) {
            builder.setConfigureResult(getConfigResult(false,"write node to datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<QueryDomainOutput>> queryDomain(QueryDomainInput input) {
        if ( null == input ) {
            return returnRpcErr("input is null!");
        }

        String topologyId = input.getTopologyId();
        if (topologyId == null || topologyId.equals("")) {
            return returnRpcErr("input param is error!");
        }

        BierTopology  topo = BierTopologyManager.getTopologyData(dataBroker,topologyId);
        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
        List<BierDomain> bierDomainList = bierTopoBuilder.getBierDomain();

        List<Domain> domainList = new ArrayList<Domain>();
        if (bierDomainList != null) {
            int domainSize = bierDomainList.size();
            for (int iloop = 0; iloop < domainSize; ++iloop) {
                BierDomain bierDomain = bierDomainList.get(iloop);

                DomainBuilder domainBuilder = new DomainBuilder();
                domainBuilder.setDomainId(bierDomain.getDomainId());
                domainList.add(domainBuilder.build());
            }
        }

        QueryDomainOutputBuilder builder = new QueryDomainOutputBuilder();
        builder.setDomain(domainList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<QuerySubdomainOutput>> querySubdomain(QuerySubdomainInput input) {
        if ( null == input ) {
            return returnRpcErr("input is null!");
        }

        String topologyId = input.getTopologyId();
        DomainId domainId = input.getDomainId();
        if (topologyId == null || topologyId.equals("") || domainId == null) {
            return returnRpcErr("input param is error!");
        }

        QuerySubdomainOutputBuilder builder = new QuerySubdomainOutputBuilder();

        BierDomain domain = BierTopologyManager.getDomainData(dataBroker,topologyId,domainId);
        if (domain == null ) {
            return returnRpcErr("domain is not exist!");
        }
        List<BierSubDomain> bierSubDomainList = domain.getBierSubDomain();
        List<Subdomain> subDomainList = new ArrayList<Subdomain>();
        if (bierSubDomainList != null) {
            int subDomainSize = bierSubDomainList.size();
            for (int iloop = 0; iloop < subDomainSize; ++iloop) {
                BierSubDomainBuilder bierSubDomainBuilder = new BierSubDomainBuilder(bierSubDomainList.get(iloop));

                SubdomainBuilder subDomainBuilder = new SubdomainBuilder();
                subDomainBuilder.setSubDomainId(bierSubDomainBuilder.getSubDomainId());
                subDomainList.add(subDomainBuilder.build());
            }
        }
        builder.setSubdomain(subDomainList);

        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<QuerySubdomainNodeOutput>> querySubdomainNode(QuerySubdomainNodeInput input) {
        if ( null == input ) {
            return returnRpcErr("input is null!");
        }
        String topologyId = input.getTopologyId();
        DomainId domainId = input.getDomainId();
        SubDomainId subDomainId = input.getSubDomainId();
        if (topologyId == null || topologyId.equals("") || domainId == null || subDomainId == null) {
            return returnRpcErr("input param is error!");
        }

        List<BierNode> nodeList = BierTopologyManager.getSubDomainNode(dataBroker,topologyId,domainId,subDomainId);
        List<SubdomainNode> subDomainNodeList = new ArrayList<SubdomainNode>();
        int nodeSize = nodeList.size();
        for (int iloop = 0; iloop < nodeSize; ++iloop) {
            BierNode node = nodeList.get(iloop);
            SubdomainNodeBuilder nodeBuilder = new SubdomainNodeBuilder(node);
            subDomainNodeList.add(nodeBuilder.build());
        }

        QuerySubdomainNodeOutputBuilder builder = new QuerySubdomainNodeOutputBuilder();
        builder.setSubdomainNode(subDomainNodeList);

        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<QuerySubdomainLinkOutput>> querySubdomainLink(QuerySubdomainLinkInput input) {
        if ( null == input ) {
            return returnRpcErr("input is null!");
        }
        String topologyId = input.getTopologyId();
        DomainId domainId = input.getDomainId();
        SubDomainId subDomainId = input.getSubDomainId();
        if (topologyId == null || topologyId.equals("") || domainId == null || subDomainId == null) {
            return returnRpcErr("input param is error!");
        }

        List<BierLink> linkList = BierTopologyManager.getSubDomainLink(dataBroker,topologyId,domainId,subDomainId);
        List<SubdomainLink> subDomainLinkList = new ArrayList<SubdomainLink>();
        int linkSize = linkList.size();
        for (int iloop = 0; iloop < linkSize; ++iloop) {
            BierLink link = linkList.get(iloop);
            SubdomainLinkBuilder linkBuilder = new SubdomainLinkBuilder(link);
            subDomainLinkList.add(linkBuilder.build());
        }

        QuerySubdomainLinkOutputBuilder builder = new QuerySubdomainLinkOutputBuilder();
        builder.setSubdomainLink(subDomainLinkList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<DeleteDomainOutput>> deleteDomain(DeleteDomainInput input) {
        DeleteDomainOutputBuilder builder = new DeleteDomainOutputBuilder();
        if ( null == input ) {
            builder.setConfigureResult(getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        String topologyId = input.getTopologyId();
        DomainId domainId = input.getDomainId();
        if (topologyId == null || topologyId.equals("") || domainId == null) {
            builder.setConfigureResult(getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        //删除域，并将属于该域的节点的域配置信息删除
        BierTopology  topo = BierTopologyManager.getTopologyData(dataBroker,topologyId);
        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);

        List<BierDomain> domainList = bierTopoBuilder.getBierDomain();
        if (!BierTopologyManager.checkDomainExist(dataBroker,topologyId,domainId,domainList)) {
            builder.setConfigureResult(getConfigResult(false,"domain is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        List<BierNode> nodeList = bierTopoBuilder.getBierNode();
        if (!BierTopologyManager.delDomainData(dataBroker,topologyId,domainId,nodeList)) {
            builder.setConfigureResult(getConfigResult(false,"delete domain form datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<DeleteSubdomainOutput>> deleteSubdomain(DeleteSubdomainInput input) {
        DeleteSubdomainOutputBuilder builder = new DeleteSubdomainOutputBuilder();
        if ( null == input ) {
            builder.setConfigureResult(getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        String topologyId = input.getTopologyId();
        DomainId domainId = input.getDomainId();
        SubDomainId subDomainId = input.getSubDomainId();
        if (topologyId == null || topologyId.equals("") || domainId == null || subDomainId == null) {
            builder.setConfigureResult(getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        //删除子域，并将属于该子域的节点的子域配置信息删除
        BierTopology  topo = BierTopologyManager.getTopologyData(dataBroker,topologyId);
        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
        List<BierDomain> domainList = bierTopoBuilder.getBierDomain();
        if (!BierTopologyManager.checkSubDomainExist(dataBroker,topologyId,domainId,subDomainId,domainList)) {
            builder.setConfigureResult(getConfigResult(false,"domain or subdomain is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        List<BierNode> nodeList = bierTopoBuilder.getBierNode();
        if (!BierTopologyManager.delSubDomainData(dataBroker,topologyId,domainId,subDomainId,nodeList)) {
            builder.setConfigureResult(getConfigResult(false,"delete subdomain form datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<DeleteNodeOutput>> deleteNode(DeleteNodeInput input) {
        DeleteNodeOutputBuilder builder = new DeleteNodeOutputBuilder();
        if ( null == input ) {
            builder.setConfigureResult(getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        String topologyId = input.getTopologyId();
        DomainId domainId = input.getDomainId();
        SubDomainId subDomainId = input.getSubDomainId();
        String nodeId = input.getNodeId();
        if (topologyId == null || topologyId.equals("") || domainId == null || subDomainId == null
                || nodeId == null || nodeId.equals("")) {
            builder.setConfigureResult(getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierNode node = BierTopologyManager.getNodeData(dataBroker, topologyId, nodeId);
        if (node == null) {
            builder.setConfigureResult(getConfigResult(false,"node is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!BierTopologyManager.delNodeFromDomain(dataBroker, topologyId,domainId,subDomainId,node)) {
            builder.setConfigureResult(getConfigResult(false,"delete node form datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    private ConfigureResult getConfigResult(boolean result,String errorCause) {
        ConfigureResultBuilder cfgResultBuilder  = new ConfigureResultBuilder();
        if (result) {
            cfgResultBuilder.setResult(ConfigureResult.Result.SUCCESS);
        } else {
            cfgResultBuilder.setResult(ConfigureResult.Result.FAILURE);
            cfgResultBuilder.setErrorCause(errorCause);
        }

        return cfgResultBuilder.build();
    }

    private static <T> Future<RpcResult<T>> returnRpcErr(String errMsg) {
        return RpcResultBuilder.<T>failed().withError(RpcError.ErrorType.APPLICATION, errMsg).buildFuture();
    }
}

