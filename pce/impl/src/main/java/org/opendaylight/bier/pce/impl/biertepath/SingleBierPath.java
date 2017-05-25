/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.biertepath;

import java.util.LinkedList;
import java.util.List;

import org.opendaylight.bier.pce.impl.pathcore.MetricStrategy;
import org.opendaylight.bier.pce.impl.pathcore.MetricTransformer;
import org.opendaylight.bier.pce.impl.pathcore.MetricTransformerFactory;
import org.opendaylight.bier.pce.impl.pathcore.PathCompator;
import org.opendaylight.bier.pce.impl.pathcore.PathProvider;
import org.opendaylight.bier.pce.impl.pathcore.BierTesRecordPerPort;
import org.opendaylight.bier.pce.impl.provider.PcePathDb;
import org.opendaylight.bier.pce.impl.provider.PceResult;
import org.opendaylight.bier.pce.impl.topology.PathsRecordPerTopology;
import org.opendaylight.bier.pce.impl.topology.TopologyProvider;
import org.opendaylight.bier.pce.impl.util.ComUtility;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class SingleBierPath implements IBierTe {
    private static final Logger LOG = LoggerFactory.getLogger(SingleBierPath.class);
    private String channelName;
    private String topoId;
    private String bfirNodeId;
    private String bferNodeId;
    private LinkedList<BierLink> path;
    private long pathMetric;
    private BierPathUnifyKey bierPathUnifyKey;
    private boolean pathUpdateFlag = false;


    public SingleBierPath(String bfirNode, Bfer bfer, String topoId,String channelName) {
        this.bfirNodeId = bfirNode;
        this.bferNodeId = bfer.getBferNodeId();
        this.topoId = (topoId != null) ? topoId : TopologyProvider.defaultTopoIdString;

        this.channelName = channelName;
        this.bierPathUnifyKey = getBierPathUnifyKey(channelName,bfirNodeId, bferNodeId);
    }

    public SingleBierPath(String bfirNode, org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.bierpath.Bfer bfer, String topoId,String channelName) {
        this.bfirNodeId = bfirNode;
        this.bferNodeId = bfer.getBferNodeId();
        this.topoId = topoId;
        this.channelName = channelName;
        this.path = ComUtility.pathLinks2Links(topoId,bfer.getBierPath().getPathLink());
        this.bierPathUnifyKey = getBierPathUnifyKey(channelName,bfirNodeId, bferNodeId);
        this.pathMetric = bfer.getBierPath().getPathMetric();
        PathsRecordPerTopology.getInstance().add(topoId, bierPathUnifyKey);
    }


    public PceResult calcPath(boolean failRollback, List<BierLink> tryToOverlapPath, String topoId) {
        PceResult result = new PceResult();

        PathProvider<MetricTransformer> pathProvider = new PathProvider(bfirNodeId, bierPathUnifyKey, bferNodeId, topoId,
                new MetricStrategy<String, BierLink>(), new MetricTransformerFactory());

        pathProvider.setOldPath(path);
        pathProvider.setFailRollback(failRollback);
        if ((tryToOverlapPath != null) && (!tryToOverlapPath.isEmpty())) {
            pathProvider.addTryToOverlapPath(tryToOverlapPath);
        }
        pathProvider.calcPath(result);
        if (failRollback && (result.isCalcFail())) {
            return result;
        }
        path = (LinkedList<BierLink>) pathProvider.getPath();
        pathMetric = pathProvider.getPathMetric();
        return result;
    }

    public long getPathMetric() {
        return this.pathMetric;
    }


    @Override
    public void writeDb() {
        PcePathDb.getInstance().writeBierPath(this);
    }


    @Override
    public void removeDb() {

        PcePathDb.getInstance().removeBierPath(getChannelName(),getBferNodeId());

    }

    @Override
    public List<BierLink> getPath() {
        return path;
    }

    @Override
    public String getBfirNodeId() {
        return bfirNodeId;
    }


    @Override
    public void destroy() {
        PathsRecordPerTopology.getInstance().remove(topoId, bierPathUnifyKey);
        BierTesRecordPerPort.getInstance().update(new BierPathUnifyKey(bierPathUnifyKey), path, null);
    }

     private static BierPathUnifyKey getBierPathUnifyKey(String channelName,String bfirNodeId, String bferNodeId) {
        return new BierPathUnifyKey(channelName,bfirNodeId, bferNodeId);
    }

    @Override
    public BierPathUnifyKey getBierPathUnifyKey() {
        return this.bierPathUnifyKey;
    }



    public String getBferNodeId() {
        return bferNodeId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void refreshPath(List<BierLink> tryToOverlapPath) {
        LOG.info("Single bier path refresh:" + bierPathUnifyKey.toString());
        LinkedList<BierLink> oldPath = path;
        long oldMetric = pathMetric;

        calcPath(false,tryToOverlapPath,topoId);

        if (!PathCompator.isPathEqual(oldPath, path) || oldMetric != pathMetric) {
            writeDb();
            if (!PathCompator.isPathEqual(oldPath, path)) {
                this.pathUpdateFlag = true;
                LOG.info(bierPathUnifyKey.toString() + " BierPath change: old path--" + oldPath + "; new path--" + path);
            }
        }
    }

    public boolean isPathUpdate() {
        return pathUpdateFlag;
    }
}
