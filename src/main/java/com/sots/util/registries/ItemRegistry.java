package com.sots.util.registries;

import com.sots.item.IngotTitanium;
import com.sots.item.ItemWrench;
import com.sots.item.LPItemBase;
import com.sots.item.ShardRutile;
import com.sots.item.modules.*;
import com.sots.item.parts.*;
import com.sots.util.StringUtil;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class ItemRegistry {

    //All Items to be Registered
    public static List<LPItemBase> registry = new ArrayList<LPItemBase>();
    public static LPItemBase shard_rutile;
    public static LPItemBase ingot_titanium;
    public static LPItemBase part_fpga;
    public static LPItemBase part_order;
    public static LPItemBase part_sink;
    public static LPItemBase part_extract;
    public static LPItemBase part_provider;
    public static LPItemBase module_base;
    public static LPItemBase module_order;
    public static LPItemBase module_provide;
    public static LPItemBase module_sink;
    public static LPItemBase module_sort;
    public static LPItemBase module_extract;
    public static LPItemBase item_wrench;

    /**
     * Initialize all Items for preInit
     * Call this in preInit on the Common Proxy
     */
    public static void init() {
        //Init Items
        shard_rutile = new ShardRutile();
        ingot_titanium = new IngotTitanium();
        part_fpga = new PartFPGA();
        part_order = new PartOrderProc();
        part_sink = new PartSinkProc();
        part_extract = new PartExtractProc();
        part_provider = new PartProviderProc();
        module_base = new ItemModuleBase();
        module_order = new ItemModuleOrder();
        module_provide = new ItemModuleProvide();
        module_sink = new ItemModuleSink();
        module_sort = new ItemModuleSort();
        module_extract = new ItemModuleExtract();
        item_wrench = new ItemWrench();

        registry.add(shard_rutile);
        registry.add(ingot_titanium);
        registry.add(part_fpga);
        registry.add(part_order);
        registry.add(part_extract);
        registry.add(part_provider);
        registry.add(part_sink);
        registry.add(module_provide);
        registry.add(module_sink);
        registry.add(module_sort);
        registry.add(module_order);
        registry.add(module_extract);
        registry.add(item_wrench);


        //Register Items
        for (LPItemBase item : registry) {
            //Register Item
            GameRegistry.register(item);
            //Register Ordedict Names
            if (!StringUtil.isNullOrWhitespace(item.getOreName()) && !StringUtil.isNullOrEmpty(item.getOreName()))
                OreDictionary.registerOre(item.getOreName(), item);
        }
    }

    /**
     * Initializes Models and Textures for registered Items.
     * Call this in Init on the Client Proxy
     */
    public static void initModels() {
        for (LPItemBase item : registry) {
            item.initModel();
        }
    }
}
