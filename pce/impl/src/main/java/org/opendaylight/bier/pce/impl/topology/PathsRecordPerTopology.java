/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.topology;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.util.ConcurrentHashSet;
import org.opendaylight.bier.pce.impl.biertepath.BierPathUnifyKey;
import org.opendaylight.bier.pce.impl.provider.PcePathImpl;


public class PathsRecordPerTopology {
    private static PathsRecordPerTopology instance = new PathsRecordPerTopology();
    private Map<String, TopologyRecord> topologyRecords = new ConcurrentHashMap<>();
    private PcePathImpl pcePathService;

    private PathsRecordPerTopology() {
    }

    public static PathsRecordPerTopology getInstance() {
        return instance;
    }

    public void setPcePathService(PcePathImpl pcePathService) {
        this.pcePathService = pcePathService;
    }

    public void add(String topoId, BierPathUnifyKey pathUnifyKey) {
        TopologyRecord topoRecord = topologyRecords.get(topoId);
        if (topoRecord == null) {
            synchronized (this) {
                topoRecord = topologyRecords.get(topoId);
                if (topoRecord == null) {
                    topoRecord = new TopologyRecord(topoId);
                    topologyRecords.put(topoId, topoRecord);
                }
            }
        }

        topoRecord.add(pathUnifyKey);
    }

    public void remove(String topoId, BierPathUnifyKey pathUnifyKey) {
        TopologyRecord topoRecord = topologyRecords.get(topoId);
        if (topoRecord != null) {
            topoRecord.remove(pathUnifyKey);
        }
    }


    private class TopologyRecord {
        private String topoId;
        private Set<BierPathUnifyKey> pathSet = new ConcurrentHashSet<>();

        TopologyRecord(String topoId) {
            this.topoId = topoId;
        }

        public void remove(BierPathUnifyKey tunnelUnifyKey) {
            pathSet.remove(tunnelUnifyKey);
        }

        public void add(BierPathUnifyKey tunnelUnifyKey) {
            pathSet.add(tunnelUnifyKey);
        }

    }
}
