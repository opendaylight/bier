/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl;

import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.BierTeBtaftWriter;
import org.opendaylight.bier.adapter.api.BierTeChannelWriter;
import org.opendaylight.bier.adapter.api.BierTeLabelRangeConfigWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.bier.service.impl.allocatebp.TopoBasedBpAllocateStrategy;
import org.opendaylight.bier.service.impl.bierconfig.BierNodeChangeListener;
import org.opendaylight.bier.service.impl.teconfig.AddResetBitMaskProcess;
import org.opendaylight.bier.service.impl.teconfig.BierNodeTeBpChangeListener;
import org.opendaylight.bier.service.impl.teconfig.BierServiceApiImpl;
import org.opendaylight.bier.service.impl.teconfig.BierTeLabelRangeChangeListener;
import org.opendaylight.bier.service.impl.teconfig.BiftInfoProcess;
import org.opendaylight.bier.service.impl.teconfig.BitStringProcess;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.frr.rev171122.te.frr.configure.topology.te.frr.link.te.frr.te.frr.domain.te.frr.sub.domain.te.frr.bsl.TeFrrSi;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.BierServiceApiService;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeLableRange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServiceManager {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceManager.class);

    private BierNodeChangeListener bierNodeChangeListener;
    private ChannelChangeListener channelChangeListener;
    private NetconfStateChangeListener netconfStateChangeListener;
    private BierNodeTeBpChangeListener bierNodeTeBpChangeListener;
    private BierTeLabelRangeChangeListener bierTeLabelRangeChangeListener;
    private TeFrrBpChangeListener teFrrBpChangeListener;
    private BindingAwareBroker.RpcRegistration<BierServiceApiService> service;

    public ServiceManager(final DataBroker dataBroker, final NotificationPublishService notificationService,
                          final RpcConsumerRegistry rpcConsumerRegistry,
                          final RpcProviderRegistry rpcProviderRegistry, BierConfigWriter bierConfig,
                          ChannelConfigWriter channelConfigWriter, BierTeChannelWriter teChannelWriter,
                          BierTeBiftWriter bierTeBiftWriter, BierTeBtaftWriter bierTeBtaftWriter,
                          BierTeBitstringWriter bierTeBitstringWriter,
                          BierTeLabelRangeConfigWriter bierTeLabelRangeConfigWriter,
                          NotificationService registerService) {
        LOG.info("set notificationPublishService");
        NotificationProvider.getInstance().setNotificationService(notificationService);
        NotificationProvider.getInstance().setRegisterService(registerService);

        LOG.info("register bier-node listener");
        bierNodeChangeListener = new BierNodeChangeListener(bierConfig);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<BierNodeParams>(
                LogicalDatastoreType.CONFIGURATION, bierNodeChangeListener.getBierNodeId()), bierNodeChangeListener);

        LOG.info("register bier-channel listener");
        channelChangeListener = new ChannelChangeListener(dataBroker, rpcConsumerRegistry, channelConfigWriter,
                teChannelWriter, bierTeBiftWriter,bierTeBitstringWriter);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<Channel>(
                LogicalDatastoreType.CONFIGURATION, channelChangeListener.getChannelId()), channelChangeListener);

        LOG.info("register netconfstate listener");
        netconfStateChangeListener = new NetconfStateChangeListener(dataBroker,bierConfig,channelConfigWriter);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<Node>(
                LogicalDatastoreType.OPERATIONAL, netconfStateChangeListener.getNodeId()), netconfStateChangeListener);

        LOG.info("register bier-node-tebp listener");
        bierNodeTeBpChangeListener = new BierNodeTeBpChangeListener(dataBroker, bierTeBiftWriter);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<TeBp>(
                        LogicalDatastoreType.CONFIGURATION, bierNodeTeBpChangeListener.getTeBpIid()),
                bierNodeTeBpChangeListener);
        LOG.info("register bier-te-lable-range");
        bierTeLabelRangeChangeListener = new BierTeLabelRangeChangeListener(bierTeLabelRangeConfigWriter);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<BierTeLableRange>(
                LogicalDatastoreType.CONFIGURATION, bierTeLabelRangeChangeListener.getBierTeLableRangeIid()),
                bierTeLabelRangeChangeListener);

        LOG.info("Register te-frr-tebp listener");
        teFrrBpChangeListener = new TeFrrBpChangeListener(dataBroker);
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<TeFrrSi>(
                        LogicalDatastoreType.CONFIGURATION, teFrrBpChangeListener.getTeFrrSiIid()),
                teFrrBpChangeListener);

        BierServiceApiImpl bierServiceApi = new BierServiceApiImpl(dataBroker, rpcConsumerRegistry);
        service = rpcProviderRegistry.addRpcImplementation(BierServiceApiService.class, bierServiceApi);

        LOG.info("Resume bp allocate strategy params!");
        TopoBasedBpAllocateStrategy.getInstance().setDataBroker(dataBroker);
        TopoBasedBpAllocateStrategy.getInstance().setRpcConsumerRegistry(rpcConsumerRegistry);
        TopoBasedBpAllocateStrategy.getInstance().resumeAbstractDataStructures();
        TopoBasedBpAllocateStrategy.getInstance().resumeTopoBasedDataStructures();

//        PathMonopolyBPAllocateStrategy.getInstance().setDataBroker(dataBroker);
//        PathMonopolyBPAllocateStrategy.getInstance().setRpcConsumerRegistry(rpcConsumerRegistry);

        LOG.info("Initialize add reset bit mask process");
        AddResetBitMaskProcess addResetBitMaskProcess = AddResetBitMaskProcess.getInstance();
        addResetBitMaskProcess.setBierTeBiftWriter(bierTeBiftWriter);
        addResetBitMaskProcess.setBierTeBtaftWriter(bierTeBtaftWriter);
        addResetBitMaskProcess.setRpcConsumerRegistry(rpcConsumerRegistry);
        addResetBitMaskProcess.setUtil(new Util(dataBroker));
        addResetBitMaskProcess.setBiftInfoProcess(new BiftInfoProcess(dataBroker, bierTeBiftWriter));
        addResetBitMaskProcess.setBitStringProcess(new BitStringProcess(dataBroker, rpcConsumerRegistry,
                bierTeBitstringWriter, bierTeBiftWriter));
        addResetBitMaskProcess.setBpAllocateStrategy(TopoBasedBpAllocateStrategy.getInstance());
    }

    public void close() {
        if (null != service) {
            service.close();
        }
    }

}
