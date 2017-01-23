/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.bier.adapter.api.BierConfigResult;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierChannelConfigProcess {
    private static final Logger LOG = LoggerFactory.getLogger(BierChannelConfigProcess.class);

    private final DataBroker dataProvider;
    private ChannelConfigWriter bierConfigWriter;
    private NotificationProvider notificationProvider;

    public BierChannelConfigProcess(DataBroker dataBroker,ChannelConfigWriter bierConfig) {
        dataProvider = dataBroker;
        bierConfigWriter = bierConfig;
        notificationProvider = new NotificationProvider();
    }

    public void processAddedChannel(Channel channel) {
        if (null == channel) {
            return;
        }
        LOG.info("Add Channel!");
        if (!checkChannelInput(channel)) {
            LOG.error("Channel input error!");
            return;
        }
        Channel channelBfr = addBfrIdToChannel(channel,channel.getEgressNode());
        BierConfigResult writedChannelResult = bierConfigWriter.writeChannel(
                ChannelConfigWriter.ConfigurationType.ADD, channelBfr);
        if (!writedChannelResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(writedChannelResult.getFailureReason());
            return;
        }
    }

    public void processDeletedChannel(Channel channel) {
        if (null == channel) {
            return;
        }
        LOG.info("Del Channel!");
        if (!checkChannelInput(channel)) {
            LOG.error("Channel input error!");
            return;
        }
        Channel channelBfr = addBfrIdToChannel(channel,channel.getEgressNode());
        BierConfigResult writedChannelResult = bierConfigWriter.writeChannel(
                ChannelConfigWriter.ConfigurationType.DELETE, channelBfr);
        if (!writedChannelResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(writedChannelResult.getFailureReason());
            return;
        }
    }

    public void processModifiedChannel(Channel before, Channel after) {
        if (null == before || null == after) {
            return;
        }
        if (!checkChannelInput(after)) {
            LOG.error("Channel input error!");
            return;
        } else {
            LOG.info("Deploy Channel!");
            Channel channel = addBfrIdToChannel(after,after.getEgressNode());
            BierConfigResult writedChannelResult = bierConfigWriter.writeChannel(
                    ChannelConfigWriter.ConfigurationType.MODIFY, channel);
            if (!writedChannelResult.isSuccessful()) {
                notificationProvider.notifyFailureReason(writedChannelResult.getFailureReason());
                return;
            }
        }
        if (checkEgressNodeDeleted(before.getEgressNode(),after.getEgressNode())) {
            LOG.info("Del Egress node!");
            List<EgressNode> egressNodeDeleted =
                 getEgressNodeListDeleted(before.getEgressNode(),after.getEgressNode());
            processDeletedEgressNode(before,egressNodeDeleted);
        }
    }

    private void processDeletedEgressNode(Channel channel,  List<EgressNode> egressNodeList) {
        if (null == channel || null == egressNodeList || egressNodeList.isEmpty()) {
            return;
        }
        Channel channelBfr = addBfrIdToChannel(channel,egressNodeList);
        BierConfigResult writedChannelResult = bierConfigWriter.writeChannelEgressNode(
               ChannelConfigWriter.ConfigurationType.DELETE, channelBfr);
        if (!writedChannelResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(writedChannelResult.getFailureReason());
            return;
        }
    }

    private List<EgressNode>  getEgressNodeListDeleted(List<EgressNode> nodeBefore,List<EgressNode> nodeAfter) {
        List<EgressNode> nodeDeleted = new ArrayList<>();
        if (null == nodeBefore &&  null == nodeAfter) {
            return nodeDeleted;
        }
        if (null == nodeAfter && null != nodeBefore) {
            return nodeBefore;
        }
        if (nodeBefore.size() > nodeAfter.size()) {
            for (EgressNode egressNode : nodeBefore) {
                if (null == getNodeById(egressNode.getNodeId(),nodeAfter)) {
                    nodeDeleted.add(egressNode);
                }
            }
        }
        return nodeDeleted;
    }

    private EgressNode getNodeById(String nodeId, List<EgressNode> nodeAfter) {
        if (null == nodeId || null == nodeAfter || nodeAfter.isEmpty()) {
            return null;
        }
        for (EgressNode node  : nodeAfter) {
            if (node.getNodeId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }


    private boolean checkEgressNodeDeleted(List<EgressNode> nodeBefore,List<EgressNode> nodeAfter) {
        if (null != nodeBefore && null == nodeAfter) {
            return true;
        }
        if (null != nodeBefore && null != nodeAfter
            && nodeBefore.size() > nodeAfter.size()) {
            return true;
        }
        return false;
    }

    private BfrId getBfrIdByBierInfo(String nodeId,DomainId domainId, SubDomainId subDomainId) {
        if (null == nodeId || null == domainId || null == subDomainId) {
            return null;
        }
        final InstanceIdentifier<BierNode> path = InstanceIdentifier.create(BierNetworkTopology.class)
                                                  .child(BierTopology.class, new BierTopologyKey("flow:1"))
                                                  .child(BierNode.class,new BierNodeKey(nodeId));
        final ReadTransaction tx = dataProvider.newReadOnlyTransaction();
        Optional<BierNode> bierNode = null;
        BierNode node = null;
        try {
            bierNode = tx.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (bierNode.isPresent()) {
                node = bierNode.get();
            }
        } catch (ReadFailedException e) {
            LOG.error("Get node from bier topology is null");
        }
        if (null == node) {
            return null;
        }
        List<Domain> domainList = node.getBierNodeParams().getDomain();
        if (null == domainList || domainList.isEmpty()) {
            return null;
        }
        for (Domain domain : domainList) {
            if (domain.getDomainId().equals(domainId)) {
                List<SubDomain> subDomainList = domain.getBierGlobal().getSubDomain();
                for (SubDomain subDomain : subDomainList) {
                    if (subDomain.getSubDomainId().equals(subDomainId)) {
                        return subDomain.getBfrId();
                    }
                }
            }
        }
        return null;
    }

    private Channel addBfrIdToChannel(Channel channel,List<EgressNode> egressNodeList) {
        if (null == channel) {
            return null;
        }
        ChannelBuilder builder = new ChannelBuilder(channel);
        BfrId ingressBfrId = getBfrIdByBierInfo(channel.getIngressNode(),channel.getDomainId(),
                    channel.getSubDomainId());
        if (null != ingressBfrId) {
            builder.setIngressBfrId(ingressBfrId);
        }
        if (null != egressNodeList && !egressNodeList.isEmpty()) {
            List<EgressNode> egressNodeBfrList = new ArrayList<>();
            for (EgressNode egressNode: egressNodeList) {
                EgressNode egressNodeBfr = addBfrToEgressNode(channel,egressNode);
                egressNodeBfrList.add(egressNodeBfr);
            }
            builder.setEgressNode(egressNodeBfrList);
        }
        return builder.build();
    }

    private EgressNode addBfrToEgressNode(Channel channel,EgressNode egressNode) {
        if (null == channel || null == egressNode) {
            return null;
        }
        BfrId egressBfrId = getBfrIdByBierInfo(egressNode.getNodeId(),channel.getDomainId(),
                  channel.getSubDomainId());
        EgressNodeBuilder builderNode = new EgressNodeBuilder(egressNode);
        builderNode.setEgressBfrId(egressBfrId);
        return builderNode.build();
    }

    private boolean checkChannelInput(Channel channel) {
        if (null == channel) {
            return false;
        }
        if (null == channel.getIngressNode()) {
            return false;
        }
        if (null == channel.getEgressNode() || channel.getEgressNode().isEmpty()) {
            return false;
        }
        return true;
    }

}
