/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;

import java.util.Collection;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
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
            .child(BierChannel.class, new BierChannelKey("flow:1")).child(Channel.class);

    private BierChannelConfigProcess bierChannelConfigProcess;

    public ChannelChangeListener(final DataBroker dataBroker,ChannelConfigWriter bierConfig) {
        bierChannelConfigProcess = new BierChannelConfigProcess(dataBroker,bierConfig);
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
                    bierChannelConfigProcess.processAddedChannel(rootNode.getDataAfter());
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("onDataTreeChanged - BierChannel config with path {} was modified: "
                                    + "old BierChannel: {}, new BierChannel: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                            rootNode.getDataAfter());
                    bierChannelConfigProcess.processModifiedChannel(rootNode.getDataBefore(),rootNode.getDataAfter());
                    break;
                case DELETE:
                    LOG.info("onDataTreeChanged - BierChannel config with path {} was deleted: "
                                    + "old BierChannel: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore());
                    bierChannelConfigProcess.processDeletedChannel(rootNode.getDataBefore());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type : {}"
                            + change.getRootNode().getModificationType());
            }
        }
    }

    public InstanceIdentifier<Channel> getChannelId() {
        return CHANNEL_IID;
    }

}
