/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.bier.adapter.api.BierConfigResult;
import org.opendaylight.bier.driver.common.DataGetter;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.OptimisticLockFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;


import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierConfiguration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobal;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.routing.Bier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev161020.Routing;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfDataOperator implements BindingAwareConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(NetconfDataOperator.class);
    private MountPointService mountService = null;


    public static final InstanceIdentifier<Routing> ROUTING_IID = InstanceIdentifier.create(Routing.class);

    public static final InstanceIdentifier<BierGlobal> BIER_GLOBAL_IID =
            ROUTING_IID.augmentation(BierConfiguration.class)
                    .child(Bier.class).child(BierGlobal.class);

    public enum OperateType {
        CREATE,
        REPLACE,
        MERGE,
        DELETE;

        OperateType() {
        }
    }




    public static final int RETRY_WRITE_MAX = 3;

    @Override
    public void onSessionInitialized(BindingAwareBroker.ConsumerContext session) {

        LOG.info("session initialized ");
        mountService = session.getSALService(MountPointService.class);

    }

    public MountPoint getMountPoint(String nodeID) {
        BierConfigResult result = new BierConfigResult(BierConfigResult.ConfigurationResult.SUCCESSFUL);
        return DataGetter.getMountPoint(nodeID,result,mountService);

    }




    public <T extends DataObject> BierConfigResult operate(OperateType type,
                                                       DataBroker dataBroker,
                                                       final int tries,
                                                       InstanceIdentifier<T> path, T data) {

        BierConfigResult ncResult = new BierConfigResult(BierConfigResult.ConfigurationResult.SUCCESSFUL);

        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        switch (type) {
            case CREATE:
            case REPLACE:
                writeTransaction.put(LogicalDatastoreType.CONFIGURATION,path,data,true);
                break;
            case MERGE:
                writeTransaction.merge(LogicalDatastoreType.CONFIGURATION,path,data,true);
                break;
            case DELETE:
                writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, path);
                break;
            default:
                break;

        }

        final CheckedFuture<Void, TransactionCommitFailedException> submitResult = writeTransaction.submit();
        Futures.addCallback(submitResult, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
            }

            @Override
            public void onFailure(final Throwable throwable) {
                if (throwable instanceof OptimisticLockFailedException) {
                    if ((tries - 1) > 0) {
                        LOG.info("Got OptimisticLockFailedException - trying again");
                        operate(type, dataBroker, tries - 1, path ,data);
                    } else {
                        ncResult.setFailureReason(BierConfigResult.NETCONF_LOCK_FAILUE);
                    }

                } else {
                    ncResult.setFailureReason(BierConfigResult.NETCONF_EDIT_FAILUE + throwable.getMessage());
                }

            }
        });

        return ncResult;

    }




    public <T extends DataObject>  BierConfigResult write(OperateType type,
                                                                 String nodeId,
                                                                 InstanceIdentifier<T> path, T data) {

        BierConfigResult ncResult = new BierConfigResult(BierConfigResult.ConfigurationResult.SUCCESSFUL);
        final DataBroker nodeBroker = DataGetter.getDataBroker(nodeId, ncResult, mountService);
        if (nodeBroker == null) {
            return ncResult;
        }

        return operate(type, nodeBroker, RETRY_WRITE_MAX, path ,data);

    }



    public <T extends DataObject> T read(DataBroker dataBroker,
                                         InstanceIdentifier<T> path) {

        return DataGetter.readData(dataBroker,path);

    }

    public <T extends DataObject> T read(String nodeId,
                                                          InstanceIdentifier<T> path) {

        return DataGetter.readData(nodeId,path,mountService);

    }


}
