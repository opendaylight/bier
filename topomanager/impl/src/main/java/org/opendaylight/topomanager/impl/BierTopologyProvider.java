/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topomanager.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.BierTopologyApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTopologyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(BierTopologyProvider.class);

    private final RpcProviderRegistry rpcRegistry;
    private final DataBroker dataBroker;
    private final NotificationPublishService notificationService;
    private BierTopologyManager topoManager;
    private BierNodeChangeListenerImpl bierNodeChangeListener;
    private BierLinkChangeListenerImpl bierLinkChangeListener;
    private BierTpChangeListenerImpl bierTpChangeListener;

    private RpcRegistration<BierTopologyApiService> topoService = null;

    public BierTopologyProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcRegistry,
                                final NotificationPublishService notificationService) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;
        this.notificationService = notificationService;
    }

    public DataBroker getDataBroker() {
        return dataBroker;
    }

    /**
    * Method called when the blueprint container is created.
    */
    public void init() {
        LOG.info("BierTopologyProvider Session Initiated");
        NotificationProvider.getInstance().setNotificationService(notificationService);
        topoManager = new BierTopologyManager(dataBroker);
        topoService = rpcRegistry.addRpcImplementation(BierTopologyApiService.class,
                new BierTopologyServiceImpl(topoManager));

        topoManager.start();
        bierNodeChangeListener = new BierNodeChangeListenerImpl(dataBroker);
        bierLinkChangeListener = new BierLinkChangeListenerImpl(dataBroker);
        bierTpChangeListener = new BierTpChangeListenerImpl(dataBroker);
    }

    /**
    * Method called when the blueprint container is destroyed.
    */
    public void close() {
        LOG.info("BierTopologyProvider Closed");
        if (topoService != null) {
            topoService.close();
        }

        if (bierNodeChangeListener != null) {
            bierNodeChangeListener.close();
        }
        if (bierLinkChangeListener != null) {
            bierLinkChangeListener.close();
        }
        if (bierTpChangeListener != null) {
            bierTpChangeListener.close();
        }
    }
}
