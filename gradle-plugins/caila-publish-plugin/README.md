# CAILA Publish Plugin

Gradle plugin for deploying JAICF bots to CAILA platform.

## Quick Start

### 1. Add Plugin

```kotlin
plugins {
    id("com.justai.jaicf.caila-publish-plugin")
}
```

### 2. Configure Credentials

Add to `~/.gradle/gradle.properties`:

```properties
caila.token=<your-caila-api-token>
caila.accountId=<your-account-id>
dockerUsername=<docker-username>
dockerPassword=<docker-password>
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

**That's it!** The plugin automatically:
- Cleans build directory
- Builds Docker image (linux/amd64)
- Pushes to registry
- Creates/updates CAILA service with public HTTP access
- Starts instance on free resource pool
- Configures S3 context manager

## S3 Context Manager (Automatic)

### Step 1: Enable S3 in Plugin (Optional - enabled by default)

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

**Done!** S3 credentials are automatically fetched and injected. Works in CAILA, falls back to InMemory locally.

## Configuration Options

### Model Settings

```kotlin
model {
    name = "my-bot"                    // Required
    displayName = "My Bot"             // Optional
    displayAuthor = "My Team"          // Optional
    shortDescription = "Description"   // Optional
    taskType = "custom"                // Default: "custom"
    modelType = "WEB_APPLICATION"      // Default: "WEB_APPLICATION"
    languages = listOf("ru", "en")     // Default: []
}
```

### HTTP Settings

```kotlin
http {
    port = 8080                    // Required
    mainPageEndpoint = "/health"   // Default: "/health"
}
```

### Public Access

```kotlin
publicSettings {
    isPublic = true                // Default: false
    featured = false               // Default: false
    hidden = false                 // Default: false
}
```

### Resource Group & Limits

```kotlin
model {
    resourceGroup = "my-resource-group"  // Default: "free-pool-quota-for-{accountId}"

    // Optional: Custom resource limits (only for custom resource groups)
    resourceLimits {
        cpuRequest = "100m"            // Default: "100m"
        memoryLimit = "500Mi"          // Default: "500Mi"
        ephemeralDiskLimit = "100Mi"   // Default: "100Mi"
        gpuRequested = false           // Default: false
    }
}
```

**Note:** Resource limits are only needed for custom resource groups. Free pool has predefined limits.

### Timeouts

```kotlin
timeouts {
    podStartTimeoutSec.set(120)    // Default: 120
}
```

### Environment Variables

```kotlin
environmentVariables {
    put("API_KEY", "your-key")
    put("LOG_LEVEL", "INFO")
}
```

S3 credentials are automatically added by the plugin.

### Docker Registry

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
    accessMode = "private"             // private | public. Default: "private"
    allowDestructiveUpdate = true      // Default: true
}
```

## Use Pre-built Image

If you have a Docker image already:

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

## Available Tasks

| Task | Description |
|------|-------------|
| `publishToCailaFromDocker` | Build Docker image and deploy to CAILA |
| `publishCailaImageFromDocker` | Build and publish Docker image only |
| `publishCailaModelFromDocker` | Publish model only (requires image task) |
| `publishToCailaFromRegistry` | Deploy pre-built image to CAILA |

## Requirements

- Gradle 9.0+
- Kotlin 2.2.0+
- Java 17+
- Docker (for automatic builds)
- CAILA account with API access

## Additional Resources

- [CAILA Documentation](https://docs.caila.io)
- [Task Types Reference](https://docs.caila.io/api/task-types)
- [CAILA API](https://caila.io/swagger-ui)
