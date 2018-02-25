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

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.bier.pce.impl.biertepath.BierPathUnifyKey;
import org.opendaylight.bier.pce.impl.biertepath.BierTeInstance;
import org.opendaylight.bier.pce.impl.biertepath.SingleBierPath;
import org.opendaylight.bier.pce.impl.pathcore.BierTesRecordPerPort;
import org.opendaylight.bier.pce.impl.pathcore.PortKey;
import org.opendaylight.bier.pce.impl.tefrr.TeFrrBackupPath;
import org.opendaylight.bier.pce.impl.tefrr.TeFrrInstance;
import org.opendaylight.bier.pce.impl.topology.PathsRecordPerSubDomain;
import org.opendaylight.bier.pce.impl.topology.TopologyProvider;
import org.opendaylight.bier.pce.impl.util.ComUtility;
import org.opendaylight.bier.pce.impl.util.TopoMockUtils;
import org.opendaylight.bier.pce.impl.util.Utils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.PathType;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveTeFrrPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveTeFrrPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPath;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.TeFrrKey;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.TeFrrKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.te.frr.key.ProtectedLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.te.frr.key.ProtectedLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkAdd;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkAddBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkChange;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkChangeBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkRemove;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.LinkRemoveBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.link.add.AddLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.link.change.NewLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.link.change.OldLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.api.rev161102.link.remove.RemoveLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yangtools.yang.common.RpcResult;


