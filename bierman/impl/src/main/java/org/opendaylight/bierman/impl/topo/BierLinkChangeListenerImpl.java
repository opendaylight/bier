/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl.topo;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

import org.opendaylight.bierman.impl.BierDataManager;
import org.opendaylight.bierman.impl.BierDataOperation;
import org.opendaylight.bierman.impl.BierDataProcess;
import org.opendaylight.bierman.impl.NotificationProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkAdd;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkAddBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkChange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkChangeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkRemove;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkRemoveBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.link.add.AddLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.link.change.NewLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.link.change.OldLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.link.remove.RemoveLinkBuilder;
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
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.nt.l3.unicast.igp.topology.rev131021.Link1;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierLinkChangeListenerImpl
        extends BierDataTreeChangeListenerImpl<Link> {
    private static final Logger LOG =  LoggerFactory.getLogger(BierLinkChangeListenerImpl.class);

    private final ExecutorService executor = Executors.newFixedThreadPool(7);
    private BierDataManager bierDataManager;

    public BierLinkChangeListenerImpl(final DataBroker dataBroker) {
        super(dataBroker, InstanceIdentifier
                .create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(TOPOLOGY_IID))).child(Link.class));
        bierDataManager = new BierDataManager(dataBroker);
    }

    @Override
    public void onDataTreeChanged(final @Nonnull Collection<DataTreeModification<Link>> changes) {
        for (DataTreeModification modification : changes) {
            switch (modification.getRootNode().getModificationType()) {
                case WRITE:
                    processAddedLink(modification);
                    break;
                case SUBTREE_MODIFIED:
                    processUpdatedLink(modification);
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
        BierLink bierLink = BIER_TOPOLOGY_ADAPTER.toBierLink(link);
        bierLink = bierDataManager.transferBierLink(bierLink);
        final String linkId = bierLink.getLinkId();
        BierDataProcess<BierLink> processor =  new BierDataProcess<BierLink>(dataBroker,
                BierDataProcess.FLAG_WRITE, (new BierLinkBuilder()).build());
        final InstanceIdentifier<BierLink> iiToTopologyRemovedLink = provideIIToTopologyLink(linkId);
        if (iiToTopologyRemovedLink != null) {
            processor.enqueueOperation(new BierDataOperation() {
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
            notifyLinkRemove(bierLink);
            notifyTopoChange(BierDataManager.TOPOLOGY_ID);

        } else {
            LOG.debug("Instance identifier to inventory wasn't translated to topology while deleting link.");
        }
    }

    public void processUpdatedLink(final DataTreeModification<Link> modification) {
        final Link linkBefore = modification.getRootNode().getDataBefore();
        final Link linkAfter = modification.getRootNode().getDataAfter();

        Long metric1 = linkBefore.augmentation(Link1.class).getIgpLinkAttributes().getMetric();
        Long metric2 = linkAfter.augmentation(Link1.class).getIgpLinkAttributes().getMetric();
        if (metric1 != metric2) {
            BierLink bierLink = BIER_TOPOLOGY_ADAPTER.toBierLink(linkAfter);
            bierLink = bierDataManager.transferBierLink(bierLink);
            final String linkIdInTopology = bierLink.getLinkId();
            if (linkIdInTopology != null) {
                final InstanceIdentifier<BierLink> iiToTopologyLink = provideIIToTopologyLink(linkIdInTopology);
                sendToTransactionChain(bierLink, iiToTopologyLink);
                notifyTopoChange(BierDataManager.TOPOLOGY_ID);
                BierLink oldLink = BIER_TOPOLOGY_ADAPTER.toBierLink(linkBefore);
                oldLink = bierDataManager.transferBierLink(oldLink);
                BierLink newLink = BIER_TOPOLOGY_ADAPTER.toBierLink(linkAfter);
                newLink = bierDataManager.transferBierLink(newLink);
                notifyLinkChange(oldLink,newLink);
            } else {
                LOG.debug("Inventory link key is null. Data can't be written to topology");
            }
        }
    }

    public void processAddedLink(final DataTreeModification<Link> modification) {
        final Link link = modification.getRootNode().getDataAfter();
        BierLink bierLink = BIER_TOPOLOGY_ADAPTER.toBierLink(link);
        bierLink = bierDataManager.transferBierLink(bierLink);
        final String linkIdInTopology = bierLink.getLinkId();
        if (linkIdInTopology != null) {
            final InstanceIdentifier<BierLink> iiToTopologyLink = provideIIToTopologyLink(linkIdInTopology);
            sendToTransactionChain(bierLink, iiToTopologyLink);
            notifyTopoChange(BierDataManager.TOPOLOGY_ID);
            notifyLinkAdd(bierLink);
        } else {
            LOG.debug("Inventory link key is null. Data can't be written to topology");
        }
    }

    public void sendToTransactionChain(final BierLink bierLink,
                                       final InstanceIdentifier<BierLink> iiToTopologyBier) {
        BierDataProcess<BierLink> processor =  new BierDataProcess<BierLink>(dataBroker,
                BierDataProcess.FLAG_WRITE, (new BierLinkBuilder()).build());
        processor.enqueueOperation(new BierDataOperation() {
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

    protected void notifyLinkAdd(final BierLink link) {
        AddLinkBuilder addLinkBuilder = new AddLinkBuilder(link);
        LinkAdd notification = new LinkAddBuilder().setTopoId(TOPOLOGY_IID)
                .setAddLink(addLinkBuilder.build()).build();
        LOG.info("notify LinkAdd:" + link);
        NotificationProvider.getInstance().notify(notification);
    }

    protected void notifyLinkRemove(final BierLink link) {
        RemoveLinkBuilder removeLinkBuilder = new RemoveLinkBuilder(link);
        LinkRemove notification = new LinkRemoveBuilder().setTopoId(TOPOLOGY_IID)
                .setRemoveLink(removeLinkBuilder.build()).build();
        LOG.info("notify LinkRemove:" + link);
        NotificationProvider.getInstance().notify(notification);
    }

    protected void notifyLinkChange(final BierLink oldLink,final BierLink newLink) {
        OldLinkBuilder oldLinkBuilder = new OldLinkBuilder(oldLink);
        NewLinkBuilder newLinkBuilder = new NewLinkBuilder(newLink);
        LinkChange notification = new LinkChangeBuilder().setTopoId(TOPOLOGY_IID)
                .setOldLink(oldLinkBuilder.build())
                .setNewLink(newLinkBuilder.build()).build();
        LOG.info("notify LinkChange: old link--" + oldLink + "; new link--" + newLink);
        NotificationProvider.getInstance().notify(notification);
    }

}
