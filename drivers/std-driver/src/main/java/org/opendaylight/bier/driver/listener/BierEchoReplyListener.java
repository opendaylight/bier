/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.listener;

import org.opendaylight.bier.driver.common.reporter.DriverNotificationProvider;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverNotifyBierEchoReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.BierEchoReplyNotify;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.IetfBierNotificationListener;


public class BierEchoReplyListener implements IetfBierNotificationListener {

    @Override
    public void onBierEchoReplyNotify(BierEchoReplyNotify notification) {

        DriverNotificationProvider.notifyEchoReply(new DriverNotifyBierEchoReplyBuilder()
                .setAddressInfo(notification.getAddressInfo())
                .setReturnInfo(notification.getReturnInfo())
                .build());
    }
}

