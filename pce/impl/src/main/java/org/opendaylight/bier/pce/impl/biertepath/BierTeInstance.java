/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.pce.impl.biertepath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opendaylight.bier.pce.impl.provider.NotificationProvider;
import org.opendaylight.bier.pce.impl.provider.PcePathDb;
import org.opendaylight.bier.pce.impl.topology.PathsRecordPerSubDomain;
import org.opendaylight.bier.pce.impl.topology.TopologyProvider;
import org.opendaylight.bier.pce.impl.util.ComUtility;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPathUpdate;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.BierPathUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.CreateBierPathInput;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.BferBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.bfer.BierPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.bier.instance.path.output.Link;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.bier.instance.path.output.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTeInstance {
    private static final Logger LOG = LoggerFactory.getLogger(BierTeInstance.class);
    private String channelName;
    private SubDomainId subDomainId;
    private String bfirNodeId;
    private LinkedHashMap<BierPathUnifyKey, SingleBierPath> bierPaths = new LinkedHashMap<>();
    private LinkedList<BierLink> allPaths = new LinkedList<>();
    private boolean pathUpdateFlag = false;

    public BierTeInstance(CreateBierPathInput input) {
        this.channelName = input.getChannelName();
        this.bfirNodeId = input.getBfirNodeId();
        this.subDomainId = input.getSubDomainId();
    }

    public void calcPath(CreateBierPathInput input, boolean isUpdate) {
        boolean isFailRollback = (input.isSaveCreateFail() != null) && (!input.isSaveCreateFail());
        String bfirNode = input.getBfirNodeId();
        SubDomainId subDomainId = input.getSubDomainId();
        String channelName = input.getChannelName();
        for (Bfer bfer : input.getBfer()) {
            SingleBierPath bierPath = new SingleBierPath(bfirNode,bfer,TopologyProvider.DEFAULT_TOPO_ID_STRING,
                    channelName,subDomainId);
            bierPath.calcPath(isFailRollback,allPaths,TopologyProvider.DEFAULT_TOPO_ID_STRING);
            if (isFailRollback && ((bierPath.getPath() == null) || (bierPath.getPath().isEmpty()))) {
                //do nothing

            } else {
                if (isUpdate) {
                    bierPath.writeDb();
                }
                BierPathUnifyKey pathKey = new BierPathUnifyKey(channelName,subDomainId,bierPath.getBfirNodeId(),
                        bierPath.getBferNodeId());
                bierPaths.put(pathKey,bierPath);
                PathsRecordPerSubDomain.getInstance().add(this.subDomainId, pathKey);
                if (bierPath.getPath() != null && !bierPath.getPath().isEmpty()) {
                    allPaths.addAll(bierPath.getPath());
                }

            }
        }
    }


    public void writeBierTeInstanceToDB() {
        PcePathDb.getInstance().writeBierInstance(this);
        for (SingleBierPath bierPath : bierPaths.values()) {
            bierPath.writeDb();
        }
    }

    public SingleBierPath getBierPath(BierPathUnifyKey key) {
        return this.bierPaths.get(key);
    }

    public boolean isBierPathEmpty() {
        return this.bierPaths.isEmpty();
    }

    public String getChannelName() {
        return this.channelName;
    }

    public String getBfirNodeId() {
        return this.bfirNodeId;
    }

    public SubDomainId getSubDomainId() {
        return this.subDomainId;
    }

    public List<SingleBierPath> getAllBierPath() {
        List<SingleBierPath> singleBierPaths = new ArrayList<>();
        if (!bierPaths.isEmpty()) {
            singleBierPaths.addAll(bierPaths.values());
        }
        return singleBierPaths;
    }

    public void removeBierPath(SingleBierPath path) {
        if (null == path) {
            return;
        }
        BierPathUnifyKey key = new BierPathUnifyKey(path.getChannelName(),path.getSubDomainId(),path.getBfirNodeId(),
                path.getBferNodeId());

        bierPaths.remove(key);
        if (path.getPath() != null) {
            for (BierLink link : path.getPath()) {
                allPaths.removeFirstOccurrence(link);
            }
        }
        path.removeDb();
    }

    public void removeAllBierPath() {
        for (SingleBierPath bierPath : bierPaths.values()) {
            bierPath.destroy();
        }

        bierPaths.clear();
    }

    public void removeBierTeInstanceDB() {
        PcePathDb.getInstance().removeBierTeInstance(this);
    }

    public void refreshPath() {
        LOG.info("BierTeInstance path refresh:" + channelName);
        allPaths.clear();
        for (SingleBierPath bierPath : bierPaths.values()) {
            bierPath.refreshPath(allPaths);
            if (bierPath.getPath() != null && !bierPath.getPath().isEmpty()) {
                allPaths.addAll(bierPath.getPath());
            }
            if (bierPath.isPathUpdate()) {
                pathUpdateFlag = true;
            }
        }
        if (isTeInstancePathUpdate()) {
            notifyPathChange();
        }
    }

    public void notifyPathChange() {
        BierPathUpdate notification = new BierPathUpdateBuilder()
                .setChannelName(channelName)
                .setBfirNodeId(bfirNodeId)
                .setBfer(buildBfers())
                .build();

        LOG.info("notifyPathChange: channelName -" + getChannelName() + " bier-te path change! ");
        NotificationProvider.getInstance().notify(notification);
    }

    public List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer> buildBfers() {
        List<org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer> bferList = new ArrayList<>();
        for (SingleBierPath bierPath : bierPaths.values()) {
            bferList.add(new BferBuilder()
                    .setBferNodeId(bierPath.getBferNodeId())
                    .setBierPath(new BierPathBuilder()
                            .setPathLink(ComUtility.transform2PathLink(bierPath.getPath()))
                            .setPathMetric(bierPath.getPathMetric())
                            .build())
                    .build());
        }
        return bferList;
    }

    public void destroy() {
        bierPaths.clear();
    }

    public boolean isTeInstancePathUpdate() {
        return pathUpdateFlag;
    }

    public List<Link> getAllLinks() {
        Set<Link> allLinkSet = new HashSet<>();
        for (BierLink link : allPaths) {
            allLinkSet.add(new LinkBuilder().setLinkId(link.getLinkId()).build());
        }
        return new ArrayList<>(allLinkSet);
    }
}
