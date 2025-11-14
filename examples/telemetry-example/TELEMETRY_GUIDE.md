# JAICF Telemetry Integration Guide

## Overview

JAICF BotEngine includes built-in support for OpenTelemetry distributed tracing. This allows you to monitor and debug your bot's behavior in production and development environments.

## How It Works

### Architecture

The telemetry integration in JAICF uses a hook-based system:

1. **TelemetryProvider**: Abstract interface for creating telemetry spans
2. **TelemetryHookBinder**: Automatically installs hooks into BotEngine
3. **OpenTelemetryTelemetryProvider**: Implementation for OpenTelemetry

### Automatic Instrumentation

When you enable telemetry with `.withTelemetry()`, JAICF automatically instruments:

#### 1. Request Lifecycle
- **jaicf.request.start**: Request begins processing
- **jaicf.request.end**: Request completes processing
- Attributes: `request.type`, `client_id`, `session.new`, `duration_ms`

#### 2. Context Management
- **jaicf.context.load**: Loading bot context from storage
- **jaicf.context.save**: Saving bot context to storage
- Attributes: `context.manager`, `context.stage`, `duration_ms`

#### 3. Activation
- **jaicf.activation.before**: Before activator selection
- **jaicf.activation.after**: After activator selection
- Attributes: `activation.state`, `activation.activator`, `duration_ms`

#### 4. Slot Filling
- **jaicf.slot_filling.start**: Slot filling begins
- **jaicf.slot_filling.progress**: Slot filling in progress
- **jaicf.slot_filling.finish**: Slot filling completes
- Attributes: `activator`, `target_state`, `result`, `duration_ms`

#### 5. Errors
All exceptions are automatically captured with:
- Full stack traces
- Span status set to ERROR
- Error message in span attributes

## Usage

### Basic Setup

```kotlin
// 1. Create OpenTelemetry tracer
val tracer = buildTracer()

// 2. Create bot with telemetry
val bot = BotEngine(
    scenario = MyScenario,
    activators = arrayOf(RegexActivator)
).withTelemetry(
    OpenTelemetryTelemetryProvider(tracer)
)

// 3. Process requests as usual
bot.process(request, reactions)
```

### OpenTelemetry Configuration

```kotlin
fun buildTracer(): Tracer {
    val resource = Resource.create(
        Attributes.builder()
            .put(AttributeKey.stringKey("service.name"), "my-bot")
            .put(AttributeKey.stringKey("service.version"), "1.0.0")
            .build()
    )
    
    val tracerProvider = SdkTracerProvider.builder()
        .setResource(resource)
        .setSampler(Sampler.alwaysOn())
        .addSpanProcessor(
            SimpleSpanProcessor.create(
                OtlpGrpcSpanExporter.builder()
                    .setEndpoint("http://localhost:4317")
                    .build()
            )
        )
        .build()

    val sdk = OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .build()

    return sdk.getTracer("my-bot", "1.0.0")
}
```

### Exporter Options

#### 1. Console Logging (Development)
```kotlin
.addSpanProcessor(
    SimpleSpanProcessor.create(LoggingSpanExporter.create())
)
```

#### 2. OTLP (Jaeger, Tempo, etc.)
```kotlin
.addSpanProcessor(
    SimpleSpanProcessor.create(
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://localhost:4317")
            .build()
    )
)
```

#### 3. Zipkin
```kotlin
.addSpanProcessor(
    SimpleSpanProcessor.create(
        ZipkinSpanExporter.builder()
            .setEndpoint("http://localhost:9411/api/v2/spans")
            .build()
    )
)
```

## Visualization with Jaeger

### Running Jaeger

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

### Viewing Traces

1. Open http://localhost:16686
2. Select your service name from the dropdown
3. Click "Find Traces"
4. Explore the trace timeline and spans

### What You'll See

- **Request timeline**: Complete flow from start to finish
- **Activation details**: Which activator handled the request
- **State transitions**: Path through your scenario
- **Performance metrics**: Duration of each operation
- **Error details**: Stack traces and error messages

## Advanced Usage

### Custom Spans in Actions

You can create custom spans in your bot actions:

```kotlin
action {
    val span = currentTelemetrySpan()
    span?.setAttribute("custom.attribute", "value")
    span?.addEvent("custom.event")
    
    // Your action logic
    reactions.say("Hello!")
}
```

### Context Propagation

Telemetry spans are automatically propagated through:
- Coroutine context
- Thread local storage
- Parent-child span relationships

### Sampling

Control which requests are traced:

```kotlin
// Always trace
.setSampler(Sampler.alwaysOn())

// Never trace (turn off)
.setSampler(Sampler.alwaysOff())

// Trace 10% of requests
.setSampler(Sampler.traceIdRatioBased(0.1))

// Parent-based sampling
.setSampler(Sampler.parentBased(Sampler.traceIdRatioBased(0.1)))
```

## Best Practices

### 1. Use Service Name and Version
Always set service name and version in your resource:
```kotlin
Resource.create(
    Attributes.builder()
        .put(AttributeKey.stringKey("service.name"), "my-bot")
        .put(AttributeKey.stringKey("service.version"), "1.0.0")
        .build()
)
```

### 2. Choose Appropriate Exporters
- **Development**: Use `LoggingSpanExporter` for console output
- **Production**: Use OTLP exporter with a tracing backend

### 3. Set Up Sampling
Don't trace every request in production:
```kotlin
.setSampler(Sampler.traceIdRatioBased(0.1)) // 10% sampling
```

### 4. Add Custom Attributes
Add business-specific attributes to help with filtering:
```kotlin
span?.setAttribute("bot.scenario", "customer-support")
span?.setAttribute("bot.language", "en")
span?.setAttribute("user.tier", "premium")
```

### 5. Monitor Performance
Watch for:
- High activation duration (slow activators)
- High context load/save duration (slow storage)
- Error rates by state
- Request distribution by activator

## Troubleshooting

### No Spans Appear

1. Check that telemetry is enabled: `.withTelemetry(...)`
2. Verify exporter configuration
3. Check network connectivity to backend
4. Ensure sampling is not set to `alwaysOff()`

### Performance Impact

Telemetry adds minimal overhead:
- ~1-5ms per request
- Most impact from network export
- Use batch span processors in production:

```kotlin
.addSpanProcessor(
    BatchSpanProcessor.builder(exporter)
        .setScheduleDelay(5, TimeUnit.SECONDS)
        .build()
)
```

### Memory Issues

If spans accumulate in memory:
- Use batch processors instead of simple processors
- Reduce sampling rate
- Check that spans are being exported successfully

## Integration with Other Systems

### Kubernetes
Add pod labels to trace attributes:
```kotlin
.put(AttributeKey.stringKey("k8s.pod.name"), System.getenv("HOSTNAME"))
.put(AttributeKey.stringKey("k8s.namespace"), System.getenv("NAMESPACE"))
```

### Correlation IDs
Use request IDs for correlation:
```kotlin
span?.setAttribute("correlation.id", request.clientId)
```

### APM Integration
Telemetry works with:
- Datadog APM
- New Relic
- Elastic APM
- Dynatrace
- AWS X-Ray

## Resources

- [OpenTelemetry Documentation](https://opentelemetry.io/docs/)
- [OpenTelemetry Java SDK](https://opentelemetry.io/docs/languages/java/)
- [Jaeger Documentation](https://www.jaegertracing.io/docs/)
- [JAICF Documentation](https://github.com/just-ai/jaicf-kotlin)



