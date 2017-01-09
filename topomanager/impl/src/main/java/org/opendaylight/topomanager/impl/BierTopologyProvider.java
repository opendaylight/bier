/*
 * Copyright © 2016 www.bupt.edu.cn and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTopologyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(BierTopologyProvider.class);

    private final RpcProviderRegistry rpcRegistry;
    private final DataBroker dataBroker;
    private final NotificationPublishService notificationService;
    private BierTopologyManager topoManager;
    private BierNodeChangeListenerImpl nodeListener;

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
        BierTopologyProcess<BierNode> processor =  new BierTopologyProcess<BierNode>(dataBroker,
                BierTopologyProcess.FLAG_WRITE,(new BierNodeBuilder()).build());
        nodeListener = new BierNodeChangeListenerImpl(dataBroker,processor);

        topoManager.start();
    }

    /**
    * Method called when the blueprint container is destroyed.
    */
    public void close() {
        LOG.info("BierTopologyProvider Closed");
        if (topoService != null) {
            topoService.close();
        }
        try {
            if (nodeListener != null) {
                nodeListener.close();
            }
        } catch (Exception e) {
            LOG.error("close nodeListener error!");
        }
    }
}

// Following is previous method
// import
// org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
// import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
// import
// org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
// import
// org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloService;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// public class HelloProvider implements BindingAwareProvider, AutoCloseable {
//
// private static final Logger LOG =
// LoggerFactory.getLogger(HelloProvider.class);
//
// private RpcRegistration<HelloService> helloService = null;
//
// @Override
// public void onSessionInitiated(ProviderContext session) {
// LOG.info("HelloProvider Session Initiated");
// helloService = session.addRpcImplementation(HelloService.class, new
// HelloImpl());
// }
//
// @Override
// public void close() throws Exception {
// LOG.info("HelloProvider Closed");
// if (helloService != null) {
// helloService.close();
// }
// }
//
// }
