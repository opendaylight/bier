/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.rpcs.oam;

import com.google.common.base.Optional;
import java.util.concurrent.Future;
import org.opendaylight.bier.adapter.api.BierOamStartEchoRequestCheck;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.bier.driver.common.reporter.DriverNotificationProvider;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.ContinuityCheckInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.ContinuityCheckOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.IetfConnectionlessOamMethodsService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.PathDiscoveryInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.PathDiscoveryOutput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierOamStartEchoRequestCheckImpl implements BierOamStartEchoRequestCheck {

    private static final Logger LOG = LoggerFactory.getLogger(BierOamStartEchoRequestCheckImpl.class);

    private NetconfDataOperator netconfDataOperator ;
    private IetfConnectionlessOamMethodsService oamMethodsService;
    private static final String RPC_SERVICE_FAILED = "Failed to get RpcService. Node id :";
    private static final String DRIVER_FAILED = "Driver failed to get OAM RPC service, "
            + "please check the netconf connetion. Node id :";

    public BierOamStartEchoRequestCheckImpl(NetconfDataOperator netconfDataOperator) {
        this.netconfDataOperator = netconfDataOperator;
    }

    private Future<RpcResult<ContinuityCheckOutput>> pingRpcRequestFailed(String nodeId) {
        return RpcResultBuilder
                .<ContinuityCheckOutput>failed()
                .withError(RpcError.ErrorType.RPC, DRIVER_FAILED + nodeId)
                .buildFuture();
    }

    private Future<RpcResult<PathDiscoveryOutput>> traceRpcRequestFailed(String nodeId) {
        return RpcResultBuilder
                .<PathDiscoveryOutput>failed()
                .withError(RpcError.ErrorType.RPC, DRIVER_FAILED + nodeId)
                .buildFuture();
    }



    public IetfConnectionlessOamMethodsService getCOamMethodsService(String nodeId) {

        MountPoint mountPoint = netconfDataOperator.getMountPoint(nodeId);
        if (mountPoint == null) {
            LOG.warn("Get mountpoint of node : {} failed!!!!", nodeId);
            DriverNotificationProvider.notifyFailure(ConfigurationResult.MOUNT_POINT_FAILUE + nodeId);
            return null;
        }

        final Optional<RpcConsumerRegistry> service = mountPoint.getService(RpcConsumerRegistry.class);
        if (!service.isPresent()) {
            LOG.error("Failed to get RpcService for node {}", nodeId);
            DriverNotificationProvider.notifyFailure(RPC_SERVICE_FAILED + nodeId);
            return null;
        }
        return service.get().getRpcService(IetfConnectionlessOamMethodsService.class);
    }


    @Override
    public Future<RpcResult<ContinuityCheckOutput>> startBierContinuityCheck(
            String nodeId,ContinuityCheckInput continuityCheckInput) {
        oamMethodsService = getCOamMethodsService(nodeId);
        if (oamMethodsService == null) {
            LOG.error("Failed to get OAM method service for continuity check.Node id : {}", nodeId);
            return pingRpcRequestFailed(nodeId);
        }
        return oamMethodsService.continuityCheck(continuityCheckInput);
    }

    @Override
    public Future<RpcResult<PathDiscoveryOutput>> startBierPathDiscovery(
            String nodeId,PathDiscoveryInput pathDiscoveryInput) {
        oamMethodsService = getCOamMethodsService(nodeId);
        if (oamMethodsService == null) {
            LOG.error("Failed to get OAM method service for trace route.Node id : {}", nodeId);
            return traceRpcRequestFailed(nodeId);
        }
        return oamMethodsService.pathDiscovery(pathDiscoveryInput);
    }

}
