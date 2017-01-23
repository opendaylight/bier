/*
 * Copyright © 2016 www.bupt.edu.cn and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topomanager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureDomainInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureDomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureNodeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureNodeOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureSubdomainInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.ConfigureSubdomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteDomainInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteDomainOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteIpv4InputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteIpv4Output;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteIpv6InputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteIpv6Output;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteNodeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.DeleteNodeOutput;
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
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.QueryTopologyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierMplsLabelRangeSize;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobalBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.AfBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;
//import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;



public class BierTopologyImplTest extends AbstractDataBrokerTest {
    private BierTopologyManager topoManager;
    private BierTopologyServiceImpl topoImpl;

    @Before
    public void setUp() throws Exception {
        initOpenflowTopo(getDataBroker());
        topoManager = new BierTopologyManager(getDataBroker());
        topoImpl = new BierTopologyServiceImpl(topoManager);
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
                .equals("flow:1"));
    }

    @Test
    public void queryTopologyTest() throws Exception {
        QueryTopologyInputBuilder inputBuilder = new QueryTopologyInputBuilder();
        inputBuilder.setTopologyId("flow:1");
        RpcResult<QueryTopologyOutput> output = topoImpl.queryTopology(
                inputBuilder.build()).get();
        QueryTopologyOutputBuilder outputBuilder = new QueryTopologyOutputBuilder(
                output.getResult());
        Assert.assertTrue(outputBuilder.getTopologyId().equals("flow:1"));
        Assert.assertTrue(outputBuilder.getNodeId().size() == 2);
        Assert.assertTrue(outputBuilder.getNodeId().get(0).getNodeId()
                .equals("1") || outputBuilder.getNodeId().get(0).getNodeId()
                .equals("2"));
        Assert.assertTrue(outputBuilder.getNodeId().get(1).getNodeId()
                .equals("2") || outputBuilder.getNodeId().get(1).getNodeId()
                .equals("1"));
        Assert.assertTrue(outputBuilder.getLinkId().size() == 1);
        Assert.assertTrue(outputBuilder.getLinkId().get(0).getLinkId()
                .equals("1"));
    }

    @Test
    public void queryNodeTest() throws Exception {
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
                .get(0).getTpId().equals("1-1"));
    }

    @Test
    public void queryLinkTest() throws Exception {
        QueryLinkInputBuilder inputBuilder = new QueryLinkInputBuilder();
        inputBuilder.setTopologyId("flow:1");
        List<String> linkIdList = new ArrayList<String>();
        linkIdList.add("1");
        inputBuilder.setLink(linkIdList);
        RpcResult<QueryLinkOutput> output = topoImpl.queryLink(
                inputBuilder.build()).get();
        QueryLinkOutputBuilder outputBuilder = new QueryLinkOutputBuilder(
                output.getResult());
        Assert.assertTrue(outputBuilder.getLink().size() == 1);
        Assert.assertTrue(outputBuilder.getLink().get(0).getLinkId()
                .equals("1"));
        Assert.assertTrue(outputBuilder.getLink().get(0).getLinkSource()
                .getSourceNode().equals("1"));
        Assert.assertTrue(outputBuilder.getLink().get(0).getLinkSource()
                .getSourceTp().equals("1-1"));
        Assert.assertTrue(outputBuilder.getLink().get(0).getLinkDest()
                .getDestNode().equals("2"));
        Assert.assertTrue(outputBuilder.getLink().get(0).getLinkDest()
                .getDestTp().equals("2-1"));
        Assert.assertTrue(outputBuilder.getLink().get(0).getLoss().intValue() == 0);
        Assert.assertTrue(outputBuilder.getLink().get(0).getDelay().intValue() == 0);
    }

    @Test
    public void configDomainTest() throws Exception {
        ConfigureDomainOutput output = configureDomain(1);
        Assert.assertTrue(output.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        ConfigureDomainOutput output2 = configureDomain(2);
        Assert.assertTrue(output2.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryDomainOutput domainOutput = queryDomain("flow:1");
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

        QuerySubdomainOutput queryOutput = querySubdomain("flow:1",
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
    public void deleteNodeTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureNode(1,1,"1");
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
                .getBierMplsLabelBase().getValue() == (long)1);
        Assert.assertTrue(queryOutput.getNode().get(0).getBierNodeParams()
                .getDomain().get(0).getBierGlobal().getSubDomain().get(0).getAf().getIpv6().get(0)
                .getBierMplsLabelRangeSize().getValue() == (short)4);

        DeleteIpv6Output output = deleteIpv6(1,1,"1",64,1,4);
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
    public void deleteSubdomainTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureSubdomain(1,2);
        QuerySubdomainOutput queryOutput = querySubdomain("flow:1",
                new DomainId(1));
        Assert.assertTrue(queryOutput.getSubdomain().size() == 2);
        Assert.assertTrue(queryOutput.getSubdomain().get(0).getSubDomainId()
                .getValue().intValue() == 1);
        Assert.assertTrue(queryOutput.getSubdomain().get(1).getSubDomainId()
                .getValue().intValue() == 2);

        DeleteSubdomainInputBuilder inputBuilder = new DeleteSubdomainInputBuilder();
        inputBuilder.setTopologyId("flow:1");
        inputBuilder.setDomainId(new DomainId(1));
        inputBuilder.setSubDomainId(new SubDomainId(1));
        RpcResult<DeleteSubdomainOutput> output = topoImpl.deleteSubdomain(
                inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QuerySubdomainOutput queryOutput2 = querySubdomain("flow:1",
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
        inputBuilder.setTopologyId("flow:1");
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
        QueryDomainOutput domainOutput = queryDomain("flow:1");
        Assert.assertTrue(domainOutput.getDomain().size() == 2);
        Assert.assertTrue(domainOutput.getDomain().get(0).getDomainId()
                .getValue().intValue() == 1);
        Assert.assertTrue(domainOutput.getDomain().get(1).getDomainId()
                .getValue().intValue() == 2);

        DeleteDomainInputBuilder inputBuilder = new DeleteDomainInputBuilder();
        inputBuilder.setTopologyId("flow:1");
        inputBuilder.setDomainId(new DomainId(1));
        RpcResult<DeleteDomainOutput> output = topoImpl.deleteDomain(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        QueryDomainOutput domainOutput2 = queryDomain("flow:1");
        Assert.assertTrue(domainOutput2.getDomain().size() == 1);
        Assert.assertTrue(domainOutput2.getDomain().get(0).getDomainId()
                .getValue().intValue() == 2);
    }

    @Test
    public void deleteDomainTest2() throws Exception {
        configureDomain(1);

        DeleteDomainInputBuilder inputBuilder = new DeleteDomainInputBuilder();
        inputBuilder.setTopologyId("flow:1");
        inputBuilder.setDomainId(new DomainId(3));
        RpcResult<DeleteDomainOutput> output = topoImpl.deleteDomain(inputBuilder.build()).get();
        Assert.assertTrue(output.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output.getResult().getConfigureResult().getErrorCause().equals("domain is not exist!"));
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
    public void querySubDomainLinkTest() throws Exception {
        configureDomain(1);
        configureSubdomain(1,1);
        configureNode(1,1,"1");

        QuerySubdomainLinkOutput output = querySubdomainLink(1,1);
        Assert.assertTrue(output.getSubdomainLink().size() == 0);
    }

    private QueryNodeOutput queryNode() throws Exception {
        QueryNodeInputBuilder inputBuilder = new QueryNodeInputBuilder();
        inputBuilder.setTopologyId("flow:1");
        List<String> nodeIdList = new ArrayList<String>();
        nodeIdList.add("1");
        inputBuilder.setNode(nodeIdList);
        RpcResult<QueryNodeOutput> output = topoImpl.queryNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureDomainOutput configureDomain(int domainId) throws Exception {
        ConfigureDomainInputBuilder inputBuilder = new ConfigureDomainInputBuilder();
        inputBuilder.setTopologyId("flow:1");
        List<DomainId> domainIdList = new ArrayList<DomainId>();
        domainIdList.add(new DomainId(domainId));
        //domainIdList.add(new DomainId(2));
        inputBuilder.setDomain(domainIdList);
        RpcResult<ConfigureDomainOutput> output = topoImpl.configureDomain(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureSubdomainOutput configureSubdomain(int domainId,int subDomainId) throws Exception {
        ConfigureSubdomainInputBuilder inputBuilder = new ConfigureSubdomainInputBuilder();
        inputBuilder.setTopologyId("flow:1");
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
        inputBuilder.setTopologyId("flow:1");
        inputBuilder.setNodeId(nodeId);
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(domainId));
        domainBuilder.setKey(new DomainKey(new DomainId(subDomainId)));

        SubDomainBuilder subDomainBuilder = new SubDomainBuilder();
        subDomainBuilder.setSubDomainId(new SubDomainId(1));

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
        ipv6Builder.setBierMplsLabelBase(new MplsLabel(1L));
        ipv6Builder.setBierMplsLabelRangeSize(new BierMplsLabelRangeSize((short)4));
        List<Ipv6> ipv6List = new ArrayList<Ipv6>();
        ipv6List.add(ipv6Builder.build());
        afBuilder.setIpv6(ipv6List);

        subDomainBuilder.setAf(afBuilder.build());
        List<SubDomain> subDomainList = new ArrayList<SubDomain>();
        subDomainList.add(subDomainBuilder.build());

        BierGlobalBuilder bierBuilder = new BierGlobalBuilder();
        bierBuilder.setSubDomain(subDomainList);
        domainBuilder.setBierGlobal(bierBuilder.build());
        List<Domain> domainList = new ArrayList<Domain>();
        domainList.add(domainBuilder.build());
        inputBuilder.setDomain(domainList);

        RpcResult<ConfigureNodeOutput> output = topoImpl.configureNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureNodeOutput modifyNodeDomain() throws Exception {
        ConfigureNodeInputBuilder inputBuilder = new ConfigureNodeInputBuilder();
        inputBuilder.setTopologyId("flow:1");
        inputBuilder.setNodeId("1");
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(2));
        domainBuilder.setKey(new DomainKey(new DomainId(2)));
        BierGlobalBuilder bierBuilder = new BierGlobalBuilder();
        domainBuilder.setBierGlobal(bierBuilder.build());
        List<Domain> domainList = new ArrayList<Domain>();
        domainList.add(domainBuilder.build());
        inputBuilder.setDomain(domainList);

        RpcResult<ConfigureNodeOutput> output = topoImpl.configureNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private ConfigureNodeOutput modifyNodeSubDomain() throws Exception {
        ConfigureNodeInputBuilder inputBuilder = new ConfigureNodeInputBuilder();
        inputBuilder.setTopologyId("flow:1");
        inputBuilder.setNodeId("1");
        DomainBuilder domainBuilder = new DomainBuilder();
        domainBuilder.setDomainId(new DomainId(1));
        domainBuilder.setKey(new DomainKey(new DomainId(1)));
        BierGlobalBuilder bierBuilder = new BierGlobalBuilder();
        List<SubDomain> subDomainList = new ArrayList<SubDomain>();
        SubDomainBuilder subDomainBuilder = new SubDomainBuilder();
        subDomainBuilder.setSubDomainId(new SubDomainId(2));
        subDomainList.add(subDomainBuilder.build());
        bierBuilder.setSubDomain(subDomainList);
        domainBuilder.setBierGlobal(bierBuilder.build());
        List<Domain> domainList = new ArrayList<Domain>();
        domainList.add(domainBuilder.build());
        inputBuilder.setDomain(domainList);

        RpcResult<ConfigureNodeOutput> output = topoImpl.configureNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private QuerySubdomainNodeOutput querySubdomainNode(int domainId,int subDomainId) throws Exception {
        QuerySubdomainNodeInputBuilder inputBuilder = new QuerySubdomainNodeInputBuilder();
        inputBuilder.setTopologyId("flow:1");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        RpcResult<QuerySubdomainNodeOutput> output = topoImpl
                .querySubdomainNode(inputBuilder.build()).get();
        return output.getResult();
    }

    private QuerySubdomainLinkOutput querySubdomainLink(int domainId,int subDomainId) throws Exception {
        QuerySubdomainLinkInputBuilder inputBuilder = new QuerySubdomainLinkInputBuilder();
        inputBuilder.setTopologyId("flow:1");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        RpcResult<QuerySubdomainLinkOutput> output = topoImpl
                .querySubdomainLink(inputBuilder.build()).get();
        return output.getResult();
    }

    private DeleteNodeOutput deleteNode(int domainId,int subDomainId,String nodeId) throws Exception {
        DeleteNodeInputBuilder inputBuilder = new DeleteNodeInputBuilder();
        inputBuilder.setTopologyId("flow:1");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        inputBuilder.setNodeId(nodeId);
        RpcResult<DeleteNodeOutput> output = topoImpl.deleteNode(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private DeleteIpv4Output deleteIpv4(int domainId,int subDomainId,String nodeId,int bitStringLength,
            int mplsLabelBase,int mplsLabelRangeSize) throws Exception {
        DeleteIpv4InputBuilder inputBuilder = new DeleteIpv4InputBuilder();
        inputBuilder.setTopologyId("flow:1");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        inputBuilder.setNodeId(nodeId);
        org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.delete.ipv4.input.Ipv4Builder ipv4Builder =
                new org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.delete.ipv4.input.Ipv4Builder();
        ipv4Builder.setBitstringlength(bitStringLength);
        ipv4Builder.setBierMplsLabelBase(new MplsLabel((long)mplsLabelBase));
        ipv4Builder.setBierMplsLabelRangeSize(new BierMplsLabelRangeSize((short)mplsLabelRangeSize));
        inputBuilder.setIpv4(ipv4Builder.build());

        RpcResult<DeleteIpv4Output> output = topoImpl.deleteIpv4(
                inputBuilder.build()).get();
        return output.getResult();
    }

    private DeleteIpv6Output deleteIpv6(int domainId,int subDomainId,String nodeId,int bitStringLength,
            int mplsLabelBase,int mplsLabelRangeSize) throws Exception {
        DeleteIpv6InputBuilder inputBuilder = new DeleteIpv6InputBuilder();
        inputBuilder.setTopologyId("flow:1");
        inputBuilder.setDomainId(new DomainId(domainId));
        inputBuilder.setSubDomainId(new SubDomainId(subDomainId));
        inputBuilder.setNodeId(nodeId);
        org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.delete.ipv6.input.Ipv6Builder ipv6Builder =
                new org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.delete.ipv6.input.Ipv6Builder();
        ipv6Builder.setBitstringlength(bitStringLength);
        ipv6Builder.setBierMplsLabelBase(new MplsLabel((long)mplsLabelBase));
        ipv6Builder.setBierMplsLabelRangeSize(new BierMplsLabelRangeSize((short)mplsLabelRangeSize));
        inputBuilder.setIpv6(ipv6Builder.build());

        RpcResult<DeleteIpv6Output> output = topoImpl.deleteIpv6(
                inputBuilder.build()).get();
        return output.getResult();
    }


    private void initOpenflowTopo(DataBroker dataBroker) {
        Topology topology = constructTopology();

        // write to openflow datastore
        final TopologyKey key = new TopologyKey(new TopologyId("flow:1"));
        final InstanceIdentifier<Topology> path = InstanceIdentifier.create(
                NetworkTopology.class).child(Topology.class, key);

        final ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, path, topology, true);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            // LOG.warn("Initial topology export failed, continuing anyway", e);
        }
    }

    private Topology constructTopology() {
        TopologyBuilder topoBuilder = new TopologyBuilder();

        String topoId = "flow:1";
        TopologyId topologyId = new TopologyId(topoId);
        TopologyKey topoKey = new TopologyKey(topologyId);
        topoBuilder.setKey(topoKey);
        topoBuilder.setTopologyId(topologyId);

        NodeBuilder nodeBuilder = new NodeBuilder();
        NodeId nodeId = new NodeId("1");
        NodeKey nodeKey = new NodeKey(nodeId);
        nodeBuilder.setNodeId(nodeId);
        nodeBuilder.setKey(nodeKey);
        List<TerminationPoint> tpList = new ArrayList<TerminationPoint>();
        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        TpId tpId = new TpId("1-1");
        TerminationPointKey tpKey = new TerminationPointKey(tpId);
        tpBuilder.setKey(tpKey);
        tpBuilder.setTpId(tpId);
        tpList.add(tpBuilder.build());
        nodeBuilder.setTerminationPoint(tpList);
        List<Node> nodeList = new ArrayList<Node>();
        nodeList.add(nodeBuilder.build());

        NodeBuilder nodeBuilder2 = new NodeBuilder();
        NodeId nodeId2 = new NodeId("2");
        NodeKey nodeKey2 = new NodeKey(nodeId2);
        nodeBuilder2.setNodeId(nodeId2);
        nodeBuilder2.setKey(nodeKey2);
        List<TerminationPoint> tpList2 = new ArrayList<TerminationPoint>();
        TerminationPointBuilder tpBuilder2 = new TerminationPointBuilder();
        TpId tpId2 = new TpId("2-1");
        TerminationPointKey tpKey2 = new TerminationPointKey(tpId2);
        tpBuilder2.setKey(tpKey2);
        tpBuilder2.setTpId(tpId2);
        tpList2.add(tpBuilder2.build());
        nodeBuilder2.setTerminationPoint(tpList2);
        nodeList.add(nodeBuilder2.build());
        topoBuilder.setNode(nodeList);

        LinkBuilder linkBuilder = new LinkBuilder();
        LinkId linkId = new LinkId("1");
        LinkKey linkKey = new LinkKey(linkId);
        linkBuilder.setKey(linkKey);
        linkBuilder.setLinkId(linkId);
        SourceBuilder sourceBuilder = new SourceBuilder();
        sourceBuilder.setSourceNode(new NodeId("1"));
        sourceBuilder.setSourceTp(new TpId("1-1"));
        linkBuilder.setSource(sourceBuilder.build());
        DestinationBuilder destBuilder = new DestinationBuilder();
        destBuilder.setDestNode(new NodeId("2"));
        destBuilder.setDestTp(new TpId("2-1"));
        linkBuilder.setDestination(destBuilder.build());
        List<Link> linkList = new ArrayList<Link>();
        linkList.add(linkBuilder.build());
        topoBuilder.setLink(linkList);

        return topoBuilder.build();
    }

    private Node constructNode(String strNodeId) {
        NodeBuilder nodeBuilder = new NodeBuilder();
        NodeId nodeId = new NodeId(strNodeId);
        NodeKey nodeKey = new NodeKey(nodeId);
        nodeBuilder.setNodeId(nodeId);
        nodeBuilder.setKey(nodeKey);
        return nodeBuilder.build();
    }

    private Link constructLink(String strLinkId, String strSourceNodeId,
                               String strSourceTpId, String strDestNodeId, String strDestTpId) {
        LinkBuilder linkBuilder = new LinkBuilder();
        LinkId linkId = new LinkId(strLinkId);
        LinkKey linkKey = new LinkKey(linkId);
        linkBuilder.setKey(linkKey);
        linkBuilder.setLinkId(linkId);
        SourceBuilder sourceBuilder = new SourceBuilder();
        sourceBuilder.setSourceNode(new NodeId(strSourceNodeId));
        sourceBuilder.setSourceTp(new TpId(strSourceTpId));
        linkBuilder.setSource(sourceBuilder.build());
        DestinationBuilder destBuilder = new DestinationBuilder();
        destBuilder.setDestNode(new NodeId(strDestNodeId));
        destBuilder.setDestTp(new TpId(strDestTpId));
        linkBuilder.setDestination(destBuilder.build());
        return linkBuilder.build();
    }
}
