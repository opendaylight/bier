/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;

import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierNetworkChannel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;


/**
 * Created by 10200860 on 2016/11/30.
 */
public class BierChannelChangeListener implements DataTreeChangeListener<BierChannel> {

    private static final Logger LOG = LoggerFactory.getLogger(BierChannelChangeListener.class);

    private static final InstanceIdentifier<BierChannel> BIER_CHANNEL_IID = InstanceIdentifier.create(BierNetworkChannel.class)
            .child(BierChannel.class);

    private final DataBroker dataProvider;

    public BierChannelChangeListener(final DataBroker dataBroker) {
        this.dataProvider = dataBroker;
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<BierChannel>> changes) {

        for (DataTreeModification<BierChannel> change: changes) {
            DataObjectModification<BierChannel> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()){
                case WRITE:
                    LOG.info("onDataTreeChanged - BierChannel config with path {} was added or replaced: old BierChannel: {}, new BierChannel: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(), rootNode.getDataAfter());
                    processAddChannel(rootNode.getDataAfter());
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("onDataTreeChanged - BierChannel config with path {} was modified: old BierChannel: {}, new BierChannel: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(), rootNode.getDataAfter());
                    processModifiedChannel(rootNode.getDataBefore(),rootNode.getDataAfter());
                    break;
                case DELETE:
                    LOG.info("onDataTreeChanged - BierChannel config with path {} was deleted: old BierChannel: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore());
                    processDeletedChannel(rootNode.getDataBefore());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type : {}" + change.getRootNode().getModificationType());
            }
        }
    }

    public InstanceIdentifier<BierChannel> getBierChannelIid() {
        return BIER_CHANNEL_IID;
    }

    public void processAddChannel(BierChannel after) {

    }

    public void processModifiedChannel(BierChannel before, BierChannel after) {

    }

    public void processDeletedChannel(BierChannel before) {

    }


}
