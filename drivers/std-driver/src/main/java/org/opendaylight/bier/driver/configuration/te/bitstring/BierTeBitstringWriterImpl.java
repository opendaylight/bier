/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration.te.bitstring;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.bier.adapter.api.BierTeBitstringWriter;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.adapter.api.ConfigurationType;
import org.opendaylight.bier.driver.NetconfDataOperator;
import org.opendaylight.bier.driver.common.util.DataWriter;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.BierTePath;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.TePath;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.bier.te.path.Path;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.bier.te.path.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.bier.te.path.PathKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierTeBitstringWriterImpl  implements BierTeBitstringWriter {
    private static final Logger LOG = LoggerFactory.getLogger(BierTeBitstringWriterImpl.class);
    private NetconfDataOperator netconfDataOperator ;

    public BierTeBitstringWriterImpl(NetconfDataOperator netconfDataOperator) {
        this.netconfDataOperator = netconfDataOperator;
    }

    public InstanceIdentifier<Path> getTePathIid(TePath tePath) {
        return InstanceIdentifier.create(BierTePath.class)
                .child(Path.class,new PathKey(tePath.getPathId()));

    }

    public CheckedFuture<Void, TransactionCommitFailedException> writeBierTeBitstring(ConfigurationType type,
                                                                                      String nodeId,
                                                                                      TePath tePath,
                                                                                      ConfigurationResult result) {

        Path path = new PathBuilder(tePath).build();


        if (type == ConfigurationType.DELETE) {
            LOG.info("delete bitstring {} in node {} ", tePath, nodeId);
            return netconfDataOperator.write(
                    DataWriter.OperateType.DELETE,
                    nodeId,
                    getTePathIid(tePath),
                    null,
                    result);
        }
        LOG.info("merge channel bitstring {} in node {} ", tePath, nodeId);
        return netconfDataOperator.write(
                DataWriter.OperateType.MERGE,
                nodeId,
                getTePathIid(tePath),
                path,
                result
        );
    }

    public ConfigurationResult writeBierTeBitstring(ConfigurationType type,String nodeId,TePath tePath) {
        ConfigurationResult result = new ConfigurationResult(ConfigurationResult.Result.FAILED);
        writeBierTeBitstring(type,nodeId,tePath,result);
        return result;

    }

}
