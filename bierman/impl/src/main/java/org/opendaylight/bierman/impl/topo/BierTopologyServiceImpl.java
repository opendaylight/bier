/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl.topo;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.bierman.impl.BierDataManager;
import org.opendaylight.bierman.impl.NotificationProvider;
import org.opendaylight.bierman.impl.RpcUtil;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.BierTopologyApiService;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureDomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureDomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureDomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureSubdomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureSubdomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureSubdomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteDomainInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteDomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteDomainOutputBuilder;
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
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTeSubdomainLinkInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTeSubdomainLinkOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTeSubdomainLinkOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTeSubdomainNodeInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTeSubdomainNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTeSubdomainNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.TestNotificationPublishInput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.TestNotificationPublishOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.TopoChangeBuilder;
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
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.te.subdomain.link.output.TeSubdomainLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.te.subdomain.link.output.TeSubdomainLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.te.subdomain.node.output.TeSubdomainNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.query.te.subdomain.node.output.TeSubdomainNodeBuilder;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTopologyServiceImpl implements BierTopologyApiService {
    private static final Logger LOG = LoggerFactory.getLogger(BierTopologyServiceImpl.class);
    private BierDataManager topoManager;

    public BierTopologyServiceImpl(BierDataManager topoManager) {
        this.topoManager = topoManager;
    }

    public ListenableFuture<RpcResult<LoadTopologyOutput>> loadTopology(LoadTopologyInput input) {
        List<Topology> topoList = new ArrayList<Topology>();
        TopologyBuilder topoBuilder = new TopologyBuilder();
        topoBuilder.setTopologyId(BierDataManager.TOPOLOGY_ID);
        topoList.add(topoBuilder.build());
        LoadTopologyOutputBuilder builder = new LoadTopologyOutputBuilder();
        builder.setTopology(topoList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public ListenableFuture<RpcResult<QueryTopologyOutput>> queryTopology(QueryTopologyInput input) {
        if (null == input) {
            return RpcUtil.returnRpcErr("input is null!");
        }

        String topologyId = input.getTopologyId();
        if (null == topologyId || topologyId.equals("")) {
            return RpcUtil.returnRpcErr("input param is error!");
        }

        BierTopology  topo = topoManager.getTopologyData(topologyId);
        if (topo == null) {
            return RpcUtil.returnRpcErr("topo is not exist!");
        }

        List<String> onlineNodesId = topoManager.nodesOnline(topologyId);

        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
        QueryTopologyOutputBuilder builder = new QueryTopologyOutputBuilder();
        builder.setTopologyId(bierTopoBuilder.getTopologyId());
        List<BierNode> bierNodeList = bierTopoBuilder.getBierNode();
        List<NodeId> nodeList = new ArrayList<NodeId>();
        if (bierNodeList != null) {
            int nodeSize = bierNodeList.size();
            for (int loopi = 0; loopi < nodeSize; ++loopi) {
                BierNode bierNode = bierNodeList.get(loopi);
                if (onlineNodesId.contains(bierNode.getNodeId())) {
                    BierNodeBuilder bierNodeBuilder = new BierNodeBuilder(bierNode);
                    NodeIdBuilder nodeBuilder = new NodeIdBuilder();
                    nodeBuilder.setNodeId(bierNodeBuilder.getNodeId());
                    nodeList.add(nodeBuilder.build());
                }
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

    public ListenableFuture<RpcResult<QueryNodeOutput>> queryNode(QueryNodeInput input) {
        if (null == input) {
            return RpcUtil.returnRpcErr("input is null!");
        }
        String topologyId = input.getTopologyId();
        List<String> nodeIdList = input.getNode();
        if (null == topologyId || topologyId.equals("") || nodeIdList == null || nodeIdList.isEmpty()) {
            return RpcUtil.returnRpcErr("input param is error!");
        }

        BierTopology  topo = topoManager.getTopologyData(topologyId);
        if (topo == null) {
            return RpcUtil.returnRpcErr("topo is not exist!");
        }
        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
        List<Node> nodeList = new ArrayList<Node>();
        List<BierNode> bierNodeList = bierTopoBuilder.getBierNode();
        if (bierNodeList != null) {
            int nodeSize = bierNodeList.size();
            for (int loopi = 0; loopi < nodeSize; ++loopi) {
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
        LOG.info("nodeList --------" + nodeList);
        QueryNodeOutputBuilder builder = new QueryNodeOutputBuilder();
        builder.setNode(nodeList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public ListenableFuture<RpcResult<QueryLinkOutput>> queryLink(QueryLinkInput input) {
        if (null == input) {
            return RpcUtil.returnRpcErr("input is null!");
        }
        String topologyId = input.getTopologyId();
        List<String> linkIdList = input.getLink();
        if (null == topologyId || topologyId.equals("") || linkIdList == null || linkIdList.isEmpty()) {
            return RpcUtil.returnRpcErr("input param is error!");
        }

        BierTopology  topo = topoManager.getTopologyData(topologyId);
        if (topo == null) {
            return RpcUtil.returnRpcErr("topo is not exist!");
        }
        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
        QueryLinkOutputBuilder builder = new QueryLinkOutputBuilder();
        List<Link> linkList = new ArrayList<Link>();
        List<BierLink> bierLinkList = bierTopoBuilder.getBierLink();
        if (bierLinkList != null) {
            int linkSize = bierLinkList.size();
            for (int loopi = 0; loopi < linkSize; ++loopi) {
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

    public ListenableFuture<RpcResult<ConfigureDomainOutput>> configureDomain(ConfigureDomainInput input) {
        ConfigureDomainOutputBuilder builder = new ConfigureDomainOutputBuilder();

        if (null == input) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        String topologyId = input.getTopologyId();
        List<DomainId> domainList = input.getDomain();
        if (topologyId == null || topologyId.equals("") || domainList == null || domainList.isEmpty()) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        List<BierDomain> addDomainList = new ArrayList<BierDomain>();
        BierTopology  topo = topoManager.getTopologyData(topologyId);
        if (topo == null) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"topo is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        List<BierDomain> allDomainList = topo.getBierDomain();
        int domainSize = domainList.size();
        for (int iloop = 0; iloop < domainSize; ++iloop) {
            DomainId domainId = domainList.get(iloop);
            boolean existFlag = false;
            if (allDomainList != null) {
                int allDomainSize = allDomainList.size();
                for (int jloop = 0; jloop < allDomainSize; ++jloop) {
                    BierDomain bierDomain = allDomainList.get(jloop);
                    BierDomainBuilder bierDomainBuilder = new BierDomainBuilder(bierDomain);
                    if (domainId.equals(bierDomainBuilder.getDomainId())) {
                        existFlag = true;
                        break;
                    }
                }
            }
            if (existFlag) {
                builder.setConfigureResult(RpcUtil.getConfigResult(false,"domain is exist!"));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }

            BierDomainBuilder bierDomainBuilder = new BierDomainBuilder();
            bierDomainBuilder.setDomainId(domainId);
            BierDomainKey domainKey = new BierDomainKey(domainId);
            bierDomainBuilder.withKey(domainKey);
            addDomainList.add(bierDomainBuilder.build());
        }

        if (!topoManager.setDomainData(topologyId,addDomainList)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"write domain to datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public ListenableFuture<RpcResult<ConfigureSubdomainOutput>> configureSubdomain(ConfigureSubdomainInput input) {
        ConfigureSubdomainOutputBuilder builder = new ConfigureSubdomainOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        String topologyId = input.getTopologyId();
        DomainId domainId = input.getDomainId();
        List<SubDomainId> subDomainIdList = input.getSubDomain();

        if (topologyId == null || topologyId.equals("") || subDomainIdList == null || subDomainIdList.isEmpty()) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        BierDomain domain = topoManager.getDomainData(topologyId,domainId);
        if (domain == null) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"domain is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        List<BierSubDomain> addSubDomainList = new ArrayList<BierSubDomain>();
        List<BierSubDomain> allSubDomainList = domain.getBierSubDomain();
        int subDomainSize = subDomainIdList.size();
        for (int iloop = 0; iloop < subDomainSize; ++iloop) {
            SubDomainId subDomainId = subDomainIdList.get(iloop);
            boolean existFlag = false;
            if (allSubDomainList != null) {
                int allSubDomain = allSubDomainList.size();
                for (int jloop = 0; jloop < allSubDomain; ++jloop) {
                    if (allSubDomainList.get(jloop).getSubDomainId().equals(subDomainId)) {
                        existFlag = true;
                        break;
                    }
                }
            }

            if (existFlag) {
                builder.setConfigureResult(RpcUtil.getConfigResult(false,"subdomain is exist!"));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }

            BierSubDomainBuilder subDomainBuilder = new BierSubDomainBuilder();
            subDomainBuilder.setSubDomainId(subDomainId);
            BierSubDomainKey subDomainKey = new BierSubDomainKey(subDomainId);
            subDomainBuilder.withKey(subDomainKey);
            addSubDomainList.add(subDomainBuilder.build());
        }

        if (!topoManager.setSubDomainData(topologyId,domainId,addSubDomainList)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"write subdomain to datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public ListenableFuture<RpcResult<QueryDomainOutput>> queryDomain(QueryDomainInput input) {
        if (null == input) {
            return RpcUtil.returnRpcErr("input is null!");
        }

        String topologyId = input.getTopologyId();
        if (topologyId == null || topologyId.equals("")) {
            return RpcUtil.returnRpcErr("input param is error!");
        }

        BierTopology  topo = topoManager.getTopologyData(topologyId);
        if (topo == null) {
            return RpcUtil.returnRpcErr("topo is not exist!");
        }
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

    public ListenableFuture<RpcResult<TestNotificationPublishOutput>> testNotificationPublish(TestNotificationPublishInput input) {
        NotificationProvider.getInstance().notify(new TopoChangeBuilder().setTopoId("flow:1").build());
        return Futures.immediateFuture(RpcResultBuilder.<TestNotificationPublishOutput>success().build());
    }

    public ListenableFuture<RpcResult<QuerySubdomainOutput>> querySubdomain(QuerySubdomainInput input) {
        if (null == input) {
            return RpcUtil.returnRpcErr("input is null!");
        }

        String topologyId = input.getTopologyId();
        DomainId domainId = input.getDomainId();
        if (topologyId == null || topologyId.equals("") || domainId == null) {
            return RpcUtil.returnRpcErr("input param is error!");
        }

        QuerySubdomainOutputBuilder builder = new QuerySubdomainOutputBuilder();

        BierDomain domain = topoManager.getDomainData(topologyId,domainId);
        if (domain == null) {
            return RpcUtil.returnRpcErr("domain is not exist!");
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

    public ListenableFuture<RpcResult<QuerySubdomainNodeOutput>> querySubdomainNode(QuerySubdomainNodeInput input) {
        if (null == input) {
            return RpcUtil.returnRpcErr("input is null!");
        }

        String errorCause = checkSubdomain(input,input.getTopologyId(),input.getDomainId(),input.getSubDomainId());
        if (!errorCause.equals("")) {
            return RpcUtil.returnRpcErr(errorCause);
        }

        List<BierNode> nodeList = topoManager.getSubDomainNode(input.getTopologyId(),
                input.getDomainId(),input.getSubDomainId());
        List<String> onlineNodesId = topoManager.nodesOnline(input.getTopologyId());
        List<SubdomainNode> subDomainNodeList = new ArrayList<SubdomainNode>();
        int nodeSize = nodeList.size();
        for (int iloop = 0; iloop < nodeSize; ++iloop) {
            BierNode node = nodeList.get(iloop);
            if (onlineNodesId.contains(node.getNodeId())) {
                SubdomainNodeBuilder nodeBuilder = new SubdomainNodeBuilder(node);
                subDomainNodeList.add(nodeBuilder.build());
            }
        }

        QuerySubdomainNodeOutputBuilder builder = new QuerySubdomainNodeOutputBuilder();
        builder.setSubdomainNode(subDomainNodeList);

        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public ListenableFuture<RpcResult<QuerySubdomainLinkOutput>> querySubdomainLink(QuerySubdomainLinkInput input) {
        if (null == input) {
            return RpcUtil.returnRpcErr("input is null!");
        }

        String errorCause = checkSubdomain(input,input.getTopologyId(),input.getDomainId(),input.getSubDomainId());
        if (!errorCause.equals("")) {
            return RpcUtil.returnRpcErr(errorCause);
        }

        List<BierLink> linkList = topoManager.getSubDomainLink(input.getTopologyId(),
                input.getDomainId(),input.getSubDomainId());
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

    public ListenableFuture<RpcResult<QueryTeSubdomainNodeOutput>> queryTeSubdomainNode(QueryTeSubdomainNodeInput input) {
        if (null == input) {
            return RpcUtil.returnRpcErr("input is null!");
        }

        String errorCause = checkSubdomain(input,input.getTopologyId(),input.getDomainId(),input.getSubDomainId());
        if (!errorCause.equals("")) {
            return RpcUtil.returnRpcErr(errorCause);
        }

        List<BierNode> nodeList = topoManager.getTeSubDomainNode(input.getTopologyId(),
                input.getDomainId(),input.getSubDomainId());
        List<String> onlineNodesId = topoManager.nodesOnline(input.getTopologyId());
        List<TeSubdomainNode> subDomainNodeList = new ArrayList<TeSubdomainNode>();
        int nodeSize = nodeList.size();
        for (int iloop = 0; iloop < nodeSize; ++iloop) {
            BierNode node = nodeList.get(iloop);
            if (onlineNodesId.contains(node.getNodeId())) {
                TeSubdomainNodeBuilder nodeBuilder = new TeSubdomainNodeBuilder(node);
                subDomainNodeList.add(nodeBuilder.build());
            }
        }

        QueryTeSubdomainNodeOutputBuilder builder = new QueryTeSubdomainNodeOutputBuilder();
        builder.setTeSubdomainNode(subDomainNodeList);

        return RpcResultBuilder.success(builder.build()).buildFuture();

    }

    public ListenableFuture<RpcResult<QueryTeSubdomainLinkOutput>> queryTeSubdomainLink(QueryTeSubdomainLinkInput input) {
        if (null == input) {
            return RpcUtil.returnRpcErr("input is null!");
        }

        String errorCause = checkSubdomain(input,input.getTopologyId(),input.getDomainId(),input.getSubDomainId());
        if (!errorCause.equals("")) {
            return RpcUtil.returnRpcErr(errorCause);
        }

        List<BierLink> linkList = topoManager.getTeSubDomainLink(input.getTopologyId(),
                input.getDomainId(),input.getSubDomainId());
        List<TeSubdomainLink> subDomainLinkList = new ArrayList<TeSubdomainLink>();
        int linkSize = linkList.size();
        for (int iloop = 0; iloop < linkSize; ++iloop) {
            BierLink link = linkList.get(iloop);
            TeSubdomainLinkBuilder linkBuilder = new TeSubdomainLinkBuilder(link);
            subDomainLinkList.add(linkBuilder.build());
        }

        QueryTeSubdomainLinkOutputBuilder builder = new QueryTeSubdomainLinkOutputBuilder();
        builder.setTeSubdomainLink(subDomainLinkList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public ListenableFuture<RpcResult<DeleteDomainOutput>> deleteDomain(DeleteDomainInput input) {
        DeleteDomainOutputBuilder builder = new DeleteDomainOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        String topologyId = input.getTopologyId();
        DomainId domainId = input.getDomainId();
        if (topologyId == null || topologyId.equals("") || domainId == null) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierTopology  topo = topoManager.getTopologyData(topologyId);
        if (topo == null) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"topo is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);

        List<BierDomain> domainList = bierTopoBuilder.getBierDomain();
        if (!topoManager.checkDomainExist(topologyId,domainId,domainList)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"domain is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        List<BierNode> nodeList = bierTopoBuilder.getBierNode();
        if (!topoManager.delDomainData(topologyId,domainId,nodeList)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"delete domain form datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public ListenableFuture<RpcResult<DeleteSubdomainOutput>> deleteSubdomain(DeleteSubdomainInput input) {
        DeleteSubdomainOutputBuilder builder = new DeleteSubdomainOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        String topologyId = input.getTopologyId();
        DomainId domainId = input.getDomainId();
        SubDomainId subDomainId = input.getSubDomainId();
        if (topologyId == null || topologyId.equals("") || domainId == null || subDomainId == null) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"input param is error!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        BierTopology  topo = topoManager.getTopologyData(topologyId);
        if (topo == null) {
            return RpcUtil.returnRpcErr("topo is not exist!");
        }
        BierTopologyBuilder bierTopoBuilder = new BierTopologyBuilder(topo);
        List<BierDomain> domainList = bierTopoBuilder.getBierDomain();
        if (!topoManager.checkSubDomainExist(topologyId,domainId,subDomainId,domainList)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"domain or subdomain is not exist!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        List<BierNode> nodeList = bierTopoBuilder.getBierNode();
        if (!topoManager.delSubDomainData(topologyId,domainId,subDomainId,nodeList)) {
            builder.setConfigureResult(RpcUtil.getConfigResult(false,"delete subdomain form datastore failed!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        builder.setConfigureResult(RpcUtil.getConfigResult(true,""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public <T> String checkSubdomain(T input,String topologyId,DomainId domainId,SubDomainId subDomainId) {
        if (topologyId == null || topologyId.equals("") || domainId == null || subDomainId == null) {
            return "input param is error!";
        }

        BierTopology  topo = topoManager.getTopologyData(topologyId);
        if (topo == null) {
            return "topo is not exist!";
        }
        List<BierDomain> domainList = topo.getBierDomain();
        if (!topoManager.checkSubDomainExist(topologyId,domainId,subDomainId,domainList)) {
            return "domain or subdomain is not exist!";
        }

        return "";
    }
}

