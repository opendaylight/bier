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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkKey;
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
    private static final Logger LOG =  LoggerFactory.getLogger(BierLinkChangeListenerImpl.class);// 日志记录

    private final ExecutorService executor = Executors.newFixedThreadPool(7);

    public BierLinkChangeListenerImpl(final DataBroker dataBroker) {
        super(dataBroker, InstanceIdentifier
                .create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(TOPOLOGY_IID))).child(Link.class));
    }

    @Override
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
                    throw new IllegalArgumentException("Unhandled modification type : {}"
                            + modification.getRootNode().getModificationType());
            }
        }
    }

    public void processRemovedLink(final DataTreeModification<Link> modification) {
        final Link link = modification.getRootNode().getDataBefore();
        final String linkId = BIER_TOPOLOGY_ADAPTER.toBierId(link.getLinkId().getValue());
        BierTopologyProcess<BierLink> processor =  new BierTopologyProcess<BierLink>(dataBroker,
                BierTopologyProcess.FLAG_WRITE, (new BierLinkBuilder()).build());
        InstanceIdentifier<BierLink> iiToTopologyRemovedLink = provideIIToTopologyLink(linkId);
        if (iiToTopologyRemovedLink != null) {
            processor.enqueueOperation(new BierTopologyOperation() {
                @Override
                public void writeOperation(final ReadWriteTransaction transaction) {
                    transaction.delete(LogicalDatastoreType.CONFIGURATION, iiToTopologyRemovedLink);
                }

                @Override
                public <T> ListenableFuture<T> readOperation(final ReadWriteTransaction transaction) {
                    return null;
                }
            });
            executor.submit(processor);
            notifyTopoChange(BierTopologyManager.TOPOLOGY_ID);

        } else {
            LOG.debug("Instance identifier to inventory wasn't translated to topology while deleting link.");
        }
    }

    public void processAddedLink(final DataTreeModification<Link> modification) {
        final Link link = modification.getRootNode().getDataAfter();
        BierLink bierLink = BIER_TOPOLOGY_ADAPTER.toBierLink(link);
        final String linkIdInTopology = bierLink.getLinkId();
        if (linkIdInTopology != null) {
            final InstanceIdentifier<BierLink> iiToTopologyLink = provideIIToTopologyLink(linkIdInTopology);
            sendToTransactionChain(bierLink, iiToTopologyLink);
            notifyTopoChange(BierTopologyManager.TOPOLOGY_ID);
        } else {
            LOG.debug("Inventory link key is null. Data can't be written to topology");
        }
    }

    public void sendToTransactionChain(final BierLink bierLink,
                                       final InstanceIdentifier<BierLink> iiToTopologyBier) {
        BierTopologyProcess<BierLink> processor =  new BierTopologyProcess<BierLink>(dataBroker,
                BierTopologyProcess.FLAG_WRITE, (new BierLinkBuilder()).build());
        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(final ReadWriteTransaction transaction) {
                transaction.merge(LogicalDatastoreType.CONFIGURATION, iiToTopologyBier, bierLink, true);
            }

            @Override
            public <T> ListenableFuture<T> readOperation(final ReadWriteTransaction transaction) {
                return null;
            }
        });
        executor.submit(processor);

    }

    public InstanceIdentifier<BierLink> provideIIToTopologyLink(final String linkIdInTopology) {
        BierLinkKey bierLinkKey = new BierLinkKey(linkIdInTopology);
        return InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(TOPOLOGY_IID))
                .child(BierLink.class, bierLinkKey);
    }
}
