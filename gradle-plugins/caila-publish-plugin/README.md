# CAILA Publish Plugin

A Gradle plugin for packaging and publishing JAICF bots to the CAILA platform.

## Overview

This plugin automates the process of building Docker images and deploying JAICF bots to CAILA MLOps platform. It provides a declarative DSL for configuring both image build settings and model deployment parameters.

## Features

- **Two deployment workflows**: Build Docker images automatically or use pre-built images
- **Type-safe DSL** for configuration
- **Model settings**: HTTP access, resource limits, auto-scaling, caching, and more
- **Integration** with `com.bmuschko.docker-java-application` plugin

## Project Structure

```
src/main/kotlin/com/justai/jaicf/plugins/caila/publish/
├── CailaPublishPlugin.kt           # Plugin entry point
├── extension/
│   ├── CailaPublishExtension.kt    # Root DSL extension
│   ├── CailaImageSpec.kt           # Image configuration
│   └── CailaModelSpec.kt           # Model configuration
├── task/
│   ├── AbstractPublishCailaImageTask.kt    # Base class for image publishing
│   ├── PublishCailaImageFromDockerTask.kt  # Publish from Docker Extension
│   ├── PublishCailaImageFromRegistryTask.kt # Publish from registry
│   └── CailaModelTask.kt           # Model publishing task
├── internal/
│   ├── client/
│   │   └── CailaApiClient.kt       # HTTP client for CAILA API
│   └── http/
│       └── HttpClientFactory.kt    # Ktor client factory
└── model/                          # DTOs for API requests/responses
    ├── PublishImageRequestDto.kt
    ├── PublishModelRequestDto.kt
    └── ...
```

## Requirements

- Gradle 9.0+
- Kotlin 2.2.0
- Java 17+
- Docker (for automatic image builds)
- CAILA account with API access

## Installation

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("com.justai.jaicf.caila-publish-plugin") version "1.0.0"
}
```

## Configuration

### Required Properties

Provide the following Gradle properties via `~/.gradle/gradle.properties` or command-line `-P` flags:

```properties
caila.token=<your-caila-api-token>
caila.accountId=<your-account-id>
```

For Docker Hub authentication (when using automatic image builds):

```properties
dockerUsername=<your-docker-username>
dockerPassword=<your-docker-password>
dockerEmail=<your-email>
```

## Usage

### Option 1: Automatic Image Build (Docker Extension)

Configure the plugin to build and push Docker images automatically:

```kotlin
cailaPublish {
    docker {
        registryCredentials {
            url.set("https://index.docker.io/v1/")
            username.set(providers.gradleProperty("dockerUsername"))
            password.set(providers.gradleProperty("dockerPassword"))
            email.set(providers.gradleProperty("dockerEmail"))
        }
        
        javaApplication {
            mainClassName = "com.example.MainKt"
            baseImage = "eclipse-temurin:17-jre-alpine"
            images = listOf("myuser/myapp:1.0.0")
            ports = listOf(8080)
            jvmArgs = listOf("-Xms256m", "-Xmx2048m")
        }
    }
    
    image {
        name.set("my-caila-image")              // Required - image name on CAILA
        accessMode.set(AccessMode.PRIVATE.mode)
    }
    
    model {
        modelName.set("my-model")               // Required - model name on CALIA
        taskType.set(TaskType.CUSTOM.tag)
        
        httpSettings {
            httpPort.set(8080)                  // Required when httpSettings used - port that the application web server listens on
            mainPageEndpoint.set("/health")     // Required when httpSettings used - path for availability check (healthcheck endpoint)
        }
        
        publicSettings {
            isPublic.set(true)                  // Enable public HTTP access
        }
    }
}
```

**Publish commands:**

```bash
# Publish both image and model
./gradlew publishToCailaFromDocker

# Or step by step
./gradlew publishCailaImageFromDocker
./gradlew publishCailaModelFromDocker
```

### Option 2: Pre-built Image (Manual Registry)

Use an existing Docker image from a registry:

```kotlin
cailaPublish {
    image {
        image.set("registry.example.com/myapp:1.0.0")  // Required
        name.set("my-caila-image")                     // Required
        accessMode.set(AccessMode.PRIVATE.mode)
    }
    
    model {
        modelName.set("my-model")
        
        httpSettings {
            httpPort.set(8080)
            mainPageEndpoint.set("/api/predict")
        }
    }
}
```

**Publish commands:**

```bash
# Publish both image and model
./gradlew publishToCailaFromRegistry

