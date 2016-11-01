# Skript
Skript is plugin for Bukkit/Spigot, which allows server owners and other people
to modify their servers without learning Java. It can also be useful if you
*do* know Java; some tasks are quicker to do with Skript, and so it can be used
for prototyping etc.

This Github fork fork of Skript is based on Mirreski's improvements on original
Njol's Skript. It is supported for **Spigot** (not Bukkit) versions for
Minecraft 1.9-1.10. Other versions might work, but no guarantees.

## Documentation
Documentation is available [here](http://bensku.github.io/Skript/) for latest
version of Skript.

## Reporting Issues
You should use Github [issue tracker](https://github.com/bensku/Skript/issues)
for all bug reports, feature requests and such. If you are not sure if something
is a bug, please still report it.

Please use search to see if issue has been reported already. Duplicates will be
closed and ignored. If the issue is indeed not yet reported, please really
use common sense: what might Skript developer need to know to solve your issue?

If issue has been reported before, and is open, you can comment to it to ask if there
has been progress or provide more information. If it has been closed, you can do
same thing, but then it might be good idea to ping `@bensku` since closed issues
are not often checked.

Finally, there is no guarantee that issue will be resolved. Sometimes it might be
harder than it sounds to you; in other cases, no one has time to take look at it.

If your having a aliases issue please report that
[here](https://github.com/tim740/skAliases/issues) instead.

## Compiling
Skript uses Gradle for compilation. Use your command prompt of preference and
navigate to Skript's source directory. Then you can just call Gradle to compile
and package Skript for you:
```
./gradlew clean build
```
On Windows environment, replace `./gradlew` with `gradlew.bat`.

You can get source code from the releases page. You may also clone this
repository, but that code might or might not be stable.

### Importing to Eclipse
With new Eclipse versions, there is integrated Gradle support. And it actually works now.
So, first get latest Eclipse, then import Skript as any Gradle project. Just
make sure to **keep** the configuration when importer asks that!

Gradle STS can also be used, but it is not recommended anymore. It is being
phased out in favor of Eclipse's integrated Gradle support.

### Importing to IDEA
Skript IDEA support is not reliable, and even more less so after switching from Maven to Gradle.
If you have idea how to fix it, please open issue or pull request.

## Contributing
So, you want to contribute to Skript? You need to:
* Have some skills in Java (unless said otherwise)
* Use Eclipse as your IDE for Skript *or* get your own IDE to support Eclipse's nullness annotations
* Not alter Eclipse nullness annotation settings; they affect even the compiler
* Use tabs as indentation; Eclipse does this for you
* Write code that follows conventions in other files of Skript's source
* **DO NOT** use NMS code

Tl;dr; follow these directions unless you have good reason not to.

### What to Contribute?
You can find issues tagged with "help wanted" on tracker to see if there is
something for you. If you want to take over one of these, just leave a comment
so other contributors don't accidentally pick same issue to work on. You can also
offer your help to any other issue, but for "help wanted" tasks, help is really
*needed*.

You don't have to pick an issue to work on. If you have good idea, you can first
create an issue to ask what other people think about it. After that, you can
write code for it, test it, and finally submit a pull request.

### I'm not Java expert, can I still help?
You do need to know something of Java programming to be much help, but you do
not need to be an expert. If you can write code that works and doesn't contain
too much [spaghetti](https://en.wikipedia.org/wiki/Spaghetti_code), you're
good to go!

## Relevant Links
* [SkUnity Forums](https://forums.skunity.com/)
* [Original Skript at BukkitDev](https://dev.bukkit.org/bukkit-plugins/skript/)
* [Addon Forums @SkUnity](https://forums.skunity.com/c/addons/none)
* [Skript Chat Discord invite](https://discord.gg/0lx4QhQvwelCZbEX)

Note that these resources are not maintained by me. If you notice something wrong in them, do not contact me.
