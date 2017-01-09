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
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.BierServiceApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;
    private final NotificationPublishService notificationService;
    private final ChannelConfigWriter bierChannelWriter;
    private final BierConfigWriter bierConfigWriter;

    private ServiceManager serviceManager;

    public ServiceProvider(final DataBroker dataBroker,final RpcProviderRegistry rpcRegistry,
                           final NotificationPublishService notificationService,
                           final BierConfigWriter bierConfigWriter, final ChannelConfigWriter channelConfigWriter) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;
        this.notificationService = notificationService;
        this.bierConfigWriter = bierConfigWriter;
        this.bierChannelWriter = channelConfigWriter;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("ServiceProvider Session Initiated");
        serviceManager = new ServiceManager(dataBroker, notificationService, bierConfigWriter, bierChannelWriter);
        rpcRegistry.addRpcImplementation(BierServiceApiService.class,serviceManager);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("ServiceProvider Closed");
    }
}