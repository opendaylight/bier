/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.driver.listener;

import static org.mockito.Mockito.verify;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.bier.driver.common.reporter.DriverNotificationProvider;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverNotifyBierEchoReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.BierEchoReplyNotify;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.BierEchoReplyNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.ReturnCode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.bier.echo.reply.ReturnInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.bier.echo.reply.address.info.BierTe;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.notification.rev170821.bier.echo.reply.address.info.BierTeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Si;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.BierTeBpInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.BierTeBpInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.bitstring.Bitpositions;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808.bier.te.address.bier.te.bp.info.bitstring.BitpositionsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.te.rev161013.BitString;


@RunWith(MockitoJUnitRunner.class)
public class BierEchoReplyListenerTest {

    private static final SubDomainId SUB_DOMAIN_ID = new SubDomainId(1);
    private static final Bsl BIER_BSL = Bsl._64Bit;
    private static final Si BIER_SI = new Si(1);
    private static final Bitpositions BIER_BITPOSITIONS = new BitpositionsBuilder()
            .setBitposition(new BitString(1)).build();
    private static final ReturnCode ECHO_REPLY_CODE = ReturnCode.ForwardSuccess;


    @Mock
    NotificationPublishService notificationService;

    private BierEchoReplyListener bierEchoReplyListener = new BierEchoReplyListener();

    @Before
    public void before() throws Exception {
        DriverNotificationProvider driverNotificationProvider = new DriverNotificationProvider(notificationService);
    }

    private BierEchoReplyNotify buildNotify() {
        BierTeBpInfo bierTeBpInfo = new BierTeBpInfoBuilder()
                .setBierTeBsl(BIER_BSL)
                .setBitstring(Collections.singletonList(new org.opendaylight.yang
                        .gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rpcs.rev170808
                        .bier.te.address.bier.te.bp.info.BitstringBuilder()
                        .setSi(BIER_SI)
                        .setBitpositions(Collections.singletonList(BIER_BITPOSITIONS))
                        .build()))
                .build();

        BierTe bierTe = new BierTeBuilder()
                .setBierTeSubdomainid(SUB_DOMAIN_ID)
                .setBierTeBpInfo(Collections.singletonList(bierTeBpInfo))
                .build();


        return new BierEchoReplyNotifyBuilder()
                .setAddressInfo(bierTe)
                .setReturnInfo(Collections.singletonList(new ReturnInfoBuilder()
                        .setReturnCode(ECHO_REPLY_CODE).build()))
                .build();
    }

    @Test
    public void testOnBierEchoReplyNotify() throws Exception {
        BierEchoReplyNotify bierEchoReplyNotify = buildNotify();
        bierEchoReplyListener.onBierEchoReplyNotify(bierEchoReplyNotify);

        verify(notificationService).offerNotification(
                new DriverNotifyBierEchoReplyBuilder()
                        .setAddressInfo(bierEchoReplyNotify.getAddressInfo())
                        .setReturnInfo(bierEchoReplyNotify.getReturnInfo())
                        .build()
        );

    }


}
