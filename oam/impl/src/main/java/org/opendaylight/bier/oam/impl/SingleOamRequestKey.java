/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.oam.impl;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.CheckType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.ModeType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.NetworkType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.ReplyMode;

public class SingleOamRequestKey {
    private SubDomainId subDomainId;
    private String ingressNode;
    private BfrId bfirId;
    private List<String> egressNodes;
    private List<BfrId> egressBfrs;
    private String targetNode;
    private NetworkType networkType;
    private CheckType checkType;
    private ModeType modeType;
    private ReplyMode replyMode;


    public SingleOamRequestKey(SubDomainId subDomainId, String ingressNode, BfrId bfirId, List<String> egressNodes,
                            List<BfrId> egressBfrs, String targetNode, NetworkType networkType, CheckType checkType,
                            ModeType modeType, ReplyMode replyMode) {
        this.subDomainId = subDomainId;
        this.ingressNode = ingressNode;
        this.bfirId = bfirId;
        this.egressNodes = egressNodes;
        this.egressBfrs = egressBfrs;
        this.targetNode = targetNode;
        this.networkType = networkType;
        this.checkType = checkType;
        this.modeType = modeType;
        this.replyMode = replyMode;
    }


    public SubDomainId getSubDomainId() {
        return subDomainId;
    }

    public String getIngressNode() {
        return ingressNode;
    }

    public BfrId getBfirId() {
        return bfirId;
    }

    public List<String> getEgressNodes() {
        return egressNodes;
    }

    public List<BfrId> getEgressBfrs() {
        return egressBfrs;
    }

    public String getTargetNode() {
        return targetNode;
    }

    public NetworkType getNetworkType() {
        return networkType;
    }

    public CheckType getCheckType() {
        return checkType;
    }

    public ModeType getModeType() {
        return modeType;
    }

    public ReplyMode getReplyMode() {
        return replyMode;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + subDomainId.hashCode();
        result = prime * result + ingressNode.hashCode();
        result = prime * result + bfirId.hashCode();
        result = prime * result + egressNodes.hashCode();
        result = prime * result + egressBfrs.hashCode();
        result = prime * result + checkType.hashCode();
        result = prime * result + modeType.hashCode();
        result = prime * result + networkType.hashCode();
        result = prime * result + replyMode.hashCode();
        result = prime * result + targetNode.hashCode();
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
        SingleOamRequestKey other = (SingleOamRequestKey) obj;
        if (!subDomainId.equals(other.getSubDomainId())) {
            return false;
        }
        if (!ingressNode.equals(other.getIngressNode())) {
            return false;
        }
        if (!bfirId.equals(other.getBfirId())) {
            return false;
        }
        if (!egressNodes.containsAll(other.getEgressNodes())) {
            return false;
        }
        if (!egressBfrs.containsAll(other.getEgressBfrs())) {
            return false;
        }
        if (!checkType.equals(other.getCheckType())) {
            return false;
        }
        if (!modeType.equals(other.getModeType())) {
            return false;
        }
        if (!networkType.equals(other.getNetworkType())) {
            return false;
        }
        if (!replyMode.equals(other.getReplyMode())) {
            return false;
        }
        if (!targetNode.equals(other.getTargetNode())) {
            return false;
        }
        return true;
    }

}
