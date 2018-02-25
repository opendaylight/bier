/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.pce.impl.pathcore;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opendaylight.bier.pce.impl.biertepath.BierPathUnifyKey;
import org.opendaylight.bier.pce.impl.biertepath.LspGetPath;
import org.opendaylight.bier.pce.impl.provider.PceResult;
import org.opendaylight.bier.pce.impl.topology.TopologyProvider;
import org.opendaylight.bier.pce.impl.util.ComUtility;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PathProvider<T extends ITransformer<BierLink>> {
    private static final Logger LOG = LoggerFactory.getLogger(PathProvider.class);
    private String bfirNodeId;
    private String bferNodeId;
    private String topoId;
    private List<BierLink> path;
    private List<BierLink> oldPath;
    private List<BierLink> tryToOverlapLinks = Lists.newArrayList();
    private List<BierLink> excludePaths = new ArrayList<>();
    private BierPathUnifyKey bierPathUnifyKey;
    private long pathMetric;
    private ICalcStrategy<String, BierLink> strategy;
    private ITransformerFactory factory;
    private boolean failRollback = false;

    public <F extends ITransformerFactory<T>> PathProvider(String bfirNodeId, BierPathUnifyKey bierPathUnifyKey,
                                                           String bferNodeId, String topoId,
                                                           ICalcStrategy<String, BierLink> strategy, F factory) {
        this.bfirNodeId = bfirNodeId;
        this.bierPathUnifyKey = bierPathUnifyKey;
        this.bferNodeId = bferNodeId;
        this.topoId = topoId;
        this.strategy = strategy;
        this.factory = factory;
    }

    public List<BierLink> getPath() {
        return this.path;
    }

    public long getPathMetric() {
        return this.pathMetric;
    }


    public void clearTryToOverlapLinks() {
        tryToOverlapLinks.clear();
    }


    public void setOldPath(List<BierLink> oldPath) {
        this.oldPath = oldPath;
    }

    public void addTryToOverlapPath(List<BierLink> avoidPath) {
        if (avoidPath != null) {
            tryToOverlapLinks.addAll(avoidPath);
        }
    }


    public void setFailRollback(boolean failRollback) {
        this.failRollback = failRollback;
    }

    public void calcPath(PceResult result) {

        calcPathProcess();

        if (failRollback && ((path == null) || (path.isEmpty()))) {
            result.setCalcFail(true);
            return;
        }
    }

    private void calcPathProcess() {
        if (excludePaths.isEmpty()) {
            calcShortestPath();
            recordPerPort();
        } else {
            calcConstrainedPath();
        }
    }

    private void calcConstrainedPath() {
        List<String> destNodeList = new ArrayList<>();
        destNodeList.add(bferNodeId);
        ContrainedOptimalPath cspf = new ContrainedOptimalPath(bfirNodeId,bferNodeId,
                TopologyProvider.getInstance().getTopoGraph(bierPathUnifyKey.getSubDomainId()), strategy);
        cspf.setExcludePath(excludePaths);
        cspf.setDestNodeList(destNodeList);


        path = cspf.calcCspf(bfirNodeId);
        calcPathMetric();
    }


    private void recordPerPort() {
        if (this.bierPathUnifyKey == null) {
            return;
        }

        BierTesRecordPerPort.getInstance().update(this.bierPathUnifyKey, oldPath, path);

    }

    private void calcShortestPath() {
        Map<String, List<BierLink>> incomingMap = calcIncomingMap();
        if (!incomingMap.containsKey(bferNodeId)) {
            return;
        }

        path = LspGetPath.getPath(incomingMap, bfirNodeId, bferNodeId);
        calcPathMetric();
    }

    private Map<String, List<BierLink>> calcIncomingMap() {
        List<String> destNodeList = new ArrayList<>();
        destNodeList.add(bferNodeId);

        OptimalPath<String, BierLink> sp = new OptimalPath(
                bfirNodeId, TopologyProvider.getInstance().getTopoGraph(bierPathUnifyKey.getSubDomainId()), strategy);
        sp.setDestNodeList(destNodeList);
        sp.setEdgeMeasure(getMetricTransform());

        sp.calcSpt();
        return sp.getIncomingEdgeMap();
    }


    private void calcPathMetric() {
        pathMetric = 0;
        if ((path == null) || (path.isEmpty())) {
            return;
        }
        for (BierLink link : path) {
            pathMetric += ComUtility.getLinkMetric(link);
        }
    }

    private ITransformer<BierLink> getMetricTransform() {
        return (T) factory.create(tryToOverlapLinks);
    }

    public void setExcludePath(List<BierLink> excludeLinks) {
        this.excludePaths = excludeLinks;
    }
}
