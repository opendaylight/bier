/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl;


import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.ReportMessage;
import org.opendaylight.yang.gen.v1.urn.bier.service.api.rev170105.ReportMessageBuilder;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationProvider {
    private static NotificationProvider instance = new NotificationProvider();
    private NotificationPublishService notificationService;
    private static final Logger LOG = LoggerFactory.getLogger(NotificationProvider.class);


    public NotificationProvider() {

    }

    public static NotificationProvider getInstance() {
        return instance;
    }

    public void setNotificationService(NotificationPublishService notificationService) {
        this.notificationService = notificationService;
    }

    public <T extends Notification> void notify(T notification) {
        if (null != notificationService) {
            LOG.info("notification publish!");
            notificationService.offerNotification(notification);
        }
    }

    public void notifyFailureReason(String failureReason) {
        LOG.info("report failureReason to app");
        ReportMessage message = new ReportMessageBuilder().setFailureReason(failureReason).build();
        NotificationProvider.getInstance().notify(message);
    }
}
