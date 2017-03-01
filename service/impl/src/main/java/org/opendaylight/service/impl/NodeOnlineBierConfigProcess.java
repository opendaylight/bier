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
            bierNodeChangeListener.processAddedNode(bierNode);
            processSendChannelContainThisNode(nodeId);
        } else {
            LOG.info("BierNode is null");
        }
    }

    private BierNode queryBierNodeById(String nodeId) {
        if (null == nodeId) {
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
            }
        } catch (ReadFailedException e) {
            LOG.error("Get bierNode from bier topology is null");
        }
        if (null != node.getBierNodeParams()) {
            return node;
        } else {
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
            }
        } catch (ReadFailedException e) {
            LOG.error("Get bierChannel from networkChannel is null");
        }
        if (0 == channel.getChannel().size()) {
            LOG.info("ChannelList is null");
        } else {
            findChannelContainThisNodeAndSend(channel.getChannel(),nodeId);
        }
    }

    private void findChannelContainThisNodeAndSend(List<Channel> channelList,String nodeId) {
        for (Channel channel : channelList) {
            if (null == channel.getIngressNode()) {
                LOG.info("IngressNode is null");
            } else if (channel.getIngressNode().equals("node" + nodeId)) {
                LOG.info("channel: {} is match, process send",channel.getIngressNode());
                bierChannelConfigProcess.processAddedChannel(channel);
            } else {
                LOG.info("channel: {} is not match",channel.getIngressNode());
            }
        }
    }
}
