/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.teconfig;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.service.impl.NotificationProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.TeInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.routing.BierTeConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.adj.type.te.adj.type.ConnectedBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.adj.type.te.adj.type.LocalDecapBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.TeSiBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.te.si.TeFIndex;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.te.si.TeFIndexBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.TeSubdomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.te.subdomain.TeBslBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BiftInfoProcess {

    private static final Logger LOG = LoggerFactory.getLogger(BiftInfoProcess.class);
    private final DataBroker dataBroker;
    private final BierTeBiftWriter bierTeBiftWriter;
    private NotificationProvider notificationProvider;
    private static final int TE_ADJ_TYPE_CONNECTED = 1;
    private static final int TE_ADJ_TYPE_LOCALDECAP = 2;

    public BiftInfoProcess(final DataBroker dataBroker, final BierTeBiftWriter bierTeBiftWriter) {
        this.dataBroker = dataBroker;
        this.bierTeBiftWriter = bierTeBiftWriter;
        notificationProvider = new NotificationProvider();
    }

    public void processAddedFwdInfo(String homeNodeId, DomainId domainId, SubDomainId subDomainId, Bsl bsl, Si si,
                                    String homeNodeTpId, Integer homeNodeBp) {
        if (null == homeNodeId || null == domainId || null == subDomainId || null == bsl || null == si
                || null == homeNodeTpId || null == homeNodeBp) {
            return;
        }

        LOG.info("Process find bier link contains this node as source");
        BierLink bierLink = queryLinkByNodeIdAndTpId(homeNodeId, homeNodeTpId);
        if (null == bierLink) {
            LOG.info("Could not find matched bierLink");
            return;
        }

        LOG.info("Check if dest node has been configured bp");
        Integer destNodeBp = getDestNodeConfiguredBp(bierLink.getLinkDest().getDestNode(), domainId,
                subDomainId, bsl, si, bierLink.getLinkDest().getDestTp());
        if (null == destNodeBp) {
            LOG.info("The bp of dest node has not been configured, do not configure bift added info");
            return;
        }

        LOG.info("Process get in-label of home node");
        MplsLabel homeNodeInLabel = processGetInLabel(homeNodeId, domainId, subDomainId, bsl, si);

        LOG.info("Process get in-label of dest node");
        MplsLabel destNodeInLabel = processGetInLabel(bierLink.getLinkDest().getDestNode(),
                domainId, subDomainId, bsl, si);

        LOG.info("Process added dest node bift info");
        processAddedNodeBiftInfo(bierLink.getLinkDest().getDestNode(), subDomainId, bsl, si,
                bierLink.getLinkDest().getDestTp(), homeNodeBp, homeNodeInLabel, destNodeInLabel);

        LOG.info("Process added home node bift info");
        processAddedNodeBiftInfo(homeNodeId, subDomainId, bsl, si, homeNodeTpId, destNodeBp,
                destNodeInLabel, homeNodeInLabel);

    }

    public void processModifiedFwdInfo(InstanceIdentifier<TeBp> identifier, TeBp after) {

    }

    public void processDeletedFwdInfo(String homeNodeId, DomainId domainId, SubDomainId subDomainId, Bsl bsl, Si si,
                                      String homeNodeTpId, Integer homeNodeBp) {
        if (null == homeNodeId || null == domainId || null == subDomainId || null == bsl || null == si
                || null == homeNodeTpId || null == homeNodeBp) {
            return;
        }

        LOG.info("Process find bier link contains this node as source");
        BierLink bierLink = queryLinkByNodeIdAndTpId(homeNodeId, homeNodeTpId);
        if (null == bierLink) {
            LOG.info("Could not find matched bierLink");
            return;
        }

        LOG.info("Check if dest node has been configured bp");
        Integer destNodeBp = getDestNodeConfiguredBp(bierLink.getLinkDest().getDestNode(), domainId,
                subDomainId, bsl, si, bierLink.getLinkDest().getDestTp());
        if (null == destNodeBp) {
            LOG.info("The bp of dest node has not been configured, do not configure bift deleted info");
            return;
        }

        LOG.info("Process deleted dest node bift info");
        processDeletedNodeBiftInfo(bierLink.getLinkDest().getDestNode(), subDomainId, bsl, si, homeNodeBp);

        LOG.info("Process deleted home node bift info");
        processDeletedNodeBiftInfo(homeNodeId, subDomainId, bsl, si, destNodeBp);

    }

    private void processAddedNodeBiftInfo(String nodeId, SubDomainId subDomainId, Bsl bsl, Si si, String tpId,
                                          Integer nodeBp, MplsLabel nodeInLabelOne, MplsLabel nodeInLabelTwo) {
        LOG.info("Process get interface of node which will be configured bift info");
        Long nodeInterFace = processGetInterface(nodeId, tpId);
        if (null == nodeInterFace) {
            LOG.info("Interface is null");
            webSocketToApp("Interface of node: " + nodeId + " is null, configure its bift failed");
            return;
        }

        LOG.info("Process construct added fwd message");
        List<TeInfo> teInfoList = new ArrayList<>();
        TeInfo teInfo = processConstructTeInfo(TE_ADJ_TYPE_CONNECTED, subDomainId, bsl, si, nodeBp, nodeInterFace,
                nodeInLabelOne, nodeInLabelTwo);
        if (null == teInfo) {
            LOG.info("Construct added message failed");
            webSocketToApp("Construct one bift info of node: " + nodeId
                    + " failed, configure its bift failed");
            return;
        }
        teInfoList.add(teInfo);

        LOG.info("Send message to southbound");
        if (!processSendInfoToSouthbound(nodeId, teInfoList, ConfigurationType.ADD)) {
            LOG.error("Add message to SBI failed");
        }
    }

    private void processDeletedNodeBiftInfo(String nodeId, SubDomainId subDomainId, Bsl bsl, Si si,
                                            Integer nodeBp) {

        LOG.info("Process construct deleted fwd message");
        List<TeInfo> teInfoList = new ArrayList<>();
        TeInfo teInfo = processConstructTeInfo(TE_ADJ_TYPE_CONNECTED, subDomainId, bsl, si, nodeBp, null, null, null);
        if (null == teInfo) {
            LOG.info("Construct deleted message failed");
            webSocketToApp("Construct one bift info of node: " + nodeId
                    + " failed, configure its bift failed");
            return;
        }
        teInfoList.add(teInfo);

        LOG.info("Send message to southbound");
        if (!processSendInfoToSouthbound(nodeId, teInfoList, ConfigurationType.DELETE)) {
            LOG.error("Del message to SBI failed");
        }
    }



    public BierLink queryLinkByNodeIdAndTpId(String nodeId, String tpId) {
        LOG.info("Get bier topology");
        BierTopology bierTopology = getBierTopologyFromDataStore();
        if (null == bierTopology || null == bierTopology.getBierLink() || bierTopology.getBierLink().isEmpty()) {
            LOG.info("Bier link list is null or empty");
        } else {
            LOG.info("query matched link and return");
            for (BierLink bierLink : bierTopology.getBierLink()) {
                if (bierLink.getLinkSource().getSourceNode().equals(nodeId)
                        && bierLink.getLinkSource().getSourceTp().equals(tpId)) {
                    return bierLink;
                }
            }
        }
        return null;
    }

    public Integer getDestNodeConfiguredBp(String nodeId, DomainId domainId, SubDomainId subDomainId, Bsl bsl,
                                                 Si si, String destTp) {
        if (null == nodeId || null == destTp) {
            return null;
        }
        BierTopology bierTopology = getBierTopologyFromDataStore();
        for (BierNode bierNode : bierTopology.getBierNode()) {
            if (bierNode.getNodeId().equals(nodeId) && null != bierNode.getBierTeNodeParams()
                    && null != bierNode.getBierTeNodeParams().getTeDomain()
                    && !bierNode.getBierTeNodeParams().getTeDomain().isEmpty()) {
                return processTeDomainOfDestNodeToGetBp(domainId, subDomainId,
                        bierNode.getBierTeNodeParams().getTeDomain(), bsl, si, destTp);
            }
        }
        return null;
    }

    private Integer processTeDomainOfDestNodeToGetBp(DomainId domainId, SubDomainId subDomainId,
                                                     List<TeDomain> teDomainList, Bsl bsl, Si si, String destTp) {
        for (TeDomain teDomain : teDomainList) {
            if (teDomain.getDomainId().equals(domainId) && null != teDomain.getTeSubDomain()
                    && !teDomain.getTeSubDomain().isEmpty()) {
                return processTeSubDomainOfTeDomainToGetBp(subDomainId, teDomain.getTeSubDomain(), bsl, si, destTp);
            }
        }
        return null;
    }

    private Integer processTeSubDomainOfTeDomainToGetBp(SubDomainId subDomainId, List<TeSubDomain> teSubDomainList,
                                                        Bsl bsl, Si si,
                                                   String destTp) {
        for (TeSubDomain teSubDomain : teSubDomainList) {
            if (teSubDomain.getSubDomainId().equals(subDomainId) && null != teSubDomain.getTeBsl()
                    && !teSubDomain.getTeBsl().isEmpty()) {
                return processTeBslOfTeSubDomainToGetBp(teSubDomain.getTeBsl(), bsl, si, destTp);
            }
        }
        return null;
    }

    private Integer processTeBslOfTeSubDomainToGetBp(List<TeBsl> teBslList, Bsl bsl, Si si, String destTp) {
        for (TeBsl teBsl : teBslList) {
            if (teBsl.getBitstringlength().equals(bsl) && null != teBsl.getTeSi() && !teBsl.getTeSi().isEmpty()) {
                return processTeSiOfTeBslToGetBp(teBsl.getTeSi(), si, destTp);
            }
        }
        return null;
    }

    private Integer processTeSiOfTeBslToGetBp(List<TeSi> teSiList, Si si, String destTp) {
        for (TeSi teSi : teSiList) {
            if (teSi.getSi().equals(si)) {
                return getBp(teSi, destTp);
            }
        }
        return null;
    }

    private Integer getBp(TeSi teSi, String destTp) {
        if (null != teSi.getTeBp() && !teSi.getTeBp().isEmpty()) {
            for (TeBp teBp : teSi.getTeBp()) {
                if (teBp.getTpId().equals(destTp)) {
                    return teBp.getBitposition();
                }
            }
        }
        return null;
    }

    public boolean processSetLocalDecapToBfirBfer(Channel channel) {
        if (!processSetLocalDecapToBfir(channel.getDomainId(), channel.getSubDomainId(), channel.getIngressNode(),
                channel.getSrcTp())) {
            LOG.error("process set bp-te-adj-type of bfir failed");
            return false;
        }
        if (!processSetLocalDecapToBfer(channel.getDomainId(), channel.getSubDomainId(), channel.getEgressNode())) {
            LOG.error("process set bp-te-adj-type of  bfer failed");
            return false;
        }
        LOG.info("process set bp-te-adj-type of bfir and bfer succeed");
        return true;
    }

    private boolean processSetLocalDecapToBfir(DomainId domainId, SubDomainId subDomainId, String nodeId,
                                               String srcTp) {
        if (null == domainId || null == subDomainId || null == nodeId || null == srcTp) {
            return false;
        }
        BierNode bfir = getBierNodeById(nodeId);
        if (null == bfir) {
            LOG.info("Get bfir from datastore failed");
            webSocketToApp("Bfir is not in datastore");
            return false;
        }
        List<TeInfo> teInfoList = getBiftTeInfoFromInput(domainId, subDomainId,bfir,srcTp);
        return processSendInfoToSouthbound(nodeId, teInfoList, ConfigurationType.ADD);
    }

    private boolean processSetLocalDecapToBfer(DomainId domainId, SubDomainId subDomainId, List<EgressNode> bferList) {
        if (null == domainId || null == subDomainId || null == bferList || bferList.isEmpty()) {
            return false;
        }
        for (EgressNode node : bferList) {
            BierNode bfer = getBierNodeById(node.getNodeId());
            if (null == bfer) {
                LOG.info("Get bfer from datastore failed");
                webSocketToApp("Bfer: " + bfer.getNodeId() + " is not in datastore");
                return false;
            }
            for (RcvTp rcvTpId : node.getRcvTp()) {
                List<TeInfo> teInfoList = getBiftTeInfoFromInput(domainId, subDomainId,bfer,rcvTpId.getTp());
                if (!processSendInfoToSouthbound(node.getNodeId(), teInfoList, ConfigurationType.ADD)) {
                    return false;
                }
            }
        }
        return true;
    }

    private BierNode getBierNodeById(String nodeId) {
        BierTopology bierTopology = getBierTopologyFromDataStore();
        if (null == bierTopology || null == bierTopology.getBierNode() || bierTopology.getBierNode().isEmpty()) {
            LOG.info("Bier node list is null or empty");
            return null;
        }
        for (BierNode bierNode : bierTopology.getBierNode()) {
            if (bierNode.getNodeId().equals(nodeId)) {
                return bierNode;
            }
        }
        return null;
    }


    private List<TeInfo> getBiftTeInfoFromInput(DomainId domainId, SubDomainId subDomainId, BierNode node,
                                                String tpId) {

        List<TeBsl> teBslList = getBslListFromDomainInfo(domainId, subDomainId, node);
        if (null == teBslList || teBslList.isEmpty()) {
            return null;
        }
        List<TeInfo> teInfoList = new ArrayList<>();
        for (TeBsl teBsl : teBslList) {
            for (TeSi teSi : teBsl.getTeSi()) {
                for (TeBp teBp : teSi.getTeBp()) {
                    if (teBp.getTpId().equals(tpId)) {
                        TeInfo teInfo = processConstructTeInfo(BiftInfoProcess.TE_ADJ_TYPE_LOCALDECAP, subDomainId,
                                teBsl.getBitstringlength(), teSi.getSi(), teBp.getBitposition(),null,
                                null,null);
                        if (null != teInfo) {
                            teInfoList.add(teInfo);
                        }
                    }
                }
            }
        }
        return teInfoList;
    }


    private List<TeBsl>  getBslListFromDomainInfo(DomainId domainId, SubDomainId subDomainId, BierNode node) {
        for (TeDomain teDomain : node.getBierTeNodeParams().getTeDomain()) {
            for (TeSubDomain teSubDomain : teDomain.getTeSubDomain()) {
                if (teDomain.getDomainId().equals(domainId) && teSubDomain.getSubDomainId().equals(subDomainId)) {
                    return teSubDomain.getTeBsl();
                }
            }
        }
        LOG.info("Bfer {} is not exit in the domain of channel", node.getNodeId());
        webSocketToApp("Bfer: " + node.getNodeId() + " is not exit in domain "
                + domainId.toString());
        return null;
    }

    private boolean  processSendInfoToSouthbound(String nodeId, List<TeInfo> teInfoList, ConfigurationType type) {
        if (null == nodeId || null == teInfoList || teInfoList.isEmpty()) {
            return false;
        }
        LOG.info("Process send info to southbound");
        for (TeInfo teInfo : teInfoList) {
            ConfigurationResult result = bierTeBiftWriter.writeTeBift(type, nodeId, teInfo);
            if (!result.isSuccessful()) {
                webSocketToApp(result.getFailureReason());
                return false;
            }
        }
        return true;
    }


    public TeInfo processConstructTeInfo(int type, SubDomainId subDomainId, Bsl bsl, Si si, Integer bp, Long interFace,
                                                  MplsLabel homeNodeInLabel, MplsLabel destNodeInLabel) {
        TeFIndexBuilder teFIndexBuilder = new TeFIndexBuilder();
        teFIndexBuilder.setTeFIndex(new BitString(bp));
        if (TE_ADJ_TYPE_CONNECTED == type) {
            teFIndexBuilder.setTeAdjType((new ConnectedBuilder()).build());
            teFIndexBuilder.setOutLabel(homeNodeInLabel);
        } else if (TE_ADJ_TYPE_LOCALDECAP == type) {
            teFIndexBuilder.setTeAdjType((new LocalDecapBuilder()).build());
        }
        teFIndexBuilder.setFIntf(interFace);
        List<TeFIndex> teFIndexList = new ArrayList<>();
        teFIndexList.add(teFIndexBuilder.build());

        TeSiBuilder teSiBuilder = new TeSiBuilder();
        teSiBuilder.setTeFIndex(teFIndexList);
        teSiBuilder.setSi(si);
        teSiBuilder.setFtLabel(destNodeInLabel);
        List<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.TeSi> teSiList
                = new ArrayList<>();
        teSiList.add(teSiBuilder.build());

        TeBslBuilder teBslBuilder = new TeBslBuilder();
        teBslBuilder.setTeSi(teSiList);
        teBslBuilder.setFwdBsl(bsl.getIntValue());
        List<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.te.subdomain.TeBsl>
                teBslList = new ArrayList<>();
        teBslList.add(teBslBuilder.build());

        TeSubdomainBuilder subdomainBuilder = new TeSubdomainBuilder();
        subdomainBuilder.setTeBsl(teBslList);
        subdomainBuilder.setSubdomainId(subDomainId);
        List<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.TeSubdomain>
                subdomainList = new ArrayList<>();
        subdomainList.add(subdomainBuilder.build());

        BierTeConfigBuilder builder = new BierTeConfigBuilder();
        builder.setTeSubdomain(subdomainList);

        return builder.build();
    }

    public Long processGetInterface(String nodeId, String tpId) {
        if (null == tpId) {
            return null;
        }
        LOG.info("Get bier topology");
        BierTopology bierTopology = getBierTopologyFromDataStore();
        if (null == bierTopology || null == bierTopology.getBierNode() || bierTopology.getBierNode().isEmpty()) {
            LOG.info("Bier node list is null or empty");
            return null;
        }
        LOG.info("Process get interface");
        for (BierNode bierNode : bierTopology.getBierNode()) {
            if (bierNode.getNodeId().equals(nodeId)) {
                return getInterface(bierNode.getBierTerminationPoint(), tpId);
            }
        }
        return null;
    }

    public BierTopology getBierTopologyFromDataStore() {
        final InstanceIdentifier<BierTopology> path = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey("example-linkstate-topology"));
        final ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        Optional<BierTopology> bierTopology = null;
        BierTopology topology = null;
        try {
            bierTopology = tx.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (bierTopology.isPresent()) {
                topology = bierTopology.get();
                LOG.info("Get bier topology success, info is {}", topology);
                return topology;
            }
        } catch (ReadFailedException e) {
            LOG.error("Bier topology is null");
        }
        return null;
    }

    private Long getInterface(List<BierTerminationPoint> list, String tpId) {
        if (null == list || list.isEmpty()) {
            LOG.info("Bier termination point list is null");
            return null;
        }
        for (BierTerminationPoint bierTerminationPoint : list) {
            if (bierTerminationPoint.getTpId().equals(tpId)) {
                return bierTerminationPoint.getTpIndex();
            }
        }
        return null;
    }

    public MplsLabel processGetInLabel(String nodeId, DomainId domainId, SubDomainId subDomainId, Bsl bsl,
                                                 Si si) {
        LOG.info("Get bier topology");
        BierTopology bierTopology = getBierTopologyFromDataStore();
        LOG.info("Process get in-label of dest node");
        for (BierNode bierNode : bierTopology.getBierNode()) {
            if (bierNode.getNodeId().equals(nodeId) && null != bierNode.getBierTeNodeParams()
                    && null != bierNode.getBierTeNodeParams().getTeDomain()
                    && !bierNode.getBierTeNodeParams().getTeDomain().isEmpty()) {
                return processSingleTeDomainToGetInLabel(domainId, subDomainId,
                        bierNode.getBierTeNodeParams().getTeDomain(), bsl, si);
            }

        }
        return null;
    }

    private MplsLabel processSingleTeDomainToGetInLabel(DomainId domainId, SubDomainId subDomainId,
                                                        List<TeDomain> teDomainList, Bsl bsl, Si si) {
        for (TeDomain teDomain : teDomainList) {
            if (teDomain.getDomainId().equals(domainId) && null != teDomain.getTeSubDomain()
                    && !teDomain.getTeSubDomain().isEmpty()) {
                processSingleTeSubDomainToGetInLabel(subDomainId, teDomain.getTeSubDomain(), bsl, si);
            }
        }
        return null;
    }

    private MplsLabel processSingleTeSubDomainToGetInLabel(SubDomainId subDomainId, List<TeSubDomain> teSubDomainList,
                                                           Bsl bsl, Si si) {
        for (TeSubDomain teSubdomain : teSubDomainList) {
            if (teSubdomain.getSubDomainId().equals(subDomainId) && null != teSubdomain.getTeBsl()
                    && !teSubdomain.getTeBsl().isEmpty()) {
                return processSingleTeBslToGetInLabel(teSubdomain.getTeBsl(), bsl, si);
            }
        }
        return null;
    }

    private MplsLabel processSingleTeBslToGetInLabel(List<TeBsl> teBslList, Bsl bsl, Si si) {
        for (TeBsl teBsl : teBslList) {
            if (teBsl.getBitstringlength().equals(bsl) && null != teBsl.getTeSi()
                    && !teBsl.getTeSi().isEmpty()) {
                return processSingleTeSiToGetInLabel(teBsl.getTeSi(), si);
            }
        }
        return null;
    }

    private MplsLabel processSingleTeSiToGetInLabel(List<TeSi> teSiList, Si si) {
        for (TeSi teSi : teSiList) {
            if (teSi.getSi().equals(si)) {
                return teSi.getFtLabel();
            }
        }
        return null;
    }

    public void webSocketToApp(String failureReason) {
        notificationProvider.notifyFailureReason(failureReason);
    }

}
