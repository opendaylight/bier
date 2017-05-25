/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.topology;

import com.google.common.annotations.VisibleForTesting;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opendaylight.bier.pce.impl.provider.DbProvider;
import org.opendaylight.bier.pce.impl.provider.NotificationProvider;
import org.opendaylight.bier.pce.impl.provider.PcePathImpl;
import org.opendaylight.bier.pce.impl.util.ComUtility;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPceService;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.BierTopologyApiListener;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkAdd;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkChange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkRemove;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.link.add.AddLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TopologyProvider implements BierTopologyApiListener {
    private static final Logger LOG = LoggerFactory.getLogger(TopologyProvider.class);
    public static final String DEFAULT_TOPO_ID_STRING = "example-linkstate-topology";
    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;
    private final NotificationPublishService notificationPublishService;
    private BindingAwareBroker.RpcRegistration<BierPceService> pceService;
    private static TopologyProvider instance;
    protected Map<String, Graph<String, BierLink>> topoGraphMap = new ConcurrentHashMap<>();
    protected Map<String, Graph<String, BierLink>> topoGraphMapAllLink = new ConcurrentHashMap<>();
    private PcePathImpl pcePathImpl = PcePathImpl.getInstance();
    protected ExecutorService executor = Executors.newFixedThreadPool(1);

    public TopologyProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcRegistry,
                             final NotificationPublishService notificationPublishService) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;
        this.notificationPublishService = notificationPublishService;
        instance = this;
    }

    @Override
    public void onTopoChange(org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.TopoChange notification) {
        //do nothing
    }

    @Override
    public void onLinkAdd(LinkAdd linkAdd) {
        LOG.info("pce-topo:onLinkAdd:" + linkAdd);
        String topoId = linkAdd.getTopoId();
        Graph<String,BierLink> topoGraph = topoGraphMap.get(topoId);
        Graph<String,BierLink> topoGraphAllLink = topoGraphMapAllLink.get(topoId);
        BierLink link = transBierLink(linkAdd.getAddLink());
        if (topoGraph != null && topoGraphAllLink != null) {
            addLink(link, topoGraphAllLink, topoGraph,topoId);
        }

    }



    @Override
    public void onLinkRemove(LinkRemove linkRemove) {
        LOG.info("pce-topo:onLinkRemove:" + linkRemove);
        String topoId = linkRemove.getTopoId();
        Graph<String,BierLink> topoGraph = topoGraphMap.get(topoId);
        Graph<String,BierLink> topoGraphAllLink = topoGraphMapAllLink.get(topoId);
        BierLink link = new BierLinkBuilder(new BierLinkBuilder(linkRemove.getRemoveLink()).build()).build();
        if (topoGraph != null && topoGraphAllLink != null) {
            removeLink(link, topoGraphAllLink, topoGraph,topoId);
        }
    }

    @Override
    public void onLinkChange(LinkChange linkChange) {
        LOG.info("pce-topo:onLinkChange:" + linkChange);
        String topoId = linkChange.getTopoId();
        Graph<String,BierLink> topoGraph = topoGraphMap.get(topoId);
        Graph<String,BierLink> topoGraphAllLink = topoGraphMapAllLink.get(topoId);
        BierLink linkOld = new BierLinkBuilder(linkChange.getOldLink()).build();
        BierLink linkNew = new BierLinkBuilder(linkChange.getNewLink()).build();
        if (topoGraph != null && topoGraphAllLink != null) {
            updateLink(linkOld,linkNew, topoGraphAllLink, topoGraph,topoId);
        }
    }

    @VisibleForTesting
    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public static TopologyProvider getInstance() {
        return instance;
    }

    public void destroy() {
        topoGraphMap.clear();
        topoGraphMapAllLink.clear();

    }

    public void init() {
        LOG.info("TopologyProvider Session Initiated");
        NotificationProvider.getInstance().setNotificationService(notificationPublishService);
        DbProvider.getInstance().setDataBroker(dataBroker);
        pceService = rpcRegistry.addRpcImplementation(BierPceService.class,pcePathImpl);
        pcePathImpl.writeDbRoot();
        setPcePathImpl(pcePathImpl);
        PathsRecordPerTopology.getInstance().setPcePathService(pcePathImpl);
    }

    public void close() {
        LOG.info("TopologyProvider Closed");
        destroy();
        pcePathImpl.destroy();

        if (pceService != null) {
            pceService.close();
        }
    }

