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

### QuickStart - Minimal Configuration

The minimal configuration you need to get started:

```kotlin
cailaPublish {
    docker {
        // REQUIRED: Java application settings
        javaApplication {
            mainClassName = "com.example.MainKt"
            images = listOf("myuser/myapp:1.0.0")
        }
    }

    image {
        name = "my-caila-image"
    }

    model {
        name = "my-model"

        http {
            port = 8080
        }
    }
}
```

**That's it!** All other settings have sensible defaults.

**Default values:**
- `registryCredentials.url`: `https://index.docker.io/v1/` (Docker Hub) with credentials from `gradle.properties`
- `baseImage`: `eclipse-temurin:17-jre-alpine` (Java 17 JRE on Alpine Linux)

### Option 1: Automatic Image Build (Docker Extension)

Full example with optional settings:

```kotlin
cailaPublish {
    docker {
        // OPTIONAL: Docker registry credentials
        // If not specified, defaults to Docker Hub with credentials from gradle.properties
        registryCredentials {
            url.set("https://docker-hub.just-ai.com")  // Custom registry URL
            username.set(providers.gradleProperty("dockerUsername"))
            password.set(providers.gradleProperty("dockerPassword"))
            email.set(providers.gradleProperty("dockerEmail"))  // Optional
        }

        // REQUIRED: Java application settings
        javaApplication {
            mainClassName = "com.example.MainKt"
            baseImage = "eclipse-temurin:17-jre-alpine"  // Optional, defaults to eclipse-temurin:17-jre-alpine
            images = listOf("myuser/myapp:1.0.0")
            ports = listOf(8080)  // Optional
            jvmArgs = listOf("-Xms256m", "-Xmx2048m")  // Optional
        }
    }

    // Optional: Image settings
    image {
        name = "my-caila-image"
        accessMode = "private"
        allowDestructiveUpdate = true
    }

    // Optional: Model settings
    model {
        name = "my-model"
        taskType = "custom"  // Default: "custom"
        displayName = "My Bot"
        displayAuthor = "My Team"
        shortDescription = "A conversational bot"

        http {
            port = 8080
            mainEndpoint = "/health"
        }

        publicSettings {
            isPublic = true
        }

        resourceLimits {
            cpuRequest = "500m"  // Default: "500m"
            memoryLimit = "1Gi"  // Default: "1Gi"
        }

        s3 {
            enabled = true
            prefix = "contexts"
            region = "ru"
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
        image = "registry.example.com/myapp:1.0.0"
        name = "my-caila-image"
    }

    model {
        name = "my-model"

        http {
            port = 8080
            mainEndpoint = "/api/predict"
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

### HTTP Client Configuration

Configure HTTP client settings for CAILA API requests:

```kotlin
httpClient {
    logLevel.set("INFO")           // HTTP log level: ALL, HEADERS, BODY, INFO, NONE. Default: ALL
    connectTimeoutMs.set(10_000)   // Connection timeout in milliseconds. Default: 10000
    requestTimeoutMs.set(35_000)   // Request timeout in milliseconds. Default: 35000
    keepAliveTimeMs.set(35_000)    // Keep-alive time in milliseconds. Default: 35000
}
```

### Image Configuration

```kotlin
image {
    image = "..."                 // Docker image path (required for manual registry workflow)
    name = "..."                  // CAILA image name (required)
    accessMode = "private"        // Access mode: "public" or "private". Default: "private"
    allowDestructiveUpdate = true // Allow overwriting existing images. Default: true
}
```

### Model Configuration

#### Basic Settings

```kotlin
model {
    name = "..."                  // Required
    taskType = "..."              // Task type for the model. Choose 'CUSTOM' if other options don't fit. See: https://docs.caila.io/api/task-types
    displayName = "..."
    displayAuthor = "..."
    shortDescription = "..."
    minInstancesCount = 1
    startTimeSec = 30.0
    rejectRequestsIfInactive = true  // Reject requests if service is inactive
    fittable = false              // Whether the service requires training on user data
    resourceGroup = "..."         // Resource group - a set of servers where service instances are launched
    languages = listOf("ru", "kk")  // Languages for display in the catalog
}
```

#### HTTP Settings

```kotlin
http {
    port = 8080               // Required: Port that the application web server listens on
    mainEndpoint = "/health"  // Required: Main page endpoint for availability check
    interfaceOnly = true      // Default: true. Enable if the service does not support gRPC API
}
```

#### Environment Variables

Configure environment variables that will be passed to your model container. The plugin provides a type-safe DSL for managing environment variables.

```kotlin
environmentVariables {
    put("API_KEY", "your-api-key")
    put("LOG_LEVEL", "INFO")
    put("DATABASE_URL", "postgres://...")

    // Or add multiple variables at once
    putAll(mapOf(
        "VAR1" to "value1",
        "VAR2" to "value2"
    ))

    // Use Gradle properties
    put("SECRET", providers.gradleProperty("mySecret").get())
}
```

**Automatic S3 Integration:**

The plugin automatically configures S3 context manager credentials if available from CAILA API. The following environment variables are automatically injected:

- `CAILA_S3_URL` - S3 endpoint URL
- `CAILA_S3_ACCESS_KEY` - S3 access key
- `CAILA_S3_SECRET_KEY` - S3 secret key
- `CAILA_S3_BUCKET_NAME` - S3 bucket name
- `CAILA_S3_KEY_PREFIX` - Key prefix (default: `contexts`)
- `CAILA_S3_REGION` - Region (default: `ru`)

These credentials are fetched automatically during model publishing and are combined with any custom environment variables you configure.

**Using S3 in Your Application:**

The easiest way to use CAILA S3 context manager is with the `CailaS3ContextManager` helper:

```kotlin
// In your bot code (e.g., HelloWorldBot.kt)
import com.justai.jaicf.context.manager.s3.CailaS3ContextManager

