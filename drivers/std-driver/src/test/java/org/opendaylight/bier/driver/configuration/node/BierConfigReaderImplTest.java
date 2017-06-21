/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.driver.configuration.node;

import static org.junit.Assert.assertEquals;
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
import org.opendaylight.bier.driver.configuration.node.BierConfigDataBuilder;
import org.opendaylight.bier.driver.configuration.node.BierConfigReaderImpl;
import org.opendaylight.bier.driver.configuration.node.BierConfigWriterImpl;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobal;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;




public class BierConfigReaderImplTest extends AbstractConcurrentDataBrokerTest {
    private MountPoint mountPoint;
    private DataBroker dataBroker ;
    private MountPointService mountPointService;
    // Optionals
    private Optional<MountPoint> optionalMountPointObject;
    private Optional<DataBroker> optionalDataBrokerObject;
    private BierConfigWriterImpl bierConfigWriter ;
    private BierConfigReaderImpl bierConfigReader ;
    private NetconfDataOperator netconfDataOperator;

    private static final String NODE_ID = "nodeId";
    private BierConfigDataBuilder bierConfigDataBuilder;
    private ConfigurationResult result =
            new ConfigurationResult(ConfigurationResult.Result.FAILED);

    @Before
    public void before() throws Exception {
        dataBroker = getDataBroker();
        bierConfigDataBuilder = new BierConfigDataBuilder();
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
        bierConfigWriter = new BierConfigWriterImpl(netconfDataOperator);
        bierConfigReader = new BierConfigReaderImpl(netconfDataOperator);

    }

    @Test
    public void testReadBierGlobal() throws Exception {
        buildMock();
        buildInstance();
        bierConfigWriter.writeDomain(ConfigurationType.ADD, NODE_ID,
                bierConfigDataBuilder.buildDomain(),result).checkedGet();

        BierGlobal bierGlobalActual = bierConfigReader.readBierGlobal(NODE_ID);
        BierGlobal bierGlobalExpected = bierConfigDataBuilder.buildBierGlobal();
        assertEquals(bierGlobalExpected,bierGlobalActual);
    }

}
