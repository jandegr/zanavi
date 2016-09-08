ZANavi
======

ZANavi is a fork of NavIT. It is for the Android platfrom only!
for more details look at our wiki

http://zanavi.cc

Code Style
==========
https://github.com/zoff99/Code-Style-Guidelines/blob/master/Android/Java.md

CircleCI
========

[![Circle CI](https://circleci.com/gh/zoff99/zanavi/tree/master.svg?style=svg)](https://circleci.com/gh/zoff99/zanavi/tree/master)


tagsoup-1.2.1.jar:
==================

http://home.ccil.org/~cowan/tagsoup/

downloaded from: http://home.ccil.org/~cowan/tagsoup/tagsoup-1.2.1.jar



====================== original README below ==================
====================== original README below ==================
====================== original README below ==================



NavIT
=====

Navit is a open source (GPL) car navigation system with routing engine.

It's modular design is capable of using vector maps of various formats
for routing and rendering of the displayed map. It's even possible to
use multiple maps at a time.

The GTK+ or SDL user interfaces are designed to work well with touch
screen displays. Points of Interest of various formats are displayed
on the map.

The current vehicle position is either read from gpsd or directly from
NMEA GPS sensors.

The routing engine not only calculates an optimal route to your
destination, but also generates directions and even speaks to you.

Navit currently speaks 27 languages :
- Brazilian Portuguese
- Bulgarian
- Chinese (Hong Kong)
- Czech
- Danish
- Dutch
- English
- Estonian
- Finnish
- French
- German
- Hebrew
- Hungarian
- Italian
- Japanese
- Norwegian Bokmal
- Polish
- Portuguese
- Romanian
- Russian
- Slovak
- Slovenian
- Spanish
- Swedish
- Telugu
- Thai
- Turkish

You can help translating via our web based translation page :
 http://translations.launchpad.net/navit/trunk/+pots/navit


For help or more information, please refer to the wiki :
 http://wiki.navit-project.org

If you don't know where to start, we recommend you to read the 
Interactive Help : http://wiki.navit-project.org/index.php/Interactive_help


Maps:
=====

The best navigation system is useless without maps. Those three maps
are known to work:

- OpenStreetMaps : display, routing, but street name search isn't complete
 (see http://wiki.navit-project.org/index.php/OpenStreetMaps )

- Grosser Reiseplaner and compliant maps : full support
 (see http://wiki.navit-project.org/index.php/European_maps )

- Garmin maps : display, routing, search is being worked on
 (see http://wiki.navit-project.org/index.php/Garmin_maps )


GPS Support:
============

Navit read the current vehicle position :
- directly from a file
- from gpsd (local or remote)
- from udp server (friends tracking) (experimental)


Routing algorithm
=================

NavIt uses a Dijkstra algorithm for routing. The routing starts at the
destination by assigning a value to each point directly connected to
destination point. The value represents the estimated time needed to
pass this distance.

Now the point with the lowest value is choosen using the Fibonacci
heap and a value is assigned to connected points whos are
unevaluated or whos current value ist greater than the new one.

The search is repeated until the origin is found.

Once the origin is reached, all that needs to be done is to follow the
points with the lowest values to the destination.


