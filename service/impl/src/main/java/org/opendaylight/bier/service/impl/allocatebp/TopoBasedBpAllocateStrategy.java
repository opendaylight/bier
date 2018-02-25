/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.service.impl.allocatebp;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.bier.service.impl.NotificationProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.StrategyDataPersistence;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.LinkConnectedBpMap;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.LinkConnectedBpMapBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.LinkConnectedBpMapKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.NodeConnectedTpIdMap;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.NodeConnectedTpIdMapBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.NodeConnectedTpIdMapKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.NodeLocalDecapBpMap;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.NodeLocalDecapBpMapBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.NodeLocalDecapBpMapKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.SubdomainDeployedChannel;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.SubdomainDeployedChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.SubdomainDeployedChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.link.connected.bp.map.LinkBp;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.link.connected.bp.map.LinkBpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.link.connected.bp.map.LinkBpKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.node.connected.tp.id.map.NodeTpId;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.node.connected.tp.id.map.NodeTpIdKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.node.connected.tp.id.map.node.tp.id.ConnectedTpId;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.node.connected.tp.id.map.node.tp.id.ConnectedTpIdBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.node.connected.tp.id.map.node.tp.id.ConnectedTpIdKey;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.node.local.decap.bp.map.NodeBp;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.node.local.decap.bp.map.NodeBpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.strategy.data.rev170901.strategy.data.persistence.node.local.decap.bp.map.NodeBpKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBpKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopoBasedBpAllocateStrategy extends AbstractBPAllocateStrategy {

    private static TopoBasedBpAllocateStrategy instance = new TopoBasedBpAllocateStrategy();

    private static final Logger LOG = LoggerFactory.getLogger(TopoBasedBpAllocateStrategy.class);
    private Map<Integer, Map<String,List<String>>> nodeConnectedTpIdMap = new HashMap<>();
    public Map<Integer, Map<String,TeBp>>  nodeLocalDecapBpMap = new HashMap<>();
    private Map<Integer, Map<SrcDstConnected,TeBp>> linkConnectedBpMap = new HashMap<>();
    private Set<Integer> subdomainDeployedChannel = new HashSet<>();
    public Map<Integer, List<BierLink>> tmpBierLinkDelete = new HashMap<>();
    public Map<Integer, List<BierLink>> tmpBierLinkAdd = new HashMap<>();

    //LISTENER_OPENNED false:not true:yes
    private static boolean LISTENER_OPENNED = false;
    private static int SRC_NODE = 1;
    private static int DST_NODE = 2;

    private TopoBasedBpAllocateStrategy() {

    }

    public TopoBasedBpAllocateStrategy(DataBroker dataBroker) {
        super(dataBroker);
    }

    @Override
    public boolean allocateBPs(Channel channel, List<Bfer> bferList) {
        int subdomainValue = channel.getSubDomainId().getValue();
        TeBsl teBsl = checkAndGetOneBsl(subdomainValue);
        TeSi teSi = checkAndGetOneSi(subdomainValue,teBsl);

        if (null == teBsl || null == teSi) {
            LOG.info("Get teBsl or teSi for subdomain {} failed!", subdomainValue);
            return false;
        }

        if (!subdomainDeployedChannel.contains(subdomainValue)) {
            StrategyDataPersistence strategyDataPersistence = queryStrategyData();
            List<SubdomainDeployedChannel> subdomainList = strategyDataPersistence.getSubdomainDeployedChannel();
            if (null != subdomainList) {
                for (SubdomainDeployedChannel subdomain:subdomainList) {
                    if (subdomain.getSubdomainId().equals(subdomainValue)) {
                        LOG.info("TopoBasedBpAllocateStrategy: Controller starting up!");
                        addChannelBferToSDBslSiMap(subdomainValue, teBsl, teSi, channel, bferList);
                        return true;
                    }
                }
            }
            LOG.info("Start first time allocate bp for subdomain {} !", subdomainValue);
            BierTopology bierTopology = getBierTopology(TOPOLOGY_ID);
            List<BierLink> currentLinks = queryTeSubdomainLink(channel.getSubDomainId(), bierTopology);
            List<BierNode> currentNodes = queryTeSubdomainNode(channel.getSubDomainId(), bierTopology);
            getUniDirectionalLinks(currentLinks);

            if (!LISTENER_OPENNED) {
                LOG.info("Start listener for te subdomain change of bier topology.");
                BierNodeTeSubdomainChangeListener teSubdomainChangeListener = new BierNodeTeSubdomainChangeListener();
                dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                        LogicalDatastoreType.CONFIGURATION, teSubdomainChangeListener.getTeSubdomainId()),
                        teSubdomainChangeListener);
                LISTENER_OPENNED = true;
            }

            TeDomain teDomain = getTeDomainFromChannel(channel);
            TeSubDomain teSubDomain = getTeSubDomainFromChannel(channel);
            if (!allocateBPToBierTopology(TOPOLOGY_ID, teDomain, teSubDomain, teBsl, teSi, currentLinks,
                    null, currentNodes)) {
                LOG.info("First time allocate bps for te subdomain {} failed!", subdomainValue);
                return false;
            }
            LOG.info("First time allocate bps for te subdomain {} success!", subdomainValue);
            subdomainDeployedChannel.add(subdomainValue);
            //Persistence
            if (!addSubdomainDeployedChannel(subdomainValue)) {
                return false;
            }
        }

        addChannelBferToSDBslSiMap(subdomainValue, teBsl, teSi, channel, bferList);
        return true;
    }

    private void addChannelBferToSDBslSiMap(Integer subdomainValue, TeBsl teBsl, TeSi teSi, Channel channel,
                                            List<Bfer> bferList) {
        LOG.info("Add channel-bfer to <subdomain bsl si> map.");
        SubdomainBslSi subdomainBslSi = new SubdomainBslSi(subdomainValue, teBsl, teSi);
        for (Bfer bfer:bferList) {
            ChannelNameBferNodeId channelBfer = new ChannelNameBferNodeId(channel.getName(), bfer.getBferNodeId());
            channelBferSubdomainBslSiMap.put(channelBfer, subdomainBslSi);
            //Persistence to datastore
            addDeleteChannelBferSubdomainBslSiMap(channel.getName(), bfer.getBferNodeId(), subdomainValue,
                    teBsl, teSi, ADD);
        }
    }

    @Override
    public boolean recycleBPs(Channel channel, List<Bfer> bferList) {
        LOG.info("Delete channel-bfer to <subdomain bsl si> map.");
        for (Bfer bfer:bferList) {
            ChannelNameBferNodeId channelBfer = new ChannelNameBferNodeId(channel.getName(), bfer.getBferNodeId());
            channelBferSubdomainBslSiMap.remove(channelBfer);
            //Persistence to Datastore
            addDeleteChannelBferSubdomainBslSiMap(channel.getName(), bfer.getBferNodeId(), null, null, null, DELETE);
        }
        return true;
    }

    public boolean allocateBPToBierTopology(String topologyId, TeDomain teDomain, TeSubDomain teSubDomain,
                                            TeBsl teBsl, TeSi teSi, List<BierLink> linkListAdd,
                                            List<BierLink> linkListDelete, List<BierNode> nodeListAdd) {


        if (!addBierLinks(topologyId, teBsl, teSi, linkListAdd,teDomain,teSubDomain)) {
            LOG.info("Add bierlinks bp failed!");
            return false;
        }

        if (!deleteBierLinks(topologyId,teBsl,teSi,linkListDelete,teDomain,teSubDomain)) {
            LOG.info("Delete bierlinks bp failed!");
            return false;
        }

        if (!addBierNodes(topologyId,teBsl,teSi,nodeListAdd,teDomain,teSubDomain)) {
            LOG.info("Add biernodes bp failed!");
            return false;
        }

        return true;
    }

    private boolean addBierLinks(String topologyId, TeBsl teBsl, TeSi teSi,
                                 List<BierLink> linkListAdd, TeDomain teDomain, TeSubDomain teSubDomain) {
        if (null == linkListAdd || linkListAdd.isEmpty()) {
            return true;
        }

        int subdomainValue = teSubDomain.getSubDomainId().getValue();

        for (BierLink bierLink:linkListAdd) {
            TeBp teBp = checkAndGetOneBP(teSubDomain.getSubDomainId().getValue(),teBsl,teSi);
            if (null == teBp) {
                LOG.info("Get new bp for add bierlink {} of subdomain {} failed!", bierLink, subdomainValue);
                return false;
            }

            if (!addSrcOrDstOfBierLink(topologyId, bierLink, teDomain, teSubDomain, teBsl, teSi, teBp, SRC_NODE)) {
                LOG.info("Add src bp of added bierlink {} to datastore failed!", bierLink);
                return false;
            }

            if (!addSrcOrDstOfBierLink(topologyId, bierLink, teDomain, teSubDomain, teBsl, teSi, teBp, DST_NODE)) {
                LOG.info("Add dst bp of added bierlink {} to datastore failed!", bierLink);
                return false;
            }

            String srcNode = bierLink.getLinkSource().getSourceNode();
            String dstNode = bierLink.getLinkDest().getDestNode();
            SrcDstConnected srcDstConnected = new SrcDstConnected(srcNode, dstNode);
            linkConnectedBpMap.get(subdomainValue).put(srcDstConnected, teBp);
            //Persistence
            if (!addDeleteLinkConnectedBpMap(subdomainValue, srcNode, dstNode, teBp.getBitposition(), ADD)) {
                return false;
            }

        }

        return true;
    }

    private boolean addSrcOrDstOfBierLink(String topologyId, BierLink bierLink, TeDomain teDomain,
                                          TeSubDomain teSubDomain, TeBsl teBsl, TeSi teSi, TeBp teBp, int type) {
        String nodeId;
        String tpId;
        if (type == SRC_NODE) {
            nodeId = bierLink.getLinkSource().getSourceNode();
            tpId = bierLink.getLinkSource().getSourceTp();
        } else {
            nodeId = bierLink.getLinkDest().getDestNode();
            tpId = bierLink.getLinkDest().getDestTp();
        }

        TeBp srcOrDstTeBp = getTeBp(TOPOLOGY_ID, nodeId, teDomain.getDomainId(), teSubDomain.getSubDomainId(),
                teBsl.getBitstringlength(), teSi.getSi(), tpId);
        if (null != srcOrDstTeBp) {
            LOG.info("Delete bp of tpId {} of node {} of add bierlink {}.", tpId, nodeId, bierLink.getLinkId());
            if (!deleteOneBpFromNode(topologyId, teDomain.getDomainId(), teSubDomain.getSubDomainId(),
                    teBsl.getBitstringlength(), teSi.getSi(), nodeId, tpId)) {
                return false;
            }
        }
        TeBpBuilder teBpBuilder = new TeBpBuilder(teBp);
        teBpBuilder.setTpId(tpId);
        teBpBuilder.setKey(new TeBpKey(tpId));
        teBp = teBpBuilder.build();
        int subdomainValue = teSubDomain.getSubDomainId().getValue();
        List<String> tpList = nodeConnectedTpIdMap.get(subdomainValue).get(nodeId);
        if (null == tpList) {
            tpList = new ArrayList<>();
            nodeConnectedTpIdMap.get(subdomainValue).put(nodeId, tpList);
        }
        tpList.add(tpId);
        //Persistence
        if (!addDeleteNodeConnectedTpIdMap(subdomainValue, nodeId, tpId, ADD)) {
            return false;
        }
        LOG.info("Configure bp of tpId {} of node {} of add bierlink {}", tpId, nodeId, bierLink.getLinkId());
        if (!configureOneBPToNode(topologyId, nodeId, teDomain, teSubDomain, teBsl, teSi, teBp)) {
            return false;
        }

        LOG.info("Test if src or dst node of add bierlink {} has only connected type adjacency.", bierLink.getLinkId());
        BierNode srcBierNode = getBierNodeById(TOPOLOGY_ID, nodeId);
        if (nodeConnectedTpIdMap.get(subdomainValue).get(nodeId).size() ==  srcBierNode.getBierTerminationPoint()
                .size()) {
            teBp = nodeLocalDecapBpMap.get(subdomainValue).get(nodeId);
            if (null != teBp) {
                LOG.info("Recycle local-decap bp of node {}", nodeId);
                recycleBslSiBp(teSubDomain.getSubDomainId().getValue(), teBsl, teSi, teBp);
                nodeLocalDecapBpMap.get(subdomainValue).remove(nodeId);
                //Persistence
                if (!addDeleteNodeLocalDecapBpMap(subdomainValue, nodeId, teBp.getBitposition(),
                        teBp.getTpId(), DELETE)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean deleteBierLinks(String topologyId, TeBsl teBsl, TeSi teSi,
                                    List<BierLink> linkListDelete, TeDomain teDomain, TeSubDomain teSubDomain) {
        if (null == linkListDelete || linkListDelete.isEmpty()) {
            return true;
        }

        int subdomainValue = teSubDomain.getSubDomainId().getValue();

        for (BierLink bierLink:linkListDelete) {
            LOG.info("Remove the bp of deleted bierlink {} of subdomain {} from datastore.", bierLink.getLinkId(),
                    subdomainValue);

            if (!deleteSrcOrDstOfBierLink(topologyId, bierLink, teDomain, teSubDomain, teBsl, teSi, SRC_NODE)) {
                LOG.info("Delete src bp of deleted bierlink {} from datastore failed!", bierLink);
                return false;
            }

            if (!deleteSrcOrDstOfBierLink(topologyId, bierLink, teDomain, teSubDomain, teBsl, teSi, DST_NODE)) {
                LOG.info("Delete dst bp of deleted bierlink {} from datastore failed!", bierLink);
                return false;
            }

            String srcNode = bierLink.getLinkSource().getSourceNode();
            String dstNode = bierLink.getLinkDest().getDestNode();
            SrcDstConnected srcDstConnected = new SrcDstConnected(srcNode, dstNode);
            TeBp teBp = linkConnectedBpMap.get(subdomainValue).get(srcDstConnected);
            LOG.info("Recycle bp of deleted bierlink {} of subdomain {}.", bierLink.getLinkId(), subdomainValue);
            recycleBslSiBp(teSubDomain.getSubDomainId().getValue(), teBsl, teSi, teBp);
            linkConnectedBpMap.get(subdomainValue).remove(srcDstConnected);
            //Persistence
            if (!addDeleteLinkConnectedBpMap(subdomainValue, srcNode, dstNode, teBp.getBitposition(), DELETE)) {
                return false;
            }
        }

        return true;

    }

    private boolean deleteSrcOrDstOfBierLink(String topologyId, BierLink bierLink, TeDomain teDomain, TeSubDomain
            teSubDomain, TeBsl teBsl, TeSi teSi, int type) {

        int subdomainValue = teSubDomain.getSubDomainId().getValue();
        String nodeId;
        String tpId;
        if (type == SRC_NODE) {
            nodeId = bierLink.getLinkSource().getSourceNode();
            tpId = bierLink.getLinkSource().getSourceTp();
        } else {
            nodeId = bierLink.getLinkDest().getDestNode();
            tpId = bierLink.getLinkDest().getDestTp();
        }


        TeBsl srcTeBsl = queryTeBsl(topologyId, nodeId, teDomain.getDomainId(), teSubDomain.getSubDomainId(),
                teBsl.getBitstringlength());
        if (null == srcTeBsl) {
            LOG.info("Bp of tpId {} of node {} has been removed from datastore.", tpId, nodeId);
            TeBp teBp = nodeLocalDecapBpMap.get(subdomainValue).get(nodeId);
            if (teBp != null) {
                LOG.info("Recycle bp of subdomain {} of node {}.", subdomainValue, nodeId);
                recycleBslSiBp(teSubDomain.getSubDomainId().getValue(), teBsl, teSi, teBp);
                nodeLocalDecapBpMap.get(subdomainValue).remove(nodeId);
                //Persistence
                if (!addDeleteNodeLocalDecapBpMap(subdomainValue, nodeId, teBp.getBitposition(),
                        teBp.getTpId(), DELETE)) {
                    return false;
                }
                nodeConnectedTpIdMap.get(subdomainValue).remove(nodeId);
                //Persistence
                if (!addDeleteNodeConnectedTpIdMap(subdomainValue, nodeId, tpId, 0)) {
                    return false;
                }
            }
        } else {
            LOG.info("Remove bp of tpId {} of node {} of deleted bierlink {} of subdomain {} from datastore.", tpId,
                    nodeId, bierLink.getLinkId(), subdomainValue);
            if (!deleteOneBpFromNode(topologyId, teDomain.getDomainId(), teSubDomain.getSubDomainId(),
                    teBsl.getBitstringlength(), teSi.getSi(), nodeId, tpId)) {
                return false;
            }
            TeBp teBp = nodeLocalDecapBpMap.get(subdomainValue).get(nodeId);
            if (null == teBp) {
                LOG.info("Get bp for new local-decap adjacency of node {}.", nodeId);
                teBp = checkAndGetOneBP(teSubDomain.getSubDomainId().getValue(), teBsl, teSi);
                if (null == teBp) {
                    LOG.info("Get bp for new local-decap adjacency failed.");
                    return false;
                }
                TeBpBuilder teBpBuilder = new TeBpBuilder(teBp);
                teBpBuilder.setTpId(tpId);
                teBpBuilder.setKey(new TeBpKey(tpId));
                teBp = teBpBuilder.build();
                nodeLocalDecapBpMap.get(subdomainValue).put(nodeId, teBp);
                //Persistence
                if (!addDeleteNodeLocalDecapBpMap(subdomainValue, nodeId, teBp.getBitposition(), teBp.getTpId(), ADD)) {
                    return false;
                }
            } else {
                TeBpBuilder teBpBuilder = new TeBpBuilder(teBp);
                teBpBuilder.setTpId(tpId);
                teBpBuilder.setKey(new TeBpKey(tpId));
                teBp = teBpBuilder.build();
            }
            if (!configureOneBPToNode(topologyId,nodeId,teDomain,teSubDomain,teBsl,teSi,teBp)) {
                LOG.info("Configure local-decap bp for src or dst node of deleted bierlink failed!");
                return false;
            }
            nodeConnectedTpIdMap.get(subdomainValue).get(nodeId).remove(tpId);
            //Persistence
            if (!addDeleteNodeConnectedTpIdMap(subdomainValue, nodeId, tpId, DELETE)) {
                return false;
            }
        }

        return true;
    }

    private boolean addBierNodes(String topologyId, TeBsl teBsl, TeSi teSi,
                                 List<BierNode> nodeListAdd, TeDomain teDomain, TeSubDomain teSubDomain) {
        if (null == nodeListAdd || nodeListAdd.isEmpty()) {
            return true;
        }

        int subdomainId = teSubDomain.getSubDomainId().getValue();

        for (BierNode bierNode:nodeListAdd) {
            List<String> tpIdList = new ArrayList<>();
            for (BierTerminationPoint bierTerminationPoint:bierNode.getBierTerminationPoint()) {
                String tpId = bierTerminationPoint.getTpId();
                tpIdList.add(tpId);
            }
            if (null != nodeConnectedTpIdMap.get(subdomainId).get(bierNode.getNodeId())) {
                tpIdList.removeAll(nodeConnectedTpIdMap.get(subdomainId).get(bierNode.getNodeId()));
            }
            if (!tpIdList.isEmpty()) {
                LOG.info("Get bp for all local-decap adjacencies of for node {}.", bierNode.getNodeId());
                TeBp teBp = checkAndGetOneBP(subdomainId,teBsl,teSi);
                if (null == teBp) {
                    LOG.info("Get bp for new local-decap adjacency failed.");
                    return false;
                }
                for (String tpId:tpIdList) {
                    TeBpBuilder teBpBuilder = new TeBpBuilder(teBp);
                    teBpBuilder.setKey(new TeBpKey(tpId));
                    teBpBuilder.setTpId(tpId);
                    teBp = teBpBuilder.build();
                    if (!configureOneBPToNode(topologyId,bierNode.getNodeId(),teDomain,teSubDomain,teBsl,teSi,teBp)) {
                        LOG.info("Configure bp for tpId {} of node {} to datastore failed.", tpId, bierNode);
                        return false;
                    }
                }
                nodeLocalDecapBpMap.get(subdomainId).put(bierNode.getNodeId(), teBp);
                //Persistence
                if (!addDeleteNodeLocalDecapBpMap(subdomainId, bierNode.getNodeId(), teBp.getBitposition(),
                        teBp.getTpId(), ADD)) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    protected TeBsl checkAndGetOneBsl(Integer subdomainid) {
        int bslValue;
        List<Integer> bslList = bslUsedInSubdomain.get(subdomainid);
        if (null != bslList) {
            bslValue = bslList.get(0);
        } else {
            LOG.info("Init data structures for subdomain {}.", subdomainid);
            Map<String, List<String>> connectedTpIdMap = new HashMap<>();
            nodeConnectedTpIdMap.put(subdomainid, connectedTpIdMap);
            //Persistence
            if (!addDeleteNodeConnectedTpIdMap(subdomainid, null, null , ADD)) {
                return null;
            }
            Map<String, TeBp> localDecapBpMap = new HashMap<>();
            nodeLocalDecapBpMap.put(subdomainid, localDecapBpMap);
            if (!addDeleteNodeLocalDecapBpMap(subdomainid, null, null, null, ADD)) {
                return null;
            }
            Map<SrcDstConnected, TeBp> connectedBpMap = new HashMap<>();
            linkConnectedBpMap.put(subdomainid, connectedBpMap);
            if (!addDeleteLinkConnectedBpMap(subdomainid, null, null, null, ADD)) {
                return null;
            }
            List<BierLink> tmpBierLinkListAdd = new ArrayList<>();
            tmpBierLinkAdd.put(subdomainid, tmpBierLinkListAdd);
            List<BierLink> tmpBierLinkListDelete = new ArrayList<>();
            tmpBierLinkDelete.put(subdomainid, tmpBierLinkListDelete);

            bslValue = calBslValueBasedOnTopoSize();
            if (bslValue == -1) {
                return null;
            }
            bslList = new ArrayList<>();
            bslList.add(bslValue);
            //Persistence to datastore
            if (!addDeleteBslUsedInSubdomain(subdomainid, bslValue, ADD)) {
                return null;
            }
            bslUsedInSubdomain.put(subdomainid,bslList);
            LOG.info("Got new bsl {} for subdomain {}.", bslValue, subdomainid);
        }

        TeBslBuilder teBslBuilder = new TeBslBuilder();
        teBslBuilder.setKey(new TeBslKey(bslValueMap.get(bslValue)));
        teBslBuilder.setBitstringlength(bslValueMap.get(bslValue));
        return teBslBuilder.build();
    }

    private int calBslValueBasedOnTopoSize() {
        LOG.info("Calculate BSL based on topology size and recommend bsl!");
        BierTopology bierTopology = getBierTopology(TOPOLOGY_ID);
        int topoSize = bierTopology.getBierNode().size() + bierTopology.getBierLink().size() / 2;
        LOG.info("Init topology size:" + topoSize);
        int recommendBsl = readRecommendBsl(TOPOLOGY_ID);
        if (recommendBsl < topoSize * 1.2) {
            NotificationProvider.getInstance().notifyFailureReason("Topology size is larger than recommended bsl!");
            LOG.info("Calculate BSL based topology size failed!");
            return -1;
        }
        while (recommendBsl > topoSize * 1.2 && recommendBsl >= 64) {
            recommendBsl = recommendBsl >>> 1;
        }
        recommendBsl = recommendBsl >> 4;
        int bslValue = 0;
        while (recommendBsl != 1) {
            recommendBsl = recommendBsl >> 1;
            bslValue ++;
        }

        return bslValue;
    }

    @Override
    protected TeSi checkAndGetOneSi(Integer subdomainId, TeBsl teBsl) {
        int siValue = 1;
        SubdomainBsl subdomainBsl = new SubdomainBsl(subdomainId, teBsl);
        List<Integer> siList = siUsedInSubdomainBsl.get(subdomainBsl);
        if (null != siList) {
            siValue = siList.get(0);
        } else {
            List<Integer> siUsedByManual = getUsedSiInSubdomainBsl(TOPOLOGY_ID, subdomainId,
                    teBsl.getBitstringlength().getIntValue());
            if (null == siUsedByManual) {
                LOG.info("Read <subdomain bsl si> from bp allocate datastore failed!");
                return null;
            }
            LOG.info("Si used by manual {}.", siUsedByManual);
            while (siUsedByManual.contains(siValue)) {
                siValue++;
            }
            siList = new ArrayList<>();
            siList.add(siValue);
            //Persistence to datastore
            if (!addDeleteSiUsedInSubdomainBsl(subdomainId,teBsl,siValue,ADD)) {
                return null;
            }
            siUsedInSubdomainBsl.put(subdomainBsl,siList);
            if (!addUsedSubdomainBslSi(TOPOLOGY_ID, subdomainId, teBsl.getBitstringlength().getIntValue(), siValue)) {
                LOG.info("Add <subdomain bsl si> to bp allocate datastore failed!");
                return null;
            }
            LOG.info("Got new si {} for subdomain {}.", siValue, subdomainId);
        }
        TeSiBuilder teSiBuilder = new TeSiBuilder();
        teSiBuilder.setKey(new TeSiKey(new Si(siValue)));
        teSiBuilder.setSi(new Si(siValue));
        return teSiBuilder.build();
    }

    @Override
    protected TeBp checkAndGetOneBP(Integer subdomainId, TeBsl teBsl, TeSi teSi) {
        SubdomainBslSi subdomainBslSi = new SubdomainBslSi(subdomainId,teBsl,teSi);
        List<Integer> bpListInBslSi = bpUsedInSubdomainBslSi.get(subdomainBslSi);
        if (null == bpListInBslSi) {
            bpListInBslSi = new ArrayList<>();
            bpUsedInSubdomainBslSi.put(subdomainBslSi,bpListInBslSi);
        }
        int bsl = teBsl.getBitstringlength().getIntValue();
        bsl = bsl << (bsl + 5);
        if (bpListInBslSi.size() + 1 > bsl) {
            LOG.info("Bp overflow!");
            return null;
        }

        int bp = 1;
        while (bpListInBslSi.contains(bp)) {
            bp++;
        }
        LOG.info("Got new bp: {}", bp);
        bpListInBslSi.add(bp);
        //Persistence to datastore
        if (!addDeleteBpUsedInSubdomainBslSi(subdomainId, teBsl, teSi, bp, ADD)) {
            return null;
        }
        TeBpBuilder teBpBuilder = new TeBpBuilder();
        teBpBuilder.setBitposition(bp);
        return teBpBuilder.build();
    }

    @Override
    protected boolean recycleBslSiBp(Integer subdomainId, TeBsl teBsl, TeSi teSi, TeBp teBp) {
        SubdomainBslSi subdomainBslSi = new SubdomainBslSi(subdomainId, teBsl, teSi);
        List<Integer> teBpUsedList = bpUsedInSubdomainBslSi.get(subdomainBslSi);
        teBpUsedList.remove(teBp.getBitposition());
        //Persistence to datastore
        if (!addDeleteBpUsedInSubdomainBslSi(subdomainId, teBsl, teSi, teBp.getBitposition(), DELETE)) {
            return false;
        }
        return true;
    }

    public void getUniDirectionalLinks(List<BierLink> bierLinkList) {
        for (int i = 0;i < bierLinkList.size();i++) {
            BierLink bierLink = bierLinkList.get(i);
            for (int j = i + 1;j < bierLinkList.size();j++) {
                BierLink otherLink = bierLinkList.get(j);
                if (bierLink.getLinkSource().getSourceNode().equals(otherLink.getLinkDest().getDestNode())
                        && bierLink.getLinkDest().getDestNode().equals(otherLink.getLinkSource().getSourceNode())) {
                    bierLinkList.remove(otherLink);
                }
            }
        }
    }


    public static TopoBasedBpAllocateStrategy getInstance() {
        return instance;
    }

    private class SrcDstConnected {
        private String srcNode;
        private String dstNode;

        SrcDstConnected(String srcNode, String dstNode) {
            this.srcNode = srcNode;
            this.dstNode = dstNode;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            SrcDstConnected other = (SrcDstConnected)obj;
            boolean sameLink = this.srcNode.equals(other.srcNode) && this.dstNode.equals(other.dstNode);
            boolean reverseLink = this.srcNode.equals(other.dstNode) && this.dstNode.equals(other.srcNode);
            return sameLink || reverseLink;
        }
    }

    private boolean addDeleteNodeConnectedTpIdMap(int subdomainId, String nodeId, String tpId, int type) {
        InstanceIdentifier<NodeConnectedTpIdMap> connectedTpIdMapIID = getStrategyPersisPath()
                .child(NodeConnectedTpIdMap.class, new NodeConnectedTpIdMapKey(subdomainId));
        InstanceIdentifier<NodeTpId> nodeTpIdIID = connectedTpIdMapIID.child(NodeTpId.class, new NodeTpIdKey(nodeId));
        InstanceIdentifier<ConnectedTpId> connectedTpIdIID = nodeTpIdIID.child(ConnectedTpId.class,
                new ConnectedTpIdKey(tpId));
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        switch (type) {
            case ADD:
                if (null == nodeId) {
                    NodeConnectedTpIdMapBuilder builder = new NodeConnectedTpIdMapBuilder();
                    builder.setSubdomainId(subdomainId);
                    builder.setKey(new NodeConnectedTpIdMapKey(subdomainId));
                    List<NodeTpId> nodeTpIdList = new ArrayList<>();
                    builder.setNodeTpId(nodeTpIdList);
                    tx.put(LogicalDatastoreType.CONFIGURATION, connectedTpIdMapIID, builder.build(), true);
                } else {
                    ConnectedTpIdBuilder builder = new ConnectedTpIdBuilder();
                    builder.setKey(new ConnectedTpIdKey(tpId));
                    builder.setTpId(tpId);
                    tx.put(LogicalDatastoreType.CONFIGURATION, connectedTpIdIID, builder.build(), true);
                }
                if (!txSubmit(tx, "Add node connected tpId map failed!")) {
                    return false;
                }
                break;
            case DELETE:
                tx.delete(LogicalDatastoreType.CONFIGURATION, connectedTpIdIID);
                if (!txSubmit(tx, "Delete node connected tpId map failed!")) {
                    return false;
                }
                break;
            default:
                tx.delete(LogicalDatastoreType.CONFIGURATION, nodeTpIdIID);
                if (!txSubmit(tx, "Delete node id failed!")) {
                    return false;
                }
        }
        return true;
    }

    private boolean addDeleteNodeLocalDecapBpMap(int subdomainId, String nodeId, Integer bp, String tpId, int type) {
        InstanceIdentifier<NodeLocalDecapBpMap> localDecapBpMapIID = getStrategyPersisPath().child(
                NodeLocalDecapBpMap.class, new NodeLocalDecapBpMapKey(subdomainId));
        InstanceIdentifier<NodeBp> nodeBpIID = localDecapBpMapIID.child(NodeBp.class, new NodeBpKey(nodeId));
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        switch (type) {
            case ADD:
                if (null == nodeId) {
                    NodeLocalDecapBpMapBuilder builder = new NodeLocalDecapBpMapBuilder();
                    builder.setSubdomainId(subdomainId);
                    builder.setKey(new NodeLocalDecapBpMapKey(subdomainId));
                    List<NodeBp> nodeBpList = new ArrayList<>();
                    builder.setNodeBp(nodeBpList);
                    tx.put(LogicalDatastoreType.CONFIGURATION, localDecapBpMapIID, builder.build(), true);
                } else {
                    NodeBpBuilder builder = new NodeBpBuilder();
                    builder.setBp(bp);
                    builder.setTpId(tpId);
                    builder.setKey(new NodeBpKey(nodeId));
                    builder.setNodeId(nodeId);
                    tx.put(LogicalDatastoreType.CONFIGURATION, nodeBpIID, builder.build(), true);
                }
                if (!txSubmit(tx, "Add node local decap bp failed!")) {
                    return false;
                }
                break;
            case DELETE:
                tx.delete(LogicalDatastoreType.CONFIGURATION, nodeBpIID);
                if (!txSubmit(tx, "Delete node local decap bp failed!")) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean addDeleteLinkConnectedBpMap(int subdomainId, String srcNode, String dstNode, Integer bp, int type) {
        InstanceIdentifier<LinkConnectedBpMap> connectedBpMapIID = getStrategyPersisPath().child(
                LinkConnectedBpMap.class, new LinkConnectedBpMapKey(subdomainId));
        InstanceIdentifier<LinkBp> linkBpIID = connectedBpMapIID.child(LinkBp.class, new LinkBpKey(dstNode, srcNode));

        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        switch (type) {
            case ADD:
                if (null == srcNode) {
                    LinkConnectedBpMapBuilder builder = new LinkConnectedBpMapBuilder();
                    builder.setSubdomainId(subdomainId);
                    builder.setKey(new LinkConnectedBpMapKey(subdomainId));
                    List<LinkBp> linkBpList = new ArrayList<>();
                    builder.setLinkBp(linkBpList);
                    tx.put(LogicalDatastoreType.CONFIGURATION, connectedBpMapIID, builder.build(), true);
                } else {
                    LinkBpBuilder builder = new LinkBpBuilder();
                    builder.setBp(bp);
                    builder.setSrcNode(srcNode);
                    builder.setDstNode(dstNode);
                    builder.setKey(new LinkBpKey(dstNode, srcNode));
                    tx.put(LogicalDatastoreType.CONFIGURATION, linkBpIID, builder.build(), true);
                }
                if (!txSubmit(tx, "Add link connected bp failed!")) {
                    return false;
                }
                break;
            case DELETE:
                try {
                    Optional<LinkBp> optional = tx.read(LogicalDatastoreType.CONFIGURATION, linkBpIID).checkedGet();
                    if (!optional.isPresent()) {
                        LOG.info("Find reverse link in datastore!");
                        linkBpIID = getStrategyPersisPath().child(LinkConnectedBpMap.class, new LinkConnectedBpMapKey(
                                subdomainId)).child(LinkBp.class, new LinkBpKey(srcNode, dstNode));
                    }
                } catch (ReadFailedException e) {
                    LOG.info(e.getStackTrace().toString());
                }
                tx.delete(LogicalDatastoreType.CONFIGURATION, linkBpIID);
                if (!txSubmit(tx, "Delete link connected bp failed!")) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean addSubdomainDeployedChannel(int subdomainId) {
        InstanceIdentifier<SubdomainDeployedChannel> subdomainDeployedChannelIID = getStrategyPersisPath()
                .child(SubdomainDeployedChannel.class, new SubdomainDeployedChannelKey(subdomainId));
        ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        SubdomainDeployedChannelBuilder builder = new SubdomainDeployedChannelBuilder();
        builder.setSubdomainId(subdomainId);
        builder.setKey(new SubdomainDeployedChannelKey(subdomainId));
        tx.put(LogicalDatastoreType.CONFIGURATION, subdomainDeployedChannelIID, builder.build(), true);
        if (!txSubmit(tx, "Add subdomain deployed channel failed!")) {
            return false;
        }
        return true;
    }

    public boolean resumeTopoBasedDataStructures() {
        StrategyDataPersistence strategyDataPersistence = queryStrategyData();
        if (null == strategyDataPersistence) {
            return false;
        }
        if (null == strategyDataPersistence.getSubdomainDeployedChannel()) {
            LOG.info("First time start up controller!");
            return true;
        }

        LOG.info("Resume node connected tpId map!");
        List<NodeConnectedTpIdMap> nodeConnectedTpIdMaps = strategyDataPersistence.getNodeConnectedTpIdMap();
        if (null == nodeConnectedTpIdMaps) {
            LOG.info("No node connected tpId data found!");
        } else {
            for (NodeConnectedTpIdMap nodeConnectedMap:nodeConnectedTpIdMaps) {
                int sudomainId = nodeConnectedMap.getSubdomainId();
                Map<String, List<String>> nodeTpIds = new HashMap<>();
                for (NodeTpId nodeTpId:nodeConnectedMap.getNodeTpId()) {
                    String nodeId = nodeTpId.getNodeId();
                    List<String> tpIds = new ArrayList<>();
                    for (ConnectedTpId connectedTpId:nodeTpId.getConnectedTpId()) {
                        tpIds.add(connectedTpId.getTpId());
                    }
                    nodeTpIds.put(nodeId, tpIds);
                }
                nodeConnectedTpIdMap.put(sudomainId, nodeTpIds);
            }
        }

        LOG.info("Resume node local decap bp map!");
        List<NodeLocalDecapBpMap> nodeLocalDecapBpMaps = strategyDataPersistence.getNodeLocalDecapBpMap();
        if (null == nodeConnectedTpIdMaps) {
            LOG.info("No node local decap bp map data found!");
        } else {
            for (NodeLocalDecapBpMap noLocalDecapBp:nodeLocalDecapBpMaps) {
                int subdomainId = noLocalDecapBp.getSubdomainId();
                Map<String, TeBp> nodeBpMap = new HashMap<>();
                for (NodeBp nodeBp:noLocalDecapBp.getNodeBp()) {
                    TeBpBuilder teBpBuilder = new TeBpBuilder();
                    teBpBuilder.setBitposition(nodeBp.getBp());
                    teBpBuilder.setTpId(nodeBp.getTpId());
                    teBpBuilder.setKey(new TeBpKey(nodeBp.getTpId()));
                    String nodeId = nodeBp.getNodeId();
                    nodeBpMap.put(nodeId, teBpBuilder.build());
                }
                nodeLocalDecapBpMap.put(subdomainId, nodeBpMap);
            }
        }

        LOG.info("Resume link connected bp map!");
        List<LinkConnectedBpMap> linkConnectedBpMaps = strategyDataPersistence.getLinkConnectedBpMap();
        if (null == linkConnectedBpMaps) {
            LOG.info("No link connected bp map data found!");
        } else {
            for (LinkConnectedBpMap linkConnectedBp:linkConnectedBpMaps) {
                int subdomainId = linkConnectedBp.getSubdomainId();
                Map<SrcDstConnected, TeBp> linkBpMap = new HashMap<>();
                for (LinkBp linkBp:linkConnectedBp.getLinkBp()) {
                    String srcNode = linkBp.getSrcNode();
                    String dstNode = linkBp.getDstNode();
                    SrcDstConnected srcDstConnected = new SrcDstConnected(srcNode, dstNode);
                    TeBpBuilder teBpBuilder = new TeBpBuilder();
                    teBpBuilder.setBitposition(linkBp.getBp());
                    linkBpMap.put(srcDstConnected, teBpBuilder.build());
                }
                linkConnectedBpMap.put(subdomainId, linkBpMap);
            }
        }

        LOG.info("Resume subdomain deployed channel and tmpBierLinkChanged map!");
        List<SubdomainDeployedChannel> sdDeployedChannels = strategyDataPersistence.getSubdomainDeployedChannel();
        if (null == sdDeployedChannels) {
            LOG.info("No subdomain deployed channel data found!");
        } else {
            for (SubdomainDeployedChannel sdDeployedChannel:sdDeployedChannels) {
                subdomainDeployedChannel.add(sdDeployedChannel.getSubdomainId());
                List<BierLink> tmpBierLinkListAdd = new ArrayList<>();
                tmpBierLinkAdd.put(sdDeployedChannel.getSubdomainId(), tmpBierLinkListAdd);
                List<BierLink> tmpBierLinkListDelete = new ArrayList<>();
                tmpBierLinkDelete.put(sdDeployedChannel.getSubdomainId(), tmpBierLinkListDelete);
            }
        }

        LOG.info("Resume listener for tesubdomain!");
        BierNodeTeSubdomainChangeListener teSubdomainChangeListener = new BierNodeTeSubdomainChangeListener();
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                        LogicalDatastoreType.CONFIGURATION, teSubdomainChangeListener.getTeSubdomainId()),
                teSubdomainChangeListener);
        LISTENER_OPENNED = true;

        return true;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        TopoBasedBpAllocateStrategy other = (TopoBasedBpAllocateStrategy)obj;
        boolean bslUsedInSubdomainMapEquals = this.bslUsedInSubdomain.equals(other.bslUsedInSubdomain);
        boolean siUsedInSubdomainBslMapEquals = this.siUsedInSubdomainBsl.equals(other.siUsedInSubdomainBsl);
        boolean bpUsedInSubdomainBslSiMapEquals = this.bpUsedInSubdomainBslSi.equals(other.bpUsedInSubdomainBslSi);
        boolean bferListOfChannelMapEquals = this.bferListOfChannel.equals(other.bferListOfChannel);
        boolean channelBferSubdomainBslSiMapEquals = this.channelBferSubdomainBslSiMap.equals(other
                .channelBferSubdomainBslSiMap);
        boolean nodeConnectedTpIdMapEquals = this.nodeConnectedTpIdMap.equals(other.nodeConnectedTpIdMap);
        boolean nodeLocalDecapBpMapEquals = this.nodeLocalDecapBpMap.equals(other.nodeLocalDecapBpMap);
        boolean linkConnectedBpMapEquals = this.linkConnectedBpMap.equals(other.linkConnectedBpMap);
        boolean subdomainDeployedChannelEquals = this.subdomainDeployedChannel.equals(other.subdomainDeployedChannel);
        return bslUsedInSubdomainMapEquals && siUsedInSubdomainBslMapEquals && bpUsedInSubdomainBslSiMapEquals
                 && channelBferSubdomainBslSiMapEquals && nodeConnectedTpIdMapEquals && linkConnectedBpMapEquals
                && subdomainDeployedChannelEquals && bferListOfChannelMapEquals && nodeLocalDecapBpMapEquals;
    }

}
