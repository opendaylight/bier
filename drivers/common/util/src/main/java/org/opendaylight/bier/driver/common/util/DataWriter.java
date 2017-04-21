/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.common.util;


import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.bier.adapter.api.ConfigurationResult;

import org.opendaylight.bier.driver.common.reporter.DriverNotificationProvider;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.OptimisticLockFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataWriter {

    private static final Logger LOG = LoggerFactory.getLogger(DataGetter.class);

    public enum OperateType {
        CREATE,
        REPLACE,
        MERGE,
        DELETE;

        OperateType() {
        }
    }


    public static final String LOCK_FAILED_RETRY = "Got OptimisticLockFailedException - trying again";
    public static final String OPERATE_TYPE = " [Operate Type] ";
    public static final String DATA_INFO = " [Data Info] ";
    public static final String BLANK = "  ";


    public static <T extends DataObject> void operateFail(final Throwable throwable,OperateType type,
                            DataBroker dataBroker,InstanceIdentifier<T> path, T data,final int tries) {
        String detailedInfo = OPERATE_TYPE + type.toString() ;
        detailedInfo = detailedInfo + DATA_INFO;
        if (data != null) {
            detailedInfo = detailedInfo + data.toString();
        }

        if (throwable instanceof OptimisticLockFailedException) {
            if ((tries - 1) > 0) {
                LOG.info(LOCK_FAILED_RETRY);
                operate(type, dataBroker, tries - 1, path, data);
            } else {
                LOG.warn(ConfigurationResult.NETCONF_LOCK_FAILUE +  detailedInfo);
                //report NETCONF_LOCK_FAILUE
                DriverNotificationProvider.notifyFailure(
                        ConfigurationResult.NETCONF_LOCK_FAILUE +  detailedInfo);

            }

        } else if (throwable instanceof TransactionCommitFailedException) {
            LOG.warn(ConfigurationResult.NETCONF_COMMIT_FAILUE +  detailedInfo);
            DriverNotificationProvider.notifyFailure(
                    ConfigurationResult.NETCONF_COMMIT_FAILUE +  detailedInfo);


        } else {
            LOG.warn(ConfigurationResult.NETCONF_EDIT_FAILUE +  detailedInfo + BLANK + throwable.getMessage());
        }


    }

    public static <T extends DataObject> CheckedFuture<Void, TransactionCommitFailedException> operate(OperateType type,
                                                              DataBroker dataBroker,
                                                              final int tries,
                                                              InstanceIdentifier<T> path, T data) {

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
                operateFail(throwable,type,dataBroker,path,data,tries);
            }
        });

        return submitResult;

    }


}
