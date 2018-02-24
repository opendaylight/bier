/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration.te.btaft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.routing.BierTeConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.frr.Btaft;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.frr.BtaftBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.frr.btaft.AddbitmaskBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class BierTeBtaftWriterImplTest extends AbstractConcurrentDataBrokerTest {

    private MountPoint mountPoint;
    private DataBroker dataBroker ;
    private MountPointService mountPointService;
    // Optionals
    private Optional<MountPoint> optionalMountPointObject;
    private Optional<DataBroker> optionalDataBrokerObject;
    private BierTeBtaftWriterImpl bierTeBtaftWriter ;
    private NetconfDataOperator netconfDataOperator;
    private BierTeConfig bierTeConfig;
    private ConfigurationResult result =
            new ConfigurationResult(ConfigurationResult.Result.FAILED);

    private static final String NODE_ID = "nodeId";
    private static final SubDomainId SUBDOMAIN_ID = new SubDomainId(1);
    private static final Integer BSL = 64;
    private static final Integer SI = 0;
    private static final BitString BIT_ADD = new BitString(3);
    private static final Integer FRR_INDEX  = 10;
    private static final Integer BSL_NEW  = 128;

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
        bierTeBtaftWriter = new BierTeBtaftWriterImpl(netconfDataOperator);

    }

    private Btaft buildBtaftEntry(Integer bsl) {
        return new BtaftBuilder()
                .setFrrIndex(FRR_INDEX)
                .setAddbitmask(Collections.singletonList(new AddbitmaskBuilder().setBitmask(BIT_ADD).build()))
                .setFrrBsl(bsl)
                .setFrrSi(SI)
                .build();
    }

    private Btaft getBtaftEntry(BierTeConfig bierTeCfg) {
        return bierTeCfg.getTeSubdomain().get(0).getBtaft().get(0);
    }


    @Test
    public void testWriteTeBtaftAdd() throws Exception {
        buildMock();
        buildInstance();
        Btaft btaftExpected = buildBtaftEntry(BSL);
        bierTeBtaftWriter.writeBierTeBtaft(ConfigurationType.ADD, NODE_ID,SUBDOMAIN_ID,
                btaftExpected, result).checkedGet();
        assertTrue(result.isSuccessful());
        Btaft btaftActual = getBtaftEntry(netconfDataOperator.read(dataBroker,
                BierTeBtaftWriterImpl.BIER_TE_CFG_IID));

        assertEquals(btaftExpected.getFrrIndex(), btaftActual.getFrrIndex());
        assertEquals(btaftExpected.getAddbitmask(), btaftActual.getAddbitmask());
        assertEquals(btaftExpected.getFrrBsl(), btaftActual.getFrrBsl());
        assertEquals(btaftExpected.getFrrSi(), btaftActual.getFrrSi());
    }

    @Test
    public void testWriteTeBtaftModify() throws Exception {
        buildMock();
        buildInstance();
        Btaft btaftAdd = buildBtaftEntry(BSL);
        bierTeBtaftWriter.writeBierTeBtaft(ConfigurationType.ADD, NODE_ID,SUBDOMAIN_ID,
                btaftAdd, result).checkedGet();
        Btaft btaftExpected = buildBtaftEntry(BSL_NEW);
        bierTeBtaftWriter.writeBierTeBtaft(ConfigurationType.MODIFY, NODE_ID,SUBDOMAIN_ID,
                btaftExpected, result).checkedGet();
        Btaft btaftActual = getBtaftEntry(netconfDataOperator.read(dataBroker,
                BierTeBtaftWriterImpl.BIER_TE_CFG_IID));

        assertEquals(btaftExpected.getFrrBsl(), btaftActual.getFrrBsl());
    }

    @Test
    public void testWriteTeBtaftDelete() throws Exception {
        buildMock();
        buildInstance();
        Btaft btaft = buildBtaftEntry(BSL);
        bierTeBtaftWriter.writeBierTeBtaft(ConfigurationType.ADD, NODE_ID,SUBDOMAIN_ID,
                btaft, result).checkedGet();
        bierTeBtaftWriter.writeBierTeBtaft(ConfigurationType.DELETE, NODE_ID,SUBDOMAIN_ID,
                btaft, result).checkedGet();
        assertTrue(result.isSuccessful());
        Btaft btaftActual = netconfDataOperator.read(dataBroker,
                bierTeBtaftWriter.getBtaftIid(SUBDOMAIN_ID,btaft));
        assertNull(btaftActual);
    }

    @Test
    public void testWriteTeBtaft() throws Exception {
        buildMock();
        buildInstance();
        Btaft btaft = buildBtaftEntry(BSL);
        ConfigurationResult writeResult = bierTeBtaftWriter.writeBierTeBtaft(ConfigurationType.ADD, NODE_ID,
                SUBDOMAIN_ID,btaft);
        assertTrue(writeResult.isSuccessful());
    }


}