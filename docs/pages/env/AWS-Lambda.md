---
layout: default
title: AWS Lambda
permalink: AWS-Lambda
parent: Environments
---

![AWS Lambda](/assets/images/env/aws-lambda.png)

[AWS Lambda](https://developer.amazon.com/en-US/docs/alexa/custom-skills/host-a-custom-skill-as-an-aws-lambda-function.html) enables developers to host their Alexa voice skills on the Amazon's serverless cloud.

> For most developers, the [Lambda free tier](http://aws.amazon.com/lambda/pricing/) is sufficient for the function supporting an Alexa skill. The first one million requests each month are free. Note that the Lambda free tier does not automatically expire, but is available indefinitely.

JAICF provides a built-in support of AWS Lambda for every [Alexa skill powered with JAICF](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/alexa).

Also it's possible to use an AWS built-in [DynamoDB](https://aws.amazon.com/dynamodb) database to transparently persist the users' data using [context](context) _client_ map.

# How to use

## 1. Create your Alexa skill
[Learn](Alexa) how to create the Alexa skill using JAICF.

## 2. Define Lambda handler
Every AWS Lambda has to have _handler_ - a special function that should be invoked each time the user interacts with your Alexa skill.
To make a handler from your JAICF scenario just create a new public class:

```kotlin
class AWSLambda: AlexaLambdaSkill(botApi = gameClockBot, dynamoDBTableName = "GameClock")
```

> Make sure you've provided _dynamoDBTableName_ in the case your bot persists some data across different user's sessions.

## 3. Append shadowJar
Build your project using shadowJar plugin to include all libraries inside the resulting JAR file.
Append this to your _build.gradle_:

```kotlin
plugins {
    kotlin("jvm") version "1.3.71"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    build {
        dependsOn(shadowJar)
    }
}
```

## 4. Create new DynamoDB table
If your JAICF project persists some data [between different user's sessions](https://developer.amazon.com/en-US/docs/alexa/custom-skills/manage-skill-session-and-session-attributes.html#save-data-between-sessions) you have to create new DynamoDB table.

- Create new [DynamoDB table](https://console.aws.amazon.com/dynamodb/) with the same name you've defined on the step 2
- Type "id" in the _Primary key_

## 5. Create new AWS Lambda

- Create new [AWS Lambda](https://console.aws.amazon.com/lambda/home)
- Select _Java 11_ in runtime settings
- Add DynamoDB full access policies to the _Execution role_ on _Permissions_ tab if your project uses DynamoDB
- Append _Alexa Skills kit_ trigger to the created lambda with Alexa skill ID you've created on the step 1
- Build and upload your JAICF project's JAR file in the _Function code_ section
- Define a full path to the handler in the _Runtime settings_, for example `com.justai.jaicf.examples.gameclock.channel.AWSLambda::handleRequest`
- Copy _ARN_ from the top right corner and paste it in the _[Alexa Console](https://developer.amazon.com/alexa/console/ask)_ > _Endpoint_ > _AWS Lambda ARN_

