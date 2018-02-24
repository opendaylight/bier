/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration.te.bift;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.bier.adapter.api.BierTeBiftWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.bier.driver.common.util.DataWriter;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BierTeConfiguration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.TeInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.routing.BierTeConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.routing.BierTeConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.TeSi;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.TeSiKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.te.si.TeFIndex;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.fwd.item.te.si.TeFIndexKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.TeSubdomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.TeSubdomainKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.te.subdomain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.te.subdomain.TeBslKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTeBiftWriterImpl implements BierTeBiftWriter {
    private static final Logger LOG = LoggerFactory.getLogger(BierTeBiftWriterImpl.class);
    private NetconfDataOperator netconfDataOperator ;

    public BierTeBiftWriterImpl(NetconfDataOperator netconfDataOperator) {
        this.netconfDataOperator = netconfDataOperator;
    }

    public static final InstanceIdentifier<BierTeConfig> BIER_TE_CFG_IID =
            NetconfDataOperator.ROUTING_IID.augmentation(BierTeConfiguration.class)
                    .child(BierTeConfig.class);


    public InstanceIdentifier<TeFIndex> getTeFIndexIId(TeInfo teInfo) {
        TeSubdomainKey teSubdomainKey = teInfo.getTeSubdomain().get(0).getKey();
        TeBslKey teBslKey = teInfo.getTeSubdomain().get(0).getTeBsl().get(0).getKey();
        TeSiKey teSiKey = teInfo.getTeSubdomain().get(0).getTeBsl().get(0).getTeSi().get(0).getKey();
        TeFIndexKey teFIndexKey = teInfo.getTeSubdomain().get(0).getTeBsl().get(0)
                .getTeSi().get(0).getTeFIndex().get(0).getKey();
        return BIER_TE_CFG_IID
                .child(TeSubdomain.class, teSubdomainKey)
                .child(TeBsl.class, teBslKey)
                .child(TeSi.class, teSiKey)
                .child(TeFIndex.class, teFIndexKey);

    }

    public TeFIndex build(TeInfo teInfo) {
        TeSubdomain teSubdomain = teInfo.getTeSubdomain().get(0);
        TeBsl teBsl = teSubdomain.getTeBsl().get(0);
        TeSi teSi = teBsl.getTeSi().get(0);
        return teSi.getTeFIndex().get(0);
    }


    public CheckedFuture<Void, TransactionCommitFailedException> writeTeBift(ConfigurationType type,
                                                                             String nodeId,
                                                                             TeInfo teInfo,
                                                                             ConfigurationResult result) {
        LOG.info("Config Te BIFT to node: {}  -- mod type {} , {}!", nodeId, type,teInfo);

        BierTeConfig bierTeConfig = new BierTeConfigBuilder().setTeSubdomain(teInfo.getTeSubdomain()).build();



        switch (type) {
            case DELETE:
                return netconfDataOperator.write(DataWriter.OperateType.DELETE,
                        nodeId,
                        getTeFIndexIId(teInfo),
                        null,
                        result
                );

            case MODIFY:
                return netconfDataOperator.write(DataWriter.OperateType.MERGE,
                        nodeId,
                        getTeFIndexIId(teInfo),
                        build(teInfo),
                        result
                );


            case ADD:
                return netconfDataOperator.write(DataWriter.OperateType.MERGE,
                        nodeId,
                        BIER_TE_CFG_IID,
                        bierTeConfig,
                        result
                );

            default: {
                LOG.info("Invalid config type : {}", type);
                return null;
            }
        }



    }

    public ConfigurationResult writeTeBift(ConfigurationType type,
                                                     String nodeId,
                                                     TeInfo teInfo) {

        LOG.debug("Modify Te BIFT to node: {}  -- mod type {} , {}!", nodeId, type,teInfo);
        ConfigurationResult result = new ConfigurationResult(ConfigurationResult.Result.FAILED);

        writeTeBift(type,nodeId,teInfo,result);
        return result;



    }




}
