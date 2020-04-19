![Logo](http://i.imgur.com/mqn4TON.png)

[![MCVersion](https://img.shields.io/badge/Minecraft%20version-1.8.8%20%26%201.12.1--1-orange.svg)](https://minecraft.net)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/mit-license.html)
[![Modules](https://img.shields.io/badge/Modules-11-yellow)](https://github.com/xTACTIXzZ/Hydrazine/wiki/Module-list)
[![Status](https://img.shields.io/badge/Status-Beta-red.svg)](http://i.investopedia.com/dimages/graphics/beta03.png)
[![ProgVer](https://img.shields.io/badge/Program%20version-1.1-blue.svg)](https://github.com/xTACTIXzZ/Hydrazine)



Hydrazine is a command-line based, modular program that uses MCProtocolLib by Steveice10 ([link](https://github.com/Steveice10/MCProtocolLib)) to interact with a minecraft server. Its purpose is to check if the target is able to withstand a bot-based attack. Currently, the program supports *Minecraft 1.8.8 and 1.12.1-1*.


## Features
- Cracked and Premium Server support
- Authentication proxy support
- Client proxy support (socks5 only)
### Modules
* **info** - *Retrieves information about a minecraft server.*
* **icongrab** - *This module grabs the icon from a server and saves it to your computer.*
* **readchat** - *This module connects to a server and passively reads the chat.*
* **chat** - *Lets you chat on a server.*
* **cflood** - *Floods a cracked server with bots.*
* **pflood** - *Floods a premium server with bots.*
* **cclient** - *This module lets you send and receive chat messages.*
* **status** - *Retrieves the status of all minecraft related services*
* **uuid** - *Returns the UUID(s) of a given player name or list.*
* **altchecker** - *Cycles through a list of accounts to check if they are able to log in. (Format: username/email:password)*
* **skinstealer** - *Steals the skin of a player and saves it to your computer.*

## How to use Hydrazine
[Tutorial](https://github.com/xTACTIXzZ/Hydrazine/wiki/How-to-use-Hydrazine)

## Modules
### What are modules
Modules are essential for Hydrazine to work. They add all of the functionality to the program and without them, Hydrazine would not be able to do anything.

#### Built-in modules
Hydrazine has some built-in modules that you can execute right from the beginning. To see a list of available modules, start Hydrazine with the '-l' switch. If you want to run a module from that list, you have to start Hydrazine with the '-h' and '-m' switch. '-h' needs to be followed by the target hostname or ip address and '-m' needs to be followed by the module name.

*Example:* ```java -jar Hydrazine.jar -h 127.0.0.1 -m info```
#### External modules
Hydrazine has the capability to execute external modules by simply running the program with the '-m' switch but this time it is followed by the absolute file path of the module.

*Example:* ```java -jar Hydrazine.jar -h localhost -m /home/user/Desktop/module.jar```

### Environment variable
If you have a folder that contains some external modules, you can set up an environment variable called "**HYDRAZINE**" (has to be uppercase, without quotation marks) with the value being the file path to that folder (e.g. /home/user/modules/) in order to simplify the process of starting external modules. Now, if you'd like to start a module from that folder, you can simply type it's name instead of the full file path to start it.

*Example:*

**This:**

```java -jar Hydrazine.jar -h 127.0.0.1 -m /home/user/modules/module.jar```

**Becomes this:**

```java -jar Hydrazine.jar -h 127.0.0.1 -m module```

### How to write your own modules
// todo

## Credits :zzz:

* Steveice10 for his awesome library [MCProtocolLib](https://github.com/Steveice10/MCProtocolLib)
* [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/)
