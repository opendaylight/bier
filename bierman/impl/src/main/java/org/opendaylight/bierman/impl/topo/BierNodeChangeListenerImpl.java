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
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierNodeChangeListenerImpl extends BierDataTreeChangeListenerImpl<Node> {
    private static final Logger LOG =  LoggerFactory.getLogger(BierNodeChangeListenerImpl.class);

    private final ExecutorService executor = Executors.newFixedThreadPool(7);

    public BierNodeChangeListenerImpl(final DataBroker dataBroker) {
        super(dataBroker, InstanceIdentifier
                .create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(TOPOLOGY_IID))).child(Node.class));
    }

    @Override
    public void onDataTreeChanged(final @Nonnull Collection<DataTreeModification<Node>> changes) {
        for (DataTreeModification modification : changes) {
            switch (modification.getRootNode().getModificationType()) {
                case WRITE:
                    processAddedNode(modification);
                    break;
                case SUBTREE_MODIFIED:
                    break;
                case DELETE:
                    processRemovedNode(modification);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type : {}"
                            + modification.getRootNode().getModificationType());
            }
        }
    }

    public void processRemovedNode(final DataTreeModification<Node> modification) {
        final Node node = modification.getRootNode().getDataBefore();
        final String nodeId = node.getNodeId().getValue();
        BierDataProcess<BierNode> processor =  new BierDataProcess<BierNode>(dataBroker,
                BierDataProcess.FLAG_WRITE, (new BierNodeBuilder()).build());
        InstanceIdentifier<BierNode> iiToTopologyRemovedNode =
                provideIIToTopologyNode(BIER_TOPOLOGY_ADAPTER.toBierNodeId(nodeId));
        if (iiToTopologyRemovedNode != null) {
            notifyTopoChange(BierDataManager.TOPOLOGY_ID);
        } else {
            LOG.debug("Instance identifier to inventory wasn't translated to topology while deleting node.");
        }

    }

    public void processAddedNode(final DataTreeModification<Node> modification) {
        final Node node = modification.getRootNode().getDataAfter();
        BierNode bierNode = BIER_TOPOLOGY_ADAPTER.toBierNode(node);
        final String nodeIdInTopology = node.getNodeId().getValue();
        if (nodeIdInTopology != null) {
            final InstanceIdentifier<BierNode> iiToTopologyNode =
                    provideIIToTopologyNode(BIER_TOPOLOGY_ADAPTER.toBierNodeId(nodeIdInTopology));
            sendToTransactionChain(bierNode, iiToTopologyNode);
            notifyTopoChange(BierDataManager.TOPOLOGY_ID);
        } else {
            LOG.debug("Inventory node key is null. Data can't be written to topology");
        }
    }

    public void sendToTransactionChain(final BierNode bierNode,
                                       final InstanceIdentifier<BierNode> iiToTopologyBier) {
        BierDataProcess<BierNode> processor =  new BierDataProcess<BierNode>(dataBroker,
                BierDataProcess.FLAG_WRITE, (new BierNodeBuilder()).build());
        processor.enqueueOperation(new BierDataOperation() {
            @Override
            public void writeOperation(final ReadWriteTransaction transaction) {
                transaction.merge(LogicalDatastoreType.CONFIGURATION, iiToTopologyBier, bierNode, true);
            }

            @Override
            public <T> ListenableFuture<T> readOperation(final ReadWriteTransaction transaction) {
                return null;
            }
        });
        executor.submit(processor);

    }

}
