/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.pce.impl.biertepath;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;

public class BierPathUnifyKey {
    protected String channelName;
    protected SubDomainId subDomainId;
    protected String bfirNode;
    protected String bferNode;

    public BierPathUnifyKey(String channelName,SubDomainId subDomainId,String bfirNode, String bferNode) {
        this.channelName = channelName;
        this.subDomainId = subDomainId;
        this.bfirNode = bfirNode;
        this.bferNode = bferNode;
    }

    public BierPathUnifyKey(BierPathUnifyKey source) {
        if (null != source.channelName) {
            this.channelName = source.channelName;
        }
        if (null != source.subDomainId) {
            this.subDomainId = source.subDomainId;
        }
        if (null != source.bfirNode) {
            this.bfirNode = source.bfirNode;
        }
        if (null != source.bferNode) {
            this.bferNode = source.bferNode;
        }
    }


    public String getBfirNode() {
        return bfirNode;
    }

    public String getChannelName() {
        return channelName;
    }

    public SubDomainId getSubDomainId() {
        return subDomainId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bfirNode == null) ? 0 : bfirNode.hashCode());
        result = prime * result + ((bferNode == null) ? 0 : bferNode.hashCode());
        result = prime * result + ((channelName == null) ? 0 : channelName.hashCode());
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
        BierPathUnifyKey other = (BierPathUnifyKey) obj;
        if (bfirNode == null) {
            if (other.bfirNode != null) {
                return false;
            }
        } else if (!bfirNode.equals(other.bfirNode)) {
            return false;
        }
        if (bferNode == null) {
            if (other.bferNode != null) {
                return false;
            }
        } else if (!bferNode.equals(other.bferNode)) {
            return false;
        }

        if (channelName == null) {
            if (other.channelName != null) {
                return false;
            }
        } else if (!channelName.equals(other.channelName)) {
            return false;
        }
        if (subDomainId == null) {
            if (other.subDomainId != null) {
                return false;
            }
        } else if (!subDomainId.equals(other.subDomainId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String str;
        str = "channel-name:" + channelName
                + "subDomainId:" + subDomainId
                + " bfirNode:" + bfirNode
                + " bferNode:" + bferNode
                + "\n";

        return str;
    }

}
