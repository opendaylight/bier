/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.pce.impl.tefrr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opendaylight.bier.pce.impl.biertepath.BierPathUnifyKey;
import org.opendaylight.bier.pce.impl.pathcore.MetricStrategy;
import org.opendaylight.bier.pce.impl.pathcore.MetricTransformer;
import org.opendaylight.bier.pce.impl.pathcore.MetricTransformerFactory;
import org.opendaylight.bier.pce.impl.pathcore.PathCompator;
import org.opendaylight.bier.pce.impl.pathcore.PathProvider;
import org.opendaylight.bier.pce.impl.provider.PceResult;
import org.opendaylight.bier.pce.impl.topology.TopologyProvider;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.PathType;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeFrrBackupPath {
    private static final Logger LOG = LoggerFactory.getLogger(TeFrrBackupPath.class);
    private BackupPathKey backupPathKey;
    private LinkedList<BierLink> path = new LinkedList<>();

    private List<BierLink> excludePaths = new ArrayList<>();
    private boolean pathUpdateFlag = false;


    public TeFrrBackupPath(BackupPathKey backupPathKey, List<BierLink> excludeLinks) {
        this.backupPathKey = backupPathKey;
        this.excludePaths = excludeLinks;
    }

    public LinkedList<BierLink> getPath() {
        return path;
    }

    public void calcBackupPath(LinkedList<BierLink> tryToOverlapPath) {
        String bfirNodeId = backupPathKey.getProtectedLink().getLinkSource().getSourceNode();
        String bferNodeId = backupPathKey.getNodeId();
        BierPathUnifyKey bierPathUnifyKey = new BierPathUnifyKey("te-frr",backupPathKey.getSubDomainId(),
                bfirNodeId,bferNodeId);
        PathProvider<MetricTransformer> pathProvider = new PathProvider(bfirNodeId, bierPathUnifyKey, bferNodeId,
                TopologyProvider.DEFAULT_TOPO_ID_STRING, new MetricStrategy<String, BierLink>(),
                new MetricTransformerFactory());

        pathProvider.setOldPath(path);
        pathProvider.setExcludePath(excludePaths);
        if ((tryToOverlapPath != null) && (!tryToOverlapPath.isEmpty())) {
            pathProvider.addTryToOverlapPath(tryToOverlapPath);
        }

        PceResult result = new PceResult();
        pathProvider.calcPath(result);

        path = new LinkedList<>(pathProvider.getPath());
    }

    public PathType getPathType() {
        return backupPathKey.getPathType();
    }

    public String getNodeId() {
        return backupPathKey.getNodeId();
    }

    public void refreshPath(LinkedList<BierLink> tryToOverlapPath) {
        LOG.info("frr path refresh:" + backupPathKey.toString());
        LinkedList<BierLink> oldPath = new LinkedList<>(path);

        calcBackupPath(tryToOverlapPath);

        if (!PathCompator.isPathEqual(oldPath, path)) {
            this.pathUpdateFlag = true;
            LOG.info(backupPathKey.toString() + " FrrPath change: old path--"
                    + oldPath + "; new path--" + path);
        }
    }

    public boolean isPathUpdate() {
        return pathUpdateFlag;
    }
}
