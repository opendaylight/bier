/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.activate.driver;

import com.google.common.base.Optional;
import java.util.Timer;

import org.opendaylight.bier.adapter.api.BierConfigReader;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;

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

public class ActivateNetconfConnetion {
    private static final Logger LOG = LoggerFactory.getLogger(ActivateNetconfConnetion.class);
    private static DataBroker dataBroker;
    private static BierConfigReader bierConfigReader;

    private static final InstanceIdentifier<Topology> NETCONF_TOPOLOGY_IID =
            InstanceIdentifier
                    .create(NetworkTopology.class)
                    .child(Topology.class,
                            new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));

    private Timer timer = new Timer();
    private static final long DELAY_INTERVAL = 180000;
    private static final long PERIOD_INTERVAL = 180000;

    public ActivateNetconfConnetion(DataBroker dataBroker,BierConfigReader bierConfigReader) {
        this.dataBroker = dataBroker;
        this.bierConfigReader = bierConfigReader;
        timer.schedule(new ReadBierGlobalTask(),DELAY_INTERVAL,PERIOD_INTERVAL);
    }

    public static void readBierConfigFromAllNetconfNodes() {

        Topology topology = null;
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        Optional<Topology> optionalData;
        try {
            optionalData = transaction.read(LogicalDatastoreType.OPERATIONAL, NETCONF_TOPOLOGY_IID).checkedGet();
            if (optionalData.isPresent()) {
                topology = optionalData.get();
            } else {
                LOG.debug("{}: Failed to read {}", Thread.currentThread().getStackTrace()[1], NETCONF_TOPOLOGY_IID);
            }
        } catch (ReadFailedException e) {
            LOG.warn("Failed to read {} ", NETCONF_TOPOLOGY_IID, e);
        }
        transaction.close();

        if (topology == null) {
            LOG.info("No netconf nodes in datastore!!");
            return;
        }

        for (final Node node : topology.getNode()) {
            NetconfNode netconfNode = node.getAugmentation(NetconfNode.class);
            if ((netconfNode.getConnectionStatus() == NetconfNodeConnectionStatus.ConnectionStatus.Connected)
                && (!node.getNodeId().getValue().equals("controller-config"))) {
                bierConfigReader.readBierGlobal(node.getNodeId().getValue());
                LOG.info("Read bier global of {}",node.getNodeId());

            }

        }


    }

    static class ReadBierGlobalTask extends java.util.TimerTask {

        public void run() {
            LOG.info("Read bier global task running");
            ActivateNetconfConnetion.readBierConfigFromAllNetconfNodes();
        }
    }



}