# Or step by step
./gradlew publishCailaImageFromRegistry
./gradlew publishCailaModelFromRegistry
```

## Configuration Reference

### Image Configuration

```kotlin
image {
    image.set("...")              // Docker image path (required for manual registry workflow)
    name.set("...")               // CAILA image name (required)
    accessMode.set("...")         // Access mode: PUBLIC or PRIVATE
    allowDestructiveUpdate.set(true)  // Allow overwriting existing images
}
```

### Model Configuration

#### Basic Settings

```kotlin
model {
    modelName.set("...")          // Required
    taskType.set("...")           // Task type for the model. Choose 'CUSTOM' if other options don't fit. See: https://docs.caila.io/api/task-types
    displayName.set("...")
    displayAuthor.set("...")
    shortDescription.set("...")
    minInstancesCount.set(1)
    startTimeSec.set(30.0)
    rejectRequestsIfInactive.set(true)  // Reject requests if service is inactive. If no active instances exist and this option is enabled, the request will return an error. If disabled, the request will wait until an instance starts or timeout occurs.
    fittable.set(false)           // Whether the service requires training on user data
    resourceGroup.set("...")      // Resource group - a set of servers where service instances are launched
    languages.set(listOf("ru", "kk"))  // Languages for display in the catalog. Specify each language separately
}
```

#### HTTP Settings

```kotlin
httpSettings {
    isHttpEnabled.set(true)       // Default: true
    httpPort.set(8080)            // Required. Port that the application web server listens on
    mainPageEndpoint.set("/")     // Required. Path for availability check (healthcheck endpoint)
    httpInterfaceOnly.set(true)   // Default: true. Enable if the service does not support gRPC API. Disable if the service is developed based on MLP SDK
}
```

#### Public Access Settings

```kotlin
publicSettings {
    isPublic.set(true)            // Default: false
    featured.set(false)
    featuredListOrder.set(0)
    hidden.set(false)
    publicTestingAllowed.set(false)
    showPersonalDataDisclaimer.set(false)
}
```

#### Resource Limits

Resources allocated to one service instance within a resource group.

```kotlin
resourceLimits {
    cpuRequest.set("1000m")       // CPU millicores guaranteed to the instance (can consume more if available)
    memoryLimit.set("2Gi")        // Amount of RAM available to the instance. If the instance exceeds the limit, it will be stopped
    ephemeralDiskLimit.set("10Gi")  // Amount of disk memory available to the instance. If the instance exceeds the limit, it will be stopped
    gpuRequested.set(false)       // Whether GPU is requested. Multiple instances can share one GPU
}
```

#### Timeouts

```kotlin
timeouts {
    podStartTimeoutSec.set(120)   // Default: 120. Timeout for starting a service instance, in seconds
    predictTimeoutSec.set(30)     // Default: 30. Prediction timeout, in seconds
    fitTimeoutSec.set(600)        // Default: 600. Fit timeout, in seconds
}
```

#### Auto-scaling

```kotlin
autoScalingConfiguration {
    enabled.set(true)
    minInstanceCount.set(1)
    maxInstanceCount.set(10)
    cooldownDurationMinutes.set(5)
    scaleUpRequestsPerMinuteThreshold.set(100)
    scaleDownRequestsPerMinuteThreshold.set(10)
}
```

#### Retries Config

Retry the request transparently to the user if the service returns an error or times out.

```kotlin
retriesConfig {
    maxRetries.set(3)             // Maximum number of retry attempts
    timeoutsMs.set(listOf(1000L, 2000L, 4000L))  // Timeouts in milliseconds. If a response is not yet received, the platform sends repeated requests to the service after the specified intervals from the first request
    maxRetriesPerInstance.set(1)  // Maximum number of retries per instance. Recommended value: 1
}
```

#### Batches Config

Combine requests into batches to optimize resource usage. The platform accumulates requests during the wait time or until the batch size is reached, then forms a batch and sends it to the service for processing. Only for services that provide the predictBatch method.

```kotlin
batchesConfig {
    batchSize.set(10)              // Maximum number of requests in a batch. 0 or 1 means batching is disabled
    timeWaitMs.set(1000)           // Maximum wait time for batch accumulation, in milliseconds
    maxLengthToSkip.set(1000000)   // Maximum request size limit for adding to batch, in bytes. Requests that are too large will be immediately sent for processing
}
```

#### Caching

Save service responses in cache for reuse.

```kotlin
caching {
    enabled.set(true)
    mongoUri.set("mongodb://...")  // URL for connecting to the MongoDB database where the cache will be stored. Create a database in any cloud service or contact support to create a database in Caila
    collectionName.set("cache")    // Collection name
    recordsLimit.set(10000)        // Records limit
}
```

#### Priority Queue

Process high-priority requests first. Request priority is passed in the Z-Priority header.

```kotlin
priorityQueue {
    enabled.set(true)
    concurrencyLevel.set(10)       // How many requests can be sent in parallel to one service instance. The platform manages request ordering only before sending them
}
```

#### Archive Settings

Store request and response history (accessible via API).

```kotlin
archiveSettings {
    enabled.set(true)
    numberOfArchivedRequests.set(1000)  // Number of stored requests
    encryptionEnabled.set(false)        // Whether to use encryption
    rsaPemPublicKey.set("...")          // Encryption key (RSA PEM public key)
}
```

## Available Tasks

### Docker Extension Workflow

| Task                          | Description                                          |
|-------------------------------|------------------------------------------------------|
| `publishCailaImageFromDocker` | Build and publish Docker image from Docker Extension |
| `publishCailaModelFromDocker` | Publish model using image from Docker Extension      |
| `publishToCailaFromDocker`    | Execute both tasks in sequence                       |

### Manual Registry Workflow

| Task                            | Description                             |
|---------------------------------|-----------------------------------------|
| `publishCailaImageFromRegistry` | Publish pre-built Docker image to CAILA |
| `publishCailaModelFromRegistry` | Publish model using pre-built image     |
| `publishToCailaFromRegistry`    | Execute both tasks in sequence          |
