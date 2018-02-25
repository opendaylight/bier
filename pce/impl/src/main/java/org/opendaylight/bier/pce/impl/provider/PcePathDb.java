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
import org.opendaylight.bier.pce.impl.tefrr.TeFrrInstance;
import org.opendaylight.bier.pce.impl.topology.TopologyProvider;
import org.opendaylight.bier.pce.impl.util.ComUtility;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferKey;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.te.frr.key.ProtectedLink;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.BierTEData;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.BierTEDataBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.BierTeFrrData;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.bier.te.frr.data.BierTeFrrSubDomain;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.bier.te.frr.data.BierTeFrrSubDomainKey;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.bier.te.frr.data.bier.te.frr.sub.domain.BierTeFrrLink;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.bier.te.frr.data.bier.te.frr.sub.domain.BierTeFrrLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.bier.te.frr.data.bier.te.frr.sub.domain.BierTeFrrLinkKey;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstance;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pcedata.rev170328.biertedata.BierTEInstanceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
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
        dataBroker.mergeData(LogicalDatastoreType.OPERATIONAL, buildBierTeDbRootPath(),
                new BierTEDataBuilder().build());
    }


    public InstanceIdentifier<BierTEData> buildBierTeDbRootPath() {
        return InstanceIdentifier.create(BierTEData.class);
    }

    public void writeBierInstance(BierTeInstance bierTeInstance) {
        dataBroker.mergeData(LogicalDatastoreType.OPERATIONAL,
                buildBierTeInstancePath(bierTeInstance.getChannelName()),bierTeInstanceCreator(bierTeInstance));
    }

    public BierTEInstance readBierInstance(String channelName) {
        return dataBroker.readData(LogicalDatastoreType.OPERATIONAL,buildBierTeInstancePath(channelName));
    }

    private InstanceIdentifier<BierTEInstance> buildBierTeInstancePath(String channelName) {
        return InstanceIdentifier.create(BierTEData.class)
                .child(BierTEInstance.class,new BierTEInstanceKey(channelName));
    }

    private BierTEInstance bierTeInstanceCreator(BierTeInstance bierTeInstance) {
        return new BierTEInstanceBuilder()
                .setChannelName(bierTeInstance.getChannelName())
                .setTopologyId(TopologyProvider.DEFAULT_TOPO_ID_STRING)
                .setSubDomainId(bierTeInstance.getSubDomainId())
                .setBfirNodeId(bierTeInstance.getBfirNodeId())
                .build();
    }

    public static InstanceIdentifier<Bfer> buildBierPathDbPath(String channelName, String bferNode) {
        return InstanceIdentifier.create(BierTEData.class)
                .child(BierTEInstance.class,new BierTEInstanceKey(channelName))
                .child(Bfer.class,new BferKey(bferNode));
    }

    public void writeBierPath(SingleBierPath singleBierPath) {
        dataBroker.mergeData(LogicalDatastoreType.OPERATIONAL,buildBierPathDbPath(singleBierPath.getChannelName(),
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
        dataBroker.deleteData(LogicalDatastoreType.OPERATIONAL,
                buildBierTeInstancePath(bierTeInstance.getChannelName()));
    }

    public void removeBierPath(String channelname, String bferNode) {
        dataBroker.deleteData(LogicalDatastoreType.OPERATIONAL,
                buildBierPathDbPath(channelname,bferNode));
    }

    public void writeTeFrrInstance(TeFrrInstance teFrrInstance) {
        dataBroker.mergeData(LogicalDatastoreType.OPERATIONAL,
                buildTeFrrInstancePath(teFrrInstance.getSubDomainId(),teFrrInstance.getProtectedLink()),
                teFrrInstanceCreator(teFrrInstance));
    }

    private BierTeFrrLink teFrrInstanceCreator(TeFrrInstance teFrrInstance) {
        BierTeFrrLinkBuilder bierTeFrrLinkBuilder = new BierTeFrrLinkBuilder();
        buildTeFrrLinkInfo(bierTeFrrLinkBuilder,teFrrInstance.getProtectedLink());
        bierTeFrrLinkBuilder.setFrrPath(teFrrInstance.buildFrrPath());
        return bierTeFrrLinkBuilder.build();
    }

    private void buildTeFrrLinkInfo(BierTeFrrLinkBuilder bierTeFrrLinkBuilder, ProtectedLink protectedLink) {
        bierTeFrrLinkBuilder.setLinkId(protectedLink.getLinkId());
        bierTeFrrLinkBuilder.setLinkSource(protectedLink.getLinkSource());
        bierTeFrrLinkBuilder.setLinkDest(protectedLink.getLinkDest());
        bierTeFrrLinkBuilder.setDelay(protectedLink.getDelay());
        bierTeFrrLinkBuilder.setLoss(protectedLink.getLoss());
        bierTeFrrLinkBuilder.setMetric(protectedLink.getMetric());
    }

    private InstanceIdentifier<BierTeFrrLink> buildTeFrrInstancePath(SubDomainId subDomainId,
                                                                  ProtectedLink protectedLink) {
        return InstanceIdentifier.create(BierTeFrrData.class)
                .child(BierTeFrrSubDomain.class,new BierTeFrrSubDomainKey(subDomainId))
                .child(BierTeFrrLink.class,new BierTeFrrLinkKey(protectedLink.getLinkId()));
    }

    public void removeTeFrrInstance(TeFrrInstance teFrrInstance) {
        dataBroker.deleteData(LogicalDatastoreType.OPERATIONAL,
                buildTeFrrInstancePath(teFrrInstance.getSubDomainId(),teFrrInstance.getProtectedLink()));
    }

    public BierTeFrrLink readBierTeFrrLink(SubDomainId subDomainId, ProtectedLink protectedLink) {
        return dataBroker.readData(LogicalDatastoreType.OPERATIONAL,buildTeFrrInstancePath(subDomainId,protectedLink));
    }
}
