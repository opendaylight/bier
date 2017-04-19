/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;

import com.google.common.base.Optional;
import java.util.List;
import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeOnlineBierConfigProcess {

    private static final Logger LOG = LoggerFactory.getLogger(NodeOnlineBierConfigProcess.class);

    private DataBroker dataBroker;
    private BierNodeChangeListener bierNodeChangeListener;
    private BierChannelConfigProcess bierChannelConfigProcess;

    public NodeOnlineBierConfigProcess(DataBroker dataBroker,BierConfigWriter bierConfigWriter,
                                       ChannelConfigWriter channelConfigWriter) {
        this.dataBroker = dataBroker;
        bierNodeChangeListener = new BierNodeChangeListener(bierConfigWriter);
        bierChannelConfigProcess = new BierChannelConfigProcess(dataBroker,channelConfigWriter);
    }

    public void queryBierConfigAndSendForNodeOnline(String nodeId) {
        BierNode bierNode = queryBierNodeById(nodeId);
        if (null != bierNode) {
            LOG.info("process send bier config");
            bierNodeChangeListener.processAddedNode(bierNode);
            LOG.info("process send channel");
            processSendChannelContainThisNode(nodeId);
        }
    }

    private BierNode queryBierNodeById(String nodeId) {
        if (null == nodeId) {
            LOG.info("nodeId is null");
            return null;
        }
        final InstanceIdentifier<BierNode> path = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class,new BierTopologyKey("flow:1"))
                .child(BierNode.class,new BierNodeKey(nodeId));
        final ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        Optional<BierNode> bierNode = null;
        BierNode node = null;
        try {
            bierNode = tx.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (bierNode.isPresent()) {
                node = bierNode.get();
                LOG.info("get bierNode from bier topology success, info is {}",node);
            }
        } catch (ReadFailedException e) {
            LOG.error("Get bierNode from bier topology is null");
        }
        if (null != node.getBierNodeParams()) {
            if (null != node.getBierNodeParams().getDomain() && 0 != node.getBierNodeParams().getDomain().size()) {
                LOG.info("Node has bier config");
                return node;
            } else {
                return null;
            }
        } else {
            LOG.info("Node has no bier config");
            return null;
        }
    }

    private void processSendChannelContainThisNode(String nodeId) {
        final InstanceIdentifier<BierChannel> path = InstanceIdentifier.create(BierNetworkChannel.class)
                .child(BierChannel.class,new BierChannelKey("flow:1"));
        final ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        Optional<BierChannel> bierChannel = null;
        BierChannel channel = null;
        try {
            bierChannel = tx.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (bierChannel.isPresent()) {
                channel = bierChannel.get();
                LOG.info("get bierChannel from networkChannel success,bierChannel is {}",channel);
            }
        } catch (ReadFailedException e) {
            LOG.error("Get bierChannel from networkChannel is null");
        }
        if (null == channel) {
            LOG.info("BierChannel is null");
        } else if (null == channel.getChannel()) {
            LOG.info("ChannelList is null");
        } else if (0 == channel.getChannel().size()) {
            LOG.info("ChannelList has no element");
        } else {
            LOG.info("process find channel contain this node and send");
            findChannelContainThisNodeAndSend(channel.getChannel(),nodeId);
        }
    }

    private void findChannelContainThisNodeAndSend(List<Channel> channelList,String nodeId) {
        for (Channel channel : channelList) {
            if (null == channel.getIngressNode()) {
                LOG.info("IngressNode is null");
            } else if (channel.getIngressNode().equals(nodeId)) {
                LOG.info("IngressNode: {} is match, process send channel",channel.getIngressNode());
                bierChannelConfigProcess.processAddedChannel(channel);
            } else {
                LOG.info("IngressNode: {} is not match",channel.getIngressNode());
            }
        }
    }
}
