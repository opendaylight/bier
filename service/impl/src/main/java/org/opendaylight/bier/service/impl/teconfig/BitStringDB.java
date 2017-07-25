/*
 * Copyright © 2016 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bier.service.impl.teconfig;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.bier.bitstring.rev170523.BierBitstring;
import org.opendaylight.yang.gen.v1.urn.bier.bitstring.rev170523.bier.bitstring.ChannelBitstring;
import org.opendaylight.yang.gen.v1.urn.bier.bitstring.rev170523.bier.bitstring.ChannelBitstringBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bitstring.rev170523.bier.bitstring.ChannelBitstringKey;
import org.opendaylight.yang.gen.v1.urn.bier.bitstring.rev170523.bier.bitstring.channel.bitstring.PathBitstring;
import org.opendaylight.yang.gen.v1.urn.bier.bitstring.rev170523.bier.bitstring.channel.bitstring.PathBitstringBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.bitstring.rev170523.bier.bitstring.channel.bitstring.PathBitstringKey;
import org.opendaylight.yang.gen.v1.urn.bier.channel.rev161102.bier.network.channel.bier.channel.Channel;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.TePath;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.bier.te.path.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.bier.te.path.rev170503.bier.te.path.PathKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BitStringDB {

    private static final Logger LOG = LoggerFactory.getLogger(BitStringDB.class);
    private final DataBroker dataBroker;

    public BitStringDB(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public boolean  checkChannelPathExisted(String name) {
        ChannelBitstring channelBitstring = getBitStringFromChannelName(name);
        if (null == channelBitstring) {
            return false;
        }
        return true;
    }

    public boolean setBitStringToDataStore(Channel channel,TePath tePath) {
        if (null == channel || null == tePath) {
            return false;
        }
        ChannelBitstring channelBitstring = constructChannelBitString(channel,tePath);
        LOG.info("" + channelBitstring);

        InstanceIdentifier<ChannelBitstring> path = getChannelBitstringPath(channel.getName());
        LOG.info("" + path);

        final ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, path, channelBitstring, true);
        try {
            LOG.info("Write");
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("BitString:write DB fail!", e);
            return false;
        }
        return true;
    }

    public boolean delBitStringToDataStore(String name) {
        if (!checkChannelPathExisted(name)) {
            return true;
        }
        InstanceIdentifier<ChannelBitstring> path = getChannelBitstringPath(name);

        final ReadWriteTransaction tx = dataBroker.newReadWriteTransaction();

        tx.delete(LogicalDatastoreType.OPERATIONAL, path);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("BitString:delete DB fail!", e);
            return false;
        }
        return true;
    }

    public ChannelBitstring getBitStringFromChannelName(String name) {
        LOG.info("Get bit string from channel name");
        InstanceIdentifier<ChannelBitstring> path = getChannelBitstringPath(name);
        LOG.info("Path: " + path);
        try {
            Optional<ChannelBitstring> channelBitstring = dataBroker.newReadOnlyTransaction().read(
                    LogicalDatastoreType.OPERATIONAL, path).get();
            if (channelBitstring == null || !channelBitstring.isPresent()) {
                return null;
            }
            LOG.info("channel bitString: " + channelBitstring.get());
            return channelBitstring.get();
        } catch (ExecutionException | InterruptedException | IllegalStateException e) {
            LOG.warn("BitString:occur exception when read databroker {}", e);
            return null;
        }
    }

    public List<TePath> getTePathFromChannel(String name) {
        ChannelBitstring channelBitstring = getBitStringFromChannelName(name);
        List<TePath> tePathList = new ArrayList<>();
        if (channelBitstring != null) {
            for (PathBitstring pathBitstring : channelBitstring.getPathBitstring()) {
                PathBuilder builder = new PathBuilder();
                builder.setBitstring(pathBitstring.getBitstring());
                builder.setBitstringlength(pathBitstring.getBitstringlength());
                builder.setSi(pathBitstring.getSi());
                builder.setSubdomainId(pathBitstring.getSubdomainId());
                builder.setPathId(pathBitstring.getPathId());
                builder.setKey(new PathKey(pathBitstring.getPathId()));
                tePathList.add(builder.build());
            }
        }
        return tePathList;
    }

    private InstanceIdentifier<ChannelBitstring> getChannelBitstringPath(String name) {
        return InstanceIdentifier.create(BierBitstring.class)
                        .child(ChannelBitstring.class, new ChannelBitstringKey(name));
    }

    private ChannelBitstring constructChannelBitString(Channel channel,TePath tePath) {
        ChannelBitstringBuilder builder = new ChannelBitstringBuilder();
        builder.setDomainId(channel.getDomainId());
        builder.setSubDomainId(channel.getSubDomainId());
        builder.setKey(new ChannelBitstringKey(channel.getName()));
        builder.setName(channel.getName());

        PathBitstringBuilder pathBuilder = new PathBitstringBuilder();
        pathBuilder.setBitstringlength(tePath.getBitstringlength());
        pathBuilder.setSubdomainId(tePath.getSubdomainId());
        pathBuilder.setPathId(tePath.getPathId());
        pathBuilder.setSi(tePath.getSi());
        pathBuilder.setKey(new PathBitstringKey(tePath.getPathId()));
        pathBuilder.setBitstring(tePath.getBitstring());

        List<PathBitstring> pathList = new ArrayList<>();
        pathList.add(pathBuilder.build());

        builder.setPathBitstring(pathList);
        return builder.build();
    }

}
