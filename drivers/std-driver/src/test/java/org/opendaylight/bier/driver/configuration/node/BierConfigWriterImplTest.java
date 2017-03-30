/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.driver.configuration.node;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

import org.opendaylight.bier.driver.configuration.node.BierConfigDataBuilder;
import org.opendaylight.bier.driver.configuration.node.BierConfigWriterImpl;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobal;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.Af;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;





public class BierConfigWriterImplTest extends AbstractConcurrentDataBrokerTest {
    private MountPoint mountPoint;
    private DataBroker dataBroker ;
    private BindingAwareBroker bindingAwareBroker;
    private BindingAwareBroker.ConsumerContext consumerContext;
    private MountPointService mountPointService;
    // Optionals
    private Optional<MountPoint> optionalMountPointObject;
    private Optional<DataBroker> optionalDataBrokerObject;
    private BierConfigWriterImpl bierConfigWriter ;
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
        bierConfigWriter = new BierConfigWriterImpl(netconfDataOperator);
        netconfDataOperator.onSessionInitialized(consumerContext);
    }


    @Test
    public void testWriteDomainAdd() throws Exception {
        buildMock();
        buildInstance();
        bierConfigWriter.writeDomain(ConfigurationType.ADD, NODE_ID,
                bierConfigDataBuilder.buildDomain(),result).checkedGet();
        assertTrue(result.isSuccessful());
        BierGlobal bierGlobalActual = netconfDataOperator.read(dataBroker,
                netconfDataOperator.BIER_GLOBAL_IID);
        BierGlobal bierGlobalExpected = bierConfigDataBuilder.buildBierGlobal();
        assertEquals(bierGlobalExpected,bierGlobalActual);
    }

    @Test
    public void testWriteDomainModify() throws Exception {
        buildMock();
        buildInstance();
        bierConfigWriter.writeDomain(ConfigurationType.ADD, NODE_ID,
                bierConfigDataBuilder.buildDomain(),result).checkedGet();
        assertTrue(result.isSuccessful());
        bierConfigWriter.writeDomain(ConfigurationType.MODIFY, NODE_ID,
                bierConfigDataBuilder.buildDomainModify(),result).checkedGet();
        assertTrue(result.isSuccessful());
        BierGlobal bierGlobalActual = netconfDataOperator.read(dataBroker,
                netconfDataOperator.BIER_GLOBAL_IID);
        BierGlobal bierGlobalExpected = bierConfigDataBuilder.buildBierGlobalModify();
        assertEquals(bierGlobalExpected,bierGlobalActual);
    }

    @Test
    public void testWriteDomainDelete() throws Exception {
        buildMock();
        buildInstance();
        bierConfigWriter.writeDomain(ConfigurationType.ADD, NODE_ID,
                bierConfigDataBuilder.buildDomain(),result).checkedGet();
        assertTrue(result.isSuccessful());
        bierConfigWriter.writeDomain(ConfigurationType.DELETE, NODE_ID,
                null,result).checkedGet();
        assertTrue(result.isSuccessful());
        BierGlobal bierGlobalActual = netconfDataOperator.read(dataBroker,
                netconfDataOperator.BIER_GLOBAL_IID);
        assertNull(bierGlobalActual);
    }


    @Test
    public void testWriteSubdomainAdd() throws Exception {
        buildMock();
        buildInstance();
        bierConfigWriter.writeSubdomain(ConfigurationType.ADD, NODE_ID,
                null,bierConfigDataBuilder.buildSubDomainSingle(),result).checkedGet();
        assertTrue(result.isSuccessful());
        BierGlobal bierGlobalActual = netconfDataOperator.read(dataBroker,
                netconfDataOperator.BIER_GLOBAL_IID);
        ArrayList<SubDomain> subDomainExpected = new ArrayList<SubDomain>();
        subDomainExpected.add(bierConfigDataBuilder.buildSubDomainSingle());
        List<SubDomain> subDomianActual = bierGlobalActual.getSubDomain();
        assertEquals(subDomainExpected,subDomianActual);

    }

    @Test
    public void testWriteSubdomainModify() throws Exception {
        buildMock();
        buildInstance();
        bierConfigWriter.writeSubdomain(ConfigurationType.ADD, NODE_ID,
                null,bierConfigDataBuilder.buildSubDomainSingle(),result).checkedGet();
        assertTrue(result.isSuccessful());
        bierConfigWriter.writeSubdomain(ConfigurationType.MODIFY, NODE_ID,
                null,bierConfigDataBuilder.buildSubDomainSingleModify(),result).checkedGet();
        assertTrue(result.isSuccessful());
        BierGlobal bierGlobalActual = netconfDataOperator.read(dataBroker,
                netconfDataOperator.BIER_GLOBAL_IID);
        ArrayList<SubDomain> subDomainExpected = new ArrayList<SubDomain>();
        subDomainExpected.add(bierConfigDataBuilder.buildSubDomainSingleModify());
        List<SubDomain> subDomianActual = bierGlobalActual.getSubDomain();
        assertEquals(subDomainExpected,subDomianActual);

    }

    @Test
    public void testWriteSubdomainDelete() throws Exception {
        buildMock();
        buildInstance();
        bierConfigWriter.writeDomain(ConfigurationType.ADD, NODE_ID,
                bierConfigDataBuilder.buildDomain(),result).checkedGet();
        assertTrue(result.isSuccessful());
        bierConfigWriter.writeSubdomain(ConfigurationType.DELETE,
                NODE_ID,null,
                bierConfigDataBuilder.buildSubDomainDelete(),result).checkedGet();
        assertTrue(result.isSuccessful());
        SubDomain subDomianActual = netconfDataOperator.read(dataBroker,
                bierConfigWriter.getSubDomainIId(BierConfigDataBuilder.SUBDOMAINID));
        assertNull(subDomianActual);
    }

    private InstanceIdentifier<Ipv4> buildIpv4IId(SubDomainId subDomainId,Ipv4 ipv4) {
        return bierConfigWriter.getSubDomainIId(subDomainId)
                .child(Af.class)
                .child(Ipv4.class, ipv4.getKey());

    }

    @Test
    public void testWriteSubdomainIpv4Add() throws Exception {
        buildMock();
        buildInstance();
        Ipv4 ipv4Expected = bierConfigDataBuilder.buildIpv4SingleAdd();
        bierConfigWriter.writeSubdomainIpv4(ConfigurationType.ADD, NODE_ID,
                null,BierConfigDataBuilder.SUBDOMAINID,ipv4Expected,result).checkedGet();
        assertTrue(result.isSuccessful());
        Ipv4 ipv4Actual = netconfDataOperator.read(dataBroker,
                buildIpv4IId(BierConfigDataBuilder.SUBDOMAINID,ipv4Expected));
        assertEquals(ipv4Actual,ipv4Expected);

    }

    @Test
    public void testWriteSubdomainIpv4Modify() throws Exception {
        buildMock();
        buildInstance();
        bierConfigWriter.writeSubdomainIpv4(ConfigurationType.ADD, NODE_ID,
                null,BierConfigDataBuilder.SUBDOMAINID,bierConfigDataBuilder.buildIpv4SingleAdd(),result).checkedGet();
        assertTrue(result.isSuccessful());
        Ipv4 ipv4Expected = bierConfigDataBuilder.buildIpv4SingleModify();
        bierConfigWriter.writeSubdomainIpv4(ConfigurationType.MODIFY, NODE_ID,
                null,BierConfigDataBuilder.SUBDOMAINID,ipv4Expected,result).checkedGet();
        assertTrue(result.isSuccessful());
        Ipv4 ipv4Actual = netconfDataOperator.read(dataBroker,
                buildIpv4IId(BierConfigDataBuilder.SUBDOMAINID,ipv4Expected));
        assertEquals(ipv4Actual,ipv4Expected);
    }

    @Test
    public void testWriteSubdomainIpv4Delete() throws Exception {
        buildMock();
        buildInstance();
        bierConfigWriter.writeSubdomainIpv4(ConfigurationType.ADD, NODE_ID,
                null,BierConfigDataBuilder.SUBDOMAINID,bierConfigDataBuilder.buildIpv4SingleAdd(),result).checkedGet();
        assertTrue(result.isSuccessful());
        bierConfigWriter.writeSubdomainIpv4(ConfigurationType.DELETE, NODE_ID,
                null,BierConfigDataBuilder.SUBDOMAINID,bierConfigDataBuilder.buildIpv4SingleDelete(),result)
                .checkedGet();
        assertTrue(result.isSuccessful());
        Ipv4 ipv4Actual = netconfDataOperator.read(dataBroker,
                buildIpv4IId(BierConfigDataBuilder.SUBDOMAINID,bierConfigDataBuilder.buildIpv4SingleDelete()));
        assertNull(ipv4Actual);
    }

    private InstanceIdentifier<Ipv6> buildIpv6IId(SubDomainId subDomainId,Ipv6 ipv6) {
        return bierConfigWriter.getSubDomainIId(subDomainId)
                .child(Af.class)
                .child(Ipv6.class, ipv6.getKey());

    }

    @Test
    public void testWriteSubdomainIpv6Add() throws Exception {
        buildMock();
        buildInstance();
        Ipv6 ipv6Expected = bierConfigDataBuilder.buildIpv6SingleAdd();
        bierConfigWriter.writeSubdomainIpv6(ConfigurationType.ADD, NODE_ID,
                null,BierConfigDataBuilder.SUBDOMAINID,ipv6Expected,result).checkedGet();
        assertTrue(result.isSuccessful());
        Ipv6 ipv6Actual = netconfDataOperator.read(dataBroker,
                buildIpv6IId(BierConfigDataBuilder.SUBDOMAINID,ipv6Expected));
        assertEquals(ipv6Actual,ipv6Expected);

    }

    @Test
    public void testWriteSubdomainIpv6Modify() throws Exception {
        buildMock();
        buildInstance();
        bierConfigWriter.writeSubdomainIpv6(ConfigurationType.ADD, NODE_ID,
                null,BierConfigDataBuilder.SUBDOMAINID,bierConfigDataBuilder.buildIpv6SingleAdd(),result).checkedGet();
        assertTrue(result.isSuccessful());
        Ipv6 ipv6Expected = bierConfigDataBuilder.buildIpv6SingleModify();
        bierConfigWriter.writeSubdomainIpv6(ConfigurationType.MODIFY, NODE_ID,
                null,BierConfigDataBuilder.SUBDOMAINID,ipv6Expected,result).checkedGet();
        assertTrue(result.isSuccessful());
        Ipv6 ipv6Actual = netconfDataOperator.read(dataBroker,
                buildIpv6IId(BierConfigDataBuilder.SUBDOMAINID,ipv6Expected));
        assertEquals(ipv6Actual,ipv6Expected);
    }

    @Test
    public void testWriteSubdomainIpv6Delete() throws Exception {
        buildMock();
        buildInstance();
        bierConfigWriter.writeSubdomainIpv6(ConfigurationType.ADD, NODE_ID,
                null,BierConfigDataBuilder.SUBDOMAINID,bierConfigDataBuilder.buildIpv6SingleAdd(),result).checkedGet();
        assertTrue(result.isSuccessful());
        bierConfigWriter.writeSubdomainIpv6(ConfigurationType.DELETE, NODE_ID,
                null,BierConfigDataBuilder.SUBDOMAINID,bierConfigDataBuilder.buildIpv6SingleDelete(),result)
                .checkedGet();
        assertTrue(result.isSuccessful());
        Ipv6 ipv6Actual = netconfDataOperator.read(dataBroker,
                buildIpv6IId(BierConfigDataBuilder.SUBDOMAINID,bierConfigDataBuilder.buildIpv6SingleDelete()));
        assertNull(ipv6Actual);
    }

}