public class TopologyProviderTest extends AbstractConcurrentDataBrokerTest {
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
    public void topoChange_LinkAddTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);
        buildBierTeInstanceForTest();

        notifyAddLink("1.1.1.1","15.15.15.15","51.51.51.51","5.5.5.5");

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        for (SingleBierPath bierPath : bierTeInstance.getAllBierPath()) {
            assertNotEquals(null,bierPath.getPath());
            assertTrue(!bierPath.getPath().isEmpty());
            if (bierPath.getBferNodeId().equals("4.4.4.4")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","4.4.4.4");
                assertEquals(10,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-1","4.4.4.4");
            }
            if (bierPath.getBferNodeId().equals("5.5.5.5")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","5.5.5.5");
                assertEquals(10,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-1","5.5.5.5");
            }
            if (bierPath.getBferNodeId().equals("6.6.6.6")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","5.5.5.5","6.6.6.6");
                assertEquals(20,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-1","6.6.6.6");
            }
        }

        bierTeInstance = pcePathProvider.getBierTeInstance("channel-2");
        for (SingleBierPath bierPath : bierTeInstance.getAllBierPath()) {
            assertNotEquals(null,bierPath.getPath());
            assertTrue(!bierPath.getPath().isEmpty());
            if (bierPath.getBferNodeId().equals("3.3.3.3")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","2.2.2.2","3.3.3.3");
                assertEquals(20,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-2","3.3.3.3");
            }
            if (bierPath.getBferNodeId().equals("5.5.5.5")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","5.5.5.5");
                assertEquals(10,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-2","5.5.5.5");
            }
            if (bierPath.getBferNodeId().equals("6.6.6.6")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),
                        "1.1.1.1","2.2.2.2","3.3.3.3","6.6.6.6");
                assertEquals(30,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-2","6.6.6.6");
            }
        }


        bierTeInstance = pcePathProvider.getBierTeInstance("channel-3");
        for (SingleBierPath bierPath : bierTeInstance.getAllBierPath()) {
            assertNotEquals(null,bierPath.getPath());
            assertTrue(!bierPath.getPath().isEmpty());
            if (bierPath.getBferNodeId().equals("4.4.4.4")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"2.2.2.2","5.5.5.5","4.4.4.4");
                assertEquals(20,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-3","4.4.4.4");
            }
            if (bierPath.getBferNodeId().equals("5.5.5.5")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"2.2.2.2","5.5.5.5");
                assertEquals(10,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-3","5.5.5.5");
            }
        }

        PortKey portKey14 = new PortKey("1.1.1.1","14.14.14.14");
        Set<BierPathUnifyKey> bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey14);
        assertEquals(1,bierPaths.size());

        PortKey portKey25 = new PortKey("2.2.2.2","25.25.25.25");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey25);
        assertEquals(2,bierPaths.size());

        PortKey portKey23 = new PortKey("2.2.2.2","23.23.23.23");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey23);
        assertEquals(2,bierPaths.size());

        PortKey portKey15 = new PortKey("1.1.1.1","15.15.15.15");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey15);
        assertEquals(3,bierPaths.size());

        removeBierInstance("channel-1","1.1.1.1",1);
        removeBierInstance("channel-2","1.1.1.1",1);
        removeBierInstance("channel-3","2.2.2.2",1);
    }

    @Test
    public void topoChange_LinkAddTest1() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInTwoSubDomain(false);
        buildBierTeInstanceForTest();
        CreateBierPathInput input = new CreateBierPathInputBuilder()
                .setBfirNodeId("1.1.1.1")
                .setChannelName("channel-4")
                .setSubDomainId(new SubDomainId(2))
                .setBfer(Utils.build3BferInfo("3.3.3.3","6.6.6.6","5.5.5.5"))
                .build();
        pcePathProvider.createBierPath(input);

        notifyAddLink("1.1.1.1","15.15.15.15","51.51.51.51","5.5.5.5");

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-4");
        for (SingleBierPath bierPath : bierTeInstance.getAllBierPath()) {
            assertNotEquals(null,bierPath.getPath());
            assertTrue(!bierPath.getPath().isEmpty());
            if (bierPath.getBferNodeId().equals("3.3.3.3")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),
                        "1.1.1.1","5.5.5.5","6.6.6.6","3.3.3.3");
                assertEquals(30,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-4","3.3.3.3");
            }
            if (bierPath.getBferNodeId().equals("5.5.5.5")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","5.5.5.5");
                assertEquals(10,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-1","5.5.5.5");
            }
            if (bierPath.getBferNodeId().equals("6.6.6.6")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","5.5.5.5","6.6.6.6");
                assertEquals(20,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-1","6.6.6.6");
            }
        }

        bierTeInstance = pcePathProvider.getBierTeInstance("channel-2");
        for (SingleBierPath bierPath : bierTeInstance.getAllBierPath()) {
            assertNotEquals(null,bierPath.getPath());
            assertTrue(!bierPath.getPath().isEmpty());
            if (bierPath.getBferNodeId().equals("3.3.3.3")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","2.2.2.2","3.3.3.3");
                assertEquals(20,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-2","3.3.3.3");
            }
            if (bierPath.getBferNodeId().equals("5.5.5.5")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","5.5.5.5");
                assertEquals(10,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-2","5.5.5.5");
            }
            if (bierPath.getBferNodeId().equals("6.6.6.6")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),
                        "1.1.1.1","2.2.2.2","3.3.3.3","6.6.6.6");
                assertEquals(30,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-2","6.6.6.6");
            }
        }

        PortKey portKey14 = new PortKey("1.1.1.1","14.14.14.14");
        Set<BierPathUnifyKey> bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey14);
        assertEquals(1,bierPaths.size());

        PortKey portKey25 = new PortKey("2.2.2.2","25.25.25.25");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey25);
        assertEquals(2,bierPaths.size());

        PortKey portKey23 = new PortKey("2.2.2.2","23.23.23.23");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey23);
        assertEquals(2,bierPaths.size());

        PortKey portKey15 = new PortKey("1.1.1.1","15.15.15.15");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey15);
        assertEquals(6,bierPaths.size());
        Utils.assertPathsPerSubDomain(new SubDomainId(1),3);
        Utils.assertPathsPerSubDomain(new SubDomainId(2),1);

        removeBierInstance("channel-1","1.1.1.1",1);
        removeBierInstance("channel-2","1.1.1.1",1);
        removeBierInstance("channel-3","2.2.2.2",1);
        removeBierInstance("channel-4","1.1.1.1",2);
    }


    @Test
    public void topoChange_LinkRemoveTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);
        buildBierTeInstanceForTest();

        notifyRemoveLink("4.4.4.4","45.45.45.45","54.54.54.54","5.5.5.5");

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        for (SingleBierPath bierPath : bierTeInstance.getAllBierPath()) {
            assertNotEquals(null,bierPath.getPath());
            assertTrue(!bierPath.getPath().isEmpty());
            if (bierPath.getBferNodeId().equals("4.4.4.4")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","4.4.4.4");
                assertEquals(10,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-1","4.4.4.4");
            }
            if (bierPath.getBferNodeId().equals("5.5.5.5")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","2.2.2.2","5.5.5.5");
                assertEquals(20,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-1","5.5.5.5");
            }
            if (bierPath.getBferNodeId().equals("6.6.6.6")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),
                        "1.1.1.1","2.2.2.2","5.5.5.5","6.6.6.6");
                assertEquals(30,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-1","6.6.6.6");
            }
        }

        bierTeInstance = pcePathProvider.getBierTeInstance("channel-2");
        for (SingleBierPath bierPath : bierTeInstance.getAllBierPath()) {
            assertNotEquals(null,bierPath.getPath());
            assertTrue(!bierPath.getPath().isEmpty());
            if (bierPath.getBferNodeId().equals("3.3.3.3")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","2.2.2.2","3.3.3.3");
                assertEquals(20,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-2","3.3.3.3");
            }
            if (bierPath.getBferNodeId().equals("5.5.5.5")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","2.2.2.2","5.5.5.5");
                assertEquals(20,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-2","5.5.5.5");
            }
            if (bierPath.getBferNodeId().equals("6.6.6.6")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),
                        "1.1.1.1","2.2.2.2","3.3.3.3","6.6.6.6");
                assertEquals(30,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-2","6.6.6.6");
            }
        }


        bierTeInstance = pcePathProvider.getBierTeInstance("channel-3");
        for (SingleBierPath bierPath : bierTeInstance.getAllBierPath()) {
            assertNotEquals(null,bierPath.getPath());
            assertTrue(!bierPath.getPath().isEmpty());
            if (bierPath.getBferNodeId().equals("4.4.4.4")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"2.2.2.2","1.1.1.1","4.4.4.4");
                assertEquals(20,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-3","4.4.4.4");
            }
            if (bierPath.getBferNodeId().equals("5.5.5.5")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"2.2.2.2","5.5.5.5");
                assertEquals(10,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-3","5.5.5.5");
            }
        }

        PortKey portKey14 = new PortKey("1.1.1.1","14.14.14.14");
        Set<BierPathUnifyKey> bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey14);
        assertEquals(2,bierPaths.size());

        PortKey portKey25 = new PortKey("2.2.2.2","25.25.25.25");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey25);
        assertEquals(4,bierPaths.size());

        PortKey portKey23 = new PortKey("2.2.2.2","23.23.23.23");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey23);
        assertEquals(2,bierPaths.size());

        PortKey portKey21 = new PortKey("2.2.2.2","21.21.21.21");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey21);
        assertEquals(1,bierPaths.size());

        PortKey portKey45 = new PortKey("4.4.4.4","45.45.45.45");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey45);
        assertEquals(0,bierPaths.size());

        removeBierInstance("channel-1","1.1.1.1",1);
        removeBierInstance("channel-2","1.1.1.1",1);
        removeBierInstance("channel-3","2.2.2.2",1);
    }

    @Test
    public void topoChange_LinkChangeTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);
        buildBierTeInstanceForTest();

        notifyChangeLink("2.2.2.2","23.23.23.23","32.32.32.32","3.3.3.3");

        BierTeInstance bierTeInstance = pcePathProvider.getBierTeInstance("channel-1");
        for (SingleBierPath bierPath : bierTeInstance.getAllBierPath()) {
            assertNotEquals(null,bierPath.getPath());
            assertTrue(!bierPath.getPath().isEmpty());
            if (bierPath.getBferNodeId().equals("4.4.4.4")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","4.4.4.4");
                assertEquals(10,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-1","4.4.4.4");
            }
            if (bierPath.getBferNodeId().equals("5.5.5.5")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","4.4.4.4","5.5.5.5");
                assertEquals(20,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-1","5.5.5.5");
            }
            if (bierPath.getBferNodeId().equals("6.6.6.6")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),
                        "1.1.1.1","4.4.4.4","5.5.5.5","6.6.6.6");
                assertEquals(30,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-1","6.6.6.6");
            }
        }

        bierTeInstance = pcePathProvider.getBierTeInstance("channel-2");
        for (SingleBierPath bierPath : bierTeInstance.getAllBierPath()) {
            assertNotEquals(null,bierPath.getPath());
            assertTrue(!bierPath.getPath().isEmpty());
            if (bierPath.getBferNodeId().equals("3.3.3.3")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","2.2.2.2","3.3.3.3");
                assertEquals(35,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-2","3.3.3.3");
            }
            if (bierPath.getBferNodeId().equals("5.5.5.5")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"1.1.1.1","2.2.2.2","5.5.5.5");
                assertEquals(20,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-2","5.5.5.5");
            }
            if (bierPath.getBferNodeId().equals("6.6.6.6")) {

                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),
                        "1.1.1.1","2.2.2.2","5.5.5.5","6.6.6.6");
                assertEquals(30,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-2","6.6.6.6");
            }
        }


        bierTeInstance = pcePathProvider.getBierTeInstance("channel-3");
        for (SingleBierPath bierPath : bierTeInstance.getAllBierPath()) {
            assertNotEquals(null,bierPath.getPath());
            assertTrue(!bierPath.getPath().isEmpty());
            if (bierPath.getBferNodeId().equals("4.4.4.4")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"2.2.2.2","5.5.5.5","4.4.4.4");
                assertEquals(20,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-3","4.4.4.4");
            }
            if (bierPath.getBferNodeId().equals("5.5.5.5")) {
                Utils.checkPath(ComUtility.transform2PathLink(bierPath.getPath()),"2.2.2.2","5.5.5.5");
                assertEquals(10,bierPath.getPathMetric());

                checkDataStore(bierPath,"channel-3","5.5.5.5");
            }
        }

        PortKey portKey14 = new PortKey("1.1.1.1","14.14.14.14");
        Set<BierPathUnifyKey> bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey14);
        assertEquals(3,bierPaths.size());

        PortKey portKey25 = new PortKey("2.2.2.2","25.25.25.25");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey25);
        assertEquals(4,bierPaths.size());

        PortKey portKey23 = new PortKey("2.2.2.2","23.23.23.23");
        bierPaths = BierTesRecordPerPort.getInstance().getPathsRecord(portKey23);
        assertEquals(1,bierPaths.size());

        removeBierInstance("channel-1","1.1.1.1",1);
        removeBierInstance("channel-2","1.1.1.1",1);
        removeBierInstance("channel-3","2.2.2.2",1);
    }


    @Test
    public void topoChange_LinkAddForTeFrrTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);
        buildTeFrrInstanceForTest();

        notifyAddLink("1.1.1.1", "15.15.15.15", "51.51.51.51", "5.5.5.5");
        ProtectedLink protectedLink1 = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("2.2.2.2", "25.25.25.25", "52.52.52.52", "5.5.5.5", 10)).build();

        TeFrrKey teFrrKey1 = new TeFrrKeyBuilder()
                .setSubDomainId(new SubDomainId(1))
                .setProtectedLink(protectedLink1)
                .build();

        TeFrrInstance teFrrInstance1 = pcePathProvider.getTeFrrInstance(teFrrKey1);
        assertTrue(teFrrInstance1 != null);
        //check excluding links
        assertEquals(4,teFrrInstance1.getExcludingLinks().size());
        BierLink link25 = TopoMockUtils.buildLink("2.2.2.2","25.25.25.25","52.52.52.52","5.5.5.5",10);
        assertTrue(teFrrInstance1.getExcludingLinks().contains(link25));
        BierLink link54 = TopoMockUtils.buildLink("5.5.5.5","54.54.54.54","45.45.45.45","4.4.4.4",10);
        assertTrue(teFrrInstance1.getExcludingLinks().contains(link54));
        BierLink link56 = TopoMockUtils.buildLink("5.5.5.5","56.56.56.56","65.65.65.65","6.6.6.6",10);
        assertTrue(teFrrInstance1.getExcludingLinks().contains(link56));
        BierLink link51 = TopoMockUtils.buildLink("5.5.5.5","51.51.51.51","15.15.15.15","1.1.1.1",10);
        assertTrue(teFrrInstance1.getExcludingLinks().contains(link51));
        assertEquals(4,teFrrInstance1.getAllBackupPath().size());

        for (TeFrrBackupPath backupPath : teFrrInstance1.getAllBackupPath()) {
            if (backupPath.getPathType().equals(PathType.NextHop)) {
                assertEquals("5.5.5.5",backupPath.getNodeId());
                Utils.checkPath(Utils.transPath(backupPath.getPath()), "2.2.2.2", "1.1.1.1", "5.5.5.5");
            } else {
                if (backupPath.getNodeId().equals("4.4.4.4")) {
                    Utils.checkPath(Utils.transPath(backupPath.getPath()), "2.2.2.2", "1.1.1.1", "4.4.4.4");
                } else if (backupPath.getNodeId().equals("1.1.1.1")) {
                    Utils.checkPath(Utils.transPath(backupPath.getPath()), "2.2.2.2", "1.1.1.1");
                } else  {
                    assertEquals("6.6.6.6",backupPath.getNodeId());
                    Utils.checkPath(Utils.transPath(backupPath.getPath()), "2.2.2.2", "3.3.3.3", "6.6.6.6");
                }
            }
        }
        ProtectedLink protectedLink2 = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("1.1.1.1", "12.12.12.12", "21.21.21.21", "2.2.2.2", 10)).build();
        TeFrrKey teFrrKey2 = new TeFrrKeyBuilder()
                .setSubDomainId(new SubDomainId(1))
                .setProtectedLink(protectedLink2)
                .build();
        TeFrrInstance teFrrInstance2 = pcePathProvider.getTeFrrInstance(teFrrKey2);
        assertTrue(teFrrInstance2 != null);
        //check excluding links
        BierLink link12 = TopoMockUtils.buildLink("1.1.1.1","12.12.12.12","21.21.21.21","2.2.2.2",10);
        BierLink link23 = TopoMockUtils.buildLink("2.2.2.2","23.23.23.23","32.32.32.32","3.3.3.3",10);
        assertEquals(3,teFrrInstance2.getExcludingLinks().size());
        assertTrue(teFrrInstance2.getExcludingLinks().contains(link12));
        assertTrue(teFrrInstance2.getExcludingLinks().contains(link23));
        assertTrue(teFrrInstance2.getExcludingLinks().contains(link25));
        assertEquals(3,teFrrInstance2.getAllBackupPath().size());

        for (TeFrrBackupPath backupPath : teFrrInstance2.getAllBackupPath()) {
            if (backupPath.getPathType().equals(PathType.NextHop)) {
                assertEquals("2.2.2.2",backupPath.getNodeId());
                Utils.checkPath(Utils.transPath(backupPath.getPath()), "1.1.1.1", "5.5.5.5", "2.2.2.2");
            } else {
                if (backupPath.getNodeId().equals("5.5.5.5")) {
                    Utils.checkPath(Utils.transPath(backupPath.getPath()), "1.1.1.1", "5.5.5.5");
                } else  {
                    assertEquals("3.3.3.3",backupPath.getNodeId());
                    Utils.checkPath(Utils.transPath(backupPath.getPath()), "1.1.1.1", "5.5.5.5", "6.6.6.6", "3.3.3.3");
                }
            }
        }
        removeTeFrrInstance();
    }


    @Test
    public void topoChange_LinkRemoveForTeFrrTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node());
        TopoMockUtils.buildBierTeNodeInOneSubDomain(true);
        buildTeFrrInstanceForTest();

        notifyRemoveLink("5.5.5.5", "56.56.56.56", "65.65.65.65", "6.6.6.6");
        ProtectedLink protectedLink1 = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("2.2.2.2", "25.25.25.25", "52.52.52.52", "5.5.5.5", 10)).build();

        TeFrrKey teFrrKey1 = new TeFrrKeyBuilder()
                .setSubDomainId(new SubDomainId(1))
                .setProtectedLink(protectedLink1)
                .build();

        TeFrrInstance teFrrInstance1 = pcePathProvider.getTeFrrInstance(teFrrKey1);
        assertTrue(teFrrInstance1 != null);
        //check excluding links
        BierLink link25 = TopoMockUtils.buildLink("2.2.2.2","25.25.25.25","52.52.52.52","5.5.5.5",10);
        BierLink link54 = TopoMockUtils.buildLink("5.5.5.5","54.54.54.54","45.45.45.45","4.4.4.4",10);
        //BierLink link56 = TopoMockUtils.buildLink("5.5.5.5","56.56.56.56","65.65.65.65","6.6.6.6",10);
        //BierLink link51 = TopoMockUtils.buildLink("5.5.5.5","51.51.51.51","15.15.15.15","1.1.1.1",10);
        assertEquals(2,teFrrInstance1.getExcludingLinks().size());
        assertTrue(teFrrInstance1.getExcludingLinks().contains(link25));
        assertTrue(teFrrInstance1.getExcludingLinks().contains(link54));
        //assertTrue(teFrrInstance1.getExcludingLinks().contains(link56));
        //assertTrue(teFrrInstance1.getExcludingLinks().contains(link51));
        assertEquals(2,teFrrInstance1.getAllBackupPath().size());

        for (TeFrrBackupPath backupPath : teFrrInstance1.getAllBackupPath()) {
            if (backupPath.getPathType().equals(PathType.NextHop)) {
                assertEquals("5.5.5.5",backupPath.getNodeId());
                Utils.checkPath(Utils.transPath(backupPath.getPath()), "2.2.2.2", "1.1.1.1", "4.4.4.4","5.5.5.5");
            } else {
                assertEquals("4.4.4.4", backupPath.getNodeId());
                Utils.checkPath(Utils.transPath(backupPath.getPath()), "2.2.2.2", "1.1.1.1", "4.4.4.4");
            }
        }
        ProtectedLink protectedLink2 = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("1.1.1.1", "12.12.12.12", "21.21.21.21", "2.2.2.2", 10)).build();
        TeFrrKey teFrrKey2 = new TeFrrKeyBuilder()
                .setSubDomainId(new SubDomainId(1))
                .setProtectedLink(protectedLink2)
                .build();
        TeFrrInstance teFrrInstance2 = pcePathProvider.getTeFrrInstance(teFrrKey2);
        assertTrue(teFrrInstance2 != null);
        //check excluding links
        BierLink link12 = TopoMockUtils.buildLink("1.1.1.1","12.12.12.12","21.21.21.21","2.2.2.2",10);
        BierLink link23 = TopoMockUtils.buildLink("2.2.2.2","23.23.23.23","32.32.32.32","3.3.3.3",10);
        assertEquals(3,teFrrInstance2.getExcludingLinks().size());
        assertTrue(teFrrInstance2.getExcludingLinks().contains(link12));
        assertTrue(teFrrInstance2.getExcludingLinks().contains(link23));
        assertTrue(teFrrInstance2.getExcludingLinks().contains(link25));
        assertEquals(3,teFrrInstance2.getAllBackupPath().size());

        for (TeFrrBackupPath backupPath : teFrrInstance2.getAllBackupPath()) {
            if (backupPath.getPathType().equals(PathType.NextHop)) {
                assertEquals("2.2.2.2",backupPath.getNodeId());
                Utils.checkPath(Utils.transPath(backupPath.getPath()), "1.1.1.1", "4.4.4.4","5.5.5.5", "2.2.2.2");
            } else {
                if (backupPath.getNodeId().equals("5.5.5.5")) {
                    Utils.checkPath(Utils.transPath(backupPath.getPath()), "1.1.1.1", "4.4.4.4","5.5.5.5");
                } else  {
                    assertEquals("3.3.3.3",backupPath.getNodeId());
                    assertTrue(backupPath.getPath().isEmpty());
                }
            }
        }
        removeTeFrrInstance();
    }

    private void buildTeFrrInstanceForTest() throws ExecutionException, InterruptedException {
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
        protectedLink = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("1.1.1.1", "12.12.12.12", "21.21.21.21", "2.2.2.2", 10)).build();
        input = new CreateTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink)
                        .build())
                .build();
        output = pcePathProvider.createTeFrrPath(input);
        assertTrue(output.get().isSuccessful());

    }


    private void removeTeFrrInstance() throws ExecutionException, InterruptedException {
        ProtectedLink protectedLink = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("2.2.2.2", "25.25.25.25", "52.52.52.52", "5.5.5.5", 10)).build();
        RemoveTeFrrPathInput input = new RemoveTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink)
                        .build())
                .build();
        Future<RpcResult<Void>> output = pcePathProvider.removeTeFrrPath(input);
        assertTrue(output.get().isSuccessful());
        protectedLink = new ProtectedLinkBuilder(TopoMockUtils
                .buildLinkEx("1.1.1.1", "12.12.12.12", "21.21.21.21", "2.2.2.2", 10)).build();
        input = new RemoveTeFrrPathInputBuilder()
                .setTeFrrKey(new TeFrrKeyBuilder()
                        .setSubDomainId(new SubDomainId(1))
                        .setProtectedLink(protectedLink)
                        .build())
                .build();
        output = pcePathProvider.removeTeFrrPath(input);
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


    private void buildBierTeInstanceForTest() throws ExecutionException, InterruptedException {

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
                .setChannelName("channel-2")
                .setSubDomainId(new SubDomainId(1))
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

    private void notifyAddLink(String srcNode, String srcTp, String destTp, String destNode) {
        Utils.writeLinksToDB(TopoMockUtils.buildLinkPairWithMetric(srcNode,srcTp,destTp,destNode,10));
        LinkAdd linkAdd1 = new LinkAddBuilder()
                .setTopoId(TopologyProvider.DEFAULT_TOPO_ID_STRING)
                .setAddLink(new AddLinkBuilder(TopoMockUtils.buildLink(srcNode,srcTp,destTp,destNode,10)).build())
                .build();
        topologyProvider.onLinkAdd(linkAdd1);
        LinkAdd linkAdd2 = new LinkAddBuilder()
                .setTopoId(TopologyProvider.DEFAULT_TOPO_ID_STRING)
                .setAddLink(new AddLinkBuilder(TopoMockUtils.buildLink(destNode,destTp,srcTp,srcNode,10)).build())
                .build();
        topologyProvider.onLinkAdd(linkAdd2);
    }

    private void notifyRemoveLink(String srcNode, String srcTp, String destTp, String destNode) {
        Utils.deleteLinkInDB(TopoMockUtils.buildLink(srcNode,srcTp,destTp,destNode,10));
        Utils.deleteLinkInDB(TopoMockUtils.buildLink(destNode,destTp,srcTp,srcNode,10));
        LinkRemove linkRemove1 = new LinkRemoveBuilder()
                .setTopoId(TopologyProvider.DEFAULT_TOPO_ID_STRING)
                .setRemoveLink(new RemoveLinkBuilder(TopoMockUtils.buildLink(srcNode,srcTp,destTp,destNode,10)).build())
                .build();
        topologyProvider.onLinkRemove(linkRemove1);
        LinkRemove linkRemove2 = new LinkRemoveBuilder()
                .setTopoId(TopologyProvider.DEFAULT_TOPO_ID_STRING)
                .setRemoveLink(new RemoveLinkBuilder(TopoMockUtils.buildLink(destNode,destTp,srcTp,srcNode,10)).build())
                .build();
        topologyProvider.onLinkRemove(linkRemove2);
    }

    private void notifyChangeLink(String srcNode, String srcTp, String destTp, String destNode) {
        Utils.writeLinksToDB(TopoMockUtils.buildLinkPairWithMetric(srcNode,srcTp,destTp,destNode,25));
        LinkChange linkChange1 = new LinkChangeBuilder()
                .setTopoId(TopologyProvider.DEFAULT_TOPO_ID_STRING)
                .setOldLink(new OldLinkBuilder(TopoMockUtils.buildLink(srcNode,srcTp,destTp,destNode,10)).build())
                .setNewLink(new NewLinkBuilder(TopoMockUtils.buildLink(srcNode,srcTp,destTp,destNode,25)).build())
                .build();
        topologyProvider.onLinkChange(linkChange1);
        LinkChange linkChange2 = new LinkChangeBuilder()
                .setTopoId(TopologyProvider.DEFAULT_TOPO_ID_STRING)
                .setOldLink(new OldLinkBuilder(TopoMockUtils.buildLink(destNode,destTp,srcTp,srcNode,10)).build())
                .setNewLink(new NewLinkBuilder(TopoMockUtils.buildLink(destNode,destTp,srcTp,srcNode,25)).build())
                .build();
        topologyProvider.onLinkChange(linkChange2);
    }


    private void checkDataStore(SingleBierPath bierPath, String channelName, String bferNode) {
        BierPath bierPathData = Utils.readBierPath(channelName,bferNode);
        assertEquals(bierPath.getPathMetric(),bierPathData.getPathMetric().longValue());
        Utils.assertBierPath(ComUtility.transform2PathLink(bierPath.getPath()),bierPathData);

    }

}

