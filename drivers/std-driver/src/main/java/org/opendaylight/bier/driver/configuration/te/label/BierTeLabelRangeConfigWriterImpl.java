/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration.te.label;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.bier.adapter.api.BierTeLabelRangeConfigWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.bier.driver.common.util.DataWriter;
import org.opendaylight.bier.driver.common.util.LabelUtil;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.BierTeLableRange;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev170311.MplsConfiguration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev170311.label.blocks.LabelBlocks;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev170311.label.blocks.label.blocks.LabelBlock;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev170311.label.blocks.label.blocks.LabelBlockKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev170311.label.blocks.label.blocks.label.block.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev170311.label.blocks.label.blocks.label.block.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev170311.routing.Mpls;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.types.rev170227.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.types.rev170227.MplsLabelGeneralUse;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BierTeLabelRangeConfigWriterImpl implements BierTeLabelRangeConfigWriter {
    private static final Logger LOG = LoggerFactory.getLogger(BierTeLabelRangeConfigWriterImpl.class);
    private static final ConfigurationResult RESULT_SUCCESS =
            new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);

    private NetconfDataOperator netconfDataOperator ;

    public static final InstanceIdentifier<LabelBlocks> MPLS_LABEL_BLOCKS_IID =
            NetconfDataOperator.ROUTING_IID.augmentation(MplsConfiguration.class).child(Mpls.class)
                    .child(LabelBlocks.class);


    public BierTeLabelRangeConfigWriterImpl(NetconfDataOperator netconfDataOperator) {
        this.netconfDataOperator = netconfDataOperator;
    }

    public InstanceIdentifier<Config> getLableBlockConfigIid(String index) {
        return MPLS_LABEL_BLOCKS_IID.child(LabelBlock.class,
                new LabelBlockKey(index)).child(Config.class);

    }



    public CheckedFuture<Void, TransactionCommitFailedException> writeBierTeLabelRange(ConfigurationType type,
                                                                                       Long min,Long max,
                                                                                       String nodeId,
                                                                                       ConfigurationResult result) {




        String index = min.toString();


        Config config = new ConfigBuilder()
                .setIndex(index)
                .setStartLabel(new MplsLabel(new MplsLabelGeneralUse(min)))
                .setEndLabel(new MplsLabel(new MplsLabelGeneralUse(max)))
                .build();

        if (type == ConfigurationType.DELETE) {

            LOG.info("delete te label range : min : {} , max : {} ,node : {} !",min,max,nodeId);

            return netconfDataOperator.write(DataWriter.OperateType.DELETE,
                    nodeId,
                    getLableBlockConfigIid(index),
                    config,
                    result
            );
        }

        LOG.info("merge te label range  : min : {} , max : {} ,node : {} !",min,max,nodeId);
        return netconfDataOperator.write(DataWriter.OperateType.MERGE,
                nodeId,
                getLableBlockConfigIid(index),
                config,
                result
        );


    }

    public ConfigurationResult writeBierTeLabelRange(ConfigurationType type,
                                                     String nodeId,
                                                     BierTeLableRange bierTeLableRange) {

        LOG.info("TE label range config in node : {}  -- {}!" ,nodeId,bierTeLableRange);
        ConfigurationResult result = new ConfigurationResult(ConfigurationResult.Result.FAILED);


        Long min = bierTeLableRange.getLabelBase().getValue();
        if (!LabelUtil.checkLabelRangeValid(min)) {
            result.setFailureReason(ConfigurationResult.LABEL_MIN + ConfigurationResult.LABEL_INVALID + nodeId);
            LOG.info(ConfigurationResult.LABEL_MIN + ConfigurationResult.LABEL_INVALID + nodeId);
            return result;

        }
        Long max = bierTeLableRange.getLabelRangeSize().getValue() + bierTeLableRange.getLabelBase().getValue() - 1;
        if (!LabelUtil.checkLabelRangeValid(max)) {
            result.setFailureReason(ConfigurationResult.LABEL_MAX + ConfigurationResult.LABEL_INVALID + nodeId);
            LOG.info(ConfigurationResult.LABEL_MAX + ConfigurationResult.LABEL_INVALID + nodeId);
            return result;

        }
        writeBierTeLabelRange(type,min,max,nodeId,result);
        return result;



    }



}
