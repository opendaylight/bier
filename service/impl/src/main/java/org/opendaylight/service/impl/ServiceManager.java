/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType.CONFIGURATION;

/**
 * Created by 10200860 on 2016/12/1.
 */
public class ServiceManager {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceManager.class);

    private final DataBroker dataProvider;

    public ServiceManager(final DataBroker dataBroker) {
        dataProvider = dataBroker;
        LOG.info("register bier-node listener");
        BierNodeChangeListener bierNodeChangeListener = new BierNodeChangeListener(dataBroker);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<BierNode>(CONFIGURATION, bierNodeChangeListener.getBierNodeIid()
        ), bierNodeChangeListener);

        LOG.info("register bier-channel listener");
        BierChannelChangeListener bierChannelChangeListener = new BierChannelChangeListener(dataBroker);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<BierChannel>(CONFIGURATION, bierChannelChangeListener.getBierChannelIid()
        ), bierChannelChangeListener);
    }
}
