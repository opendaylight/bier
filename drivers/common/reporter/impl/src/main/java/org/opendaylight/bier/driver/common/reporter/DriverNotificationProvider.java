/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.common.reporter;

import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverFailure;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverNotifyBierEchoReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DriverNotificationProvider {

    private static NotificationPublishService notificationService;
    private static final Logger LOG = LoggerFactory.getLogger(DriverNotificationProvider.class);

    public DriverNotificationProvider(NotificationPublishService notificationService) {
        LOG.info("set notificationService {}",notificationService);
        this.notificationService = notificationService;
    }

    public void init() {
        LOG.info("session init");
    }

    public void close() {
        LOG.info("session close");
    }


    public static void notifyFailure(String failureMessage) {
        LOG.info("report failure : {}",failureMessage);
        DriverFailure driverFailure = new DriverFailureBuilder().setFailureMessage(failureMessage).build();
        if (null != notificationService) {
            LOG.info("notification publish : {}",failureMessage);
            notificationService.offerNotification(driverFailure);
        }
    }

    public static void notifyEchoReply(DriverNotifyBierEchoReply echoReply) {
        LOG.info("notify echo relply : {}",echoReply);

        if (null != notificationService) {
            LOG.info("notification publish : {}",echoReply);
            notificationService.offerNotification(echoReply);
        }
    }
}
