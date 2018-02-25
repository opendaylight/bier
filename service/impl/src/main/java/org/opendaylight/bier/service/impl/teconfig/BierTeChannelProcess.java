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
import java.util.Iterator;
import java.util.List;

import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.BierTeChannelWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
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
    private BitStringProcess bitStringProcess;
    private BiftInfoProcess biftInfoProcess;
    private int flag = 0;

    public BierTeChannelProcess(DataBroker dataBroker, RpcConsumerRegistry rpcConsumerRegistry,
                                BierTeChannelWriter teChannelWriter, BierTeBiftWriter bierTeBiftWriter,
                                BierTeBitstringWriter bierTeBitstringWriter) {
        this.dataBroker = dataBroker;
        this.teChannelWriter = teChannelWriter;
        this.biftInfoProcess = new BiftInfoProcess(dataBroker, bierTeBiftWriter);
        this.bitStringProcess = new BitStringProcess(dataBroker,rpcConsumerRegistry,
                bierTeBitstringWriter, bierTeBiftWriter);
    }


    public void processAddedTeChannel(Channel channel) {
        LOG.info("Process bitString");
        boolean processBitStringResult = bitStringProcess.bierTeBitStringProcess(channel, channel,
                bitStringProcess.PATH_ADD);
        if (false == processBitStringResult) {
            LOG.info("Process bitString failed");
            return;
        }

        LOG.info("Get path pathId List");
        List<Long> pathIdList = bitStringProcess.getPathIdList(channel.getName());

        LOG.info("Add te-channel");
        deployTeChannel(ConfigurationType.ADD, channel.getIngressNode(), channel, pathIdList);

    }

    public void processDeletedTeChannel(Channel channel) {
        LOG.info("Process bitString");
        boolean processBitStringResult = bitStringProcess.bierTeBitStringProcess(channel, null,
                bitStringProcess.PATH_REMOVE_ALL);
        if (false == processBitStringResult) {
            LOG.info("Process bitString failed");
            return;
        }

        LOG.info("Delete te-channel");
        deployTeChannel(ConfigurationType.DELETE, channel.getIngressNode(), channel, null);
    }

    public void processModifiedTeChannel(Channel before, Channel after) {
        if (!before.getIngressNode().equals(after.getIngressNode())) {
            if (processIngressNodeChange(before, after)) {
                flag = 1;
            }
        } else {
            if (processEgressNodeChange(before, after)) {
                flag = 1;
            }
        }

        if (1 == flag) {
            List<Long> pathIdList = bitStringProcess.getPathIdList(after.getName());
            LOG.info("Modify te-channel");
            deployTeChannel(ConfigurationType.MODIFY, after.getIngressNode(), after, pathIdList);
        }
    }

    public void processUpdateTeChannel(BierPathUpdate updatePath) {
        if (null == updatePath) {
            return;
        }

        Channel channel = queryChannelByName(updatePath.getChannelName());
        if (channel == null) {
            return;
        }

        boolean processBitStringResult = bitStringProcess.updateBitStringList(channel, updatePath);
        if (false == processBitStringResult) {
            LOG.info("Update bitString failed");
            return;
        }
        List<Long> pathIdList = bitStringProcess.getPathIdList(updatePath.getChannelName());
        if (pathIdList.isEmpty()) {
            LOG.error("get path-id failed!");
            return;
        }

        deployTeChannel(ConfigurationType.MODIFY, channel.getIngressNode(), channel, pathIdList);
    }

    private boolean processIngressNodeChange(Channel before, Channel after) {
        LOG.info("Delete before path");
        boolean processDelBitStringResult = bitStringProcess.bierTeBitStringProcess(before,null,
                bitStringProcess.PATH_REMOVE_ALL);
        if (false == processDelBitStringResult) {
            return false;
        }

        LOG.info("Process new bitString");
        boolean processNewBitStringResult = bitStringProcess.bierTeBitStringProcess(after, after,
                bitStringProcess.PATH_ADD);
        if (false == processNewBitStringResult) {
            return false;
        }
        return true;
    }

    private boolean processEgressNodeChange(Channel before, Channel after) {
        List<EgressNode> channelBeforeSavedEgressNodeList = new ArrayList<>(before.getEgressNode());
        LOG.info("channelBeforeSavedEgressNodeList: " + channelBeforeSavedEgressNodeList);
        List<EgressNode> deletedList = new ArrayList<>(before.getEgressNode());
        List<EgressNode> addedOrModifiedList = new ArrayList<>(after.getEgressNode());
        Iterator<EgressNode> iterator1 = deletedList.iterator();
        while (iterator1.hasNext()) {
            EgressNode egressNode = iterator1.next();
            if (null != getEgressNodeById(egressNode.getNodeId(), addedOrModifiedList)) {
                addedOrModifiedList.remove(egressNode);
                iterator1.remove();
            }
        }
        Channel channelTemp = before;

        if (!deletedList.isEmpty()) {
            boolean processDelBitStringResult = false;
            LOG.info("Reconstruct te-channel, deletedList is: " + deletedList);
            Channel newChannel = reConstructChangedTeChannel(after, deletedList);
            LOG.info("newChannel: " + newChannel);
            //channelTemp = channelTemp - deletedList;
            channelTemp = reConstructTeChannelBesidesDeletedList(channelTemp, deletedList);
            LOG.info("channelTemp: " + channelTemp);

            LOG.info("Process delete bitString");
            if (channelBeforeSavedEgressNodeList.size() == deletedList.size()) {
                LOG.info("channelBeforeSavedEgressNodeList: " + channelBeforeSavedEgressNodeList);
                LOG.info("Remove all");
                processDelBitStringResult = bitStringProcess.bierTeBitStringProcess(newChannel, null,
                        bitStringProcess.PATH_REMOVE_ALL);
            } else {
                LOG.info("Remove");
                processDelBitStringResult = bitStringProcess.bierTeBitStringProcess(newChannel, channelTemp,
                        bitStringProcess.PATH_REMOVE);
            }
            if (false == processDelBitStringResult) {
                return false;
            }
        }
        if (!addedOrModifiedList.isEmpty()) {
            LOG.info("Reconstruct te-channel, list is: " + addedOrModifiedList);
            Channel newChannel = reConstructChangedTeChannel(after, addedOrModifiedList);
            LOG.info("newChannel: " + newChannel);
            //channelTemp = channelTemp + addedOrModifiedList;
            channelTemp = reConstructTeChannelContainedAddedOrModifiedList(channelTemp, addedOrModifiedList);
            LOG.info("channelTemp: " + channelTemp);

            LOG.info("Process added or modified bitString");
            boolean processAddedOrModifiedBitStringResult = bitStringProcess.bierTeBitStringProcess(newChannel,
                    channelTemp, bitStringProcess.PATH_ADD);
            if (false == processAddedOrModifiedBitStringResult) {
                return false;
            }
        }
        return true;
    }

    private EgressNode getEgressNodeById(String nodeId, List<EgressNode> list) {
        for (EgressNode egressNode : list) {
            if (egressNode.getNodeId().equals(nodeId)) {
                return egressNode;
            }
        }
        return null;
    }

    private Channel reConstructChangedTeChannel(Channel channel, List<EgressNode> list) {
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
        builder.setBpAssignmentStrategy(channel.getBpAssignmentStrategy());
        return builder.build();
    }

    private Channel reConstructTeChannelBesidesDeletedList(Channel channelTemp, List<EgressNode> list) {
        ChannelBuilder builder = reConstructChannelBasicInfo(channelTemp);
        List<EgressNode> currentEgressNodeList = channelTemp.getEgressNode();
        for (EgressNode egressNode : list) {
            currentEgressNodeList.remove(egressNode);
        }
        builder.setEgressNode(currentEgressNodeList);
        return builder.build();
    }

    private Channel reConstructTeChannelContainedAddedOrModifiedList(Channel channelTemp, List<EgressNode> list) {
        ChannelBuilder builder = reConstructChannelBasicInfo(channelTemp);
        List<EgressNode> currentEgressNodeList = channelTemp.getEgressNode();
        for (EgressNode egressNode : list) {
            currentEgressNodeList.add(egressNode);
        }
        builder.setEgressNode(currentEgressNodeList);
        return builder.build();
    }

    private ChannelBuilder reConstructChannelBasicInfo(Channel channelTemp) {
        ChannelBuilder builder = new ChannelBuilder();
        builder.setName(channelTemp.getName());
        builder.setSrcIp(channelTemp.getSrcIp());
        builder.setDstGroup(channelTemp.getDstGroup());
        builder.setDomainId(channelTemp.getDomainId());
        builder.setSubDomainId(channelTemp.getSubDomainId());
        builder.setSourceWildcard(channelTemp.getSourceWildcard());
        builder.setGroupWildcard(channelTemp.getGroupWildcard());
        builder.setIngressNode(channelTemp.getIngressNode());
        builder.setIngressBfrId(channelTemp.getIngressBfrId());
        builder.setSrcTp(channelTemp.getSrcTp());
        builder.setBierForwardingType(channelTemp.getBierForwardingType());
        builder.setBpAssignmentStrategy(channelTemp.getBpAssignmentStrategy());
        return builder;
    }

    private void deployTeChannel(ConfigurationType type, String nodeId, Channel teChannel,
                                                List<Long> pathIdList) {
        ConfigurationResult writedChannelResult = teChannelWriter.writeBierTeChannel(type, nodeId,
                teChannel, pathIdList);
        if (!writedChannelResult.isSuccessful()) {
            biftInfoProcess.webSocketToApp(writedChannelResult.getFailureReason());
        }
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
            LOG.error("Get bierChannelList from networkChannel failed:" + e.getStackTrace());
            return null;
        }
        if (null == channel || null == channel.getChannel() || channel.getChannel().isEmpty()) {
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
