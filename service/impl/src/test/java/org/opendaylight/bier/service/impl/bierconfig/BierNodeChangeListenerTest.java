/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.bierconfig;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import org.opendaylight.bier.adapter.api.BierConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopology;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.BierTopologyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNode;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierNodeKey;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.Af;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.AfBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BierNodeChangeListenerTest extends AbstractConcurrentDataBrokerTest {


    private BierConfigWriterMock bierConfigWriterMock;
    private BierNodeChangeListener bierNodeChangeListener;

    public void setUp() {
        bierConfigWriterMock = new BierConfigWriterMock();
        bierNodeChangeListener = new BierNodeChangeListener(bierConfigWriterMock);
        getDataBroker().registerDataTreeChangeListener(new DataTreeIdentifier<BierNodeParams>(
                LogicalDatastoreType.CONFIGURATION, bierNodeChangeListener.getBierNodeId()), bierNodeChangeListener);
    }

    @Test
    public void testBierNodeChangeListener() {
        setUp();

        List<Ipv4> ipv4List = new ArrayList<>();
        ipv4List.add(constructIpv4(64, 101L, (short)30));

        List<Ipv6> ipv6List = new ArrayList<>();
        ipv6List.add(constructIpv6(128, 98L, (short)48));

        List<SubDomain> subDomainList = new ArrayList<>();
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit,
                constructAf(ipv4List, ipv6List)));

        Domain domain1 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));

        List<Domain> domainListAfter1 = new ArrayList<>();
        domainListAfter1.add(domain1);

        //Test add domain from null to one
        BierNode bierNodeBefore = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(null));
        BierNode bierNodeAfter1 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));

        addBierNodeToDataStore(bierNodeBefore);
        addBierNodeToDataStore(bierNodeAfter1);
        assertAddedDomainTest1(bierConfigWriterMock.getDomainProcessList());


        //Test add domain from one to two
        Domain domain2 = constructDomain(0002, constructBierGlobal(BierEncapsulation.class, Bsl._512Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));

        List<Domain> domainListAfter2 = new ArrayList<>();
        domainListAfter2.add(domain1);
        domainListAfter2.add(domain2);

        BierNode bierNodeAfter2 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter2));

        addBierNodeToDataStore(bierNodeAfter2);
        assertAddedDomainTest2(bierConfigWriterMock.getDomainProcessList());

        //Test delete domain from two to one
        BierNode bierNodeAfter3 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));

        addBierNodeToDataStore(bierNodeAfter3);
        assertDeletedDomainTest1(bierConfigWriterMock.getDomainProcessList());

        //Test modify parameters of domain
        Domain domain3 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 10,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain1);
        domainListAfter1.add(domain3);

        BierNode bierNodeAfter4 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));

        addBierNodeToDataStore(bierNodeAfter4);
        assertModifiedDomainTest(bierConfigWriterMock.getDomainProcessList());

        //Test modify parameters of domain and delete subDomain
        Domain domain4 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", null));
        domainListAfter1.remove(domain3);
        domainListAfter1.add(domain4);

        BierNode bierNodeAfter5 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));

        addBierNodeToDataStore(bierNodeAfter5);
        assertModifiedParametersOfDomainAndDeletedSubDomainTest(bierConfigWriterMock.getDomainProcessList(),
                bierConfigWriterMock.getSubDomainProcessList());

        //Test add subDomain
        Domain domain5 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain4);
        domainListAfter1.add(domain5);

        BierNode bierNodeAfter6 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));

        addBierNodeToDataStore(bierNodeAfter6);
        assertAddedSubDomainTest1(bierConfigWriterMock.getSubDomainProcessList());

        //Test add subDomain again
        subDomainList.add(constructSubDomain(0002, IgpType.ISIS, 12, 13, Bsl._512Bit,
                constructAf(ipv4List, ipv6List)));
        Domain domain6 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain5);
        domainListAfter1.add(domain6);

        BierNode bierNodeAfter7 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));

        addBierNodeToDataStore(bierNodeAfter7);
        assertAddedSubDomainTest2(bierConfigWriterMock.getSubDomainProcessList());

        //Test delete subDomain
        subDomainList.remove(constructSubDomain(0002, IgpType.ISIS, 12, 13, Bsl._512Bit,
                constructAf(ipv4List, ipv6List)));
        Domain domain7 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain6);
        domainListAfter1.add(domain7);

        BierNode bierNodeAfter8 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));

        addBierNodeToDataStore(bierNodeAfter8);
        assertDeletedSubDomainTest(bierConfigWriterMock.getSubDomainProcessList());

        //Test modify parameters of subDomain
        subDomainList.remove(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit,
                constructAf(ipv4List, ipv6List)));
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._128Bit,
                constructAf(ipv4List, ipv6List)));

        Domain domain8 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain7);
        domainListAfter1.add(domain8);

        BierNode bierNodeAfter9 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));

        addBierNodeToDataStore(bierNodeAfter9);
        assertModifiedParametersOfSubDomainTest(bierConfigWriterMock.getSubDomainProcessList());

        //Test modify parameters of subDomain and delete ipv4
        subDomainList.remove(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._128Bit,
                constructAf(ipv4List, ipv6List)));
        ipv4List.add(constructIpv4(80, 110L, (short)45));
        ipv6List.add(constructIpv6(256, 120L, (short)56));
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._128Bit,
                constructAf(ipv4List, ipv6List)));
        Domain domain9 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain8);
        domainListAfter1.add(domain9);
        BierNode bierNodeAfter10 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));
        addBierNodeToDataStore(bierNodeAfter10);

        subDomainList.remove(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._128Bit,
                constructAf(ipv4List, ipv6List)));
        ipv4List.remove(constructIpv4(80, 110L, (short)45));
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._256Bit,
                constructAf(ipv4List, ipv6List)));

        Domain domain10 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain9);
        domainListAfter1.add(domain10);

        BierNode bierNodeAfter11 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));

        addBierNodeToDataStore(bierNodeAfter11);
        assertModifiedParametersOfSubDomainAndDeletedIpv4Test(bierConfigWriterMock.getSubDomainProcessList(),
                bierConfigWriterMock.getIpv4ProcessList());

        //Test modify parameters of subDomain and delete ipv6
        subDomainList.remove(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._256Bit,
                constructAf(ipv4List, ipv6List)));
        ipv6List.remove(constructIpv6(256, 120L, (short)56));
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit,
                constructAf(ipv4List, ipv6List)));

        Domain domain11 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain10);
        domainListAfter1.add(domain11);

        BierNode bierNodeAfter12 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));

        addBierNodeToDataStore(bierNodeAfter12);
        assertModifiedParametersOfSubDomainAndDeletedIpv6Test(bierConfigWriterMock.getSubDomainProcessList(),
                bierConfigWriterMock.getIpv6ProcessList());

        //Test modify parameters of subDomain and delete ipv4 and ipv6
        subDomainList.remove(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit,
                constructAf(ipv4List, ipv6List)));
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._256Bit,
                constructAf(null, null)));
        Domain domain12 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain11);
        domainListAfter1.add(domain12);

        BierNode bierNodeAfter13 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));

        addBierNodeToDataStore(bierNodeAfter13);
        assertModifiedParametersOfSubDomainAndDeletedIpv4Ipv6Test(bierConfigWriterMock.getSubDomainProcessList(),
                bierConfigWriterMock.getIpv4ProcessList(), bierConfigWriterMock.getIpv6ProcessList());

        //Test only delete ipv4
        subDomainList.remove(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._256Bit,
                constructAf(null, null)));
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit,
                constructAf(ipv4List, ipv6List)));

        Domain domain13 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain12);
        domainListAfter1.add(domain13);

        BierNode bierNodeAfter14 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));
        addBierNodeToDataStore(bierNodeAfter14);

        subDomainList.remove(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit,
                constructAf(ipv4List, ipv6List)));
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit,
                constructAf(null, ipv6List)));

        Domain domain14 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain13);
        domainListAfter1.add(domain14);

        BierNode bierNodeAfter15 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));
        addBierNodeToDataStore(bierNodeAfter15);
        assertDeleteDSubDomainIpv4(bierConfigWriterMock.getIpv4ProcessList());

        //Test only delete ipv6
        subDomainList.remove(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit,
                constructAf(null, ipv6List)));
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit,
                constructAf(null, null)));

        Domain domain15 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain14);
        domainListAfter1.add(domain15);

        BierNode bierNodeAfter16 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));
        addBierNodeToDataStore(bierNodeAfter16);
        assertDeleteDSubDomainIpv6(bierConfigWriterMock.getIpv6ProcessList());

        //Test add af
        subDomainList.remove(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit,
                constructAf(null, null)));
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit,
                null));

        Domain domain16 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain15);
        domainListAfter1.add(domain16);

        BierNode bierNodeAfter17 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));
        addBierNodeToDataStore(bierNodeAfter17);

        subDomainList.remove(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit,
                null));
        subDomainList.add(constructSubDomain(0001, IgpType.ISIS, 12, 13, Bsl._512Bit,
                constructAf(ipv4List, null)));

        Domain domain17 = constructDomain(0001, constructBierGlobal(BierEncapsulation.class, Bsl._128Bit, 9,
                "102.112.20.40/24", "fe80::7009:fe25:8170:36af/64", subDomainList));
        domainListAfter1.remove(domain16);
        domainListAfter1.add(domain17);

        BierNode bierNodeAfter18 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(domainListAfter1));
        addBierNodeToDataStore(bierNodeAfter18);
        assertAddedAfChange(bierConfigWriterMock.getSubDomainProcessList());

        //Delete domain from one to null
        BierNode bierNodeAfter19 = constructBierNode("0001", "Node1", "0001", 35, 38,
                constructBierTerminationPointList("0002"), constructBierNodeParams(null));
        addBierNodeToDataStore(bierNodeAfter19);
        assertDeletedDomainTest2(bierConfigWriterMock.getDomainProcessList());

    }

    private void addBierNodeToDataStore(BierNode bierNode) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<BierNode> bierNodePath = InstanceIdentifier.create(BierNetworkTopology.class)
                .child(BierTopology.class, new BierTopologyKey("example-linkstate-topology"))
                .child(BierNode.class, new BierNodeKey(bierNode.getNodeId()));
        tx.put(LogicalDatastoreType.CONFIGURATION, bierNodePath, bierNode, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
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

    private Af constructAf(List<Ipv4> ipv4List, List<Ipv6> ipv6List) {
        AfBuilder afBuilder = new AfBuilder();
        if (null != ipv4List && !ipv4List.isEmpty()) {
            afBuilder.setIpv4(ipv4List);
            if (null != ipv6List && !ipv6List.isEmpty()) {
                afBuilder.setIpv6(ipv6List);
                return afBuilder.build();
            } else {
                afBuilder.setIpv6(null);
                return afBuilder.build();
            }
        } else {
            afBuilder.setIpv4(null);
            if (null != ipv6List && !ipv6List.isEmpty()) {
                afBuilder.setIpv6(ipv6List);
                return afBuilder.build();
            } else {
                afBuilder.setIpv6(null);
                return afBuilder.build();
            }
        }
    }


    private SubDomain constructSubDomain(Integer subDomainId, IgpType igpType, Integer mtId,
                                         Integer bfrId, Bsl bsl, Af af) {
        SubDomainBuilder subDomainBuilder = new SubDomainBuilder();
        subDomainBuilder.setSubDomainId(new SubDomainId(subDomainId));
        subDomainBuilder.setIgpType(igpType);
        subDomainBuilder.setMtId(new MtId(mtId));
        subDomainBuilder.setBfrId(new BfrId(bfrId));
        subDomainBuilder.setBitstringlength(bsl);
        subDomainBuilder.setAf(af);
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
        Assert.assertEquals(domainList.size(), 1);
        Assert.assertEquals(domainList.get(0).getDomainId(), new DomainId(0001));
        BierGlobal bierGlobal = domainList.get(0).getBierGlobal();
        Assert.assertEquals(bierGlobal.getBitstringlength(), Bsl._512Bit);
        assertAddedOrDeletedDomainBierGlobalData(bierGlobal);
    }

    private void assertAddedDomainTest2(List<Domain> domainList) {
        Assert.assertEquals(domainList.size(), 2);
        Assert.assertEquals(domainList.get(1).getDomainId(), new DomainId(0002));
        BierGlobal bierGlobal = domainList.get(1).getBierGlobal();
        Assert.assertEquals(bierGlobal.getBitstringlength(), Bsl._512Bit);
        assertAddedOrDeletedDomainBierGlobalData(bierGlobal);
    }

    private void assertDeletedDomainTest1(List<Domain> domainList) {
        Assert.assertEquals(domainList.size(), 3);
        Assert.assertEquals(domainList.get(2).getDomainId(), new DomainId(0002));
        BierGlobal bierGlobal = domainList.get(2).getBierGlobal();
        Assert.assertEquals(bierGlobal.getBitstringlength(), Bsl._512Bit);
        assertAddedOrDeletedDomainBierGlobalData(bierGlobal);
    }

    private void assertAddedOrDeletedDomainBierGlobalData(BierGlobal bierGlobal) {
        SubDomain subDomain = bierGlobal.getSubDomain().get(0);
        Ipv4 ipv4 = subDomain.getAf().getIpv4().get(0);
        Ipv6 ipv6 = subDomain.getAf().getIpv6().get(0);
        Assert.assertEquals(bierGlobal.getEncapsulationType(), BierEncapsulation.class);
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
        Assert.assertEquals(domainList.size(), 4);
        Assert.assertEquals(domainList.get(3).getDomainId(), new DomainId(0001));
        BierGlobal bierGlobal = domainList.get(3).getBierGlobal();
        Assert.assertEquals(bierGlobal.getBitstringlength(), Bsl._128Bit);
    }

    private void assertModifiedParametersOfDomainAndDeletedSubDomainTest(List<Domain> domainList,
                                                                         List<SubDomain> subDomainList) {
        Assert.assertEquals(domainList.size(), 5);
        Assert.assertEquals(domainList.get(4).getDomainId(), new DomainId(0001));
        Assert.assertEquals(domainList.get(4).getBierGlobal().getBitstringlength(), Bsl._128Bit);
        Assert.assertEquals(domainList.get(4).getBierGlobal().getBfrId(), new BfrId(9));
        Assert.assertEquals(subDomainList.size(), 1);
        Assert.assertEquals(subDomainList.get(0).getSubDomainId(), new SubDomainId(0001));
        assertAddedOrDeletedSubDomainData(subDomainList.get(0));
    }

    private void assertAddedSubDomainTest1(List<SubDomain> subDomainList) {
        Assert.assertEquals(subDomainList.size(), 2);
        Assert.assertEquals(subDomainList.get(1).getSubDomainId(), new SubDomainId(0001));
        Assert.assertEquals(subDomainList.get(1).getBitstringlength(), Bsl._512Bit);
        assertAddedOrDeletedSubDomainData(subDomainList.get(1));
    }

    private void assertAddedSubDomainTest2(List<SubDomain> subDomainList) {
        Assert.assertEquals(subDomainList.size(), 3);
        Assert.assertEquals(subDomainList.get(2).getSubDomainId(), new SubDomainId(0002));
        Assert.assertEquals(subDomainList.get(2).getBitstringlength(), Bsl._512Bit);
        assertAddedOrDeletedSubDomainData(subDomainList.get(2));
    }

    private void assertDeletedSubDomainTest(List<SubDomain> subDomainList) {
        Assert.assertEquals(subDomainList.size(), 4);
        Assert.assertEquals(subDomainList.get(3).getSubDomainId(), new SubDomainId(0002));
        Assert.assertEquals(subDomainList.get(3).getBitstringlength(), Bsl._512Bit);
        assertAddedOrDeletedSubDomainData(subDomainList.get(3));
    }

    private void assertModifiedParametersOfSubDomainTest(List<SubDomain> subDomainList) {
        Assert.assertEquals(subDomainList.size(), 5);
        Assert.assertEquals(subDomainList.get(4).getSubDomainId(), new SubDomainId(0001));
        Assert.assertEquals(subDomainList.get(4).getBitstringlength(), Bsl._128Bit);
        assertAddedOrDeletedSubDomainData(subDomainList.get(4));
    }

    private void assertAddedOrDeletedSubDomainData(SubDomain subDomain) {
        Ipv4 ipv4 = subDomain.getAf().getIpv4().get(0);
        Ipv6 ipv6 = subDomain.getAf().getIpv6().get(0);
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

    private void assertModifiedParametersOfSubDomainAndDeletedIpv4Test(List<SubDomain> subDomainList,
                                                                       List<Ipv4> ipv4List) {
        Assert.assertEquals(subDomainList.size(), 7);
        Assert.assertEquals(subDomainList.get(6).getSubDomainId(),new SubDomainId(0001));
        Assert.assertEquals(subDomainList.get(6).getBitstringlength(), Bsl._256Bit);
        Assert.assertEquals(ipv4List.size(), 1);
        Assert.assertEquals(ipv4List.get(0).getBitstringlength(), new Integer(80));
        Assert.assertEquals(ipv4List.get(0).getBierMplsLabelBase(), new MplsLabel(110L));
        Assert.assertEquals(ipv4List.get(0).getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize((short)45));
    }

    private void assertModifiedParametersOfSubDomainAndDeletedIpv6Test(List<SubDomain> subDomainList,
                                                                       List<Ipv6> ipv6List) {
        Assert.assertEquals(subDomainList.size(), 8);
        Assert.assertEquals(subDomainList.get(7).getSubDomainId(),new SubDomainId(0001));
        Assert.assertEquals(subDomainList.get(7).getBitstringlength(), Bsl._512Bit);
        Assert.assertEquals(ipv6List.size(), 1);
        Assert.assertEquals(ipv6List.get(0).getBitstringlength(), new Integer(256));
        Assert.assertEquals(ipv6List.get(0).getBierMplsLabelBase(), new MplsLabel(120L));
        Assert.assertEquals(ipv6List.get(0).getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize((short)56));
    }

    private void assertModifiedParametersOfSubDomainAndDeletedIpv4Ipv6Test(List<SubDomain> subDomainList,
                                                                           List<Ipv4> ipv4List, List<Ipv6> ipv6List) {
        Assert.assertEquals(subDomainList.size(), 9);
        Assert.assertEquals(subDomainList.get(8).getSubDomainId(),new SubDomainId(0001));
        Assert.assertEquals(subDomainList.get(8).getBitstringlength(), Bsl._256Bit);
        Assert.assertEquals(ipv4List.size(), 2);
        Assert.assertEquals(ipv4List.get(1).getBitstringlength(), new Integer(64));
        Assert.assertEquals(ipv4List.get(1).getBierMplsLabelBase(), new MplsLabel(101L));
        Assert.assertEquals(ipv4List.get(1).getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize((short)30));
        Assert.assertEquals(ipv6List.size(), 2);
        Assert.assertEquals(ipv6List.get(1).getBitstringlength(), new Integer(128));
        Assert.assertEquals(ipv6List.get(1).getBierMplsLabelBase(), new MplsLabel(98L));
        Assert.assertEquals(ipv6List.get(1).getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize((short)48));
    }

    private void assertDeleteDSubDomainIpv4(List<Ipv4> ipv4List) {
        Assert.assertEquals(ipv4List.size(), 3);
        Assert.assertEquals(ipv4List.get(2).getBitstringlength(), new Integer(64));
        Assert.assertEquals(ipv4List.get(2).getBierMplsLabelBase(), new MplsLabel(101L));
        Assert.assertEquals(ipv4List.get(2).getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize((short)30));
    }

    private void assertDeleteDSubDomainIpv6(List<Ipv6> ipv6List) {
        Assert.assertEquals(ipv6List.size(), 3);
        Assert.assertEquals(ipv6List.get(2).getBitstringlength(), new Integer(128));
        Assert.assertEquals(ipv6List.get(2).getBierMplsLabelBase(), new MplsLabel(98L));
        Assert.assertEquals(ipv6List.get(2).getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize((short)48));
    }

    private void assertAddedAfChange(List<SubDomain> subDomainList) {
        Assert.assertEquals(subDomainList.size(), 12);
        Assert.assertNotNull(subDomainList.get(11).getAf());
    }

    private void assertDeletedDomainTest2(List<Domain> domainList) {
        Assert.assertEquals(domainList.size(), 6);
        Assert.assertEquals(domainList.get(5).getDomainId(), new DomainId(0001));
        BierGlobal bierGlobal = domainList.get(5).getBierGlobal();
        Assert.assertEquals(bierGlobal.getBitstringlength(), Bsl._128Bit);
        Assert.assertEquals(bierGlobal.getSubDomain().size(), 1);
        SubDomain subDomain = bierGlobal.getSubDomain().get(0);
        Ipv4 ipv4 = subDomain.getAf().getIpv4().get(0);
        Assert.assertEquals(bierGlobal.getEncapsulationType(), BierEncapsulation.class);
        Assert.assertEquals(bierGlobal.getBfrId(), new BfrId(9));
        Assert.assertEquals(bierGlobal.getIpv4BfrPrefix(), new Ipv4Prefix("102.112.20.40/24"));
        Assert.assertEquals(bierGlobal.getIpv6BfrPrefix(), new Ipv6Prefix("fe80::7009:fe25:8170:36af/64"));
        Assert.assertEquals(subDomain.getSubDomainId(), new SubDomainId(0001));
        Assert.assertEquals(subDomain.getBitstringlength(), Bsl._512Bit);
        Assert.assertEquals(subDomain.getIgpType(), IgpType.ISIS);
        Assert.assertEquals(subDomain.getBfrId(), new BfrId(13));
        Assert.assertEquals(subDomain.getMtId(), new MtId(12));
        Assert.assertEquals(subDomain.getAf().getIpv4().size(), 1);
        Assert.assertEquals(ipv4.getBitstringlength(), new Integer(64));
        Assert.assertEquals(ipv4.getBierMplsLabelBase(), new MplsLabel(101L));
        Assert.assertEquals(ipv4.getBierMplsLabelRangeSize(), new BierMplsLabelRangeSize(Short.valueOf("30")));
    }
}