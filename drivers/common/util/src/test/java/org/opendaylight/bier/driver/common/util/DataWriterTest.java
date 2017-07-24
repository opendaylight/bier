/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.org.opendaylight.bier.driver.common.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.CheckedFuture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.bier.adapter.api.ConfigurationResult;
import org.opendaylight.bier.driver.common.reporter.DriverNotificationProvider;
import org.opendaylight.bier.driver.common.util.DataWriter;
import org.opendaylight.bier.driver.common.util.IidConstants;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.OptimisticLockFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.BierChannel;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverFailure;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverFailureBuilder;

@RunWith(MockitoJUnitRunner.class)
public class DataWriterTest {
    @Mock
    DataBroker dataBroker;
    @Mock
    WriteTransaction writeTransaction;
    @Mock
    BierChannel bierChannel;
    @Mock
    NotificationPublishService notificationService;

    private CheckedFuture<Void, TransactionCommitFailedException> submitResult;

    private static final int TRY_TIME_MAX = 3;




    private void initInstance() {
        DriverNotificationProvider driverNotificationProvider = new DriverNotificationProvider(notificationService);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        submitResult = mock(CheckedFuture.class);
        when(writeTransaction.submit()).thenReturn(submitResult);
    }

    @Test
    public void testOperateCreate() throws Exception {
        initInstance();
        DataWriter.operate(DataWriter.OperateType.CREATE,
                dataBroker,TRY_TIME_MAX, IidConstants.BIER_CHANNEL_IID,bierChannel);
        verify(writeTransaction).put(LogicalDatastoreType.CONFIGURATION,IidConstants.BIER_CHANNEL_IID,bierChannel,true);


    }

    @Test
    public void testOperateReplace() throws Exception {
        initInstance();
        DataWriter.operate(DataWriter.OperateType.REPLACE,
                dataBroker,TRY_TIME_MAX, IidConstants.BIER_CHANNEL_IID,bierChannel);
        verify(writeTransaction).put(LogicalDatastoreType.CONFIGURATION,IidConstants.BIER_CHANNEL_IID,bierChannel,true);


    }

    @Test
    public void testOperateMerge() throws Exception {
        initInstance();
        DataWriter.operate(DataWriter.OperateType.MERGE,
                dataBroker,TRY_TIME_MAX, IidConstants.BIER_CHANNEL_IID,bierChannel);
        verify(writeTransaction).merge(LogicalDatastoreType.CONFIGURATION,
                IidConstants.BIER_CHANNEL_IID, bierChannel, true);


    }

    @Test
    public void testOperateDelte() throws Exception {
        initInstance();
        DataWriter.operate(DataWriter.OperateType.DELETE,
                dataBroker,TRY_TIME_MAX, IidConstants.BIER_CHANNEL_IID,bierChannel);
        verify(writeTransaction).delete(LogicalDatastoreType.CONFIGURATION, IidConstants.BIER_CHANNEL_IID);

    }

    @Test
    public void testOperateLockFailTryAgain() throws Exception {
        initInstance();
        OptimisticLockFailedException throwable = mock(OptimisticLockFailedException.class);
        DataWriter.operateFail(throwable,DataWriter.OperateType.DELETE,
                dataBroker,IidConstants.BIER_CHANNEL_IID,bierChannel,TRY_TIME_MAX);
        verify(writeTransaction).delete(LogicalDatastoreType.CONFIGURATION, IidConstants.BIER_CHANNEL_IID);
    }

    @Test
    public void testOperateFailureLockFail() throws Exception {
        initInstance();
        OptimisticLockFailedException throwable = mock(OptimisticLockFailedException.class);
        DataWriter.operateFail(throwable,DataWriter.OperateType.DELETE,
                dataBroker,IidConstants.BIER_CHANNEL_IID,bierChannel,1);

        String detailedInfo = ConfigurationResult.NETCONF_LOCK_FAILUE
                + DataWriter.OPERATE_TYPE
                + DataWriter.OperateType.DELETE.toString()
                + DataWriter.DATA_INFO
                + bierChannel.toString();


        DriverFailure driverFailure = new DriverFailureBuilder().setFailureMessage(detailedInfo).build();
        verify(notificationService).offerNotification(driverFailure);



    }

    @Test
    public void testOperateFailureCommitFail() throws Exception {
        initInstance();
        TransactionCommitFailedException throwable = mock(TransactionCommitFailedException.class);
        DataWriter.operateFail(throwable,DataWriter.OperateType.DELETE,
                dataBroker,IidConstants.BIER_CHANNEL_IID,bierChannel,1);
        String detailedInfo = ConfigurationResult.NETCONF_COMMIT_FAILUE
                + DataWriter.OPERATE_TYPE
                + DataWriter.OperateType.DELETE.toString()
                + DataWriter.DATA_INFO
                + bierChannel.toString();


        DriverFailure driverFailure = new DriverFailureBuilder().setFailureMessage(detailedInfo).build();
        verify(notificationService).offerNotification(driverFailure);



    }
}