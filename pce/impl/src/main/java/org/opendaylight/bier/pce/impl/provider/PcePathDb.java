/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.provider;

import org.opendaylight.bier.pce.impl.biertepath.BierTeInstance;
import org.opendaylight.bier.pce.impl.biertepath.SingleBierPath;
import org.opendaylight.bier.pce.impl.util.ComUtility;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.BierTEData;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.BierTEDataBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferKey;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstance;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcePathDb {
    private static final Logger LOG = LoggerFactory.getLogger(PcePathDb.class);
    public final DbProvider dataBroker;
    private static PcePathDb instance = new PcePathDb();

    public PcePathDb() {
        dataBroker = DbProvider.getInstance();
    }

    public static PcePathDb getInstance() {
        return instance;
    }

    public void bierTeWriteDbRoot() {
        dataBroker.mergeData(LogicalDatastoreType.CONFIGURATION, buildBierTeDbRootPath(),
                new BierTEDataBuilder().build());
    }


    public InstanceIdentifier<BierTEData> buildBierTeDbRootPath() {
        return InstanceIdentifier.create(BierTEData.class);
    }


    public BierTeInstance BierTeInstanceConvert(BierTEInstance dbData) {
        if (null == dbData) {
            return null;
        }
        return new BierTeInstance(dbData);
    }



    public void writeBierInstance(BierTeInstance bierTeInstance) {
        dataBroker.mergeData(LogicalDatastoreType.CONFIGURATION,
                buildBierTeInstancePath(bierTeInstance.getChannelName()),bierTeInstanceCreator(bierTeInstance));
    }

    public BierTEInstance readBierInstance(String channelName) {
        return dataBroker.readData(LogicalDatastoreType.CONFIGURATION,buildBierTeInstancePath(channelName));
    }

    private InstanceIdentifier<BierTEInstance> buildBierTeInstancePath(String channelName) {
        return InstanceIdentifier.create(BierTEData.class)
                .child(BierTEInstance.class,new BierTEInstanceKey(channelName));
    }

    private BierTEInstance bierTeInstanceCreator(BierTeInstance bierTeInstance) {
        return new BierTEInstanceBuilder()
                .setChannelName(bierTeInstance.getChannelName())
                .setTopologyId(bierTeInstance.getTopoId())
                .setBfirNodeId(bierTeInstance.getBfirNodeId())
                .build();
    }

    public static InstanceIdentifier<Bfer> buildBierPathDbPath(String channelName, String bferNode) {
        return InstanceIdentifier.create(BierTEData.class)
                .child(BierTEInstance.class,new BierTEInstanceKey(channelName))
                .child(Bfer.class,new BferKey(bferNode));
    }

    public void writeBierPath(SingleBierPath singleBierPath) {
        dataBroker.mergeData(LogicalDatastoreType.CONFIGURATION,buildBierPathDbPath(singleBierPath.getChannelName(),
                singleBierPath.getBferNodeId()),bierPathsCreator(singleBierPath));
    }


    private Bfer bierPathsCreator(SingleBierPath singleBierPath) {
        return new BferBuilder()
                .setBferNodeId(singleBierPath.getBferNodeId())
                .setBferNodeId(singleBierPath.getBferNodeId())
                .setBierPath(new BierPathBuilder()
                        .setPathLink(ComUtility.transform2PathLink(singleBierPath.getPath()))
                        .setPathMetric(singleBierPath.getPathMetric())
                        .build())
                .build();
    }

    public void removeBierTeInstance(BierTeInstance bierTeInstance) {
        dataBroker.deleteData(LogicalDatastoreType.CONFIGURATION,
                buildBierTeInstancePath(bierTeInstance.getChannelName()));
    }

    public void removeBierPath(String channelname, String bferNode) {
        dataBroker.deleteData(LogicalDatastoreType.CONFIGURATION,
                buildBierPathDbPath(channelname,bferNode));
    }
}
