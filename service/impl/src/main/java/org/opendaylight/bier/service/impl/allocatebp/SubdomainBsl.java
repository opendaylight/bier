/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bier.service.impl.allocatebp;

import org.opendaylight.yang.gen.v1.urn.bier.topology.rev161102.bier.te.node.params.te.domain.te.sub.domain.TeBsl;

public class SubdomainBsl {
    private Integer subdomainId;
    private TeBsl teBsl;

    public SubdomainBsl(Integer subdomainId, TeBsl teBsl) {
        this.subdomainId = subdomainId;
        this.teBsl = teBsl;
    }

    public Integer getSubdomainId() {
        return subdomainId;
    }

    public TeBsl getTeBsl() {
        return teBsl;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        SubdomainBsl other = (SubdomainBsl)obj;
        return this.subdomainId.equals(other.getSubdomainId()) && this.getTeBsl().getBitstringlength()
                .equals(other.getTeBsl().getBitstringlength());
    }
}
