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
import org.opendaylight.bier.pce.impl.topology.TopologyProvider;
import org.opendaylight.bier.pce.impl.util.TopoMockUtils;
import org.opendaylight.bier.pce.impl.util.Utils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.bier.instance.path.output.Link;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstance;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;

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

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setBfer(Utils.build2BferInfo("2.2.2.2","4.4.4.4"))
                .setSaveCreateFail(false)
                .build();
        pcePathProvider.createBierPath(input);
        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(2,bierTeInstance.getAllBierPath().size());

        input = new CreateBierPathInputBuilder()
                .setBfirNodeId("2.2.2.2")
                .setChannelName("channel-1")
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

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setBfer(Utils.build2BferInfo("2.2.2.2","4.4.4.4"))
                .setSaveCreateFail(false)
                .build();
        pcePathProvider.createBierPath(input);
        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(2,bierTeInstance.getAllBierPath().size());

        RemoveBierPathInput removeInput = new RemoveBierPathInputBuilder()
                .setBfirNodeId("2.2.2.2")
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
                .build();
        output = pcePathProvider.removeBierPath(removeInput);

        assertTrue(output.get().isSuccessful());
        bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(2,bierTeInstance.getAllBierPath().size());
    }

    @Test
    public void createBierPathFailWithNotSaveTest() throws InterruptedException, ExecutionException {
        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
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
    }


    @Test
    public void createBierPathSuccessWithNotSaveTest() throws InterruptedException, ExecutionException {
        List<BierLink> links = TopoMockUtils.buildFourNodeTopo();
        Utils.writeLinksToDB(links);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
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
        removeBierInstance("channel-1","1.1.1.1");
    }


    @Test
    public void createBierPathSuccessWithSaveTest() throws InterruptedException, ExecutionException {
        List<BierLink> links = TopoMockUtils.buildFourNodeTopo();
        Utils.writeLinksToDB(links);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
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
        removeBierInstance("channel-1","1.1.1.1");
    }



    @Test
    public void bierPathCalcWhenUpdateChannelInfoTets() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
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
        removeBierInstance("channel-1","1.1.1.1");
    }

    @Test
    public void removeAllBierPathTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setBfer(Utils.build3BferInfo("4.4.4.4","5.5.5.5","6.6.6.6"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());

        RemoveBierPathInput removeInput = new RemoveBierPathInputBuilder()
                .setChannelName("channel-2")
                .build();
        Future<RpcResult<RemoveBierPathOutput>> removeOutput = pcePathProvider.removeBierPath(removeInput);
        assertTrue(removeOutput.get().isSuccessful());
        assertEquals(null,removeOutput.get().getResult().getBfer());

        removeInput = new RemoveBierPathInputBuilder()
                .setChannelName("channel-1")
                .setBfirNodeId("1.1.1.1")
                .build();
        removeOutput = pcePathProvider.removeBierPath(removeInput);
        assertTrue(removeOutput.get().isSuccessful());
        assertEquals(null,removeOutput.get().getResult().getBfer());

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        assertEquals(null,bierTeInstance);

        BierTEInstance bierTEInstanceData = PcePathDb.getInstance().readBierInstance("channel-1");
        assertEquals(null,bierTEInstanceData);

    }

    @Test
    public void removeSpecifiedBierPathTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setBfer(Utils.build3BferInfo("4.4.4.4","5.5.5.5","6.6.6.6"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());

        List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input
                .Bfer> bferList = new ArrayList<>();
        bferList.add(new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input
                .BferBuilder().setBferNodeId("5.5.5.5").build());
        RemoveBierPathInput removeInput = new RemoveBierPathInputBuilder()
                .setChannelName("channel-1")
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

        removeBierInstance("channel-1","1.1.1.1");
    }

    @Test
    public void removeAllBierPathTest2() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
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

        bferList.clear();
        bferList.add(new org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.remove.bier.path.input
                .BferBuilder().setBferNodeId("6.6.6.6").build());

        removeInput = new RemoveBierPathInputBuilder()
                .setChannelName("channel-1")
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

    }


    @Test
    public void queryBierPathInputCheckTest() throws InterruptedException, ExecutionException {
        List<BierLink> links = TopoMockUtils.buildFourNodeTopo();
        Utils.writeLinksToDB(links);

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
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
                .setBferNodeId("3.3.3.3")
                .build();
        output = pcePathProvider.queryBierPath(queryInput);

        assertTrue(!output.get().isSuccessful());
        assertEquals("bier instance does not exists!",output.get().getErrors().iterator().next().getMessage());

        queryInput = new QueryBierPathInputBuilder()
                .setBfirNodeId("2.2.2.2")
                .setChannelName("channel-1")
                .setBferNodeId("3.3.3.3")
                .build();
        output = pcePathProvider.queryBierPath(queryInput);

        assertTrue(!output.get().isSuccessful());
        assertEquals("bfir NodeId is not equals, with the same channel-name!",
                output.get().getErrors().iterator().next().getMessage());

        queryInput = new QueryBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setBferNodeId("3.3.3.3")
                .build();
        output = pcePathProvider.queryBierPath(queryInput);

        assertTrue(!output.get().isSuccessful());
        assertEquals("bier path does not exists!",output.get().getErrors().iterator().next().getMessage());
    }

    @Test
    public void queryBierPathTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        buildBierTeInstanceForQueryAndRecovery();

        QueryBierPathInput queryInput = new QueryBierPathInputBuilder()
                .setChannelName("channel-1")
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
                .setBfirNodeId("2.2.2.2")
                .setBferNodeId("5.5.5.5")
                .build();
        queryOutput = pcePathProvider.queryBierPath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        bierPath = queryOutput.get().getResult().getBierPath();
        assertEquals(10,bierPath.getPathMetric().intValue());
        Utils.checkPath(bierPath.getPathLink(),"2.2.2.2","5.5.5.5");
        removeBierInstance("channel-1","1.1.1.1");
        removeBierInstance("channel-2","1.1.1.1");
        removeBierInstance("channel-3","2.2.2.2");
    }


    @Test
    public void queryBierInstancePathTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        buildBierTeInstanceForQueryAndRecovery();

        QueryBierInstancePathInput queryInput = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-1")
                .build();
        Future<RpcResult<QueryBierInstancePathOutput>> queryOutput = pcePathProvider.queryBierInstancePath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        List<Link> links = queryOutput.get().getResult().getLink();
        Utils.assertQueryLinks("channel-1",links);

        queryInput = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-2")
                .build();
        queryOutput = pcePathProvider.queryBierInstancePath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        links = queryOutput.get().getResult().getLink();
        Utils.assertQueryLinks("channel-2",links);

        queryInput = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-3")
                .build();
        queryOutput = pcePathProvider.queryBierInstancePath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        links = queryOutput.get().getResult().getLink();
        Utils.assertQueryLinks("channel-3",links);

        queryInput = new QueryBierInstancePathInputBuilder()
                .setChannelName("channel-4")
                .build();
        queryOutput = pcePathProvider.queryBierInstancePath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        assertEquals(null,queryOutput.get().getResult().getLink());

        queryOutput = pcePathProvider.queryBierInstancePath(new QueryBierInstancePathInputBuilder().build());
        assertTrue(!queryOutput.get().isSuccessful());
        assertEquals("channel-name is null!",queryOutput.get().getErrors().iterator().next().getMessage());

        removeBierInstance("channel-1","1.1.1.1");
        removeBierInstance("channel-2","1.1.1.1");
        removeBierInstance("channel-3","2.2.2.2");
    }

    @Test
    public void multicastPathCalcTets1() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
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

        BierPathUnifyKey pathKey = new BierPathUnifyKey("channel-1","1.1.1.1","3.3.3.3");
        Set<BierPathUnifyKey> bierPaths = BierTesRecordPerPort.getInstance()
                .getPathsRecord(new PortKey("2.2.2.2","23.23.23.23"));
        assertEquals(1,bierPaths.size());
        assertTrue(bierPaths.contains(pathKey));

        pathKey = new BierPathUnifyKey("channel-1","1.1.1.1","6.6.6.6");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(new PortKey("5.5.5.5","56.56.56.56"));
        assertEquals(1,bierPaths.size());
        assertTrue(bierPaths.contains(pathKey));
        removeBierInstance("channel-1","1.1.1.1");
    }



    @Test
    public void multicastPathCalcTets2() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
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
        BierPathUnifyKey pathKey14 = new BierPathUnifyKey("channel-1","1.1.1.1","4.4.4.4");
        BierPathUnifyKey pathKey15 = new BierPathUnifyKey("channel-1","1.1.1.1","5.5.5.5");
        BierPathUnifyKey pathKey16 = new BierPathUnifyKey("channel-1","1.1.1.1","6.6.6.6");

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
        removeBierInstance("channel-1","1.1.1.1");
    }

    private void buildBierTeInstanceForQueryAndRecovery() throws ExecutionException, InterruptedException {

        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-1")
                .setBfer(Utils.build3BferInfo("4.4.4.4","5.5.5.5","6.6.6.6"))
                .build();
        Future<RpcResult<CreateBierPathOutput>> output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());

        input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-2")
                .setBfer(Utils.build3BferInfo("3.3.3.3","6.6.6.6","5.5.5.5"))
                .build();
        output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());

        input = new CreateBierPathInputBuilder()
                .setBfirNodeId("2.2.2.2")
                .setChannelName("channel-3")
                .setBfer(Utils.build2BferInfo("5.5.5.5","4.4.4.4"))
                .build();
        output = pcePathProvider.createBierPath(input);
        assertTrue(output.get().isSuccessful());
    }

    private void removeBierInstance(String channelName, String bfirNode) {
        RemoveBierPathInput input = new RemoveBierPathInputBuilder()
                .setChannelName(channelName)
                .setBfirNodeId(bfirNode)
                .build();
        pcePathProvider.removeBierPath(input);
    }
}

