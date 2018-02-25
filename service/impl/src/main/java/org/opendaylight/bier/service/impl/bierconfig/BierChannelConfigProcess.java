/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.bierconfig;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.service.impl.NotificationProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.egress.node.RcvTp;
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
        ConfigurationResult writedChannelResult = bierConfigWriter.writeChannel(
                ConfigurationType.MODIFY, channel);
        if (!writedChannelResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(writedChannelResult.getFailureReason());
            return;
        }
    }

    public void processDeletedChannel(Channel channel) {
        ConfigurationResult writedChannelResult = bierConfigWriter.writeChannel(
                ConfigurationType.DELETE, channel);
        if (!writedChannelResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(writedChannelResult.getFailureReason());
            return;
        }
    }

    public List<EgressNode> getTpChangedEgressNodes(List<EgressNode> beforeList,List<EgressNode> afterList) {
        ArrayList<EgressNode> egressNodes = new ArrayList<>();
        for (EgressNode beforeNode : beforeList) {
            for (EgressNode afterNode : afterList) {
                if (beforeNode.getNodeId().equals(afterNode.getNodeId())) {
                    List<RcvTp> rcvTpsBefore = beforeNode.getRcvTp();
                    List<RcvTp> rcvTpsAfter = afterNode.getRcvTp();
                    if ((rcvTpsAfter == null) || (rcvTpsAfter.isEmpty())) {
                        LOG.info("Egress node  {} with tps null",afterNode);
                        continue;
                    }
                    if ((rcvTpsAfter.containsAll(rcvTpsBefore))
                            && (rcvTpsBefore.containsAll(rcvTpsAfter))) {
                        continue;
                    }
                    egressNodes.add(afterNode);

                }
            }
        }
        return egressNodes;

    }

    public void processModifiedChannel(Channel before, Channel after) {

        if (null == before.getIngressNode()) {
            LOG.info("First time deploy channel:" + after.getName());
            processAddedChannel(after);
        } else if (!before.getIngressNode().equals(after.getIngressNode())
                || ((before.getIngressNode().equals(after.getIngressNode()))
                        && (before.getSrcTp() != null || after.getSrcTp() != null)
                        && !(before.getSrcTp().equals(after.getSrcTp())))) {
            LOG.info("Ingress node change of channel:" + after.getName());
            processDeletedChannel(before);
            processAddedChannel(after);
        } else {
            LOG.info("Egress node list change of channel:" + after.getName());
            List<EgressNode> egressNodeAdded = getEgressNodeListAddOrDelete(after.getEgressNode(),
                    before.getEgressNode());
            if (!egressNodeAdded.isEmpty()) {
                processEgressNodeAdded(after,egressNodeAdded);
            }

            List<EgressNode> egressNodeDeleted =
                    getEgressNodeListAddOrDelete(before.getEgressNode(),after.getEgressNode());
            if (!egressNodeDeleted.isEmpty()) {
                processEgressNodeDeleted(before,egressNodeDeleted);
            }

            List<EgressNode> egressNodeTpChange =
                    getTpChangedEgressNodes(before.getEgressNode(),after.getEgressNode());
            if (!egressNodeTpChange.isEmpty()) {
                processTpChangedEgressNodes(after,egressNodeTpChange);
            }
        }
    }

    private void processTpChangedEgressNodes(Channel channel,List<EgressNode> changedEgressNodes) {
        if (null == channel || null == changedEgressNodes || changedEgressNodes.isEmpty()) {
            return;
        }
        ChannelBuilder builder = new ChannelBuilder(channel);
        builder.setEgressNode(changedEgressNodes);
        LOG.info("TPs of egress nodes changes, channel info -- {} ; changed egress nodes -- {}",
                channel,changedEgressNodes);
        ConfigurationResult modifyEgressNodeResult = bierConfigWriter.writeChannelEgressNode(
                ConfigurationType.MODIFY, builder.build());
        if (!modifyEgressNodeResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(modifyEgressNodeResult.getFailureReason());
            return;
        }
    }

    private void processEgressNodeAdded(Channel channel, List<EgressNode> egressNodeList) {
        if (null == channel || null == egressNodeList || egressNodeList.isEmpty()) {
            return;
        }
        ChannelBuilder builder = new ChannelBuilder(channel);
        builder.setEgressNode(egressNodeList);
        ConfigurationResult addEgressNodeResult = bierConfigWriter.writeChannelEgressNode(
                ConfigurationType.ADD, builder.build());
        if (!addEgressNodeResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(addEgressNodeResult.getFailureReason());
            return;
        }
    }

    private void processEgressNodeDeleted(Channel channel,  List<EgressNode> egressNodeList) {
        if (null == channel || null == egressNodeList || egressNodeList.isEmpty()) {
            return;
        }
        ChannelBuilder builder = new ChannelBuilder(channel);
        builder.setEgressNode(egressNodeList);
        ConfigurationResult deleteEgressNodeResult = bierConfigWriter.writeChannelEgressNode(
               ConfigurationType.DELETE, builder.build());
        if (!deleteEgressNodeResult.isSuccessful()) {
            notificationProvider.notifyFailureReason(deleteEgressNodeResult.getFailureReason());
            return;
        }
    }

    private List<EgressNode>  getEgressNodeListAddOrDelete(List<EgressNode> egressNodeList1,List<EgressNode>
            egressNodeList2) {
        List<EgressNode> nodeList = new ArrayList<>();
        if (null == egressNodeList1) {
            return nodeList;
        }
        if (null == egressNodeList2) {
            return egressNodeList1;
        }
        for (EgressNode egressNode:egressNodeList1) {
            boolean nodeNotExist = true;
            for (EgressNode node:egressNodeList2) {
                if (node.getNodeId().equals(egressNode.getNodeId())) {
                    nodeNotExist = false;
                    break;
                }
            }
            if (nodeNotExist) {
                nodeList.add(egressNode);
            }
        }

        return nodeList;
    }

}
