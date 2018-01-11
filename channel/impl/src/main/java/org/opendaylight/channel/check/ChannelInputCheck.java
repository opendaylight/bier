/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.check;

import org.opendaylight.channel.util.ChannelDBUtil;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.BierForwardingType;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;


public class ChannelInputCheck implements InputCheck {

    private ChannelDBUtil channelDBUtil = ChannelDBUtil.getInstance();

    public static final String INPUT_IS_NULL = "Input is null!";
    public static final String CHANNEL_NAME_IS_NULL = "channel-name is null!";
    public static final String SRCIP_IS_NULL = "src-ip is null!";
    public static final String DEST_GROUP_IS_NULL = "dest-group is null!";
    public static final String DOMAIN_ID_IS_NULL = "domain-id is null!";
    public static final String SUB_DOMAIN_ID_IS_NULL = "sub-domain-id is null!";
    public static final String SRC_WILDCARD_IS_NULL = "src-wildcard is null!";
    public static final String GROUP_WILDCARD_IS_NULL = "group-wildcard is null!";
    public static final String INGRESS_IS_NULL = "ingress-node is null!";
    public static final String SRC_TP_IS_NULL = "src-tp is null!";
    public static final String RCV_TP_IS_NULL = "rcv-tp is null!";
    public static final String EGRESS_IS_NULL = "egress-node is null!";
    public static final String EGRESS_NODE_ID_IS_NULL = "egress-node id is null!";
    public static final String BIER_FORWARDING_TYPE_IS_NULL = "bier forwarding type is null!";
    public static final String CHANNEL_EXISTS = "The Channel already exists!";
    public static final String CHANNEL_NOT_EXISTS = "The Channel does not exists!";
    public static final String CHANNEL_DEPLOYED = "The Channel has deployed,can not modify";
    public static final String NOT_MULTICAST_IP = " is not multicast ipaddress!";
    public static final String IS_ILLEGAL = " is illegal!";
    public static final String WILDCARD_IS_INVALID = "wildcard is invalid!it must be in the range [1,32].";
    public static final String INGRESS_NOT_IN_SUBDOMIN = "ingress-node is not in this sub-domain!";
    public static final String EGRESS_NOT_IN_SUBDOMIN = "egress-node is not in this sub-domain!";
    public static final String SRCTP_NOT_IN_SUBDOMIN = "src-tp is not in this sub-domain!";
    public static final String RCVTP_NOT_IN_SUBDOMIN = "rcv-tp is not in this sub-domain!";
    public static final String INGRESS_EGRESS_CONFLICT = "ingress-node and egress-nodes conflict!the node must not"
            + " be both ingress and egress.";
    public static final String FORWARDING_TYPE_CONFLICT = "forwarding-type conflict! can not change forwarding-type,"
            + " when update deploy-channel info.";
    public static final String STRATEGY_CONFLICT = "assignment-strategy conflict! can not change strategy, when update "
            + "deploy-channel info.";

    private static final Integer MULTICAST_IPV4_1ST_SEGMENT_MIN = 224;
    private static final Integer MULTICAST_IPV4_1ST_SEGMENT_MAX = 239;
    private static final Integer UNICAST_IPV4_1ST_SEGMENT_MAX = 223;
    private static final Integer UNICAST_IPV4_1ST_SEGMENT_MIN = 1;
    private static final Short WILDCARD_MIN = 1;
    private static final Short WILDCARD_MAX = 32;

    @Override
    public CheckResult check() {
        return new CheckResult(false,"");
    }

    public CheckResult checkIpRange(String key, IpAddress ipAddress, boolean isMulticastIp) {
        if (ipAddress != null) {
            String[] sections = ipAddress.getIpv4Address().getValue().split("\\.");
            Integer value = Integer.parseInt(sections[0]);
            if (isMulticastIp) {
                if (value < MULTICAST_IPV4_1ST_SEGMENT_MIN || value > MULTICAST_IPV4_1ST_SEGMENT_MAX) {
                    return new CheckResult(true, key + NOT_MULTICAST_IP);
                }
            } else {
                if (value > UNICAST_IPV4_1ST_SEGMENT_MAX || value < UNICAST_IPV4_1ST_SEGMENT_MIN) {
                    return new CheckResult(true, key + IS_ILLEGAL);
                }
            }
        }
        return new CheckResult(false,"");
    }

    public CheckResult checkWildCard(Short groupWildcard, Short sourceWildcard) {
        if (isWildCastIllegal(groupWildcard) || isWildCastIllegal(sourceWildcard)) {
            return new CheckResult(true,WILDCARD_IS_INVALID);
        }
        return new CheckResult(false,"");
    }

    private boolean isWildCastIllegal(Short wildcard) {
        if (wildcard != null) {
            return ((wildcard > WILDCARD_MAX) || (wildcard < WILDCARD_MIN));
        } else {
            return true;
        }
    }

    public boolean checkChannelExist(String channelName, String topoId) {
        return channelDBUtil.isChannelExists(channelName, topoId);
    }

    public boolean checkChannelExist(Channel channel) {
        return channel == null ? false : true;
    }

    public boolean hasChannelDeployed(String name, String topologyId) {
        return channelDBUtil.hasChannelDeplyed(name,topologyId);
    }

    public boolean nodeInSubdomain(String topologyId, String node, DomainId domainId, SubDomainId subDomainId,
                                   BierForwardingType type) {
        return channelDBUtil.isBierNodeInSubDomain(topologyId,node,domainId,subDomainId,type);
    }

    public boolean tpInTeSubdomain(String topologyId, String node, DomainId domainId, SubDomainId subDomainId,
                                   String tpId) {
        return channelDBUtil.isTpInTeSubdomain(topologyId,node,domainId,subDomainId,tpId);
    }
}
