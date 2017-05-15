/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.driver.configuration.interfaces;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.bier.driver.common.util.DataWriter;
import org.opendaylight.bier.driver.configuration.interfaces.DeviceInterfaceReaderImpl;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.Interfaces;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.InterfaceBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class DeviceInterfaceReaderImplTest extends AbstractConcurrentDataBrokerTest {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceInterfaceReaderImplTest.class);

    private MountPoint mountPoint;
    private DataBroker dataBroker ;
    private BindingAwareBroker bindingAwareBroker;
    private BindingAwareBroker.ConsumerContext consumerContext;
    private MountPointService mountPointService;
    private Optional<MountPoint> optionalMountPointObject;
    private Optional<DataBroker> optionalDataBrokerObject;
    private DeviceInterfaceReaderImpl deviceIfReader ;
    private Interfaces interfaces;
    private NetconfDataOperator netconfDataOperator;

    private static final String NODE_ID = "nodeId";
    private static final String IF_NAME = "fei-0/1/0/1";

    private ConfigurationResult result =
            new ConfigurationResult(ConfigurationResult.Result.FAILED);

    @Before
    public void before() throws Exception {
        dataBroker = getDataBroker();

    }

    private void buildMock() {
        mountPoint = mock(MountPoint.class);
        bindingAwareBroker = mock(BindingAwareBroker.class);
        consumerContext = mock(BindingAwareBroker.ConsumerContext.class);
        mountPointService = mock(MountPointService.class);
        optionalMountPointObject = mock(Optional.class);
        optionalDataBrokerObject = mock(Optional.class);

        when(bindingAwareBroker.registerConsumer(any(BindingAwareConsumer.class))).thenReturn(consumerContext);
        when(consumerContext.getSALService(any())).thenReturn(mountPointService);
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
        netconfDataOperator = new NetconfDataOperator(bindingAwareBroker);
        deviceIfReader = new DeviceInterfaceReaderImpl(netconfDataOperator);
        netconfDataOperator.onSessionInitialized(consumerContext);
    }

    private void buildIfData() {
        interfaces = new InterfacesBuilder()
                .setInterface(Collections.singletonList(new InterfaceBuilder().setName(IF_NAME).build()))
                .build();
    }

    @Test
    public void testReadDeviceInterface() throws Exception {
        buildMock();
        buildInstance();
        buildIfData();
        netconfDataOperator.write(
                DataWriter.OperateType.MERGE,
                NODE_ID,
                DeviceInterfaceReaderImpl.INTERFACES_IID,
                interfaces,
                result).get();//use get() to wait until the data is stored
        List<BierTerminationPoint> bierTerminationPointList = deviceIfReader.readDeviceInterface(NODE_ID);
        assertEquals(IF_NAME,bierTerminationPointList.get(0).getIfName());
    }


}