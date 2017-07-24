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
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
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


    //data
    private static final String NODE_ID = "nodeId";
    private static final IpAddress SRC_IP = new IpAddress(new Ipv4Address("10.41.42.60"));
    private static final short SRC_WILDCARD = 8;
    private static final IpAddress DEST_GROUP = new IpAddress(new Ipv4Address("224.0.0.5"));
    private static final short DEST_WILDCARD = 4;
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
        channelConfigWriter = new ChannelConfigWriterImpl(netconfDataOperator);
        channelConfigReader = new ChannelConfigReaderImpl(netconfDataOperator);
    }



    @Test
    public void testReadChannel() throws Exception {
        buildMock();
        buildInstance();

        //egress nodes list
        ArrayList<EgressNode> egressNodeArrayList = new ArrayList<EgressNode>();
        egressNodeArrayList.add(new EgressNodeBuilder().setEgressBfrId(new BfrId(EGRESS_BFR1)).build());
        egressNodeArrayList.add(new EgressNodeBuilder().setEgressBfrId(new BfrId(EGRESS_BFR2)).build());

        ArrayList<BfrId> egressBfrIdsExpected = new ArrayList<BfrId>();
        egressBfrIdsExpected.add(new BfrId(EGRESS_BFR1));
        egressBfrIdsExpected.add(new BfrId(EGRESS_BFR2));

        //write channel to datastore

        Channel channel = new ChannelBuilder()
                .setIngressNode(NODE_ID)
                .setSrcIp(SRC_IP)
                .setSourceWildcard(SRC_WILDCARD)
                .setDstGroup(DEST_GROUP)
                .setGroupWildcard(DEST_WILDCARD)
                .setIngressBfrId(new BfrId(100))
                .setEgressNode(egressNodeArrayList)
                .build();

        channelConfigWriter.writeChannel(ConfigurationType.ADD,channel,result).checkedGet();
        List<BfrId> egressBfrIdsActual = channelConfigReader.readChannel(channel);

        assertTrue(egressBfrIdsActual.containsAll(egressBfrIdsExpected));
        assertTrue(egressBfrIdsExpected.containsAll(egressBfrIdsActual));
    }
}