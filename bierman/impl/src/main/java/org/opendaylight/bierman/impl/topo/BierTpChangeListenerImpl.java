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
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
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
        final BierTerminationPoint bierTp = BIER_TOPOLOGY_ADAPTER.toBierTp(tp);
        final String bierTpId = bierTp.getTpId();
        final InstanceIdentifier<TerminationPoint> iiTp = modification.getRootPath().getRootIdentifier();
        final InstanceIdentifier<BierTerminationPoint> iiToTopologyRemovedtp = provideIIToTopologyTp(bierTpId, iiTp);
        final BierDataProcess<BierTerminationPoint> processor =
                new BierDataProcess<BierTerminationPoint>(dataBroker,
                        BierDataProcess.FLAG_WRITE, (new BierTerminationPointBuilder()).build());
        if (iiToTopologyRemovedtp != null) {
            processor.enqueueOperation(new BierDataOperation() {
                @Override
                public void writeOperation(final ReadWriteTransaction transaction) {
                    transaction.delete(LogicalDatastoreType.CONFIGURATION, iiToTopologyRemovedtp);
                }

                @Override
                public <T> ListenableFuture<T> readOperation(final ReadWriteTransaction transaction) {
                    return null;
                }
            });
            executor.submit(processor);
            notifyTopoChange(BierDataManager.TOPOLOGY_ID);
        } else {
            LOG.debug("Instance identifier to "
                    + "inventory wasn't translated to topology while deleting termination point.");
        }
    }

    private void processUpdatedTerminationPoints(final DataTreeModification<TerminationPoint> modification) {

    }

    private void processAddedTerminationPoints(final DataTreeModification<TerminationPoint> modification) {
        final TerminationPoint tp = modification.getRootNode().getDataAfter();
        final BierTerminationPoint bierTp = BIER_TOPOLOGY_ADAPTER.toBierTp(tp);
        final String tpId = bierTp.getTpId();
        final InstanceIdentifier<TerminationPoint> iiTp = modification.getRootPath().getRootIdentifier();
        if (tpId != null) {
            InstanceIdentifier<BierTerminationPoint> iiToTopologyAddedtp = provideIIToTopologyTp(tpId, iiTp);
            sendToTransactionChain(bierTp, iiToTopologyAddedtp);
        } else {
            LOG.debug("Inventory node key is null. Data can't be written to topology");
        }
    }

    public void sendToTransactionChain(final BierTerminationPoint bierTp,
                                       final InstanceIdentifier<BierTerminationPoint> iiToTopologyBier) {
        BierDataProcess<BierTerminationPoint> processor =  new BierDataProcess<>(dataBroker,
                BierDataProcess.FLAG_WRITE, (new BierTerminationPointBuilder()).build());
        processor.enqueueOperation(new BierDataOperation() {
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

    public InstanceIdentifier<BierTerminationPoint> provideIIToTopologyTp(
            final String tpIdInBier,
            final InstanceIdentifier<TerminationPoint> iiTp) {
        final String nodeId = iiTp.firstKeyOf(Node.class).getNodeId().getValue();
        final String bierNodeId = BIER_TOPOLOGY_ADAPTER.toBierNodeId(nodeId);
        if (tpIdInBier != null && bierNodeId != null) {
            InstanceIdentifier<BierNode> iiToBierNode = provideIIToTopologyNode(bierNodeId);
            return iiToBierNode.builder().child(BierTerminationPoint.class,
                    new BierTerminationPointKey(tpIdInBier)).build();
        } else {
            LOG.debug("Value of termination point ID in topology is null. "
                    + "Instance identifier to topology can't be built");
            return null;
        }
    }
}