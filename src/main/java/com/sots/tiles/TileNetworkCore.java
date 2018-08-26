package com.sots.tiles;

import com.sots.routing.Network;

import java.util.UUID;

public class TileNetworkCore extends TileGenericPipe {

    private boolean ownsNetwork = false;

    private void makeNetwork() {
        if (!ownsNetwork) {
            network = new Network(UUID.randomUUID());
            nodeID = network.setRoot(this);
            hasNetwork = true;
            ownsNetwork = true;
        }
    }

    public void updateNetwork() {
        network.purgeNetwork();
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (!ownsNetwork) {
                makeNetwork();
            }
        }
    }
}
