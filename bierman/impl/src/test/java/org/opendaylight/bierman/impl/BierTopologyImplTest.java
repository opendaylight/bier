/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bierman.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.bierman.impl.bierconfig.BierConfigServiceImpl;
import org.opendaylight.bierman.impl.teconfig.BierTeConfigServiceImpl;
import org.opendaylight.bierman.impl.topo.BierTopologyServiceImpl;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.ConfigureNodeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.ConfigureNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteIpv4InputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteIpv4Output;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteIpv6InputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteIpv6Output;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteNodeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.DeleteNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.ConfigureTeLabelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.ConfigureTeLabelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.ConfigureTeNodeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.ConfigureTeNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.ConfigureTeSubdomainInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.ConfigureTeSubdomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeBpInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeBpOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeBslInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeBslOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeLabelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeLabelOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeSiInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeSiOutput;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeSubdomainInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.DeleteTeSubdomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureDomainInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureDomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureSubdomainInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureSubdomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteDomainInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteDomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteSubdomainInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteSubdomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LoadTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LoadTopologyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryDomainInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryDomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryLinkInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryLinkOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryLinkOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryNodeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainLinkInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainLinkOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainNodeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QuerySubdomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTeSubdomainLinkInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTeSubdomainLinkOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTeSubdomainNodeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTeSubdomainNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierTeLabelRangeSize;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.TeDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.TeSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBslKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSiKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBp;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBpBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.te.si.TeBpKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierEncapsulationMpls;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierMplsLabelRangeSize;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.IgpType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobalBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.AfBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class BierTopologyImplTest extends AbstractConcurrentDataBrokerTest {
    private BierDataManager topoManager;
    private BierTopologyServiceImpl topoImpl;
    private BierConfigServiceImpl bierConfigImpl;
    private BierTeConfigServiceImpl bierTeConfigImpl;

    @Before
    public void setUp() throws Exception {
        BierTopologyTestDataInit.initTopo(getDataBroker());
        topoManager = new BierDataManager(getDataBroker());
        topoImpl = new BierTopologyServiceImpl(topoManager);
        bierConfigImpl = new BierConfigServiceImpl(topoManager);
        bierTeConfigImpl = new BierTeConfigServiceImpl(topoManager);
        topoManager.start();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void loadTopologyTest() throws Exception {
        RpcResult<LoadTopologyOutput> output = topoImpl.loadTopology().get();

        // assert
        LoadTopologyOutputBuilder outputBuilder = new LoadTopologyOutputBuilder(
                output.getResult());
        Assert.assertTrue(outputBuilder.getTopology().size() == 1);
        Assert.assertTrue(outputBuilder.getTopology().get(0).getTopologyId()
                .equals("example-linkstate-topology"));
    }

    @Test
    public void queryTopologyTest() throws Exception {
        QueryTopologyInputBuilder inputBuilder = new QueryTopologyInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        RpcResult<QueryTopologyOutput> output = topoImpl.queryTopology(
                inputBuilder.build()).get();
        QueryTopologyOutputBuilder outputBuilder = new QueryTopologyOutputBuilder(
                output.getResult());
        Assert.assertTrue(outputBuilder.getTopologyId().equals("example-linkstate-topology"));
        Assert.assertTrue(outputBuilder.getNodeId().size() == 2);
        Assert.assertTrue(outputBuilder.getNodeId().get(0).getNodeId()
                .equals("1") || outputBuilder.getNodeId().get(0).getNodeId()
                .equals("2"));
        Assert.assertTrue(outputBuilder.getNodeId().get(1).getNodeId()
                .equals("2") || outputBuilder.getNodeId().get(1).getNodeId()
                .equals("1"));
        Assert.assertTrue(outputBuilder.getLinkId().size() == 1);
        Assert.assertTrue(outputBuilder.getLinkId().get(0).getLinkId()
                .equals("2,192.168.54.13-1,192.168.54.11"));
    }

    @Test
    public void queryTopologyTest2() throws Exception {
        RpcResult<QueryTopologyOutput> output = topoImpl.queryTopology(null).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input is null!",output.getErrors().iterator().next().getMessage());

        QueryTopologyInputBuilder inputBuilder = new QueryTopologyInputBuilder();
        output = topoImpl.queryTopology(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input param is error!",output.getErrors().iterator().next().getMessage());

        inputBuilder.setTopologyId("flow:2");
        output = topoImpl.queryTopology(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("topo is not exist!",output.getErrors().iterator().next().getMessage());
    }

    @Test
    public void queryNodeTest() throws Exception {
        RpcResult<QueryNodeOutput> output = topoImpl.queryNode(null).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input is null!",output.getErrors().iterator().next().getMessage());

        QueryNodeInputBuilder inputBuilder = new QueryNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        output = topoImpl.queryNode(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input param is error!",output.getErrors().iterator().next().getMessage());

        inputBuilder.setTopologyId("flow:2");
        List<String> nodeIdList = new ArrayList<String>();
        nodeIdList.add("1");
        inputBuilder.setNode(nodeIdList);
        output = topoImpl.queryNode(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("topo is not exist!",output.getErrors().iterator().next().getMessage());
    }

    @Test
    public void queryNodeTest2() throws Exception {
        QueryNodeOutput output = queryNode();
        Assert.assertTrue(output.getNode().size() == 1);
        Assert.assertTrue(output.getNode().get(0).getNodeId().equals("1"));
        Assert.assertTrue(output.getNode().get(0).getName().equals("1"));
        Assert.assertTrue(output.getNode().get(0).getRouterId().equals("1"));
        Assert.assertTrue(output.getNode().get(0).getLatitude().intValue() == 0);
        Assert.assertTrue(output.getNode().get(0).getLongitude().intValue() == 0);
        Assert.assertTrue(output.getNode().get(0).getBierNodeParams()
                .getDomain().size() == 0);
        Assert.assertTrue(output.getNode().get(0).getBierTerminationPoint()
                .size() == 1);
        Assert.assertTrue(output.getNode().get(0).getBierTerminationPoint()
                .get(0).getTpId().equals("192.168.54.11"));
    }

    @Test
    public void queryLinkTest() throws Exception {
        QueryLinkInputBuilder inputBuilder = new QueryLinkInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        List<String> linkIdList = new ArrayList<String>();
        linkIdList.add("2,192.168.54.13-1,192.168.54.11");
        inputBuilder.setLink(linkIdList);
        RpcResult<QueryLinkOutput> output = topoImpl.queryLink(
                inputBuilder.build()).get();
        QueryLinkOutputBuilder outputBuilder = new QueryLinkOutputBuilder(
                output.getResult());
        Assert.assertTrue(outputBuilder.getLink().size() == 1);
        Assert.assertTrue(outputBuilder.getLink().get(0).getLinkId()
                .equals("2,192.168.54.13-1,192.168.54.11"));
        Assert.assertTrue(outputBuilder.getLink().get(0).getLinkSource()
                .getSourceNode().equals("2"));
        Assert.assertTrue(outputBuilder.getLink().get(0).getLinkSource()
                .getSourceTp().equals("192.168.54.13"));
        Assert.assertTrue(outputBuilder.getLink().get(0).getLinkDest()
                .getDestNode().equals("1"));
        Assert.assertTrue(outputBuilder.getLink().get(0).getLinkDest()
                .getDestTp().equals("192.168.54.11"));
        Assert.assertTrue(outputBuilder.getLink().get(0).getLoss().intValue() == 0);
        Assert.assertTrue(outputBuilder.getLink().get(0).getDelay().intValue() == 0);
    }

    @Test
    public void queryLinkTest2() throws Exception {
        RpcResult<QueryLinkOutput> output = topoImpl.queryLink(null).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input is null!",output.getErrors().iterator().next().getMessage());

        QueryLinkInputBuilder inputBuilder = new QueryLinkInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        output = topoImpl.queryLink(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input param is error!",output.getErrors().iterator().next().getMessage());

        inputBuilder.setTopologyId("flow:2");
        List<String> linkIdList = new ArrayList<String>();
        linkIdList.add("1");
        inputBuilder.setLink(linkIdList);
        output = topoImpl.queryLink(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("topo is not exist!",output.getErrors().iterator().next().getMessage());
    }

    @Test
    public void configDomainTest() throws Exception {
        ConfigureDomainOutput output = configureDomain(1);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        ConfigureDomainOutput output2 = configureDomain(2);
        Assert.assertTrue(output2.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryDomainOutput domainOutput = queryDomain("example-linkstate-topology");
        Assert.assertTrue(domainOutput.getDomain().size() == 2);
        Assert.assertTrue(domainOutput.getDomain().get(0).getDomainId()
                .getValue().intValue() == 1);
        Assert.assertTrue(domainOutput.getDomain().get(1).getDomainId()
                .getValue().intValue() == 2);
    }

    @Test
    public void configDomainTest2() throws Exception {
        ConfigureDomainOutput output = configureDomain(1);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        ConfigureDomainOutput output2 = configureDomain(1);
        Assert.assertTrue(output2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output2.getConfigureResult().getErrorCause().equals("domain is exist!"));
    }

    @Test
    public void configDomainTest3() throws Exception {
        RpcResult<ConfigureDomainOutput> output = topoImpl.configureDomain(null).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause().equals("input is null!"));

        ConfigureDomainInputBuilder inputBuilder = new ConfigureDomainInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        output = topoImpl.configureDomain(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause().equals("input param is error!"));

        inputBuilder.setTopologyId("flow:2");
        List<DomainId> domainIdList = new ArrayList<DomainId>();
        domainIdList.add(new DomainId(1));
        inputBuilder.setDomain(domainIdList);
        output = topoImpl.configureDomain(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause().equals("topo is not exist!"));
    }

    @Test
    public void queryDomainTest() throws Exception {
        RpcResult<QueryDomainOutput> output = topoImpl.queryDomain(null).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input is null!",output.getErrors().iterator().next().getMessage());

        QueryDomainInputBuilder inputBuilder = new QueryDomainInputBuilder();
        output = topoImpl.queryDomain(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input param is error!",output.getErrors().iterator().next().getMessage());

        inputBuilder.setTopologyId("flow:2");
        output = topoImpl.queryDomain(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("topo is not exist!",output.getErrors().iterator().next().getMessage());
    }

    @Test
    public void configSubdomainTest() throws Exception {
        ConfigureSubdomainOutput output = configureSubdomain(1,1);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        String errorCause = "domain is not exist!";
        Assert.assertTrue(output.getConfigureResult().getErrorCause()
                .equals(errorCause));
    }

    @Test
    public void configSubdomainTest2() throws Exception {
        configureDomain(1);
        ConfigureSubdomainOutput output = configureSubdomain(1,1);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        ConfigureSubdomainOutput output2 = configureSubdomain(1,2);
        Assert.assertTrue(output2.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QuerySubdomainOutput queryOutput = querySubdomain("example-linkstate-topology",
                new DomainId(1));
        Assert.assertTrue(queryOutput.getSubdomain().size() == 2);
        Assert.assertTrue(queryOutput.getSubdomain().get(0).getSubDomainId()
                .getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getSubdomain().get(1).getSubDomainId()
                .getValue().intValue() == 2);
    }

    @Test
    public void configSubdomainTest3() throws Exception {
        configureDomain(1);
        ConfigureSubdomainOutput output = configureSubdomain(1,1);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        ConfigureSubdomainOutput output2 = configureSubdomain(1,1);
        Assert.assertTrue(output2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output2.getConfigureResult().getErrorCause().equals("subdomain is exist!"));
    }

    @Test
    public void configSubdomainTest4() throws Exception {
        RpcResult<ConfigureSubdomainOutput> output = topoImpl.configureSubdomain(null).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause().equals("input is null!"));

        ConfigureSubdomainInputBuilder inputBuilder = new ConfigureSubdomainInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(1));
        output = topoImpl.configureSubdomain(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause().equals("input param is error!"));
    }

    @Test
    public void querySubDomainTest() throws Exception {
        RpcResult<QuerySubdomainOutput> output = topoImpl.querySubdomain(null).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input is null!",output.getErrors().iterator().next().getMessage());

        QuerySubdomainInputBuilder inputBuilder = new QuerySubdomainInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        output = topoImpl.querySubdomain(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input param is error!",output.getErrors().iterator().next().getMessage());

        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(1));
        output = topoImpl.querySubdomain(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("domain is not exist!",output.getErrors().iterator().next().getMessage());
    }

    @Test
    public void configNodeTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        ConfigureNodeOutput output = configureNode(1,1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
    }

    @Test
    public void configNodeTest2() throws Exception {
        ConfigureNodeOutput output = configureNode(1,1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals("domain or subdomain is not exist!"));
    }

    @Test
    public void configNodeTest3() throws Exception {
        configureDomain(1);
        ConfigureNodeOutput output = configureNode(1,1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals("domain or subdomain is not exist!"));
    }

    @Test
    public void configNodeTest4() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        ConfigureNodeOutput output = configureNode(1,1,"3");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals("node is not exist!"));
    }

    @Test
    public void configNodeTest5() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureNode(1,1,"1");
        ConfigureNodeOutput output = configureNode(1,1,"2");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals("node bfrId is exist in same subdomain!"));
    }

    @Test
    public void configNodeTest6() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureSubdomain(1,2);
        configureNode(1,1,"1");
        ConfigureNodeOutput output = configureNode(1,2,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals("node label range is overlapped!"));
    }

    @Test
    public void configNodeTest7() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        ConfigureNodeOutput output = configureNode1(1,1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals(" domain is null or empty!"));
    }

    @Test
    public void configNodeTest8() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        ConfigureNodeOutput output = configureNode2(1,1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals("domain bitstringlength is null!"));
    }

    @Test
    public void configNodeTest9() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        ConfigureNodeOutput output = configureNode3(1,1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals("domain bfrid is null!"));
    }

    @Test
    public void configNodeTest10() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        ConfigureNodeOutput output = configureNode4(1,1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause()
                .equals("domain ipv4-bfr-prefix and ipv6-bfr-prefix are null on the same time!"));
    }

    @Test
    public void configNodeTest11() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        ConfigureNodeOutput output = configureNode5(1,1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals("subdomain igp-type is null!"));
    }

    @Test
    public void configNodeTest12() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        ConfigureNodeOutput output = configureNode6(1,1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause()
                .equals("subdomain af is null when encapsulation-type is mpls!"));
    }

    @Test
    public void configNodeTest13() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        ConfigureNodeOutput output = configureNode7(1,1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause()
                .equals("subdomain ipv4 and ipv6 are null on the same time!"));
    }

    @Test
    public void configNodeTest14() throws Exception {
        RpcResult<ConfigureNodeOutput> output = bierConfigImpl.configureNode(null).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause().equals("input is null!"));

        ConfigureNodeInputBuilder inputBuilder = new ConfigureNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        output = bierConfigImpl.configureNode(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause().equals("input param is error!"));
    }

    @Test
    public void configureTeSubDomainTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1, 1);
        configureTeLabel(new MplsLabel(1L), new BierTeLabelRangeSize(5L), "1");
        ConfigureTeSubdomainOutput output = configureTeSubDomain("3", 1, 1);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause()
                .equals("node is not exist!"));

        ConfigureTeSubdomainOutput output1 = configureTeSubDomain(null, 1, 1);
        Assert.assertTrue(output1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output1.getConfigureResult().getErrorCause()
                .equals("input param is error!"));

        ConfigureTeSubdomainOutput output2 = configureTeSubDomain("1", 1, 3);
        Assert.assertTrue(output2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output2.getConfigureResult().getErrorCause()
                .equals("domain or subdomain is not exist!"));

        ConfigureTeSubdomainOutput output3 = configureTeSubDomain("1", 1, 1);
        Assert.assertTrue(output3.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        ConfigureTeSubdomainOutput output4 = configureTeSubDomain("1", 1, 1);
        Assert.assertTrue(output4.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output4.getConfigureResult().getErrorCause()
                .equals("te-SubDomain already configured !"));

    }

    @Test
    public void configureTeNodeTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        ConfigureTeNodeOutput output = configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getBitstringlength()
                == Bsl._64Bit);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().size()
                == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getSi().getValue().intValue() == 1);
        Long baseLabel = queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getFtLabel().getValue();
        Assert.assertTrue(baseLabel >= 1L && baseLabel <= 6L);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().get(0).getTpId().equals("192.168.54.13"));

        ConfigureTeNodeOutput output1 = configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.14",2,"1");
        Assert.assertTrue(output1.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        ConfigureTeNodeOutput output2 = configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",1,"1");
        Assert.assertTrue(output2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output2.getConfigureResult().getErrorCause()
                .equals("node tp-id or bitposition is exist in same si!"));
    }


    @Test
    public void configureTeLabelTest() throws Exception {
        ConfigureTeLabelOutput output = configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeLableRange().getLabelBase().getValue() == 1L);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeLableRange().getLabelRangeSize()
                .getValue() == (short)5);
    }

    @Test
    public void configureTeLabelTest1() throws Exception {
        ConfigureTeLabelOutput output = configureTeLabel(null,new BierTeLabelRangeSize(5L),"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals("label-base is null!"));

        ConfigureTeLabelOutput output1 = configureTeLabel(new MplsLabel(100L),null,"1");
        Assert.assertTrue(output1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output1.getConfigureResult().getErrorCause().equals("label-range-size is null!"));

        ConfigureTeLabelOutput output2 = configureTeLabel(new MplsLabel(1048575L),new BierTeLabelRangeSize(1L),"1");
        Assert.assertTrue(output2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output2.getConfigureResult().getErrorCause()
                .equals("label-base plus label-range-size range [0‥1048575]"));

        ConfigureTeLabelOutput output3 = configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(1L),null);
        Assert.assertTrue(output3.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output3.getConfigureResult().getErrorCause().equals("input param is error!"));

        ConfigureTeLabelOutput output4 = configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(1L),"7");
        Assert.assertTrue(output4.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output4.getConfigureResult().getErrorCause().equals("node is not exist!"));


    }

    @Test
    public void modifyNodeDomainTest() throws Exception {
        configureDomain(1);
        configureDomain(2);
        configureSubdomain(1,1);
        configureNode(1,1,"1");
        ConfigureNodeOutput output = modifyNodeDomain();
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().size() == 2);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(1).getDomainId().getValue().intValue() == 2);

    }

    @Test
    public void modifyNodeSubDomainTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureSubdomain(1,2);
        configureNode(1,1,"1");
        ConfigureNodeOutput output = modifyNodeSubDomain();
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().size() == 2);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(1)
                .getSubDomainId().getValue().intValue() == 2);
    }

    @Test
    public void deleteNodeTest1() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureNode(1,1,"1");
        configureNode2(1,1,"1");
        QuerySubdomainNodeOutput queryOutput = querySubdomainNode(1,1);
        Assert.assertTrue(queryOutput.getSubdomainNode().size() == 1);
        Assert.assertTrue(queryOutput.getSubdomainNode().get(0).getNodeId()
                .equals("1"));
        Assert.assertTrue(queryOutput.getSubdomainNode().get(0).getBierNodeParams()
                .getDomain().size() == 1);
        Assert.assertTrue(queryOutput.getSubdomainNode().get(0).getBierNodeParams()
                .getDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getSubdomainNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().size() == 1);
        Assert.assertTrue(queryOutput.getSubdomainNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);

        DeleteNodeOutput output = deleteNode(1,1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QuerySubdomainNodeOutput queryOutput2 = querySubdomainNode(1,1);
        Assert.assertTrue(queryOutput2.getSubdomainNode().size() == 0);
    }

    @Test
    public void deleteNodeTest2() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureNode(1,1,"1");

        DeleteNodeOutput output = deleteNode(2,1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause()
                .equals("node is not belong to domain or subdomain!"));
    }

    @Test
    public void deleteNodeTest3() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);

        DeleteNodeOutput output = deleteNode(1,1,"3");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause()
                .equals("node is not exist!"));
    }

    @Test
    public void deleteNodeTest4() throws Exception {
        DeleteNodeInputBuilder inputBuilder = new DeleteNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(1));
        inputBuilder.setSubDomainId(new SubDomainId(1));
        RpcResult<DeleteNodeOutput> output = bierConfigImpl.deleteNode(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause()
                .equals("input param is error!"));

        inputBuilder.setNodeId("3");
        output = bierConfigImpl.deleteNode(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause()
                .equals("node is not exist!"));

        inputBuilder.setNodeId("1");
        output = bierConfigImpl.deleteNode(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause()
                .equals("node is not belong to domain or subdomain!"));
    }

    @Test
    public void deleteTeSubdomainTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureSubdomain(1,2);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        configureTeSubDomain("1", 1, 1);
        configureTeSubDomain("1", 1, 2);

        DeleteTeSubdomainOutput output = deleteTeSubdomain(1,2,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput1 = queryNode();
        Assert.assertTrue(queryOutput1.getNode().size() == 1);
        Assert.assertTrue(queryOutput1.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getSubDomainId().getValue().intValue() == 1);

        DeleteTeSubdomainOutput output1 = deleteTeSubdomain(1,1,"1");
        Assert.assertTrue(output1.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput2 = queryNode();
        Assert.assertTrue(queryOutput2.getNode().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().size() == 0);
    }

    @Test
    public void deleteTeSubdomainTest1() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        configureTeSubDomain("1", 1, 1);

        DeleteTeSubdomainOutput output = deleteTeSubdomain(1,2,null);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause()
                .equals("input param is error!"));

        DeleteTeSubdomainOutput output1 = deleteTeSubdomain(1,2,"5");
        Assert.assertTrue(output1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output1.getConfigureResult().getErrorCause()
                .equals("node is not exist!"));

        DeleteTeSubdomainOutput output2 = deleteTeSubdomain(1,6,"1");
        Assert.assertTrue(output2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output2.getConfigureResult().getErrorCause()
                .equals("node is not belong to domain or subdomain!"));

    }

    @Test
    public void deleteTeSubdomainTest2() throws Exception {
        configureDomain(1);
        configureDomain(2);
        configureSubdomain(1,1);
        configureSubdomain(2,1);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",64,"1");
        configureTeNode(2,1,Bsl._64Bit,new Si(1),"192.168.54.14",4,"1");

        DeleteTeSubdomainOutput output = deleteTeSubdomain(1,1,"1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
    }

    @Test
    public void deleteTeBslTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",64,"1");
        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getBitstringlength()
                == Bsl._64Bit);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().size()
                == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getSi().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().get(0).getTpId().equals("192.168.54.13"));

        DeleteTeBslOutput output = deleteTeBsl(1,1,"1",Bsl._64Bit);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput2 = queryNode();
        Assert.assertTrue(queryOutput2.getNode().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().size() == 0);
    }

    @Test
    public void deleteTeBslTest1() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",64,"1");
        configureTeNode(1,1,Bsl._128Bit,new Si(2),"192.168.54.14",128,"1");
        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().size() == 2);
        Bsl bsl = queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getBitstringlength();
        Assert.assertTrue((bsl == Bsl._64Bit) || (bsl == Bsl._128Bit));
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().size()
                == 1);
        int si = queryOutput.getNode().get(0).getBierTeNodeParams().getTeDomain().get(0)
                .getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0).getSi().getValue().intValue();
        Assert.assertTrue((si == 1) || (si == 2));
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().size() == 1);
        String tpid = queryOutput.getNode().get(0).getBierTeNodeParams().getTeDomain().get(0)
                .getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0).getTeBp().get(0).getTpId();
        Assert.assertTrue(tpid.equals("192.168.54.13") || (tpid.equals("192.168.54.14")));
        DeleteTeBslOutput output = deleteTeBsl(1,1,"1",Bsl._128Bit);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        QueryNodeOutput queryOutput2 = queryNode();
        Assert.assertTrue(queryOutput2.getNode().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().size() == 1);

        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getBitstringlength()
                == Bsl._64Bit);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().size()
                == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getSi().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().get(0).getTpId().equals("192.168.54.13"));
    }

    @Test
    public void deleteTeBslTest2() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureTeLabel(new MplsLabel(100L),new BierTeLabelRangeSize(5L),"1");
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",64,"1");

        DeleteTeBslOutput output = deleteTeBsl(1,1,null,Bsl._64Bit);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause()
                .equals("input param is error!"));

        DeleteTeBslOutput output1 = deleteTeBsl(1,1,"7",Bsl._64Bit);
        Assert.assertTrue(output1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output1.getConfigureResult().getErrorCause()
                .equals("node is not exist!"));

        DeleteTeBslOutput output2 = deleteTeBsl(3,1,"1",Bsl._64Bit);
        Assert.assertTrue(output2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output2.getConfigureResult().getErrorCause()
                .equals("node is not belong to domain or subdomain!"));

        DeleteTeBslOutput output3 = deleteTeBsl(1,1,"1",Bsl._128Bit);
        Assert.assertTrue(output3.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output3.getConfigureResult().getErrorCause()
                .equals("Te-Bsl is not exist!"));
    }

    @Test
    public void deleteTeBslTest3() throws Exception {
        configureDomain(1);
        configureDomain(2);
        configureSubdomain(1,1);
        configureSubdomain(2,1);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"2");
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",64,"1");
        configureTeNode(2,1,Bsl._64Bit,new Si(1),"192.168.54.15",4,"1");
        configureTeNode(2,1,Bsl._64Bit,new Si(1),"192.168.54.16",6,"2");

        DeleteTeBslOutput output = deleteTeBsl(1,1,"1",Bsl._64Bit);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
    }

    @Test
    public void deleteTeSiTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",64,"1");
        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getBitstringlength()
                == Bsl._64Bit);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().size()
                == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getSi().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().get(0).getTpId().equals("192.168.54.13"));

        DeleteTeSiOutput output = deleteTeSi(1,1,"1",Bsl._64Bit,new Si(1));
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput2 = queryNode();
        Assert.assertTrue(queryOutput2.getNode().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().size() == 0);
    }

    @Test
    public void deleteTeSiTest1() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",64,"1");
        configureTeNode(1,1,Bsl._64Bit,new Si(3),"192.168.54.15",256,"1");
        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getBitstringlength() == Bsl._64Bit);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().size()
                == 2);
        int si = queryOutput.getNode().get(0).getBierTeNodeParams().getTeDomain().get(0)
                .getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0).getSi().getValue().intValue();
        Assert.assertTrue((si == 1) || (si == 3));
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().size() == 1);
        String tpid = queryOutput.getNode().get(0).getBierTeNodeParams().getTeDomain().get(0)
                .getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0).getTeBp().get(0).getTpId();
        Assert.assertTrue(tpid.equals("192.168.54.13") || (tpid.equals("192.168.54.15")));

        DeleteTeSiOutput output = deleteTeSi(1,1,"1",Bsl._64Bit,new Si(3));
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput2 = queryNode();
        Assert.assertTrue(queryOutput2.getNode().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getBitstringlength()
                == Bsl._64Bit);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().size()
                == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getSi().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().get(0).getTpId().equals("192.168.54.13"));
    }

    @Test
    public void deleteTeSiTest2() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",64,"1");

        DeleteTeSiOutput output = deleteTeSi(1,1,null,Bsl._64Bit,new Si(1));
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause()
                .equals("input param is error!"));

        DeleteTeSiOutput output1 = deleteTeSi(1,1,"7",Bsl._64Bit,new Si(1));
        Assert.assertTrue(output1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output1.getConfigureResult().getErrorCause()
                .equals("node is not exist!"));

        DeleteTeSiOutput output2 = deleteTeSi(3,1,"1",Bsl._64Bit,new Si(1));
        Assert.assertTrue(output2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output2.getConfigureResult().getErrorCause()
                .equals("node is not belong to domain or subdomain!"));

        DeleteTeSiOutput output3 = deleteTeSi(1,1,"1",Bsl._128Bit,new Si(1));
        Assert.assertTrue(output3.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output3.getConfigureResult().getErrorCause()
                .equals("Te-Bsl is not exist!"));

        DeleteTeSiOutput output4 = deleteTeSi(1,1,"1",Bsl._64Bit,new Si(2));
        Assert.assertTrue(output4.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output4.getConfigureResult().getErrorCause()
                .equals("Te-Si is not exist!"));

    }

    @Test
    public void deleteTeBpTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",64,"1");
        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getBitstringlength()
                == Bsl._64Bit);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().size()
                == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getSi().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().get(0).getTpId().equals("192.168.54.13"));

        DeleteTeBpOutput output = deleteTeBp(1,1,"1",Bsl._64Bit,new Si(1),"192.168.54.13");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput2 = queryNode();
        Assert.assertTrue(queryOutput2.getNode().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().size() == 0);
    }

    @Test
    public void deleteTeBpTest1() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",64,"1");
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.15",256,"1");
        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getBitstringlength() == Bsl._64Bit);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().size()
                == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams().getTeDomain().get(0)
                .getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0).getSi().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().size() == 2);
        String tpid = queryOutput.getNode().get(0).getBierTeNodeParams().getTeDomain().get(0)
                .getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0).getTeBp().get(0).getTpId();
        Assert.assertTrue(tpid.equals("192.168.54.13") || (tpid.equals("192.168.54.15")));

        DeleteTeBpOutput output = deleteTeBp(1,1,"1",Bsl._64Bit,new Si(1),"192.168.54.15");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput2 = queryNode();
        Assert.assertTrue(queryOutput2.getNode().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getBitstringlength()
                == Bsl._64Bit);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().size()
                == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getSi().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0).getTeBsl().get(0).getTeSi().get(0)
                .getTeBp().get(0).getTpId().equals("192.168.54.13"));
    }

    @Test
    public void deleteTeBpTest2() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",64,"1");

        DeleteTeBpOutput output = deleteTeBp(1,1,null,Bsl._64Bit,new Si(1),"192.168.54.13");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause()
                .equals("input param is error!"));

        DeleteTeBpOutput output1 = deleteTeBp(1,1,"7",Bsl._64Bit,new Si(1),"192.168.54.13");
        Assert.assertTrue(output1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output1.getConfigureResult().getErrorCause()
                .equals("node is not exist!"));

        DeleteTeBpOutput output2 = deleteTeBp(3,1,"1",Bsl._64Bit,new Si(1),"192.168.54.13");
        Assert.assertTrue(output2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output2.getConfigureResult().getErrorCause()
                .equals("node is not belong to domain or subdomain!"));

        DeleteTeBpOutput output3 = deleteTeBp(1,1,"1",Bsl._128Bit,new Si(1),"192.168.54.13");
        Assert.assertTrue(output3.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output3.getConfigureResult().getErrorCause()
                .equals("Te-Bsl is not exist!"));

        DeleteTeBpOutput output4 = deleteTeBp(1,1,"1",Bsl._64Bit,new Si(2),"192.168.54.13");
        Assert.assertTrue(output4.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output4.getConfigureResult().getErrorCause()
                .equals("Te-Si is not exist!"));

        DeleteTeBpOutput output5 = deleteTeBp(1,1,"1",Bsl._64Bit,new Si(1),"192.168.54.14");
        Assert.assertTrue(output5.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output5.getConfigureResult().getErrorCause()
                .equals("Te-Bp is not exist!"));
    }

    @Test
    public void deleteIpv4Test() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureNode(1,1,"1");
        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0).getAf().getIpv4().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0).getAf().getIpv4().get(0)
                .getBitstringlength().intValue() == 64);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0).getAf().getIpv4().get(0)
                .getBierMplsLabelBase().getValue() == (long)1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0).getAf().getIpv4().get(0)
                .getBierMplsLabelRangeSize().getValue() == (short)4);

        DeleteIpv4Output output = deleteIpv4(1,1,"1",64,1,4);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput2 = queryNode();
        Assert.assertTrue(queryOutput2.getNode().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierNodeParams()
                .getDomain().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0).getAf().getIpv4().size() == 0);
    }

    @Test
    public void deleteIpv4Test2() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureNode(1,1,"1");

        DeleteIpv4Output output = deleteIpv4(1,2,"1",64,1,4);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause()
                .equals("node is not belong to domain or subdomain!"));
    }

    @Test
    public void deleteIpv4Test3() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);

        DeleteIpv4Output output = deleteIpv4(1,1,"3",64,1,4);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals("node is not exist!"));
    }

    @Test
    public void deleteIpv4Test4() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureNode(1,1,"1");

        DeleteIpv4Output output = deleteIpv4(1,1,"1",4,1,4);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals("ipv4 is not exist!"));
    }

    @Test
    public void deleteIpv4Test5() throws Exception {
        DeleteIpv4InputBuilder inputBuilder = new DeleteIpv4InputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(1));
        inputBuilder.setSubDomainId(new SubDomainId(1));
        RpcResult<DeleteIpv4Output> output = bierConfigImpl.deleteIpv4(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause()
                .equals("input param is error!"));

        inputBuilder.setNodeId("3");
        output = bierConfigImpl.deleteIpv4(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause()
                .equals("node is not exist!"));

        inputBuilder.setNodeId("1");
        output = bierConfigImpl.deleteIpv4(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause()
                .equals("node is not belong to domain or subdomain!"));
    }

    @Test
    public void deleteIpv6Test() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureNode(1,1,"1");
        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0).getAf().getIpv6().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0).getAf().getIpv6().get(0)
                .getBitstringlength().intValue() == 64);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0).getAf().getIpv6().get(0)
                .getBierMplsLabelBase().getValue() == (long)5);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0).getAf().getIpv6().get(0)
                .getBierMplsLabelRangeSize().getValue() == (short)4);

        DeleteIpv6Output output = deleteIpv6(1,1,"1",64,5,4);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput2 = queryNode();
        Assert.assertTrue(queryOutput2.getNode().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierNodeParams()
                .getDomain().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().size() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
        Assert.assertTrue(queryOutput2.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0).getAf().getIpv6().size() == 0);
    }

    @Test
    public void deleteIpv6Test2() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureNode(1,1,"1");

        DeleteIpv6Output output = deleteIpv6(1,2,"1",64,1,4);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause()
                .equals("node is not belong to domain or subdomain!"));
    }

    @Test
    public void deleteIpv6Test3() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);

        DeleteIpv6Output output = deleteIpv6(1,1,"3",64,1,4);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals("node is not exist!"));
    }

    @Test
    public void deleteIpv6Test4() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureNode(1,1,"1");

        DeleteIpv6Output output = deleteIpv6(1,1,"1",4,1,4);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getConfigureResult().getErrorCause().equals("ipv6 is not exist!"));
    }

    @Test
    public void deleteIpv6Test5() throws Exception {
        DeleteIpv6InputBuilder inputBuilder = new DeleteIpv6InputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(1));
        inputBuilder.setSubDomainId(new SubDomainId(1));
        RpcResult<DeleteIpv6Output> output = bierConfigImpl.deleteIpv6(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause()
                .equals("input param is error!"));

        inputBuilder.setNodeId("3");
        output = bierConfigImpl.deleteIpv6(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause()
                .equals("node is not exist!"));

        inputBuilder.setNodeId("1");
        output = bierConfigImpl.deleteIpv6(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause()
                .equals("node is not belong to domain or subdomain!"));
    }

    @Test
    public void deleteSubdomainTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureSubdomain(1,2);
        QuerySubdomainOutput queryOutput = querySubdomain("example-linkstate-topology",
                new DomainId(1));
        Assert.assertTrue(queryOutput.getSubdomain().size() == 2);
        Assert.assertTrue(queryOutput.getSubdomain().get(0).getSubDomainId()
                .getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getSubdomain().get(1).getSubDomainId()
                .getValue().intValue() == 2);

        DeleteSubdomainInputBuilder inputBuilder = new DeleteSubdomainInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(1));
        inputBuilder.setSubDomainId(new SubDomainId(1));
        RpcResult<DeleteSubdomainOutput> output = topoImpl.deleteSubdomain(
                inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QuerySubdomainOutput queryOutput2 = querySubdomain("example-linkstate-topology",
                new DomainId(1));
        Assert.assertTrue(queryOutput2.getSubdomain().size() == 1);
        Assert.assertTrue(queryOutput2.getSubdomain().get(0).getSubDomainId()
                .getValue().intValue() == 2);
    }

    @Test
    public void deleteSubdomainTest2() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);

        DeleteSubdomainInputBuilder inputBuilder = new DeleteSubdomainInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(1));
        inputBuilder.setSubDomainId(new SubDomainId(3));
        RpcResult<DeleteSubdomainOutput> output = topoImpl.deleteSubdomain(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause()
                .equals("domain or subdomain is not exist!"));
    }

    @Test
    public void deleteDomainTest() throws Exception {
        configureDomain(1);
        configureDomain(2);
        QueryDomainOutput domainOutput = queryDomain("example-linkstate-topology");
        Assert.assertTrue(domainOutput.getDomain().size() == 2);
        Assert.assertTrue(domainOutput.getDomain().get(0).getDomainId()
                .getValue().intValue() == 1);
        Assert.assertTrue(domainOutput.getDomain().get(1).getDomainId()
                .getValue().intValue() == 2);

        DeleteDomainInputBuilder inputBuilder = new DeleteDomainInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(1));
        RpcResult<DeleteDomainOutput> output = topoImpl.deleteDomain(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryDomainOutput domainOutput2 = queryDomain("example-linkstate-topology");
        Assert.assertTrue(domainOutput2.getDomain().size() == 1);
        Assert.assertTrue(domainOutput2.getDomain().get(0).getDomainId()
                .getValue().intValue() == 2);
    }

    @Test
    public void deleteDomainTest2() throws Exception {
        configureDomain(1);

        DeleteDomainInputBuilder inputBuilder = new DeleteDomainInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(3));
        RpcResult<DeleteDomainOutput> output = topoImpl.deleteDomain(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause().equals("domain is not exist!"));
    }

    @Test
    public void deleteDomainTest3() throws Exception {
        RpcResult<DeleteDomainOutput> output = topoImpl.deleteDomain(null).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause().equals("input is null!"));

        DeleteDomainInputBuilder inputBuilder = new DeleteDomainInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        output = topoImpl.deleteDomain(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause().equals("input param is error!"));

        inputBuilder.setTopologyId("flow:2");
        inputBuilder.setDomainId(new DomainId(1));
        output = topoImpl.deleteDomain(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause().equals("topo is not exist!"));
    }

    @Test
    public void deleteTeLabelTest() throws Exception {
        ConfigureTeLabelOutput output = configureTeLabel(new MplsLabel(1L), new BierTeLabelRangeSize(5L), "1");
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryNodeOutput queryOutput = queryNode();
        Assert.assertTrue(queryOutput.getNode().size() == 1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeLableRange().getLabelBase().getValue() == 1L);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierTeLableRange().getLabelRangeSize()
                .getValue() == (short)5);

        DeleteTeLabelInputBuilder inputBuilder = new DeleteTeLabelInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId("1");
        RpcResult<DeleteTeLabelOutput> output1 = bierTeConfigImpl.deleteTeLabel(inputBuilder.build()).get();
        Assert.assertTrue(output1.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        QueryNodeOutput queryOutput1 = queryNode();
        Assert.assertTrue(queryOutput1.getNode().size() == 1);
        Assert.assertTrue(queryOutput1.getNode().get(0).getNodeId().equals("1"));
        Assert.assertTrue(queryOutput1.getNode().get(0).getBierTeLableRange() == null);
    }

    @Test
    public void deleteTeLabelTest1() throws Exception {
        DeleteTeLabelInputBuilder inputBuilder = new DeleteTeLabelInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId("1");
        RpcResult<DeleteTeLabelOutput> output1 = bierTeConfigImpl.deleteTeLabel(inputBuilder.build()).get();
        Assert.assertTrue(output1.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output1.getResult().getConfigureResult().getErrorCause().equals("Te-Label is not exist!"));
    }

    @Test
    public void deleteTeLabelTest2() throws Exception {
        DeleteTeLabelInputBuilder inputBuilder = new DeleteTeLabelInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId(null);
        RpcResult<DeleteTeLabelOutput> output1 = bierTeConfigImpl.deleteTeLabel(inputBuilder.build()).get();
        Assert.assertTrue(output1.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output1.getResult().getConfigureResult().getErrorCause().equals("input param is error!"));

        RpcResult<DeleteTeLabelOutput> output2 = bierTeConfigImpl.deleteTeLabel(null).get();
        Assert.assertTrue(output2.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output2.getResult().getConfigureResult().getErrorCause().equals("input is null!"));
    }

    @Test
    public void querySubDomainNodeTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureNode(1,1,"1");

        QuerySubdomainNodeOutput output = querySubdomainNode(1,1);
        Assert.assertTrue(output.getSubdomainNode().size() == 1);
        Assert.assertTrue(output.getSubdomainNode().get(0).getNodeId()
                .equals("1"));
        Assert.assertTrue(output.getSubdomainNode().get(0).getBierNodeParams()
                .getDomain().size() == 1);
        Assert.assertTrue(output.getSubdomainNode().get(0).getBierNodeParams()
                .getDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(output.getSubdomainNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().size() == 1);
        Assert.assertTrue(output.getSubdomainNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
    }

    @Test
    public void querySubDomainNodeTest2() throws Exception {
        RpcResult<QuerySubdomainNodeOutput> output = topoImpl.querySubdomainNode(null).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input is null!",output.getErrors().iterator().next().getMessage());

        QuerySubdomainNodeInputBuilder inputBuilder = new QuerySubdomainNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        output = topoImpl.querySubdomainNode(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input param is error!",output.getErrors().iterator().next().getMessage());

        inputBuilder.setTopologyId("flow:2");
        inputBuilder.setDomainId(new DomainId(1));
        inputBuilder.setSubDomainId(new SubDomainId(1));
        output = topoImpl.querySubdomainNode(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("topo is not exist!",output.getErrors().iterator().next().getMessage());

        inputBuilder.setTopologyId("example-linkstate-topology");
        output = topoImpl.querySubdomainNode(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("domain or subdomain is not exist!",output.getErrors().iterator().next().getMessage());
    }

    @Test
    public void querySubDomainLinkTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureNode(1,1,"1");

        QuerySubdomainLinkOutput output = querySubdomainLink(1,1);
        Assert.assertTrue(output.getSubdomainLink().size() == 0);
    }

    @Test
    public void querySubDomainLinkTest2() throws Exception {
        RpcResult<QuerySubdomainLinkOutput> output = topoImpl.querySubdomainLink(null).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input is null!",output.getErrors().iterator().next().getMessage());

        QuerySubdomainLinkInputBuilder inputBuilder = new QuerySubdomainLinkInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        output = topoImpl.querySubdomainLink(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input param is error!",output.getErrors().iterator().next().getMessage());

        inputBuilder.setTopologyId("flow:2");
        inputBuilder.setDomainId(new DomainId(1));
        inputBuilder.setSubDomainId(new SubDomainId(1));
        output = topoImpl.querySubdomainLink(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("topo is not exist!",output.getErrors().iterator().next().getMessage());

        inputBuilder.setTopologyId("example-linkstate-topology");
        output = topoImpl.querySubdomainLink(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("domain or subdomain is not exist!",output.getErrors().iterator().next().getMessage());

    }

    @Test
    public void queryTeSubDomainNodeTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureTeLabel(new MplsLabel(1L),new BierTeLabelRangeSize(5L),"1");
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",64,"1");
        QueryTeSubdomainNodeOutput output = queryTeSubdomainNode(1,1);
        Assert.assertTrue(output.getTeSubdomainNode().size() == 1);
        Assert.assertTrue(output.getTeSubdomainNode().get(0).getNodeId()
                .equals("1"));
        Assert.assertTrue(output.getTeSubdomainNode().get(0).getBierTeNodeParams()
                .getTeDomain().size() == 1);
        Assert.assertTrue(output.getTeSubdomainNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getDomainId().getValue().intValue() == 1);
        Assert.assertTrue(output.getTeSubdomainNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().size() == 1);
        Assert.assertTrue(output.getTeSubdomainNode().get(0).getBierTeNodeParams()
                .getTeDomain().get(0).getTeSubDomain().get(0)
                .getSubDomainId().getValue().intValue() == 1);
    }

    @Test
    public void queryTeSubDomainNodeTest2() throws Exception {
        RpcResult<QueryTeSubdomainNodeOutput> output = topoImpl.queryTeSubdomainNode(null).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input is null!",output.getErrors().iterator().next().getMessage());

        QueryTeSubdomainNodeInputBuilder inputBuilder = new QueryTeSubdomainNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        output = topoImpl.queryTeSubdomainNode(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input param is error!",output.getErrors().iterator().next().getMessage());

        inputBuilder.setTopologyId("flow:2");
        inputBuilder.setDomainId(new DomainId(1));
        inputBuilder.setSubDomainId(new SubDomainId(1));
        output = topoImpl.queryTeSubdomainNode(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("topo is not exist!",output.getErrors().iterator().next().getMessage());

        inputBuilder.setTopologyId("example-linkstate-topology");
        output = topoImpl.queryTeSubdomainNode(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("domain or subdomain is not exist!",output.getErrors().iterator().next().getMessage());
    }

    @Test
    public void queryTeSubDomainLinkTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureTeNode(1,1,Bsl._64Bit,new Si(1),"192.168.54.13",64,"1");
        QueryTeSubdomainLinkOutput output = queryTeSubdomainLink(1,1);
        Assert.assertTrue(output.getTeSubdomainLink().size() == 0);
    }

    @Test
    public void queryTeSubDomainLinkTest2() throws Exception {
        RpcResult<QueryTeSubdomainLinkOutput> output = topoImpl.queryTeSubdomainLink(null).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input is null!",output.getErrors().iterator().next().getMessage());

        QueryTeSubdomainLinkInputBuilder inputBuilder = new QueryTeSubdomainLinkInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        output = topoImpl.queryTeSubdomainLink(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("input param is error!",output.getErrors().iterator().next().getMessage());

        inputBuilder.setTopologyId("flow:2");
        inputBuilder.setDomainId(new DomainId(1));
        inputBuilder.setSubDomainId(new SubDomainId(1));
        output = topoImpl.queryTeSubdomainLink(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("topo is not exist!",output.getErrors().iterator().next().getMessage());

        inputBuilder.setTopologyId("example-linkstate-topology");
        output = topoImpl.queryTeSubdomainLink(inputBuilder.build()).get();
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals("domain or subdomain is not exist!",output.getErrors().iterator().next().getMessage());

    }

    private QueryNodeOutput queryNode() throws Exception {
        QueryNodeInputBuilder inputBuilder = new QueryNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        List<String> nodeIdList = new ArrayList<String>();
        nodeIdList.add("1");
        inputBuilder.setNode(nodeIdList);
        RpcResult<QueryNodeOutput> output = topoImpl.queryNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureDomainOutput configureDomain(int domainId) throws Exception {
        ConfigureDomainInputBuilder inputBuilder = new ConfigureDomainInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        List<DomainId> domainIdList = new ArrayList<DomainId>();
        domainIdList.add(new DomainId(domainId));
        inputBuilder.setDomain(domainIdList);
        RpcResult<ConfigureDomainOutput> output = topoImpl.configureDomain(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureSubdomainOutput configureSubdomain(int domainId,int subDomainId) throws Exception {
        ConfigureSubdomainInputBuilder inputBuilder = new ConfigureSubdomainInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(domainId));
        List<SubDomainId> subdomainIdList = new ArrayList<SubDomainId>();
        subdomainIdList.add(new SubDomainId(subDomainId));
        inputBuilder.setSubDomain(subdomainIdList);
        RpcResult<ConfigureSubdomainOutput> output = topoImpl
                .configureSubdomain(inputBuilder.build()).get();
        return output.getResult();
    }

    private QueryDomainOutput queryDomain(String topologyId) throws Exception {
        QueryDomainInputBuilder inputBuilder = new QueryDomainInputBuilder();
        inputBuilder.setTopologyId(topologyId);
        RpcResult<QueryDomainOutput> output = topoImpl.queryDomain(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private QuerySubdomainOutput querySubdomain(String topologyId,
            DomainId domainId) throws Exception {
        QuerySubdomainInputBuilder inputBuilder = new QuerySubdomainInputBuilder();
        inputBuilder.setTopologyId(topologyId);
        inputBuilder.setDomainId(domainId);
        RpcResult<QuerySubdomainOutput> output = topoImpl.querySubdomain(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureNodeOutput configureNode(int domainId,int subDomainId,String nodeId) throws Exception {
        ConfigureNodeInputBuilder inputBuilder = new ConfigureNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId(nodeId);
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(domainId));
        domainBuilder.setKey(new DomainKey(new DomainId(domainId)));

        SubDomainBuilder subDomainBuilder = new SubDomainBuilder();
        subDomainBuilder.setSubDomainId(new SubDomainId(subDomainId));
        subDomainBuilder.setBfrId(new BfrId(1));
        subDomainBuilder.setIgpType(IgpType.OSPF);

        Ipv4Builder ipv4Builder = new Ipv4Builder();
        ipv4Builder.setBitstringlength(64);
        ipv4Builder.setBierMplsLabelBase(new MplsLabel(1L));
        ipv4Builder.setBierMplsLabelRangeSize(new BierMplsLabelRangeSize((short)4));
        List<Ipv4> ipv4List = new ArrayList<Ipv4>();
        ipv4List.add(ipv4Builder.build());
        AfBuilder afBuilder = new AfBuilder();
        afBuilder.setIpv4(ipv4List);

        Ipv6Builder ipv6Builder = new Ipv6Builder();
        ipv6Builder.setBitstringlength(64);
        ipv6Builder.setBierMplsLabelBase(new MplsLabel(5L));
        ipv6Builder.setBierMplsLabelRangeSize(new BierMplsLabelRangeSize((short)4));
        List<Ipv6> ipv6List = new ArrayList<Ipv6>();
        ipv6List.add(ipv6Builder.build());
        afBuilder.setIpv6(ipv6List);

        subDomainBuilder.setAf(afBuilder.build());
        List<SubDomain> subDomainList = new ArrayList<SubDomain>();
        subDomainList.add(subDomainBuilder.build());

        BierGlobalBuilder bierBuilder = new BierGlobalBuilder();
        bierBuilder.setSubDomain(subDomainList);
        bierBuilder.setBfrId(new BfrId(1));
        bierBuilder.setEncapsulationType(BierEncapsulationMpls.class);
        bierBuilder.setBitstringlength(Bsl._64Bit);
        bierBuilder.setIpv4BfrPrefix(new Ipv4Prefix("10.41.41.41/22"));
        domainBuilder.setBierGlobal(bierBuilder.build());
        List<Domain> domainList = new ArrayList<Domain>();
        domainList.add(domainBuilder.build());
        inputBuilder.setDomain(domainList);

        RpcResult<ConfigureNodeOutput> output = bierConfigImpl.configureNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureNodeOutput configureNode1(int domainId,int subDomainId,String nodeId) throws Exception {
        ConfigureNodeInputBuilder inputBuilder = new ConfigureNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId(nodeId);
        List<Domain> domainList = new ArrayList<Domain>();
        inputBuilder.setDomain(domainList);

        RpcResult<ConfigureNodeOutput> output = bierConfigImpl.configureNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureNodeOutput configureNode2(int domainId,int subDomainId,String nodeId) throws Exception {
        ConfigureNodeInputBuilder inputBuilder = new ConfigureNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId(nodeId);
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(domainId));
        domainBuilder.setKey(new DomainKey(new DomainId(domainId)));

        BierGlobalBuilder bierBuilder = new BierGlobalBuilder();
        bierBuilder.setEncapsulationType(BierEncapsulationMpls.class);
        domainBuilder.setBierGlobal(bierBuilder.build());
        List<Domain> domainList = new ArrayList<Domain>();
        domainList.add(domainBuilder.build());
        inputBuilder.setDomain(domainList);

        RpcResult<ConfigureNodeOutput> output = bierConfigImpl.configureNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureNodeOutput configureNode3(int domainId,int subDomainId,String nodeId) throws Exception {
        ConfigureNodeInputBuilder inputBuilder = new ConfigureNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId(nodeId);
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(domainId));
        domainBuilder.setKey(new DomainKey(new DomainId(domainId)));

        BierGlobalBuilder bierBuilder = new BierGlobalBuilder();
        bierBuilder.setEncapsulationType(BierEncapsulationMpls.class);
        bierBuilder.setBitstringlength(Bsl._64Bit);
        domainBuilder.setBierGlobal(bierBuilder.build());
        List<Domain> domainList = new ArrayList<Domain>();
        domainList.add(domainBuilder.build());
        inputBuilder.setDomain(domainList);

        RpcResult<ConfigureNodeOutput> output = bierConfigImpl.configureNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureNodeOutput configureNode4(int domainId,int subDomainId,String nodeId) throws Exception {
        ConfigureNodeInputBuilder inputBuilder = new ConfigureNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId(nodeId);
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(domainId));
        domainBuilder.setKey(new DomainKey(new DomainId(domainId)));

        BierGlobalBuilder bierBuilder = new BierGlobalBuilder();
        bierBuilder.setBfrId(new BfrId(1));
        bierBuilder.setEncapsulationType(BierEncapsulationMpls.class);
        bierBuilder.setBitstringlength(Bsl._64Bit);
        domainBuilder.setBierGlobal(bierBuilder.build());
        List<Domain> domainList = new ArrayList<Domain>();
        domainList.add(domainBuilder.build());
        inputBuilder.setDomain(domainList);

        RpcResult<ConfigureNodeOutput> output = bierConfigImpl.configureNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureNodeOutput configureNode5(int domainId,int subDomainId,String nodeId) throws Exception {
        ConfigureNodeInputBuilder inputBuilder = new ConfigureNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId(nodeId);
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(domainId));
        domainBuilder.setKey(new DomainKey(new DomainId(domainId)));

        SubDomainBuilder subDomainBuilder = new SubDomainBuilder();
        subDomainBuilder.setSubDomainId(new SubDomainId(subDomainId));
        subDomainBuilder.setBfrId(new BfrId(1));
        List<SubDomain> subDomainList = new ArrayList<SubDomain>();
        subDomainList.add(subDomainBuilder.build());

        BierGlobalBuilder bierBuilder = new BierGlobalBuilder();
        bierBuilder.setSubDomain(subDomainList);
        bierBuilder.setBfrId(new BfrId(1));
        bierBuilder.setEncapsulationType(BierEncapsulationMpls.class);
        bierBuilder.setBitstringlength(Bsl._64Bit);
        bierBuilder.setIpv4BfrPrefix(new Ipv4Prefix("10.41.41.41/22"));
        domainBuilder.setBierGlobal(bierBuilder.build());
        List<Domain> domainList = new ArrayList<Domain>();
        domainList.add(domainBuilder.build());
        inputBuilder.setDomain(domainList);

        RpcResult<ConfigureNodeOutput> output = bierConfigImpl.configureNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureNodeOutput configureNode6(int domainId,int subDomainId,String nodeId) throws Exception {
        ConfigureNodeInputBuilder inputBuilder = new ConfigureNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId(nodeId);
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(domainId));
        domainBuilder.setKey(new DomainKey(new DomainId(domainId)));

        SubDomainBuilder subDomainBuilder = new SubDomainBuilder();
        subDomainBuilder.setSubDomainId(new SubDomainId(subDomainId));
        subDomainBuilder.setBfrId(new BfrId(1));
        subDomainBuilder.setIgpType(IgpType.OSPF);
        List<SubDomain> subDomainList = new ArrayList<SubDomain>();
        subDomainList.add(subDomainBuilder.build());

        BierGlobalBuilder bierBuilder = new BierGlobalBuilder();
        bierBuilder.setSubDomain(subDomainList);
        bierBuilder.setBfrId(new BfrId(1));
        bierBuilder.setEncapsulationType(BierEncapsulationMpls.class);
        bierBuilder.setBitstringlength(Bsl._64Bit);
        bierBuilder.setIpv4BfrPrefix(new Ipv4Prefix("10.41.41.41/22"));
        domainBuilder.setBierGlobal(bierBuilder.build());
        List<Domain> domainList = new ArrayList<Domain>();
        domainList.add(domainBuilder.build());
        inputBuilder.setDomain(domainList);

        RpcResult<ConfigureNodeOutput> output = bierConfigImpl.configureNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureNodeOutput configureNode7(int domainId,int subDomainId,String nodeId) throws Exception {
        ConfigureNodeInputBuilder inputBuilder = new ConfigureNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId(nodeId);
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(domainId));
        domainBuilder.setKey(new DomainKey(new DomainId(domainId)));

        SubDomainBuilder subDomainBuilder = new SubDomainBuilder();
        subDomainBuilder.setSubDomainId(new SubDomainId(subDomainId));
        subDomainBuilder.setBfrId(new BfrId(1));
        subDomainBuilder.setIgpType(IgpType.OSPF);
        AfBuilder afBuilder = new AfBuilder();
        subDomainBuilder.setAf(afBuilder.build());
        List<SubDomain> subDomainList = new ArrayList<SubDomain>();
        subDomainList.add(subDomainBuilder.build());

        BierGlobalBuilder bierBuilder = new BierGlobalBuilder();
        bierBuilder.setSubDomain(subDomainList);
        bierBuilder.setBfrId(new BfrId(1));
        bierBuilder.setEncapsulationType(BierEncapsulationMpls.class);
        bierBuilder.setBitstringlength(Bsl._64Bit);
        bierBuilder.setIpv4BfrPrefix(new Ipv4Prefix("10.41.41.41/22"));
        domainBuilder.setBierGlobal(bierBuilder.build());
        List<Domain> domainList = new ArrayList<Domain>();
        domainList.add(domainBuilder.build());
        inputBuilder.setDomain(domainList);

        RpcResult<ConfigureNodeOutput> output = bierConfigImpl.configureNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureTeNodeOutput configureTeNode(int domainId,int subDomainId,Bsl bsl,Si si,String tpId,
                                                  int bitposition,String nodeId) throws Exception {
        ConfigureTeNodeInputBuilder inputBuilder = new ConfigureTeNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId(nodeId);

        TeBpBuilder teBpBuilder = new TeBpBuilder();
        teBpBuilder.setBitposition(bitposition);
        teBpBuilder.setTpId(tpId);
        teBpBuilder.setKey(new TeBpKey(tpId));
        List<TeBp> teBpList = new ArrayList<TeBp>();
        teBpList.add(teBpBuilder.build());

        TeSiBuilder teSiBuilder = new TeSiBuilder();
        teSiBuilder.setSi(si);
        teSiBuilder.setKey(new TeSiKey(si));
        teSiBuilder.setTeBp(teBpList);
        List<TeSi> teSiList = new ArrayList<TeSi>();
        teSiList.add(teSiBuilder.build());

        TeBslBuilder teBslBuilder = new TeBslBuilder();
        teBslBuilder.setBitstringlength(bsl);
        teBslBuilder.setKey(new TeBslKey(bsl));
        teBslBuilder.setTeSi(teSiList);
        List<TeBsl> teBslList = new ArrayList<>();
        teBslList.add(teBslBuilder.build());

        TeSubDomainBuilder subDomainBuilder = new TeSubDomainBuilder();
        subDomainBuilder.setSubDomainId(new SubDomainId(subDomainId));
        subDomainBuilder.setKey(new TeSubDomainKey(new SubDomainId(subDomainId)));
        subDomainBuilder.setTeBsl(teBslList);
        List<TeSubDomain> subDomainList = new ArrayList<TeSubDomain>();
        subDomainList.add(subDomainBuilder.build());

        TeDomainBuilder domainBuilder = new TeDomainBuilder();
        domainBuilder.setDomainId(new DomainId(domainId));
        domainBuilder.setKey(new TeDomainKey(new DomainId(domainId)));
        domainBuilder.setTeSubDomain(subDomainList);
        List<TeDomain> domainList = new ArrayList<TeDomain>();
        domainList.add(domainBuilder.build());

        inputBuilder.setTeDomain(domainList);
        RpcResult<ConfigureTeNodeOutput> output = bierTeConfigImpl.configureTeNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureTeSubdomainOutput configureTeSubDomain(
            String nodeId, int domainId, int subDomainId) throws Exception {
        ConfigureTeSubdomainInputBuilder  configureTeSubdomainInputBuilder = new ConfigureTeSubdomainInputBuilder();
        configureTeSubdomainInputBuilder.setTopologyId("example-linkstate-topology");
        configureTeSubdomainInputBuilder.setNodeId(nodeId);
        configureTeSubdomainInputBuilder.setDomainId(new DomainId(domainId));
        configureTeSubdomainInputBuilder.setSubDomainId(new SubDomainId(subDomainId));

        RpcResult<ConfigureTeSubdomainOutput> output = bierTeConfigImpl.configureTeSubdomain(
                configureTeSubdomainInputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureTeLabelOutput configureTeLabel(MplsLabel labelBase,BierTeLabelRangeSize labelRangeSize,
                                                    String nodeId) throws Exception {
        ConfigureTeLabelInputBuilder inputBuilder = new ConfigureTeLabelInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId(nodeId);
        inputBuilder.setLabelBase(labelBase);
        inputBuilder.setLabelRangeSize(labelRangeSize);

        RpcResult<ConfigureTeLabelOutput> output = bierTeConfigImpl.configureTeLabel(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureNodeOutput modifyNodeDomain() throws Exception {
        ConfigureNodeInputBuilder inputBuilder = new ConfigureNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId("1");
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(2));
        domainBuilder.setKey(new DomainKey(new DomainId(2)));
        BierGlobalBuilder bierBuilder = new BierGlobalBuilder();
        bierBuilder.setEncapsulationType(BierEncapsulationMpls.class);
        bierBuilder.setBitstringlength(Bsl._64Bit);
        bierBuilder.setBfrId(new BfrId(1));
        bierBuilder.setIpv4BfrPrefix(new Ipv4Prefix("10.41.41.41/22"));
        domainBuilder.setBierGlobal(bierBuilder.build());
        List<Domain> domainList = new ArrayList<Domain>();
        domainList.add(domainBuilder.build());
        inputBuilder.setDomain(domainList);

        RpcResult<ConfigureNodeOutput> output = bierConfigImpl.configureNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureNodeOutput modifyNodeSubDomain() throws Exception {
        ConfigureNodeInputBuilder inputBuilder = new ConfigureNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setNodeId("1");
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(1));
        domainBuilder.setKey(new DomainKey(new DomainId(1)));

        SubDomainBuilder subDomainBuilder = new SubDomainBuilder();
        subDomainBuilder.setSubDomainId(new SubDomainId(2));
        subDomainBuilder.setBfrId(new BfrId(1));
        subDomainBuilder.setIgpType(IgpType.OSPF);

        Ipv4Builder ipv4Builder = new Ipv4Builder();
        ipv4Builder.setBitstringlength(64);
        ipv4Builder.setBierMplsLabelBase(new MplsLabel(20L));
        ipv4Builder.setBierMplsLabelRangeSize(new BierMplsLabelRangeSize((short)4));
        List<Ipv4> ipv4List = new ArrayList<Ipv4>();
        ipv4List.add(ipv4Builder.build());
        AfBuilder afBuilder = new AfBuilder();
        afBuilder.setIpv4(ipv4List);
        subDomainBuilder.setAf(afBuilder.build());
        List<SubDomain> subDomainList = new ArrayList<SubDomain>();
        subDomainList.add(subDomainBuilder.build());
        BierGlobalBuilder bierBuilder = new BierGlobalBuilder();
        bierBuilder.setSubDomain(subDomainList);
        domainBuilder.setBierGlobal(bierBuilder.build());
        List<Domain> domainList = new ArrayList<Domain>();
        domainList.add(domainBuilder.build());
        inputBuilder.setDomain(domainList);

        RpcResult<ConfigureNodeOutput> output = bierConfigImpl.configureNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private QuerySubdomainNodeOutput querySubdomainNode(int domainId,int subDomainId) throws Exception {
        QuerySubdomainNodeInputBuilder inputBuilder = new QuerySubdomainNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        RpcResult<QuerySubdomainNodeOutput> output = topoImpl
                .querySubdomainNode(inputBuilder.build()).get();
        return output.getResult();
    }

    private QueryTeSubdomainNodeOutput queryTeSubdomainNode(int domainId,int subDomainId) throws Exception {
        QueryTeSubdomainNodeInputBuilder inputBuilder = new QueryTeSubdomainNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        RpcResult<QueryTeSubdomainNodeOutput> output = topoImpl
                .queryTeSubdomainNode(inputBuilder.build()).get();
        return output.getResult();
    }

    private QuerySubdomainLinkOutput querySubdomainLink(int domainId,int subDomainId) throws Exception {
        QuerySubdomainLinkInputBuilder inputBuilder = new QuerySubdomainLinkInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        RpcResult<QuerySubdomainLinkOutput> output = topoImpl
                .querySubdomainLink(inputBuilder.build()).get();
        return output.getResult();
    }

    private QueryTeSubdomainLinkOutput queryTeSubdomainLink(int domainId,int subDomainId) throws Exception {
        QueryTeSubdomainLinkInputBuilder inputBuilder = new QueryTeSubdomainLinkInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        RpcResult<QueryTeSubdomainLinkOutput> output = topoImpl
                .queryTeSubdomainLink(inputBuilder.build()).get();
        return output.getResult();
    }

    private DeleteNodeOutput deleteNode(int domainId,int subDomainId,String nodeId) throws Exception {
        DeleteNodeInputBuilder inputBuilder = new DeleteNodeInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        inputBuilder.setNodeId(nodeId);
        RpcResult<DeleteNodeOutput> output = bierConfigImpl.deleteNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private DeleteIpv4Output deleteIpv4(int domainId,int subDomainId,String nodeId,int bitStringLength,
            int mplsLabelBase,int mplsLabelRangeSize) throws Exception {
        DeleteIpv4InputBuilder inputBuilder = new DeleteIpv4InputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        inputBuilder.setNodeId(nodeId);
        org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.delete.ipv4.input.Ipv4Builder ipv4Builder =
                new org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.delete.ipv4.input.Ipv4Builder();
        ipv4Builder.setBitstringlength(bitStringLength);
        ipv4Builder.setBierMplsLabelBase(new MplsLabel((long)mplsLabelBase));
        ipv4Builder.setBierMplsLabelRangeSize(new BierMplsLabelRangeSize((short)mplsLabelRangeSize));
        inputBuilder.setIpv4(ipv4Builder.build());

        RpcResult<DeleteIpv4Output> output = bierConfigImpl.deleteIpv4(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private DeleteIpv6Output deleteIpv6(int domainId,int subDomainId,String nodeId,int bitStringLength,
            int mplsLabelBase,int mplsLabelRangeSize) throws Exception {
        DeleteIpv6InputBuilder inputBuilder = new DeleteIpv6InputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        inputBuilder.setNodeId(nodeId);
        org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.delete.ipv6.input.Ipv6Builder ipv6Builder =
                new org.opendaylight.yang.gen.v1.urn.bier.config.api.rev161102.delete.ipv6.input.Ipv6Builder();
        ipv6Builder.setBitstringlength(bitStringLength);
        ipv6Builder.setBierMplsLabelBase(new MplsLabel((long)mplsLabelBase));
        ipv6Builder.setBierMplsLabelRangeSize(new BierMplsLabelRangeSize((short)mplsLabelRangeSize));
        inputBuilder.setIpv6(ipv6Builder.build());

        RpcResult<DeleteIpv6Output> output = bierConfigImpl.deleteIpv6(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private DeleteTeSubdomainOutput deleteTeSubdomain(int domainId, int subDomainId, String nodeId) throws Exception {
        DeleteTeSubdomainInputBuilder inputBuilder = new DeleteTeSubdomainInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        inputBuilder.setNodeId(nodeId);

        RpcResult<DeleteTeSubdomainOutput> output = bierTeConfigImpl.deleteTeSubdomain(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private DeleteTeBslOutput deleteTeBsl(int domainId,int subDomainId,String nodeId,
                                          Bsl bitStringLength) throws Exception {
        DeleteTeBslInputBuilder inputBuilder = new DeleteTeBslInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        inputBuilder.setNodeId(nodeId);
        inputBuilder.setBitstringlength(bitStringLength);

        RpcResult<DeleteTeBslOutput> output = bierTeConfigImpl.deleteTeBsl(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private DeleteTeSiOutput deleteTeSi(int domainId, int subDomainId, String nodeId,
                                        Bsl bitStringLength,Si si) throws Exception {
        DeleteTeSiInputBuilder inputBuilder = new DeleteTeSiInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        inputBuilder.setNodeId(nodeId);
        inputBuilder.setBitstringlength(bitStringLength);
        inputBuilder.setSi(si);

        RpcResult<DeleteTeSiOutput> output = bierTeConfigImpl.deleteTeSi(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private DeleteTeBpOutput deleteTeBp(int domainId, int subDomainId, String nodeId,
                                        Bsl bitStringLength,Si si,String tpId) throws Exception {
        DeleteTeBpInputBuilder inputBuilder = new DeleteTeBpInputBuilder();
        inputBuilder.setTopologyId("example-linkstate-topology");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        inputBuilder.setNodeId(nodeId);
        inputBuilder.setBitstringlength(bitStringLength);
        inputBuilder.setSi(si);
        inputBuilder.setTpId(tpId);

        RpcResult<DeleteTeBpOutput> output = bierTeConfigImpl.deleteTeBp(
                inputBuilder.build()).get();
        return output.getResult();
    }
}