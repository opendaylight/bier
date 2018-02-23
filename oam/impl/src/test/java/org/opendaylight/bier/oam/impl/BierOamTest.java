/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.oam.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.bier.adapter.api.BierOamStartEchoRequestCheck;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverNotifyBierEchoReply;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.CheckType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.ModeType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.NetworkType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.StartEchoRequestInput;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.StartEchoRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.StartEchoRequestOutput;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.start.echo.request.input.TargetNodeIds;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.start.echo.request.input.TargetNodeIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.BierServiceApiService;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringInput;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringInputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringOutput;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.GetTargetBitstringOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.PathInfo;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.PathInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.ReplyMode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.address.TargetBfers;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.address.TargetBfersBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.ContinuityCheckInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.ContinuityCheckOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.PathDiscoveryInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.PathDiscoveryOutput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({NotificationProvider.class})
public class BierOamTest extends AbstractConcurrentDataBrokerTest {

    private DataBroker dataBroker;
    private OamImpl oamImpl;
    private BierOamStartEchoRequestCheck bierOamConfig;
    private BierServiceApiService serviceApi;
//    NotificationProvider notificationProvider;

    @Before
    public void setUp() throws Exception {
        dataBroker = getDataBroker();
        DbProvider.getInstance().setDataBroker(dataBroker);
        bierOamConfig = mock(BierOamStartEchoRequestCheck.class);
        serviceApi = mock(BierServiceApiService.class);
//        notificationProvider = mock(NotificationProvider.class);
//        PowerMockito.mockStatic(NotificationProvider.class);
//        PowerMockito.when(NotificationProvider.getInstance()).thenReturn(notificationProvider);
        new ConfigureOam(bierOamConfig);
        oamImpl = new OamImpl();
        oamImpl.setExecutor(MoreExecutors.newDirectExecutorService());
        oamImpl.setBierService(serviceApi);
        BierBasicMockUtils.buildBierTopoAndChannel();
    }

    @After
    public void tearDown() throws Exception {
        oamImpl.destroy();
    }

    @Test
    public void startOamRequestPingCfgFailTest() throws ExecutionException, InterruptedException {
        List<TargetNodeIds> targetNodes = new ArrayList<>();
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node2").build());
        RpcResult<StartEchoRequestOutput> result = startOamRequest("c1",CheckType.OnDemand,ModeType.Ping,
                ReplyMode.ReplyViaIPv4IPv6UDPPacket,targetNodes,false);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(result.getResult().getConfigureResult().getErrorCause().equals("config failed! ; "));
        //verify(bierOamConfig,times(0)).startBierContinuityCheck(eq("node1"),any(ContinuityCheckInput.class));
        OamInstanceKey instanceKey = new OamInstanceKey(new SubDomainId(1),"node1",new BfrId(11),
                BierBasicMockUtils.getEgressNodes("c1"),BierBasicMockUtils.getBfers("c1"),targetNodes,
                NetworkType.Bier,CheckType.OnDemand,ModeType.Ping, ReplyMode.ReplyViaIPv4IPv6UDPPacket,255);
        OamInstance oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance == null);
        for (TargetNodeIds targetNode :targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1),"node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c1"),BierBasicMockUtils.getBfers("c1"),
                    targetNode.getTargetNodeId(), NetworkType.Bier,CheckType.OnDemand,ModeType.Ping,
                    ReplyMode.ReplyViaIPv4IPv6UDPPacket);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Assert.assertTrue(oamRequest == null);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances == null);
        }
    }

    @Test
    public void startOamRequestPingTimeOutTest() throws ExecutionException, InterruptedException {
    /*    when(bierOamConfig.startBierContinuityCheck(anyString(), any(ContinuityCheckInput.class)))
                .thenReturn(Futures.immediateFuture(RpcResultBuilder.<ContinuityCheckOutput>success().build()));
*/
        List<TargetNodeIds> targetNodes = new ArrayList<>();
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node2").build());
        RpcResult<StartEchoRequestOutput> result = startOamRequest("c1",CheckType.OnDemand,ModeType.Ping,
                ReplyMode.ReplyViaIPv4IPv6UDPPacket,targetNodes,true);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        verify(bierOamConfig,times(1)).startBierContinuityCheck(eq("node1"),any(ContinuityCheckInput.class));
        OamInstanceKey instanceKey = new OamInstanceKey(new SubDomainId(1),"node1",new BfrId(11),
                BierBasicMockUtils.getEgressNodes("c1"),BierBasicMockUtils.getBfers("c1"),targetNodes,
                NetworkType.Bier,CheckType.OnDemand,ModeType.Ping, ReplyMode.ReplyViaIPv4IPv6UDPPacket,255);
        OamInstance oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance != null);
        Assert.assertTrue(oamInstance.isTimerActive());
        Assert.assertTrue(oamInstance.getOamRequestSet().size() == 1);
        for (TargetNodeIds targetNode :targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1),"node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c1"),BierBasicMockUtils.getBfers("c1"),
                    targetNode.getTargetNodeId(), NetworkType.Bier,CheckType.OnDemand,ModeType.Ping,
                    ReplyMode.ReplyViaIPv4IPv6UDPPacket);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances != null);
            Assert.assertTrue(oamInstances.size() == 1);
            Assert.assertTrue(oamInstances.contains(oamInstance));
            Assert.assertTrue(oamInstance.getOamRequestSet().contains(oamRequest));
            Assert.assertTrue(oamRequest.isTimerActive());
            oamRequest.stopTimer();
            oamRequest.destroy();
        }
        oamInstance.stopTimer();
        oamInstance.notifyEchoReply();
        oamInstance.destroy();
