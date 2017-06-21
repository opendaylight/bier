/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.driver.configuration.te.label;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.driver.NetconfDataOperator;

import org.opendaylight.bier.driver.configuration.te.label.BierTeLabelRangeConfigWriterImpl;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierTeLabelRangeSize;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeLableRange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeLableRangeBuilder;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev170311.label.blocks.label.blocks.label.block.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class BierTeLabelRangeConfigWriterImplTest  extends AbstractConcurrentDataBrokerTest {
    private MountPoint mountPoint;
    private DataBroker dataBroker ;
    private MountPointService mountPointService;
    // Optionals
    private Optional<MountPoint> optionalMountPointObject;
    private Optional<DataBroker> optionalDataBrokerObject;
    private BierTeLabelRangeConfigWriterImpl bierTeLabelRangeConfigWriter;
    private NetconfDataOperator netconfDataOperator;

    private ConfigurationResult result =
           new ConfigurationResult(ConfigurationResult.Result.FAILED);

    private static final String NODE_ID = "nodeId";
    private static final Long LABLE_MIN = 16L;
    private static final Long LABLE_MAX = 216L;
    private static final Long LABLE_MAX_MOD = 250L;
    private static final Long LABLE_RANGE_SIZE = 200L;
    private static final Long LABLE_MIN_INVALID = 1L;


    @Before
    public void before() throws Exception {
        dataBroker = getDataBroker();
    }

    private void buildMock() {
        mountPoint = mock(MountPoint.class);
        mountPointService = mock(MountPointService.class);
        optionalMountPointObject = mock(Optional.class);
        optionalDataBrokerObject = mock(Optional.class);

        when(mountPointService.getMountPoint(any(InstanceIdentifier.class))).thenReturn(optionalMountPointObject);
        when(mountPoint.getService(eq(DataBroker.class))).thenReturn(optionalDataBrokerObject);
        // Mock getting mountpoint
        when(optionalMountPointObject.isPresent()).thenReturn(true);
        // OptionalGetWithoutIsPresent
        when(optionalMountPointObject.get()).thenReturn(mountPoint);
        when(optionalDataBrokerObject.isPresent()).thenReturn(true);
        // OptionalGetWithoutIsPresent
        when(optionalDataBrokerObject.get()).thenReturn(dataBroker);

    }

    private void buildInstance() {
        netconfDataOperator = new NetconfDataOperator(mountPointService);
        bierTeLabelRangeConfigWriter = new BierTeLabelRangeConfigWriterImpl(netconfDataOperator);

    }


   //@Test
    public void testWriteLabelRangeAdd() throws Exception {
        buildMock();
        buildInstance();
        bierTeLabelRangeConfigWriter.writeBierTeLabelRange(ConfigurationType.ADD,
              LABLE_MIN,LABLE_MAX,NODE_ID,result).checkedGet();
        assertTrue(result.isSuccessful());
        Config config = netconfDataOperator.read(dataBroker,
              bierTeLabelRangeConfigWriter.getLableBlockConfigIid(LABLE_MIN.toString()));
        assertEquals(LABLE_MIN, config.getStartLabel().getMplsLabelGeneralUse().getValue());
        assertEquals(LABLE_MAX, config.getEndLabel().getMplsLabelGeneralUse().getValue());
    }

    //@Test
    public void testWriteLabelRangeModify() throws Exception {
        buildMock();
        buildInstance();
        bierTeLabelRangeConfigWriter.writeBierTeLabelRange(ConfigurationType.ADD,
               LABLE_MIN,LABLE_MAX,NODE_ID,result).checkedGet();
        assertTrue(result.isSuccessful());
        bierTeLabelRangeConfigWriter.writeBierTeLabelRange(ConfigurationType.MODIFY,
               LABLE_MIN,LABLE_MAX_MOD,NODE_ID,result).checkedGet();
        Config config = netconfDataOperator.read(dataBroker,
               bierTeLabelRangeConfigWriter.getLableBlockConfigIid(LABLE_MIN.toString()));
        assertEquals(LABLE_MIN, config.getStartLabel().getMplsLabelGeneralUse().getValue());
        assertEquals(LABLE_MAX_MOD, config.getEndLabel().getMplsLabelGeneralUse().getValue());
    }

    //@Test
    public void testWriteLabelRangeDelete() throws Exception {
        buildMock();
        buildInstance();
        bierTeLabelRangeConfigWriter.writeBierTeLabelRange(ConfigurationType.ADD,
               LABLE_MIN,LABLE_MAX,NODE_ID,result).checkedGet();
        assertTrue(result.isSuccessful());
        bierTeLabelRangeConfigWriter.writeBierTeLabelRange(ConfigurationType.DELETE,
               LABLE_MIN,LABLE_MAX_MOD,NODE_ID,result).checkedGet();
        Config config = netconfDataOperator.read(dataBroker,
               bierTeLabelRangeConfigWriter.getLableBlockConfigIid(LABLE_MIN.toString()));
        assertNull(config);
    }

    //@Test
    public void testWriteLabelRangeValid() throws Exception {
        buildMock();
        buildInstance();
        BierTeLableRange bierTeLableRange = new BierTeLableRangeBuilder()
               .setLabelBase(new MplsLabel(LABLE_MIN))
               .setLabelRangeSize(new BierTeLabelRangeSize(LABLE_RANGE_SIZE))
               .build();
        ConfigurationResult writeResult = bierTeLabelRangeConfigWriter.writeBierTeLabelRange(ConfigurationType.ADD,
                NODE_ID, bierTeLableRange);
        assertTrue(writeResult.isSuccessful());
    }

    @Test
    public void testWriteLabelRangeInValid() throws Exception {
        buildMock();
        buildInstance();
        BierTeLableRange bierTeLableRange = new BierTeLableRangeBuilder()
                .setLabelBase(new MplsLabel(LABLE_MIN_INVALID))
                .setLabelRangeSize(new BierTeLabelRangeSize(LABLE_RANGE_SIZE))
                .build();
        ConfigurationResult writeResult = bierTeLabelRangeConfigWriter.writeBierTeLabelRange(ConfigurationType.ADD,
                NODE_ID, bierTeLableRange);
        assertFalse(writeResult.isSuccessful());
    }



}