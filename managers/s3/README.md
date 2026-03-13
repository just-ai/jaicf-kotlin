# JAICF AWS S3 Bot Context Manager

AWS S3 implementation of `BotContextManager` for storing JAICF bot contexts in Amazon S3.

## Overview

This module provides a `BotContextManager` implementation that stores bot conversation contexts as JSON files in an Amazon S3 bucket. It's suitable for serverless deployments, cloud-native applications, and scenarios where you need scalable, durable context storage.

## Features

- Stores bot contexts as JSON files in S3
- Supports multiple AWS authentication methods
- Configurable S3 object key prefix
- Compatible with all AWS regions
- Thread-safe and production-ready

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.justai.jaicf:s3:$jaicfVersion")
}
```

## Prerequisites

- AWS account with S3 access
- S3 bucket created in your desired region
- AWS credentials configured (see Authentication section)

## Usage

### CAILA Integration (Recommended)

If you're using the [CAILA Publish Plugin](../../gradle-plugins/caila-publish-plugin), the easiest way to set up S3 context manager is with `CailaS3ContextManager`:

```kotlin
import com.justai.jaicf.BotEngine
import com.justai.jaicf.context.manager.s3.CailaS3ContextManager

val botEngine = BotEngine(
    scenario = myScenario,
    defaultContextManager = CailaS3ContextManager.createOrNull() ?: InMemoryBotContextManager
)
```

`CailaS3ContextManager.createOrNull()` automatically reads S3 configuration from environment variables that are injected by the CAILA Publish Plugin:
- `CAILA_S3_BUCKET_NAME`
- `CAILA_S3_ACCESS_KEY`
- `CAILA_S3_SECRET_KEY`
- `CAILA_S3_REGION` (default: "ru")
- `CAILA_S3_KEY_PREFIX` (default: "contexts")
- `CAILA_S3_URL` (optional, for custom S3 endpoints)

If the environment variables are not set, it returns `null` so you can fall back to another context manager.

### Basic Setup with Default Credentials

You can also use S3BotContextManager directly with the default AWS credentials provider chain:

```kotlin
import com.justai.jaicf.BotEngine
import com.justai.jaicf.context.manager.s3.S3Config
import software.amazon.awssdk.regions.Region

val contextManager = S3Config.create(
    bucketName = "my-bot-contexts",
    region = Region.US_EAST_1
)

val botEngine = BotEngine(
    scenario = myScenario,
    contextManager = contextManager
)
```

### Setup with Explicit Credentials

For development or testing, you can provide explicit AWS credentials:

```kotlin
val contextManager = S3Config.create(
    bucketName = "my-bot-contexts",
    region = Region.EU_WEST_1,
    accessKeyId = System.getenv("AWS_ACCESS_KEY_ID"),
    secretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY")
)
```

### Setup with Custom Key Prefix

Organize contexts with a custom S3 key prefix:

```kotlin
val contextManager = S3Config.create(
    bucketName = "my-bot-contexts",
    region = Region.AP_SOUTHEAST_1,
    keyPrefix = "production/contexts"
)
```

### Advanced: Custom S3 Client

For advanced scenarios (custom endpoint, VPC, etc.):

```kotlin
import software.amazon.awssdk.services.s3.S3Client
import com.justai.jaicf.context.manager.s3.S3BotContextManager

val customS3Client = S3Client.builder()
    .region(Region.US_WEST_2)
    .endpointOverride(URI.create("https://custom-s3-endpoint.com"))
    .build()

val contextManager = S3BotContextManager(
    s3Client = customS3Client,
    bucketName = "my-bot-contexts",
    keyPrefix = "contexts"
)
```

## Authentication

The S3BotContextManager supports multiple authentication methods through AWS SDK:

### 1. Environment Variables (Recommended for Production)

```bash
export AWS_ACCESS_KEY_ID="your-access-key-id"
export AWS_SECRET_ACCESS_KEY="your-secret-access-key"
export AWS_REGION="us-east-1"
```

### 2. AWS Credentials File

Create `~/.aws/credentials`:

```ini
[default]
aws_access_key_id = your-access-key-id
aws_secret_access_key = your-secret-access-key
```

And `~/.aws/config`:

```ini
[default]
region = us-east-1
```

### 3. IAM Role (For EC2, ECS, Lambda)

When running on AWS infrastructure, use IAM roles attached to your instance/container. No credentials needed in code.

### 4. Explicit Credentials (Not Recommended for Production)

```kotlin
val contextManager = S3Config.create(
    bucketName = "my-bot-contexts",
    region = Region.US_EAST_1,
    accessKeyId = "your-access-key-id",
    secretAccessKey = "your-secret-access-key"
)
```

## S3 Bucket Configuration

### Required Permissions

Your AWS credentials need the following S3 permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Resource": "arn:aws:s3:::my-bot-contexts/*"
    }
  ]
}
```

### Bucket Structure

Contexts are stored as JSON files with the following structure:

```
my-bot-contexts/
└── contexts/
    ├── client-id-1.json
    ├── client-id-2.json
    └── client-id-3.json
```

Each file contains:

```json
{
  "clientId": "user-123",
  "result": null,
  "client": {},
  "session": {},
  "dialogContext": {
    "currentState": "/main",
    "backStateStack": []
  }
}
```

## Cost Considerations

- **Storage**: S3 Standard pricing applies (~$0.023 per GB/month)
- **Requests**: PUT and GET requests are charged separately
- **Optimization**: Consider using S3 Intelligent-Tiering for cost optimization
- **Lifecycle**: Set up lifecycle policies to delete old contexts

## Performance

- **Latency**: Typical GET/PUT operations take 50-100ms
- **Scalability**: S3 automatically scales to handle high request rates
- **Caching**: Consider implementing a caching layer (e.g., Redis) for high-traffic bots

## Troubleshooting

### "Access Denied" Error

Ensure your AWS credentials have the required S3 permissions.

### "No Such Bucket" Error

Verify the bucket name and region are correct. The bucket must exist before using the context manager.

### Slow Performance

- Check if you're in the same AWS region as your S3 bucket
- Consider enabling S3 Transfer Acceleration for cross-region access
- Implement caching for frequently accessed contexts

## Examples

See the [examples directory](../../examples) for complete bot implementations using S3BotContextManager.

## Links

- [JAICF Documentation](https://github.com/just-ai/jaicf-kotlin)
- [AWS S3 Documentation](https://docs.aws.amazon.com/s3/)
- [AWS SDK for Java Documentation](https://docs.aws.amazon.com/sdk-for-java/)