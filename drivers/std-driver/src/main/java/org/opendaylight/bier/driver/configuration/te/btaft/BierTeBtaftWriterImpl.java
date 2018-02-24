/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration.te.btaft;

import com.google.common.util.concurrent.CheckedFuture;
import java.util.Collections;
import org.opendaylight.bier.adapter.api.BierTeBtaftWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.bier.driver.common.util.DataWriter;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BierTeConfiguration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.routing.BierTeConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.routing.BierTeConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.frr.Btaft;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.TeSubdomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.TeSubdomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.te.info.TeSubdomainKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTeBtaftWriterImpl implements BierTeBtaftWriter {
    private static final Logger LOG = LoggerFactory.getLogger(BierTeBtaftWriterImpl.class);
    private NetconfDataOperator netconfDataOperator ;

    public BierTeBtaftWriterImpl(NetconfDataOperator netconfDataOperator) {
        this.netconfDataOperator = netconfDataOperator;
    }

    public static final InstanceIdentifier<BierTeConfig> BIER_TE_CFG_IID =
            NetconfDataOperator.ROUTING_IID.augmentation(BierTeConfiguration.class)
                    .child(BierTeConfig.class);


    public InstanceIdentifier<Btaft> getBtaftIid(SubDomainId subDomainId, Btaft btaft) {


        return BIER_TE_CFG_IID
                .child(TeSubdomain.class, new TeSubdomainKey(subDomainId))
                .child(Btaft.class, btaft.getKey());

    }




    public CheckedFuture<Void, TransactionCommitFailedException> writeBierTeBtaft(ConfigurationType type,String nodeId,
                                                                             SubDomainId subDomainId, Btaft btaft,
                                                                             ConfigurationResult result) {

        TeSubdomain teSubdomain = new TeSubdomainBuilder()
                .setSubdomainId(subDomainId)
                .setBtaft(Collections.singletonList(btaft))
                .build();

        BierTeConfig bierTeConfig = new BierTeConfigBuilder()
                .setTeSubdomain(Collections.singletonList(teSubdomain))
                .build();



        switch (type) {
            case DELETE:
                return netconfDataOperator.write(DataWriter.OperateType.DELETE,
                        nodeId,
                        getBtaftIid(subDomainId,btaft),
                        null,
                        result
                );

            case MODIFY:
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

    public ConfigurationResult writeBierTeBtaft(ConfigurationType type, String nodeId,
                                                SubDomainId subDomainId, Btaft btaft) {
        LOG.debug("Modify Te BTAFT to node: {}  -- mod type {} , {}!", nodeId, type,btaft);
        ConfigurationResult result = new ConfigurationResult(ConfigurationResult.Result.FAILED);

        writeBierTeBtaft(type, nodeId, subDomainId,btaft,result);
        return result;
    }




}
