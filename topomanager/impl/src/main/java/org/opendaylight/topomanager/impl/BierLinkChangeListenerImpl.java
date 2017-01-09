/**
 * Copyright © 2016 www.bupt.edu.cn and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topomanager.impl;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierLinkChangeListenerImpl
        extends BierDataTreeChangeListenerImpl<Link> {
    private static final Logger LOG =  LoggerFactory.getLogger(BierLinkChangeListenerImpl.class);

    public BierLinkChangeListenerImpl(final DataBroker dataBroker, final BierTopologyProcess bierTopologyProcess) {
        super(bierTopologyProcess, dataBroker, InstanceIdentifier
                .create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow：1"))).child(Link.class));
    }

    public void onDataTreeChanged(final @Nonnull Collection<DataTreeModification<Link>> changes) {
        for (DataTreeModification modification : changes) {
            switch (modification.getRootNode().getModificationType()) {
                case WRITE:
                    processAddedLink(modification);
                    break;
                case SUBTREE_MODIFIED:
                    break;
                case DELETE:
                    processRemovedLink(modification);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type : {}" + modification.getRootNode()
                            .getModificationType());
            }
        }
    }

    public void processRemovedLink(final DataTreeModification modification) {
        final InstanceIdentifier<Link> iiToLinkInInventory = modification.getRootPath().getRootIdentifier();
        final String linkId = provideTopologyNodeId(iiToLinkInInventory);
        InstanceIdentifier<BierLink> iiToTopologyRemovedLink = provideIIToTopologyLink(linkId);
        if (iiToTopologyRemovedLink != null) {
            bierTopologyProcess.enqueueOperation(new BierTopologyOperation() {
                @Override
                public void writeOperation(final ReadWriteTransaction transaction) {
                    transaction.delete(LogicalDatastoreType.OPERATIONAL, iiToTopologyRemovedLink);
                }

                @Override
                public <T> ListenableFuture<T> readOperation(final ReadWriteTransaction transaction) {
                    return null;
                }
            });
            notifyTopoChange(BierTopologyManager.TOPOLOGY_ID);
        }
    }

    public void processAddedLink(final DataTreeModification modification) {
        final InstanceIdentifier<Link> iiToLinkInNetwork = modification.getRootPath().getRootIdentifier();
        final String linkIdInTopology = provideTopologyLinkId(iiToLinkInNetwork);
        if (linkIdInTopology != null) {
            final InstanceIdentifier<BierLink> iiToTopologyLink = provideIIToTopologyLink(linkIdInTopology);
            sendToTransactionChain(prepareTopologyNode(linkIdInTopology, iiToLinkInNetwork), iiToTopologyLink);
            notifyTopoChange(BierTopologyManager.TOPOLOGY_ID);
        } else {
            LOG.debug("Inventory node key is null. Data can't be written to topology");
        }
    }

    public static BierLink prepareTopologyNode(final String nodeIdInTopology,
                                               final InstanceIdentifier<Link> iiToNodeInInventory) {
        BierLinkBuilder bierNodeBuilder = new BierLinkBuilder();
        bierNodeBuilder.setLinkId(nodeIdInTopology);
        return bierNodeBuilder.build();
    }
}
