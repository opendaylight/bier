/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl.topo;

import org.opendaylight.bierman.impl.BierDataAdapter;
import org.opendaylight.bierman.impl.NotificationProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.TopoChange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.TopoChangeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class BierDataTreeChangeListenerImpl<T extends DataObject>
        implements DataTreeChangeListener<T>, AutoCloseable {

    private static final Logger LOG =
            LoggerFactory.getLogger(BierDataTreeChangeListenerImpl.class);

    protected static final String TOPOLOGY_IID = "example-linkstate-topology";

    protected  static final BierDataAdapter BIER_TOPOLOGY_ADAPTER = new BierDataAdapter();

    protected DataBroker dataBroker;

    protected final ListenerRegistration<DataTreeChangeListener> listenerRegistration;

    public BierDataTreeChangeListenerImpl(final DataBroker dataBroker,
                                          final InstanceIdentifier<T> ii) {
        final DataTreeIdentifier<T> identifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, ii);
        this.dataBroker = dataBroker;
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

    public InstanceIdentifier<BierNode> provideIIToTopologyNode(final String nodeIdInTopology) {
        BierNodeKey bierNodeKey = new BierNodeKey(nodeIdInTopology);
        return InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(TOPOLOGY_IID))
                .child(BierNode.class, bierNodeKey);
    }
}
