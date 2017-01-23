/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.adapter.api;

import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;

public interface BierConfigWriter {



    ConfigurationResult writeDomain(ConfigurationType type, String nodeId, Domain domain);

    ConfigurationResult writeSubdomain(ConfigurationType type, String nodeId,
                                           DomainId domainId, SubDomain subDomain);


    ConfigurationResult writeSubdomainIpv4(ConfigurationType type, String nodeId,
                       DomainId domainId, SubDomainId subDomainId,
                       Ipv4 ipv4);

    ConfigurationResult writeSubdomainIpv6(ConfigurationType type, String nodeId,
                       DomainId domainId, SubDomainId subDomainId,
                       Ipv6 ipv6);

}