val bot = BotEngine(
    scenario = YourScenario,
    defaultContextManager = CailaS3ContextManager.createOrNull() ?: InMemoryBotContextManager,
    activators = arrayOf(...)
)
```

`CailaS3ContextManager.createOrNull()` automatically reads all CAILA S3 environment variables and creates a configured S3BotContextManager instance. If the environment variables are not set, it returns `null` so you can fall back to another context manager.

Alternatively, you can manually configure S3 context manager using environment variables:

```kotlin
import com.justai.jaicf.context.manager.s3.S3Config
import software.amazon.awssdk.regions.Region
import java.net.URI

val s3ContextManager = System.getenv("CAILA_S3_BUCKET_NAME")?.let { bucketName ->
    S3Config.create(
        bucketName = bucketName,
        region = Region.of(System.getenv("CAILA_S3_REGION") ?: "ru"),
        accessKeyId = System.getenv("CAILA_S3_ACCESS_KEY")
            ?: error("Missing CAILA_S3_ACCESS_KEY"),
        secretAccessKey = System.getenv("CAILA_S3_SECRET_KEY")
            ?: error("Missing CAILA_S3_SECRET_KEY"),
        keyPrefix = System.getenv("CAILA_S3_KEY_PREFIX") ?: "contexts",
        endpointOverride = System.getenv("CAILA_S3_URL")?.let { URI.create(it) }
    )
}

val bot = BotEngine(
    scenario = YourScenario,
    defaultContextManager = s3ContextManager ?: InMemoryBotContextManager,
    activators = arrayOf(...)
)
```

If S3 credentials are not available from CAILA API, the plugin logs a warning and continues without S3 configuration.

#### S3 Settings

Configure S3 context manager settings. The plugin automatically fetches credentials from CAILA API.

```kotlin
s3 {
    enabled = true                 // Default: true. Enable or disable S3 context manager
    prefix = "my-bot/contexts"     // Default: "contexts". Key prefix for S3 objects
    region = "us"                  // Default: "ru". S3 region
}

// To disable S3
s3 {
    enabled = false
}
```

#### Public Access Settings

```kotlin
public {
    isPublic = true            // Default: false
    featured = false
    featuredListOrder = 0
    hidden = false
    publicTestingAllowed = false
    showPersonalDataDisclaimer = false
}
```

#### Resource Limits

Resources allocated to one service instance within a resource group.

```kotlin
resources {
    cpuRequest = "500m"        // Default: "500m". CPU millicores guaranteed to the instance
    memoryLimit = "1Gi"        // Default: "1Gi". Amount of RAM available to the instance
    ephemeralDiskLimit = "10Gi"  // Amount of disk memory available to the instance
    gpuRequested = false       // Whether GPU is requested. Multiple instances can share one GPU
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
