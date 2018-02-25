/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.bier.pce.impl.biertepath.BierPathUnifyKey;
import org.opendaylight.bier.pce.impl.biertepath.BierTeInstance;
import org.opendaylight.bier.pce.impl.pathcore.BierTesRecordPerPort;
import org.opendaylight.bier.pce.impl.pathcore.PortKey;
import org.opendaylight.bier.pce.impl.topology.PathsRecordPerSubDomain;
import org.opendaylight.bier.pce.impl.topology.TopologyProvider;
import org.opendaylight.bier.pce.impl.util.TopoMockUtils;
import org.opendaylight.bier.pce.impl.util.Utils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryChannelThroughPortInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryChannelThroughPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryChannelThroughPortOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryTeFrrPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryTeFrrPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryTeFrrPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveTeFrrPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveTeFrrPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.TeFrrKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.te.frr.key.ProtectedLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.te.frr.key.ProtectedLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.bier.instance.path.output.Link;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.channel.through.port.output.RelatedChannel;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.te.frr.path.output.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.FrrPath;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.ExcludingLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.ExcludingLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.NextHopPath;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.NextNextHopPath;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.bier.te.frr.data.bier.te.frr.sub.domain.BierTeFrrLink;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstance;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.TeSubdomainAdd;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.TeSubdomainAddBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.TeSubdomainDelete;
import org.opendaylight.yang.gen.v1.urn.bier.te.config.api.rev161102.TeSubdomainDeleteBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yangtools.yang.common.RpcResult;


public class PcePathImplTest extends AbstractConcurrentDataBrokerTest {
    private DataBroker dataBroker;
    PcePathImpl pcePathProvider;

    TopologyProvider topologyProvider;

    @Before
    public void setUp() throws Exception {
        dataBroker = getDataBroker();
        DbProvider.getInstance().setDataBroker(dataBroker);
        pcePathProvider = PcePathImpl.getInstance();
        PcePathDb.getInstance().bierTeWriteDbRoot();

        topologyProvider = new TopologyProvider(dataBroker,null,null);
        topologyProvider.setExecutor(MoreExecutors.newDirectExecutorService());
        topologyProvider.setPcePathImpl(pcePathProvider);
    }

    @After
    public void tearDown() throws Exception {
        TopologyProvider.getInstance().destroy();
        pcePathProvider.destroy();
        BierTesRecordPerPort.getInstance().destroy();
        PathsRecordPerSubDomain.getInstance().destroy();
    }

    @Test
    public void createBierPathInputCheckTest() throws InterruptedException, ExecutionException {
        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setBfer(null)
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);

