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

import org.opendaylight.bier.pce.impl.pathcore.BierTesRecordPerPort;
import org.opendaylight.bier.pce.impl.pathcore.MetricStrategy;
import org.opendaylight.bier.pce.impl.pathcore.MetricTransformer;
import org.opendaylight.bier.pce.impl.pathcore.MetricTransformerFactory;
import org.opendaylight.bier.pce.impl.pathcore.PathCompator;
import org.opendaylight.bier.pce.impl.pathcore.PathProvider;
import org.opendaylight.bier.pce.impl.provider.PcePathDb;
import org.opendaylight.bier.pce.impl.provider.PceResult;
import org.opendaylight.bier.pce.impl.topology.PathsRecordPerSubDomain;
import org.opendaylight.bier.pce.impl.topology.TopologyProvider;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.create.bier.path.input.Bfer;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
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


    public SingleBierPath(String bfirNode, Bfer bfer, String topoId,String channelName,SubDomainId subDomainId) {
        this.bfirNodeId = bfirNode;
        this.bferNodeId = bfer.getBferNodeId();
        this.topoId = (topoId != null) ? topoId : TopologyProvider.DEFAULT_TOPO_ID_STRING;
        this.channelName = channelName;
        this.bierPathUnifyKey = getBierPathUnifyKey(channelName,subDomainId,bfirNodeId, bferNodeId);
    }

    public PceResult calcPath(boolean failRollback, List<BierLink> tryToOverlapPath, String topoId) {
        PathProvider<MetricTransformer> pathProvider = new PathProvider(bfirNodeId, bierPathUnifyKey, bferNodeId,
                topoId,new MetricStrategy<String, BierLink>(), new MetricTransformerFactory());

        pathProvider.setOldPath(path);
        pathProvider.setFailRollback(failRollback);
        if ((tryToOverlapPath != null) && (!tryToOverlapPath.isEmpty())) {
            pathProvider.addTryToOverlapPath(tryToOverlapPath);
        }

        PceResult result = new PceResult();
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

    public SubDomainId getSubDomainId() {
        return this.bierPathUnifyKey.getSubDomainId();
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
        PathsRecordPerSubDomain.getInstance().remove(getSubDomainId(), bierPathUnifyKey);
        BierTesRecordPerPort.getInstance().update(new BierPathUnifyKey(bierPathUnifyKey), path, null);
    }

    private static BierPathUnifyKey getBierPathUnifyKey(String channelName,SubDomainId subDomainId, String bfirNodeId,
                                                        String bferNodeId) {
        return new BierPathUnifyKey(channelName,subDomainId,bfirNodeId, bferNodeId);
    }

    public String getBferNodeId() {
        return bferNodeId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void refreshPath(List<BierLink> tryToOverlapPath) {
        LOG.info("Single bier path refresh:" + bierPathUnifyKey.toString());
        LinkedList<BierLink> oldPath = new LinkedList<>(path);
        long oldMetric = pathMetric;

        calcPath(false,tryToOverlapPath,topoId);

        if (!PathCompator.isPathEqual(oldPath, path) || oldMetric != pathMetric) {
            writeDb();
            if (!PathCompator.isPathEqual(oldPath, path)) {
                this.pathUpdateFlag = true;
                LOG.info(bierPathUnifyKey.toString() + " BierPath change: old path--"
                        + oldPath + "; new path--" + path);
            }
        }
    }

    public boolean isPathUpdate() {
        return pathUpdateFlag;
    }
}
