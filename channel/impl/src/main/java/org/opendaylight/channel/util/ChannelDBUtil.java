/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.util;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.AddChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.DeployChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.ModifyChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.QueryChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.RemoveChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChannelDBUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ChannelDBUtil.class);
    private ChannelDBContext context;
    private static ChannelDBUtil instance = new ChannelDBUtil();

    public ChannelDBUtil() {
    }


    public void setContext(ChannelDBContext context) {
        this.context = context;
    }

    public static ChannelDBUtil getInstance() {
        return instance;
    }

    public boolean isChannelExists(String name, String topologyId) {
        Optional<Channel> channel = readChannel(name,topologyId);

        if (channel == null || !channel.isPresent()) {
            return false;
        } else {
            return true;
        }
    }

    private Optional<Channel> readChannel(String name, String topologyId) {
        ReadOnlyTransaction rtx = context.newReadOnlyTransaction();
        try {
            return rtx.read(LogicalDatastoreType.CONFIGURATION,
                    buildChannelPath(name,topologyId)).get();

        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Channel:occur exception when read databroker {}", e);
            return null;

        }
    }

    private Optional<BierChannel> readBierChannel(String topologyId) {
        ReadOnlyTransaction rtx = context.newReadOnlyTransaction();
        try {
            return rtx.read(LogicalDatastoreType.CONFIGURATION,
                    buildBierChannelPath(topologyId)).get();

        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Channel:occur exception when read databroker {}", e);
            return null;

        }
    }

    private Optional<BierNetworkChannel> readBierNetworkChannel() {
        ReadOnlyTransaction rtx = context.newReadOnlyTransaction();
        try {
            return rtx.read(LogicalDatastoreType.CONFIGURATION,
                    buildBierNetworkChannelPath()).get();

        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Channel:occur exception when read databroker {}", e);
            return null;

        }
    }

    private InstanceIdentifier<Channel> buildChannelPath(String name, String topologyId) {
        return InstanceIdentifier.create(BierNetworkChannel.class)
                .child(BierChannel.class, new BierChannelKey(topologyId))
                .child(Channel.class, new ChannelKey(name));
    }

    private InstanceIdentifier<BierNetworkChannel> buildBierNetworkChannelPath() {
        return InstanceIdentifier.create(BierNetworkChannel.class);
    }

    public boolean writeChannelToDB(AddChannelInput input) {
        WriteTransaction wtx = context.newWriteOnlyTransaction();
        boolean result = true;

        if (!isBierTopoIdExists(input.getTopologyId())) {
            BierChannel bierChannel = new BierChannelBuilder()
                    .setTopologyId(input.getTopologyId())
                    .build();
            wtx.put(LogicalDatastoreType.CONFIGURATION,
                    buildBierChannelPath(input.getTopologyId()),bierChannel,true);
            result = submitTransaction(wtx);
        }

        if (result) {
            ChannelBuilder channelBuilder = new ChannelBuilder(input);
            wtx.put(LogicalDatastoreType.CONFIGURATION,
                    buildChannelPath(input.getName(),input.getTopologyId()),channelBuilder.build(),true);
            result = submitTransaction(wtx);
        }
        return result;
    }


    public boolean writeDeployChannelToDB(DeployChannelInput input) {
        WriteTransaction wtx = context.newWriteOnlyTransaction();
        Channel channel = buildDeployChannelInfo(input);
        wtx.merge(LogicalDatastoreType.CONFIGURATION,
                buildChannelPath(input.getChannelName(),input.getTopologyId()),channel,true);
        return submitTransaction(wtx);

    }

    private Channel buildDeployChannelInfo(DeployChannelInput input) {
        List<EgressNode> egressNodeList = new ArrayList<>();
        for (org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.deploy.channel.input.EgressNode egressNode
                : input.getEgressNode()) {
            egressNodeList.add(new EgressNodeBuilder().setNodeId(egressNode.getNodeId()).build());
        }
        return new ChannelBuilder().setIngressNode(input.getIngressNode()).setEgressNode(egressNodeList).build();
    }

    private boolean isBierTopoIdExists(String topologyId) {
        Optional<BierChannel> bierChannel = readBierChannel(topologyId);

        if (bierChannel == null || !bierChannel.isPresent()) {
            return false;
        } else {
            return true;
        }


    }

    private InstanceIdentifier<BierChannel> buildBierChannelPath(String topologyId) {
        return InstanceIdentifier.create(BierNetworkChannel.class)
                .child(BierChannel.class, new BierChannelKey(topologyId));
    }

    private boolean submitTransaction(WriteTransaction writeTransaction) {
        try {
            writeTransaction.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Channel:write DB fail!", e);
            return false;
        }
        return true;
    }

    public boolean deleteChannelFromDB(RemoveChannelInput input) {
        if (!isChannelExists(input.getChannelName(),input.getTopologyId())) {
            return true;
        }
        WriteTransaction wtx = context.newWriteOnlyTransaction();

        wtx.delete(LogicalDatastoreType.CONFIGURATION, buildChannelPath(input.getChannelName(),input.getTopologyId()));
        return submitTransaction(wtx);

    }

    public boolean modifyChannelToDB(ModifyChannelInput input) {
        WriteTransaction wtx = context.newWriteOnlyTransaction();

        ChannelBuilder channelBuilder = new ChannelBuilder(input);
        wtx.merge(LogicalDatastoreType.CONFIGURATION,
                buildChannelPath(input.getName(),input.getTopologyId()),channelBuilder.build(),true);
        return submitTransaction(wtx);
    }

    public List<Channel> queryChannels(QueryChannelInput input) {
        List<Channel> channels = new ArrayList<>();
        for (String channelName : input.getChannelName()) {
            Optional<Channel> channel = readChannel(channelName, input.getTopologyId());
            if (channel != null && channel.isPresent()) {
                channels.add(channel.get());
            }
        }
        return channels;
    }

    public List<String> getChannels(String topoId) {
        List<String> channelNames = new ArrayList<>();
        Optional<BierChannel> bierChannel = readBierChannel(topoId);
        if (bierChannel != null && bierChannel.isPresent()) {
            for (Channel channel : bierChannel.get().getChannel()) {
                channelNames.add(channel.getName());
            }
        }
        return channelNames;
    }

    public void initDB() {
        WriteTransaction wtx = context.newWriteOnlyTransaction();

        Optional<BierNetworkChannel> bierChannels = readBierNetworkChannel();
        if (bierChannels == null || !bierChannels.isPresent()) {
            wtx.put(LogicalDatastoreType.CONFIGURATION, buildBierNetworkChannelPath(),
                    new BierNetworkChannelBuilder().build());
        }
        submitTransaction(wtx);
    }

}
