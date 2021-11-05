# HydroEnergy

[![](https://jitpack.io/v/SinTh0r4s/HydroEnergy.svg)](https://jitpack.io/#SinTh0r4s/HydroEnergy)
[![](https://github.com/SinTh0r4s/HydroEnergy/actions/workflows/gradle.yml/badge.svg)](https://github.com/SinTh0r4s/HydroEnergy/actions/workflows/gradle.yml)

### For Minecraft 1.7.10

This mod provides an immersive alternative energy storage to battery buffers. You can flood vast areas and use them for energy storage. The size of artificial lakes is technically not limited! You may charge its energy capacity with a pump and/or draw on it with a turbine. And maybe, some rain will give you a bonus every now and then.

Your artificial lake will appear correctly as you drain or fill it and entities can interact with it in the same way.

Why not flood some ruins for a nice visual when you are low on energy? [Click here](https://www.youtube.com/watch?v=0zPsRyaXN_w) to see what i mean! \
[![HydroEnergy Demo Video](https://i.ibb.co/pZJgFFP/fake-video2.png)](https://www.youtube.com/watch?v=0zPsRyaXN_w)

This mod is tailored to GregTech: New Horizons 2, but feel free to use it however you like. Even though this mod is build against the custom GT5U from GT:NH, it should still work fine with other GT5U versions.

### Features
- Flood landscapes for massive EU storage
- MyTown2 support for server deployment and grief protection
- Gregified
- Minimal server load per dam: comparable to a battery buffer!
- Upgradable pumps and turbines!
- Visual debug mode to show the area of the dam and allow you easy work on "leaks"/terraforming in survival mode

### How-To

The mod-pack GregTech: New Horizons 2 contains explanatory quests for this mod in MV-age.

### Dependencies

#### Required Mods:

 - Minecraft Forge
    - Injected classes: [_ActiveRenderInfo_](https://github.com/SinTh0r4s/HydroEnergy/blob/master/src/main/java/com/sinthoras/hydroenergy/mixins/minecraft/ActiveRenderInfoMixin.java) ,[_Chunk_](https://github.com/SinTh0r4s/HydroEnergy/blob/master/src/main/java/com/sinthoras/hydroenergy/mixins/minecraft/ChunkMixin.java), [_ChunkProviderClient_](https://github.com/SinTh0r4s/HydroEnergy/blob/master/src/main/java/com/sinthoras/hydroenergy/mixins/minecraft/ChunkProviderClientMixin.java), [_Entity_](https://github.com/SinTh0r4s/HydroEnergy/blob/master/src/main/java/com/sinthoras/hydroenergy/mixins/minecraft/EntityMixin.java), [_EntityRenderer_](https://github.com/SinTh0r4s/HydroEnergy/blob/master/src/main/java/com/sinthoras/hydroenergy/mixins/minecraft/EntityRendererMixin.java), [_World_](https://github.com/SinTh0r4s/HydroEnergy/blob/master/src/main/java/com/sinthoras/hydroenergy/mixins/minecraft/WorldMixin.java), [_WorldRenderer_](https://github.com/SinTh0r4s/HydroEnergy/blob/master/src/main/java/com/sinthoras/hydroenergy/mixins/minecraft/WorldRendererMixin.java)
 - [GregTech5-Unofficial](https://github.com/GTNewHorizons/GT5-Unofficial)
    - Injected class: [_GT_PollutionRenderer_](https://github.com/SinTh0r4s/HydroEnergy/blob/master/src/main/java/com/sinthoras/hydroenergy/mixins/gregtech/GT_PollutionRendererMixin.java)
 - [SpongeMixins](https://github.com/GTNewHorizons/SpongeMixins)

### Known issues
 - Apple dropped some OpenGL support. Rendering is not quite possible on new Mac's. There is a backup in place, but you will have to deal with things looking not quite right.
 - Shaders won't look right, but it will not crash the game.
 - A [list of tolerated, minor bugs](https://github.com/SinTh0r4s/HydroEnergy/issues/16).
