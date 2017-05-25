/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.pathcore;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.util.ConcurrentHashSet;
import org.opendaylight.bier.pce.impl.biertepath.BierPathUnifyKey;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.network.topology.bier.topology.BierLink;


public class BierTesRecordPerPort {
    private static BierTesRecordPerPort instance = new BierTesRecordPerPort();
    private Map<PortKey, PortRecord> portRecords = new ConcurrentHashMap<>();

    private BierTesRecordPerPort() {
    }

    public static BierTesRecordPerPort getInstance() {
        return instance;
    }

    public void update(BierPathUnifyKey pathKey, List<BierLink> oldPath, List<BierLink> newPath) {
        if (oldPath == null && newPath == null) {
            return;
        }

        if (oldPath == null) {
            addRecords(newPath, pathKey);
        } else if (newPath == null) {
            delRecords(oldPath, pathKey);
        } else {
            mergeRecords(pathKey, oldPath, newPath);
        }
    }

    public Set<BierPathUnifyKey> getPathsRecord(PortKey portKey) {
        PortRecord portRecord = portRecords.get(portKey);
        return (portRecord == null) ? null : portRecord.getPathsRecord();
    }


    private void addRecords(List<BierLink> newPath, BierPathUnifyKey bierPath) {
        for (BierLink link : newPath) {
            addRecord(link, bierPath);
        }
    }


    private void addRecord(BierLink link, BierPathUnifyKey bierPath) {
        PortKey portKey = new PortKey(link.getLinkSource().getSourceNode(), link.getLinkSource().getSourceTp());
        PortRecord record = portRecords.get(portKey);
        if (record == null) {
            synchronized (this) {
                record = portRecords.get(portKey);
                if (record == null) {
                    record = new PortRecord(link);
                    portRecords.put(portKey, record);
                }
            }
        }

        record.add(bierPath);
    }

    private void delRecords(List<BierLink> oldPath, BierPathUnifyKey bierPath) {
        for (BierLink link : oldPath) {
            delRecord(link, bierPath);
        }
    }

    private void mergeRecords(BierPathUnifyKey bierPath, List<BierLink> oldPath,
                              List<BierLink> newPath) {
        Set<BierLink> newLinkSet = new HashSet<>();
        for (BierLink link : newPath) {
            newLinkSet.add(link);
        }

        Iterator<BierLink> it = oldPath.iterator();
        while (it.hasNext()) {
            BierLink link = it.next();
            if (newLinkSet.contains(link)) {
                newLinkSet.remove(link);
                continue;
            }

            delRecord(link, bierPath);
        }

        for (BierLink link : newLinkSet) {
            addRecord(link, bierPath);
        }
    }

    private void delRecord(BierLink link, BierPathUnifyKey bierPath) {
        PortKey portKey = new PortKey(link.getLinkSource().getSourceNode(), link.getLinkSource().getSourceTp());
        PortRecord record = portRecords.get(portKey);
        if (record != null) {
            record.delete(bierPath);
        }
    }


    private class PortRecord {
        @SuppressWarnings("unused")
        private PortKey portKey;
        private Set<BierPathUnifyKey> bierPathSet = new ConcurrentHashSet<>();

        PortRecord(BierLink link) {
            this.portKey = new PortKey(link.getLinkSource().getSourceNode(),
                    link.getLinkSource().getSourceTp());
        }

        public Set<BierPathUnifyKey> getPathsRecord() {
            return bierPathSet;
        }

        public void delete(BierPathUnifyKey pathKey) {
            bierPathSet.remove(pathKey);
        }

        public void add(BierPathUnifyKey pathKey) {
            bierPathSet.add(pathKey);
        }
    }

    public void destroy() {
        portRecords.clear();
    }
}
