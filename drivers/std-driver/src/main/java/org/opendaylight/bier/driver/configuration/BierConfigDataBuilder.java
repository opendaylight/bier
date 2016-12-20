/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


import org.opendaylight.bier.adapter.api.BierConfigWriter.ConfigurationType;

import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;


import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierConfiguration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierConfigurationBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobalBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.AfBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.routing.BierBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev161020.Routing;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev161020.RoutingBuilder;




public class BierConfigDataBuilder {

    public static Routing build(ConfigurationType type, Domain domain) {

        if (type == ConfigurationType.DELETE) {

            return new RoutingBuilder()
                    .removeAugmentation(BierConfiguration.class)
                    .build();
        } else {
            return new RoutingBuilder()
                    .addAugmentation(BierConfiguration.class,
                            new BierConfigurationBuilder()
                                    .setBier(new BierBuilder()
                                            .setBierGlobal(domain.getBierGlobal())
                                            .build())
                                    .build())
                    .build();


        }
    }

    public static Routing build(ConfigurationType type,
                                SubDomain subDomain,
                                BierConfiguration bierConfiguration) {

        List<SubDomain> subDomains = bierConfiguration.getBier().getBierGlobal().getSubDomain();

        Iterator iter = subDomains.iterator();
        while (iter.hasNext()) {
            SubDomain subDomainIter = (SubDomain)iter.next();
            if (subDomainIter.getKey().equals(subDomain.getKey())) {
                iter.remove();
                break;
            }
        }

        return new RoutingBuilder()
                .addAugmentation(BierConfiguration.class,
                        new BierConfigurationBuilder(bierConfiguration).build())
                .build();

    }

    public static Routing build(ConfigurationType type,
                                SubDomain subDomain) {

        return new RoutingBuilder()
                .addAugmentation(BierConfiguration.class,new BierConfigurationBuilder()
                        .setBier(new BierBuilder()
                                .setBierGlobal(new BierGlobalBuilder()
                                        .setSubDomain(Collections.singletonList(subDomain))
                                        .build()
                                )
                                .build())
                        .build())
                .build();

    }


    public static Routing build(ConfigurationType type,
                                SubDomainId subDomainId, Ipv4 ipv4,
                                BierConfiguration bierConfiguration) {

        List<SubDomain> subDomains = bierConfiguration.getBier().getBierGlobal().getSubDomain();


        Iterator iter = subDomains.iterator();

        while (iter.hasNext()) {
            SubDomain subDomainIter = (SubDomain)iter.next();
            if (subDomainIter.getKey().getSubDomainId().equals(subDomainId)) {
                List<Ipv4> ipv4s = subDomainIter.getAf().getIpv4();
                Iterator iter2 = ipv4s.iterator();

                while (iter2.hasNext()) {
                    Ipv4 ipv4Iter = (Ipv4)iter2.next();
                    if ( ipv4Iter.getKey().equals(ipv4.getKey()) ) {
                        iter2.remove();
                        break;
                    }
                }
                break;
            }
        }

        return new RoutingBuilder()
                .addAugmentation(BierConfiguration.class,
                        new BierConfigurationBuilder(bierConfiguration).build())
                .build();

    }

    public static Routing build(ConfigurationType type,
                                SubDomainId subDomainId,Ipv4 ipv4) {

        return new RoutingBuilder()
                .addAugmentation(BierConfiguration.class,new BierConfigurationBuilder()
                        .setBier(new BierBuilder()
                                .setBierGlobal(new BierGlobalBuilder()
                                        .setSubDomain(Collections.singletonList(
                                                new SubDomainBuilder()
                                                        .setSubDomainId(subDomainId)
                                                        .setAf(new AfBuilder()
                                                                .setIpv4(Collections.singletonList(ipv4))
                                                                .build())
                                                        .build()))
                                        .build()
                                )
                                .build())
                        .build())
                .build();
    }


    public static Routing build(ConfigurationType type,
                                SubDomainId subDomainId,Ipv6 ipv6,
                                BierConfiguration bierConfiguration) {

        List<SubDomain> subDomains = bierConfiguration.getBier().getBierGlobal().getSubDomain();


        Iterator iter = subDomains.iterator();

        while (iter.hasNext()) {
            SubDomain subDomainIter = (SubDomain)iter.next();
            if (subDomainIter.getKey().getSubDomainId().equals(subDomainId)) {
                List<Ipv6> ipv6s = subDomainIter.getAf().getIpv6();
                Iterator iter2 = ipv6s.iterator();

                while (iter2.hasNext()) {
                    Ipv6 ipv6Iter = (Ipv6)iter2.next();
                    if ( ipv6Iter.getKey().equals(ipv6.getKey()) ) {
                        iter2.remove();
                        break;
                    }
                }
                break;
            }
        }

        return new RoutingBuilder()
                .addAugmentation(BierConfiguration.class,
                        new BierConfigurationBuilder(bierConfiguration).build())
                .build();

    }

    public static Routing build(ConfigurationType type,
                                SubDomainId subDomainId,Ipv6 ipv6) {

        return new RoutingBuilder()
                .addAugmentation(BierConfiguration.class,new BierConfigurationBuilder()
                        .setBier(new BierBuilder()
                                .setBierGlobal(new BierGlobalBuilder()
                                        .setSubDomain(Collections.singletonList(
                                                new SubDomainBuilder()
                                                        .setSubDomainId(subDomainId)
                                                        .setAf(new AfBuilder()
                                                                .setIpv6(Collections.singletonList(ipv6))
                                                                .build())
                                                        .build()))
                                        .build()
                                )
                                .build())
                        .build())
                .build();
    }



}
