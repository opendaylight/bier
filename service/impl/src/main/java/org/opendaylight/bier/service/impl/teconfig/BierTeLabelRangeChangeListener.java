/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.teconfig;

import java.util.Collection;
import org.opendaylight.bier.adapter.api.BierTeLabelRangeConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.service.impl.NotificationProvider;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeLableRange;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BierTeLabelRangeChangeListener implements DataTreeChangeListener<BierTeLableRange> {

    private static final Logger LOG = LoggerFactory.getLogger(BierTeLabelRangeChangeListener.class);
    private BierTeLabelRangeConfigWriter bierTeLabelRangeConfigWriter;
    private NotificationProvider notificationProvider;

    public BierTeLabelRangeChangeListener(BierTeLabelRangeConfigWriter bierTeLabelRangeConfigWriter) {
        this.bierTeLabelRangeConfigWriter = bierTeLabelRangeConfigWriter;
        notificationProvider = new NotificationProvider();
    }

    private static final InstanceIdentifier<BierTeLableRange> BIER_TE_LABLE_RANGE_IID = InstanceIdentifier
            .create(BierNetworkTopology.class)
            .child(BierTopology.class, new BierTopologyKey("example-linkstate-topology"))
            .child(BierNode.class).child(BierTeLableRange.class);

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<BierTeLableRange>> changes) {
        for (DataTreeModification<BierTeLableRange> change : changes) {
            DataObjectModification<BierTeLableRange> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    LOG.info("onDataTreeChanged - BierTeLableRange config with path {} was added or replaced: "
                                    + "old BierTeLableRange: {}, new BierTeLableRange: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                            rootNode.getDataAfter());
                    if (null == rootNode.getDataBefore()) {
                        processLabelRangeInfo(ConfigurationType.ADD, getNodeId(change.getRootPath()
                                        .getRootIdentifier()), rootNode.getDataAfter());
                    } else {
                        processLabelRangeInfo(ConfigurationType.DELETE, getNodeId(change.getRootPath()
                                        .getRootIdentifier()), rootNode.getDataBefore());
                        processLabelRangeInfo(ConfigurationType.ADD, getNodeId(change.getRootPath()
                                        .getRootIdentifier()), rootNode.getDataAfter());
                    }
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("onDataTreeChanged - BierTeLableRange config with path {} was modified: "
                                    + "old BierTeLableRange: {}, new BierTeLableRange: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                            rootNode.getDataAfter());
                    processLabelRangeInfo(ConfigurationType.DELETE, getNodeId(change.getRootPath().getRootIdentifier()),
                            rootNode.getDataBefore());
                    processLabelRangeInfo(ConfigurationType.ADD, getNodeId(change.getRootPath().getRootIdentifier()),
                            rootNode.getDataAfter());
                    break;
                case DELETE:
                    LOG.info("onDataTreeChanged - BierTeLableRange config with path {} was deleted: "
                                    + "old BierTeLableRange: {}", change.getRootPath().getRootIdentifier(),
                            rootNode.getDataBefore());
                    processLabelRangeInfo(ConfigurationType.DELETE, getNodeId(change.getRootPath().getRootIdentifier()),
                            rootNode.getDataBefore());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type : {}"
                            + change.getRootNode().getModificationType());
            }
        }
    }

    public InstanceIdentifier<BierTeLableRange> getBierTeLableRangeIid() {
        return BIER_TE_LABLE_RANGE_IID;
    }

    private String getNodeId(InstanceIdentifier<BierTeLableRange> identifier) {
        return identifier.firstKeyOf(BierNode.class).getNodeId();
    }

    private void processLabelRangeInfo(ConfigurationType type, String nodeId, BierTeLableRange bierTeLableRange) {
        ConfigurationResult result = bierTeLabelRangeConfigWriter.writeBierTeLabelRange(type, nodeId, bierTeLableRange);
        if (!result.isSuccessful()) {
            LOG.info("Send info to southbound failed");
            notificationProvider.notifyFailureReason(result.getFailureReason());
        }
    }
}
