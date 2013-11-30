MinetickMod
===========

This is my mod of the popular [Craftbukkit Minecraft server](https://github.com/Bukkit/CraftBukkit).
Its main purpose is to increase the server's processing speed, by introducing multithreading and other optimizations. And while I'm at it, fixing bugs that I found. 

Developement started privately back in April of 2013. Since May 1st (Minecraft version 1.5.1 at that time) this mod is powering the server [Minetick](www.minetick.de) every day. Which means that this mod is pretty well tested already.

The main achievements so far:

- Parallel processing of the entities of different worlds
- Queued, limited and parallel chunk generation in different worlds

Here is a graphical overview on what changed:




An important guideline in this project is to do as little changes as possible, but as much as necessary.
Therefore this project is not, and will not be, a complete rewrite of Minecrafts server side, to get as much benefit out of parallel processing as possible. Instead I carefully examine and profile the various parts of the server code and decide then, what is worth to be optimized with a reasonable amount of work.

In this repository, every version will have its own branch. I probably won't do a Mod for every Minecraft version, just for the most important ones.
