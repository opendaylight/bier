/**
 * Copyright © 2016 www.bupt.edu.cn and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topomanager.impl;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Callable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.TopoChange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.TopoChangeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class BierDataTreeChangeListenerImpl<T extends DataObject>
        implements DataTreeChangeListener<T>, AutoCloseable {

    private static final Logger LOG =
            LoggerFactory.getLogger(BierDataTreeChangeListenerImpl.class);

    private static final long STARTUP_LOOP_TICK = 500L;

    private static final int STARTUP_LOOP_MAX_RETRIES = 8;

    protected final ListenerRegistration<DataTreeChangeListener> listenerRegistration;

    protected BierTopologyProcess bierTopologyProcess;

    protected static final InstanceIdentifier<Topology> II_TO_TOPOLOGY =
            InstanceIdentifier
                    .create(NetworkTopology.class)
                    .child(Topology.class, new TopologyKey(new TopologyId("flow:1")));

    public BierDataTreeChangeListenerImpl(final BierTopologyProcess operationProcessor,
                                      final DataBroker dataBroker,
                                      final InstanceIdentifier<T> ii) {
        final DataTreeIdentifier<T> identifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, ii);
        final SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK, STARTUP_LOOP_MAX_RETRIES);
        try {
            listenerRegistration = looper.loopUntilNoException(
                    new Callable<ListenerRegistration<DataTreeChangeListener>>() {
                        @Override
                        public ListenerRegistration<DataTreeChangeListener> call() throws Exception {
                            return dataBroker.registerDataTreeChangeListener(identifier,
                                    BierDataTreeChangeListenerImpl.this);
                        }
                    });
        } catch (Exception e) {
            LOG.error("Data listener registration failed!");
            throw new IllegalStateException("TopologyManager startup fail! TM bundle needs restart.", e);
        }
        this.bierTopologyProcess = operationProcessor;
    }

    @Override
    public void close() throws Exception {
        listenerRegistration.close();
    }

    protected <T extends DataObject> void sendToTransactionChain(final T node,
                                                                 final InstanceIdentifier<T> iiToTopologyNode) {
        bierTopologyProcess.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(final ReadWriteTransaction transaction) {
                transaction.merge(LogicalDatastoreType.OPERATIONAL, iiToTopologyNode, node, true);

            }

            @Override
            public <T> ListenableFuture<T> readOperation(final ReadWriteTransaction transaction) {
                return null;
            }
        });
    }

    protected InstanceIdentifier<BierNode> provideIIToTopologyNode(final String nodeIdInTopology) {
        BierNodeKey nodeKeyInTopology = new BierNodeKey(nodeIdInTopology);
        return II_TO_TOPOLOGY
                .create(BierTopology.class)
                .child(BierNode.class, nodeKeyInTopology);
    }

    protected String provideTopologyNodeId(final InstanceIdentifier<T> iiToNodeInInventory) {
        final BierNodeKey inventoryNodeKey = iiToNodeInInventory.firstKeyOf(BierNode.class);
        if (inventoryNodeKey != null) {
            return inventoryNodeKey.getNodeId();
        }
        return null;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    protected InstanceIdentifier<BierLink> provideIIToTopologyLink(final String linkIdInTopology) {
        BierLinkKey linkKeyInTopology = new BierLinkKey(linkIdInTopology);
        return II_TO_TOPOLOGY
                .create(BierTopology.class)
                .child(BierLink.class, linkKeyInTopology);
    }

    protected String provideTopologyLinkId(final InstanceIdentifier<T> iiToNodeInInventory) {
        final BierLinkKey inventoryLinkKey = iiToNodeInInventory.firstKeyOf(BierLink.class);
        if (inventoryLinkKey != null) {
            return inventoryLinkKey.getLinkId();
        }
        return null;
    }

    protected void notifyTopoChange(String topoId) {
        TopoChange notification = new TopoChangeBuilder().setTopoId(topoId).build();
        LOG.info("notify TopoChange:" + topoId);
        NotificationProvider.getInstance().notify(notification);
    }
}
