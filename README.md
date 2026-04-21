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
