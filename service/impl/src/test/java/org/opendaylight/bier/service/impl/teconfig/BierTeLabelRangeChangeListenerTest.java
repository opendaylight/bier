/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.teconfig;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.bier.adapter.api.BierTeLabelRangeConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierTeLabelRangeSize;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeLableRange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeLableRangeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class BierTeLabelRangeChangeListenerTest extends AbstractConcurrentDataBrokerTest {

    private BierTeLabelRangeConfigWriterMock bierTeLabelRangeConfigWriterMock;
    private BierTeLabelRangeChangeListener bierTeLabelRangeChangeListener;

    public void setUp() {
        bierTeLabelRangeConfigWriterMock = new BierTeLabelRangeConfigWriterMock();
        bierTeLabelRangeChangeListener = new BierTeLabelRangeChangeListener(bierTeLabelRangeConfigWriterMock);
        getDataBroker().registerDataTreeChangeListener(new DataTreeIdentifier<BierTeLableRange>(
                LogicalDatastoreType.CONFIGURATION, bierTeLabelRangeChangeListener.getBierTeLableRangeIid()),
                bierTeLabelRangeChangeListener);
    }

    @Test
    public void bierTeLabelRangeChangeListenerTest() {
        setUp();
        //Test add bierTeLabelRange
        BierNode bierNodeBefore = constructBierNode("001", "Node1", "0001", 35, 38,
                null, null, null, null);
        BierNode bierNodeAfter1 = constructBierNode("001", "Node1", "0001", 35, 38,
                null, null, null, constructBierTeLableRange(new MplsLabel(new Long(5L)),
                        new BierTeLabelRangeSize(new Long(3L))));
        addBierNodeToDataStore(bierNodeBefore);
        addBierNodeToDataStore(bierNodeAfter1);
        assertAddedBierTeLabelRangeTest(bierTeLabelRangeConfigWriterMock.getBierTeLableRangeList());

        //Test modify bierTeLabelRange
        BierNode bierNodeAfter2 = constructBierNode("001", "Node1", "0001", 35, 38,
                null, null, null, constructBierTeLableRange(new MplsLabel(new Long(5L)),
                        new BierTeLabelRangeSize(new Long(2L))));
        addBierNodeToDataStore(bierNodeAfter2);
        assertModifiedBierTeLabelRangeTest(bierTeLabelRangeConfigWriterMock.getBierTeLableRangeList());

        //Test delete bierTeLabelRange
        BierNode bierNodeAfter3 = constructBierNode("001", "Node1", "0001", 35, 38,
                null, null, null, null);
        addBierNodeToDataStore(bierNodeAfter3);
        assertDeletedBierTeLabelRangeTest(bierTeLabelRangeConfigWriterMock.getBierTeLableRangeList());
    }

    private BierTeLableRange constructBierTeLableRange(MplsLabel labelBase, BierTeLabelRangeSize bierTeLabelRangeSize) {
        BierTeLableRangeBuilder builder = new BierTeLableRangeBuilder();
        builder.setLabelBase(labelBase);
        builder.setLabelRangeSize(bierTeLabelRangeSize);
        return builder.build();
    }

    private BierNode constructBierNode(String nodeId, String name, String routerId, int latitude,
                                       int longitude, List<BierTerminationPoint> bierTerminationPointList,
                                       BierNodeParams bierNodeParams, BierTeNodeParams bierTeNodeParams,
                                       BierTeLableRange bierTeLableRange) {
        BierNodeBuilder bierNodeBuilder = new BierNodeBuilder();
        bierNodeBuilder.setNodeId(nodeId);
        bierNodeBuilder.setName(name);
        bierNodeBuilder.setRouterId(routerId);
        bierNodeBuilder.setLatitude(BigInteger.valueOf(latitude));
        bierNodeBuilder.setLongitude(BigInteger.valueOf(longitude));
        bierNodeBuilder.setBierTerminationPoint(bierTerminationPointList);
        bierNodeBuilder.setBierNodeParams(bierNodeParams);
        bierNodeBuilder.setBierTeNodeParams(bierTeNodeParams);
        bierNodeBuilder.setBierTeLableRange(bierTeLableRange);
        return bierNodeBuilder.build();
    }

    private void addBierNodeToDataStore(BierNode bierNode) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<BierNode> bierNodePath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey("example-linkstate-topology"))
                .child(BierNode.class, new BierNodeKey(bierNode.getNodeId()));
        tx.put(LogicalDatastoreType.CONFIGURATION, bierNodePath, bierNode, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    private void assertAddedBierTeLabelRangeTest(List<BierTeLableRange> list) {
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0).getLabelBase(), new MplsLabel(new Long(5L)));
        Assert.assertEquals(list.get(0).getLabelRangeSize(), new BierTeLabelRangeSize(new Long(3L)));
    }

    private void assertModifiedBierTeLabelRangeTest(List<BierTeLableRange> list) {
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.get(1).getLabelBase(), new MplsLabel(new Long(5L)));
        Assert.assertEquals(list.get(1).getLabelRangeSize(), new BierTeLabelRangeSize(new Long(3L)));
        Assert.assertEquals(list.get(2).getLabelBase(), new MplsLabel(new Long(5L)));
        Assert.assertEquals(list.get(2).getLabelRangeSize(), new BierTeLabelRangeSize(new Long(2L)));
    }

    private void assertDeletedBierTeLabelRangeTest(List<BierTeLableRange> list) {
        Assert.assertEquals(list.size(), 4);
        Assert.assertEquals(list.get(3).getLabelBase(), new MplsLabel(new Long(5L)));
        Assert.assertEquals(list.get(3).getLabelRangeSize(), new BierTeLabelRangeSize(new Long(2L)));
    }

    private static class BierTeLabelRangeConfigWriterMock implements BierTeLabelRangeConfigWriter {

        private List<BierTeLableRange> bierTeLableRangeList = new ArrayList<>();

        @Override
        public ConfigurationResult writeBierTeLabelRange(ConfigurationType type, String nodeId,
                                                         BierTeLableRange bierTeLableRange) {
            switch (type) {
                case ADD:
                    bierTeLableRangeList.add(bierTeLableRange);
                    break;
                case MODIFY:
                    bierTeLableRangeList.add(bierTeLableRange);
                    break;
                case DELETE:
                    bierTeLableRangeList.add(bierTeLableRange);
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }

        private List<BierTeLableRange> getBierTeLableRangeList() {
            return bierTeLableRangeList;
        }
    }
}
