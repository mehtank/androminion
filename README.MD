# Androminon: Unofficial Dominion for Android

[All releases](https://github.com/mehtank/androminion/releases)

This is neither supported nor endorsed by Rio Grande Games.

### How to compile

(Coming soon)

### Steps to run a multiplayer game

**You need the source code to compile the server vdom!**

In the vdom directory, type 'ant run' (or just start com.vdom.core.Game with whatever arguments you want - the build file build.xml has some default arguments).  Be sure you know the host name or ip address for the machine that's running the game server, and that the player ports (by default 2255 and 2256) aren't blocked by your machine's firewall.

Open the app on two separate devices.  Use the "join game" dialog to enter your name, the host name or ip address of the machine running the server, then the port that shows up in the terminal where you're running the server.  Once the first player connects, a second port will show up; enter that one in on the second device (should be 2255 and 2256). The interface for the first player that connects will be missing a lot of stuff until the second player connects, but once both players are there, things work fine.

### On network permissions and usage

This app functions by using a strictly local networking layer to talk between the player and the game -- currently, unless using multiplayer, **no data is sent in or out of the phone**.  Nonetheless, this local network still requires networking permission, and such internal data transfer may show up as total app usage.
