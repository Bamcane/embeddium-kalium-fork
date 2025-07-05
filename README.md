<img src="src/main/resources/icon.png" width="128">

# Kalium
Kalium is an unofficial fork of Embeddium in order to support new versions.

Kalium is fully  basd on [Celeritas](https://git.taumc.org/embeddedt/celeritas), which is a fork of Embeddium and Iris by embeddedt.

What I did is just porting the code to 1.21.10 instead of 1.21.1, and planning to release the binary on the Modrinth.

As I am a C developer, I'm bad at Java developing, so I use AI-tools to help me solve some problems. :3

I hopes anyone could create a fork of Kalium. Then I can leave the main position of maintaining Kalium. :3

This project is **not supported by embeddedt**, Do not report issues encountered with it to their gitea or in their Discord.
- please open an issue on our [Issues page](https://github.com/Bamcane/embeddium-kalium-fork/issues)

# About Embeddium

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/sk9rgfiA?label=Modrinth&labelColor=%232D2D2D)](https://modrinth.com/mod/embeddium)
[![Modrinth Version](https://img.shields.io/modrinth/v/sk9rgfiA?label=Latest%20version&labelColor=%232D2D2D)](https://modrinth.com/mod/embeddium/versions)
[![](http://cf.way2muchnoise.eu/short_embeddium_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/embeddium)
[![](http://cf.way2muchnoise.eu/versions/Available%20for_embeddium_full.svg)](https://www.curseforge.com/minecraft/mc-mods/embeddium/files)

Embeddium is a free and open-source performance mod for Minecraft clients. It is based on the last FOSS-licensed version of the Sodium codebase, and includes additional bugfixes & features for better mod compatibility.

## Highlights

* All performance improvements from Sodium 0.5.8 and earlier, i.e. a rewritten terrain renderer, various optimizations to the immediate-mode rendering pipeline (used by entities, GUIs, block entities, etc.), and other miscellaneous improvements
* Available for Minecraft Forge on 1.20.1 and older, and Fabric/NeoForge on 1.20.1 and newer
* Integrated Fabric Rendering API support (Indium is no longer required, and will not work with Embeddium)
* Frequent patch updates to fix mod compatibility issues soon after being reported & reproduced
* Additional APIs for mod integration
* Optional support for translucency sorting (can be enabled in Video Settings)

## For developers

If you're looking to add Embeddium to your development environment, please take a look at the [dedicated wiki page](https://github.com/FiniteReality/embeddium/wiki/For-Developers) for instructions & recommended guidelines for integration.

## Credits

* JellySquid & the CaffeineMC team, for making Sodium in the first place, without which this project would not be possible
* Asek3, for the initial port to Forge
* XFactHD, for providing a list of gamebreaking Rubidium issues to start this work off, and for testing early builds
* Pepper, for their invaluable assistance with getting Forge lighting to work on Sodium 0.5
* @CelestialAbyss and @input-Here for making the new logo design

## License

Embeddium is licensed under the Lesser GNU General Public License version 3.

Portions of the option screen code are based on Reese's Sodium Options by FlashyReese, and are used under the terms of
the [MIT license](https://opensource.org/license/mit), located in `src/main/resources/licenses/rso.txt`. 
