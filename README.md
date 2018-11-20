# Skript [![Build Status](https://travis-ci.org/SkriptLang/Skript.svg?branch=master)](https://travis-ci.org/SkriptLang/Skript)
Skript is a plugin for Bukkit/Spigot, which allows server owners and other people
to modify their servers without learning Java. It can also be useful if you
*do* know Java; some tasks are quicker to do with Skript, and so it can be used
for prototyping etc.

This Github fork of Skript is based on Mirreski's improvements which was built
on Njol's original Skript. It is supported for **Spigot** (not Bukkit) versions of
Minecraft 1.9-1.13. Other versions might work, but no guarantees. **Paper** is
recommended, but not mandatory - without it, some features such as timings
will be not available.

## Documentation
Documentation is available [here](https://skriptlang.github.io/Skript/) for the
latest version of Skript.

## Reporting Issues
Please see our [contribution guidelines](https://github.com/SkriptLang/Skript/blob/master/.github/CONTRIBUTING.md)
before reporting issues.

## A Note About Addons
We don't support addons here, even though some of Skript developers have also
developed their own addons.

## Compiling
Skript uses Gradle for compilation. Use your command prompt of preference and
navigate to Skript's source directory. Then you can just call Gradle to compile
and package Skript for you:

```bash
./gradlew clean build # on UNIX-based systems (mac, linux)
gradlew clean build # on Windows
```

You can get source code from the [releases](https://github.com/SkriptLang/Skript/releases) page.
You may also clone this repository, but that code may or may not be stable.

### Importing to Eclipse
With new Eclipse versions, there is integrated Gradle support, and it actually works now.
So, first get latest Eclipse, then import Skript as any Gradle project. Just
make sure to **keep** the configuration when the importer asks for that!

If you encounter strange issues, make sure you follow the instructions above and have
actually downloaded latest Eclipse or update your installation correctly. Skript's
new Gradle version (starting from dev26) does not work very well with older Eclipse
versions. Also, do *not* use Gradle STS; it is outdated.

### Importing to IDEA
You'll need to make sure that nullness annotations are working correctly. Also,
when sending pull requests, make sure not to change IDEA configuration files
that may have been stored in the repository.

## Contributing
Please review our [contribution guidelines](https://github.com/SkriptLang/Skript/blob/master/.github/CONTRIBUTING.md).
In addition to that, if you are contributing Java code, check our
[coding conventions](https://github.com/SkriptLang/Skript/blob/master/CODING_CONVENTIONS.md).

## Maven repository
If you use Skript as (soft) dependency for your plugin, and use maven or Gradle,
this is for you.

First, you need to add the JitPack repository at the **END** of all your repositories. Skript is not available in Maven Central.
```gradle
repositories {
    jcenter()
    ...
    maven { 
        url 'https://jitpack.io' 
    }
}
```

Or, if you use Maven:
```maven
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

For versions of Skript after dev37 you might need to add the paper-api repository to prevent build issues.

```gradle
maven { 
    url 'https://repo.destroystokyo.com/repository/maven-public/' 
}
```

Or, if you use Maven:
```maven
<repository>
    <id>destroystokyo-repo</id>
    <url>https://repo.destroystokyo.com/content/repositories/snapshots/</url>
</repository>
```

Then you will also need to add Skript as a dependency.
```gradle
dependencies {
    implementation 'com.github.SkriptLang:Skript:[versionTag]'
}
```

An example of the version tag would be ```dev37c```.

> Note: If Gradle isn't able to resolve Skript's dependencies, just [disable the resolution of transitive dependencies](https://docs.gradle.org/current/userguide/managing_transitive_dependencies.html#sub:disabling_resolution_transitive_dependencies) for Skript in your project.

Or, if you use Maven:
```
<dependency>
    <groupId>com.github.SkriptLang</groupId>
    <artifactId>Skript</artifactId>
    <version>[versionTag]</version>
</dependency>
```

## Relevant Links
* [SkUnity Forums](https://forums.skunity.com/)
* [Original Skript at BukkitDev](https://dev.bukkit.org/bukkit-plugins/skript/) (inactive)
* [Addon Releases @SkUnity](https://forums.skunity.com/forums/addon-releases/)
* [Skript Chat Discord Invite](https://discord.gg/0lx4QhQvwelCZbEX)
* [Skript Hub](https://skripthub.net/)

Note that these resources are not maintained by me. If you notice something wrong with them, do not contact me.

## Developers
Current team behind Skript:

* [bensku](https://github.com/bensku) (Skript maintainer/developer)
* [Snow-Pyon](https://github.com/Snow-Pyon) (Skript developer)
* [Pikachu920](https://github.com/Pikachu920) (Skript developer)
* [Nicofisi](https://github.com/Nicofisi) (Skript developer)
* [TheBentoBox](https://github.com/TheBentoBox) (issue tracker manager, aliases developer)

Also, of course, we should thank [Njol](https://github.com/Njol) for creating
Skript and [Mirreski](https://github.com/Mirreski) for maintaining it for a
long time.

And of course, Skript has received lots of pull requests over time.
You can find all contributors [here](https://github.com/SkriptLang/Skript/graphs/contributors).

All code is owned by it's writer, licensed for others under GPLv3 (see LICENSE)
unless otherwise specified.
