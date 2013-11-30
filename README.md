MinetickMod
===========

This is my mod of the popular Craftbukkit Minecraft server.
Its main purpose is to increase the server's processing speed, by introducing multithreading and other optimizations. And while I'm at it, fixing bugs that I found. 

The main achievements so far:

- Parallel processing of the entities of different worlds
- Queued, limited and parallel chunk generation in different worlds

An important guideline in this project is to do as little changes as possible, but as much as necessary.
Therefore this project is not, and will not be, a complete rewrite of Minecrafts server side, to get as much benefit out of parallel processing as possible. Instead I carefully examine and profile the various parts of the server code and decide then, what is worth to be optimized with a reasonable amount of work.

In this repository, every version will have its own branch. I probably won't do a Mod for every Minecraft version, just for the most important ones.
