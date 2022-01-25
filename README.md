# StructuredModLoader
[![CurseForge](https://cf.way2muchnoise.eu/573056.svg)](https://www.curseforge.com/minecraft/mc-mods/structured-mod-loader-forge)[![GitHub](https://img.shields.io/badge/Git-Hub-blue)](https://github.com/Chaos02/SubFolderLoader)[![GitHub issues](https://img.shields.io/github/issues/Chaos02/SubFolderLoader?logo=GitHub)](https://github.com/Chaos02/SubFolderLoader/issues)[![GitHub stars](https://img.shields.io/github/stars/Chaos02/SubFolderLoader?logo=GitHub)](https://github.com/Chaos02/SubFolderLoader/stargazers)

[![CurseVersions](https://cf.way2muchnoise.eu/versions/573056.svg)](https://www.curseforge.com/minecraft/mc-mods/structured-mod-loader-forge/files/all)

StructuredModLoader, short SML, is a [Minecraft](minecraft.net) [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/) mod for those that wish to achieve a tidier `\mods` folder!
Once placed inside the main mods directory, it will recursively search for `.jar` files in all subfolders and present them to the regular Forge modloader FML.
This Mod is mainly aimed at modpack creators, to keep track of library mods and actually content-adding ones [See example directory tree][1]
It mod will log everything it does or *does not* to the regular gamelog.
## Getting started:
It's as easy, as installing literally any other mod:
Drop the downloaded .jar file from the [Releases](https://github.com/Chaos02/SubFolderLoader/tags) page into the `mods` folder of your Minecraft Forge installation!
By default directories containing one of the keywords
***ignore***,***unstable*** and ***disable***
will be excluded from loading, aswell as any folder that is deeper than ***3*** folders in the directory tree.
### Configuration:
Upon first launch, the Mod will create a config file located at `\config\StructuredModLoader.toml`, where you can configure the infinite list of keywords to be excluded aswell as the maximum recursion depth, ***capped at 5***
If the file should get deleted or its Syntax ([TOML](https://github.com/toml-lang/toml)) is invalid, the invalid values will be reset to their default and the file will be created again.
#### Examples:
[1]: Modpack setup:
```
Directory depth will be counted from the \mods folder,
Content is depth 1, tech depth 2 and AE2 depth 3.
Any number above 3 will be excluded by default.

(regular mods folder)
(second bar here)
   \/

mod pack root
├── config
├── LICENSE
├── logs
├── mods
│   ├── Content
│   │   ├── flora
│   │   ├── furniture-7.0.0-pre28-1.18.1.jar
│   │   ├── gemsnjewels-1.18.1-0.2.3.jar
│   │   ├── ironchest-1.18-13.0.5.jar
│   │   ├── mcw-bridges-2.0.1-mc1.18.1.jar
│   │   ├── miscellaneous
│   │   ├── tech
│   │   │   ├── AppliedEnergistics2
│   │   │   │   ├── This folder WONT get loaded by default
│   │   │   │   │   ├── notLoadedMods.jar
│   │   │   │   └── appliedenergistics2-10.0.0-beta.4.jar
│   │   │   ├── Create
│   │   │   │   ├── createaddition-1.18.1-20220111b.jar
│   │   │   │   └── create-mc1.18.1_v0.4c.jar
│   │   │   ├── EnderIO
│   │   │   ├── Immersive Engineering
│   │   │   │   └── ImmersiveEngineering-1.18.1-7.0.0-142.jar
│   │   │   ├── Refined Storage
│   │   │   │   ├── ExtraDisks-1.18.1-2.0.2.jar
│   │   │   │   ├── ExtraStorage-1.18.1-2.0.1.jar
│   │   │   │   ├── refinedstorage-1.10.0-beta.4.jar
│   │   │   │   └── rsrequestify-2.2.0.jar
│   │   │   ├── RFTools
│   │   │   │   ├── rftoolsbase-1.18-3.0.3.jar
│   │   │   │   ├── rftoolsbuilder-1.18-4.0.5.jar
│   │   │   │   ├── rftoolscontrol-1.18-5.0.1.jar
│   │   │   │   ├── rftoolspower-1.18-4.0.3.jar
│   │   │   │   ├── rftoolsstorage-1.18-3.0.5.jar
│   │   │   │   └── rftoolsutility-1.18-4.0.6.jar
│   │   │   └── WorldGeneration
│   ├── game engine improvements
│   ├── game engine improvements (unstable)
│   ├── LibrariesAndAPIs
│   │   └── XYZ.jar
│   ├── mods (unstable)
│   ├── preview_OptiFine_1.18.1_HD_U_H5_pre4.jar
│   ├── StructuredModLoader-1.0.0.jar
│   └── utility
│       ├── BetterF3-1.1.5+1.18.jar
│       ├── catalogue-1.6.0-1.18.jar
│       ├── cloth-config-6.0.43-forge.jar
│       ├── configured-1.5.0-1.18.jar
│       ├── Controlling-forge-1.18.1-9.0.11.jar
│       ├── journeymap-1.18-5.8.0alpha2.jar
│       ├── JustEnough
│       │   ├── jei-1.18.1-9.1.1.48.jar
│       │   ├── JustEnoughProfessions-1.18-1.2.2.jar
│       │   └── JustEnoughResources-1.18.1-0.13.1.137.jar
│       └── mods here
├── options.txt
├── pack-icon.png
└── resourcepacks

NOTE: I AM NOT AFFILIATED WITH ANY OF THE MENTIONED MODS!
Tho, they are very nice!
```
Notice the StructuredModLoader.jar is still inside the regular `\mods` folder, just like Optifine!

## Dependencies:

 - [Minecraft](minecraft.net) `1.18+`
 - [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/) `39+`

## Known incompatibilities:
 - [Optifine](optifine.net) (`FIX: place in regular \mods folder`)
