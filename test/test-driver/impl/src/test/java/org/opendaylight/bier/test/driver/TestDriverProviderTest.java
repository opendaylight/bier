/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.test.driver;

import static org.junit.Assert.assertEquals;

import static org.mockito.Matchers.any;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.bier.adapter.api.BierConfigReader;
import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.ChannelConfigReader;
import org.opendaylight.bier.adapter.api.ChannelConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.driver.common.util.IidConstants;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.ChannelKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNode;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.channel.EgressNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.CheckBierGlobalInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.CheckBierGlobalInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.CheckChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.CheckChannelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.ConfigType;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.ReadBierGlobalInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.ReadBierGlobalInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetChannelInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetChannelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetDomainConfigInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetDomainConfigInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetEgressNodeInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetEgressNodeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetIpv4ConfigInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetIpv4ConfigInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetSubdomainConfigInput;
import org.opendaylight.yang.gen.v1.urn.bier.test.driver.rev161219.SetSubdomainConfigInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierEncapsulationMpls;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierMplsLabelRangeSize;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.IgpType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobal;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobalBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.AfBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;



@RunWith(MockitoJUnitRunner.class)
public class TestDriverProviderTest {
    @Mock
    DataBroker dataBroker;
    @Mock
    BierConfigWriter bierConfigWriter;
    @Mock
    BierConfigReader bierConfigReader;
    @Mock
    RpcProviderRegistry rpcRegistry;
    @Mock
    ChannelConfigWriter channelConfigWriter;
    @Mock
    ChannelConfigReader channelConfigReader;


    private TestDriverProvider testDriverProvider;

