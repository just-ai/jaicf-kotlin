package com.justai.jaicf.plugins.caila.publish.extension

import com.justai.jaicf.plugins.caila.publish.util.ModelType
import com.justai.jaicf.plugins.caila.publish.util.TaskType
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class CailaModelSpec
@Inject
constructor(
    private val objectFactory: ObjectFactory,
) {
    @get:Input
    val name: Property<String> = objectFactory.property()

    /**
     * Task type for the model.
     * Choose 'CUSTOM' if other options don't fit.
     * See documentation for more details: https://docs.caila.io/api/task-types
     */
    @get:Input
    val taskType: Property<TaskType> = objectFactory.property<TaskType>()
        .convention(TaskType.CUSTOM)

    /**
     * Model type for the service.
     * Use ModelType.WEB_APPLICATION for web applications, ModelType.MLP for MLP SDK-based services.
     * Default: ModelType.WEB_APPLICATION
     */
    @get:Input
    @Optional
    val modelType: Property<ModelType> = objectFactory.property<ModelType>()
        .convention(ModelType.WEB_APPLICATION)

    @get:Input
    @Optional
    val displayName: Property<String> = objectFactory.property()

    @get:Input
    @Optional
    val displayAuthor: Property<String> = objectFactory.property()

    @get:Input
    @Optional
    val shortDescription: Property<String> = objectFactory.property()

    /**
     * Languages for display in the catalog.
     * Specify each language separately. Examples: ru, kk
     */
    @get:Input
    @Optional
    val languages: ListProperty<String> = objectFactory.listProperty()

    /**
     * Environment variables to be passed to the model container.
     * Provides a type-safe DSL for configuring environment variables.
     */
    @get:Input
    @Optional
    val environmentVariables: Property<EnvVariablesSpec> = objectFactory.property()

    /**
     * Resource group - a set of servers where service instances are launched.
     * If not specified, defaults to free-pool-quota-for-{accountId}
     */
    @get:Input
    @Optional
    val resourceGroup: Property<String> = objectFactory.property()

    @get:Input
    @Optional
    val timeouts: Property<TimeoutsSpec> = objectFactory.property()

    @get:Input
    @Optional
    val resourceLimits: Property<ResourceLimitsSpec> = objectFactory.property()

    @get:Input
    @Optional
    val http: Property<HttpSettingsSpec> = objectFactory.property()

    @get:Input
    @Optional
    val publicSettings: Property<PublicSettingsSpec> = objectFactory.property()

    @get:Input
    @Optional
    val s3: Property<S3SettingsSpec> = objectFactory.property()

    fun timeouts(action: Action<TimeoutsSpec>) {
        val spec = objectFactory.newInstance(TimeoutsSpec::class.java)
        action.execute(spec)
        this.timeouts.set(spec)
    }

    fun resourceLimits(action: Action<ResourceLimitsSpec>) {
        val spec = objectFactory.newInstance(ResourceLimitsSpec::class.java)
        action.execute(spec)
        this.resourceLimits.set(spec)
    }

    fun environmentVariables(action: Action<EnvVariablesSpec>) {
        val spec = objectFactory.newInstance(EnvVariablesSpec::class.java)
        action.execute(spec)
        this.environmentVariables.set(spec)
    }

    fun s3(action: Action<S3SettingsSpec>) {
        val spec = objectFactory.newInstance(S3SettingsSpec::class.java)
        action.execute(spec)
        this.s3.set(spec)
    }

    fun http(action: Action<HttpSettingsSpec>) {
        val spec = objectFactory.newInstance(HttpSettingsSpec::class.java)
        action.execute(spec)
        this.http.set(spec)
    }

    fun publicSettings(action: Action<PublicSettingsSpec>) {
        val spec = objectFactory.newInstance(PublicSettingsSpec::class.java)
        action.execute(spec)
        this.publicSettings.set(spec)
    }
}

/**
 * S3 settings for automatic S3 context manager configuration
 */
open class S3SettingsSpec
@Inject
constructor(
    private val objectFactory: ObjectFactory,
) {
    /**
     * Enable or disable S3 context manager.
     * Default: true (enabled if credentials are available)
     */
    @get:Input
    @Optional
    val enabled: Property<Boolean> = objectFactory.property<Boolean>()
        .convention(true)

    @get:Input
    @Optional
    val prefix: Property<String> = objectFactory.property<String>()
        .convention("contexts")

    @get:Input
    @Optional
    val region: Property<String> = objectFactory.property<String>()
        .convention("ru")
}

open class TimeoutsSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        /**
         * Timeout for starting a service instance, in seconds.
         */
        val podStartTimeoutSec: Property<Long> = objectFactory.property<Long>().convention(120)
    }

/**
 * Resources allocated to one service instance within a resource group.
 */
open class ResourceLimitsSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        /**
         * CPU millicores guaranteed to the instance (can consume more if available).
         */
        val cpuRequest: Property<String> = objectFactory.property<String>()
            .convention("100m")

        /**
         * Amount of RAM available to the instance.
         * If the instance exceeds the limit, it will be stopped.
         */
        val memoryLimit: Property<String> = objectFactory.property<String>()
            .convention("500Mi")

        /**
         * Amount of disk memory available to the instance.
         * If the instance exceeds the limit, it will be stopped.
         */
        val ephemeralDiskLimit: Property<String> = objectFactory.property<String>()
            .convention("100Mi")

        /**
         * Whether GPU is requested.
         * Multiple instances can share one GPU.
         */
        val gpuRequested: Property<Boolean> = objectFactory.property<Boolean>()
            .convention(false)
    }

open class HttpSettingsSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        @get:Input
        @get:Optional
        val isHttpEnabled: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

        /**
         * Port that the application web server listens on.
         */
        @get:Input
        val port: Property<Int> = objectFactory.property()

        /**
         * Path for availability check (healthcheck endpoint).
         * Default: "/health"
         */
        @get:Input
        @get:Optional
        val mainPageEndpoint: Property<String> = objectFactory.property<String>()
            .convention("/health")

        /**
         * Enable if the service does not support gRPC API.
         * Disable if the service is developed based on MLP SDK.
         */
        @get:Input
        @get:Optional
        val interfaceOnly: Property<Boolean> = objectFactory.property<Boolean>().convention(true)
    }

open class PublicSettingsSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        val isPublic: Property<Boolean> = objectFactory.property<Boolean>().convention(true)
        val featured: Property<Boolean> = objectFactory.property<Boolean>().convention(false)
        val hidden: Property<Boolean> = objectFactory.property<Boolean>().convention(false)
        val publicTestingAllowed: Property<Boolean> = objectFactory.property<Boolean>().convention(false)
        val showPersonalDataDisclaimer: Property<Boolean> = objectFactory.property<Boolean>().convention(false)
    }
