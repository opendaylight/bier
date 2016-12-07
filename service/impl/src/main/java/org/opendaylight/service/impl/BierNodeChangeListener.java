/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;

import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.routing.BierBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.routing.bier.BierGlobalBuilder;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by 10200860 on 2016/11/29.
 */
public class BierNodeChangeListener implements DataTreeChangeListener<BierNode> {

    private static final Logger LOG = LoggerFactory.getLogger(BierNodeChangeListener.class);

    private static final InstanceIdentifier<BierNode> BIER_NODE_IID = InstanceIdentifier.create(BierNetworkTopology.class)
            .child(BierTopology.class, new BierTopologyKey("flow:1")).child(BierNode.class);

    private final DataBroker dataProvider;

    public BierNodeChangeListener(final DataBroker dataBroker) {
        dataProvider = dataBroker;
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<BierNode>> changes) {

        for(DataTreeModification<BierNode> change: changes) {
            DataObjectModification<BierNode> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()){
                case WRITE:
                    LOG.info("onDataTreeChanged - BierNode config with path {} was added or replaced: old BierNode: {}, new BierNode: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(), rootNode.getDataAfter());
                    processAddedNode(rootNode.getDataAfter());
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("onDataTreeChanged - BierNode config with path {} was modified: old BierNode: {}, new BierNode: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(), rootNode.getDataAfter());
                    processModifiedNode(rootNode.getDataBefore(),rootNode.getDataAfter());
                    break;
                case DELETE:
                    LOG.info("onDataTreeChanged - BierNode config with path {} was deleted: old BierNode: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore());
                    processDeletedNode(rootNode.getDataBefore());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type : {}" + change.getRootNode().getModificationType());
            }
        }
    }

    public InstanceIdentifier<BierNode> getBierNodeIid() {
        return BIER_NODE_IID;
    }

    public void processAddedNode(BierNode after) {
        BierBuilder bierBuilder = new BierBuilder();
        BierGlobalBuilder bierGlobalBuilder = new BierGlobalBuilder();

    }

    public void processModifiedNode(BierNode before, BierNode after) {

    }

    public void processDeletedNode(BierNode before) {

    }


}
