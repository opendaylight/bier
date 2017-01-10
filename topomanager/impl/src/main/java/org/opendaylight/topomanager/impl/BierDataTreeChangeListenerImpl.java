/**
 * Copyright © 2016 www.bupt.edu.cn and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topomanager.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.TopoChange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.TopoChangeBuilder;
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

    protected static final String TOPOLOGY_IID = "flow:1";

    protected  static final BierTopologyAdapter BIER_TOPOLOGY_ADAPTER = new BierTopologyAdapter();

    protected DataBroker dataBroker;

    protected final ListenerRegistration<DataTreeChangeListener> listenerRegistration;
    /*
    protected static final InstanceIdentifier<Topology> II_TO_TOPOLOGY =
            InstanceIdentifier
                    .create(NetworkTopology.class)
                    .child(Topology.class, new TopologyKey(new TopologyId("flow:1")));
    */

    public BierDataTreeChangeListenerImpl(final DataBroker dataBroker,
                                          final InstanceIdentifier<T> ii) {
        final DataTreeIdentifier<T> identifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, ii);
        this.dataBroker = dataBroker;
        final SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK, STARTUP_LOOP_MAX_RETRIES);
        /*try {
            listenerRegistration = looper.loopUntilNoException(
                    new Callable<ListenerRegistration<DataTreeChangeListener>>() {
                        @Override
                        public ListenerRegistration<DataTreeChangeListener> call() {
                            return dataBroker.registerDataTreeChangeListener(identifier,
                                    BierDataTreeChangeListenerImpl.this);
                        }
                    });
        } catch (Exception e) {
            LOG.error("Data listener registration failed!");
            throw new IllegalStateException("TopologyManager startup fail! TM bundle needs restart.", e);
        }
        */
        listenerRegistration = dataBroker.registerDataTreeChangeListener(identifier,
                BierDataTreeChangeListenerImpl.this);
    }

    @Override
    public void close() {
        listenerRegistration.close();
    }

    protected void notifyTopoChange(final String topoId) {
        TopoChange notification = new TopoChangeBuilder().setTopoId(topoId).build();
        LOG.info("notify TopoChange:" + topoId);
        NotificationProvider.getInstance().notify(notification);
    }
}
