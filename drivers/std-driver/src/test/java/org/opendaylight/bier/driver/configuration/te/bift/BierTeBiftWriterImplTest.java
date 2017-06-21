/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.driver.configuration.te.bift;

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
import org.opendaylight.bier.driver.configuration.te.bift.BierTeBiftWriterImpl;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.routing.BierTeConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.routing.BierTeConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.adj.type.TeAdjType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.adj.type.te.adj.type.ConnectedBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.TeSiBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.te.si.TeFIndex;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.te.si.TeFIndexBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.TeSubdomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.te.subdomain.TeBslBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class BierTeBiftWriterImplTest extends AbstractConcurrentDataBrokerTest {

    private MountPoint mountPoint;
    private DataBroker dataBroker ;
    private MountPointService mountPointService;
    // Optionals
    private Optional<MountPoint> optionalMountPointObject;
    private Optional<DataBroker> optionalDataBrokerObject;
    private BierTeBiftWriterImpl bierTeBiftWriter ;
    private NetconfDataOperator netconfDataOperator;
    private BierTeConfig bierTeConfig;
    private ConfigurationResult result =
            new ConfigurationResult(ConfigurationResult.Result.FAILED);

    private static final String NODE_ID = "nodeId";
    private static final Integer SUBDOMAIN_ID = 1;
    private static final Integer BSL = 64;
    private static final Integer SI = 0;
    private static final Integer BIT_POSITION = 1;
    private static final Long IF_INDEX  = 10L;
    private static final Long IN_LABEL = 50L;
    private static final Long OUT_LABEL = 51L;
    private static final Long OUT_LABEL_NEW = 50L;
    private static final TeAdjType ADJ_TYPE = new ConnectedBuilder().build();

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
        bierTeBiftWriter = new BierTeBiftWriterImpl(netconfDataOperator);

    }

    private void buildBift(Long outLabel) {
        bierTeConfig = new BierTeConfigBuilder()
                .setTeSubdomain(Collections.singletonList(new TeSubdomainBuilder()
                        .setSubdomainId(new SubDomainId(SUBDOMAIN_ID))
                        .setTeBsl(Collections.singletonList(new TeBslBuilder()
                                .setFwdBsl(BSL)
                                .setTeSi(Collections.singletonList(new TeSiBuilder()
                                        .setFtLabel(new MplsLabel(IN_LABEL))
                                                .setSi(new Si(SI))
                                                .setTeFIndex(Collections.singletonList(new TeFIndexBuilder()
                                                        .setTeFIndex(new BitString(BIT_POSITION))
                                                        .setTeAdjType(ADJ_TYPE)
                                                        .setOutLabel(new MplsLabel(outLabel))
                                                        .build()))
                                                .build()))

                                        .build()))
                                .build()))
                        .build();
    }

    private TeFIndex getBiftEntry(BierTeConfig bierTeCfg) {
        return bierTeCfg.getTeSubdomain().get(0).getTeBsl()
                .get(0).getTeSi().get(0).getTeFIndex().get(0);
    }

    @Test
    public void testWriteTeBiftAdd() throws Exception {
        buildMock();
        buildInstance();
        buildBift(OUT_LABEL);
        bierTeBiftWriter.writeTeBift(ConfigurationType.ADD, NODE_ID,
                bierTeConfig,result).checkedGet();
        assertTrue(result.isSuccessful());
        TeFIndex teFIndexActual = getBiftEntry(netconfDataOperator.read(dataBroker,
                BierTeBiftWriterImpl.BIER_TE_CFG_IID));
        TeFIndex teFIndexExpected = getBiftEntry(bierTeConfig);
        assertEquals(teFIndexExpected.getTeFIndex(), teFIndexActual.getTeFIndex());
        assertEquals(teFIndexExpected.getFIntf(), teFIndexActual.getFIntf());
        assertEquals(teFIndexExpected.getOutLabel(), teFIndexActual.getOutLabel());
    }

    @Test
    public void testWriteTeBiftModify() throws Exception {
        buildMock();
        buildInstance();
        buildBift(OUT_LABEL);
        bierTeBiftWriter.writeTeBift(ConfigurationType.ADD, NODE_ID,
                bierTeConfig,result).checkedGet();
        assertTrue(result.isSuccessful());
        buildBift(OUT_LABEL_NEW);
        bierTeBiftWriter.writeTeBift(ConfigurationType.MODIFY, NODE_ID,
                bierTeConfig,result).checkedGet();
        assertTrue(result.isSuccessful());
        TeFIndex teFIndexActual = getBiftEntry(netconfDataOperator.read(dataBroker,
                BierTeBiftWriterImpl.BIER_TE_CFG_IID));
        TeFIndex teFIndexExpected = getBiftEntry(bierTeConfig);

        assertEquals(teFIndexExpected.getOutLabel(), teFIndexActual.getOutLabel());

    }

    @Test
    public void testWriteTeBiftDelete() throws Exception {
        buildMock();
        buildInstance();
        buildBift(OUT_LABEL);
        bierTeBiftWriter.writeTeBift(ConfigurationType.ADD, NODE_ID,
                bierTeConfig,result).checkedGet();
        assertTrue(result.isSuccessful());
        bierTeBiftWriter.writeTeBift(ConfigurationType.DELETE, NODE_ID,
                bierTeConfig,result).checkedGet();
        assertTrue(result.isSuccessful());
        TeFIndex teFIndexActual = netconfDataOperator.read(dataBroker,
                bierTeBiftWriter.getTeFIndexIId(bierTeConfig));
        assertNull(teFIndexActual);
    }

    @Test
    public void testWriteTeBift() throws Exception {
        buildMock();
        buildInstance();
        buildBift(OUT_LABEL);
        ConfigurationResult writeResult = bierTeBiftWriter.writeTeBift(ConfigurationType.ADD, NODE_ID,
                bierTeConfig);
        assertTrue(writeResult.isSuccessful());
    }


}