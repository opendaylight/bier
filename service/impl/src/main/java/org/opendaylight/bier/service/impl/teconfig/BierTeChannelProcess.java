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
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.BierTeChannelWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.service.impl.NotificationProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierForwardingType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPathUpdate;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class BierTeChannelProcess {

    private static final Logger LOG = LoggerFactory.getLogger(BierTeChannelProcess.class);

    private final DataBroker dataBroker;
    private BierTeChannelWriter teChannelWriter;
    private NotificationProvider notificationProvider;
    private BitStringProcess bitStringProcess;
    private BiftInfoProcess biftInfoProcess;
    private static final int ADD_TE_CHANNEL = 1;
    private static final int DELETE_TE_CHANNEL = 2;

    public BierTeChannelProcess(DataBroker dataBroker, RpcConsumerRegistry rpcConsumerRegistry,
                                BierTeChannelWriter teChannelWriter, BierTeBiftWriter bierTeBiftWriter,
                                BierTeBitstringWriter bierTeBitstringWriter) {
        this.dataBroker = dataBroker;
        this.teChannelWriter = teChannelWriter;
        this.notificationProvider = new NotificationProvider();
        this.biftInfoProcess = new BiftInfoProcess(dataBroker,bierTeBiftWriter);
        this.bitStringProcess = new BitStringProcess(dataBroker,rpcConsumerRegistry,
                bierTeBitstringWriter, bierTeBiftWriter);
    }


    public void processAddedTeChannel(Channel channel) {
        LOG.info("Check channel input");
        if (!checkChannelInput(channel)) {
            LOG.error("Channel input error!");
            return;
        }

        LOG.info("Process set bp te-adj-type of bfir and bfer to local-decap");
        boolean result = biftInfoProcess.processSetLocalDecapToBfirBfer(channel);
        if (false == result) {
            LOG.info("Process set bp type failed");
            return;
        }

        LOG.info("Process bitString");
        boolean processBitStringResult = bitStringProcess.bierTeBitStringProcess(channel, ADD_TE_CHANNEL);
        if (false == processBitStringResult) {
            LOG.info("Process bitString failed");
            biftInfoProcess.webSocketToApp("Process bitString of " + channel.getName()
                    + " failed");
            return;
        }

        LOG.info("Get path pathId List");
        List<Long> pathIdList = bitStringProcess.getPathIdList(channel.getName());

        LOG.info("Add te-channel");
        deployTeChannel(ConfigurationType.ADD, channel.getIngressNode(), channel, pathIdList);

    }

    public void processDeletedTeChannel(Channel channel) {
        LOG.info("Check channel input");
        if (!checkChannelInput(channel)) {
            LOG.error("Channel input error!");
            return;
        }

        LOG.info("Delete te-channel");
        ConfigurationResult result = deployTeChannel(ConfigurationType.DELETE, channel.getIngressNode(),
                channel, null);
        if (result.isSuccessful()) {
            bitStringProcess.bierTeBitStringProcess(channel, DELETE_TE_CHANNEL);
        }
    }

    public void processModifiedTeChannel(Channel before, Channel after) {
        LOG.info("Check channel input");
        if (!checkChannelInput(after)) {
            LOG.error("Channel input error!");
            return;
        }

        LOG.info("Process set bp te-adj-type of bfir and bfer to local-decap");
        boolean result = biftInfoProcess.processSetLocalDecapToBfirBfer(after);
        if (false == result) {
            LOG.info("Process set bp type failed");
            return;
        }

        if (checkEgressNodeListChange(before.getEgressNode(), after.getEgressNode())) {
            if (checkEgressNodeAdded(before.getEgressNode(), after.getEgressNode())) {
                List<EgressNode> addedList = getEgressNodeChangeList(before.getEgressNode(), after.getEgressNode());

                LOG.info("Reconstruct te-channel");
                Channel channel = reConstructTeChannel(after, addedList);

                LOG.info("Process bitString");
                boolean processBitStringResult = bitStringProcess.bierTeBitStringProcess(channel, ADD_TE_CHANNEL);
                if (false == processBitStringResult) {
                    LOG.info("Process bitString failed");
                    biftInfoProcess.webSocketToApp("Process bitString of " + channel.getName()
                            + " failed");
                    return;
                }
            } else {
                List<EgressNode> deletedList = getEgressNodeChangeList(before.getEgressNode(), after.getEgressNode());

                LOG.info("Reconstruct te-channel");
                Channel channel = reConstructTeChannel(after, deletedList);

                LOG.info("Process bitString");
                boolean processBitStringResult = bitStringProcess.bierTeBitStringProcess(channel, DELETE_TE_CHANNEL);
                if (false == processBitStringResult) {
                    LOG.info("Process bitString failed");
                    biftInfoProcess.webSocketToApp("Process bitString of " + channel.getName()
                            + " failed");
                    return;
                }
            }

            LOG.info("Get path pathId List");
            List<Long> pathIdList = bitStringProcess.getPathIdList(after.getName());

            LOG.info("Modify te-channel");
            deployTeChannel(ConfigurationType.MODIFY, after.getIngressNode(), after, pathIdList);
        } else {
            LOG.info("Egress node list not change, check ingress node change");
            if (!before.getIngressNode().equals(after.getIngressNode())) {
                LOG.info("Process bitString again");
                boolean processBitStringResult = bitStringProcess.bierTeBitStringProcess(after, ADD_TE_CHANNEL);
                if (false == processBitStringResult) {
                    LOG.info("Process bitString failed");
                    biftInfoProcess.webSocketToApp("Process bitString of " + after.getName()
                            + " failed");
                    return;
                }
                List<Long> pathIdList = bitStringProcess.getPathIdList(after.getName());
                LOG.info("Modify te-channel");
                deployTeChannel(ConfigurationType.MODIFY, after.getIngressNode(), after, pathIdList);
            } else {
                LOG.info("Ingress node and egress node both not change, deploy channel");
                deployTeChannel(ConfigurationType.MODIFY, after.getIngressNode(), after, null);
            }
        }
    }

    public void processUpdateTeChannel(BierPathUpdate updatePath) {
        if (null == updatePath) {
            return;
        }

        boolean processBitStringResult = bitStringProcess.updateBitStringList(updatePath.getChannelName(), updatePath);
        if (false == processBitStringResult) {
            LOG.info("Update bitString failed");
            biftInfoProcess.webSocketToApp("Update bitString of " + updatePath.getChannelName()
                    + " failed");
            return;
        }

        List<Long> pathIdList = bitStringProcess.getPathIdList(updatePath.getChannelName());

        Channel channel = queryChannelByName(updatePath.getChannelName());
        if (null == channel) {
            return;
        }

        deployTeChannel(ConfigurationType.MODIFY, channel.getIngressNode(), channel, pathIdList);
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

    private boolean checkEgressNodeListChange(List<EgressNode> before, List<EgressNode> after) {
        if (null == before || before.isEmpty()) {
            return true;
        } else {
            if (before.size() != after.size()) {
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean checkEgressNodeAdded(List<EgressNode> before, List<EgressNode> after) {
        if ((null == before || before.isEmpty()) && after.size() > 0) {
            return true;
        }
        if (before.size() < after.size()) {
            return true;
        }
        return false;
    }

    private List<EgressNode> getEgressNodeChangeList(List<EgressNode> before, List<EgressNode> after) {
        if ((null == before || before.isEmpty()) && after.size() > 0) {
            return after;
        }
        List<EgressNode> egressNodeChange = new ArrayList<>();
        if (before.size() < after.size()) {
            for (EgressNode egressNode : after) {
                if (null == getEgressNodeById(egressNode.getNodeId(), before)) {
                    egressNodeChange.add(egressNode);
                }
            }
        } else if (before.size() > after.size()) {
            for (EgressNode egressNode : before) {
                if (null == getEgressNodeById(egressNode.getNodeId(), after)) {
                    egressNodeChange.add(egressNode);
                }
            }
        }
        return egressNodeChange;
    }

    private EgressNode getEgressNodeById(String nodeId, List<EgressNode> list) {
        for (EgressNode egressNode : list) {
            if (egressNode.getNodeId().equals(nodeId)) {
                return egressNode;
            }
        }
        return null;
    }

    private Channel reConstructTeChannel(Channel channel, List<EgressNode> list) {
        ChannelBuilder builder = new ChannelBuilder();
        builder.setKey(channel.getKey());
        builder.setName(channel.getName());
        builder.setSrcIp(channel.getSrcIp());
        builder.setDstGroup(channel.getDstGroup());
        builder.setDomainId(channel.getDomainId());
        builder.setSubDomainId(channel.getSubDomainId());
        builder.setSourceWildcard(channel.getSourceWildcard());
        builder.setGroupWildcard(channel.getGroupWildcard());
        builder.setIngressNode(channel.getIngressNode());
        builder.setIngressBfrId(channel.getIngressBfrId());
        builder.setSrcTp(channel.getSrcTp());
        builder.setEgressNode(list);
        builder.setBierForwardingType(channel.getBierForwardingType());

        return builder.build();
    }

    private ConfigurationResult deployTeChannel(ConfigurationType type, String nodeId, Channel teChannel,
                                                List<Long> pathIdList) {
        ConfigurationResult writedChannelResult = teChannelWriter.writeBierTeChannel(type, nodeId,
                teChannel, pathIdList);
        if (!writedChannelResult.isSuccessful()) {
            biftInfoProcess.webSocketToApp(writedChannelResult.getFailureReason());
        }
        return writedChannelResult;
    }

    private Channel queryChannelByName(String channelName) {
        if (null == channelName) {
            return null;
        }
        final InstanceIdentifier<BierChannel> path = InstanceIdentifier.create(BierNetworkChannel.class)
                .child(BierChannel.class, new BierChannelKey("example-linkstate-topology"));
        final ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        Optional<BierChannel> bierChannel = null;
        BierChannel channel = null;
        try {
            bierChannel = tx.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (bierChannel.isPresent()) {
                channel = bierChannel.get();
            }
        } catch (ReadFailedException e) {
            LOG.error("Get bierChannelList from networkChannel failed");
        }
        if (null == channel || null == channel.getChannel() || 0 == channel.getChannel().size()) {
            return null;
        }
        for (Channel singleChannel : channel.getChannel()) {
            if (singleChannel.getBierForwardingType().equals(BierForwardingType.BierTe)
                    && singleChannel.getName().equals(channelName)) {
                return singleChannel;
            }
        }
        return null;
    }

}
