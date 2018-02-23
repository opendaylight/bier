/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.oam.impl;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.CheckType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.ModeType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.NetworkType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.start.echo.request.input.TargetNodeIds;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.ReplyMode;

public class OamInstanceKey {
    private SubDomainId subDomainId;
    private String ingressNode;
    private BfrId bfirId;
    private List<BfrId> egressBfrs;
    private List<String> egressNodes;
    private List<String> targetNodes;
    private NetworkType networkType;
    private CheckType checkType;
    private ModeType modeType;
    private ReplyMode replyMode;
    private Integer maxTtl;

    public OamInstanceKey(SubDomainId subDomainId, String ingressNode,BfrId bfirId, List<String> egressNodes,
                          List<BfrId> egressBfrs, List<TargetNodeIds> targetNodes, NetworkType networkType,
                          CheckType checkType, ModeType modeType, ReplyMode replyMode, Integer maxTtl) {
        this.subDomainId = subDomainId;
        this.ingressNode = ingressNode;
        this.bfirId = bfirId;
        this.egressNodes = egressNodes;
        this.egressBfrs = egressBfrs;
        this.targetNodes = trans(targetNodes);
        this.networkType = networkType;
        this.checkType = checkType;
        this.modeType = modeType;
        this.replyMode = replyMode;
        this.maxTtl = (maxTtl == null ? 255 : maxTtl);
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
        result = prime * result + maxTtl.hashCode();
        result = prime * result + modeType.hashCode();
        result = prime * result + networkType.hashCode();
        result = prime * result + replyMode.hashCode();
        result = prime * result + targetNodes.hashCode();
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
        OamInstanceKey other = (OamInstanceKey) obj;
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
        if (!maxTtl.equals(other.getMaxTtl())) {
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
        if (!targetNodes.containsAll(other.getTargetNodes())) {
            return false;
        }
        return true;
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

    public List<String> getTargetNodes() {
        return targetNodes;
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

    public Integer getMaxTtl() {
        return maxTtl;
    }


    private List<String> trans(List<TargetNodeIds> targetNodes) {
        List<String> list = new ArrayList<>();
        for (TargetNodeIds targetNode : targetNodes) {
            list.add(targetNode.getTargetNodeId());
        }
        return list;
    }
}
