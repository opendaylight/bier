/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.bierconfig;

import java.util.Collection;
import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParams;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierNodeChangeListener implements DataTreeChangeListener<BierNodeParams> {

    private static final Logger LOG = LoggerFactory.getLogger(BierNodeChangeListener.class);

    private static final InstanceIdentifier<BierNodeParams> BIER_NODE_IID = InstanceIdentifier
            .create(BierNetworkTopology.class)
            .child(BierTopology.class, new BierTopologyKey("example-linkstate-topology"))
            .child(BierNode.class)
            .child(BierNodeParams.class);

    private BierParametersConfigProcess bierParametersConfigProcess;

    public BierNodeChangeListener(BierConfigWriter bierConfig) {
        bierParametersConfigProcess = new BierParametersConfigProcess(bierConfig);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<BierNodeParams>> changes) {
        for (DataTreeModification<BierNodeParams> change : changes) {
            DataObjectModification<BierNodeParams> rootNode = change.getRootNode();
            if (!bierInfoCheck(rootNode.getDataBefore(), rootNode.getDataAfter())) {
                LOG.info("Does not configuration bier info, do nothing");
            } else {
                switch (rootNode.getModificationType()) {
                    case WRITE:
                        LOG.info("onDataTreeChanged - BierNodeParams config with path {} was added or replaced: "
                                        + "old BierNodeParams: {}, new BierNodeParams: {}",
                                change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                                rootNode.getDataAfter());
                        if (null == rootNode.getDataBefore() && null != rootNode.getDataAfter()) {
                            processAddedNode(change.getRootPath().getRootIdentifier()
                                    .firstIdentifierOf(BierNodeParams.class),rootNode.getDataAfter());
                        } else {
                            processModifiedNode(change.getRootPath().getRootIdentifier()
                                    .firstIdentifierOf(BierNodeParams.class),rootNode.getDataBefore(),
                                    rootNode.getDataAfter());
                        }
                        break;
                    case SUBTREE_MODIFIED:
                        LOG.info("onDataTreeChanged - BierNodeParams config with path {} was modified: "
                                        + "old BierNodeParams: {}, new BierNodeParams: {}",
                                change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                                rootNode.getDataAfter());
                        processModifiedNode(change.getRootPath().getRootIdentifier(),rootNode.getDataBefore(),
                                rootNode.getDataAfter());
                        break;
                    case DELETE:
                        LOG.info("onDataTreeChanged - BierNodeParams config with path {} was deleted: "
                                        + "old BierNodeParams: {}",
                                change.getRootPath().getRootIdentifier(), rootNode.getDataBefore());
                        processDeletedNode(change.getRootPath().getRootIdentifier()
                                .firstIdentifierOf(BierNodeParams.class),rootNode.getDataBefore());
                        break;
                    default:
                        throw new IllegalArgumentException("Unhandled modification type : {}"
                                + change.getRootNode().getModificationType());
                }
            }
        }
    }

    public InstanceIdentifier<BierNodeParams> getBierNodeId() {
        return BIER_NODE_IID;
    }

    public void processAddedNode(InstanceIdentifier<BierNodeParams> identifier, BierNodeParams afterNode) {
        String nodeId = identifier.firstKeyOf(BierNode.class).getNodeId();
        bierParametersConfigProcess.processAddedDomain(nodeId, afterNode.getDomain());
    }

    public void processAddedNode(String nodeId, BierNodeParams afterNode) {
        bierParametersConfigProcess.processAddedDomain(nodeId, afterNode.getDomain());
    }

    public void processDeletedNode(InstanceIdentifier<BierNodeParams> identifier, BierNodeParams beforeNode) {
        String nodeId = identifier.firstKeyOf(BierNode.class).getNodeId();
        bierParametersConfigProcess.processDeletedDomain(nodeId, beforeNode.getDomain());
    }

    public void processModifiedNode(InstanceIdentifier<BierNodeParams> identifier, BierNodeParams beforeNode,
                                    BierNodeParams afterNode) {
        String nodeId = identifier.firstKeyOf(BierNode.class).getNodeId();
        bierParametersConfigProcess.processDomain(nodeId, beforeNode.getDomain(), afterNode.getDomain());
    }

    private boolean bierInfoCheck(BierNodeParams before, BierNodeParams after) {

        if ((before == null || before.getDomain() == null || before.getDomain().isEmpty())
                && (after == null || after.getDomain() == null || after.getDomain().isEmpty())) {
            LOG.info("No Bier parameters!");
            return false;
        }
        return true;
    }

}
