/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.allocatebp;

import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;
import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.te.bsl.TeSi;

public class SubdomainBslSi {
    private Integer subdomainValue;
    private TeBsl teBsl;
    private TeSi teSi;

    public SubdomainBslSi(Integer subdomainValue, TeBsl teBsl, TeSi teSi) {
        this.subdomainValue = subdomainValue;
        this.teBsl = teBsl;
        this.teSi = teSi;
    }

    public Integer getSubdomainValue() {
        return subdomainValue;
    }

    public TeBsl getTeBsl() {
        return teBsl;
    }

    public TeSi getTeSi() {
        return teSi;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        SubdomainBslSi other = (SubdomainBslSi)obj;
        boolean subDomainEqual = this.subdomainValue.equals(other.getSubdomainValue());
        boolean teBslEqual = this.teBsl.getBitstringlength().equals(other.getTeBsl().getBitstringlength());
        boolean teSiEqual = this.teSi.getSi().equals(other.getTeSi().getSi());
        return subDomainEqual && teBslEqual && teSiEqual;
    }
}
