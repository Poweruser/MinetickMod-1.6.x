MinetickMod
===========

This is my mod of the popular [Craftbukkit Minecraft server](https://github.com/Bukkit/CraftBukkit).
Its main purpose is to increase the server's processing speed, by introducing multithreading and other optimizations. And while I'm at it, fixing bugs that I found. 

Developement started privately back in April of 2013. Since May 1st (Minecraft version 1.5.1 at that time) this mod is powering the server [Minetick](http://www.minetick.de) every day.

The main achievements so far:

- Prioritized parallel processing of the entities of different worlds
- Queued, limited and parallel chunk generation in different worlds
- Main thread independent creation, orebfuscation and compression of chunk packets

Parts of the changes are pretty well tested already (like parallel ticking of entities), other parts that I added recently (like offloaded packet creation) still require lots of testing. 

Here is a (very abstract) graphical overview on what changed:

![](https://raw.github.com/wiki/Poweruser/MinetickMod/MinetickMod_Dez2013_1stRelease_EN.png)


As you probably notice: I'm using 4 worlds in this example here and by default a server only has 3 worlds (Overworld, nether and the end). This is a hint to you, that your gain from this mod is greater the more you spread the players on your server, and therefore the load on the worlds, equally among several worlds.
To load up additional worlds, I can recommend the plugin [Multiverse](http://dev.bukkit.org/bukkit-plugins/multiverse-core/)

An important guideline in this project is to do as little changes as possible, but as much as necessary.
Therefore this project is not, and will not be, a complete rewrite of Minecrafts server side, to get as much benefit out of parallel processing as possible. Instead I carefully examine and profile the various parts of the server code and decide then, what is worth to be optimized with a reasonable amount of work.

In this repository, every Minecraft version of this mod will have its own branch. I probably won't do them all, just for the most important ones.
