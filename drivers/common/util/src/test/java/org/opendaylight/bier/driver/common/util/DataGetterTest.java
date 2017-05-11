/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.driver.common.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.junit.Test;


import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.driver.common.util.DataGetter;
import org.opendaylight.bier.driver.common.util.IidBuilder;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticast;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.multicast.information.rev161028.multicast.information.pure.multicast.PureMulticastBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class DataGetterTest {
    private static final String NODE_ID = "device1";
    ConfigurationResult result = new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);

    @Test
    public void testGetMountPointMountPointServiceNull() throws Exception {
        MountPointService mountService = null;
        MountPoint mountPoint = DataGetter.getMountPoint(NODE_ID, result, mountService);
        assertNull(mountPoint);
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testGetMountPointMountPointNull() throws Exception {

        MountPointService mountService = mock(MountPointService.class);
        Optional<MountPoint> mountPointOptional = mock(Optional.class);
        when(mountService.getMountPoint(any())).thenReturn(mountPointOptional);
        when(mountPointOptional.isPresent()).thenReturn(false);
        MountPoint mountPoint = DataGetter.getMountPoint(NODE_ID, result, mountService);
        assertNull(mountPoint);
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testGetMountPointSuccess() throws Exception {

        MountPointService mountService = mock(MountPointService.class);
        Optional<MountPoint> mountPointOptional = mock(Optional.class);
        when(mountService.getMountPoint(any())).thenReturn(mountPointOptional);
        when(mountPointOptional.isPresent()).thenReturn(true);

        MountPoint mountPoint = mock(MountPoint.class);
        when(mountPointOptional.get()).thenReturn(mountPoint);

        assertNotNull(DataGetter.getMountPoint(NODE_ID, result, mountService));
        assertTrue(result.isSuccessful());
    }

    @Test
    public void testGetDataBrokerGetMountPointNull() throws Exception {
        MountPointService mountService = null;
        DataBroker dataBroker = DataGetter.getDataBroker(NODE_ID, result, mountService);
        assertNull(dataBroker);
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testGetDataBrokerNull() throws Exception {
        MountPointService mountService = mock(MountPointService.class);
        Optional<MountPoint> mountPointOptional = mock(Optional.class);
        when(mountService.getMountPoint(any())).thenReturn(mountPointOptional);
        when(mountPointOptional.isPresent()).thenReturn(true);

        MountPoint mountPoint = mock(MountPoint.class);
        when(mountPointOptional.get()).thenReturn(mountPoint);

        Optional<DataBroker> dataBrokerOptional = mock(Optional.class);
        when(mountPoint.getService(DataBroker.class)).thenReturn(dataBrokerOptional);

        when(dataBrokerOptional.isPresent()).thenReturn(false);
        DataBroker dataBroker = DataGetter.getDataBroker(NODE_ID, result, mountService);
        assertNull(dataBroker);
        assertFalse(result.isSuccessful());
    }


    @Test
    public void testGetDataBrokerSuccess() throws Exception {
        MountPointService mountService = mock(MountPointService.class);
        Optional<MountPoint> mountPointOptional = mock(Optional.class);
        when(mountService.getMountPoint(any())).thenReturn(mountPointOptional);
        when(mountPointOptional.isPresent()).thenReturn(true);

        MountPoint mountPoint = mock(MountPoint.class);
        when(mountPointOptional.get()).thenReturn(mountPoint);

        Optional<DataBroker> dataBrokerOptional = mock(Optional.class);
        when(mountPoint.getService(DataBroker.class)).thenReturn(dataBrokerOptional);

        when(dataBrokerOptional.isPresent()).thenReturn(true);
        DataBroker dataBroker = mock(DataBroker.class);
        when(dataBrokerOptional.get()).thenReturn(dataBroker);
        assertNotNull(DataGetter.getDataBroker(NODE_ID, result, mountService));
        assertTrue(result.isSuccessful());
    }

    @Test
    public void testReadDataDataBrokerNull() throws Exception {
        MountPointService mountService = null;

        assertNull(DataGetter.readData(NODE_ID,
                IidBuilder.buildPureMulticastIId(new ChannelBuilder().build()),
                mountService,
                LogicalDatastoreType.CONFIGURATION));
    }

    //@Test
    public void testReadDataFailed() throws Exception {
        MountPointService mountService = mock(MountPointService.class);
        Optional<MountPoint> mountPointOptional = mock(Optional.class);
        when(mountService.getMountPoint(any())).thenReturn(mountPointOptional);
        when(mountPointOptional.isPresent()).thenReturn(true);

        MountPoint mountPoint = mock(MountPoint.class);
        when(mountPointOptional.get()).thenReturn(mountPoint);

        Optional<DataBroker> dataBrokerOptional = mock(Optional.class);
        when(mountPoint.getService(DataBroker.class)).thenReturn(dataBrokerOptional);

        when(dataBrokerOptional.isPresent()).thenReturn(true);
        DataBroker dataBroker = mock(DataBroker.class);
        when(dataBrokerOptional.get()).thenReturn(dataBroker);

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);

        CheckedFuture<Optional<PureMulticast>, ReadFailedException> readResult = mock(CheckedFuture.class);
        Channel channel = new ChannelBuilder().build();
        InstanceIdentifier<PureMulticast> pureMulticastIId = IidBuilder.buildPureMulticastIId(channel);
        when(transaction.read(any(),eq(pureMulticastIId))).thenReturn(readResult);
        Optional<PureMulticast> pureMulticastOptional = mock(Optional.class);
        when(readResult.checkedGet()).thenReturn(pureMulticastOptional);
        when(pureMulticastOptional.isPresent()).thenReturn(false);

        assertNull(DataGetter.readData(NODE_ID,pureMulticastIId,mountService,LogicalDatastoreType.CONFIGURATION));

    }


    //@Test
    public void testReadDataSuccess() throws Exception {
        MountPointService mountService = mock(MountPointService.class);
        Optional<MountPoint> mountPointOptional = mock(Optional.class);
        when(mountService.getMountPoint(any())).thenReturn(mountPointOptional);
        when(mountPointOptional.isPresent()).thenReturn(true);

        MountPoint mountPoint = mock(MountPoint.class);
        when(mountPointOptional.get()).thenReturn(mountPoint);

        Optional<DataBroker> dataBrokerOptional = mock(Optional.class);
        when(mountPoint.getService(DataBroker.class)).thenReturn(dataBrokerOptional);

        when(dataBrokerOptional.isPresent()).thenReturn(true);
        DataBroker dataBroker = mock(DataBroker.class);
        when(dataBrokerOptional.get()).thenReturn(dataBroker);

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);

        CheckedFuture<Optional<PureMulticast>, ReadFailedException> readResult = mock(CheckedFuture.class);
        Channel channel = new ChannelBuilder().build();
        InstanceIdentifier<PureMulticast> pureMulticastIId = IidBuilder.buildPureMulticastIId(channel);
        when(transaction.read(any(),eq(pureMulticastIId))).thenReturn(readResult);
        Optional<PureMulticast> pureMulticastOptional = mock(Optional.class);
        when(readResult.checkedGet()).thenReturn(pureMulticastOptional);
        when(pureMulticastOptional.isPresent()).thenReturn(true);
        PureMulticast pureMulticast = new PureMulticastBuilder().build();
        when(pureMulticastOptional.get()).thenReturn(pureMulticast);
        assertNotNull(DataGetter.readData(NODE_ID,pureMulticastIId,mountService, LogicalDatastoreType.CONFIGURATION));

    }


}