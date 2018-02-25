/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.topology;

import com.google.common.annotations.VisibleForTesting;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.util.ConcurrentHashSet;
import org.opendaylight.bier.pce.impl.biertepath.BierPathUnifyKey;
import org.opendaylight.bier.pce.impl.provider.PcePathImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;


public class PathsRecordPerSubDomain {
    private static PathsRecordPerSubDomain instance = new PathsRecordPerSubDomain();
    private Map<SubDomainId, SubDomainRecord> subDomainRecords = new ConcurrentHashMap<>();
    private PcePathImpl pcePathService;

    private PathsRecordPerSubDomain() {
    }

    public static PathsRecordPerSubDomain getInstance() {
        return instance;
    }

    public void setPcePathService(PcePathImpl pcePathService) {
        this.pcePathService = pcePathService;
    }

    public void add(SubDomainId subDomainId, BierPathUnifyKey pathUnifyKey) {
        SubDomainRecord subDomainRecord = subDomainRecords.get(subDomainId);
        if (subDomainRecord == null) {
            synchronized (this) {
                subDomainRecord = subDomainRecords.get(subDomainId);
                if (subDomainRecord == null) {
                    subDomainRecord = new SubDomainRecord(subDomainId);
                    subDomainRecords.put(subDomainId, subDomainRecord);
                }
            }
        }

        subDomainRecord.add(pathUnifyKey);
    }

    public void remove(SubDomainId subDomainId, BierPathUnifyKey pathUnifyKey) {
        SubDomainRecord subDomainRecord = subDomainRecords.get(subDomainId);
        if (subDomainRecord != null) {
            subDomainRecord.remove(pathUnifyKey);
        }
    }

    @VisibleForTesting
    public Set<BierPathUnifyKey> getBierPathSetBySubDomainId(SubDomainId subDomainId) {
        SubDomainRecord subDomainRecord = subDomainRecords.get(subDomainId);
        if (subDomainRecord == null) {
            return new HashSet<>();
        }
        return subDomainRecord.getPathSet();
    }

    public void destroy() {
        subDomainRecords.clear();
    }

    private class SubDomainRecord {
        private SubDomainId subDomainId;
        private Set<BierPathUnifyKey> pathSet = new ConcurrentHashSet<>();

        SubDomainRecord(SubDomainId subDomainId) {
            this.subDomainId = subDomainId;
        }

        public void remove(BierPathUnifyKey tunnelUnifyKey) {
            pathSet.remove(tunnelUnifyKey);
        }

        public void add(BierPathUnifyKey tunnelUnifyKey) {
            pathSet.add(tunnelUnifyKey);
        }

        public Set<BierPathUnifyKey> getPathSet() {
            return pathSet;
        }
    }
}