/*        verify(notificationProvider,times(1)).notify(Utils.buildPingTimeOutReply(new SubDomainId(1),
                "node1",BierBasicMockUtils.getEgressNodes("c1"),targetNodes));*/

        oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance == null);
        for (TargetNodeIds targetNode :targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1),"node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c1"),BierBasicMockUtils.getBfers("c1"),
                    targetNode.getTargetNodeId(), NetworkType.Bier,CheckType.OnDemand,ModeType.Ping,
                    ReplyMode.ReplyViaIPv4IPv6UDPPacket);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Assert.assertTrue(oamRequest == null);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances == null);
        }
    }

    @Test
    public void receiveOamReplyPingSuccessTest() throws ExecutionException, InterruptedException {
        List<TargetNodeIds> targetNodes = new ArrayList<>();
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node2").build());
        startOamRequest("c1",CheckType.OnDemand,ModeType.Ping,
                ReplyMode.DoNotReply,targetNodes,true);
        List<TargetBfers> targetBfers = new ArrayList<>();
        targetBfers.add(new TargetBfersBuilder().setBierBfrid(new BfrId(2)).build());
        receiveReply(new SubDomainId(1),"c1",11,targetBfers,ReplyMode.DoNotReply,ModeType.Ping,true);
        OamInstanceKey instanceKey = new OamInstanceKey(new SubDomainId(1),"node1",new BfrId(11),
                BierBasicMockUtils.getEgressNodes("c1"),BierBasicMockUtils.getBfers("c1"),targetNodes,
                NetworkType.Bier,CheckType.OnDemand,ModeType.Ping, ReplyMode.DoNotReply,255);
        OamInstance oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance == null);
        for (TargetNodeIds targetNode :targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1),"node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c1"),BierBasicMockUtils.getBfers("c1"),
                    targetNode.getTargetNodeId(), NetworkType.Bier,CheckType.OnDemand,ModeType.Ping,
                    ReplyMode.DoNotReply);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Assert.assertTrue(oamRequest == null);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances == null);
        }
    }


    @Test
    public void receiveOamReplyPingPartialFailedTest() throws ExecutionException, InterruptedException {
        List<TargetNodeIds> targetNodes = new ArrayList<>();
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node2").build());
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node3").build());

        RpcResult<StartEchoRequestOutput> result = startOamRequest("c2", CheckType.OnDemand, ModeType.Ping,
                ReplyMode.DoNotReply, targetNodes, true);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        verify(bierOamConfig, times(2)).startBierContinuityCheck(eq("node4"), any(ContinuityCheckInput.class));
        List<TargetBfers> targetBfers = new ArrayList<>();
        targetBfers.add(new TargetBfersBuilder().setBierBfrid(new BfrId(2)).build());
        receiveReply(new SubDomainId(1),"c2",44,targetBfers,ReplyMode.DoNotReply,ModeType.Ping,false);
        targetBfers.clear();
        targetBfers.add(new TargetBfersBuilder().setBierBfrid(new BfrId(33)).build());
        receiveReply(new SubDomainId(1),"c2",44,targetBfers,ReplyMode.DoNotReply,ModeType.Ping,true);
        OamInstanceKey instanceKey = new OamInstanceKey(new SubDomainId(1), "node4",new BfrId(44),
                BierBasicMockUtils.getEgressNodes("c2"), BierBasicMockUtils.getBfers("c2"), targetNodes,
                NetworkType.Bier, CheckType.OnDemand, ModeType.Ping, ReplyMode.DoNotReply, 255);
        OamInstance oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance == null);
        for (TargetNodeIds targetNode : targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1), "node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c1"), BierBasicMockUtils.getBfers("c1"),
                    targetNode.getTargetNodeId(), NetworkType.Bier, CheckType.OnDemand, ModeType.Trace,
                    ReplyMode.ReplyViaIPv4IPv6UDPPacket);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Assert.assertTrue(oamRequest == null);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances == null);
        }
    }

    @Test
    public void startOamRequestTraceTimeOutTest() throws ExecutionException, InterruptedException {

        List<TargetNodeIds> targetNodes = new ArrayList<>();
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node2").build());
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node3").build());

        RpcResult<StartEchoRequestOutput> result = startOamRequest("c1", CheckType.OnDemand, ModeType.Trace,
                ReplyMode.ReplyViaIPv4IPv6UDPPacket, targetNodes, true);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        verify(bierOamConfig, times(2)).startBierPathDiscovery(eq("node1"), any(PathDiscoveryInput.class));
        OamInstanceKey instanceKey = new OamInstanceKey(new SubDomainId(1), "node1",new BfrId(11),
                BierBasicMockUtils.getEgressNodes("c1"), BierBasicMockUtils.getBfers("c1"), targetNodes,
                NetworkType.Bier, CheckType.OnDemand, ModeType.Trace, ReplyMode.ReplyViaIPv4IPv6UDPPacket, 255);
        OamInstance oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance != null);
        Assert.assertTrue(oamInstance.isTimerActive());
        Assert.assertTrue(oamInstance.getOamRequestSet().size() == 2);
        for (TargetNodeIds targetNode : targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1), "node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c1"), BierBasicMockUtils.getBfers("c1"),
                    targetNode.getTargetNodeId(), NetworkType.Bier, CheckType.OnDemand, ModeType.Trace,
                    ReplyMode.ReplyViaIPv4IPv6UDPPacket);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances != null);
            Assert.assertTrue(oamInstances.size() == 1);
            Assert.assertTrue(oamInstances.contains(oamInstance));
            Assert.assertTrue(oamInstance.getOamRequestSet().contains(oamRequest));
            Assert.assertTrue(oamRequest.isTimerActive());
            oamRequest.stopTimer();
            oamRequest.destroy();
        }
        oamInstance.stopTimer();
        oamInstance.notifyEchoReply();
        oamInstance.destroy();

        oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance == null);
        for (TargetNodeIds targetNode : targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1), "node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c1"), BierBasicMockUtils.getBfers("c1"),
                    targetNode.getTargetNodeId(), NetworkType.Bier, CheckType.OnDemand, ModeType.Trace,
                    ReplyMode.ReplyViaIPv4IPv6UDPPacket);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Assert.assertTrue(oamRequest == null);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances == null);
        }
    }

    @Test
    public void receiveOamReplyTraceSuccessTest() throws ExecutionException, InterruptedException {
        List<TargetNodeIds> targetNodes = new ArrayList<>();
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node2").build());
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node3").build());

        startOamRequest("c1", CheckType.OnDemand, ModeType.Trace, ReplyMode.ReplyViaIPv4IPv6UDPPacket,
                targetNodes, true);
        List<TargetBfers> targetBfers = new ArrayList<>();
        targetBfers.add(new TargetBfersBuilder().setBierBfrid(new BfrId(2)).build());
        targetBfers.add(new TargetBfersBuilder().setBierBfrid(new BfrId(33)).build());
        receiveReply(new SubDomainId(1),"c1",11,targetBfers,ReplyMode.ReplyViaIPv4IPv6UDPPacket,ModeType.Trace,true);
        OamInstanceKey instanceKey = new OamInstanceKey(new SubDomainId(1), "node1",new BfrId(11),
                BierBasicMockUtils.getEgressNodes("c1"), BierBasicMockUtils.getBfers("c1"), targetNodes,
                NetworkType.Bier, CheckType.OnDemand, ModeType.Trace, ReplyMode.ReplyViaIPv4IPv6UDPPacket, 255);
        OamInstance oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance == null);
        for (TargetNodeIds targetNode : targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1), "node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c1"), BierBasicMockUtils.getBfers("c1"),
                    targetNode.getTargetNodeId(), NetworkType.Bier, CheckType.OnDemand, ModeType.Trace,
                    ReplyMode.ReplyViaIPv4IPv6UDPPacket);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Assert.assertTrue(oamRequest == null);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances == null);
        }
    }

    @Test
    public void receiveOamReplyTracePartialTimeOutTest() throws ExecutionException, InterruptedException {
        List<TargetNodeIds> targetNodes = new ArrayList<>();
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node2").build());
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node3").build());
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node5").build());

        startOamRequest("c3", CheckType.OnDemand, ModeType.Trace, ReplyMode.ReplyViaIPv4IPv6UDPPacket,
                targetNodes, true);
        List<TargetBfers> targetBfers = new ArrayList<>();
        targetBfers.add(new TargetBfersBuilder().setBierBfrid(new BfrId(2)).build());
        targetBfers.add(new TargetBfersBuilder().setBierBfrid(new BfrId(33)).build());
        receiveReply(new SubDomainId(1),"c3",11,targetBfers,ReplyMode.ReplyViaIPv4IPv6UDPPacket,ModeType.Trace,true);
        OamInstanceKey instanceKey = new OamInstanceKey(new SubDomainId(1), "node1",new BfrId(11),
                BierBasicMockUtils.getEgressNodes("c3"), BierBasicMockUtils.getBfers("c3"), targetNodes,
                NetworkType.Bier, CheckType.OnDemand, ModeType.Trace, ReplyMode.ReplyViaIPv4IPv6UDPPacket, 255);
        OamInstance oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance != null);
        Assert.assertTrue(oamInstance.getOamRequestSet().size() == 1);
        for (TargetNodeIds targetNode : targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1), "node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c3"), BierBasicMockUtils.getBfers("c3"),
                    targetNode.getTargetNodeId(), NetworkType.Bier, CheckType.OnDemand, ModeType.Trace,
                    ReplyMode.ReplyViaIPv4IPv6UDPPacket);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            if (targetNode.getTargetNodeId() == "node5") {
                Assert.assertTrue(oamRequest != null);
                Assert.assertTrue(oamInstances != null);
                Assert.assertTrue(oamInstances.contains(oamInstance));
                Assert.assertTrue(oamInstance.getOamRequestSet().contains(oamRequest));
                oamRequest.stopTimer();
                oamRequest.destroy();
            } else {
                Assert.assertTrue(oamRequest == null);
                Assert.assertTrue(oamInstances == null);
            }
        }
        oamInstance.stopTimer();
        oamInstance.notifyEchoReply();
        oamInstance.destroy();
        oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance == null);
        for (TargetNodeIds targetNode : targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1), "node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c3"), BierBasicMockUtils.getBfers("c3"),
                    targetNode.getTargetNodeId(), NetworkType.Bier, CheckType.OnDemand, ModeType.Trace,
                    ReplyMode.ReplyViaIPv4IPv6UDPPacket);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Assert.assertTrue(oamRequest == null);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances == null);
        }
    }


    @Test
    public void multiOamInstanceTest() throws ExecutionException, InterruptedException {
        List<TargetNodeIds> targetNodes3 = new ArrayList<>();
        targetNodes3.add(new TargetNodeIdsBuilder().setTargetNodeId("node2").build());
        targetNodes3.add(new TargetNodeIdsBuilder().setTargetNodeId("node3").build());
        startOamRequest("c3", CheckType.OnDemand, ModeType.Trace, ReplyMode.ReplyViaIPv4IPv6UDPPacket,
                targetNodes3, true);
        List<TargetNodeIds> targetNodes4 = new ArrayList<>(targetNodes3);
        targetNodes4.add(new TargetNodeIdsBuilder().setTargetNodeId("node5").build());

        startOamRequest("c4", CheckType.OnDemand, ModeType.Trace, ReplyMode.ReplyViaIPv4IPv6UDPPacket,
                targetNodes4, true);
        List<TargetBfers> targetBfers = new ArrayList<>();
        targetBfers.add(new TargetBfersBuilder().setBierBfrid(new BfrId(2)).build());
        targetBfers.add(new TargetBfersBuilder().setBierBfrid(new BfrId(33)).build());
        receiveReply(new SubDomainId(1),"c3",11,targetBfers,ReplyMode.ReplyViaIPv4IPv6UDPPacket,ModeType.Trace,true);
        OamInstanceKey instanceKey3 = new OamInstanceKey(new SubDomainId(1), "node1",new BfrId(11),
                BierBasicMockUtils.getEgressNodes("c3"), BierBasicMockUtils.getBfers("c3"), targetNodes3,
                NetworkType.Bier, CheckType.OnDemand, ModeType.Trace, ReplyMode.ReplyViaIPv4IPv6UDPPacket, 255);
        OamInstance oamInstance3 = oamImpl.getOamInstance(instanceKey3);
        Assert.assertTrue(oamInstance3 == null);
        OamInstanceKey instanceKey4 = new OamInstanceKey(new SubDomainId(1), "node1",new BfrId(11),
                BierBasicMockUtils.getEgressNodes("c4"), BierBasicMockUtils.getBfers("c4"), targetNodes4,
                NetworkType.Bier, CheckType.OnDemand, ModeType.Trace, ReplyMode.ReplyViaIPv4IPv6UDPPacket, 255);
        OamInstance oamInstance4 = oamImpl.getOamInstance(instanceKey4);
        Assert.assertTrue(oamInstance4 != null);
        Assert.assertTrue(oamInstance4.getOamRequestSet().size() == 1);
        for (TargetNodeIds targetNode : targetNodes4) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1), "node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c3"), BierBasicMockUtils.getBfers("c3"),
                    targetNode.getTargetNodeId(), NetworkType.Bier, CheckType.OnDemand, ModeType.Trace,
                    ReplyMode.ReplyViaIPv4IPv6UDPPacket);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            //SingleOamRequest oamRequest3 = oamImpl.getSingleRequest(oamRequestKey3);
            //Set<OamInstance> oamInstances3 = oamImpl.getRequestInstanceSet(oamRequestKey3);
            if (targetNode.getTargetNodeId() == "node5") {
                Assert.assertTrue(oamRequest != null);
                Assert.assertTrue(oamInstances != null);
                Assert.assertTrue(oamInstances.contains(oamInstance4));
                Assert.assertTrue(!oamInstances.contains(oamInstance3));
                Assert.assertTrue(oamInstance4.getOamRequestSet().contains(oamRequest));
                oamRequest.stopTimer();
                oamRequest.destroy();
            } else {
                Assert.assertTrue(oamRequest == null);
                Assert.assertTrue(oamInstances == null);
            }
        }
        oamInstance4.stopTimer();
        oamInstance4.notifyEchoReply();
        oamInstance4.destroy();
        oamInstance4 = oamImpl.getOamInstance(instanceKey4);
        Assert.assertTrue(oamInstance4 == null);
        for (TargetNodeIds targetNode : targetNodes4) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1), "node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c3"), BierBasicMockUtils.getBfers("c3"),
                    targetNode.getTargetNodeId(), NetworkType.Bier, CheckType.OnDemand, ModeType.Trace,
                    ReplyMode.ReplyViaIPv4IPv6UDPPacket);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Assert.assertTrue(oamRequest == null);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances == null);
        }
        Thread.sleep(50000);
    }

    @Test
    public void receivePingForTeSuccessTest() throws ExecutionException, InterruptedException {
        List<TargetNodeIds> targetNodes = new ArrayList<>();
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node2").build());
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node3").build());

        when(serviceApi.getTargetBitstring(Utils.buildGetTargetBitstringInput("c5",targetNodes)))
                .thenReturn(Utils.buildGetTargetBitstringOutput("c5",targetNodes));

        RpcResult<StartEchoRequestOutput> result = startOamRequest("c5", CheckType.OnDemand, ModeType.Ping,
                ReplyMode.DoNotReply, targetNodes, true);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
/*        verify(bierOamConfig, times(1)).startBierContinuityCheck(eq("node4"),
                eq(Utils.buildBierContinuityCheck("c5","node2")));
        verify(bierOamConfig, times(1)).startBierContinuityCheck(eq("node4"),
                eq(Utils.buildBierContinuityCheck("c5","node3")));*/

        verify(bierOamConfig, times(2)).startBierContinuityCheck(eq("node4"), any(ContinuityCheckInput.class));

        //List<TargetBfers> targetBfers = new ArrayList<>();
        //targetBfers.add(new TargetBfersBuilder().setBierBfrid(new BfrId(2)).build());
        //receiveReply(new SubDomainId(1),"c5",44,targetBfers,ReplyMode.DoNotReply,ModeType.Ping,false);
        receiveTeReply("c5",new SubDomainId(1),"node2",ModeType.Ping,true,false,
                new TargetBfersBuilder().setBierBfrid(new BfrId(2)).build(),false);
        //targetBfers.clear();
        //targetBfers.add(new TargetBfersBuilder().setBierBfrid(new BfrId(33)).build());
        //receiveReply(new SubDomainId(1),"c5",44,targetBfers,ReplyMode.DoNotReply,ModeType.Ping,true);
        receiveTeReply("c5",new SubDomainId(1),"node3",ModeType.Ping,true,false,
                new TargetBfersBuilder().setBierBfrid(new BfrId(33)).build(),true);
        OamInstanceKey instanceKey = new OamInstanceKey(new SubDomainId(1), "node4",new BfrId(44),
                BierBasicMockUtils.getEgressNodes("c5"), BierBasicMockUtils.getBfers("c5"), targetNodes,
                NetworkType.BierTe, CheckType.OnDemand, ModeType.Ping, ReplyMode.DoNotReply, 255);
        OamInstance oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance == null);
        Assert.assertTrue(oamImpl.getBierTeAddressMap().isEmpty());
        for (TargetNodeIds targetNode : targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1), "node4",new BfrId(44),
                    BierBasicMockUtils.getEgressNodes("c5"), BierBasicMockUtils.getBfers("c5"),
                    targetNode.getTargetNodeId(), NetworkType.BierTe, CheckType.OnDemand, ModeType.Ping,
                    ReplyMode.DoNotReply);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Assert.assertTrue(oamRequest == null);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances == null);
        }
    }


    @Test
    public void receiveTraceForTeSuccessTest() throws ExecutionException, InterruptedException {
        List<TargetNodeIds> targetNodes = new ArrayList<>();
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node2").build());
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node3").build());
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node5").build());

        when(serviceApi.getTargetBitstring(Utils.buildGetTargetBitstringInput("c6",targetNodes)))
                .thenReturn(Utils.buildGetTargetBitstringOutput("c6",targetNodes));

        RpcResult<StartEchoRequestOutput> result = startOamRequest("c6", CheckType.OnDemand, ModeType.Trace,
                ReplyMode.DoNotReply, targetNodes, true);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        verify(bierOamConfig, times(3)).startBierPathDiscovery(eq("node1"), any(PathDiscoveryInput.class));

        receiveTeReply("c6",new SubDomainId(1),"node2",ModeType.Trace,true,false,
                new TargetBfersBuilder().setBierBfrid(new BfrId(2)).build(),false);

        receiveTeReply("c6",new SubDomainId(1),"node3",ModeType.Trace,true,false,
                new TargetBfersBuilder().setBierBfrid(new BfrId(33)).build(),true);
        receiveTeReply("c6",new SubDomainId(1),"node5",ModeType.Trace,true,false,
                new TargetBfersBuilder().setBierBfrid(new BfrId(55)).build(),true);

        OamInstanceKey instanceKey = new OamInstanceKey(new SubDomainId(1), "node1",new BfrId(11),
                BierBasicMockUtils.getEgressNodes("c6"), BierBasicMockUtils.getBfers("c6"), targetNodes,
                NetworkType.BierTe, CheckType.OnDemand, ModeType.Trace, ReplyMode.DoNotReply, 255);
        OamInstance oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance == null);
        Assert.assertTrue(oamImpl.getBierTeAddressMap().isEmpty());
        for (TargetNodeIds targetNode : targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1), "node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c6"), BierBasicMockUtils.getBfers("c6"),
                    targetNode.getTargetNodeId(), NetworkType.BierTe, CheckType.OnDemand, ModeType.Trace,
                    ReplyMode.DoNotReply);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Assert.assertTrue(oamRequest == null);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances == null);
        }
    }

    @Test
    public void startTeRequestPingTimeOutTest() throws ExecutionException, InterruptedException {

        List<TargetNodeIds> targetNodes = new ArrayList<>();
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node2").build());
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node3").build());

        when(serviceApi.getTargetBitstring(Utils.buildGetTargetBitstringInput("c5",targetNodes)))
                .thenReturn(Utils.buildGetTargetBitstringOutput("c5",targetNodes));

        RpcResult<StartEchoRequestOutput> result = startOamRequest("c5", CheckType.OnDemand, ModeType.Ping,
                ReplyMode.DoNotReply, targetNodes, true);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        verify(bierOamConfig,times(2)).startBierContinuityCheck(eq("node4"),any(ContinuityCheckInput.class));

        OamInstanceKey instanceKey = new OamInstanceKey(new SubDomainId(1),"node4",new BfrId(44),
                BierBasicMockUtils.getEgressNodes("c5"),BierBasicMockUtils.getBfers("c5"),targetNodes,
                NetworkType.BierTe,CheckType.OnDemand,ModeType.Ping, ReplyMode.DoNotReply,255);
        OamInstance oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance != null);
        Assert.assertTrue(oamInstance.isTimerActive());
        Assert.assertTrue(oamInstance.getOamRequestSet().size() == 2);
        Assert.assertEquals(2,OamImpl.getInstance().getBierTeAddressMap().size());
        for (TargetNodeIds targetNode :targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1),"node4",new BfrId(44),
                    BierBasicMockUtils.getEgressNodes("c5"),BierBasicMockUtils.getBfers("c5"),
                    targetNode.getTargetNodeId(), NetworkType.BierTe,CheckType.OnDemand,ModeType.Ping,
                    ReplyMode.DoNotReply);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances != null);
            Assert.assertTrue(oamInstances.size() == 1);
            Assert.assertTrue(oamInstances.contains(oamInstance));
            Assert.assertTrue(oamInstance.getOamRequestSet().contains(oamRequest));
            Assert.assertTrue(oamRequest.isTimerActive());
            oamRequest.stopTimer();
            oamRequest.destroy();
        }
        oamInstance.stopTimer();
        oamInstance.notifyEchoReply();
        oamInstance.destroy();

        oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance == null);
        Assert.assertTrue(OamImpl.getInstance().getBierTeAddressMap().isEmpty());
        for (TargetNodeIds targetNode :targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1),"node4",new BfrId(44),
                    BierBasicMockUtils.getEgressNodes("c5"),BierBasicMockUtils.getBfers("c5"),
                    targetNode.getTargetNodeId(), NetworkType.BierTe,CheckType.OnDemand,ModeType.Ping,
                    ReplyMode.DoNotReply);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Assert.assertTrue(oamRequest == null);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances == null);
        }
    }

    @Test
    public void startTeRequestTraceTimeOutTest() throws ExecutionException, InterruptedException {

        List<TargetNodeIds> targetNodes = new ArrayList<>();
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node2").build());
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node3").build());
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node5").build());

        when(serviceApi.getTargetBitstring(Utils.buildGetTargetBitstringInput("c6",targetNodes)))
                .thenReturn(Utils.buildGetTargetBitstringOutput("c6",targetNodes));

        RpcResult<StartEchoRequestOutput> result = startOamRequest("c6", CheckType.OnDemand, ModeType.Trace,
                ReplyMode.DoNotReply, targetNodes, true);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        verify(bierOamConfig,times(3)).startBierPathDiscovery(eq("node1"),any(PathDiscoveryInput.class));

        OamInstanceKey instanceKey = new OamInstanceKey(new SubDomainId(1),"node1",new BfrId(11),
                BierBasicMockUtils.getEgressNodes("c6"),BierBasicMockUtils.getBfers("c6"),targetNodes,
                NetworkType.BierTe,CheckType.OnDemand,ModeType.Trace, ReplyMode.DoNotReply,255);
        OamInstance oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance != null);
        Assert.assertTrue(oamInstance.isTimerActive());
        Assert.assertTrue(oamInstance.getOamRequestSet().size() == 3);
        Assert.assertEquals(3,OamImpl.getInstance().getBierTeAddressMap().size());
        for (TargetNodeIds targetNode :targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1),"node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c6"),BierBasicMockUtils.getBfers("c6"),
                    targetNode.getTargetNodeId(), NetworkType.BierTe,CheckType.OnDemand,ModeType.Trace,
                    ReplyMode.DoNotReply);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances != null);
            Assert.assertTrue(oamInstances.size() == 1);
            Assert.assertTrue(oamInstances.contains(oamInstance));
            Assert.assertTrue(oamInstance.getOamRequestSet().contains(oamRequest));
            Assert.assertTrue(oamRequest.isTimerActive());
            oamRequest.stopTimer();
            oamRequest.destroy();
        }
        oamInstance.stopTimer();
        oamInstance.notifyEchoReply();
        oamInstance.destroy();

        oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance == null);
        Assert.assertTrue(OamImpl.getInstance().getBierTeAddressMap().isEmpty());
        for (TargetNodeIds targetNode :targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1),"node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c6"),BierBasicMockUtils.getBfers("c6"),
                    targetNode.getTargetNodeId(), NetworkType.BierTe,CheckType.OnDemand,ModeType.Trace,
                    ReplyMode.DoNotReply);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Assert.assertTrue(oamRequest == null);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances == null);
        }
    }


    @Test
    public void receiveTeReplyTracePartialSuccessTest() throws ExecutionException, InterruptedException {

        List<TargetNodeIds> targetNodes = new ArrayList<>();
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node2").build());
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node3").build());
        targetNodes.add(new TargetNodeIdsBuilder().setTargetNodeId("node5").build());

        when(serviceApi.getTargetBitstring(Utils.buildGetTargetBitstringInput("c6",targetNodes)))
                .thenReturn(Utils.buildGetTargetBitstringOutput("c6",targetNodes));

        RpcResult<StartEchoRequestOutput> result = startOamRequest("c6", CheckType.OnDemand, ModeType.Trace,
                ReplyMode.DoNotReply, targetNodes, true);
        Assert.assertTrue(result.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        verify(bierOamConfig,times(3)).startBierPathDiscovery(eq("node1"),any(PathDiscoveryInput.class));


        OamInstanceKey instanceKey = new OamInstanceKey(new SubDomainId(1),"node1",new BfrId(11),
                BierBasicMockUtils.getEgressNodes("c6"),BierBasicMockUtils.getBfers("c6"),targetNodes,
                NetworkType.BierTe,CheckType.OnDemand,ModeType.Trace, ReplyMode.DoNotReply,255);
        OamInstance oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance != null);
        Assert.assertTrue(oamInstance.isTimerActive());
        Assert.assertTrue(oamInstance.getOamRequestSet().size() == 3);
        Assert.assertEquals(3,OamImpl.getInstance().getBierTeAddressMap().size());

        receiveTeReply("c6",new SubDomainId(1),"node2",ModeType.Trace,true,false,
                new TargetBfersBuilder().setBierBfrid(new BfrId(2)).build(),true);

        receiveTeReply("c6",new SubDomainId(1),"node3",ModeType.Trace,false,false,
                new TargetBfersBuilder().setBierBfrid(new BfrId(33)).build(),true);
        receiveTeReply("c6",new SubDomainId(1),"node5",ModeType.Trace,false,true,
                new TargetBfersBuilder().setBierBfrid(new BfrId(33)).build(),true);

        SingleOamRequestKey oamRequestKey1 = new SingleOamRequestKey(new SubDomainId(1),"node1",new BfrId(11),
                BierBasicMockUtils.getEgressNodes("c6"),BierBasicMockUtils.getBfers("c6"), "node5", NetworkType.BierTe,
                CheckType.OnDemand,ModeType.Trace, ReplyMode.DoNotReply);
        SingleOamRequest oamRequest1 = oamImpl.getSingleRequest(oamRequestKey1);
        Set<OamInstance> oamInstances1 = oamImpl.getRequestInstanceSet(oamRequestKey1);
        Assert.assertTrue(oamInstances1 != null);
        Assert.assertTrue(oamInstances1.size() == 1);
        Assert.assertTrue(oamInstances1.contains(oamInstance));
        Assert.assertTrue(oamInstance.getOamRequestSet().contains(oamRequest1));
        Assert.assertTrue(oamRequest1.isTimerActive());
        oamRequest1.stopTimer();
        oamRequest1.destroy();

        oamInstance.stopTimer();
        oamInstance.notifyEchoReply();
        oamInstance.destroy();

        oamInstance = oamImpl.getOamInstance(instanceKey);
        Assert.assertTrue(oamInstance == null);
        Assert.assertTrue(OamImpl.getInstance().getBierTeAddressMap().isEmpty());
        for (TargetNodeIds targetNode :targetNodes) {
            SingleOamRequestKey oamRequestKey = new SingleOamRequestKey(new SubDomainId(1),"node1",new BfrId(11),
                    BierBasicMockUtils.getEgressNodes("c6"),BierBasicMockUtils.getBfers("c6"),
                    targetNode.getTargetNodeId(), NetworkType.BierTe,CheckType.OnDemand,ModeType.Trace,
                    ReplyMode.DoNotReply);
            SingleOamRequest oamRequest = oamImpl.getSingleRequest(oamRequestKey);
            Assert.assertTrue(oamRequest == null);
            Set<OamInstance> oamInstances = oamImpl.getRequestInstanceSet(oamRequestKey);
            Assert.assertTrue(oamInstances == null);
        }
    }

    private void receiveReply(SubDomainId subDomainId, String channelName, Integer bfirId,
                              List<TargetBfers> targetBferList,ReplyMode replyMode,ModeType modeType,
                              boolean isSuccess) {
        for (TargetBfers targetBfer : targetBferList) {
            DriverNotifyBierEchoReply reply = Utils.buildBierEchoReply(subDomainId, channelName, bfirId, replyMode,
                    modeType, isSuccess, targetBfer);
            oamImpl.onDriverNotifyBierEchoReply(reply);
        }
    }
    private void receiveTeReply(String channelName, SubDomainId subDomainId,String targetNode,ModeType modeType,
                                boolean isSuccess, boolean isTimeOut, TargetBfers targetBfer,boolean isOrdered) {
        List<DriverNotifyBierEchoReply> replies = Utils.buildBierTeEchoReply(channelName,subDomainId,targetNode,
                modeType,isSuccess,isTimeOut,targetBfer,isOrdered);
        for (DriverNotifyBierEchoReply reply : replies) {
            oamImpl.onDriverNotifyBierEchoReply(reply);
        }
    }


    private RpcResult<StartEchoRequestOutput> startOamRequest(String channelName, CheckType checkType,
                                                              ModeType modeType, ReplyMode replyMode,
                                                              List<TargetNodeIds> targetNodes,boolean isSuccess)
            throws ExecutionException, InterruptedException {
        if (isSuccess) {
            if (modeType == ModeType.Ping) {
                when(bierOamConfig.startBierContinuityCheck(anyString(), any(ContinuityCheckInput.class)))
                        .thenReturn(Futures.immediateFuture(RpcResultBuilder.<ContinuityCheckOutput>success().build()));
            } else {
                when(bierOamConfig.startBierPathDiscovery(anyString(), any(PathDiscoveryInput.class)))
                        .thenReturn(Futures.immediateFuture(RpcResultBuilder.<PathDiscoveryOutput>success().build()));
            }
        } else {
            if (modeType == ModeType.Ping) {
                when(bierOamConfig.startBierContinuityCheck(anyString(), any(ContinuityCheckInput.class)))
                        .thenReturn(Futures.immediateFuture(RpcResultBuilder.<ContinuityCheckOutput>failed()
                                .withError(RpcError.ErrorType.APPLICATION,"config failed!").build()));
            } else {
                when(bierOamConfig.startBierPathDiscovery(anyString(), any(PathDiscoveryInput.class)))
                        .thenReturn(Futures.immediateFuture(RpcResultBuilder.<PathDiscoveryOutput>failed()
                                .withError(RpcError.ErrorType.APPLICATION,"config failed!").build()));
            }
        }
        StartEchoRequestInput input = new StartEchoRequestInputBuilder()
                .setChannelName(channelName)
                .setTopologyId(BierBasicMockUtils.DEFAULT_TOPO)
                .setCheckType(checkType)
                .setModeType(modeType)
                .setTargetNodeIds(targetNodes)
                .setReplyMode(replyMode)
                .setMaxTtl(255)
                .build();
        return oamImpl.startEchoRequest(input).get();
    }
}
