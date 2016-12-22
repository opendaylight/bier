/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;

import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServiceManager {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceManager.class);

    private BierNodeChangeListener bierNodeChangeListener;
    private ChannelChangeListener channelChangeListener;

    public ServiceManager(final DataBroker dataBroker, BierConfigWriter bierConfig,
                          ChannelConfigWriter bierChannelWriter) {
        LOG.info("register bier-node listener");
        bierNodeChangeListener = new BierNodeChangeListener(bierConfig);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<BierNode>(
                LogicalDatastoreType.CONFIGURATION, bierNodeChangeListener.getBierNodeId()), bierNodeChangeListener);

        LOG.info("register bier-channel listener");
        channelChangeListener = new ChannelChangeListener(dataBroker,bierChannelWriter);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<Channel>(
                LogicalDatastoreType.CONFIGURATION, channelChangeListener.getChannelId()), channelChangeListener);
    }
}
