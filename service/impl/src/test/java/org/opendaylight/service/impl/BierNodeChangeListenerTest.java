/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.service.impl;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParams;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierNodeParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierEncapsulation;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierMplsLabelRangeSize;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.IgpType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.MtId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobal;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobalBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.AfBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;

public class BierNodeChangeListenerTest {


    private BierConfigWriterMock bierConfigWriterMock;
    private BierNodeChangeListener bierNodeChangeListener;


    public void setUp() {
        bierConfigWriterMock = new BierConfigWriterMock();
        bierNodeChangeListener = new BierNodeChangeListener(bierConfigWriterMock);
    }


    @Test
    public void testAddedDomainChangeListener1() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainList = new ArrayList<>();
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));

        Domain domain = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));

        List<Domain> domainListAfter = new ArrayList<>();
        domainListAfter.add(domain);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(null));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertAddedDomainTest1(bierConfigWriterMock.getDomainProcessList());
    }

    @Test
    public void testAddedDomainChangeListener2() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainList = new ArrayList<>();
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        Domain domain2 = constructDomain(0002, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        List<Domain> domainListBefore = new ArrayList<>();
        List<Domain> domainListAfter = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListAfter.add(domain1);
        domainListAfter.add(domain2);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertAddedDomainTest2(bierConfigWriterMock.getDomainProcessList());
    }

    @Test
    public void testDeletedDomainChangeListener1() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainList = new ArrayList<>();
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));

        Domain domain = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));

        List<Domain> domainListBefore = new ArrayList<>();
        domainListBefore.add(domain);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(null));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertDeletedDomainTest1(bierConfigWriterMock.getDomainProcessList());
    }

    @Test
    public void testDeletedDomainChangeListener2() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainList = new ArrayList<>();
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        Domain domain2 = constructDomain(0002, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));

        List<Domain> domainListBefore = new ArrayList<>();
        List<Domain> domainListAfter = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListBefore.add(domain2);
        domainListAfter.add(domain2);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertDeletedDomainTest2(bierConfigWriterMock.getDomainProcessList());

    }

    @Test
    public void testDeletedDomainChangeListener3() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainList = new ArrayList<>();
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));

        Domain domain = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));

        List<Domain> domainListBefore = new ArrayList<>();
        domainListBefore.add(domain);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));

        bierNodeChangeListener.processDeletedNode(bierNodeBefore);

        assertDeletedDomainTest3(bierConfigWriterMock.getDomainProcessList());
    }

    @Test
    public void testModifiedParametersOfDomainChangeListener() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainList = new ArrayList<>();
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        Domain domain3 = constructDomain(0002, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.42/22", "fe80::7009:fe25:8170:36af/64", subDomainList));
        List<Domain> domainListBefore = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListBefore.add(domain3);

        Domain domain2 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        Domain domain4 = constructDomain(0002, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.42/22", "fe80::7009:fe25:8170:36af/63", subDomainList));
        List<Domain> domainListAfter = new ArrayList<>();
        domainListAfter.add(domain2);
        domainListAfter.add(domain4);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertModifiedDomainTest(bierConfigWriterMock.getDomainProcessList());

    }

    @Test
    public void testModifiedParametersOfDomainAndDeletedSubDomainChangeListener() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainListBefore = new ArrayList<>();
        List<SubDomain> subDomainListAfter = new ArrayList<>();
        subDomainListBefore.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));
        subDomainListBefore.add(constructSubDomain(0002, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));
        subDomainListAfter.add(constructSubDomain(0002, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListBefore));
        Domain domain2 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListAfter));

        List<Domain> domainListBefore = new ArrayList<>();
        List<Domain> domainListAfter = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListAfter.add(domain2);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertModifiedParametersOfDomainAndDeletedSubDomainTest(bierConfigWriterMock.getDomainProcessList(),
                bierConfigWriterMock.getSubDomainProcessList());
    }

    @Test
    public void testAddedSubDomainChangeListener1() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainListAfter = new ArrayList<>();
        subDomainListAfter.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", null));
        Domain domain2 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListAfter));

        List<Domain> domainListBefore = new ArrayList<>();
        List<Domain> domainListAfter = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListAfter.add(domain2);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertAddedSubDomainTest1(bierConfigWriterMock.getSubDomainProcessList());
    }

    @Test
    public void testAddedSubDomainChangeListener2() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainListBefore = new ArrayList<>();
        List<SubDomain> subDomainListAfter = new ArrayList<>();
        subDomainListBefore.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));
        subDomainListAfter.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));
        subDomainListAfter.add(constructSubDomain(0002, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListBefore));
        Domain domain2 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListAfter));

        List<Domain> domainListBefore = new ArrayList<>();
        List<Domain> domainListAfter = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListAfter.add(domain2);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertAddedSubDomainTest2(bierConfigWriterMock.getSubDomainProcessList());
    }

    @Test
    public void testOnlyDeletedSubDomainChangeListener1() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainListBefore = new ArrayList<>();
        subDomainListBefore.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListBefore));
        Domain domain2 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", null));

        List<Domain> domainListBefore = new ArrayList<>();
        List<Domain> domainListAfter = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListAfter.add(domain2);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertDeletedSubDomainTest1(bierConfigWriterMock.getSubDomainProcessList());
    }

    @Test
    public void testOnlyDeletedSubDomainChangeListener2() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainListBefore = new ArrayList<>();
        List<SubDomain> subDomainListAfter = new ArrayList<>();
        subDomainListBefore.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));
        subDomainListBefore.add(constructSubDomain(0002, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));
        subDomainListAfter.add(constructSubDomain(0002, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListBefore));
        Domain domain2 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListAfter));

        List<Domain> domainListBefore = new ArrayList<>();
        List<Domain> domainListAfter = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListAfter.add(domain2);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertDeletedSubDomainTest2(bierConfigWriterMock.getSubDomainProcessList());
    }

    @Test
    public void testModifiedParametersOfSubDomainChangeListener() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainListBefore = new ArrayList<>();
        List<SubDomain> subDomainListAfter = new ArrayList<>();
        subDomainListBefore.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6List));
        subDomainListAfter.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._256Bit, ipv4List, ipv6List));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListBefore));
        Domain domain2 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListAfter));

        List<Domain> domainListBefore = new ArrayList<>();
        List<Domain> domainListAfter = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListAfter.add(domain2);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertModifiedParametersOfSubDomainTest(bierConfigWriterMock.getSubDomainProcessList());
    }

    @Test
    public void testModifiedParametersOfSubDomainAndDeletedIpv4ChangeListener() {
        setUp();

        List<Ipv4> ipv4ListBefore = new ArrayList<>();
        List<Ipv4> ipv4ListAfter = new ArrayList<>();
        ipv4ListBefore.add(constructIpv4(64, 101L, (short)30));
        ipv4ListBefore.add(constructIpv4(80, 110L, (short)45));
        ipv4ListAfter.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainListBefore = new ArrayList<>();
        List<SubDomain> subDomainListAfter = new ArrayList<>();
        subDomainListBefore.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4ListBefore, ipv6List));
        subDomainListAfter.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._256Bit, ipv4ListAfter, ipv6List));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListBefore));
        Domain domain2 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListAfter));

        List<Domain> domainListBefore = new ArrayList<>();
        List<Domain> domainListAfter = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListAfter.add(domain2);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertModifiedParametersOfSubDomainAndDeletedIpv4Test(bierConfigWriterMock.getSubDomainProcessList(),
                bierConfigWriterMock.getIpv4ProcessList());
    }

    @Test
    public void testModifiedParametersOfSubDomainAndDeletedIpv6ChangeListener() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6ListBefore = new ArrayList<>();
        List<Ipv6> ipv6ListAfter = new ArrayList<>();
        ipv6ListBefore.add(constructIpv6(128, 98L, (short)48));
        ipv6ListBefore.add(constructIpv6(256, 120L, (short)56));
        ipv6ListAfter.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainListBefore = new ArrayList<>();
        List<SubDomain> subDomainListAfter = new ArrayList<>();
        subDomainListBefore.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6ListBefore));
        subDomainListAfter.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._256Bit, ipv4List, ipv6ListAfter));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListBefore));
        Domain domain2 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListAfter));

        List<Domain> domainListBefore = new ArrayList<>();
        List<Domain> domainListAfter = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListAfter.add(domain2);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertModifiedParametersOfSubDomainAndDeletedIpv6Test(bierConfigWriterMock.getSubDomainProcessList(),
                bierConfigWriterMock.getIpv6ProcessList());
    }

    @Test
    public void testModifiedParametersOfSubDomainAndDeletedIpv4Ipv6ChangeListener() {
        setUp();

        List<Ipv4> ipv4ListBefore = new ArrayList<>();
        ipv4ListBefore.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6ListBefore = new ArrayList<>();
        ipv6ListBefore.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainListBefore = new ArrayList<>();
        List<SubDomain> subDomainListAfter = new ArrayList<>();
        subDomainListBefore.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4ListBefore,
                ipv6ListBefore));
        subDomainListAfter.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._256Bit, null, null));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListBefore));
        Domain domain2 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListAfter));

        List<Domain> domainListBefore = new ArrayList<>();
        List<Domain> domainListAfter = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListAfter.add(domain2);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertModifiedParametersOfSubDomainAndDeletedIpv4Ipv6Test(bierConfigWriterMock.getSubDomainProcessList(),
                bierConfigWriterMock.getIpv4ProcessList(), bierConfigWriterMock.getIpv6ProcessList());
    }

    @Test
    public void testOnlyDeletedIpv4ChangeListener() {
        setUp();

        List<Ipv4> ipv4ListBefore = new ArrayList<>();
        List<Ipv4> ipv4ListAfter = new ArrayList<>();
        ipv4ListBefore.add(constructIpv4(64, 101L, (short)30));
        ipv4ListBefore.add(constructIpv4(80, 110L, (short)45));
        ipv4ListAfter.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainListBefore = new ArrayList<>();
        List<SubDomain> subDomainListAfter = new ArrayList<>();
        subDomainListBefore.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4ListBefore, ipv6List));
        subDomainListAfter.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4ListAfter, ipv6List));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListBefore));
        Domain domain2 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListAfter));

        List<Domain> domainListBefore = new ArrayList<>();
        List<Domain> domainListAfter = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListAfter.add(domain2);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertDeleteDSubDomainIpv4(bierConfigWriterMock.getIpv4ProcessList());
    }

    @Test
    public void testOnlyDeletedIpv6ChangeListener() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6ListBefore = new ArrayList<>();
        List<Ipv6> ipv6ListAfter = new ArrayList<>();
        ipv6ListBefore.add(constructIpv6(128, 98L, (short)48));
        ipv6ListBefore.add(constructIpv6(256, 120L, (short)56));
        ipv6ListAfter.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainListBefore = new ArrayList<>();
        List<SubDomain> subDomainListAfter = new ArrayList<>();
        subDomainListBefore.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6ListBefore));
        subDomainListAfter.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit, ipv4List, ipv6ListAfter));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListBefore));
        Domain domain2 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainListAfter));

        List<Domain> domainListBefore = new ArrayList<>();
        List<Domain> domainListAfter = new ArrayList<>();
        domainListBefore.add(domain1);
        domainListAfter.add(domain2);

        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListBefore));
        BierNode bierNodeAfter = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter));

        bierNodeChangeListener.processModifiedNode(bierNodeBefore, bierNodeAfter);

        assertDeleteDSubDomainIpv6(bierConfigWriterMock.getIpv6ProcessList());
    }


    private static class BierConfigWriterMock implements BierConfigWriter {

        private List<Domain> domainProcessList = new ArrayList<>();
        private List<SubDomain> subDomainProcessList = new ArrayList<>();
        private List<Ipv4> ipv4ProcessList = new ArrayList<>();
        private List<Ipv6> ipv6ProcessList = new ArrayList<>();

        @Override
        public ConfigurationResult writeDomain(ConfigurationType type, String nodeId, Domain domain) {
            switch (type) {
                case ADD:
                    if (nodeId.equals("0001")) {
                        domainProcessList.add(domain);
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                case MODIFY:
                    if (nodeId.equals("0001")) {
                        domainProcessList.add(domain);
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                case DELETE:
                    if (nodeId.equals("0001")) {
                        domainProcessList.add(domain);
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }

        @Override
        public ConfigurationResult writeSubdomain(ConfigurationType type, String nodeId, DomainId domainId,
                                               SubDomain subDomain) {
            switch (type) {
                case ADD:
                    if (nodeId.equals("0001") && domainId.equals(new DomainId(0001))) {
                        subDomainProcessList.add(subDomain);
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                case MODIFY:
                    if (nodeId.equals("0001") && domainId.equals(new DomainId(0001))) {
                        subDomainProcessList.add(subDomain);
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                case DELETE:
                    if (nodeId.equals("0001") && domainId.equals(new DomainId(0001))) {
                        subDomainProcessList.add(subDomain);
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }

        @Override
        public ConfigurationResult writeSubdomainIpv4(ConfigurationType type, String nodeId, DomainId domainId,
                                                   SubDomainId subDomainId, Ipv4 ipv4) {
            switch (type) {
                case ADD:
                    break;
                case MODIFY:
                    break;
                case DELETE:
                    if (nodeId.equals("0001") && domainId.equals(new DomainId(0001))
                            && subDomainId.equals(new SubDomainId(0001))) {
                        ipv4ProcessList.add(ipv4);
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }

        @Override
        public ConfigurationResult writeSubdomainIpv6(ConfigurationType type, String nodeId, DomainId domainId,
                                                   SubDomainId subDomainId, Ipv6 ipv6) {
            switch (type) {
                case ADD:
                    break;
                case MODIFY:
                    break;
                case DELETE:
                    if (nodeId.equals("0001") && domainId.equals(new DomainId(0001))
                            && subDomainId.equals(new SubDomainId(0001))) {
                        ipv6ProcessList.add(ipv6);
                    } else {
                        return new ConfigurationResult(ConfigurationResult.Result.FAILED);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Type is not matched");
            }
            return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        }

        private List<Domain> getDomainProcessList() {
            return domainProcessList;
        }

        private List<SubDomain> getSubDomainProcessList() {
            return subDomainProcessList;
        }

        private List<Ipv4> getIpv4ProcessList() {
            return ipv4ProcessList;
        }

        private List<Ipv6> getIpv6ProcessList() {
            return ipv6ProcessList;
        }
    }


    private Ipv4 constructIpv4(Integer bitStringLength, Long mplsLabel, short value) {
        Ipv4Builder ipv4Builder = new Ipv4Builder();
        ipv4Builder.setBitstringlength(bitStringLength);
        ipv4Builder.setBierMplsLabelBase(new MplsLabel(mplsLabel));
        ipv4Builder.setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(value));
        return ipv4Builder.build();
    }

    private Ipv6 constructIpv6(Integer bitStringLength, Long mplsLabel, short value) {
        Ipv6Builder ipv6Builder = new Ipv6Builder();
        ipv6Builder.setBitstringlength(bitStringLength);
        ipv6Builder.setBierMplsLabelBase(new MplsLabel(mplsLabel));
        ipv6Builder.setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(value));
        return ipv6Builder.build();
    }


    private SubDomain constructSubDomain(Integer subDomainId, IgpType igpType, Integer mtId,
                                         Integer bfrId, Bsl bsl, List<Ipv4> ipv4List,
                                         List<Ipv6> ipv6List) {
        SubDomainBuilder subDomainBuilder = new SubDomainBuilder();
        subDomainBuilder.setSubDomainId(new SubDomainId(subDomainId));
        subDomainBuilder.setIgpType(igpType);
        subDomainBuilder.setMtId(new MtId(mtId));
        subDomainBuilder.setBfrId(new BfrId(bfrId));
        subDomainBuilder.setBitstringlength(bsl);

        AfBuilder afBuilder = new AfBuilder();
        if (null != ipv4List && !ipv4List.isEmpty()) {
            afBuilder.setIpv4(ipv4List);
        }
        if (null != ipv6List && !ipv6List.isEmpty()) {
            afBuilder.setIpv6(ipv6List);
        }
        subDomainBuilder.setAf(afBuilder.build());
        return subDomainBuilder.build();
    }

    private BierGlobal constructBierGlobal(java.lang.Class<BierEncapsulation> value, Bsl bsl,
                                           Integer bfrId, String ipv4Prefix, String ipv6Prefix,
                                           List<SubDomain> subDomainList) {
        BierGlobalBuilder bierGlobalBuilder = new BierGlobalBuilder();
        bierGlobalBuilder.setEncapsulationType(value);
        bierGlobalBuilder.setBitstringlength(bsl);
        bierGlobalBuilder.setBfrId(new BfrId(bfrId));
        bierGlobalBuilder.setIpv4BfrPrefix(new Ipv4Prefix(ipv4Prefix));
        bierGlobalBuilder.setIpv6BfrPrefix(new Ipv6Prefix(ipv6Prefix));
        if (null != subDomainList && !subDomainList.isEmpty()) {
            bierGlobalBuilder.setSubDomain(subDomainList);
        }
        return bierGlobalBuilder.build();
    }

    private Domain constructDomain(Integer domainId, BierGlobal bierGlobal) {
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(domainId));
        domainBuilder.setBierGlobal(bierGlobal);
        return domainBuilder.build();
    }

    private BierNodeParams constructBierNodeParams(List<Domain> domainList) {
        BierNodeParamsBuilder bierNodeParamsBuilder = new BierNodeParamsBuilder();
        bierNodeParamsBuilder.setDomain(domainList);
        return bierNodeParamsBuilder.build();
    }

    private List<BierTerminationPoint> constructBierTerminationPointList(String tpId) {
        BierTerminationPointBuilder bierTerminationPointBuilder = new BierTerminationPointBuilder();
        List<BierTerminationPoint> bierTerminationPointList = new ArrayList<>();
        bierTerminationPointBuilder.setTpId(tpId);
        bierTerminationPointList.add(bierTerminationPointBuilder.build());
        return bierTerminationPointList;
    }

    private BierNode constructBierNode(String nodeId, String name, String routerId, int latitude,
                                       int longitude, List<BierTerminationPoint> bierTerminationPointList,
                                       BierNodeParams bierNodeParams) {
        BierNodeBuilder bierNodeBuilder = new BierNodeBuilder();
        bierNodeBuilder.setNodeId(nodeId);
        bierNodeBuilder.setName(name);
        bierNodeBuilder.setRouterId(routerId);
        bierNodeBuilder.setLatitude(BigInteger.valueOf(latitude));
        bierNodeBuilder.setLongitude(BigInteger.valueOf(longitude));
        bierNodeBuilder.setBierTerminationPoint(bierTerminationPointList);
        bierNodeBuilder.setBierNodeParams(bierNodeParams);
        return bierNodeBuilder.build();
    }

    private void assertAddedDomainTest1(List<Domain> domainList) {
        assertAddedOrDeletedDomainData(domainList);
    }

    private void assertAddedDomainTest2(List<Domain> domainList) {
        BierGlobal bierGlobal = domainList.get(0).getBierGlobal();
        SubDomain subDomain = bierGlobal.getSubDomain().get(0);
        Ipv4 ipv4 = subDomain.getAf().getIpv4().get(0);
        Ipv6 ipv6 = subDomain.getAf().getIpv6().get(0);
        Assert.assertEquals(domainList.size(), 1);
        Assert.assertEquals(domainList.get(0).getDomainId(), new DomainId(0002));
        Assert.assertEquals(bierGlobal.getEncapsulationType(), BierEncapsulation.class);
        Assert.assertEquals(bierGlobal.getBitstringlength(), Bsl._512Bit);
        Assert.assertEquals(bierGlobal.getBfrId(), new BfrId(10));
        Assert.assertEquals(bierGlobal.getIpv4BfrPrefix(), new Ipv4Prefix("102.112.20.40/24"));
        Assert.assertEquals(bierGlobal.getIpv6BfrPrefix(), new Ipv6Prefix("fe80::7009:fe25:8170:36af/64"));
        Assert.assertEquals(bierGlobal.getSubDomain().size(), 1);
        Assert.assertEquals(subDomain.getSubDomainId(), new SubDomainId(0001));
        Assert.assertEquals(subDomain.getBitstringlength(), Bsl._512Bit);
        Assert.assertEquals(subDomain.getIgpType(), IgpType.ISIS);
        Assert.assertEquals(subDomain.getBfrId(), new BfrId(13));
        Assert.assertEquals(subDomain.getMtId(), new MtId(12));
        Assert.assertEquals(subDomain.getAf().getIpv4().size(), 1);
        Assert.assertEquals(subDomain.getAf().getIpv6().size(), 1);
        Assert.assertEquals(ipv4.getBitstringlength(), new Integer(64));
        Assert.assertEquals(ipv4.getBierMplsLabelBase(), new MplsLabel(101L));
        Assert.assertEquals(ipv4.getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize(Short.valueOf("30")));
        Assert.assertEquals(ipv6.getBitstringlength(), new Integer(128));
        Assert.assertEquals(ipv6.getBierMplsLabelBase(), new MplsLabel(98L));
        Assert.assertEquals(ipv6.getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize(Short.valueOf("48")));
    }

    private void assertDeletedDomainTest1(List<Domain> domainList) {
        assertAddedOrDeletedDomainData(domainList);
    }

    private void assertDeletedDomainTest2(List<Domain> domainList) {
        assertAddedOrDeletedDomainData(domainList);
    }

    private void assertDeletedDomainTest3(List<Domain> domainList) {
        assertAddedOrDeletedDomainData(domainList);
    }

    private void assertAddedOrDeletedDomainData(List<Domain> domainList) {
        BierGlobal bierGlobal = domainList.get(0).getBierGlobal();
        SubDomain subDomain = bierGlobal.getSubDomain().get(0);
        Ipv4 ipv4 = subDomain.getAf().getIpv4().get(0);
        Ipv6 ipv6 = subDomain.getAf().getIpv6().get(0);
        Assert.assertEquals(domainList.size(), 1);
        Assert.assertEquals(domainList.get(0).getDomainId(), new DomainId(0001));
        Assert.assertEquals(bierGlobal.getEncapsulationType(), BierEncapsulation.class);
        Assert.assertEquals(bierGlobal.getBitstringlength(), Bsl._512Bit);
        Assert.assertEquals(bierGlobal.getBfrId(), new BfrId(10));
        Assert.assertEquals(bierGlobal.getIpv4BfrPrefix(), new Ipv4Prefix("102.112.20.40/24"));
        Assert.assertEquals(bierGlobal.getIpv6BfrPrefix(), new Ipv6Prefix("fe80::7009:fe25:8170:36af/64"));
        Assert.assertEquals(bierGlobal.getSubDomain().size(), 1);
        Assert.assertEquals(subDomain.getSubDomainId(), new SubDomainId(0001));
        Assert.assertEquals(subDomain.getBitstringlength(), Bsl._512Bit);
        Assert.assertEquals(subDomain.getIgpType(), IgpType.ISIS);
        Assert.assertEquals(subDomain.getBfrId(), new BfrId(13));
        Assert.assertEquals(subDomain.getMtId(), new MtId(12));
        Assert.assertEquals(subDomain.getAf().getIpv4().size(), 1);
        Assert.assertEquals(subDomain.getAf().getIpv6().size(), 1);
        Assert.assertEquals(ipv4.getBitstringlength(), new Integer(64));
        Assert.assertEquals(ipv4.getBierMplsLabelBase(), new MplsLabel(101L));
        Assert.assertEquals(ipv4.getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize(Short.valueOf("30")));
        Assert.assertEquals(ipv6.getBitstringlength(), new Integer(128));
        Assert.assertEquals(ipv6.getBierMplsLabelBase(), new MplsLabel(98L));
        Assert.assertEquals(ipv6.getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize(Short.valueOf("48")));
    }

    private void assertModifiedDomainTest(List<Domain> domainList) {
        BierGlobal bierGlobal1 = domainList.get(0).getBierGlobal();
        BierGlobal bierGlobal2 = domainList.get(1).getBierGlobal();
        Assert.assertEquals(domainList.size(), 2);
        Assert.assertEquals(domainList.get(0).getDomainId(), new DomainId(0001));
        Assert.assertEquals(domainList.get(1).getDomainId(), new DomainId(0002));
        Assert.assertEquals(bierGlobal1.getBitstringlength(), Bsl._128Bit);
        Assert.assertEquals(bierGlobal2.getIpv6BfrPrefix(), new Ipv6Prefix("fe80::7009:fe25:8170:36af/63"));
    }

    private void assertAddedSubDomainTest1(List<SubDomain> subDomainList) {
        assertAddedOrDeletedSubDomainData(subDomainList);
    }

    private void assertAddedSubDomainTest2(List<SubDomain> subDomainList) {
        SubDomain subDomain = subDomainList.get(0);
        Ipv4 ipv4 = subDomain.getAf().getIpv4().get(0);
        Ipv6 ipv6 = subDomain.getAf().getIpv6().get(0);
        Assert.assertEquals(subDomainList.size(), 1);
        Assert.assertEquals(subDomain.getSubDomainId(), new SubDomainId(0002));
        Assert.assertEquals(subDomain.getBitstringlength(), Bsl._512Bit);
        Assert.assertEquals(subDomain.getBfrId(), new BfrId(13));
        Assert.assertEquals(subDomain.getMtId(), new MtId(12));
        Assert.assertEquals(subDomain.getIgpType(), IgpType.ISIS);
        Assert.assertEquals(subDomain.getAf().getIpv4().size(), 1);
        Assert.assertEquals(subDomain.getAf().getIpv6().size(), 1);
        Assert.assertEquals(ipv4.getBitstringlength(), new Integer(64));
        Assert.assertEquals(ipv4.getBierMplsLabelBase(), new MplsLabel(101L));
        Assert.assertEquals(ipv4.getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize(Short.valueOf("30")));
        Assert.assertEquals(ipv6.getBitstringlength(), new Integer(128));
        Assert.assertEquals(ipv6.getBierMplsLabelBase(), new MplsLabel(98L));
        Assert.assertEquals(ipv6.getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize(Short.valueOf("48")));
    }

    private void assertDeletedSubDomainTest1(List<SubDomain> subDomainList) {
        assertAddedOrDeletedSubDomainData(subDomainList);
    }

    private void assertDeletedSubDomainTest2(List<SubDomain> subDomainList) {
        assertAddedOrDeletedSubDomainData(subDomainList);
    }

    private void assertAddedOrDeletedSubDomainData(List<SubDomain> subDomainList) {
        SubDomain subDomain = subDomainList.get(0);
        Ipv4 ipv4 = subDomain.getAf().getIpv4().get(0);
        Ipv6 ipv6 = subDomain.getAf().getIpv6().get(0);
        Assert.assertEquals(subDomainList.size(), 1);
        Assert.assertEquals(subDomain.getSubDomainId(), new SubDomainId(0001));
        Assert.assertEquals(subDomain.getBitstringlength(), Bsl._512Bit);
        Assert.assertEquals(subDomain.getBfrId(), new BfrId(13));
        Assert.assertEquals(subDomain.getMtId(), new MtId(12));
        Assert.assertEquals(subDomain.getIgpType(), IgpType.ISIS);
        Assert.assertEquals(subDomain.getAf().getIpv4().size(), 1);
        Assert.assertEquals(subDomain.getAf().getIpv6().size(), 1);
        Assert.assertEquals(ipv4.getBitstringlength(), new Integer(64));
        Assert.assertEquals(ipv4.getBierMplsLabelBase(), new MplsLabel(101L));
        Assert.assertEquals(ipv4.getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize(Short.valueOf("30")));
        Assert.assertEquals(ipv6.getBitstringlength(), new Integer(128));
        Assert.assertEquals(ipv6.getBierMplsLabelBase(), new MplsLabel(98L));
        Assert.assertEquals(ipv6.getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize(Short.valueOf("48")));
    }

    private void assertModifiedParametersOfDomainAndDeletedSubDomainTest(List<Domain> domainList,
                                                                     List<SubDomain> subDomainList) {
        SubDomain subDomain = subDomainList.get(0);
        Ipv4 ipv4 = subDomain.getAf().getIpv4().get(0);
        Ipv6 ipv6 = subDomain.getAf().getIpv6().get(0);
        Assert.assertEquals(domainList.size(), 1);
        Assert.assertEquals(domainList.get(0).getDomainId(), new DomainId(0001));
        Assert.assertEquals(domainList.get(0).getBierGlobal().getBitstringlength(), Bsl._128Bit);
        Assert.assertEquals(subDomainList.size(), 1);
        Assert.assertEquals(subDomain.getSubDomainId(), new SubDomainId(0001));
        Assert.assertEquals(subDomain.getBitstringlength(), Bsl._512Bit);
        Assert.assertEquals(subDomain.getBfrId(), new BfrId(13));
        Assert.assertEquals(subDomain.getMtId(), new MtId(12));
        Assert.assertEquals(subDomain.getIgpType(), IgpType.ISIS);
        Assert.assertEquals(subDomain.getAf().getIpv4().size(), 1);
        Assert.assertEquals(subDomain.getAf().getIpv6().size(), 1);
        Assert.assertEquals(ipv4.getBitstringlength(), new Integer(64));
        Assert.assertEquals(ipv4.getBierMplsLabelBase(), new MplsLabel(101L));
        Assert.assertEquals(ipv4.getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize(Short.valueOf("30")));
        Assert.assertEquals(ipv6.getBitstringlength(), new Integer(128));
        Assert.assertEquals(ipv6.getBierMplsLabelBase(), new MplsLabel(98L));
        Assert.assertEquals(ipv6.getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize(Short.valueOf("48")));

    }

    private void assertModifiedParametersOfSubDomainTest(List<SubDomain> subDomainList) {
        Assert.assertEquals(subDomainList.size(), 1);
        Assert.assertEquals(subDomainList.get(0).getSubDomainId(), new SubDomainId(0001));
        Assert.assertEquals(subDomainList.get(0).getBitstringlength(), Bsl._256Bit);
    }

    private void assertDeleteDSubDomainIpv4(List<Ipv4> ipv4List) {
        Assert.assertEquals(ipv4List.size(), 1);
        Assert.assertEquals(ipv4List.get(0).getBitstringlength(), new Integer(80));
        Assert.assertEquals(ipv4List.get(0).getBierMplsLabelBase(), new MplsLabel(110L));
        Assert.assertEquals(ipv4List.get(0).getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize((short)45));
    }

    private void assertDeleteDSubDomainIpv6(List<Ipv6> ipv6List) {
        Assert.assertEquals(ipv6List.size(), 1);
        Assert.assertEquals(ipv6List.get(0).getBitstringlength(), new Integer(256));
        Assert.assertEquals(ipv6List.get(0).getBierMplsLabelBase(), new MplsLabel(120L));
        Assert.assertEquals(ipv6List.get(0).getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize((short)56));
    }

    private void assertModifiedParametersOfSubDomainAndDeletedIpv4Test(List<SubDomain> subDomainList,
                                                                       List<Ipv4> ipv4List) {
        Assert.assertEquals(subDomainList.size(), 1);
        Assert.assertEquals(subDomainList.get(0).getSubDomainId(),new SubDomainId(0001));
        Assert.assertEquals(subDomainList.get(0).getBitstringlength(), Bsl._256Bit);
        Assert.assertEquals(ipv4List.size(), 1);
        Assert.assertEquals(ipv4List.get(0).getBitstringlength(), new Integer(80));
        Assert.assertEquals(ipv4List.get(0).getBierMplsLabelBase(), new MplsLabel(110L));
        Assert.assertEquals(ipv4List.get(0).getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize((short)45));
    }

    private void assertModifiedParametersOfSubDomainAndDeletedIpv6Test(List<SubDomain> subDomainList,
                                                                       List<Ipv6> ipv6List) {
        Assert.assertEquals(subDomainList.size(), 1);
        Assert.assertEquals(subDomainList.get(0).getSubDomainId(),new SubDomainId(0001));
        Assert.assertEquals(subDomainList.get(0).getBitstringlength(), Bsl._256Bit);
        Assert.assertEquals(ipv6List.size(), 1);
        Assert.assertEquals(ipv6List.get(0).getBitstringlength(), new Integer(256));
        Assert.assertEquals(ipv6List.get(0).getBierMplsLabelBase(), new MplsLabel(120L));
        Assert.assertEquals(ipv6List.get(0).getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize((short)56));
    }

    private void assertModifiedParametersOfSubDomainAndDeletedIpv4Ipv6Test(List<SubDomain> subDomainList,
                                                                           List<Ipv4> ipv4List, List<Ipv6> ipv6List) {
        Assert.assertEquals(subDomainList.size(), 1);
        Assert.assertEquals(subDomainList.get(0).getSubDomainId(),new SubDomainId(0001));
        Assert.assertEquals(subDomainList.get(0).getBitstringlength(), Bsl._256Bit);
        Assert.assertEquals(ipv4List.size(), 1);
        Assert.assertEquals(ipv4List.get(0).getBitstringlength(), new Integer(64));
        Assert.assertEquals(ipv4List.get(0).getBierMplsLabelBase(), new MplsLabel(101L));
        Assert.assertEquals(ipv4List.get(0).getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize((short)30));
        Assert.assertEquals(ipv6List.size(), 1);
        Assert.assertEquals(ipv6List.get(0).getBitstringlength(), new Integer(128));
        Assert.assertEquals(ipv6List.get(0).getBierMplsLabelBase(), new MplsLabel(98L));
        Assert.assertEquals(ipv6List.get(0).getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize((short)48));
    }
}
