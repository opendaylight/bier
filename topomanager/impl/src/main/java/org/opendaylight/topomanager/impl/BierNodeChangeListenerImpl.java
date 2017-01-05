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
    private static final Logger LOG =  LoggerFactory.getLogger(BierNodeChangeListenerImpl.class);    // 日志记录

    public BierNodeChangeListenerImpl(final DataBroker dataBroker, final BierTopologyProcess bierTopologyProcess) {
        super(bierTopologyProcess, dataBroker, InstanceIdentifier
                .create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow：1"))).child(Node.class));
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
        final InstanceIdentifier<Node> iiToNodeInInventory = modification.getRootPath().getRootIdentifier();
        final String nodeId = provideTopologyNodeId(iiToNodeInInventory);
        InstanceIdentifier<BierNode> iiToTopologyRemovedNode = provideIIToTopologyNode(nodeId);
        if (iiToTopologyRemovedNode != null) {
            bierTopologyProcess.enqueueOperation(new BierTopologyOperation() {
                @Override
                public void writeOperation(final ReadWriteTransaction transaction) {
                    transaction.delete(LogicalDatastoreType.OPERATIONAL, iiToTopologyRemovedNode);
                }

                @Override
                public <T> ListenableFuture<T> readOperation(final ReadWriteTransaction transaction) {
                    return null;
                }
            });
            notifyTopoChange(BierTopologyManager.TOPOLOGY_ID);
        } else {
            LOG.debug("Instance identifier to inventory wasn't translated to topology while deleting node.");
        }
    }

    public void processAddedNode(final DataTreeModification<Node> modification) {
        final InstanceIdentifier<Node> iiToNodeInNetwork = modification.getRootPath().getRootIdentifier();
        final String nodeIdInTopology = provideTopologyNodeId(iiToNodeInNetwork);
        if (nodeIdInTopology != null) {
            final InstanceIdentifier<BierNode> iiToTopologyNode = provideIIToTopologyNode(nodeIdInTopology);
            sendToTransactionChain(prepareTopologyNode(nodeIdInTopology, iiToNodeInNetwork), iiToTopologyNode);
            notifyTopoChange(BierTopologyManager.TOPOLOGY_ID);
        } else {
            LOG.debug("Inventory node key is null. Data can't be written to topology");
        }
    }

    public static BierNode prepareTopologyNode(final String nodeIdInTopology,
                                               final InstanceIdentifier<Node> iiToNodeInInventory) {
        BierNodeBuilder bierNodeBuilder = new BierNodeBuilder();
        bierNodeBuilder.setNodeId(nodeIdInTopology);
        return bierNodeBuilder.build();
    }
}
