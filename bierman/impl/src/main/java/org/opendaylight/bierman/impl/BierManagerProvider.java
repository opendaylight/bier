/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl;

import org.opendaylight.bier.adapter.api.DeviceInterfaceReader;
import org.opendaylight.bierman.impl.bierconfig.BierConfigServiceImpl;
import org.opendaylight.bierman.impl.teconfig.BierBpAllocateParamsConfigServiceImpl;
import org.opendaylight.bierman.impl.teconfig.BierTeConfigServiceImpl;
import org.opendaylight.bierman.impl.teconfig.BierTeFrrConfigServiceImpl;
import org.opendaylight.bierman.impl.topo.BierLinkChangeListenerImpl;
import org.opendaylight.bierman.impl.topo.BierNodeChangeListenerImpl;
import org.opendaylight.bierman.impl.topo.BierTopologyServiceImpl;
import org.opendaylight.bierman.impl.topo.BierTpChangeListenerImpl;
import org.opendaylight.bierman.impl.topo.NetConfigStateChangeListenerImpl;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.bp.allocate.rev170818.BierBpAllocateParamsConfigService;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.BierConfigApiService;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.BierTeConfigApiService;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.BierTopologyApiService;
import org.opendaylight.yang.gen.v1.urn.bier.te.frr.config.api.rev171128.BierTeFrrConfigApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierManagerProvider {

    private static final Logger LOG = LoggerFactory.getLogger(BierManagerProvider.class);

    private final RpcProviderRegistry rpcRegistry;
    private final DataBroker dataBroker;
    private final NotificationPublishService notificationService;
    private BierDataManager topoManager;
    private DeviceInterfaceReader deviceInterfaceReader;
    private BierNodeChangeListenerImpl bierNodeChangeListener;
    private BierLinkChangeListenerImpl bierLinkChangeListener;
    private BierTpChangeListenerImpl bierTpChangeListener;
    private NetConfigStateChangeListenerImpl netConfStateChangeListener;

    private RpcRegistration<BierTopologyApiService> topoService = null;
    private RpcRegistration<BierConfigApiService> bierCfgService = null;
    private RpcRegistration<BierTeConfigApiService> bierTeCfgService = null;
    private RpcRegistration<BierBpAllocateParamsConfigService> bpAllocateService = null;
    private RpcRegistration<BierTeFrrConfigApiService> bierTeFrrCfgService = null;

    public BierManagerProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcRegistry,
                                final NotificationPublishService notificationService,
                                final DeviceInterfaceReader deviceInterfaceReader) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;
        this.notificationService = notificationService;
        this.deviceInterfaceReader = deviceInterfaceReader;
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
        topoManager = new BierDataManager(dataBroker);
        topoService = rpcRegistry.addRpcImplementation(BierTopologyApiService.class,
                new BierTopologyServiceImpl(topoManager));

        bierCfgService = rpcRegistry.addRpcImplementation(BierConfigApiService.class,
                new BierConfigServiceImpl(topoManager));

        bierTeCfgService = rpcRegistry.addRpcImplementation(BierTeConfigApiService.class,
                new BierTeConfigServiceImpl(topoManager));

        bpAllocateService = rpcRegistry.addRpcImplementation(BierBpAllocateParamsConfigService.class,
                new BierBpAllocateParamsConfigServiceImpl(dataBroker));

        bierTeFrrCfgService = rpcRegistry.addRpcImplementation(BierTeFrrConfigApiService.class,
                new BierTeFrrConfigServiceImpl(dataBroker,topoManager));

        topoManager.start();
        bierNodeChangeListener = new BierNodeChangeListenerImpl(dataBroker);
        bierLinkChangeListener = new BierLinkChangeListenerImpl(dataBroker);
        bierTpChangeListener = new BierTpChangeListenerImpl(dataBroker);
        netConfStateChangeListener = new NetConfigStateChangeListenerImpl(dataBroker,topoManager,deviceInterfaceReader);
    }

    /**
    * Method called when the blueprint container is destroyed.
    */
    public void close() {
        LOG.info("BierTopologyProvider Closed");
        if (topoService != null) {
            topoService.close();
        }

        if (bierCfgService != null) {
            bierCfgService.close();
        }

        if (bierTeCfgService != null) {
            bierTeCfgService.close();
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
        if (netConfStateChangeListener != null) {
            netConfStateChangeListener.close();
        }
        if (bpAllocateService != null) {
            bpAllocateService.close();
        }
        if (bierTeFrrCfgService != null) {
            bierTeFrrCfgService.close();
        }
    }
}
