/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.channel.util;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;

public class ChannelDBContext {
    private final DataBroker dataBroker;

    public ChannelDBContext(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public ReadOnlyTransaction newReadOnlyTransaction() {
        return dataBroker.newReadOnlyTransaction();
    }

    public WriteTransaction newWriteOnlyTransaction() {
        return dataBroker.newWriteOnlyTransaction();
    }
}
