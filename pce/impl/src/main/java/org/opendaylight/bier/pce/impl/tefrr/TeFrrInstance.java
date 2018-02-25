/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.pce.impl.tefrr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opendaylight.bier.pce.impl.provider.NotificationProvider;
import org.opendaylight.bier.pce.impl.provider.PcePathDb;
import org.opendaylight.bier.pce.impl.topology.TopologyProvider;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.PathType;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.TeFrrPathUpdate;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.TeFrrPathUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.backup.path.Path;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.backup.path.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.TeFrrKey;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.TeFrrKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.te.frr.key.ProtectedLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.te.frr.path.output.Link;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.query.te.frr.path.output.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.FrrPath;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.FrrPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.ExcludingLink;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.ExcludingLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.NextHopPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.NextNextHopPath;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.te.frr.path.frr.path.NextNextHopPathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeFrrInstance {
    private static final Logger LOG = LoggerFactory.getLogger(TeFrrInstance.class);
    private SubDomainId subDomainId;
    private ProtectedLink protectedLink;
    private LinkedHashMap<BackupPathKey, TeFrrBackupPath> frrPaths = new LinkedHashMap<>();
    private LinkedList<BierLink> allPaths = new LinkedList<>();
    private Set<BierLink> excludingLinks = new HashSet<>();
    private boolean pathUpdateFlag = false;

    public TeFrrInstance(TeFrrKey teFrrKey) {
        this.subDomainId = teFrrKey.getSubDomainId();
        this.protectedLink = teFrrKey.getProtectedLink();
    }

    public void calcBackupPath() {
        BierLink nextHopLink = buildNextHopLink();
        List<BierLink> nextNextHopLinks = getNNHLinksFromTopo(subDomainId, nextHopLink);
        excludingLinks.add(nextHopLink);
        excludingLinks.addAll(nextNextHopLinks);
        List<BierLink> excludeLinks = new ArrayList<>(excludingLinks);
        //excludeLinks.add(nextHopLink);


/*        BackupPathKey backupPathKey_NH = new BackupPathKey(subDomainId,protectedLink,
                nextHopLink.getLinkDest().getDestNode(),PathType.NextHop);
        TeFrrBackupPath backupPath_NH = new TeFrrBackupPath(backupPathKey_NH,excludeLinks);
        backupPath_NH.calcBackupPath(allPaths);
        if (backupPath_NH.getPath() != null && !backupPath_NH.getPath().isEmpty()) {
            allPaths.addAll(backupPath_NH.getPath());
        }
        frrPaths.put(backupPathKey_NH,backupPath_NH);*/

        createAndCalcBackupPath(nextHopLink,excludeLinks,PathType.NextHop);
        for (BierLink link : nextNextHopLinks) {
            /*List<BierLink> bypassLinks = new ArrayList<>(excludeLinks);
            bypassLinks.add(link);*/
            createAndCalcBackupPath(link, excludeLinks,PathType.NextNextHop);
        }
    }

    private BierLink buildNextHopLink() {
        return new BierLinkBuilder()
                .setLinkId(protectedLink.getLinkId())
                .setLinkSource(protectedLink.getLinkSource())
                .setLinkDest(protectedLink.getLinkDest())
                .setDelay(protectedLink.getDelay())
                .setLoss(protectedLink.getLoss())
                .setMetric(protectedLink.getMetric())
                .build();
    }

    private void createAndCalcBackupPath(BierLink link, List<BierLink> excludeLinks, PathType pathType) {
        BackupPathKey backupPathKey = new BackupPathKey(subDomainId,protectedLink,
                link.getLinkDest().getDestNode(), pathType);
        TeFrrBackupPath backupPath = new TeFrrBackupPath(backupPathKey,excludeLinks);
        backupPath.calcBackupPath(allPaths);
        frrPaths.put(backupPathKey,backupPath);
        if (backupPath.getPath() != null && !backupPath.getPath().isEmpty()) {
            allPaths.addAll(backupPath.getPath());
        }
    }

    private List<BierLink> getNNHLinksFromTopo(SubDomainId subDomainId, BierLink nextHopLink) {
        return TopologyProvider.getInstance().getNNHLinks(subDomainId,nextHopLink);
    }

    public List<BierLink> getExcludingLinks() {
        return new ArrayList<>(excludingLinks);
    }

    public List<TeFrrBackupPath> getAllBackupPath() {
        List<TeFrrBackupPath> frrBackupPaths = new ArrayList<>();
        if (!frrPaths.isEmpty()) {
            frrBackupPaths.addAll(frrPaths.values());
        }
        return frrBackupPaths;
    }

    public void removeAllBackupPath() {
        frrPaths.clear();
    }

    public List<Link> getAllPathLinks() {
        Set<Link> allLinkSet = new HashSet<>();
        for (BierLink link : allPaths) {
            allLinkSet.add(new LinkBuilder().setLinkId(link.getLinkId()).build());
        }
        return new ArrayList<>(allLinkSet);
    }


    public FrrPath buildFrrPath() {
        FrrPathBuilder frrPathBuilder = new FrrPathBuilder();
        List<NextNextHopPath> nextNextHopPaths = new ArrayList<>();
        List<TeFrrBackupPath> frrBackupPaths = getAllBackupPath();
        frrPathBuilder.setExcludingLink(buildExcludingLink(getExcludingLinks()));
        for (TeFrrBackupPath frrPath : frrBackupPaths) {
            if (frrPath.getPathType().equals(PathType.NextHop)) {
                frrPathBuilder.setNextHopPath(new NextHopPathBuilder()
                        .setDestinationNode(frrPath.getNodeId())
                        .setPath(transPath(frrPath.getPath()))
                        .build());
            }
            if (frrPath.getPathType().equals(PathType.NextNextHop)) {
                nextNextHopPaths.add(new NextNextHopPathBuilder()
                        .setDestinationNode(frrPath.getNodeId())
                        .setPath(transPath(frrPath.getPath()))
                        .build());
            }
        }
        frrPathBuilder.setNextNextHopPath(nextNextHopPaths);
        return frrPathBuilder.build();
    }



    public SubDomainId getSubDomainId() {
        return this.subDomainId;
    }

    public ProtectedLink getProtectedLink() {
        return this.protectedLink;
    }

    public void writeTeFrrInstanceToDB() {
        PcePathDb.getInstance().writeTeFrrInstance(this);
    }


    private List<Path> transPath(LinkedList<BierLink> links) {
        List<Path> pathList = new ArrayList<>();
        if (links == null) {
            return pathList;
        }
        for (BierLink link : links) {
            pathList.add(new PathBuilder(link).build());
        }
        return pathList;
    }

    private List<ExcludingLink> buildExcludingLink(List<BierLink> excludingLinks) {
        List<ExcludingLink> excludingLinkList = new ArrayList<>();
        for (BierLink link : excludingLinks) {
            excludingLinkList.add(new ExcludingLinkBuilder(link).build());
        }
        return excludingLinkList;
    }

    public void removeTeFrrInstanceDB() {
        PcePathDb.getInstance().removeTeFrrInstance(this);
    }

    public void refresh() {
        //LinkedHashMap<BackupPathKey, TeFrrBackupPath> oldFrrPaths = new LinkedHashMap<>(frrPaths);
        /*frrPaths.clear();
        allPaths.clear();*/
        BierLink nextHopLink = buildNextHopLink();
        List<BierLink> nextNextHopLinks = getNNHLinksFromTopo(subDomainId, nextHopLink);
        if (excludingLinks.size() != (nextNextHopLinks.size() + 1)
                || !containNextNextHopLinks(excludingLinks,nextNextHopLinks)) {
            pathUpdateFlag = true;
            frrPaths.clear();
            allPaths.clear();
            excludingLinks.clear();
            excludingLinks.add(nextHopLink);
            excludingLinks.addAll(nextNextHopLinks);
            List<BierLink> excludeLinks = new ArrayList<>(excludingLinks);
            createAndCalcBackupPath(nextHopLink,excludeLinks,PathType.NextHop);
            for (BierLink link : nextNextHopLinks) {
                createAndCalcBackupPath(link, excludeLinks,PathType.NextNextHop);
            }
        } else {
            allPaths.clear();
            for (TeFrrBackupPath teFrrBackupPath : frrPaths.values()) {
                teFrrBackupPath.refreshPath(allPaths);
                if (teFrrBackupPath.getPath() != null && !teFrrBackupPath.getPath().isEmpty()) {
                    allPaths.addAll(teFrrBackupPath.getPath());
                }
                if (teFrrBackupPath.isPathUpdate()) {
                    pathUpdateFlag = true;
                }
            }
        }
        if (isFrrInstanceUpdate()) {
            writeTeFrrInstanceToDB();
            notifyPathChange();
        }
    }

    private void notifyPathChange() {
        TeFrrPathUpdate notification = new TeFrrPathUpdateBuilder()
                .setTeFrrKey(buildTeFrrKey())
                .setFrrPath(buildFrrPath())
                .build();

        LOG.info("notifyFrrPathChange: protectedLink -" + getProtectedLink() + " backup path change! ");
        NotificationProvider.getInstance().notify(notification);
    }

    private TeFrrKey buildTeFrrKey() {
        return new TeFrrKeyBuilder().setSubDomainId(subDomainId).setProtectedLink(protectedLink).build();
    }

    private boolean containNextNextHopLinks(Set<BierLink> excludingLinks, List<BierLink> nextNextHopLinks) {
        for (BierLink link : nextNextHopLinks) {
            if (!excludingLinks.contains(link)) {
                return false;
            }
        }
        return true;
    }

    private boolean isFrrInstanceUpdate() {
        return pathUpdateFlag;
    }
}
