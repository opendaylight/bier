/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.provider;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.bier.pce.impl.biertepath.BierTeInstance;
import org.opendaylight.bier.pce.impl.biertepath.SingleBierPath;
import org.opendaylight.bier.pce.impl.topology.TopologyProvider;
import org.opendaylight.bier.pce.impl.biertepath.BierPathUnifyKey;
import org.opendaylight.bier.pce.impl.util.ComUtility;
import org.opendaylight.bier.pce.impl.util.RpcReturnUtils;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPceService;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.bier.path.output.BierPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.BierTEData;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstance;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;


public class PcePathImpl implements BierPceService {
    private static final Logger LOG = LoggerFactory.getLogger(PcePathImpl.class);
    private ConcurrentHashMap<String, BierTeInstance> bierTeInstances = new ConcurrentHashMap<>();
    private PcePathDb pcePathDb = PcePathDb.getInstance();
    private static PcePathImpl instance = new PcePathImpl();

    private PcePathImpl() {
    }

    public void recoveryDb() {
        bierPathDbRecovery();
    }

    public void destroy() {
        for (BierTeInstance bierTeInstance : bierTeInstances.values()) {
            bierTeInstance.destroy();
        }
        bierTeInstances.clear();
    }

    public static PcePathImpl getInstance() {
        return instance;
    }




    public BierTeInstance getBierTeInstance(String channelName) {
        return bierTeInstances.get(channelName);
    }



    @Override
    public Future<RpcResult<CreateBierPathOutput>> createBierPath(
            CreateBierPathInput input) {
        if (input == null || input.getChannelName() == null || input.getBfirNodeId() == null ||
                input.getBfer()== null || input.getBfer().isEmpty()) {
            return RpcReturnUtils.returnErr("Unlegal argument!");
        }

        LOG.debug(input.toString());

        BierTeInstance bierTeInstance = getBierTeInstance(input.getChannelName());

        if (bierTeInstance == null) {
            bierTeInstance = new BierTeInstance(input);

            bierTeInstance.calcPath(input,false);
            if (!bierTeInstance.isBierPathEmpty()) {
                bierTeInstances.put(bierTeInstance.getChannelName(),bierTeInstance);
                bierTeInstance.writeBierTeInstanceToDB();
            }

        } else {
            if (!bierTeInstance.getBfirNodeId().equals(input.getBfirNodeId())) {
                return RpcReturnUtils.returnErr("bfir NodeId is not equals, with the same channel-name!");
            }
            bierTeInstance.calcPath(input,true);
        }

        CreateBierPathOutputBuilder output = new CreateBierPathOutputBuilder();
        output.setChannelName(input.getChannelName());
        output.setBfirNodeId(input.getBfirNodeId());
        output.setBfer(bierTeInstance.buildBfers());

        return Futures.immediateFuture(RpcResultBuilder.success(output).build());
    }


    @Override
    public Future<RpcResult<RemoveBierPathOutput>> removeBierPath(RemoveBierPathInput input) {
        RemoveBierPathOutputBuilder output = new RemoveBierPathOutputBuilder();
        if (input.getChannelName() != null && input.getBfirNodeId() != null) {
            LOG.debug(input.toString());
            BierTeInstance bierTeInstance = getBierTeInstance(input.getChannelName());
            output.setChannelName(input.getChannelName());
            output.setBfirNodeId(input.getBfirNodeId());
            if (bierTeInstance == null) {
                return Futures.immediateFuture(RpcResultBuilder
                        .success(output.build()).build());
            }
            if (!bierTeInstance.getBfirNodeId().equals(input.getBfirNodeId())) {
                return RpcReturnUtils.returnErr("bfir NodeId is not equals, with the same channel-name!");

            }

            if (input.getBfer() == null || input.getBfer().isEmpty()) {
                bierTeInstance.removeAllBierPath();
                bierTeInstance.removeBierTeInstanceDB();
                bierTeInstances.remove(input.getChannelName());
            } else {
                for (org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328
                        .remove.bier.path.input.Bfer bfer : input.getBfer()) {
                    SingleBierPath bierPath = bierTeInstance.getBierPath(new BierPathUnifyKey(input.getChannelName(),
                            input.getBfirNodeId(),bfer.getBferNodeId()));
                    if (bierPath != null) {
                        bierPath.destroy();
                        bierTeInstance.removeBierPath(bierPath);
                    }
                }
                if (bierTeInstance.isBierPathEmpty()) {
                    bierTeInstance.removeBierTeInstanceDB();
                    bierTeInstances.remove(input.getChannelName());
                } else {
                    output.setBfer(bierTeInstance.buildBfers());
                }
            }
        }
        return Futures.immediateFuture(RpcResultBuilder
                .success(output.build()).build());
    }

