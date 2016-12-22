/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;

import java.util.Collection;
import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierNodeChangeListener implements DataTreeChangeListener<BierNode> {

    private static final Logger LOG = LoggerFactory.getLogger(BierNodeChangeListener.class);

    private static final InstanceIdentifier<BierNode> BIER_NODE_IID = InstanceIdentifier
            .create(BierNetworkTopology.class)
            .child(BierTopology.class, new BierTopologyKey("flow:1")).child(BierNode.class);

    private BierParametersConfigProcess bierParametersConfigProcess;

    public BierNodeChangeListener(BierConfigWriter bierConfig) {
        bierParametersConfigProcess = new BierParametersConfigProcess(bierConfig);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<BierNode>> changes) {
        for (DataTreeModification<BierNode> change : changes) {
            DataObjectModification<BierNode> rootNode = change.getRootNode();
            if (!bierInfoCheck(rootNode.getDataBefore(), rootNode.getDataAfter())) {
                LOG.info("Does not configuration bier info, do nothing");
            } else {
                switch (rootNode.getModificationType()) {
                    case WRITE:
                        LOG.info("onDataTreeChanged - BierNode config with path {} was added or replaced: "
                                        + "old BierNode: {}, new BierNode: {}",
                                change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                                rootNode.getDataAfter());
                        processAddedNode(rootNode.getDataAfter());
                        break;
                    case SUBTREE_MODIFIED:
                        LOG.info("onDataTreeChanged - BierNode config with path {} was modified: "
                                        + "old BierNode: {}, new BierNode: {}",
                                change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                                rootNode.getDataAfter());
                        processModifiedNode(rootNode.getDataBefore(), rootNode.getDataAfter());
                        break;
                    case DELETE:
                        LOG.info("onDataTreeChanged - BierNode config with path {} was deleted: old BierNode: {}",
                                change.getRootPath().getRootIdentifier(), rootNode.getDataBefore());
                        processDeletedNode(rootNode.getDataBefore());
                        break;
                    default:
                        throw new IllegalArgumentException("Unhandled modification type : {}"
                                + change.getRootNode().getModificationType());
                }
            }
        }
    }

    public InstanceIdentifier<BierNode> getBierNodeId() {
        return BIER_NODE_IID;
    }

    public void processAddedNode(BierNode afterNode) {
        if (null == afterNode) {
            return;
        }
        String nodeId = afterNode.getNodeId();
        bierParametersConfigProcess.processAddedDomain(nodeId, afterNode.getBierNodeParams().getDomain());
    }

    public void processDeletedNode(BierNode beforeNode) {
        if (null == beforeNode) {
            return;
        }
        String nodeId = beforeNode.getNodeId();
        bierParametersConfigProcess.processDeletedDomain(nodeId, beforeNode.getBierNodeParams().getDomain());
    }

    public void processModifiedNode(BierNode beforeNode, BierNode afterNode) {
        if (null == beforeNode || null == afterNode) {
            return;
        }
        String nodeId = beforeNode.getNodeId();
        bierParametersConfigProcess.processDomain(nodeId,beforeNode.getBierNodeParams().getDomain(),
             afterNode.getBierNodeParams().getDomain());
    }

    private boolean bierInfoCheck(BierNode before, BierNode after) {
        if (null == before || null == after) {
            return false;
        }
        if ((before.getBierNodeParams().getDomain() == null || before.getBierNodeParams().getDomain().isEmpty())
              && (after.getBierNodeParams().getDomain() == null || after.getBierNodeParams().getDomain().isEmpty())) {
            LOG.info("No Bier parameters!");
            return false;
        } else {
            return true;
        }
    }

}