/*
    public Graph<String, BierLink> getTopoGraphRecover(String topoId) {
        Graph<String, BierLink> topoGraph = topoGraphMap.get(topoId);
        if (topoGraph == null) {
            topoGraph = newTopoGraph(topoId);
        }

        return topoGraph;
    }
*/


    private Graph<String, BierLink> transformTopo2Graph(List<BierLink> list) {
        Graph<String, BierLink> graph = new SparseMultigraph<>();
        if (list == null) {
            return graph;
        }
        addLinks2Gragh(list, graph,"topoGraphAllLink");

        return graph;
    }

    private void addLinks2Gragh(List<BierLink> links, Graph<String, BierLink> graph, String logMessage) {
        for (BierLink link : links) {
            BierLink bierLink = new BierLinkBuilder(link).build();
            addLink2Gragh(bierLink, graph, logMessage);
        }
    }

    protected void addLink2Gragh(BierLink link, Graph<String, BierLink> graph, String logMessage) {
        String srcId = link.getLinkSource().getSourceNode();
        String destId = link.getLinkDest().getDestNode();

        if (srcId.equals(destId)) {
            return;
        }

        // Make sure the vertex are there before adding the edge
        graph.addVertex(srcId);
        graph.addVertex(destId);

        // add the link between
        if (!graph.containsEdge(link)) {
            graph.addEdge(link, srcId, destId, EdgeType.DIRECTED);
        }
        linkInfo2Log("topo:addlink to " + logMessage , link);
    }



    private void updateLink(BierLink originLink, BierLink updatedLink,
                            Graph<String, BierLink> topoGraphAllLink,
                            Graph<String, BierLink> topoGraph, String topoId) {
        linkInfo2Log("topo:updateLink", updatedLink);

        removeLinkFromGraphAllLink(originLink,topoGraphAllLink);
        addLink2Gragh(updatedLink, topoGraphAllLink, "topoGraphAllLink");

        if (topoGraph.containsEdge(originLink)) {
            removeLinkFromGraph(originLink, topoGraph, "topoGraph");
            addLink2Gragh(updatedLink, topoGraph,"topoGraph");
            pcePathImpl.refreshAllBierTePath(topoId);
        }
    }


    private void removeLinkFromGraphAllLink(BierLink link,Graph<String, BierLink> topoGraphAllLink) {
        if (!topoGraphAllLink.containsEdge(link)) {
            return;
        }

        topoGraphAllLink.removeEdge(link);
        String srcId = link.getLinkSource().getSourceNode();
        String destId = link.getLinkDest().getDestNode();
        if (0 == topoGraphAllLink.getNeighborCount(srcId)) {
            topoGraphAllLink.removeVertex(srcId);
        }
        if (0 == topoGraphAllLink.getNeighborCount(destId)) {
            topoGraphAllLink.removeVertex(destId);
        }
        linkInfo2Log("topo:removelink from topoGraphAllLink", link);
    }

    protected List<BierLink> getLinks(String topoId) {
        InstanceIdentifier<BierTopology> path = InstanceIdentifier.builder(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(topoId))
                .build();

        BierTopology topology = DbProvider.getInstance().readData(LogicalDatastoreType.CONFIGURATION,path);

        return (topology != null) ? topology.getBierLink() : null;
    }

    public void initGraph() {
        newTopoGraph(DEFAULT_TOPO_ID_STRING);
    }

    public void setPcePathImpl(PcePathImpl pcePathImpl) {
        this.pcePathImpl = pcePathImpl;
    }

    public Graph<String, BierLink> getTopoGraph(String topoIdInput) {
        String topoId = (topoIdInput == null) ? DEFAULT_TOPO_ID_STRING : topoIdInput;

        Graph<String, BierLink> topoGraph = topoGraphMap.get(topoId);
        if (topoGraph == null) {
            synchronized (this) {
                topoGraph = topoGraphMap.get(topoId);
                if (topoGraph == null) {
                    topoGraph = newTopoGraph(topoId);
                    if (topoGraph == null) {
                        LOG.error("getTopoGraph:topoGraph is null!");
                        return null;
                    }
                }
            }
        }

        return topoGraph;
    }

    public static void setInstance(TopologyProvider provider) {
        instance = provider;
    }

    private Graph<String, BierLink> newTopoGraph(String topoId) {
        List<BierLink> links = getLinks(topoId);
        Graph<String, BierLink> topoGraphAllLink = transformTopo2Graph(links);
        if (topoGraphAllLink == null) {
            return null;
        }
        topoGraphMapAllLink.put(topoId, topoGraphAllLink);

        Graph<String, BierLink> topoGraph = newTopoGraphBidirect(topoGraphAllLink);
        if (topoGraph == null) {
            return null;
        }
        topoGraphMap.put(topoId, topoGraph);

        return topoGraph;
    }

    private Graph<String, BierLink> newTopoGraphBidirect(Graph<String, BierLink> topoGraphAllLink) {
        Graph<String, BierLink> graphNew = new SparseMultigraph<>();
        if (topoGraphAllLink == null) {
            return graphNew;
        }

        List<BierLink> linksReverse;

        for (String localNode : topoGraphAllLink.getVertices()) {
            for (BierLink outEdge : topoGraphAllLink.getOutEdges(localNode)) {
                if (graphNew.containsEdge(outEdge)) {
                    continue;
                }
                linksReverse = ComUtility.getReverseLink(topoGraphAllLink, outEdge);
                if ((linksReverse != null) && (!linksReverse.isEmpty())) {
                    addLink2Gragh(outEdge, graphNew, "topoGraph");
                    addLinks2Gragh(linksReverse, graphNew, "topoGraph");
                }
            }
        }

        return graphNew;
    }

    protected void addLink(BierLink link, Graph<String, BierLink> topoGraphAllLink,
                           Graph<String, BierLink> topoGraph,String topoId) {

        addLink2Gragh(link, topoGraphAllLink, "topoGraphAllLink");

        List<BierLink> linkReverse = ComUtility.getReverseLink(topoGraphAllLink, link);
        if ((linkReverse == null) || (linkReverse.isEmpty())) {
            LOG.info("reverse link not exist,link=" + ComUtility.getLinkString(link));
            return;
        }
        addLink2Graph(link, linkReverse, topoGraph, "topoGraph");
        pcePathImpl.refreshAllBierTePath(topoId);
    }

    protected void removeLink(BierLink link, Graph<String, BierLink> topoGraphAllLink,
                              Graph<String, BierLink> topoGraph,String topoId) {
        removeLinkFromGraph(link, topoGraphAllLink, "topoGraphAllLink");

        if (topoGraph.containsEdge(link)) {
            removeLinkFromGraph(link, topoGraph, "topoGraph");
            List<BierLink> otherLinks = ComUtility.getOtherLink(topoGraph, link);
            if ((null == otherLinks) || (otherLinks.isEmpty())) {

                List<BierLink> linksReverse = ComUtility.getReverseLink(topoGraph, link);
                if ((linksReverse == null) || (linksReverse.isEmpty())) {
                    LOG.error("no reverse link!", link.toString());
                } else {
                    removeLinksFromGraph(linksReverse, topoGraph);
                }
            }

            pcePathImpl.refreshAllBierTePath(topoId);
        }
    }

    private void addLink2Graph(BierLink link, List<BierLink> linksReverse,
                               Graph<String, BierLink> topoGraph, String logMessage) {
        addLink2Gragh(link, topoGraph,logMessage);
        addLinks2Gragh(linksReverse, topoGraph, logMessage);
    }

    private void removeLinkFromGraph(BierLink link, Graph<String, BierLink> graph, String logMessage) {
        if (!graph.containsEdge(link)) {
            return;
        }

        graph.removeEdge(link);

        String srcId = link.getLinkSource().getSourceNode();
        String destId = link.getLinkDest().getDestNode();

        if (!graph.containsVertex(srcId)) {
            LOG.error("srcId does not exist!", link.toString());
            return;
        } else {
            if (0 == graph.getNeighborCount(srcId)) {
                graph.removeVertex(srcId);
            }
        }
        if (!graph.containsVertex(destId)) {
            LOG.error("destId does not exist!", link.toString());
            return;
        } else {
            if (0 == graph.getNeighborCount(destId)) {
                graph.removeVertex(destId);
            }
        }
        linkInfo2Log("topo:removelink from " + logMessage , link);
    }

    private void removeLinksFromGraph(List<BierLink> links, Graph<String, BierLink> topoGraph) {
        if ((links == null) || (links.isEmpty())) {
            return;
        }
        for (BierLink link : links) {
            removeLinkFromGraph(link, topoGraph, "topoGraph");
        }
    }



    private void linkInfo2Log(String headInfo, BierLink link) {
        LOG.info(headInfo + " {" + link + "} ");
    }

    private BierLink transBierLink(AddLink addLink) {
        return new BierLinkBuilder(addLink).build();
    }

}
