/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl;

import java.util.Collection;

import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.BierTeChannelWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.bier.service.impl.bierconfig.BierChannelConfigProcess;
import org.opendaylight.bier.service.impl.teconfig.BierTeChannelProcess;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierForwardingType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChannelChangeListener implements DataTreeChangeListener<Channel> {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelChangeListener.class);

    private static final InstanceIdentifier<Channel> CHANNEL_IID = InstanceIdentifier.create(BierNetworkChannel.class)
            .child(BierChannel.class, new BierChannelKey("example-linkstate-topology"))
            .child(Channel.class);


    private BierChannelConfigProcess bierChannelConfigProcess;
    private BierTeChannelProcess bierTeChannelProcess;

    public ChannelChangeListener(final DataBroker dataBroker, final RpcConsumerRegistry rpcConsumerRegistry,
                                 ChannelConfigWriter channelConfigWriter, BierTeChannelWriter teChannelWriter,
                                 BierTeBiftWriter bierTeBiftWriter,BierTeBitstringWriter bierTeBitstringWriter) {
        bierChannelConfigProcess = new BierChannelConfigProcess(dataBroker, channelConfigWriter);
        bierTeChannelProcess = new BierTeChannelProcess(dataBroker, rpcConsumerRegistry, teChannelWriter,
                bierTeBiftWriter,bierTeBitstringWriter);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Channel>> changes) {
        for (DataTreeModification<Channel> change: changes) {
            DataObjectModification<Channel> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    LOG.info("onDataTreeChanged - BierChannel config with path {} was added or replaced: "
                                    + "old BierChannel: {}, new BierChannel: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                            rootNode.getDataAfter());
                    if (null == rootNode.getDataBefore()) {
                        processAddedChannel(rootNode.getDataAfter());
                    } else {
                        processModifiedChannel(rootNode.getDataBefore(), rootNode.getDataAfter());
                    }
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("onDataTreeChanged - BierChannel config with path {} was modified: "
                                    + "old BierChannel: {}, new BierChannel: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                            rootNode.getDataAfter());
                    processModifiedChannel(rootNode.getDataBefore(),rootNode.getDataAfter());
                    break;
                case DELETE:
                    LOG.info("onDataTreeChanged - BierChannel config with path {} was deleted: "
                                    + "old BierChannel: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore());
                    processDeletedChannel(rootNode.getDataBefore());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type : {}"
                            + change.getRootNode().getModificationType());
            }
        }
    }

    public void processAddedChannel(Channel channel) {
        LOG.info("Check channel input");
        if (!checkChannelInput(channel)) {
            LOG.info("does not deploy-channel,do nothing!");
            return;
        }

        LOG.info("Add Channel!");
        if (channel.getBierForwardingType().equals(BierForwardingType.BierTe)) {
            LOG.info("Process add bier-te channel ");
            bierTeChannelProcess.processAddedTeChannel(channel);
        } else {
            LOG.info("Process add bier channel");
            bierChannelConfigProcess.processAddedChannel(channel);
        }
    }

    public void processDeletedChannel(Channel channel) {
        LOG.info("Check channel input");
        if (!checkChannelInput(channel)) {
            LOG.error("Channel input error!");
            return;
        }

        LOG.info("Del Channel!");
        if (channel.getBierForwardingType().equals(BierForwardingType.BierTe)) {
            LOG.info("Process delete bier-te channel");
            bierTeChannelProcess.processDeletedTeChannel(channel);
        } else {
            LOG.info("Process delete bier channel");
            bierChannelConfigProcess.processDeletedChannel(channel);
        }
    }

    public void processModifiedChannel(Channel before, Channel after) {
        if (null == before) {
            return;
        }
        if (!checkChannelInput(after)) {
            LOG.error("Channel input error!");
            return;
        }

        if (after.getBierForwardingType().equals(BierForwardingType.BierTe)) {
            if (before.getBierForwardingType() == null) {
                LOG.info("Process add bier-te channel ");
                bierTeChannelProcess.processAddedTeChannel(after);
            } else {
                LOG.info("Process modify bier-te channel");
                bierTeChannelProcess.processModifiedTeChannel(before, after);
            }
        } else {
            LOG.info("process deploy bier channel!");
            bierChannelConfigProcess.processModifiedChannel(before,after);
        }
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
        if (null == channel.getBierForwardingType()) {
            return false;
        }
        return true;
    }

    public InstanceIdentifier<Channel> getChannelId() {
        return CHANNEL_IID;
    }

}
