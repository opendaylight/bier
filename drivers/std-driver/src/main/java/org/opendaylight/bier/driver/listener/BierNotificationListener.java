/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.listener;

import org.opendaylight.bier.driver.common.reporter.DriverNotificationProvider;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrIdCollision;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrZero;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.IetfBierListener;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainIdCollision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierNotificationListener implements IetfBierListener {

    private static final Logger LOG = LoggerFactory.getLogger(BierNotificationListener.class);

    private static final String SUBDOMIAN_ID_COLLISION = "Subdomain ID collison : ";
    private static final String BFR_ZERO = "BFR ID is zero :";
    private static final String BFR_ID_COLLISION = "BFR ID collison : ";

    @Override
    public void onSubDomainIdCollision(SubDomainIdCollision notification) {
        LOG.info(SUBDOMIAN_ID_COLLISION + notification.getSubDomainId().toString());
        DriverNotificationProvider.notifyFailure(SUBDOMIAN_ID_COLLISION + notification.getSubDomainId().toString());

    }

    @Override
    public void onBfrZero(BfrZero notification) {

        LOG.info(BFR_ZERO + notification.getIpv4BfrPrefix().toString());
        DriverNotificationProvider.notifyFailure(BFR_ZERO + notification.getIpv4BfrPrefix().toString());
    }

    @Override
    public void onBfrIdCollision(BfrIdCollision notification) {
        LOG.info(BFR_ID_COLLISION + notification.getBfrId().toString());
        DriverNotificationProvider.notifyFailure(BFR_ID_COLLISION + notification.getBfrId().toString());
    }

}