    private ConfigurationResult result = new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);

    private static final String NODE_ID = "device1";
    private static final Integer DOMAIN_ID = 1;
    private static final SubDomainId SUBDOMAINID = new SubDomainId(new Integer(55));
    public static final Integer BSL = new Integer(64);
    public static final MplsLabel MPLS_LABEL_BASE = new MplsLabel(new Long(10));

    private static final IpAddress SRC_IP = new IpAddress(new Ipv4Address("10.41.42.60"));
    private static final short SRC_WILDCARD = 8;
    private static final IpAddress DEST_GROUP = new IpAddress(new Ipv4Address("224.0.0.5"));
    private static final short DEST_WILDCARD = 4;
    private static final Integer INGRESS_BFR1 = new Integer(10);
    private static final Integer EGRESS_BFR1 = new Integer(11);
    private static final Integer EGRESS_BFR2 = new Integer(22);
    private static final String EGRESS_NODE_ID1 = "device11";
    private static final String EGRESS_NODE_ID2 = "device22";
    private static final String CHANNEL_NAME = "channel_1";

    private void initInstance() {
        testDriverProvider = new TestDriverProvider(dataBroker);
        testDriverProvider.setBierConfigWriter(bierConfigWriter);
        testDriverProvider.setBierConfigReader(bierConfigReader);
        testDriverProvider.setChannelConfigWriter(channelConfigWriter);
        testDriverProvider.setChannelConfigReader(channelConfigReader);
        testDriverProvider.setRpcRegistry(rpcRegistry);



    }

    private List<Ipv4> buildIpv4() {
        ArrayList<Ipv4> ipv4ArrayList = new ArrayList<Ipv4>();
        ipv4ArrayList.add(new Ipv4Builder().setBitstringlength(64)
                .setBierMplsLabelBase(new MplsLabel(new Long(10)))
                .setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(new Short((short) 1)))
                .build());
        ipv4ArrayList.add(new Ipv4Builder().setBitstringlength(256)
                .setBierMplsLabelBase(new MplsLabel(new Long(100)))
                .setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(new Short((short) 2)))
                .build());
        return ipv4ArrayList;
    }

    private List<Ipv4> buildIpv4RangeSizeDiff() {
        ArrayList<Ipv4> ipv4ArrayList = new ArrayList<Ipv4>();
        ipv4ArrayList.add(new Ipv4Builder().setBitstringlength(64)
                .setBierMplsLabelBase(new MplsLabel(new Long(10)))
                .setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(new Short((short) 10)))
                .build());
        ipv4ArrayList.add(new Ipv4Builder().setBitstringlength(256)
                .setBierMplsLabelBase(new MplsLabel(new Long(100)))
                .setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(new Short((short) 2)))
                .build());
        return ipv4ArrayList;
    }

    private List<Ipv4> buildIpv4SizeDiff() {
        ArrayList<Ipv4> ipv4ArrayList = new ArrayList<Ipv4>();
        ipv4ArrayList.add(new Ipv4Builder().setBitstringlength(64)
                .setBierMplsLabelBase(new MplsLabel(new Long(10)))
                .setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(new Short((short) 1)))
                .build());
        return ipv4ArrayList;
    }

    private List<SubDomain> buildSubDomainIpv4RangeSizeDidff() {
        ArrayList<SubDomain> subDomainArrayList = new ArrayList<SubDomain>();
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(55)))
                .setBfrId(new BfrId(1))
                .setBitstringlength(Bsl._1024Bit)
                .setIgpType(IgpType.OSPF)
                .setAf(new AfBuilder()
                        .setIpv4(buildIpv4RangeSizeDiff())
                        .build())
                .build());
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(66)))
                .setBfrId(new BfrId(2))
                .setBitstringlength(Bsl._2048Bit)
                .setIgpType(IgpType.ISIS)
                .build());
        return subDomainArrayList;
    }

    private List<SubDomain> buildSubDomainIpv4SizeDiff() {
        ArrayList<SubDomain> subDomainArrayList = new ArrayList<SubDomain>();
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(55)))
                .setBfrId(new BfrId(1))
                .setBitstringlength(Bsl._1024Bit)
                .setIgpType(IgpType.OSPF)
                .setAf(new AfBuilder()
                        .setIpv4(buildIpv4SizeDiff())
                        .build())
                .build());
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(66)))
                .setBfrId(new BfrId(2))
                .setBitstringlength(Bsl._2048Bit)
                .setIgpType(IgpType.ISIS)
                .build());
        return subDomainArrayList;
    }


    private List<SubDomain> buildSubDomain() {
        ArrayList<SubDomain> subDomainArrayList = new ArrayList<SubDomain>();
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(55)))
                .setBfrId(new BfrId(1))
                .setBitstringlength(Bsl._1024Bit)
                .setIgpType(IgpType.OSPF)
                .setAf(new AfBuilder()
                        .setIpv4(buildIpv4())
                        .build())
                .build());
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(66)))
                .setBfrId(new BfrId(2))
                .setBitstringlength(Bsl._2048Bit)
                .setIgpType(IgpType.ISIS)
                .build());
        return subDomainArrayList;
    }

    private List<SubDomain> buildSubDomainBfrIdDiff() {
        ArrayList<SubDomain> subDomainArrayList = new ArrayList<SubDomain>();
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(55)))
                .setBfrId(new BfrId(10))
                .setBitstringlength(Bsl._1024Bit)
                .setIgpType(IgpType.OSPF)
                .setAf(new AfBuilder()
                        .setIpv4(buildIpv4())
                        .build())
                .build());
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(66)))
                .setBfrId(new BfrId(2))
                .setBitstringlength(Bsl._2048Bit)
                .setIgpType(IgpType.ISIS)
                .build());
        return subDomainArrayList;
    }

    private List<SubDomain> buildSubDomainBslDiff() {
        ArrayList<SubDomain> subDomainArrayList = new ArrayList<SubDomain>();
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(55)))
                .setBfrId(new BfrId(1))
                .setBitstringlength(Bsl._256Bit)
                .setIgpType(IgpType.OSPF)
                .setAf(new AfBuilder()
                        .setIpv4(buildIpv4())
                        .build())
                .build());
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(66)))
                .setBfrId(new BfrId(2))
                .setBitstringlength(Bsl._2048Bit)
                .setIgpType(IgpType.ISIS)
                .build());
        return subDomainArrayList;
    }

    private List<SubDomain> buildSubDomainIgpTypeDiff() {
        ArrayList<SubDomain> subDomainArrayList = new ArrayList<SubDomain>();
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(55)))
                .setBfrId(new BfrId(1))
                .setBitstringlength(Bsl._1024Bit)
                .setIgpType(IgpType.ISIS)
                .setAf(new AfBuilder()
                        .setIpv4(buildIpv4())
                        .build())
                .build());
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(66)))
                .setBfrId(new BfrId(2))
                .setBitstringlength(Bsl._2048Bit)
                .setIgpType(IgpType.ISIS)
                .build());
        return subDomainArrayList;
    }

    private List<SubDomain> buildSubDomainSizeDiff() {
        ArrayList<SubDomain> subDomainArrayList = new ArrayList<SubDomain>();
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(55)))
                .setBfrId(new BfrId(1))
                .setBitstringlength(Bsl._1024Bit)
                .setIgpType(IgpType.OSPF)
                .setAf(new AfBuilder()
                        .setIpv4(buildIpv4())
                        .build())
                .build());
        return subDomainArrayList;
    }

    private BierGlobal buildBierGlobal() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(3)))
                .setBitstringlength(Bsl._4096Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.60/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("fe80::/10"))
                .setSubDomain(buildSubDomain())
                .setEncapsulationType(BierEncapsulationMpls.class)
                .build();

    }

    private BierGlobal buildBierGlobalBfrIdDiff() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(4)))
                .setBitstringlength(Bsl._4096Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.60/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("fe80::/10"))
                .setSubDomain(buildSubDomain())
                .setEncapsulationType(BierEncapsulationMpls.class)
                .build();

    }

    private BierGlobal buildBierGlobalBslDiff() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(3)))
                .setBitstringlength(Bsl._256Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.60/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("fe80::/10"))
                .setSubDomain(buildSubDomain())
                .setEncapsulationType(BierEncapsulationMpls.class)
                .build();

    }

    private BierGlobal buildBierGlobalIpv4BfrPrefixDiff() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(3)))
                .setBitstringlength(Bsl._4096Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.61/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("fe80::/10"))
                .setSubDomain(buildSubDomain())
                .setEncapsulationType(BierEncapsulationMpls.class)
                .build();

    }

    private BierGlobal buildBierGlobalIpv6BfrPrefixDiff() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(3)))
                .setBitstringlength(Bsl._4096Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.60/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("ff80::/10"))
                .setSubDomain(buildSubDomain())
                .setEncapsulationType(BierEncapsulationMpls.class)
                .build();

    }

    private BierGlobal buildBierGlobalEncapDiff() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(3)))
                .setBitstringlength(Bsl._4096Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.60/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("fe80::/10"))
                .setSubDomain(buildSubDomain())
                .setEncapsulationType(null)
                .build();

    }

    private BierGlobal buildBierGlobalSubDomainBfrIdDiff() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(3)))
                .setBitstringlength(Bsl._4096Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.60/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("fe80::/10"))
                .setSubDomain(buildSubDomainBfrIdDiff())
                .setEncapsulationType(BierEncapsulationMpls.class)
                .build();

    }

    private BierGlobal buildBierGlobalSubDomainBslDiff() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(3)))
                .setBitstringlength(Bsl._4096Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.60/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("fe80::/10"))
                .setSubDomain(buildSubDomainBslDiff())
                .setEncapsulationType(BierEncapsulationMpls.class)
                .build();

    }

    private BierGlobal buildBierGlobalSubDomainIgpDiff() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(3)))
                .setBitstringlength(Bsl._4096Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.60/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("fe80::/10"))
                .setSubDomain(buildSubDomainIgpTypeDiff())
                .setEncapsulationType(BierEncapsulationMpls.class)
                .build();

    }

    private BierGlobal buildBierGlobalSubdomainSizeDiff() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(3)))
                .setBitstringlength(Bsl._4096Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.60/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("fe80::/10"))
                .setSubDomain(buildSubDomainSizeDiff())
                .setEncapsulationType(BierEncapsulationMpls.class)
                .build();

    }

    private BierGlobal buildBierGlobalIpv4RangeSizeDidff() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(3)))
                .setBitstringlength(Bsl._4096Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.60/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("fe80::/10"))
                .setSubDomain(buildSubDomainIpv4RangeSizeDidff())
                .setEncapsulationType(BierEncapsulationMpls.class)
                .build();

    }

    private BierGlobal buildBierGlobalIpv4SizeDiff() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(3)))
                .setBitstringlength(Bsl._4096Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.60/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("fe80::/10"))
                .setSubDomain(buildSubDomainIpv4SizeDiff())
                .setEncapsulationType(BierEncapsulationMpls.class)
                .build();

    }



    private BierNode buildBierNode() {
        Domain domain = new DomainBuilder().setBierGlobal(buildBierGlobal()).build();
        ArrayList<Domain> domainArrayList = new ArrayList<>();
        domainArrayList.add(domain);
        return new BierNodeBuilder()
                .setBierNodeParams(new BierNodeParamsBuilder()
                        .setDomain(domainArrayList)
                        .build())
                .build();
    }


    @Test
    public void testSetDomainConfigAdd() throws Exception {
        initInstance();
        SetDomainConfigInput input = new SetDomainConfigInputBuilder()
                .setNodeName(NODE_ID)
                .setDomainId(DOMAIN_ID)
                .setBierGlobal(buildBierGlobal())
                .setWriteType(ConfigType.ADD)
                .build();
        when(bierConfigWriter.writeDomain(any(),any(),any())).thenReturn(result);
        testDriverProvider.setDomainConfig(input);
        verify(bierConfigWriter).writeDomain(ConfigurationType.ADD, NODE_ID,
                new DomainBuilder().setBierGlobal(input.getBierGlobal()).build());
    }

    @Test
    public void testSetDomainConfigModify() throws Exception {
        initInstance();
        SetDomainConfigInput input = new SetDomainConfigInputBuilder()
                .setNodeName(NODE_ID)
                .setDomainId(DOMAIN_ID)
                .setBierGlobal(buildBierGlobal())
                .setWriteType(ConfigType.MODIFY)
                .build();
        when(bierConfigWriter.writeDomain(any(),any(),any())).thenReturn(result);
        testDriverProvider.setDomainConfig(input);
        verify(bierConfigWriter).writeDomain(ConfigurationType.MODIFY, NODE_ID,
                new DomainBuilder().setBierGlobal(input.getBierGlobal()).build());
    }

    @Test
    public void testSetDomainConfigDelete() throws Exception {
        initInstance();
        SetDomainConfigInput input = new SetDomainConfigInputBuilder()
                .setNodeName(NODE_ID)
                .setDomainId(DOMAIN_ID)
                .setBierGlobal(buildBierGlobal())
                .setWriteType(ConfigType.DELETE)
                .build();
        when(bierConfigWriter.writeDomain(any(),any(),any())).thenReturn(result);
        testDriverProvider.setDomainConfig(input);
        verify(bierConfigWriter).writeDomain(ConfigurationType.DELETE, NODE_ID,
                new DomainBuilder().setBierGlobal(input.getBierGlobal()).build());
    }

    @Test
    public void testSetSubdomainConfig() throws Exception {
        initInstance();
        SetSubdomainConfigInput input = new SetSubdomainConfigInputBuilder()
                .setNodeName(NODE_ID)
                .setDomainId(DOMAIN_ID)
                .setSubDomainId(SUBDOMAINID)
                .setWriteType(ConfigType.ADD)
                .build();
        when(bierConfigWriter.writeSubdomain(any(),any(),any(),any())).thenReturn(result);
        testDriverProvider.setSubdomainConfig(input);
        verify(bierConfigWriter).writeSubdomain(ConfigurationType.ADD, NODE_ID, new DomainId(DOMAIN_ID),
                new SubDomainBuilder().setSubDomainId(input.getSubDomainId()).build());
    }

    @Test
    public void testSetIpv4Config() throws Exception {
        initInstance();
        SetIpv4ConfigInput input = new SetIpv4ConfigInputBuilder()
                .setNodeName(NODE_ID)
                .setDomainId(DOMAIN_ID)
                .setSubDomainId(SUBDOMAINID.getValue())
                .setWriteType(ConfigType.ADD)
                .setBitstringlength(BSL)
                .setBierMplsLabelBase(MPLS_LABEL_BASE)
                .build();
        when(bierConfigWriter.writeSubdomainIpv4(any(), any(), any(), any(), any())).thenReturn(result);
        testDriverProvider.setIpv4Config(input);
        verify(bierConfigWriter).writeSubdomainIpv4(ConfigurationType.ADD, NODE_ID, new DomainId(DOMAIN_ID),
                new SubDomainId(SUBDOMAINID),
                new Ipv4Builder().setBitstringlength(BSL).setBierMplsLabelBase(MPLS_LABEL_BASE).build());
    }

    @Test
    public void testReadBierGlobal() throws Exception {
        initInstance();
        ReadBierGlobalInput input = new ReadBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        testDriverProvider.readBierGlobal(input);
        verify(bierConfigReader).readBierGlobal(NODE_ID);
    }

    @Test
    public void testCheckBierGlobalSuccess() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        BierGlobal bierGlobal = buildBierGlobal();
        when(bierConfigReader.readBierGlobal(any())).thenReturn(bierGlobal);
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        BierNode bierNode = buildBierNode();
        when(bierNodeOptional.get()).thenReturn(bierNode);

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.SUCCESS,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());

    }

    @Test
    public void testCheckBierGlobalFailedBfrId() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(buildBierGlobalBfrIdDiff());
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(buildBierNode());

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());


    }

    @Test
    public void testCheckBierGlobalFailedBsl() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(buildBierGlobalBslDiff());
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(buildBierNode());

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());

    }

    @Test
    public void testCheckBierGlobalFailedIpv4BfrPrefix() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(buildBierGlobalIpv4BfrPrefixDiff());
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(buildBierNode());

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());


    }

    @Test
    public void testCheckBierGlobalFailedIpv6BfrPrefix() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(buildBierGlobalIpv6BfrPrefixDiff());
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(buildBierNode());

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());


    }

    @Test
    public void testCheckBierGlobalFailedEncapDiff() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(buildBierGlobalEncapDiff());
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(buildBierNode());

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());


    }

    @Test
    public void testCheckBierGlobalFailedSubDomainBfrIdDiff() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(buildBierGlobalSubDomainBfrIdDiff());
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(buildBierNode());

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());


    }

    @Test
    public void testCheckBierGlobalFailedSubDomainBslDiff() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(buildBierGlobalSubDomainBslDiff());
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(buildBierNode());

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());


    }

    @Test
    public void testCheckBierGlobalFailedSubDomainIgpdDiff() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(buildBierGlobalSubDomainIgpDiff());
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(buildBierNode());

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());


    }

    @Test
    public void testCheckBierGlobalFailedSubDomainSizeDiff() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(buildBierGlobalSubdomainSizeDiff());
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(buildBierNode());

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());


    }

    @Test
    public void testCheckBierGlobalFailedIpv4RangeSizeDidff() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(buildBierGlobalIpv4RangeSizeDidff());
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(buildBierNode());

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());


    }


    @Test
    public void testCheckBierGlobalFailedIpv4SizeDidff() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(buildBierGlobalIpv4SizeDiff());
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(buildBierNode());

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());


    }

    @Test
    public void testCheckBierGlobalFailedDeviceNull() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(null);
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(buildBierNode());

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());


    }

    @Test
    public void testCheckBierGlobalFailedControllerNull() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(buildBierGlobal());
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(null);

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());


    }

    @Test
    public void testCheckBierGlobalBothNull() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<BierNode> bierNodeOptional = mock(Optional.class);
        InstanceIdentifier<BierNode> bierNodeIId = IidConstants.BIER_TOPO_IID
                .child(BierNode.class,new BierNodeKey(NODE_ID));

        when(bierConfigReader.readBierGlobal(any())).thenReturn(null);
        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<BierNode>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(),eq(bierNodeIId))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(bierNodeOptional);
        when(bierNodeOptional.isPresent()).thenReturn(true);
        when(bierNodeOptional.get()).thenReturn(null);

        CheckBierGlobalInput input = new CheckBierGlobalInputBuilder().setNodeName(NODE_ID).build();
        assertEquals(ConfigureResult.Result.SUCCESS,
                testDriverProvider.checkBierGlobal(input).get().getResult().getConfigureResult().getResult());


    }


    private Channel buildChannelData() {
        ArrayList<EgressNode> egressNodeArrayList = new ArrayList<EgressNode>();
        egressNodeArrayList.add(new EgressNodeBuilder()
                .setEgressBfrId(new BfrId(EGRESS_BFR1))
                .setNodeId(EGRESS_NODE_ID1)
                .build());
        egressNodeArrayList.add(new EgressNodeBuilder()
                .setEgressBfrId(new BfrId(EGRESS_BFR2))
                .setNodeId(EGRESS_NODE_ID2)
                .build());
        return new ChannelBuilder()
                .setIngressNode(NODE_ID)
                .setSrcIp(SRC_IP)
                .setSourceWildcard(SRC_WILDCARD)
                .setDstGroup(DEST_GROUP)
                .setGroupWildcard(DEST_WILDCARD)
                .setIngressBfrId(new BfrId(INGRESS_BFR1))
                .setEgressNode(egressNodeArrayList)
                .setSubDomainId(SUBDOMAINID)
                .build();

    }

    private Channel buildChannelDataEgressNull() {
        return new ChannelBuilder()
                .setIngressNode(NODE_ID)
                .setSrcIp(SRC_IP)
                .setSourceWildcard(SRC_WILDCARD)
                .setDstGroup(DEST_GROUP)
                .setGroupWildcard(DEST_WILDCARD)
                .setIngressBfrId(new BfrId(INGRESS_BFR1))
                .setSubDomainId(SUBDOMAINID)
                .build();

    }

    @Test
    public void testSetChannel() throws Exception {
        initInstance();

        ArrayList<org.opendaylight.yang.gen.v1.urn
                .bier.test.driver.rev161219
                .set.channel.input
                .EgressNode> egressNodeArrayList = new ArrayList<>();
        egressNodeArrayList.add(
                new org.opendaylight.yang.gen.v1.urn
                .bier.test.driver.rev161219
                .set.channel.input
                .EgressNodeBuilder()
                        .setEgressBfrId(new BfrId(EGRESS_BFR1))
                        .setNodeId(EGRESS_NODE_ID1).build());
        egressNodeArrayList.add(
                new org.opendaylight.yang.gen.v1.urn
                        .bier.test.driver.rev161219
                        .set.channel.input
                        .EgressNodeBuilder()
                        .setEgressBfrId(new BfrId(EGRESS_BFR2))
                        .setNodeId(EGRESS_NODE_ID2).build());
        SetChannelInput input = new SetChannelInputBuilder()
                .setIngressNode(NODE_ID)
                .setIngressBfrId(new BfrId(INGRESS_BFR1))
                .setDstGroup(DEST_GROUP)
                .setSrcIp(SRC_IP)
                .setGroupWildcard(DEST_WILDCARD)
                .setSourceWildcard(SRC_WILDCARD)
                .setEgressNode(egressNodeArrayList)
                .setSubDomainId(SUBDOMAINID)
                .setWriteType(ConfigType.ADD)
                .build();
        when(channelConfigWriter.writeChannel(any(),any())).thenReturn(result);
        testDriverProvider.setChannel(input);
        verify(channelConfigWriter).writeChannel(ConfigurationType.ADD,buildChannelData());
    }

    @Test
    public void testSetEgressNode() throws Exception {
        initInstance();
        SetEgressNodeInput input = new SetEgressNodeInputBuilder()
                .setIngressBfrId(new BfrId(INGRESS_BFR1))
                .setIngressNode(NODE_ID)
                .setDstGroup(DEST_GROUP)
                .setSrcIp(SRC_IP)
                .setGroupWildcard(DEST_WILDCARD)
                .setSourceWildcard(SRC_WILDCARD)
                .setSubDomainId(SUBDOMAINID)
                .setEgressNode(EGRESS_NODE_ID1)
                .setEgressBfrId(new BfrId(EGRESS_BFR1))
                .setWriteType(ConfigType.ADD)
                .build();
        when(channelConfigWriter.writeChannelEgressNode(any(), any())).thenReturn(result);
        testDriverProvider.setEgressNode(input);
        verify(channelConfigWriter).writeChannelEgressNode(ConfigurationType.ADD, new ChannelBuilder()
                .setDstGroup(input.getDstGroup())
                .setEgressNode(Collections.singletonList(
                        new EgressNodeBuilder()
                                .setEgressBfrId(input.getEgressBfrId())
                                .setNodeId(input.getEgressNode())
                                .build()))
                .setGroupWildcard(input.getGroupWildcard())
                .setIngressBfrId(input.getIngressBfrId())
                .setIngressNode(input.getIngressNode())
                .setSourceWildcard(input.getSourceWildcard())
                .setSrcIp(input.getSrcIp())
                .setSubDomainId(input.getSubDomainId()).build());

    }

    @Test
    public void testCheckChannelSuccess() throws Exception {
        initInstance();

        Channel channel = buildChannelData();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<Channel> channelOptional = mock(Optional.class);

        InstanceIdentifier<Channel> multicastInfoIid =
                IidConstants.BIER_CHANNEL_IID.child(Channel.class, new ChannelKey(CHANNEL_NAME));
        List<BfrId> bfrIdArrayList = Lists.transform(channel.getEgressNode(),
                new Function<EgressNode, BfrId>() {
                    @java.lang.Override
                    public BfrId apply(EgressNode input) {
                        return input.getEgressBfrId();
                    }
                });

        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<Channel>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(), eq(multicastInfoIid))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(channelOptional);
        when(channelOptional.isPresent()).thenReturn(true);
        when(channelOptional.get()).thenReturn(channel);
        when(channelConfigReader.readChannel(any())).thenReturn(bfrIdArrayList);

        CheckChannelInput input = new CheckChannelInputBuilder().setChannelName(CHANNEL_NAME).build();
        assertEquals(ConfigureResult.Result.SUCCESS,
                testDriverProvider.checkChannel(input).get().getResult().getConfigureResult().getResult());

    }

    @Test
    public void testCheckChannelFailedDeviceNull() throws Exception {
        initInstance();

        Channel channel = buildChannelData();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<Channel> channelOptional = mock(Optional.class);

        InstanceIdentifier<Channel> multicastInfoIid =
                IidConstants.BIER_CHANNEL_IID.child(Channel.class, new ChannelKey(CHANNEL_NAME));
        List<BfrId> bfrIdArrayList = Lists.transform(channel.getEgressNode(),
                new Function<EgressNode, BfrId>() {
                    @java.lang.Override
                    public BfrId apply(EgressNode input) {
                        return input.getEgressBfrId();
                    }
                });

        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<Channel>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(), eq(multicastInfoIid))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(channelOptional);
        when(channelOptional.isPresent()).thenReturn(true);
        when(channelOptional.get()).thenReturn(channel);
        when(channelConfigReader.readChannel(any())).thenReturn(null);

        CheckChannelInput input = new CheckChannelInputBuilder().setChannelName(CHANNEL_NAME).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkChannel(input).get().getResult().getConfigureResult().getResult());

    }

    @Test
    public void testCheckChannelFailedControllerNull() throws Exception {
        initInstance();

        Channel channel = buildChannelDataEgressNull();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);
        Optional<Channel> channelOptional = mock(Optional.class);

        InstanceIdentifier<Channel> multicastInfoIid =
                IidConstants.BIER_CHANNEL_IID.child(Channel.class, new ChannelKey(CHANNEL_NAME));
        List<BfrId> bfrIdArrayList = Lists.transform(buildChannelData().getEgressNode(),
                new Function<EgressNode, BfrId>() {
                    @java.lang.Override
                    public BfrId apply(EgressNode input) {
                        return input.getEgressBfrId();
                    }
                });

        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<Channel>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(), eq(multicastInfoIid))).thenReturn(readResult);
        when(readResult.checkedGet()).thenReturn(channelOptional);
        when(channelOptional.isPresent()).thenReturn(true);
        when(channelOptional.get()).thenReturn(channel);
        when(channelConfigReader.readChannel(any())).thenReturn(bfrIdArrayList);

        CheckChannelInput input = new CheckChannelInputBuilder().setChannelName(CHANNEL_NAME).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkChannel(input).get().getResult().getConfigureResult().getResult());

    }

    @Test
    public void testCheckChannelFailedBfrDiff() throws Exception {
        initInstance();

        ReadOnlyTransaction transaction = mock(ReadOnlyTransaction.class);


        InstanceIdentifier<Channel> multicastInfoIid =
                IidConstants.BIER_CHANNEL_IID.child(Channel.class, new ChannelKey(CHANNEL_NAME));
        List<BfrId> bfrIdArrayList = new ArrayList<>();
        bfrIdArrayList.add(new BfrId(EGRESS_BFR1));

        when(dataBroker.newReadOnlyTransaction()).thenReturn(transaction);
        CheckedFuture<Optional<Channel>, ReadFailedException> readResult = mock(CheckedFuture.class);
        when(transaction.read(any(), eq(multicastInfoIid))).thenReturn(readResult);
        Optional<Channel> channelOptional = mock(Optional.class);
        when(readResult.checkedGet()).thenReturn(channelOptional);
        when(channelOptional.isPresent()).thenReturn(true);
        Channel channel = buildChannelData();
        when(channelOptional.get()).thenReturn(channel);
        when(channelConfigReader.readChannel(any())).thenReturn(bfrIdArrayList);

        CheckChannelInput input = new CheckChannelInputBuilder().setChannelName(CHANNEL_NAME).build();
        assertEquals(ConfigureResult.Result.FAILURE,
                testDriverProvider.checkChannel(input).get().getResult().getConfigureResult().getResult());

    }

}
