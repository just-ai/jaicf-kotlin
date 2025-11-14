# Telemetry Example

This example demonstrates how to use OpenTelemetry tracing with JAICF BotEngine.

## Overview

The example shows:
- How to configure OpenTelemetry with JAICF
- Automatic tracing of bot requests, activations, and actions
- Integration with different exporters (logging and OTLP)
- Custom span attributes and events

## Features

The telemetry integration automatically traces:
- **Request lifecycle**: Start and end of each request with duration
- **Context management**: Loading and saving bot context
- **Activation**: Which activator handled the request and which state was selected
- **Slot filling**: Slot filling process stages
- **Errors**: Automatic exception recording in spans

## Running the Example

### 1. Simple Console Example

Run the basic console example with logging exporter:

```bash
./gradlew :telemetry-example:run
```

This will:
- Start a simple bot with telemetry enabled
- Process a few test messages
- Output traces to console logs

### 2. With Jaeger (Optional)

To visualize traces in Jaeger UI:

1. Start Jaeger with Docker:
```bash
docker run -d --name jaeger \
  -p 5775:5775/udp \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 14268:14268 \
  -p 14250:14250 \
  -p 9411:9411 \
  -p 4317:4317 \
  -p 4318:4318 \
  jaegertracing/all-in-one:latest
```

2. Run the example
3. Open http://localhost:16686 to view traces in Jaeger UI

### 3. HTTP Server Example

Run the HTTP server example:

```bash
./gradlew :telemetry-example:run -PmainClass=com.justai.jaicf.examples.telemetry.HttpServerExampleKt
```

Then send requests:
```bash
curl -X POST http://localhost:8080/bot \
  -H "Content-Type: application/json" \
  -d '{"query": "hello"}'
```

## Code Structure

- `TelemetryScenario.kt` - Simple bot scenario with multiple states
- `ConsoleExample.kt` - Console-based example
- `HttpServerExample.kt` - HTTP server example
- `TelemetryConfig.kt` - OpenTelemetry configuration utilities

## Key Concepts

### Enabling Telemetry

```kotlin
val bot = BotEngine(
    scenario = MyScenario,
    activators = arrayOf(RegexActivator)
).withTelemetry(
    OpenTelemetryTelemetryProvider(
        tracer = buildTracer()
    )
)
```

### OpenTelemetry Configuration

```kotlin
val tracerProvider = SdkTracerProvider.builder()
    .setResource(resource)
    .setSampler(Sampler.alwaysOn())
    .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
    .build()
```

## Span Attributes

The telemetry integration adds these attributes automatically:

- `jaicf.request.type` - Type of the request
- `jaicf.request.client_id` - Client ID
- `jaicf.session.new` - Whether it's a new session
- `jaicf.activation.state` - Activated state path
- `jaicf.activation.activator` - Name of the activator
- `jaicf.duration_ms` - Duration in milliseconds

## Learn More

- [OpenTelemetry Documentation](https://opentelemetry.io/docs/)
- [JAICF Documentation](https://github.com/just-ai/jaicf-kotlin)



