---
layout: default
title: Environments
nav_order: 9
permalink: Environments
has_children: true
---

Once you've implemented your JAICF-based conversational agent, you have to deploy it somewhere to process users' requests.
Also you may need to connect it to any database that persists the state of dialogue for each user.

JAICF provides libraries and extensions that simplifies this process enabling you to use any supported environment for your project.

# Servers

Here is a set of containers that can be used to serve your JAICF agents.
Please learn how to use each of them by the manuals listed below.

* [Ktor](Ktor)
* [Spring Boot](Spring-Boot)

# Clouds

Once you're ready to go in production, you have to host your JAICF project anywhere in the cloud.
Being the Kotlin application JAICF project can be ran on every Java-compatible cloud environment.
Here we list some ready to use environments to speed-up your deployment.

* [Heroku](Heroku)
* [AWS Lambda](AWS-Lambda)

# Databases

Here is a persistence implementations that can be used to transparently store and fetch the current dialogue state and associated user's data.

> Learn more about context [here](context).

* [MongoDB](Mongo-DB)
* [MapDB](Map-DB)

# Others

* [Docker](Docker)
* [Android](Android)