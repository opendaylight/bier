/*
 * Copyright (c) 2016 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.oam.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.CheckType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.ModeType;
import org.opendaylight.yang.gen.v1.urn.bier.oam.api.rev170808.NetworkType;
import org.opendaylight.yang.gen.v1.urn.bier.pce.rev170328.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.BitstringInfo;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.PathInfo;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.BierLink;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.bier.echo.reply.AddressInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.bier.echo.reply.address.info.BierTeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.BierTeBpInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.BierTeBpInfoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SingleOamRequest {
    private static final Logger LOG = LoggerFactory.getLogger(SingleOamRequest.class);
    private SingleOamRequestKey singleRequestKey;
    private Timer timer;
    private Integer maxTtl;
    private static final long DELAY = 15000;
    private BitstringInfo bitstringInfo;
    private PathInfo pathInfo;
    private List<PathLink> waitRespondLinks;

    public SingleOamRequest(SingleOamRequestKey key, Integer maxTtl) {
        this.singleRequestKey = key;
        this.maxTtl = maxTtl;
    }

    public SingleOamRequestKey getSingleRequestKey() {
        return singleRequestKey;
    }

    public boolean isWaitRespondLinksEmpty() {
        return waitRespondLinks.isEmpty();
    }

    public ConfigurationResult sendOamRequest() {
        int index = singleRequestKey.getEgressNodes().indexOf(singleRequestKey.getTargetNode());
        BfrId targetBfr = singleRequestKey.getEgressBfrs().get(index);
        if (singleRequestKey.getCheckType() == CheckType.OnDemand) {
            switch (singleRequestKey.getModeType()) {
                case Ping:
                    return ConfigureOam.getInstance().startBierContinuityCheck(singleRequestKey, targetBfr, maxTtl,
                            bitstringInfo);
                case Trace:
                    return ConfigureOam.getInstance().startBierPathDiscovery(singleRequestKey, targetBfr, maxTtl,
                            bitstringInfo);
                default:
                    return new ConfigurationResult(ConfigurationResult.Result.FAILED, "mode-type error!");
            }
        } else {
            return new ConfigurationResult(ConfigurationResult.Result.FAILED,
                    "Do not Supported check-type of Proactive!");
        }
    }

    public void startTimer(final SingleOamRequest oamRequest) {
        if (singleRequestKey.getCheckType() == CheckType.OnDemand) {
            if (timer == null) {
                timer = new Timer();
            } else {
                timer.cancel();
                timer = new Timer();
            }
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    LOG.info("&&&& single request time out &&&&&&");
                    oamRequest.destroy();
                }
            }, DELAY);
        }
    }

    public void destroy() {
        Set<OamInstance> oamInstanceSet = OamImpl.getInstance().getRequestInstanceSet(this.singleRequestKey);
        if (oamInstanceSet != null) {
            for (OamInstance oamInstance : oamInstanceSet) {
                if (singleRequestKey.getNetworkType().equals(NetworkType.BierTe)
                        && singleRequestKey.getModeType().equals(ModeType.Trace)) {
                    if (!waitRespondLinks.isEmpty()) {
                        oamInstance.buildRespondNodes(this, waitRespondLinks.get(0).getLinkDest().getDestNode());
                        oamInstance.addToTraceTargetNodes(singleRequestKey.getTargetNode(),
                                "failed!time out!(single-request)");
                        oamInstance.removeFromWaitingTargets(singleRequestKey.getTargetNode());
                    }
                }
                oamInstance.removeSingleRequest(this);
            }
        }
        OamImpl.getInstance().removeFromRequestInstanceMap(this.singleRequestKey);
        OamImpl.getInstance().removeFromSingleRequestMap(this.singleRequestKey);
        OamImpl.getInstance().removeFromBierTeAddressMap(this);
    }

    public void putToRequestInstanceMap(OamInstance oamInstance) {
        Set<OamInstance> oamInstanceSet = OamImpl.getInstance().getRequestInstanceSet(singleRequestKey);
        if (oamInstanceSet == null) {
            oamInstanceSet = new HashSet<>();
            oamInstanceSet.add(oamInstance);
            OamImpl.getInstance().putToRequestInstanceMap(singleRequestKey,oamInstanceSet);
        } else {
            oamInstanceSet.add(oamInstance);
        }
    }

    public void putToSingleRequestMap() {
        OamImpl.getInstance().putToSingleRequestMap(this.singleRequestKey,this);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public boolean isTimerActive() {
        if (timer != null) {
            return true;
        }
        return false;
    }

    public void setBitstringInfo(BitstringInfo bitstringInfo) {
        this.bitstringInfo = bitstringInfo;
    }

    public void setPathInfo(PathInfo pathInfo) {
        this.pathInfo = pathInfo;
    }

    public void setWaitRespondLinks(List<PathLink> pathLinks) {
        this.waitRespondLinks = new ArrayList<PathLink>(pathLinks);
    }

    public void putToBierTeAddressMap() {
        if (singleRequestKey.getNetworkType().equals(NetworkType.BierTe)) {
            OamImpl.getInstance().putToBierTeAddressMap(buildBierTeAddress(), this);
        }
    }

    private AddressInfo buildBierTeAddress() {
        return new BierTeBuilder()
                .setBierTeSubdomainid(singleRequestKey.getSubDomainId())
                .setReplyModeTe(singleRequestKey.getReplyMode())
                .setBierTeBpInfo(buildBierTeBpInfo())
                .build();
    }

    private List<BierTeBpInfo> buildBierTeBpInfo() {
        List<BierTeBpInfo> bierTeBpInfos = new ArrayList<>();
        for (BierTeBpInfo bierTeBpInfo : bitstringInfo.getBierTeBpInfo()) {
            bierTeBpInfos.add(new BierTeBpInfoBuilder()
                    .setBierTeBsl(bierTeBpInfo.getBierTeBsl())
                    .setBitstring(bierTeBpInfo.getBitstring())
                    .build());
        }
        return bierTeBpInfos;
    }

    public void removePathLink(String nodeId) {
        for (BierLink link : waitRespondLinks) {
            if (link.getLinkDest().getDestNode().equals(nodeId)) {
                waitRespondLinks.remove(link);
                break;
            }
        }
    }

    public PathInfo getPathInfo() {
        return pathInfo;
    }
}
