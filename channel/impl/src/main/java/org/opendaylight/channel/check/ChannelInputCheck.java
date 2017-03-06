/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.check;

import org.opendaylight.channel.util.ChannelDBUtil;
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
    public static final String EGRESS_IS_NULL = "egress-node is null!";
    public static final String CHANNEL_EXISTS = "The Channel already exists!";
    public static final String CHANNEL_NOT_EXISTS = "The Channel does not exists!";
    public static final String CHANNEL_DEPLOYED = "The Channel has deployed,can not modify";
    public static final String NOT_MULTICAST_IP = " is not multicast ipaddress!";
    public static final String IS_ILLEGAL = " is illegal!";
    public static final String WILDCARD_IS_INVALID = "wildcard is invalid!it must be in the range [1,32].";
    public static final String INGRESS_NOT_IN_SUBDOMIN = "ingress-node is not in this sub-domain!";
    public static final String EGRESS_NOT_IN_SUBDOMIN = "egress-node is not in this sub-domain!";

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
            String[] sections = ipAddress.getIpv4Address().getValue().toString().split("\\.");
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
        return (wildcard != null) && ((wildcard > WILDCARD_MAX) || (wildcard < WILDCARD_MIN));
    }

    public boolean checkChannelExist(String channelName, String topoId) {
        return channelDBUtil.isChannelExists(channelName, topoId);
    }

    public boolean hasChannelDeployed(String name, String topologyId) {
        return channelDBUtil.hasChannelDeplyed(name,topologyId);
    }

    public Channel getChannel(String topologyId, String channelName) {
        return channelDBUtil.readChannel(channelName,topologyId).get();
    }

    public boolean nodeInSubdomain(String topologyId, String node, DomainId domainId, SubDomainId subDomainId) {
        return channelDBUtil.isBierNodeInSubDomain(topologyId,node,domainId,subDomainId);
    }
}
