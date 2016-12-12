/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.impl;

import org.opendaylight.channel.util.ChannelDBContext;
import org.opendaylight.channel.util.ChannelDBUtil;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierChannelApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;
    private BindingAwareBroker.RpcRegistration<BierChannelApiService> channelService;

    public ChannelProvider(final DataBroker dataBroker, RpcProviderRegistry rpcRegistry) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("ChannelProvider Session Initiated");
        ChannelDBContext context = new ChannelDBContext(dataBroker);
        ChannelImpl channelImpl = new ChannelImpl();
        ChannelDBUtil.getInstance().setContext(context);
        ChannelDBUtil.getInstance().initDB();
        channelService = rpcRegistry.addRpcImplementation(BierChannelApiService.class,channelImpl);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("ChannelProvider Closed");
        if (channelService != null) {
            channelService.close();
        }
    }
}