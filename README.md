# Farming for Blockheads

Minecraft Mod. Adds farming utilities, such as a seed market, fertilizer and feeding troughs.

- [Modpack Permissions](https://mods.twelveiterations.com/permissions)

#### Forge

[![Versions](http://cf.way2muchnoise.eu/versions/261924_latest.svg)](https://www.curseforge.com/minecraft/mc-mods/farming-for-blockheads)
[![Downloads](http://cf.way2muchnoise.eu/full_261924_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/farming-for-blockheads)

#### Fabric

[![Versions](http://cf.way2muchnoise.eu/versions/554586_latest.svg)](https://www.curseforge.com/minecraft/mc-mods/farming-for-blockheads-fabric)
[![Downloads](http://cf.way2muchnoise.eu/full_554586_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/farming-for-blockheads-fabric)

## Contributing

If you're interested in contributing to the mod, you can check
out [issues labelled as "help wanted"](https://github.com/TwelveIterations/FarmingForBlockheads/issues?q=is%3Aopen+is%3Aissue+label%3A%22help+wanted%22).

When it comes to new features, it's best to confer with me first to ensure we share the same vision. You can join us on [Discord](https://discord.gg/VAfZ2Nau6j) if you'd like to talk.

Contributions must be done through pull requests. I will not be able to accept translations, code or other assets through any other channels.

## Custom Market Entries

Data packs are now used to configure the market. You can place JSON files inside a `farmingforblockheads_compat`
folder (so the resulting path would be `data/<namespace>/farmingforblockheads_compat/{files}.json`) within a datapack which
will be loaded. For simple scenarios, you can also use the `MarketRegistry.json` file
under `config/farmingforblockheads/MarketRegistry.json`.

See here for an example: https://gist.github.com/BlayTheNinth/93a48d092d6dc570a64c81e1912dc4d0

Group overrides can be used to enable or disable inbuilt extensions.

Enabled by Default:

* Vanilla
    * Vanilla Seeds
    * Vanilla Saplings
    * Bone Meal
* Pam's Harvestcraft 2
   * Pam's HarvestCraft 2 Crops Seeds
   * Pam's HarvestCraft 2 Trees Saplings
* Biomes O' Plenty
    * BiomesOPlenty Saplings
* Create
    * Create Fertilizer
* Farmer's Delight
    * Farmers Delight Seeds
* Quark
    * Quark Saplings
* Simple Farming
    * SimpleFarming Saplings
    * SimpleFarming Seeds
* Supplementaries
    * Supplementaries Seeds
* Tropicraft
    * Tropicraft Fertilizer
    * Tropicraft Saplings

Optional Groups (many of
these [provided by seanimusprime](https://github.com/TwelveIterations/FarmingForBlockheads/issues/125)):

* Vanilla
    * Vanilla Flowers
    * Vanilla Mushrooms
    * Vanilla Crops
    * Vanilla Soil
    * Animal Eggs
* Biomes O' Plenty
    * BiomesOPlenty Flowers
    * BiomesOPlenty Mushroom
    * BiomesOPlenty Soil
* BetterEnd
    * BetterEnd Saplings
    * BetterEnd Seeds
    * BetterEnd Mushrooms
* BetterNether
    * BetterNether Saplings
    * BetterNether Seeds
    * BetterNether Mushrooms
    * BetterNether Soil
* Buzzier Bees
    * Buzzier Bees Flowers
* Cinderscapes
    * Cinderscapes Berries
    * Cinderscapes Soil
* Endergetic Expansion
    * Endergetic Expansion Soil
* Farmer's Delight
    * Farmers Delight Mushroom Colonies
    * Farmers Delight Soil
    * Farmers Delight Wild Crops
* Farming for Blockheads
    * Farming for Blockheads Fertilizer
* Minecolonies
    * Minecolonies Soil
* Minecraft Earth Mod
    * Minecraft Earth Flowers
* Nether's Delight
    * Nethers Delight Compost
* The Outer End
    * Outer End Berries
    * Outer End Flowers
    * Outer End Soil
* Quark
    * Quark Mushroom
    * Quark Soil
* The Abyss: Chapter II
    * The Abyss Saplings
    * The Abyss Soil
* Tropicraft
    * Tropicraft Flowers
    * Tropicraft Crops
* Twilight Forest
    * Twilight Forest Berries
    * Twilight Forest Saplings
    * Twilight Forest Soil

Some other mods may also add native support for their own seeds. Check the data packs added by those mods to find their
group names.

## IMC API

The below is a list of IMC messages handled by Farming for Blockheads.

* **RegisterMarketCategory** (NBT)
    * RegistryName (String) - Resource location, must be prefixed with your mod id!
    * Tooltip (String) - Should be the language key for the tooltip
    * Texture (String) - Resource location to the texture sheet for your category icon
    * TextureX (Integer) - X coordinate of the icon on the texture sheet (icons must be 20x20)
    * TextureY (Integer) - Y coordinate of the icon on the texture sheet (icons must be 20x20)
* **RegisterMarketEntry** (NBT)
    * OutputItem (ItemStack)
    * CostItem (ItemStack)
    * Category (String) - Resource location, must be a registered category. Default categories
      are `farmingforblockheads:seeds`, `farmingforblockheads:saplings` and `farmingforblockheads:other`

## Java API

If the IMC API is not enough for you, you can build against Farming for Blockheads' Java API.
The Java API allows everything the IMC API does, and certain tasks can only be achieved via the Java API.
However, if you don't need that extra control, it is recommended to use the IMC API.

## Adding Farming for Blockheads to a development environment

Note that you will also need to add Balm if you want to test your integration in your environment.

### Using CurseMaven

Add the following to your `build.gradle`:

```groovy
repositories {
    maven { url "https://www.cursemaven.com" }
}

dependencies {
    // Replace ${farmingforblockheads_file_id} and ${balm_file_id} with the id of the file you want to depend on.
    // You can find it in the URL of the file on CurseForge (e.g. 3914527).
    // Forge: implementation fg.deobf("curse.maven:balm-531761:${balm_file_id}")
    // Fabric: modImplementation "curse.maven:balm-fabric-500525:${balm_file_id}"
    
    // Forge: implementation fg.deobf("curse.maven:farming-for-blockheads-261924:${farmingforblockheads_file_id}")
    // Fabric: modImplementation "curse.maven:farming-for-blockheads-fabric-554586:${farmingforblockheads_file_id}"
}
```

### Using Twelve Iterations Maven (includes snapshot and mojmap versions)

Add the following to your `build.gradle`:

```groovy
repositories {
    maven { 
        url "https://maven.twelveiterations.com/repository/maven-public/" 
        
        content {
            includeGroup "net.blay09.mods"
        }
    }
}

dependencies {
    // Replace ${farmingforblockheads_version} and ${balm_version} with the version you want to depend on. 
    // You can find the latest version for a given Minecraft version at https://maven.twelveiterations.com/service/rest/repository/browse/maven-public/net/blay09/mods/balm-common/ and https://maven.twelveiterations.com/service/rest/repository/browse/maven-public/net/blay09/mods/farmingforblockheads-common/
    // Common (mojmap): implementation "net.blay09.mods:balm-common:${balm_version}"
    // Forge: implementation fg.deobf("net.blay09.mods:balm-forge:${balm_version}")
    // Fabric: modImplementation "net.blay09.mods:balm-fabric:${balm_version}"
    
    // Common (mojmap): implementation "net.blay09.mods:farmingforblockheads-common:${farmingforblockheads_version}"
    // Forge: implementation fg.deobf("net.blay09.mods:farmingforblockheads-forge:${farmingforblockheads_version}")
    // Fabric: modImplementation "net.blay09.mods:farmingforblockheads-fabric:${farmingforblockheads_version}"
}
```
