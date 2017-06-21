/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver;

import com.google.common.util.concurrent.CheckedFuture;

import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.driver.common.util.DataGetter;

import org.opendaylight.bier.driver.common.util.DataWriter;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierConfiguration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobal;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.routing.Bier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.ex.rev161020.Routing;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfDataOperator  {
    private static final Logger LOG = LoggerFactory.getLogger(NetconfDataOperator.class);
    private MountPointService mountService = null;


    public static final InstanceIdentifier<Routing> ROUTING_IID = InstanceIdentifier.create(Routing.class);

    public static final InstanceIdentifier<BierGlobal> BIER_GLOBAL_IID =
            ROUTING_IID.augmentation(BierConfiguration.class)
                    .child(Bier.class).child(BierGlobal.class);
    public static final int RETRY_WRITE_MAX = 3;


    public NetconfDataOperator(MountPointService mountService) {
        this.mountService = mountService;
    }





    public MountPoint getMountPoint(String nodeID) {
        ConfigurationResult result = new ConfigurationResult(ConfigurationResult.Result.SUCCESSFUL);
        return DataGetter.getMountPoint(nodeID,result,mountService);

    }

    public <T extends DataObject> CheckedFuture<Void, TransactionCommitFailedException> write(
                                                                 DataWriter.OperateType type,
                                                                 String nodeId,
                                                                 InstanceIdentifier<T> path,
                                                                 T data,
                                                                 ConfigurationResult result) {
        final DataBroker nodeBroker = DataGetter.getDataBroker(nodeId, result, mountService);
        if (nodeBroker == null) {
            return null;
        }

        result.setCfgResult(ConfigurationResult.Result.SUCCESSFUL);

        return DataWriter.operate(type, nodeBroker, RETRY_WRITE_MAX, path, data);

    }


    public <T extends DataObject> T readConfigration(DataBroker dataBroker,
                                         InstanceIdentifier<T> path) {

        return DataGetter.readData(dataBroker,path, LogicalDatastoreType.CONFIGURATION);

    }

    public <T extends DataObject> T readConfigration(String nodeId,
                                                          InstanceIdentifier<T> path) {

        return DataGetter.readData(nodeId,path,mountService,LogicalDatastoreType.CONFIGURATION);

    }

    public <T extends DataObject> T read(String nodeId,
                                         InstanceIdentifier<T> path) {

        return readConfigration(nodeId,path);

    }

    public <T extends DataObject> T read(DataBroker dataBroker,
                                                     InstanceIdentifier<T> path) {

        return readConfigration(dataBroker,path);

    }


}
