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
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NetconfStateChangeListener implements DataTreeChangeListener<Node> {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfStateChangeListener.class);

    private static final InstanceIdentifier<Node> NODE_IID = InstanceIdentifier
            .create(NetworkTopology.class).child(Topology.class, new TopologyKey(new TopologyId(TopologyNetconf
                    .QNAME.getLocalName()))).child(Node.class);

    private NodeOnlineBierConfigProcess nodeOnlineBierConfigProcess;

    public NetconfStateChangeListener(final DataBroker dataBroker, BierConfigWriter bierConfigWriter,
                                      ChannelConfigWriter channelConfigWriter) {
        nodeOnlineBierConfigProcess = new NodeOnlineBierConfigProcess(dataBroker,bierConfigWriter,channelConfigWriter);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Node>> changes) {
        for (DataTreeModification<Node> change: changes) {
            DataObjectModification<Node> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    LOG.info("Node {} was created",rootNode.getDataAfter().getNodeId().getValue());
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("process modify procedure");
                    NetconfNode ncNodeNew = rootNode.getDataAfter().getAugmentation(NetconfNode.class);
                    NetconfNode ncNodeOld = rootNode.getDataBefore().getAugmentation(NetconfNode.class);
                    if ((ncNodeNew.getConnectionStatus() == NetconfNodeConnectionStatus.ConnectionStatus.Connected)
                            && (ncNodeOld.getConnectionStatus() != NetconfNodeConnectionStatus
                            .ConnectionStatus.Connected)) {
                        LOG.info("Node {} was connected",rootNode.getDataAfter().getNodeId().getValue());
                        if (null != rootNode.getDataAfter().getNodeId().getValue()) {
                            nodeOnlineBierConfigProcess.queryBierConfigAndSendForNodeOnline(rootNode.getDataAfter()
                                    .getNodeId().getValue());
                        } else {
                            LOG.info("NodeId is null");
                        }
                    }
                    break;
                case DELETE:
                    LOG.info("Node {} was deleted",rootNode.getDataBefore().getNodeId().getValue());
                    break;
                default:
                    break;
            }
        }
    }

    public InstanceIdentifier<Node> getNodeId() {
        return NODE_IID;
    }
}
