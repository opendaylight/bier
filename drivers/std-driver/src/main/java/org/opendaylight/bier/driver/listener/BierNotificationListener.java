/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.listener;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrIdCollision;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrZero;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.IetfBierListener;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainIdCollision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BierNotificationListener implements IetfBierListener {

    private static final Logger LOG = LoggerFactory.getLogger(BierNotificationListener.class);

    @Override
    public void onSubDomainIdCollision(SubDomainIdCollision notification) {
        LOG.info("SubDomainIdCollision {}",notification);

    }

    @Override
    public void onBfrZero(BfrZero notification) {
        LOG.info("BfrZero {}",notification);
    }

    @Override
    public void onBfrIdCollision(BfrIdCollision notification) {


        LOG.info("BfrIdCollision {}",notification);
    }

}
