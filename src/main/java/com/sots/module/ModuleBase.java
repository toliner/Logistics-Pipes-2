package com.sots.module;

import com.sots.tiles.TileRoutedPipe;

import java.util.UUID;

public class ModuleBase implements IModule {

    protected final UUID MODULE_ID;

    public ModuleBase(UUID ID) {
        MODULE_ID = ID;
    }

    @Override
    public boolean execute(TileRoutedPipe te) {
        return false;
    }

    @Override
    public boolean canExecute() {
        return false;
    }

    @Override
    public boolean canInsert() {
        return false;
    }

    @Override
    public ModuleType modType() {
        return ModuleType.NONE;
    }

    @Override
    public void disconnect() {
        return;
    }

}
