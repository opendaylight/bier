/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package test.org.opendaylight.bier.driver.listener;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.bier.driver.common.reporter.DriverNotificationProvider;

import org.opendaylight.bier.driver.listener.BierNotificationListener;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverFailure;
import org.opendaylight.yang.gen.v1.urn.bier.driver.reporter.rev170213.DriverFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrIdCollision;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrIdCollisionBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrZero;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrZeroBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainIdCollision;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainIdCollisionBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;

@RunWith(MockitoJUnitRunner.class)
public class BierNotificationListenerTest {




    @Mock
    NotificationPublishService notificationService;

    private BierNotificationListener bierNotificationListener = new BierNotificationListener();
    private static final String SUBDOMIAN_ID_COLLISION = "Subdomain ID collison : ";
    private static final String BFR_ZERO = "BFR ID is zero :";
    private static final String BFR_ID_COLLISION = "BFR ID collison : ";


    @Before
    public void before() throws Exception {
        DriverNotificationProvider driverNotificationProvider = new DriverNotificationProvider(notificationService);
    }

    @Test
    public void testOnSubDomainIdCollision() throws Exception {

        SubDomainIdCollision subDomainIdCollision = new SubDomainIdCollisionBuilder()
                .setSubDomainId(new SubDomainId(new Integer(100))).build();
        String failMessage = SUBDOMIAN_ID_COLLISION + subDomainIdCollision.getSubDomainId().toString();
        bierNotificationListener.onSubDomainIdCollision(subDomainIdCollision);
        DriverFailure driverFailure = new DriverFailureBuilder().setFailureMessage(failMessage).build();
        verify(notificationService).offerNotification(driverFailure);

    }


    @Test
    public void testOnBfrZero() throws Exception {
        BfrZero bfrZero = new BfrZeroBuilder().setIpv4BfrPrefix(new Ipv4Prefix("10.46.30.30/32")).build();
        String failMessage = BFR_ZERO + bfrZero.getIpv4BfrPrefix().toString();
        bierNotificationListener.onBfrZero(bfrZero);
        DriverFailure driverFailure = new DriverFailureBuilder().setFailureMessage(failMessage).build();
        verify(notificationService).offerNotification(driverFailure);


    }


    @Test
    public void testOnBfrIdCollision() throws Exception {
        BfrIdCollision bfrIdCollision = new BfrIdCollisionBuilder().setBfrId(new BfrId(100)) .build();
        String failMessage = BFR_ID_COLLISION + bfrIdCollision.getBfrId().toString();
        bierNotificationListener.onBfrIdCollision(bfrIdCollision);
        DriverFailure driverFailure = new DriverFailureBuilder().setFailureMessage(failMessage).build();
        verify(notificationService).offerNotification(driverFailure);

    }
}