    public void bierPathDbRecovery() {
        BierTEData bierTeData;
        try {
            bierTeData = pcePathDb.dataBroker.readData(LogicalDatastoreType.CONFIGURATION,
                    pcePathDb.buildBierTeDbRootPath());
            if (bierTeData != null) {
                bierTeRecoveryDataFromDb(bierTeData);
            } else {
                pcePathDb.bierTeWriteDbRoot();
            }
        } catch (ExecutionException e) {
            LOG.debug("bierTeDbRecovery read failed " + e);
        }
    }

    private void bierTeRecoveryDataFromDb(BierTEData bierteData) throws ExecutionException {
        if ((null == bierteData) || (bierteData.getBierTEInstance().isEmpty())) {
            return;
        }

        for (BierTEInstance bierTeInstanceData : bierteData.getBierTEInstance()) {
            BierTeInstance bierTe = getBierTeInstance(bierTeInstanceData.getChannelName());
            if (bierTe == null) {
                TopologyProvider.getInstance().getTopoGraphRecover(bierTeInstanceData.getTopologyId());
                bierTe = pcePathDb.BierTeInstanceConvert(bierTeInstanceData);
                bierTeInstances.put(bierTeInstanceData.getChannelName(),bierTe);
            } else {
                LOG.error("bierTeRecoveryDataFromDb: bierteInstance is not null:{"
                        + bierTe.getChannelName() + bierTe.getBfirNodeId() + "}!");
            }
        }
    }



    @Override
    public Future<RpcResult<QueryBierPathOutput>> queryBierPath(QueryBierPathInput input) {
        if (input.getChannelName() == null || input.getBfirNodeId() == null || input.getBferNodeId() == null) {
            return RpcReturnUtils.returnErr("Unlegal argument!");
        }
        LOG.debug(input.toString());
        BierTeInstance bierTeInstance = getBierTeInstance(input.getChannelName());
        if (bierTeInstance == null) {
            return RpcReturnUtils.returnErr("bier instance does not exists!");

        }
        if (!bierTeInstance.getBfirNodeId().equals(input.getBfirNodeId())) {
            return RpcReturnUtils.returnErr("bfir NodeId is not equals, with the same channel-name!");

        }

        BierPathUnifyKey pathKey = new BierPathUnifyKey(input.getChannelName(),input.getBfirNodeId(), input.getBferNodeId());
        SingleBierPath bierPath = bierTeInstance.getBierPath(pathKey);
        if (bierPath == null) {
            return RpcReturnUtils.returnErr("bier path does not exists!");

        }
        List<BierLink> pathLink = bierPath.getPath();
        QueryBierPathOutputBuilder outputBuilder = new QueryBierPathOutputBuilder();
        outputBuilder.setBierPath(new BierPathBuilder()
                .setPathLink(ComUtility.transform2PathLink(pathLink))
                .setPathMetric(bierPath.getPathMetric())
                .build());
        return Futures.immediateFuture(RpcResultBuilder.success(outputBuilder.build()).build());
    }

    @Override
    public Future<RpcResult<QueryBierInstancePathOutput>> queryBierInstancePath(QueryBierInstancePathInput input) {
        if (input.getChannelName() == null ) {
            return RpcReturnUtils.returnErr("channel-name is null!");
        }
        LOG.debug(input.toString());
        BierTeInstance bierTeInstance = getBierTeInstance(input.getChannelName());
        QueryBierInstancePathOutputBuilder outputBuilder = new QueryBierInstancePathOutputBuilder();
        if (bierTeInstance != null) {
            outputBuilder.setLink(bierTeInstance.getAllLinks());
        }
        return Futures.immediateFuture(RpcResultBuilder.success(outputBuilder.build()).build());
    }


    public void refreshAllBierTePath(String topoId) {
        if (topoId == null) {
            return;
        }
        Collection<BierTeInstance> bierTeInstanceList = bierTeInstances.values();
        for (BierTeInstance bierTeInstance : bierTeInstanceList) {
            if (bierTeInstance.getTopoId().equals(topoId)) {
                bierTeInstance.refreshPath();
            }
        }
    }
}




