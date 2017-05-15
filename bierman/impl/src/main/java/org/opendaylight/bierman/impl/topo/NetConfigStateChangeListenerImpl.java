/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl.topo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import org.opendaylight.bier.adapter.api.DeviceInterfaceReader;
import org.opendaylight.bierman.impl.BierDataManager;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointBuilder;
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



public class NetConfigStateChangeListenerImpl extends BierDataTreeChangeListenerImpl<Node> {
    private static final Logger LOG =  LoggerFactory.getLogger(NetConfigStateChangeListenerImpl.class);
    private BierDataManager topoManager;
    private DeviceInterfaceReader deviceInterfaceReader;

    public NetConfigStateChangeListenerImpl(final DataBroker dataBroker,final BierDataManager topoManager,
                                            DeviceInterfaceReader deviceInterfaceReader) {
        super(dataBroker, InstanceIdentifier
                .create(NetworkTopology.class).child(Topology.class, new TopologyKey(new TopologyId(TopologyNetconf
                        .QNAME.getLocalName()))).child(Node.class));
        this.topoManager = topoManager;
        this.deviceInterfaceReader = deviceInterfaceReader;
    }

    @Override
    public void onDataTreeChanged(final @Nonnull Collection<DataTreeModification<Node>> changes) {
        for (DataTreeModification<Node> change: changes) {
            DataObjectModification<Node> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    String addNodeId = rootNode.getDataAfter().getNodeId().getValue();
                    LOG.info("Node {} was created",addNodeId);
                    NetconfNode ncNode = rootNode.getDataAfter().getAugmentation(NetconfNode.class);
                    if (ncNode.getConnectionStatus() == NetconfNodeConnectionStatus.ConnectionStatus.Connected) {
                        updateBierNodeTp(addNodeId);
                    }
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("process modify procedure");
                    NetconfNode ncNodeNew = rootNode.getDataAfter().getAugmentation(NetconfNode.class);
                    NetconfNode ncNodeOld = rootNode.getDataBefore().getAugmentation(NetconfNode.class);
                    if ((ncNodeNew.getConnectionStatus() == NetconfNodeConnectionStatus.ConnectionStatus.Connected)
                            && (ncNodeOld.getConnectionStatus() != NetconfNodeConnectionStatus
                            .ConnectionStatus.Connected)) {
                        String nodeId = rootNode.getDataAfter().getNodeId().getValue();
                        if (null != nodeId) {
                            LOG.info("Node {} was connected",nodeId);
                            updateBierNodeTp(nodeId);
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

    public void updateBierNodeTp(String nodeId) {
        List<BierTerminationPoint> devTpList = deviceInterfaceReader.readDeviceInterface(nodeId);
        if (devTpList == null || devTpList.isEmpty()) {
            return;
        }

        BierNode node = topoManager.getNodeData(TOPOLOGY_IID, nodeId);
        if (node == null) {
            return;
        }
        List<BierTerminationPoint> newBierTpList = new ArrayList<BierTerminationPoint>();
        List<BierTerminationPoint> bierTpList = node.getBierTerminationPoint();
        int bierTpSize = bierTpList.size();
        for (int iloop = 0; iloop < bierTpSize; ++iloop) {
            BierTerminationPoint bierTp = bierTpList.get(iloop);
            int devTpSize = devTpList.size();
            for (int jloop = 0; jloop < devTpSize; ++jloop) {
                BierTerminationPoint devTp = devTpList.get(jloop);
                if (devTp.getTpIpPrefix().equals(bierTp.getTpId())) {
                    BierTerminationPointBuilder tpBuilder = new BierTerminationPointBuilder(devTp);
                    tpBuilder.setTpId(bierTp.getTpId());
                    newBierTpList.add(tpBuilder.build());
                }
            }
        }
        BierNodeBuilder newBierNodeBuilder = new BierNodeBuilder(node);
        newBierNodeBuilder.setBierTerminationPoint(newBierTpList);
        topoManager.setNodeData(TOPOLOGY_IID, newBierNodeBuilder.build());
    }
}