/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.service.impl.activate.driver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.Collections;
import org.junit.Test;

import org.opendaylight.bier.adapter.api.BierConfigReader;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.service.impl.activate.driver.ActivateNetconfConnetion;

import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus;

import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;





public class ActivateNetconfConnetionTest {

    private static final NodeId NODE_ID = new NodeId("device_1");

    private Topology buildTopology() {
        Node node = new NodeBuilder()
                .setNodeId(NODE_ID)
                .addAugmentation(NetconfNode.class, new NetconfNodeBuilder()
                        .setConnectionStatus(NetconfNodeConnectionStatus.ConnectionStatus.Connected)
                        .build())
                .build();

        return new TopologyBuilder().setNode(Collections.singletonList(node)).build();
    }

    @Test
    public void testReadBierConfigFromAllNetconfNodes() throws Exception {
        DataBroker dataBroker = mock(DataBroker.class);
        BierConfigReader bierConfigReader = mock(BierConfigReader.class);
        ActivateNetconfConnetion activateNetconfConnetion = new ActivateNetconfConnetion(dataBroker,bierConfigReader);
        Optional<Topology> optionalData = mock(Optional.class);
        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<Topology>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class,new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName()))))))
                .thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(optionalData);
        when(optionalData.isPresent()).thenReturn(true);
        when(optionalData.get()).thenReturn(buildTopology());
        ActivateNetconfConnetion.readBierConfigFromAllNetconfNodes();
        verify(bierConfigReader).readBierGlobal(NODE_ID.getValue());
    }

}
