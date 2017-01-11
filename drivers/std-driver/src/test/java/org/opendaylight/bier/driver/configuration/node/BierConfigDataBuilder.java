/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.driver.configuration.node;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.bier.common.rev161102.DomainId;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.Domain;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.node.params.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BfrId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierEncapsulationMpls;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.BierMplsLabelRangeSize;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.Bsl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.IgpType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.SubDomainId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobal;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.BierGlobalBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomain;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.global.cfg.bier.global.SubDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.AfBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.bier.rev160723.bier.subdomain.af.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.mpls.rev160705.MplsLabel;




public class BierConfigDataBuilder {

    public static final SubDomainId SUBDOMAINID = new SubDomainId(new Integer(55));
    public static final Integer BSL = new Integer(64);
    public static final MplsLabel MPLS_LABEL_BASE = new MplsLabel(new Long(10));

    public Ipv4 buildIpv4SingleAdd() {
        return new Ipv4Builder().setBitstringlength(BSL)
                .setBierMplsLabelBase(MPLS_LABEL_BASE)
                .setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(new Short((short) 1)))
                .build();

    }

    public Ipv4 buildIpv4SingleModify() {
        return new Ipv4Builder().setBitstringlength(BSL)
                .setBierMplsLabelBase(MPLS_LABEL_BASE)
                .setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(new Short((short) 2)))
                .build();

    }

    public Ipv4 buildIpv4SingleDelete() {
        return new Ipv4Builder()
                .setBitstringlength(BSL)
                .setBierMplsLabelBase(MPLS_LABEL_BASE)
                .build();
    }

    public Ipv6 buildIpv6SingleAdd() {
        return new Ipv6Builder().setBitstringlength(BSL)
                .setBierMplsLabelBase(MPLS_LABEL_BASE)
                .setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(new Short((short) 1)))
                .build();

    }

    public Ipv6 buildIpv6SingleModify() {
        return new Ipv6Builder().setBitstringlength(BSL)
                .setBierMplsLabelBase(MPLS_LABEL_BASE)
                .setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(new Short((short) 2)))
                .build();

    }

    public Ipv6 buildIpv6SingleDelete() {
        return new Ipv6Builder()
                .setBitstringlength(BSL)
                .setBierMplsLabelBase(MPLS_LABEL_BASE)
                .build();
    }


    public List<Ipv4> buildIpv4() {
        ArrayList<Ipv4> ipv4ArrayList = new ArrayList<Ipv4>();
        ipv4ArrayList.add(new Ipv4Builder().setBitstringlength(64)
                .setBierMplsLabelBase(new MplsLabel(new Long(10)))
                .setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(new Short((short) 1)))
                .build());
        ipv4ArrayList.add(new Ipv4Builder().setBitstringlength(256)
                .setBierMplsLabelBase(new MplsLabel(new Long(100)))
                .setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(new Short((short) 2)))
                .build());
        return ipv4ArrayList;
    }

    public List<Ipv6> buildIpv6() {
        ArrayList<Ipv6> ipv6ArrayList = new ArrayList<Ipv6>();
        ipv6ArrayList.add(new Ipv6Builder().setBitstringlength(128)
                .setBierMplsLabelBase(new MplsLabel(new Long(20)))
                .setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(new Short((short)3)))
                .build());
        ipv6ArrayList.add(new Ipv6Builder().setBitstringlength(512)
                .setBierMplsLabelBase(new MplsLabel(new Long(200)))
                .setBierMplsLabelRangeSize(new BierMplsLabelRangeSize(new Short((short)4)))
                .build());
        return ipv6ArrayList;

    }

    public SubDomain buildSubDomainSingle() {

        return new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(88)))
                .setBfrId(new BfrId(1))
                .setBitstringlength(Bsl._1024Bit)
                .setIgpType(IgpType.OSPF)
                .setAf(new AfBuilder()
                        .setIpv4(buildIpv4())
                        .setIpv6(buildIpv6())
                        .build())
                .build();
    }

    public SubDomain buildSubDomainSingleModify() {
        return new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(88)))
                .setBfrId(new BfrId(2))
                .setBitstringlength(Bsl._2048Bit)
                .setIgpType(IgpType.ISIS)
                .setAf(new AfBuilder()
                        .setIpv4(buildIpv4())
                        .setIpv6(buildIpv6())
                        .build())
                .build();
    }


    public SubDomain buildSubDomainDelete() {
        return new SubDomainBuilder()
                .setSubDomainId(SUBDOMAINID)
                .build();
    }

    public List<SubDomain> buildSubDomain() {
        ArrayList<SubDomain> subDomainArrayList = new ArrayList<SubDomain>();
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(55)))
                .setBfrId(new BfrId(1))
                .setBitstringlength(Bsl._1024Bit)
                .setIgpType(IgpType.OSPF)
                .setAf(new AfBuilder()
                        .setIpv4(buildIpv4())
                        .setIpv6(buildIpv6())
                        .build())
                .build());
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(66)))
                .setBfrId(new BfrId(2))
                .setBitstringlength(Bsl._2048Bit)
                .setIgpType(IgpType.ISIS)
                .build());
        return subDomainArrayList;
    }

    public List<SubDomain> buildSubDomainModify() {
        ArrayList<SubDomain> subDomainArrayList = new ArrayList<SubDomain>();
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(55)))
                .setBfrId(new BfrId(10))
                .setBitstringlength(Bsl._512Bit)
                .setIgpType(IgpType.ISIS)
                .setAf(new AfBuilder()
                        .setIpv4(buildIpv4())
                        .setIpv6(buildIpv6())
                        .build())
                .build());
        subDomainArrayList.add(new SubDomainBuilder()
                .setSubDomainId(new SubDomainId(new Integer(66)))
                .setBfrId(new BfrId(20))
                .setBitstringlength(Bsl._128Bit)
                .setIgpType(IgpType.OSPF)
                .build());
        return subDomainArrayList;
    }


    public BierGlobal buildBierGlobalModify() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(4)))
                .setBitstringlength(Bsl._64Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.61/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("fe81::/10"))
                .setSubDomain(buildSubDomain())
                .setEncapsulationType(BierEncapsulationMpls.class)
                .build();

    }

    public BierGlobal buildBierGlobal() {
        return new BierGlobalBuilder()
                .setBfrId(new BfrId(new Integer(3)))
                .setBitstringlength(Bsl._4096Bit)
                .setIpv4BfrPrefix(new Ipv4Prefix("10.42.93.60/32"))
                .setIpv6BfrPrefix(new Ipv6Prefix("fe80::/10"))
                .setSubDomain(buildSubDomain())
                .setEncapsulationType(BierEncapsulationMpls.class)
                .build();

    }

    public Domain buildDomain() {

        return  new DomainBuilder()
                .setBierGlobal(buildBierGlobal())
                .setDomainId(new DomainId(1))
                .build();
    }

    public Domain buildDomainModify() {

        return  new DomainBuilder()
                .setBierGlobal(buildBierGlobalModify())
                .setDomainId(new DomainId(1))
                .build();
    }
}
