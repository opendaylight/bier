/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.driver.configuration.channel;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.bier.driver.configuration.channel.ChannelConfigReaderImpl;
import org.opendaylight.bier.driver.configuration.channel.ChannelConfigWriterImpl;
import org.opendaylight.bier.driver.configuration.channel.ChannelTestDataBuilder;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;




public class ChannelConfigReaderImplTest extends AbstractConcurrentDataBrokerTest {

    private MountPoint mountPoint;
    private DataBroker dataBroker ;
    private MountPointService mountPointService;
    // Optionals

    private Optional<MountPoint> optionalMountPointObject;

    private Optional<DataBroker> optionalDataBrokerObject;
    private ChannelConfigWriterImpl channelConfigWriter ;
    private ChannelConfigReaderImpl channelConfigReader;
    private NetconfDataOperator netconfDataOperator;
    private ConfigurationResult result =
            new ConfigurationResult(ConfigurationResult.Result.FAILED);



    private static final Integer EGRESS_BFR1 = new Integer(11);
    private static final Integer EGRESS_BFR2 = new Integer(22);

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
        channelConfigWriter = new ChannelConfigWriterImpl(netconfDataOperator,dataBroker);
        channelConfigReader = new ChannelConfigReaderImpl(netconfDataOperator);
    }



    @Test
    public void testReadChannel() throws Exception {
        buildMock();
        buildInstance();

        ArrayList<BfrId> egressBfrIdsExpected = new ArrayList<BfrId>();
        egressBfrIdsExpected.add(new BfrId(EGRESS_BFR1));
        egressBfrIdsExpected.add(new BfrId(EGRESS_BFR2));

        //interface to datastore
        ChannelTestDataBuilder.writeInterfaceInfo(ChannelTestDataBuilder.RETRY_WRITE_MAX,dataBroker);


        //write channel to datastore

        Channel channel = ChannelTestDataBuilder.buildChannelData(new BfrId(100),
                new BfrId(EGRESS_BFR1),new BfrId(EGRESS_BFR2));

        channelConfigWriter.writeChannel(ConfigurationType.ADD,channel,result).checkedGet();
        List<BfrId> egressBfrIdsActual = channelConfigReader.readChannel(channel);

        assertTrue(egressBfrIdsActual.containsAll(egressBfrIdsExpected));
        assertTrue(egressBfrIdsExpected.containsAll(egressBfrIdsActual));
    }
}