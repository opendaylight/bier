/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.provider;

import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import org.opendaylight.bier.pce.impl.biertepath.BierPathUnifyKey;
import org.opendaylight.bier.pce.impl.biertepath.BierTeInstance;
import org.opendaylight.bier.pce.impl.biertepath.SingleBierPath;
import org.opendaylight.bier.pce.impl.pathcore.BierTesRecordPerPort;
import org.opendaylight.bier.pce.impl.pathcore.PortKey;
import org.opendaylight.bier.pce.impl.tefrr.TeFrrInstance;
import org.opendaylight.bier.pce.impl.util.ComUtility;
import org.opendaylight.bier.pce.impl.util.RpcReturnUtils;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPceService;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateTeFrrPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierInstancePathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryBierPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryChannelThroughPortInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryChannelThroughPortOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryChannelThroughPortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryTeFrrPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryTeFrrPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.QueryTeFrrPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathOutput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveBierPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.RemoveTeFrrPathInput;

import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.TeFrrKey;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.bier.path.output.BierPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.channel.through.port.output.RelatedChannel;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.channel.through.port.output.RelatedChannelBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcePathImpl implements BierPceService {
    private static final Logger LOG = LoggerFactory.getLogger(PcePathImpl.class);
    private final ConcurrentHashMap<String, BierTeInstance> bierTeInstances = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<TeFrrKey,TeFrrInstance> teFrrInstances = new ConcurrentHashMap<>();
    private final PcePathDb pcePathDb = PcePathDb.getInstance();
    private static PcePathImpl instance = new PcePathImpl();

    private PcePathImpl() {
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
    public Future<RpcResult<CreateBierPathOutput>> createBierPath(CreateBierPathInput input) {
        if (input == null || input.getChannelName() == null || input.getBfirNodeId() == null
                || input.getBfer() == null || input.getBfer().isEmpty()) {
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
    public Future<RpcResult<CreateTeFrrPathOutput>> createTeFrrPath(CreateTeFrrPathInput input) {
        if (input == null || input.getTeFrrKey() == null || input.getTeFrrKey().getSubDomainId() == null
                || input.getTeFrrKey().getProtectedLink() == null
                || input.getTeFrrKey().getProtectedLink().getLinkDest() == null
                || input.getTeFrrKey().getProtectedLink().getLinkSource() == null) {
            return RpcReturnUtils.returnErr("Unlegal argument!");
        }
        LOG.debug(input.toString());
        TeFrrInstance teFrrInstance = getTeFrrInstance(input.getTeFrrKey());
        if (teFrrInstance == null) {
            teFrrInstance = new TeFrrInstance(input.getTeFrrKey());
            teFrrInstance.calcBackupPath();
            teFrrInstances.put(input.getTeFrrKey(),teFrrInstance);
            teFrrInstance.writeTeFrrInstanceToDB();
        }
        CreateTeFrrPathOutput output = new CreateTeFrrPathOutputBuilder()
                .setFrrPath(teFrrInstance.buildFrrPath())
                .build();
        return Futures.immediateFuture(RpcResultBuilder.success(output).build());
    }

    public TeFrrInstance getTeFrrInstance(TeFrrKey frrKey) {
        return teFrrInstances.get(frrKey);
    }

    @Override
    public Future<RpcResult<QueryChannelThroughPortOutput>> queryChannelThroughPort(QueryChannelThroughPortInput
                                                                                                input) {
        if (input == null || input.getNodeId() == null || input.getTpId() == null) {
            return RpcReturnUtils.returnErr("input is null, or node-id is null, or tp-id is null!");
        }
        Set<BierPathUnifyKey> paths = BierTesRecordPerPort.getInstance()
                .getPathsRecord(new PortKey(input.getNodeId(),input.getTpId()));
        Set<RelatedChannel> channelSet = new HashSet<>();
        if (paths != null) {
            for (BierPathUnifyKey path : paths) {
                channelSet.add(new RelatedChannelBuilder()
                        .setChannelName(path.getChannelName())
                        .setBfir(path.getBfirNode())
                        .build());
            }
        }
        QueryChannelThroughPortOutput output = new QueryChannelThroughPortOutputBuilder()
                .setRelatedChannel(new ArrayList<>(channelSet)).build();

        return Futures.immediateFuture(RpcResultBuilder.success(output).build());
    }

    @Override
    public Future<RpcResult<RemoveBierPathOutput>> removeBierPath(RemoveBierPathInput input) {
        RemoveBierPathOutputBuilder output = new RemoveBierPathOutputBuilder();
        if (input.getChannelName() != null && input.getBfirNodeId() != null && input.getSubDomainId() != null) {
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
                            input.getSubDomainId(), input.getBfirNodeId(),bfer.getBferNodeId()));
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

    @Override
    public Future<RpcResult<Void>> removeTeFrrPath(RemoveTeFrrPathInput input) {
        if (input == null || input.getTeFrrKey() == null || input.getTeFrrKey().getSubDomainId() == null
                || input.getTeFrrKey().getProtectedLink() == null
                || input.getTeFrrKey().getProtectedLink().getLinkDest() == null
                || input.getTeFrrKey().getProtectedLink().getLinkSource() == null) {
            return RpcReturnUtils.returnErr("Unlegal argument!");
        }
        LOG.debug(input.toString());
        TeFrrInstance teFrrInstance = getTeFrrInstance(input.getTeFrrKey());
        if (teFrrInstance != null) {
            teFrrInstance.removeAllBackupPath();
            teFrrInstance.removeTeFrrInstanceDB();
            teFrrInstances.remove(input.getTeFrrKey());
        }
        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }

    @Override
    public Future<RpcResult<QueryBierPathOutput>> queryBierPath(QueryBierPathInput input) {
        if (input.getChannelName() == null || input.getBfirNodeId() == null || input.getBferNodeId() == null
                || input.getSubDomainId() == null) {
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

        BierPathUnifyKey pathKey = new BierPathUnifyKey(input.getChannelName(),input.getSubDomainId(),
                input.getBfirNodeId(), input.getBferNodeId());
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
        if (input.getChannelName() == null) {
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


    @Override
    public Future<RpcResult<QueryTeFrrPathOutput>> queryTeFrrPath(QueryTeFrrPathInput input) {
        if (input == null || input.getTeFrrKey() == null || input.getTeFrrKey().getSubDomainId() == null
                || input.getTeFrrKey().getProtectedLink() == null
                || input.getTeFrrKey().getProtectedLink().getLinkDest() == null
                || input.getTeFrrKey().getProtectedLink().getLinkSource() == null) {
            return RpcReturnUtils.returnErr("Unlegal argument!");
        }
        LOG.debug(input.toString());
        TeFrrInstance teFrrInstance = getTeFrrInstance(input.getTeFrrKey());
        QueryTeFrrPathOutputBuilder outputBuilder = new QueryTeFrrPathOutputBuilder();
        if (teFrrInstance != null) {
            outputBuilder.setLink(teFrrInstance.getAllPathLinks());
        }
        return Futures.immediateFuture(RpcResultBuilder.success(outputBuilder.build()).build());
    }

    public void refreshAllBierTePath(SubDomainId subDomainId) {
        if (subDomainId == null) {
            return;
        }
        Collection<BierTeInstance> bierTeInstanceList = bierTeInstances.values();
        for (BierTeInstance bierTeInstance : bierTeInstanceList) {
            if (bierTeInstance.getSubDomainId().equals(subDomainId)) {
                bierTeInstance.refreshPath();
            }
        }
    }

    public void writeDbRoot() {
        pcePathDb.bierTeWriteDbRoot();
    }

    public void refreshAllTeFrrInstance(SubDomainId subDomainId) {
        if (subDomainId == null) {
            return;
        }
        for (TeFrrInstance teFrrInstance : teFrrInstances.values()) {
            if (teFrrInstance.getSubDomainId().equals(subDomainId)) {
                teFrrInstance.refresh();
            }
        }
    }
}




