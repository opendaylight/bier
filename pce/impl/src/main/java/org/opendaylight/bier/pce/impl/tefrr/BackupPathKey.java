/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.pce.impl.tefrr;

import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.PathType;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.frr.key.te.frr.key.ProtectedLink;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;

public class BackupPathKey {
    private SubDomainId subDomainId;
    private ProtectedLink protectedLink;
    private String nodeId;
    private PathType pathType;

    public BackupPathKey(SubDomainId subDomainId, ProtectedLink link, String nodeId, PathType pathType) {
        this.subDomainId = subDomainId;
        this.protectedLink = link;
        this.nodeId = nodeId;
        this.pathType = pathType;
    }

    public SubDomainId getSubDomainId() {
        return subDomainId;
    }

    public ProtectedLink getProtectedLink() {
        return protectedLink;
    }

    public String getNodeId() {
        return nodeId;
    }

    public PathType getPathType() {
        return pathType;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((protectedLink == null) ? 0 : protectedLink.hashCode());
        result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
        result = prime * result + ((pathType == null) ? 0 : pathType.hashCode());
        result = prime * result + ((subDomainId == null) ? 0 : subDomainId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BackupPathKey other = (BackupPathKey) obj;
        if (nodeId == null) {
            if (other.getNodeId() != null) {
                return false;
            }
        } else if (!nodeId.equals(other.getNodeId())) {
            return false;
        }
        if (protectedLink == null) {
            if (other.getProtectedLink() != null) {
                return false;
            }
        } else if (!protectedLink.equals(other.getProtectedLink())) {
            return false;
        }

        if (pathType == null) {
            if (other.getPathType() != null) {
                return false;
            }
        } else if (!pathType.equals(other.getPathType())) {
            return false;
        }
        if (subDomainId == null) {
            if (other.getSubDomainId() != null) {
                return false;
            }
        } else if (!subDomainId.equals(other.getSubDomainId())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String str;
        str = "protectedLink:" + protectedLink
                + "subDomainId:" + subDomainId
                + " nodeId:" + nodeId
                + " pathType:" + pathType
                + "\n";

        return str;
    }
}
