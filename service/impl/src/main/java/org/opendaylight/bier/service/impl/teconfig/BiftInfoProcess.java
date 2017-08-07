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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.service.impl.NotificationProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.BierChannelApiService;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelWithPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.QueryChannelWithPortOutput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.api.rev161102.query.channel.with.port.output.QueryChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPceService;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryChannelThroughPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryChannelThroughPortOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.channel.through.port.output.RelatedChannel;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.TePath;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.bier.te.path.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.bier.te.path.PathKey;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.te.path.Bitstring;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.te.path.BitstringBuilder;
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
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BiftInfoProcess {

    private static final Logger LOG = LoggerFactory.getLogger(BiftInfoProcess.class);
    private final DataBroker dataBroker;
    private final RpcConsumerRegistry rpcConsumerRegistry;
    private final BierTeBiftWriter bierTeBiftWriter;
    private final BierTeBitstringWriter bierTeBitstringWriter;
    private BitStringDB bitStringDB;
    private NotificationProvider notificationProvider;
    private static final int TE_ADJ_TYPE_CONNECTED = 1;
    private static final int TE_ADJ_TYPE_LOCALDECAP = 2;
    public static final int SET_LOCALDECAP = 3;
    public static final int DEL_LOCALDECAP = 4;
    private static final int ADD = 5;
    private static final int DELETE = 6;

    public BiftInfoProcess(final DataBroker dataBroker, final RpcConsumerRegistry rpcConsumerRegistry,
                           final BierTeBiftWriter bierTeBiftWriter, final BierTeBitstringWriter bierTeBitstringWriter) {
        this.dataBroker = dataBroker;
        this.rpcConsumerRegistry = rpcConsumerRegistry;
        this.bierTeBiftWriter = bierTeBiftWriter;
        this.bierTeBitstringWriter = bierTeBitstringWriter;
        this.bitStringDB = new BitStringDB(dataBroker);
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
            LOG.info("Could not find matched bierLink, check bp type whether local decap or not");
            processLocalDecapBpInfoAndBitString(ADD, homeNodeId, domainId, subDomainId, bsl, si, homeNodeTpId,
                    homeNodeBp);
            return;
        }
        LOG.info("BierLink is " + bierLink);

        LOG.info("Check if dest node has been configured bp");
        Integer destNodeBp = getDestNodeConfiguredBp(bierLink.getLinkDest().getDestNode(), domainId,
                subDomainId, bsl, si, bierLink.getLinkDest().getDestTp());
        if (null == destNodeBp) {
            LOG.info("The bp of dest node has not been configured, do not configure bift added info");
            return;
        }
        LOG.info("Dest node bp is " + destNodeBp);

        LOG.info("Process get ft-label of home node");
        MplsLabel homeNodeInLabel = processGetInLabel(homeNodeId, domainId, subDomainId, bsl, si);
        LOG.info("Home node ft label is " + homeNodeInLabel);

        LOG.info("Process get in-label of dest node");
        MplsLabel destNodeInLabel = processGetInLabel(bierLink.getLinkDest().getDestNode(),
                domainId, subDomainId, bsl, si);
        LOG.info("Dest node ft label is " + destNodeInLabel);

        LOG.info("Process added dest node bift info");
        processAddedNodeBiftInfo(bierLink.getLinkDest().getDestNode(), subDomainId, bsl, si,
                bierLink.getLinkDest().getDestTp(), homeNodeBp, homeNodeInLabel, destNodeInLabel);

        LOG.info("Process added home node bift info");
        processAddedNodeBiftInfo(homeNodeId, subDomainId, bsl, si, homeNodeTpId, destNodeBp,
                destNodeInLabel, homeNodeInLabel);

        LOG.info("Query home node tpId channel by link");
        List<RelatedChannel> homeNodeChannelList = queryChannelByLink(homeNodeId, homeNodeTpId);
        if (null != homeNodeChannelList && homeNodeChannelList.isEmpty()) {
            List<Channel> transferedHomeNodeChannelList = transferConnectedTypeBpChannel(homeNodeChannelList,
                    domainId, subDomainId);
            if (null != transferedHomeNodeChannelList && !transferedHomeNodeChannelList.isEmpty()) {
                queryBitStringByChannelAndSendToSouth(ADD, transferedHomeNodeChannelList,
                        subDomainId, bsl, si, destNodeBp);
            }
        }

        LOG.info("Query dest node tpId channel by link");
        List<RelatedChannel> destNodeChannelList = queryChannelByLink(bierLink.getLinkDest().getDestNode(),
                bierLink.getLinkDest().getDestTp());
        if (null == destNodeChannelList || destNodeChannelList.isEmpty()) {
            LOG.info("Could not find matched channel");
            return;
        }

        List<Channel> transferedDestNodeChannelList = transferConnectedTypeBpChannel(destNodeChannelList,
                domainId, subDomainId);
        if (null == transferedDestNodeChannelList || transferedDestNodeChannelList.isEmpty()) {
            return;
        }

        queryBitStringByChannelAndSendToSouth(ADD, transferedDestNodeChannelList, subDomainId, bsl,
                si, homeNodeBp);
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
            LOG.info("Could not find matched bierLink, check bp type whether local decap or not");
            processLocalDecapBpInfoAndBitString(DELETE, homeNodeId, domainId, subDomainId, bsl, si,
                    homeNodeTpId, homeNodeBp);
            return;
        }
        LOG.info("BierLink is " + bierLink);

        LOG.info("Check if dest node has been configured bp");
        Integer destNodeBp = getDestNodeConfiguredBp(bierLink.getLinkDest().getDestNode(), domainId,
                subDomainId, bsl, si, bierLink.getLinkDest().getDestTp());
        if (null == destNodeBp) {
            LOG.info("The bp of dest node has not been configured, do not configure bift deleted info");
            return;
        }
        LOG.info("Dest node bp is " + destNodeBp);

        LOG.info("Process deleted dest node bift info");
        processDeletedNodeBiftInfo(bierLink.getLinkDest().getDestNode(), subDomainId, bsl, si, homeNodeBp);

        LOG.info("Process deleted home node bift info");
        processDeletedNodeBiftInfo(homeNodeId, subDomainId, bsl, si, destNodeBp);

        LOG.info("Query home node tpId channel by link");
        List<RelatedChannel> homeNodeChannelList = queryChannelByLink(homeNodeId, homeNodeTpId);
        if (null != homeNodeChannelList && !homeNodeChannelList.isEmpty()) {
            List<Channel> transferedHomeNodeChannelList = transferConnectedTypeBpChannel(homeNodeChannelList,
                    domainId, subDomainId);
            if (null != transferedHomeNodeChannelList && !transferedHomeNodeChannelList.isEmpty()) {
                queryBitStringByChannelAndSendToSouth(DELETE, transferedHomeNodeChannelList, subDomainId, bsl,
                        si, destNodeBp);
            }
        }


        LOG.info("Query dest node tpId channel by link");
        List<RelatedChannel> destNodeChannelList = queryChannelByLink(bierLink.getLinkDest().getDestNode(),
                bierLink.getLinkDest().getDestTp());
        if (null == destNodeChannelList || destNodeChannelList.isEmpty()) {
            LOG.info("Could not find matched channel");
            return;
        }

        List<Channel> transferedDestNodeChannelList = transferConnectedTypeBpChannel(destNodeChannelList,
                domainId, subDomainId);
        if (null == transferedDestNodeChannelList || transferedDestNodeChannelList.isEmpty()) {
            return;
        }

        queryBitStringByChannelAndSendToSouth(DELETE, transferedDestNodeChannelList, subDomainId, bsl,
                si, homeNodeBp);
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
        LOG.info("Interface is " + nodeInterFace);

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
        LOG.info("TeInfo is " + teInfo);
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
        LOG.info("TeInfo is " + teInfo);
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

    public boolean processSetLocalDecapToBfirBfer(int type, Channel channel) {
        LOG.info("Process bfir");
        if (!processSetLocalDecapToBfir(type, channel.getDomainId(), channel.getSubDomainId(),
                channel.getIngressNode(), channel.getSrcTp())) {
            LOG.error("process set bp-te-adj-type of bfir failed");
            return false;
        }
        LOG.info("Process bfer");
        if (!processSetLocalDecapToBfer(type, channel.getDomainId(), channel.getSubDomainId(),
                channel.getEgressNode())) {
            LOG.error("process set bp-te-adj-type of  bfer failed");
            return false;
        }
        LOG.info("process set bp-te-adj-type of bfir and bfer succeed");
        return true;
    }

    private boolean processSetLocalDecapToBfir(int type, DomainId domainId, SubDomainId subDomainId, String nodeId,
                                               String srcTp) {
        if (null == domainId || null == subDomainId || null == nodeId || null == srcTp) {
            return false;
        }
        LOG.info("Bfir");
        BierNode bfir = getBierNodeById(nodeId);
        if (null == bfir) {
            LOG.info("Get bfir from datastore failed");
            webSocketToApp("Bfir is not in datastore");
            return false;
        }
        LOG.info("Get bfir info");
        List<TeInfo> teInfoList = getBiftTeInfoFromInput(domainId, subDomainId,bfir,srcTp);
        if (SET_LOCALDECAP == type) {
            return processSendInfoToSouthbound(nodeId, teInfoList, ConfigurationType.ADD);
        } else {
            return processSendInfoToSouthbound(nodeId, teInfoList, ConfigurationType.DELETE);
        }
    }

    private boolean processSetLocalDecapToBfer(int type, DomainId domainId, SubDomainId subDomainId,
                                               List<EgressNode> bferList) {
        if (null == domainId || null == subDomainId || null == bferList || bferList.isEmpty()) {
            return false;
        }
        LOG.info("Bfer");
        for (EgressNode node : bferList) {
            BierNode bfer = getBierNodeById(node.getNodeId());
            if (null == bfer) {
                LOG.info("Get bfer from datastore failed");
                webSocketToApp("Bfer: " + node.getNodeId() + " is not in datastore");
                return false;
            }
            LOG.info("Get bfer info");
            for (RcvTp rcvTpId : node.getRcvTp()) {
                List<TeInfo> teInfoList = getBiftTeInfoFromInput(domainId, subDomainId,bfer,rcvTpId.getTp());
                if (SET_LOCALDECAP == type) {
                    if (!processSendInfoToSouthbound(node.getNodeId(), teInfoList, ConfigurationType.ADD)) {
                        return false;
                    }
                } else {
                    if (!processSendInfoToSouthbound(node.getNodeId(), teInfoList, ConfigurationType.DELETE)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public BierNode getBierNodeById(String nodeId) {
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


    public List<TeInfo> getBiftTeInfoFromInput(DomainId domainId, SubDomainId subDomainId, BierNode node,
                                                String tpId) {

        List<TeBsl> teBslList = getBslListFromDomainInfo(domainId, subDomainId, node);
        LOG.info("teBsl list: " + teBslList);
        if (null == teBslList || teBslList.isEmpty()) {
            return null;
        }
        LOG.info("Construct info, tpId is " + tpId);
        List<TeInfo> teInfoList = new ArrayList<>();
        for (TeBsl teBsl : teBslList) {
            for (TeSi teSi : teBsl.getTeSi()) {
                for (TeBp teBp : teSi.getTeBp()) {
                    LOG.info("Inside tebp, tpId is " + teBp.getTpId());
                    if (teBp.getTpId().equals(tpId)) {
                        LOG.info("TeInfo");
                        TeInfo teInfo = processConstructTeInfo(TE_ADJ_TYPE_LOCALDECAP, subDomainId,
                                teBsl.getBitstringlength(), teSi.getSi(), teBp.getBitposition(),null,
                                null,null);
                        if (null != teInfo) {
                            LOG.info("Add Info, teInfo is " + teInfo);
                            teInfoList.add(teInfo);
                        }
                    }
                }
            }
        }
        LOG.info("TeInfoList size is " + teInfoList.size());
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
            LOG.info("TeInfo is " + teInfo);
            ConfigurationResult result = bierTeBiftWriter.writeTeBift(type, nodeId, teInfo);
            if (!result.isSuccessful()) {
                LOG.info("Send failed");
                webSocketToApp(result.getFailureReason());
                return false;
            }
        }
        LOG.info("Send success");
        return true;
    }


    public TeInfo processConstructTeInfo(int type, SubDomainId subDomainId, Bsl bsl, Si si, Integer bp, Long interFace,
                                                  MplsLabel homeNodeInLabel, MplsLabel destNodeInLabel) {
        LOG.info("Process build info");
        TeFIndexBuilder teFIndexBuilder = new TeFIndexBuilder();
        teFIndexBuilder.setTeFIndex(new BitString(bp));
        if (TE_ADJ_TYPE_CONNECTED == type) {
            teFIndexBuilder.setTeAdjType(new ConnectedBuilder().setConnected(true).build());
            teFIndexBuilder.setOutLabel(homeNodeInLabel);
        } else if (TE_ADJ_TYPE_LOCALDECAP == type) {
            LOG.info("Process set local-decap");
            teFIndexBuilder.setTeAdjType(new LocalDecapBuilder().setLocalDecap(true).build());
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
        LOG.info("Bsl is " + bsl);
        teBslBuilder.setFwdBsl(transferBslToInteger(bsl));
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

    private Integer transferBslToInteger(Bsl bsl) {
        String[] bslName = bsl.getName().split("-");
        return Integer.valueOf(bslName[0]);
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
            LOG.error("Bier topology is null:" + e.getStackTrace());
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
        LOG.info("Process get in-label of node");
        for (BierNode bierNode : bierTopology.getBierNode()) {
            if (bierNode.getNodeId().equals(nodeId) && null != bierNode.getBierTeNodeParams()
                    && null != bierNode.getBierTeNodeParams().getTeDomain()
                    && !bierNode.getBierTeNodeParams().getTeDomain().isEmpty()) {
                LOG.info("Process teDomain");
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
                LOG.info("Process teSubDomain");
                return processSingleTeSubDomainToGetInLabel(subDomainId, teDomain.getTeSubDomain(), bsl, si);
            }
        }
        return null;
    }

    private MplsLabel processSingleTeSubDomainToGetInLabel(SubDomainId subDomainId, List<TeSubDomain> teSubDomainList,
                                                           Bsl bsl, Si si) {
        for (TeSubDomain teSubdomain : teSubDomainList) {
            if (teSubdomain.getSubDomainId().equals(subDomainId) && null != teSubdomain.getTeBsl()
                    && !teSubdomain.getTeBsl().isEmpty()) {
                LOG.info("Process teBsl");
                return processSingleTeBslToGetInLabel(teSubdomain.getTeBsl(), bsl, si);
            }
        }
        return null;
    }

    private MplsLabel processSingleTeBslToGetInLabel(List<TeBsl> teBslList, Bsl bsl, Si si) {
        for (TeBsl teBsl : teBslList) {
            if (teBsl.getBitstringlength().equals(bsl) && null != teBsl.getTeSi()
                    && !teBsl.getTeSi().isEmpty()) {
                LOG.info("Process teSi");
                return processSingleTeSiToGetInLabel(teBsl.getTeSi(), si);
            }
        }
        return null;
    }

    private MplsLabel processSingleTeSiToGetInLabel(List<TeSi> teSiList, Si si) {
        for (TeSi teSi : teSiList) {
            if (teSi.getSi().equals(si)) {
                LOG.info("Process get label " + teSi.getFtLabel());
                return teSi.getFtLabel();
            }
        }
        LOG.info("null");
        return null;
    }

    public void webSocketToApp(String failureReason) {
        notificationProvider.notifyFailureReason(failureReason);
    }

    private void processLocalDecapBpInfoAndBitString(int type, String nodeId, DomainId domainId,
                                                     SubDomainId subDomainId, Bsl bsl, Si si, String tpId, Integer bp) {
        QueryChannelWithPortInputBuilder builder = new QueryChannelWithPortInputBuilder();
        builder.setNodeId(nodeId);
        builder.setDomainId(domainId);
        builder.setSubDomainId(subDomainId);
        builder.setTpId(tpId);
        Future<RpcResult<QueryChannelWithPortOutput>> future = rpcConsumerRegistry
                .getRpcService(BierChannelApiService.class).queryChannelWithPort(builder.build());
        try {
            QueryChannelWithPortOutput output = future.get().getResult();
            if (null == output || null == output.getQueryChannel() || output.getQueryChannel().isEmpty()) {
                LOG.info("Channel is null");
                return;
            } else {
                TeInfo teInfo = processConstructTeInfo(TE_ADJ_TYPE_LOCALDECAP, subDomainId, bsl, si, bp,
                        null, null, null);
                List<TeInfo> teInfoList = new ArrayList<>();
                teInfoList.add(teInfo);
                if (ADD == type) {
                    if (!processSendInfoToSouthbound(nodeId, teInfoList, ConfigurationType.ADD)) {
                        return;
                    }
                } else {
                    if (!processSendInfoToSouthbound(nodeId, teInfoList, ConfigurationType.DELETE)) {
                        return;
                    }
                }
                processBitString(type, output.getQueryChannel(), domainId, subDomainId, bsl, si, bp);
            }

        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Process is Interrupted by", e);
        }
    }

    private void processBitString(int type, List<QueryChannel> channelList, DomainId domainId,
                                  SubDomainId subDomainId, Bsl bsl, Si si, Integer bp) {
        List<QueryChannel> tmpList = new ArrayList<>();
        for (QueryChannel channel : channelList) {
            if (channel.isIsRcvTp()) {
                tmpList.add(channel);
            }
        }
        if (tmpList.isEmpty()) {
            return;
        }
        List<Channel> transferedChannelList = transferLocalDecapTypeBpChannel(tmpList, domainId, subDomainId);

        queryBitStringByChannelAndSendToSouth(type, transferedChannelList, subDomainId, bsl, si, bp);

    }

    private List<RelatedChannel> queryChannelByLink(String nodeId, String tpId) {
        QueryChannelThroughPortInputBuilder builder = new QueryChannelThroughPortInputBuilder();
        builder.setNodeId(nodeId);
        builder.setTpId(tpId);
        Future<RpcResult<QueryChannelThroughPortOutput>> future = rpcConsumerRegistry
                .getRpcService(BierPceService.class).queryChannelThroughPort(builder.build());
        try {
            QueryChannelThroughPortOutput output = future.get().getResult();
            if (null != output) {
                LOG.info("Channel list is exit " + output);
                return output.getRelatedChannel();
            }
            LOG.info("Channel list is null");
            return null;

        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Process is Interrupted by", e);
        }
        return null;
    }

    private void queryBitStringByChannelAndSendToSouth(int type, List<Channel> channelList, SubDomainId subDomainId,
                                                       Bsl bsl, Si si, Integer bp) {
        for (Channel channel : channelList) {
            List<TePath> tempList = new ArrayList<>();
            List<TePath> tePathList = bitStringDB.getTePathFromChannel(channel.getName());
            if (null == tePathList || tePathList.isEmpty()) {
                continue;
            } else {
                for (TePath tePath : tePathList) {
                    if (tePath.getSubdomainId().equals(subDomainId) && tePath.getBitstringlength().equals(bsl)
                            && tePath.getSi().equals(si)) {
                        tempList.add(constructNewTePath(type, tePath, bp));
                    }
                }
            }

            sendBitStringToSouthAndSetToDataStore(channel, tempList);
        }
    }

    private TePath constructNewTePath(int type, TePath tePath, Integer bp) {
        PathBuilder pathBuilder = new PathBuilder();
        pathBuilder.setPathId(tePath.getPathId());
        pathBuilder.setKey(new PathKey(tePath.getPathId()));
        pathBuilder.setSubdomainId(tePath.getSubdomainId());
        pathBuilder.setBitstringlength(tePath.getBitstringlength());
        pathBuilder.setSi(tePath.getSi());
        BitstringBuilder bitstringBuilder = new BitstringBuilder();
        bitstringBuilder.setBitposition(new BitString(bp));
        List<Bitstring> list = tePath.getBitstring();
        if (ADD == type) {
            list.add(bitstringBuilder.build());
            pathBuilder.setBitstring(list);
        } else if (DELETE == type) {
            list.remove(bitstringBuilder.build());
            pathBuilder.setBitstring(list);
        }
        return pathBuilder.build();
    }

    private List<Channel> transferConnectedTypeBpChannel(List<RelatedChannel> channelList, DomainId domainId,
                                                         SubDomainId subDomainId) {
        List<Channel> tempList = new ArrayList<>();
        for (RelatedChannel channel : channelList) {
            ChannelBuilder builder = new ChannelBuilder();
            builder.setKey(new ChannelKey(channel.getChannelName()));
            builder.setName(channel.getChannelName());
            builder.setDomainId(domainId);
            builder.setSubDomainId(subDomainId);
            builder.setIngressNode(channel.getBfir());
            tempList.add(builder.build());
        }
        return tempList;
    }

    private List<Channel> transferLocalDecapTypeBpChannel(List<QueryChannel> channelList, DomainId domainId,
                                                          SubDomainId subDomainId) {
        List<Channel> tempList = new ArrayList<>();
        for (QueryChannel channel : channelList) {
            ChannelBuilder builder = new ChannelBuilder();
            builder.setKey(new ChannelKey(channel.getChannelName()));
            builder.setName(channel.getChannelName());
            builder.setDomainId(domainId);
            builder.setSubDomainId(subDomainId);
            builder.setIngressNode(channel.getBfir());
            tempList.add(builder.build());
        }
        return tempList;
    }

    private void sendBitStringToSouthAndSetToDataStore(Channel channel, List<TePath> tePathList) {
        if (null == channel) {
            return;
        }
        if (tePathList.isEmpty()) {
            return;
        }
        for (TePath tePath : tePathList) {
            LOG.info("Send new bitString to south");
            if (setBitStringConfigToSouthbound(channel.getIngressNode(), ConfigurationType.MODIFY, tePath)) {
                LOG.info("Set new bitString to data store");
                bitStringDB.setBitStringToDataStore(channel, tePath);
            }
        }
    }

    private boolean setBitStringConfigToSouthbound(String bfir, ConfigurationType type, TePath tePath) {
        if (null == bfir || null == tePath) {
            return false;
        }
        LOG.info("Process send BitString to southbound");
        ConfigurationResult result = bierTeBitstringWriter.writeBierTeBitstring(type,bfir,tePath);
        if (!result.isSuccessful()) {
            webSocketToApp(result.getFailureReason());
            return false;
        }
        return true;
    }

}
