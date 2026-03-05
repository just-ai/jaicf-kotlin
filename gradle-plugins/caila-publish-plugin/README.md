# CAILA Publish Plugin

Gradle plugin for deploying JAICF bots to CAILA MLOps platform.

## Quick Start

### 1. Installation

```kotlin
plugins {
    id("com.justai.jaicf.caila-publish-plugin") version "1.0.0"
}
```

### 2. Configure Credentials

Add to `~/.gradle/gradle.properties`:

```properties
caila.token=<your-api-token>
caila.accountId=<your-account-id>
dockerUsername=<docker-username>
dockerPassword=<docker-password>
dockerEmail=<your-email>
```

### 3. Minimal Configuration

```kotlin
cailaPublish {
    docker {
        javaApplication {
            mainClassName = "com.example.MainKt"
            images = listOf("myuser/myapp:1.0.0")
        }
    }

    image {
        name = "my-bot-image"
    }

    model {
        name = "my-bot"

        http {
            port = 8080
        }
    }
}
```

### 4. Deploy

```bash
./gradlew publishToCailaFromDocker
```

**That's it!** The plugin will automatically:
- Clean build directory
- Build Docker image
- Push to registry
- Create/update CAILA image
- Create/update CAILA model
- Configure S3 context manager (if available)
- Start 1 instance on resource pool

## S3 Context Manager Setup

### Step 1: Configure Plugin

```kotlin
model {
    s3 {
        enabled = true       // Default: true
        prefix = "contexts"  // Default: "contexts"
        region = "ru"        // Default: "ru"
    }
}
```

### Step 2: Use in Bot Code

```kotlin
import com.justai.jaicf.BotEngine
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.context.manager.s3.CailaS3ContextManager

val bot = BotEngine(
    scenario = YourScenario,
    defaultContextManager = CailaS3ContextManager.createOrNull() ?: InMemoryBotContextManager,
    activators = arrayOf(...)
)
```

**Done!** S3 credentials are automatically fetched from CAILA and injected as environment variables.

## Common Configuration Options

### Docker Settings

```kotlin
docker {
    registryCredentials {
        url.set("https://docker-hub.just-ai.com")
        username.set(providers.gradleProperty("dockerUsername"))
        password.set(providers.gradleProperty("dockerPassword"))
    }

    javaApplication {
        mainClassName = "com.example.MainKt"
        baseImage = "eclipse-temurin:17-jre-alpine"  // Default
        images = listOf("myuser/myapp:1.0.0")
        ports = listOf(8080)
        jvmArgs = listOf("-Xms256m", "-Xmx512m")
    }
}
```

### Image Settings

```kotlin
image {
    name = "my-bot-image"              // Required
    accessMode = "private"             // private | public. Default: private
    allowDestructiveUpdate = true      // Default: true
}
```

### Model Settings

```kotlin
model {
    name = "my-bot"                    // Required
    displayName = "My Bot"
    displayAuthor = "My Team"
    shortDescription = "Bot description"
    minInstancesCount = 1              // Default: 1 (auto-starts instance)

    http {
        port = 8080                    // Required
        mainEndpoint = "/health"       // Default healthcheck endpoint
    }
}
```

### Environment Variables

```kotlin
environmentVariables {
    put("API_KEY", "your-key")
    put("LOG_LEVEL", "INFO")
    putAll(mapOf(
        "VAR1" to "value1",
        "VAR2" to "value2"
    ))
}
```

S3 credentials are automatically added by the plugin.

### Resource Limits

```kotlin
resourceLimits {
    cpuRequest = "500m"       // Default: 500m
    memoryLimit = "1Gi"       // Default: 1Gi
}
```

### Auto-scaling

```kotlin
autoScalingConfiguration {
    enabled.set(true)
    minInstanceCount.set(1)
    maxInstanceCount.set(5)
    scaleUpRequestsPerMinuteThreshold.set(100)
    scaleDownRequestsPerMinuteThreshold.set(10)
}
```

### Public Access

```kotlin
publicSettings {
    isPublic = true           // Default: false
}
```

## Available Tasks

| Task | Description |
|------|-------------|
| `publishToCailaFromDocker` | Build Docker image and deploy to CAILA |
| `publishCailaImageFromDocker` | Build and publish Docker image only |
| `publishCailaModelFromDocker` | Publish model only (requires image) |
| `publishToCailaFromRegistry` | Deploy pre-built image to CAILA |

## Advanced Configuration

<details>
<summary>Timeouts</summary>

```kotlin
timeouts {
    podStartTimeoutSec.set(120)   // Default: 120
    predictTimeoutSec.set(30)     // Default: 30
    fitTimeoutSec.set(600)        // Default: 600
}
```
</details>

<details>
<summary>Retries</summary>

```kotlin
retriesConfig {
    maxRetries.set(3)
    timeoutsMs.set(listOf(1000L, 2000L, 4000L))
    maxRetriesPerInstance.set(1)
}
```
</details>

<details>
<summary>Batching</summary>

```kotlin
batchesConfig {
    batchSize.set(10)
    timeWaitMs.set(1000)
    maxLengthToSkip.set(1000000)
}
```
</details>

<details>
<summary>Caching</summary>

```kotlin
caching {
    enabled.set(true)
    mongoUri.set("mongodb://...")
    collectionName.set("cache")
    recordsLimit.set(10000)
}
```
</details>

<details>
<summary>Priority Queue</summary>

```kotlin
priorityQueue {
    enabled.set(true)
    concurrencyLevel.set(10)
}
```
</details>

<details>
<summary>Archive Settings</summary>

```kotlin
archiveSettings {
    enabled.set(true)
    numberOfArchivedRequests.set(1000)
    encryptionEnabled.set(false)
}
```
</details>

## Requirements

- Gradle 9.0+
- Kotlin 2.2.0+
- Java 17+
- Docker (for automatic builds)
- CAILA account with API access

## Use Pre-built Image

If you already have a Docker image:

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
        }
    }
}
```

Then run:

```bash
./gradlew publishToCailaFromRegistry
```