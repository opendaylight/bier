/*
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
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTpChangeListenerImpl
        extends BierDataTreeChangeListenerImpl<TerminationPoint> {
    private static final Logger LOG =  LoggerFactory.getLogger(BierTpChangeListenerImpl.class);    // 日志记录

    private final ExecutorService executor = Executors.newFixedThreadPool(7);


    public BierTpChangeListenerImpl(final DataBroker dataBroker) {
        super(dataBroker, InstanceIdentifier
                .create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(TOPOLOGY_IID)))
                .child(Node.class)
                .child(TerminationPoint.class));
    }

    @Override
    public void onDataTreeChanged(final @Nonnull Collection<DataTreeModification<TerminationPoint>> modifications) {
        for (DataTreeModification<TerminationPoint> modification : modifications) {
            switch (modification.getRootNode().getModificationType()) {
                case WRITE:
                    processAddedTerminationPoints(modification);
                    break;
                case SUBTREE_MODIFIED:
                    processUpdatedTerminationPoints(modification);
                    break;
                case DELETE:
                    processRemovedTerminationPoints(modification);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type: {}"
                            + modification.getRootNode().getModificationType());
            }
        }
    }

    private void processRemovedTerminationPoints(final DataTreeModification<TerminationPoint> modification) {
        final TerminationPoint tp = modification.getRootNode().getDataBefore();
        final String tpId = BIER_TOPOLOGY_ADAPTER.toBierId(tp.getTpId().getValue());
        final BierTopologyProcess<BierTerminationPoint> processor =
                new BierTopologyProcess<BierTerminationPoint>(dataBroker,
                        BierTopologyProcess.FLAG_WRITE, (new BierTerminationPointBuilder()).build());
        InstanceIdentifier<BierTerminationPoint> iiToTopologyRemovedtp = provideIIToTopologyTp(tpId);
        if (iiToTopologyRemovedtp != null) {

            processor.enqueueOperation(new BierTopologyOperation() {
                @Override
                public void writeOperation(final ReadWriteTransaction transaction) {
                    //BierTopologyManagerUtil.removeAffectedLinks(terminationPointId, transaction, II_TO_TOPOLOGY);
                    transaction.delete(LogicalDatastoreType.CONFIGURATION, iiToTopologyRemovedtp);
                }

                @Override
                public <T> ListenableFuture<T> readOperation(final ReadWriteTransaction transaction) {
                    return null;
                }
            });
            executor.submit(processor);
            notifyTopoChange(BierTopologyManager.TOPOLOGY_ID);
        } else {
            LOG.debug("Instance identifier to "
                    + "inventory wasn't translated to topology while deleting termination point.");
        }
    }

    private void processUpdatedTerminationPoints(final DataTreeModification<TerminationPoint> modification) {

    }

    private void processAddedTerminationPoints(final DataTreeModification<TerminationPoint> modification) {
        final TerminationPoint tp = modification.getRootNode().getDataAfter();
        BierTerminationPoint bierTp = BIER_TOPOLOGY_ADAPTER.toBierTp(tp);
        final String tpIdInTopology = bierTp.getTpId();
        if (tpIdInTopology != null) {
            final InstanceIdentifier<BierTerminationPoint> iiToTopologytp = provideIIToTopologyTp(tpIdInTopology);
            sendToTransactionChain(bierTp, iiToTopologytp);
            //removeLinks(modification.getRootNode().getDataAfter(), point);
        } else {
            LOG.debug("Inventory node key is null. Data can't be written to topology");
        }
    }

    private void removeLinks(final TerminationPoint tp, final BierTerminationPoint point) {
        final BierTopologyProcess<BierTerminationPoint> processor =
                new BierTopologyProcess<BierTerminationPoint>(dataBroker,
                        BierTopologyProcess.FLAG_WRITE, (new BierTerminationPointBuilder()).build());
        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(final ReadWriteTransaction transaction) {

            }

            @Override
            public <T> ListenableFuture<T> readOperation(final ReadWriteTransaction transaction) {
                return null;
            }
        });
    }

    public void sendToTransactionChain(final BierTerminationPoint bierTp,
                                       final InstanceIdentifier<BierTerminationPoint> iiToTopologyBier) {
        BierTopologyProcess<BierTerminationPoint> processor =  new BierTopologyProcess<>(dataBroker,
                BierTopologyProcess.FLAG_WRITE, (new BierTerminationPointBuilder()).build());
        processor.enqueueOperation(new BierTopologyOperation() {
            @Override
            public void writeOperation(final ReadWriteTransaction transaction) {
                transaction.merge(LogicalDatastoreType.CONFIGURATION, iiToTopologyBier, bierTp, true);
            }

            @Override
            public <T> ListenableFuture<T> readOperation(final ReadWriteTransaction transaction) {
                return null;
            }
        });
        executor.submit(processor);
    }

    public InstanceIdentifier<BierTerminationPoint> provideIIToTopologyTp(final String tpIdInTopology) {
        BierTerminationPointKey bierTerminationPointKey = new BierTerminationPointKey(tpIdInTopology);
        return InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey(TOPOLOGY_IID))
                .child(BierNode.class)
                .child(BierTerminationPoint.class, bierTerminationPointKey);
    }
}