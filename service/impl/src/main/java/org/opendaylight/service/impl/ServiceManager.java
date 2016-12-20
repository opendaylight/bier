/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;

import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by 10200860 on 2016/12/1.
 */
public class ServiceManager {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceManager.class);

    private final DataBroker dataProvider;
    private BierNodeChangeListener bierNodeChangeListener;
    private ChannelChangeListener channelChangeListener;
    private BierConfigWriter bierConfigWriter;

    public ServiceManager(final DataBroker dataBroker, BierConfigWriter bierConfig) {
        dataProvider = dataBroker;
        bierConfigWriter = bierConfig;
        LOG.info("register bier-node listener");
        bierNodeChangeListener = new BierNodeChangeListener(dataProvider, bierConfigWriter);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<BierNode>(
                LogicalDatastoreType.CONFIGURATION, bierNodeChangeListener.getBierNodeIid()), bierNodeChangeListener);

        LOG.info("register bier-channel listener");
        channelChangeListener = new ChannelChangeListener(dataProvider);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<Channel>(
                LogicalDatastoreType.CONFIGURATION, channelChangeListener.getChannelIid()), channelChangeListener);
    }
}