        assertTrue(!output.get().isSuccessful());
        assertEquals("Unlegal argument!",output.get().getErrors().iterator().next().getMessage());

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(null,bierTeInstance);
    }


    @Test
    public void createBierPathInputCheckWhenUpdateTest() throws InterruptedException, ExecutionException {
        List<BierLink> links = TopoMockUtils.buildFourNodeTopo();
        Utils.writeLinksToDB(links);
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build2BferInfo("2.2.2.2","4.4.4.4"))
                .setSaveCreateFail(false)
                .build();
        pcePathProvider.createBierPath(input);
        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(2,bierTeInstance.getAllBierPath().size());

        input = new CreateBierPathInputBuilder()
                .setBfirNodeId("2.2.2.2")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build2BferInfo("3.3.3.3","5.5.5.5"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);

        assertTrue(!output.get().isSuccessful());
        assertEquals("bfir NodeId is not equals, with the same channel-name!",
                output.get().getErrors().iterator().next().getMessage());

        bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(2,bierTeInstance.getAllBierPath().size());
    }

    @Test
    public void removeBierPathInputCheckTest() throws InterruptedException, ExecutionException {
        List<BierLink> links = TopoMockUtils.buildFourNodeTopo();
        Utils.writeLinksToDB(links);
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build2BferInfo("2.2.2.2","4.4.4.4"))
                .setSaveCreateFail(false)
                .build();
        pcePathProvider.createBierPath(input);
        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(2,bierTeInstance.getAllBierPath().size());

        RemoveBierPathInput removeInput = new RemoveBierPathInputBuilder()
                .setBfirNodeId("2.2.2.2")
                .setSubDomainId(new SubDomainId(1))
                .setChannelName("channel-1")
                .build();
        Future<RpcResult<RemoveBierPathOutput>> output = pcePathProvider.removeBierPath(removeInput);

        assertTrue(!output.get().isSuccessful());
        assertEquals("bfir NodeId is not equals, with the same channel-name!",
                output.get().getErrors().iterator().next().getMessage());

        bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(2,bierTeInstance.getAllBierPath().size());

        removeInput = new RemoveBierPathInputBuilder()
                .setBfirNodeId("2.2.2.2")
                .setSubDomainId(new SubDomainId(1))
                .build();
        output = pcePathProvider.removeBierPath(removeInput);

        assertTrue(output.get().isSuccessful());
        bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(2,bierTeInstance.getAllBierPath().size());
        removeBierInstance("channel-1","1.1.1.1",1);
    }

    @Test
    public void createBierPathFailWithNotSaveTest() throws InterruptedException, ExecutionException {
        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build2BferInfo("2.2.2.2","4.4.4.4"))
                .setSaveCreateFail(false)
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);

        List<Bfer> bfers = output.get().getResult().getBfer();
        for (Bfer bfer : bfers) {
            Utils.checkPathNull(bfer.getBierPath().getPathLink());
        }

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(null,bierTeInstance);
    }


    @Test
    public void createBierPathFailWithSaveTest() throws InterruptedException, ExecutionException {

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build2BferInfo("2.2.2.2","4.4.4.4"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);

        List<Bfer> bfers = output.get().getResult().getBfer();
        assertEquals(2,bfers.size());
        for (Bfer bfer : bfers) {
            Utils.checkPathNull(bfer.getBierPath().getPathLink());
        }

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertNotEquals(null,bierTeInstance);
        removeBierInstance("channel-1","1.1.1.1",1);
    }


    @Test
    public void createBierPathSuccessWithNotSaveTest() throws InterruptedException, ExecutionException {
        List<BierLink> links = TopoMockUtils.buildFourNodeTopo();
        Utils.writeLinksToDB(links);
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build2BferInfo("2.2.2.2","4.4.4.4"))
                .setSaveCreateFail(false)
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);

        List<Bfer> bfers = output.get().getResult().getBfer();
        assertEquals(2,bfers.size());
        for (Bfer bfer : bfers) {
            assertNotEquals(null,bfer.getBierPath().getPathLink());
            assertTrue(!bfer.getBierPath().getPathLink().isEmpty());
            if (bfer.getBferNodeId().equals("2.2.2.2")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","2.2.2.2");
            }
            if (bfer.getBferNodeId().equals("4.4.4.4")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","2.2.2.2","4.4.4.4");
            }
        }

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertNotEquals(null,bierTeInstance);
        assertTrue(bierTeInstance.getAllBierPath().size() == 2);
        removeBierInstance("channel-1","1.1.1.1",1);
    }


    @Test
    public void createBierPathSuccessWithSaveTest() throws InterruptedException, ExecutionException {
        List<BierLink> links = TopoMockUtils.buildFourNodeTopo();
        Utils.writeLinksToDB(links);
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build2BferInfo("3.3.3.3","4.4.4.4"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);

        List<Bfer> bfers = output.get().getResult().getBfer();
        assertEquals(2,bfers.size());
        for (Bfer bfer : bfers) {
            assertNotEquals(null,bfer.getBierPath().getPathLink());
            assertTrue(!bfer.getBierPath().getPathLink().isEmpty());
            if (bfer.getBferNodeId().equals("3.3.3.3")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","3.3.3.3");
            }
            if (bfer.getBferNodeId().equals("4.4.4.4")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","3.3.3.3","4.4.4.4");
            }
        }

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertNotEquals(bierTeInstance, null);
        assertTrue(bierTeInstance.getAllBierPath().size() == 2);

        BierTEInstance bierTEInstanceData = PcePathDb.getInstance().readBierInstance("channel-1");
        assertEquals("1.1.1.1",bierTEInstanceData.getBfirNodeId());
        assertEquals(2,bierTEInstanceData.getBfer().size());
        for (Bfer bfer : bierTEInstanceData.getBfer()) {
            for (Bfer bierPath : bfers) {
                if (bierPath.getBferNodeId().equals(bfer.getBferNodeId())) {
                    Utils.assertBierPathData(bierPath.getBierPath(),bfer.getBierPath());
                }
            }
        }
        removeBierInstance("channel-1","1.1.1.1",1);
    }



    @Test
    public void bierPathCalcWhenUpdateChannelInfoTests() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build2BferInfo("5.5.5.5","6.6.6.6"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);

        List<Bfer> bfers = output.get().getResult().getBfer();
        assertEquals(2,bfers.size());
        for (Bfer bfer : bfers) {
            assertNotEquals(null,bfer.getBierPath().getPathLink());
            assertTrue(!bfer.getBierPath().getPathLink().isEmpty());

            if (bfer.getBferNodeId().equals("5.5.5.5")) {
                assertEquals(2,bfer.getBierPath().getPathLink().size());
                assertEquals(20,bfer.getBierPath().getPathMetric().intValue());
            }
            if (bfer.getBferNodeId().equals("6.6.6.6")) {
                assertTrue(bfer.getBierPath().getPathLink()
                        .contains(new PathLinkBuilder(TopoMockUtils.buildLinkEx("5.5.5.5", "56.56.56.56",
                                "65.65.65.65","6.6.6.6",10)).build()));
                assertEquals(30,bfer.getBierPath().getPathMetric().intValue());
            }
        }

        BierTEInstance bierTEInstanceData = PcePathDb.getInstance().readBierInstance("channel-1");
        assertEquals("1.1.1.1",bierTEInstanceData.getBfirNodeId());
        assertEquals(2,bierTEInstanceData.getBfer().size());
        for (Bfer bfer : bierTEInstanceData.getBfer()) {
            for (Bfer bierPath : bfers) {
                if (bierPath.getBferNodeId().equals(bfer.getBferNodeId())) {
                    Utils.assertBierPathData(bierPath.getBierPath(),bfer.getBierPath());
                }
            }
        }

        input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build2BferInfo("4.4.4.4","3.3.3.3"))
                .build();
        output = pcePathProvider.createBierPath(input);

        bfers = output.get().getResult().getBfer();
        assertEquals(4,bfers.size());
        for (Bfer bfer : bfers) {
            assertNotEquals(null,bfer.getBierPath().getPathLink());
            assertTrue(!bfer.getBierPath().getPathLink().isEmpty());
            if (bfer.getBferNodeId().equals("3.3.3.3")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","2.2.2.2","3.3.3.3");
                assertEquals(20,bfer.getBierPath().getPathMetric().intValue());
            }
            if (bfer.getBferNodeId().equals("4.4.4.4")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","4.4.4.4");
                assertEquals(10,bfer.getBierPath().getPathMetric().intValue());
            }
            if (bfer.getBferNodeId().equals("5.5.5.5")) {
                assertEquals(2,bfer.getBierPath().getPathLink().size());
                assertEquals(20,bfer.getBierPath().getPathMetric().intValue());
            }
            if (bfer.getBferNodeId().equals("6.6.6.6")) {
                assertTrue(bfer.getBierPath().getPathLink()
                        .contains(new PathLinkBuilder(TopoMockUtils.buildLinkEx("5.5.5.5","56.56.56.56",
                                "65.65.65.65","6.6.6.6",10)).build()));
                assertEquals(30,bfer.getBierPath().getPathMetric().intValue());
            }
        }

        bierTEInstanceData = PcePathDb.getInstance().readBierInstance("channel-1");
        assertEquals("1.1.1.1",bierTEInstanceData.getBfirNodeId());
        assertEquals(4,bierTEInstanceData.getBfer().size());
        for (Bfer bfer : bierTEInstanceData.getBfer()) {
            for (Bfer bierPath : bfers) {
                if (bierPath.getBferNodeId().equals(bfer.getBferNodeId())) {
                    Utils.assertBierPathData(bierPath.getBierPath(),bfer.getBierPath());
                }
            }
        }
        removeBierInstance("channel-1","1.1.1.1",1);
    }

    @Test
    public void removeAllBierPathTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build3BferInfo("4.4.4.4","5.5.5.5","6.6.6.6"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());

        RemoveBierPathInput removeInput = new RemoveBierPathInputBuilder()
                .setChannelName("channel-2")
                .setSubDomainId(new SubDomainId(1))
                .build();
        Future<RpcResult<RemoveBierPathOutput>> removeOutput = pcePathProvider.removeBierPath(removeInput);
        assertTrue(removeOutput.get().isSuccessful());
        assertEquals(null,removeOutput.get().getResult().getBfer());

        removeInput = new RemoveBierPathInputBuilder()
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfirNodeId("1.1.1.1")
                .build();
        removeOutput = pcePathProvider.removeBierPath(removeInput);
        assertTrue(removeOutput.get().isSuccessful());
        assertEquals(null,removeOutput.get().getResult().getBfer());

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(null,bierTeInstance);

        BierTEInstance bierTEInstanceData = PcePathDb.getInstance().readBierInstance("channel-1");
        assertEquals(null,bierTEInstanceData);

        QueryBierInstancePathInput queryBierTe = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-1").build();
        QueryBierInstancePathOutput queryOutput = pcePathProvider.queryBierInstancePath(queryBierTe).get().getResult();
        List<Link> links = queryOutput.getLink();
        assertTrue(links == null);

    }

    @Test
    public void removeSpecifiedBierPathTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build3BferInfo("4.4.4.4","5.5.5.5","6.6.6.6"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());

        QueryBierInstancePathInput queryBierTe = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-1").build();
        QueryBierInstancePathOutput queryOutput = pcePathProvider.queryBierInstancePath(queryBierTe).get().getResult();
        List<Link> links = queryOutput.getLink();
        BierLink link14 = TopoMockUtils.buildLink("1.1.1.1","14.14.14.14","41.41.41.41","4.4.4.4",10);
        BierLink link45 = TopoMockUtils.buildLink("4.4.4.4","45.45.45.45","54.54.54.54","5.5.5.5",10);
        BierLink link56 = TopoMockUtils.buildLink("5.5.5.5","56.56.56.56","65.65.65.65","6.6.6.6",10);
        Utils.checkLinkId(links,link14,link45,link56);

        List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input
                .Bfer> bferList = new ArrayList<>();
        bferList.add(new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input
                .BferBuilder().setBferNodeId("5.5.5.5").build());
        RemoveBierPathInput removeInput = new RemoveBierPathInputBuilder()
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfirNodeId("1.1.1.1")
                .setBfer(bferList)
                .build();
        Future<RpcResult<RemoveBierPathOutput>> removeOutput = pcePathProvider.removeBierPath(removeInput);
        assertTrue(removeOutput.get().isSuccessful());

        List<Bfer> bfers = removeOutput.get().getResult().getBfer();
        assertEquals(2,bfers.size());
        for (Bfer bfer : bfers) {
            assertNotEquals(null,bfer.getBierPath().getPathLink());
            assertTrue(!bfer.getBierPath().getPathLink().isEmpty());

            if (bfer.getBferNodeId().equals("4.4.4.4")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","4.4.4.4");
                assertEquals(10,bfer.getBierPath().getPathMetric().intValue());
            }
            if (bfer.getBferNodeId().equals("6.6.6.6")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","4.4.4.4","5.5.5.5","6.6.6.6");
                assertEquals(30,bfer.getBierPath().getPathMetric().intValue());
            }
        }

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertNotEquals(null,bierTeInstance);
        assertTrue(bierTeInstance.getAllBierPath().size() == 2);


        BierTEInstance bierTEInstanceData = PcePathDb.getInstance().readBierInstance("channel-1");
        assertNotEquals(null,bierTEInstanceData);
        BierTEInstance expectedData = Utils.buildBierTeInstanceData("channel-1","1.1.1.1","4.4.4.4","6.6.6.6");
        assertEquals(2,bierTEInstanceData.getBfer().size());
        for (Bfer bfer : bierTEInstanceData.getBfer()) {
            for (Bfer bierPath : expectedData.getBfer()) {
                if (bierPath.getBferNodeId().equals(bfer.getBferNodeId())) {
                    Utils.assertBierPathData(bierPath.getBierPath(),bfer.getBierPath());
                }
            }
        }

        bferList.clear();
        bferList.add(new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input
                .BferBuilder().setBferNodeId("6.6.6.6").build());
        removeInput = new RemoveBierPathInputBuilder()
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfirNodeId("1.1.1.1")
                .setBfer(bferList)
                .build();
        removeOutput = pcePathProvider.removeBierPath(removeInput);
        assertTrue(removeOutput.get().isSuccessful());

        bfers = removeOutput.get().getResult().getBfer();
        assertEquals(1,bfers.size());

        queryOutput = pcePathProvider.queryBierInstancePath(queryBierTe).get().getResult();
        links = queryOutput.getLink();
        Utils.checkLinkId(links,link14);

        removeBierInstance("channel-1","1.1.1.1",1);
    }

    @Test
    public void removeAllBierPathTest2() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build3BferInfo("4.4.4.4","5.5.5.5","6.6.6.6"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());

        List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input
                .Bfer> bferList = new ArrayList<>();
        bferList.add(new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input
                .BferBuilder().setBferNodeId("5.5.5.5").build());
        bferList.add(new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input
                .BferBuilder().setBferNodeId("4.4.4.4").build());

        RemoveBierPathInput removeInput = new RemoveBierPathInputBuilder()
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfirNodeId("1.1.1.1")
                .setBfer(bferList)
                .build();
        Future<RpcResult<RemoveBierPathOutput>> removeOutput = pcePathProvider.removeBierPath(removeInput);
        assertTrue(removeOutput.get().isSuccessful());

        List<Bfer> bfers = removeOutput.get().getResult().getBfer();
        assertEquals(1,bfers.size());
        for (Bfer bfer : bfers) {
            assertNotEquals(null,bfer.getBierPath().getPathLink());
            assertTrue(!bfer.getBierPath().getPathLink().isEmpty());

            Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","4.4.4.4","5.5.5.5","6.6.6.6");
            assertEquals(30,bfer.getBierPath().getPathMetric().intValue());

        }

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertNotEquals(null,bierTeInstance);
        assertTrue(bierTeInstance.getAllBierPath().size() == 1);

        BierTEInstance bierTEInstanceData = PcePathDb.getInstance().readBierInstance("channel-1");
        assertNotEquals(null,bierTEInstanceData);
        assertTrue(bierTEInstanceData.getBfer().size() == 1);

        QueryBierInstancePathInput queryBierTe = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-1").build();
        QueryBierInstancePathOutput queryOutput = pcePathProvider.queryBierInstancePath(queryBierTe).get().getResult();
        List<Link> links = queryOutput.getLink();
        BierLink link14 = TopoMockUtils.buildLink("1.1.1.1","14.14.14.14","41.41.41.41","4.4.4.4",10);
        BierLink link45 = TopoMockUtils.buildLink("4.4.4.4","45.45.45.45","54.54.54.54","5.5.5.5",10);
        BierLink link56 = TopoMockUtils.buildLink("5.5.5.5","56.56.56.56","65.65.65.65","6.6.6.6",10);
        Utils.checkLinkId(links,link14,link45,link56);

        bferList.clear();
        bferList.add(new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input
                .BferBuilder().setBferNodeId("6.6.6.6").build());

        removeInput = new RemoveBierPathInputBuilder()
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfirNodeId("1.1.1.1")
                .setBfer(bferList)
                .build();
        removeOutput = pcePathProvider.removeBierPath(removeInput);
        assertTrue(removeOutput.get().isSuccessful());
        assertEquals(null,removeOutput.get().getResult().getBfer());

        bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(null,bierTeInstance);

        bierTEInstanceData = PcePathDb.getInstance().readBierInstance("channel-1");
        assertEquals(null,bierTEInstanceData);

        queryOutput = pcePathProvider.queryBierInstancePath(queryBierTe).get().getResult();
        links = queryOutput.getLink();
        assertTrue(links == null);
    }


    @Test
    public void queryBierPathInputCheckTest() throws InterruptedException, ExecutionException {
        List<BierLink> links = TopoMockUtils.buildFourNodeTopo();
        Utils.writeLinksToDB(links);
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build2BferInfo("2.2.2.2","4.4.4.4"))
                .setSaveCreateFail(false)
                .build();
        pcePathProvider.createBierPath(input);
        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(2,bierTeInstance.getAllBierPath().size());

        QueryBierPathInput queryInput = new QueryBierPathInputBuilder()
                .setBfirNodeId("2.2.2.2")
                .build();
        Future<RpcResult<QueryBierPathOutput>> output = pcePathProvider.queryBierPath(queryInput);

        assertTrue(!output.get().isSuccessful());
        assertEquals("Unlegal argument!",output.get().getErrors().iterator().next().getMessage());

        queryInput = new QueryBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-2")
                .setSubDomainId(new SubDomainId(1))
                .setBferNodeId("3.3.3.3")
                .build();
        output = pcePathProvider.queryBierPath(queryInput);

        assertTrue(!output.get().isSuccessful());
        assertEquals("bier instance does not exists!",output.get().getErrors().iterator().next().getMessage());

        queryInput = new QueryBierPathInputBuilder()
                .setBfirNodeId("2.2.2.2")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBferNodeId("3.3.3.3")
                .build();
        output = pcePathProvider.queryBierPath(queryInput);

        assertTrue(!output.get().isSuccessful());
        assertEquals("bfir NodeId is not equals, with the same channel-name!",
                output.get().getErrors().iterator().next().getMessage());

        queryInput = new QueryBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBferNodeId("3.3.3.3")
                .build();
        output = pcePathProvider.queryBierPath(queryInput);

        assertTrue(!output.get().isSuccessful());
        assertEquals("bier path does not exists!",output.get().getErrors().iterator().next().getMessage());
        removeBierInstance("channel-1","1.1.1.1",1);
    }

    @Test
    public void queryBierPathTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);
        buildBierTeInstanceForQueryAndRecovery();

        QueryBierPathInput queryInput = new QueryBierPathInputBuilder()
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfirNodeId("1.1.1.1")
                .setBferNodeId("5.5.5.5")
                .build();
        Future<RpcResult<QueryBierPathOutput>> queryOutput = pcePathProvider.queryBierPath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.bier.path.output
                .BierPath bierPath = queryOutput.get().getResult().getBierPath();
        assertEquals(20,bierPath.getPathMetric().intValue());
        Utils.checkPath(bierPath.getPathLink(),"1.1.1.1","4.4.4.4","5.5.5.5");

        queryInput = new QueryBierPathInputBuilder()
                .setChannelName("channel-2")
                .setSubDomainId(new SubDomainId(1))
                .setBfirNodeId("1.1.1.1")
                .setBferNodeId("5.5.5.5")
                .build();
        queryOutput = pcePathProvider.queryBierPath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        bierPath = queryOutput.get().getResult().getBierPath();
        assertEquals(20,bierPath.getPathMetric().intValue());
        Utils.checkPath(bierPath.getPathLink(),"1.1.1.1","2.2.2.2","5.5.5.5");

        queryInput = new QueryBierPathInputBuilder()
                .setChannelName("channel-3")
                .setSubDomainId(new SubDomainId(1))
                .setBfirNodeId("2.2.2.2")
                .setBferNodeId("5.5.5.5")
                .build();
        queryOutput = pcePathProvider.queryBierPath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        bierPath = queryOutput.get().getResult().getBierPath();
        assertEquals(10,bierPath.getPathMetric().intValue());
        Utils.checkPath(bierPath.getPathLink(),"2.2.2.2","5.5.5.5");
        removeBierInstance("channel-1","1.1.1.1",1);
        removeBierInstance("channel-2","1.1.1.1",1);
        removeBierInstance("channel-3","2.2.2.2",1);
    }


    @Test
    public void queryBierInstancePathTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);
        buildBierTeInstanceForQueryAndRecovery();

        QueryBierInstancePathInput queryInput = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-1")
                .build();
        Future<RpcResult<QueryBierInstancePathOutput>> queryOutput = pcePathProvider.queryBierInstancePath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        List<Link> links = queryOutput.get().getResult().getLink();
        Utils.assertQueryLinks("channel-1",links,false);

        queryInput = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-2")
                .build();
        queryOutput = pcePathProvider.queryBierInstancePath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        links = queryOutput.get().getResult().getLink();
        Utils.assertQueryLinks("channel-2",links,false);

        queryInput = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-3")
                .build();
        queryOutput = pcePathProvider.queryBierInstancePath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        links = queryOutput.get().getResult().getLink();
        Utils.assertQueryLinks("channel-3",links,false);

        queryInput = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-4")
                .build();
        queryOutput = pcePathProvider.queryBierInstancePath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        assertEquals(null,queryOutput.get().getResult().getLink());

        queryOutput = pcePathProvider.queryBierInstancePath(new QueryBierInstancePathInputBuilder().build());
        assertTrue(!queryOutput.get().isSuccessful());
        assertEquals("channel-name is null!",queryOutput.get().getErrors().iterator().next().getMessage());

        removeBierInstance("channel-1","1.1.1.1",1);
        removeBierInstance("channel-2","1.1.1.1",1);
        removeBierInstance("channel-3","2.2.2.2",1);
    }

    @Test
    public void queryChannelThroughPortTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);
        buildBierTeInstanceForQueryAndRecovery();

        Future<RpcResult<QueryChannelThroughPortOutput>> queryOutput = pcePathProvider.queryChannelThroughPort(null);
        assertTrue(!queryOutput.get().isSuccessful());
        assertEquals("input is null, or node-id is null, or tp-id is null!",
                queryOutput.get().getErrors().iterator().next().getMessage());

        QueryChannelThroughPortInput queryInput = new QueryChannelThroughPortInputBuilder()
                .setNodeId("1.1.1.1")
                .setTpId("12.12.12.12")
                .build();
        queryOutput = pcePathProvider.queryChannelThroughPort(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        List<RelatedChannel> channels = queryOutput.get().getResult().getRelatedChannel();
        Utils.assertChannelInfo("channel-2","1.1.1.1",channels);

        queryInput = new QueryChannelThroughPortInputBuilder()
                .setNodeId("5.5.5.5")
                .setTpId("54.54.54.54")
                .build();
        queryOutput = pcePathProvider.queryChannelThroughPort(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        channels = queryOutput.get().getResult().getRelatedChannel();
        Utils.assertChannelInfo("channel-3","2.2.2.2",channels);

        queryInput = new QueryChannelThroughPortInputBuilder()
                .setNodeId("4.4.4.4")
                .setTpId("45.45.45.45")
                .build();
        queryOutput = pcePathProvider.queryChannelThroughPort(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        channels = queryOutput.get().getResult().getRelatedChannel();
        Utils.assertChannelInfo("channel-1","1.1.1.1",channels);

        queryInput = new QueryChannelThroughPortInputBuilder()
                .setNodeId("6.6.6.6")
                .setTpId("63.63.63.63")
                .build();
        queryOutput = pcePathProvider.queryChannelThroughPort(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        channels = queryOutput.get().getResult().getRelatedChannel();
        assertTrue(channels.isEmpty());

        removeBierInstance("channel-1","1.1.1.1",1);
        removeBierInstance("channel-2","1.1.1.1",1);
        removeBierInstance("channel-3","2.2.2.2",1);
    }


    @Test
    public void multicastPathCalcTets1() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build3BferInfo("5.5.5.5","6.6.6.6","3.3.3.3"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);

        List<Bfer> bfers = output.get().getResult().getBfer();
        for (Bfer bfer : bfers) {
            assertNotEquals(null,bfer.getBierPath().getPathLink());
            assertTrue(!bfer.getBierPath().getPathLink().isEmpty());
            if (bfer.getBferNodeId().equals("3.3.3.3")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","2.2.2.2","3.3.3.3");
                assertEquals(20,bfer.getBierPath().getPathMetric().intValue());
            }
            if (bfer.getBferNodeId().equals("5.5.5.5")) {
                assertEquals(2,bfer.getBierPath().getPathLink().size());
                assertEquals(20,bfer.getBierPath().getPathMetric().intValue());
            }
            if (bfer.getBferNodeId().equals("6.6.6.6")) {
                assertTrue(bfer.getBierPath().getPathLink()
                        .contains(new PathLinkBuilder(TopoMockUtils.buildLinkEx("5.5.5.5","56.56.56.56",
                                "65.65.65.65","6.6.6.6",10)).build()));
                assertEquals(30,bfer.getBierPath().getPathMetric().intValue());
            }
        }

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertNotEquals(null,bierTeInstance);
        assertTrue(bierTeInstance.getAllBierPath().size() == 3);

        BierPathUnifyKey pathKey = new BierPathUnifyKey("channel-1",new SubDomainId(1),"1.1.1.1","3.3.3.3");
        Set<BierPathUnifyKey> bierPaths = BierTesRecordPerPort.getInstance()
                .getPathsRecord(new PortKey("2.2.2.2","23.23.23.23"));
        assertEquals(1,bierPaths.size());
        assertTrue(bierPaths.contains(pathKey));

        pathKey = new BierPathUnifyKey("channel-1",new SubDomainId(1),"1.1.1.1","6.6.6.6");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(new PortKey("5.5.5.5","56.56.56.56"));
        assertEquals(1,bierPaths.size());
        assertTrue(bierPaths.contains(pathKey));
        removeBierInstance("channel-1","1.1.1.1",1);
    }

    @Test
    public void multicastPathCalcTets2() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build3BferInfo("4.4.4.4","5.5.5.5","6.6.6.6"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);

        List<Bfer> bfers = output.get().getResult().getBfer();
        for (Bfer bfer : bfers) {
            assertNotEquals(null,bfer.getBierPath().getPathLink());
            assertTrue(!bfer.getBierPath().getPathLink().isEmpty());
            if (bfer.getBferNodeId().equals("4.4.4.4")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","4.4.4.4");
                assertEquals(10,bfer.getBierPath().getPathMetric().intValue());
            }
            if (bfer.getBferNodeId().equals("5.5.5.5")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","4.4.4.4","5.5.5.5");
                assertEquals(20,bfer.getBierPath().getPathMetric().intValue());
            }
            if (bfer.getBferNodeId().equals("6.6.6.6")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","4.4.4.4","5.5.5.5","6.6.6.6");
                assertEquals(30,bfer.getBierPath().getPathMetric().intValue());
            }
        }

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertNotEquals(null,bierTeInstance);
        assertTrue(bierTeInstance.getAllBierPath().size() == 3);

        Set<BierPathUnifyKey> bierPaths = BierTesRecordPerPort.getInstance()
                .getPathsRecord(new PortKey("1.1.1.1","14.14.14.14"));
        assertEquals(3,bierPaths.size());
        BierPathUnifyKey pathKey14 = new BierPathUnifyKey("channel-1",new SubDomainId(1),"1.1.1.1","4.4.4.4");
        BierPathUnifyKey pathKey15 = new BierPathUnifyKey("channel-1",new SubDomainId(1),"1.1.1.1","5.5.5.5");
        BierPathUnifyKey pathKey16 = new BierPathUnifyKey("channel-1",new SubDomainId(1),"1.1.1.1","6.6.6.6");

        assertTrue(bierPaths.contains(pathKey14));
        assertTrue(bierPaths.contains(pathKey15));
        assertTrue(bierPaths.contains(pathKey16));

        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(new PortKey("4.4.4.4","45.45.45.45"));
        assertEquals(2,bierPaths.size());
        assertTrue(bierPaths.contains(pathKey15));
        assertTrue(bierPaths.contains(pathKey16));

        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(new PortKey("5.5.5.5","56.56.56.56"));
        assertEquals(1,bierPaths.size());
        assertTrue(bierPaths.contains(pathKey16));
        removeBierInstance("channel-1","1.1.1.1",1);
    }

    @Test
    public void calcPathInTwoSubDomainTest() throws ExecutionException, InterruptedException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInTwoSubDomain(false);
        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setSubDomainId(new SubDomainId(1))
                .setChannelName("channel-2")
                .setBfer(Utils.build3BferInfo("3.3.3.3","6.6.6.6","5.5.5.5"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());
        QueryBierInstancePathInput queryInput = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-2")
                .build();
        Future<RpcResult<QueryBierInstancePathOutput>> queryOutput = pcePathProvider.queryBierInstancePath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        List<Link> links = queryOutput.get().getResult().getLink();
        Utils.assertQueryLinks("channel-2",links,false);

        input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setSubDomainId(new SubDomainId(2))
                .setChannelName("channel-4")
                .setBfer(Utils.build3BferInfo("3.3.3.3","6.6.6.6","5.5.5.5"))
                .build();
        output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());
        List<Bfer> bfers = output.get().getResult().getBfer();
        for (Bfer bfer : bfers) {
            assertNotEquals(null,bfer.getBierPath().getPathLink());
            assertTrue(!bfer.getBierPath().getPathLink().isEmpty());
            if (bfer.getBferNodeId().equals("3.3.3.3")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","4.4.4.4","5.5.5.5","6.6.6.6","3.3.3.3");
                assertEquals(40,bfer.getBierPath().getPathMetric().intValue());
            }
            if (bfer.getBferNodeId().equals("5.5.5.5")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","4.4.4.4","5.5.5.5");
                assertEquals(20,bfer.getBierPath().getPathMetric().intValue());
            }
            if (bfer.getBferNodeId().equals("6.6.6.6")) {
                Utils.checkPath(bfer.getBierPath().getPathLink(),"1.1.1.1","4.4.4.4","5.5.5.5","6.6.6.6");
                assertEquals(30,bfer.getBierPath().getPathMetric().intValue());
            }
        }

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-4");
        assertNotEquals(null,bierTeInstance);
        assertEquals(3,bierTeInstance.getAllBierPath().size());

        Set<BierPathUnifyKey> bierPaths = BierTesRecordPerPort.getInstance()
                .getPathsRecord(new PortKey("1.1.1.1","14.14.14.14"));
        assertEquals(3,bierPaths.size());
        BierPathUnifyKey pathKey13 = new BierPathUnifyKey("channel-4",new SubDomainId(2),"1.1.1.1","3.3.3.3");
        BierPathUnifyKey pathKey15 = new BierPathUnifyKey("channel-4",new SubDomainId(2),"1.1.1.1","5.5.5.5");
        BierPathUnifyKey pathKey16 = new BierPathUnifyKey("channel-4",new SubDomainId(2),"1.1.1.1","6.6.6.6");

        assertTrue(bierPaths.contains(pathKey13));
        assertTrue(bierPaths.contains(pathKey15));
        assertTrue(bierPaths.contains(pathKey16));

        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(new PortKey("5.5.5.5","56.56.56.56"));
        assertEquals(2,bierPaths.size());
        assertTrue(bierPaths.contains(pathKey16));
        assertTrue(bierPaths.contains(pathKey13));

        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(new PortKey("6.6.6.6","63.63.63.63"));
        assertEquals(1,bierPaths.size());
        assertTrue(bierPaths.contains(pathKey13));

        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(new PortKey("3.3.3.3","36.36.36.36"));
        BierPathUnifyKey pathKey116 = new BierPathUnifyKey("channel-2",new SubDomainId(1),"1.1.1.1","6.6.6.6");
        assertEquals(1,bierPaths.size());
        assertTrue(bierPaths.contains(pathKey116));

        Utils.assertPathsPerSubDomain(new SubDomainId(1),1);
        Utils.assertPathsPerSubDomain(new SubDomainId(2),1);

        queryInput = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-4")
                .build();
        queryOutput = pcePathProvider.queryBierInstancePath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        links = queryOutput.get().getResult().getLink();
        Utils.assertQueryLinks("channel-4",links,false);
        removeBierInstance("channel-1","1.1.1.1",1);
        removeBierInstance("channel-2","1.1.1.1",1);
        removeBierInstance("channel-3","2.2.2.2",1);
        removeBierInstance("channel-4","1.1.1.1",2);
    }

    @Test
    public void createTeFrrPathCheckTest() throws ExecutionException, InterruptedException {
        Future<RpcResult<CreateTeFrrPathOutput>> output = pcePathProvider.createTeFrrPath(null);
        assertTrue(!output.get().isSuccessful());
        assertEquals("Unlegal argument!",output.get().getErrors().iterator().next().getMessage());
        CreateTeFrrPathInput input = new CreateTeFrrPathInputBuilder().build();
        output = pcePathProvider.createTeFrrPath(input);
        assertTrue(!output.get().isSuccessful());
        assertEquals("Unlegal argument!",output.get().getErrors().iterator().next().getMessage());
        input = new CreateTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder().setSubDomainId(new SubDomainId(1)).build())
                .build();
        output = pcePathProvider.createTeFrrPath(input);
        assertTrue(!output.get().isSuccessful());
        assertEquals("Unlegal argument!",output.get().getErrors().iterator().next().getMessage());
        input = new CreateTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(new ProtectedLinkBuilder()
                                .setLinkSource(new LinkSourceBuilder()
                                        .setSourceNode("node1")
                                        .setSourceTp("tp1")
                                        .build())
                                .build())
                        .build())
                .build();
        output = pcePathProvider.createTeFrrPath(input);
        assertTrue(!output.get().isSuccessful());
        assertEquals("Unlegal argument!",output.get().getErrors().iterator().next().getMessage());
    }

    @Test
    public void createTeFrrPathTest() throws ExecutionException, InterruptedException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);
        ProtectedLink protectedLink = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("2.2.2.2", "25.25.25.25", "52.52.52.52", "5.5.5.5", 10)).build();
        CreateTeFrrPathInput input = new CreateTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink)
                        .build())
                .build();
        Future<RpcResult<CreateTeFrrPathOutput>> output = pcePathProvider.createTeFrrPath(input);
        assertTrue(output.get().isSuccessful());
        FrrPath frrPath = output.get().getResult().getFrrPath();
        //check next-hop path
        assertNotNull(frrPath.getNextHopPath());
        checkNextHopFrrPath("5.5.5.5",frrPath.getNextHopPath());
        Utils.checkPath(Utils.transPath(frrPath.getNextHopPath().getPath()),
                "2.2.2.2", "1.1.1.1", "4.4.4.4", "5.5.5.5");
        //check next next-hop path
        assertEquals(2,frrPath.getNextNextHopPath().size());
        for (NextNextHopPath nextNextHopPath : frrPath.getNextNextHopPath()) {
            checkNextNextHopFrrPath(nextNextHopPath);
            if (nextNextHopPath.getDestinationNode().equals("4.4.4.4")) {
                Utils.checkPath(Utils.transPath(nextNextHopPath.getPath()), "2.2.2.2", "1.1.1.1", "4.4.4.4");
            } else  {
                assertEquals("6.6.6.6",nextNextHopPath.getDestinationNode());
                Utils.checkPath(Utils.transPath(nextNextHopPath.getPath()), "2.2.2.2", "3.3.3.3", "6.6.6.6");
            }
        }
        //check excluding links
        BierLink link25 = TopoMockUtils.buildLink("2.2.2.2","25.25.25.25","52.52.52.52","5.5.5.5",10);
        BierLink link54 = TopoMockUtils.buildLink("5.5.5.5","54.54.54.54","45.45.45.45","4.4.4.4",10);
        BierLink link56 = TopoMockUtils.buildLink("5.5.5.5","56.56.56.56","65.65.65.65","6.6.6.6",10);
        ExcludingLink excludingLink25 = new ExcludingLinkBuilder(link25).build();
        ExcludingLink excludingLink54 = new ExcludingLinkBuilder(link54).build();
        ExcludingLink excludingLink56 = new ExcludingLinkBuilder(link56).build();
        assertTrue(frrPath.getExcludingLink().contains(excludingLink25));
        assertTrue(frrPath.getExcludingLink().contains(excludingLink54));
        assertTrue(frrPath.getExcludingLink().contains(excludingLink56));

        //check te-frr path data
        BierTeFrrLink bierTeFrrData = PcePathDb.getInstance().readBierTeFrrLink(new SubDomainId(1),protectedLink);
        assertEquals(frrPath,bierTeFrrData.getFrrPath());
    }

    @Test
    public void removeTeFrrPathTest() throws ExecutionException, InterruptedException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);
        ProtectedLink protectedLink = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("2.2.2.2", "25.25.25.25", "52.52.52.52", "5.5.5.5", 10)).build();
        CreateTeFrrPathInput input = new CreateTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink)
                        .build())
                .build();
        pcePathProvider.createTeFrrPath(input);
        RemoveTeFrrPathInput removeInput = new RemoveTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink)
                        .build())
                .build();
        Future<RpcResult<Void>> output = pcePathProvider.removeTeFrrPath(removeInput);
        assertTrue(output.get().isSuccessful());
        BierTeFrrLink bierTeFrrData = PcePathDb.getInstance().readBierTeFrrLink(new SubDomainId(1),protectedLink);
        assertTrue(bierTeFrrData == null);

        QueryTeFrrPathInput queryInput = new QueryTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink)
                        .build())
                .build();
        Future<RpcResult<QueryTeFrrPathOutput>> queryOutput = pcePathProvider.queryTeFrrPath(queryInput);
        assertTrue(output.get().isSuccessful());
        assertTrue(queryOutput.get().getResult().getLink() == null);

        output = pcePathProvider.removeTeFrrPath(removeInput);
        assertTrue(output.get().isSuccessful());
    }

    @Test
    public void queryTeFrrPathTest() throws ExecutionException, InterruptedException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);
        ProtectedLink protectedLink = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("2.2.2.2", "25.25.25.25", "52.52.52.52", "5.5.5.5", 10)).build();
        CreateTeFrrPathInput input = new CreateTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink)
                        .build())
                .build();
        pcePathProvider.createTeFrrPath(input);
        QueryTeFrrPathInput queryInput = new QueryTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink)
                        .build())
                .build();
        Future<RpcResult<QueryTeFrrPathOutput>> output = pcePathProvider.queryTeFrrPath(queryInput);
        assertTrue(output.get().isSuccessful());
        assertTrue(!output.get().getResult().getLink().isEmpty());
        checkFrrPathLinkId(protectedLink,output.get().getResult().getLink());

        ProtectedLink protectedLink1 = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("2.2.2.2", "23.23.23.23", "32.32.32.32", "3.3.3.3", 10)).build();
        queryInput = new QueryTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink1)
                        .build())
                .build();
        output = pcePathProvider.queryTeFrrPath(queryInput);
        assertTrue(output.get().isSuccessful());
        assertTrue(output.get().getResult().getLink() == null);
    }


    @Test
    public void queryTeFrrPathInTwoSubdomainTest() throws ExecutionException, InterruptedException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);
        ProtectedLink protectedLink = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("2.2.2.2", "25.25.25.25", "52.52.52.52", "5.5.5.5", 10)).build();
        CreateTeFrrPathInput input = new CreateTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink)
                        .build())
                .build();
        pcePathProvider.createTeFrrPath(input);

        ProtectedLink protectedLink2 = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("6.6.6.6", "63.63.63.63", "36.36.36.36", "3.3.3.3", 10)).build();
        input = new CreateTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(2))
                        .setProtectedLink(protectedLink2)
                        .build())
                .build();
        Future<RpcResult<CreateTeFrrPathOutput>> output1 = pcePathProvider.createTeFrrPath(input);
        assertTrue(output1.get().isSuccessful());

        QueryTeFrrPathInput queryInput = new QueryTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink)
                        .build())
                .build();
        Future<RpcResult<QueryTeFrrPathOutput>> output = pcePathProvider.queryTeFrrPath(queryInput);
        assertTrue(output.get().isSuccessful());
        assertTrue(!output.get().getResult().getLink().isEmpty());
        checkFrrPathLinkId(protectedLink,output.get().getResult().getLink());

        ProtectedLink protectedLink1 = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("2.2.2.2", "23.23.23.23", "32.32.32.32", "3.3.3.3", 10)).build();
        queryInput = new QueryTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink1)
                        .build())
                .build();
        output = pcePathProvider.queryTeFrrPath(queryInput);
        assertTrue(output.get().isSuccessful());
        assertTrue(output.get().getResult().getLink() == null);

        queryInput = new QueryTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(2))
                        .setProtectedLink(protectedLink2)
                        .build())
                .build();
        output = pcePathProvider.queryTeFrrPath(queryInput);
        assertTrue(output.get().isSuccessful());
        assertTrue(output.get().getResult().getLink().isEmpty());
    }

    @Test
    public void teSubdomainAddTest() throws ExecutionException, InterruptedException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInTwoSubDomain(false);
        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setSubDomainId(new SubDomainId(1))
                .setChannelName("channel-2")
                .setBfer(Utils.build3BferInfo("3.3.3.3","6.6.6.6","5.5.5.5"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());
        QueryBierInstancePathInput queryInput1 = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-2")
                .build();
        Future<RpcResult<QueryBierInstancePathOutput>> queryOutput1 = pcePathProvider
                .queryBierInstancePath(queryInput1);
        assertTrue(queryOutput1.get().isSuccessful());
        List<Link> links = queryOutput1.get().getResult().getLink();
        Utils.assertQueryLinks("channel-2",links,false);

        ProtectedLink protectedLink2 = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("6.6.6.6", "63.63.63.63", "36.36.36.36", "3.3.3.3", 10)).build();
        CreateTeFrrPathInput inputFrr = new CreateTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(2))
                        .setProtectedLink(protectedLink2)
                        .build())
                .build();
        Future<RpcResult<CreateTeFrrPathOutput>> output1 = pcePathProvider.createTeFrrPath(inputFrr);
        assertTrue(output1.get().isSuccessful());

        QueryTeFrrPathInput queryInput2 = new QueryTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(2))
                        .setProtectedLink(protectedLink2)
                        .build())
                .build();
        Future<RpcResult<QueryTeFrrPathOutput>> queryOutput2 = pcePathProvider.queryTeFrrPath(queryInput2);
        assertTrue(queryOutput2.get().isSuccessful());
        assertTrue(queryOutput2.get().getResult().getLink().isEmpty());

        TopoMockUtils.buildBierTeNodeInTwoSubDomain(true);
        notifyTeSubdomainAdd("2.2.2.2",new SubDomainId(2));

        queryOutput1 = pcePathProvider.queryBierInstancePath(queryInput1);
        assertTrue(queryOutput1.get().isSuccessful());
        links = queryOutput1.get().getResult().getLink();
        Utils.assertQueryLinks("channel-2",links,false);
        queryOutput2 = pcePathProvider.queryTeFrrPath(queryInput2);
        assertTrue(queryOutput2.get().isSuccessful());
        assertTrue(queryOutput2.get().getResult().getLink().size() == 3);

    }


    @Test
    public void teSubdomainDeleteTest() throws ExecutionException, InterruptedException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInTwoSubDomain(true);
        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setSubDomainId(new SubDomainId(2))
                .setChannelName("channel-2")
                .setBfer(Utils.build3BferInfo("3.3.3.3","6.6.6.6","5.5.5.5"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());
        QueryBierInstancePathInput queryInput1 = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-2")
                .build();
        Future<RpcResult<QueryBierInstancePathOutput>> queryOutput1 = pcePathProvider
                .queryBierInstancePath(queryInput1);
        assertTrue(queryOutput1.get().isSuccessful());
        List<Link> links = queryOutput1.get().getResult().getLink();
        Utils.assertQueryLinks("channel-2",links,false);

        ProtectedLink protectedLink2 = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("6.6.6.6", "63.63.63.63", "36.36.36.36", "3.3.3.3", 10)).build();
        CreateTeFrrPathInput inputFrr = new CreateTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink2)
                        .build())
                .build();
        Future<RpcResult<CreateTeFrrPathOutput>> output1 = pcePathProvider.createTeFrrPath(inputFrr);
        assertTrue(output1.get().isSuccessful());

        QueryTeFrrPathInput queryInput2 = new QueryTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink2)
                        .build())
                .build();
        Future<RpcResult<QueryTeFrrPathOutput>> queryOutput2 = pcePathProvider.queryTeFrrPath(queryInput2);
        assertTrue(queryOutput2.get().isSuccessful());
        assertTrue(queryOutput2.get().getResult().getLink().size() == 3);

        TopoMockUtils.buildBierTeNodeInTwoSubDomain(false);
        notifyTeSubdomainDelete("2.2.2.2",new SubDomainId(2));

        queryOutput1 = pcePathProvider.queryBierInstancePath(queryInput1);
        assertTrue(queryOutput1.get().isSuccessful());
        links = queryOutput1.get().getResult().getLink();
        Utils.assertQueryLinks("channel-2",links,true);

        queryOutput2 = pcePathProvider.queryTeFrrPath(queryInput2);
        assertTrue(queryOutput2.get().isSuccessful());
        assertTrue(queryOutput2.get().getResult().getLink().size() == 3);

    }

    private void notifyTeSubdomainAdd(String node, SubDomainId subDomainId) {
        TeSubdomainAdd teSubdomainAdd = new TeSubdomainAddBuilder()
                .setTopologyId(TopoMockUtils.DEFAULT_TOPO)
                .setDomainId(new DomainId(1))
                .setSubDomainId(subDomainId)
                .setNodeId(node)
                .build();
        TopologyProvider.getInstance().onTeSubdomainAdd(teSubdomainAdd);
    }

    private void notifyTeSubdomainDelete(String node, SubDomainId subDomainId) {
        TeSubdomainDelete teSubdomainDelete = new TeSubdomainDeleteBuilder()
                .setTopologyId(TopoMockUtils.DEFAULT_TOPO)
                .setDomainId(new DomainId(1))
                .setSubDomainId(subDomainId)
                .setNodeId(node)
                .build();
        TopologyProvider.getInstance().onTeSubdomainDelete(teSubdomainDelete);
    }


    private void checkFrrPathLinkId(ProtectedLink protectedLink,
                                    List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328
                                            .query.te.frr.path.output.Link> frrLinks) {
        if (protectedLink.getLinkId().equals(Utils.buildLinkId("2.2.2.2","25.25.25.25","52.52.52.52","5.5.5.5"))) {
            assertEquals(5, frrLinks.size());
            assertTrue(frrLinks.contains(new LinkBuilder()
                    .setLinkId(Utils.buildLinkId("2.2.2.2", "21.21.21.21", "12.12.12.12", "1.1.1.1"))
                    .build()));
            assertTrue(frrLinks.contains(new LinkBuilder()
                    .setLinkId(Utils.buildLinkId("1.1.1.1", "14.14.14.14", "41.41.41.41", "4.4.4.4"))
                    .build()));
            assertTrue(frrLinks.contains(new LinkBuilder()
                    .setLinkId(Utils.buildLinkId("4.4.4.4", "45.45.45.45", "54.54.54.54", "5.5.5.5"))
                    .build()));
            assertTrue(frrLinks.contains(new LinkBuilder()
                    .setLinkId(Utils.buildLinkId("2.2.2.2", "23.23.23.23", "32.32.32.32", "3.3.3.3"))
                    .build()));
            assertTrue(frrLinks.contains(new LinkBuilder()
                    .setLinkId(Utils.buildLinkId("3.3.3.3", "36.36.36.36", "63.63.63.63", "6.6.6.6"))
                    .build()));
        }

    }

    private void checkNextHopFrrPath(String node, NextHopPath nextHopPath) {
        assertEquals(node, nextHopPath.getDestinationNode());
        assertNotEquals(null, nextHopPath.getPath());
        assertTrue(!nextHopPath.getPath().isEmpty());
    }

    private void checkNextNextHopFrrPath(NextNextHopPath nextNextHopPath) {
        //assertEquals(node, nextNextHopPath.getDestinationNode());
        assertNotEquals(null, nextNextHopPath.getPath());
        assertTrue(!nextNextHopPath.getPath().isEmpty());
    }

    private void buildBierTeInstanceForQueryAndRecovery() throws ExecutionException, InterruptedException {

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build3BferInfo("4.4.4.4","5.5.5.5","6.6.6.6"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());

        input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setSubDomainId(new SubDomainId(1))
                .setChannelName("channel-2")
                .setBfer(Utils.build3BferInfo("3.3.3.3","6.6.6.6","5.5.5.5"))
                .build();
        output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());

        input = new CreateBierPathInputBuilder()
                .setBfirNodeId("2.2.2.2")
                .setChannelName("channel-3")
                .setSubDomainId(new SubDomainId(1))
                .setBfer(Utils.build2BferInfo("5.5.5.5","4.4.4.4"))
                .build();
        output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());
    }

    private void removeBierInstance(String channelName, String bfirNode, int subDomainId) {
        RemoveBierPathInput input = new RemoveBierPathInputBuilder()
                .setChannelName(channelName)
                .setSubDomainId(new SubDomainId(subDomainId))
                .setBfirNodeId(bfirNode)
                .build();
        pcePathProvider.removeBierPath(input);
    }
}

