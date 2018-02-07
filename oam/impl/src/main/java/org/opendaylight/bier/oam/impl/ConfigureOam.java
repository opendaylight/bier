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
import java.util.concurrent.ExecutionException;

import org.opendaylight.bier.adapter.api.BierOamStartEchoRequestCheck;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.get.target.bitstring.output.target.node.ids.BitstringInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.address.TargetBfers;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.address.TargetBfersBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.address.bier.address.BfrsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.address.bier.address.bfrs.BierBfers;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.address.bier.address.bfrs.BierBfersBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.path.discovery.input.destination.tp.tp.address.BierAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.path.discovery.input.destination.tp.tp.address.BierTeAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.ContinuityCheckInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.ContinuityCheckInputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.ContinuityCheckOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.PathDiscoveryInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.PathDiscoveryInputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.PathDiscoveryOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.methods.rev170518.path.discovery.input.DestinationTpBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.rev170609.SessionType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless.oam.rev170609.tp.address.TpAddress;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigureOam {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigureOam.class);
    private final BierOamStartEchoRequestCheck bierOamStartEchoRequestCheck;
    private static ConfigureOam instance;

    public ConfigureOam(BierOamStartEchoRequestCheck bierOamStartEchoRequestCheck) {
        this.bierOamStartEchoRequestCheck = bierOamStartEchoRequestCheck;
        instance = this;
    }

    public static ConfigureOam getInstance() {
        return instance;
    }

    public ConfigurationResult startBierPathDiscovery(SingleOamRequestKey key, BfrId targetNode, Integer maxTtl,
                                                      BitstringInfo bitstringInfo) {
        List<BierBfers> bfers = new ArrayList<>();
        for (BfrId bfer : key.getEgressBfrs()) {
            bfers.add(new BierBfersBuilder().setBierBfrid(bfer).build());
        }
        List<TargetBfers> targetBfers = new ArrayList<>();
        targetBfers.add(new TargetBfersBuilder().setBierBfrid(targetNode).build());

        PathDiscoveryInput input = new PathDiscoveryInputBuilder()
                .setMaxTtl(maxTtl.shortValue())
                .setDestinationTp(new DestinationTpBuilder()
                        .setTpAddress(buildTpAddress(key,bfers,targetBfers,bitstringInfo))
                        .build())
                .setSessionTypeEnum(SessionType.SessionTypeEnum.forValue(key.getCheckType().getIntValue()))
                .build();
        try {
            LOG.info("config oam echo request,bier-path-discovery : " + input);
            RpcResult<PathDiscoveryOutput> result = bierOamStartEchoRequestCheck
                    .startBierPathDiscovery(key.getIngressNode(),input)
                    .get();
            if (result.isSuccessful()) {
                return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
            } else {
                return new ConfigurationResult(ConfigurationResult.Result.FAILED,result.getErrors().toString());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Oam configuration exception:" + e);
            return new ConfigurationResult(ConfigurationResult.Result.FAILED,"Oam configuration exception");
        }
    }

    private TpAddress buildTpAddress(SingleOamRequestKey key, List<BierBfers> bfers, List<TargetBfers> targetBfers,
                                     BitstringInfo bitstringInfo) {
        switch (key.getNetworkType()) {
            case Bier:
                return new BierAddressBuilder()
                    .setReplyMode(key.getReplyMode())
                    .setBierAddress(new BfrsBuilder()
                            .setBierSubdomainid(key.getSubDomainId())
                            .setBfir(key.getBfirId())
                            .setBierBfers(bfers)
                            .build())
                    .setTargetBfers(targetBfers)
                    .build();
            case BierTe:
                return new BierTeAddressBuilder()
                        .setReplyModeTe(key.getReplyMode())
                        .setBierTeSubdomainid(key.getSubDomainId())
                        .setBierTeBpInfo(bitstringInfo.getBierTeBpInfo())
                        .build();
            default:
                return null;
        }
    }

    public ConfigurationResult startBierContinuityCheck(SingleOamRequestKey key, BfrId targetNode, Integer maxTtl,
                                                        BitstringInfo bitstringInfo) {
        List<BierBfers> bfers = new ArrayList<>();
        for (BfrId bfer : key.getEgressBfrs()) {
            bfers.add(new BierBfersBuilder().setBierBfrid(bfer).build());
        }
        List<TargetBfers> targetBfers = new ArrayList<>();
        targetBfers.add(new TargetBfersBuilder().setBierBfrid(targetNode).build());

        ContinuityCheckInput input = new ContinuityCheckInputBuilder()
                .setTtl(maxTtl.shortValue())
                .setSessionTypeEnum(SessionType.SessionTypeEnum.forValue(key.getCheckType().getIntValue()))
                .setCount(1L)
                .setDestinationTp(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.connectionless
                        .oam.methods.rev170518.continuity.check.input.DestinationTpBuilder()
                        .setTpAddress(buildTpAddress(key,bfers,targetBfers,bitstringInfo))
                        .build())
                .build();
        try {
            LOG.info("config oam echo request,bier-continuity-check: " + input);
            RpcResult<ContinuityCheckOutput> result = bierOamStartEchoRequestCheck
                    .startBierContinuityCheck(key.getIngressNode(),input).get();
            if (result.isSuccessful()) {
                return new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
            } else {
                return new ConfigurationResult(ConfigurationResult.Result.FAILED,
                        result.getErrors().iterator().next().getMessage());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Oam configuration exception:" + e);
            return new ConfigurationResult(ConfigurationResult.Result.FAILED,"Oam configuration exception");
        }
    }
}
