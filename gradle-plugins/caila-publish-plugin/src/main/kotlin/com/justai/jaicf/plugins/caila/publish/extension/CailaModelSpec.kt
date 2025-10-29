package com.justai.jaicf.plugins.caila.publish.extension

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
    val modelName: Property<String> = objectFactory.property()

    /**
     * Task type for the model.
     * Choose 'CUSTOM' if other options don't fit.
     * See documentation for more details: https://docs.caila.io/api/task-types
     */
    @get:Input
    val taskType: Property<String> = objectFactory.property<String>()
        .convention(TaskType.CUSTOM.tag)

    @get:Input
    @Optional
    val displayName: Property<String> = objectFactory.property()

    @get:Input
    @Optional
    val displayAuthor: Property<String> = objectFactory.property()

    /**
     * Reject requests if service is inactive.
     * If no active instances exist and this option is enabled, the request will return an error.
     * If disabled, the request will wait until an instance starts or timeout occurs.
     * Instance startup begins regardless of this setting.
     */
    @get:Input
    @Optional
    val rejectRequestsIfInactive: Property<Boolean> = objectFactory.property()

    @get:Input
    @Optional
    val config: Property<String> = objectFactory.property()

    @get:Input
    @Optional
    val env: Property<String> = objectFactory.property()

    /**
     * Whether the service requires training on user data.
     */
    @get:Input
    @Optional
    val fittable: Property<Boolean> = objectFactory.property()

    @get:Input
    @Optional
    val hostingType: Property<String> = objectFactory.property()

    /**
     * Resource group - a set of servers where service instances are launched.
     */
    @get:Input
    @Optional
    val resourceGroup: Property<String> = objectFactory.property()

    @get:Input
    @Optional
    val shortDescription: Property<String> = objectFactory.property()

    @get:Input
    @Optional
    val minInstancesCount: Property<Int> = objectFactory.property()

    @get:Input
    @Optional
    val startTimeSec: Property<Double> = objectFactory.property()

    @get:Input
    @Optional
    val additionalFlags: ListProperty<String> = objectFactory.listProperty()

    /**
     * Languages for display in the catalog.
     * Specify each language separately. Examples: ru, kk
     */
    @get:Input
    @Optional
    val languages: ListProperty<String> = objectFactory.listProperty()

    @get:Input
    @Optional
    val aliases: ListProperty<String> = objectFactory.listProperty()

    @get:Input
    @Optional
    val persistentVolumes: ListProperty<PersistentVolumeSpec> = objectFactory.listProperty()

    /**
     * Docker images with static resources that the service can use, such as model weights.
     * Available for selection are images added to 'My Space' as well as public ones.
     */
    @get:Input
    @Optional
    val dataImageMounts: ListProperty<DataImageMountSpec> = objectFactory.listProperty()

    @get:Input
    @Optional
    val timeouts: Property<TimeoutsSpec> = objectFactory.property()

    @get:Input
    @Optional
    val resourceLimits: Property<ResourceLimitsSpec> = objectFactory.property()

    @get:Input
    @Optional
    val retriesConfig: Property<RetriesConfigSpec> = objectFactory.property()

    @get:Input
    @Optional
    val batchesConfig: Property<BatchesConfigSpec> = objectFactory.property()

    @get:Input
    @Optional
    val caching: Property<CachingSpec> = objectFactory.property()

    @get:Input
    @Optional
    val priorityQueue: Property<PriorityQueueSpec> = objectFactory.property()

    @get:Input
    @Optional
    val autoScalingConfiguration: Property<AutoScalingConfigurationSpec> = objectFactory.property()

    @get:Input
    @Optional
    val httpSettings: Property<HttpSettingsSpec> = objectFactory.property()

    @get:Input
    @Optional
    val archiveSettings: Property<ArchiveSettingsSpec> = objectFactory.property()

    @get:Input
    @Optional
    val publicSettings: Property<PublicSettingsSpec> = objectFactory.property()

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

    fun retriesConfig(action: Action<RetriesConfigSpec>) {
        val spec = objectFactory.newInstance(RetriesConfigSpec::class.java)
        action.execute(spec)
        this.retriesConfig.set(spec)
    }

    fun batchesConfig(action: Action<BatchesConfigSpec>) {
        val spec = objectFactory.newInstance(BatchesConfigSpec::class.java)
        action.execute(spec)
        this.batchesConfig.set(spec)
    }

    fun caching(action: Action<CachingSpec>) {
        val spec = objectFactory.newInstance(CachingSpec::class.java)
        action.execute(spec)
        this.caching.set(spec)
    }

    fun priorityQueue(action: Action<PriorityQueueSpec>) {
        val spec = objectFactory.newInstance(PriorityQueueSpec::class.java)
        action.execute(spec)
        this.priorityQueue.set(spec)
    }

    fun autoScalingConfiguration(action: Action<AutoScalingConfigurationSpec>) {
        val spec = objectFactory.newInstance(AutoScalingConfigurationSpec::class.java)
        action.execute(spec)
        this.autoScalingConfiguration.set(spec)
    }

    fun httpSettings(action: Action<HttpSettingsSpec>) {
        val spec = objectFactory.newInstance(HttpSettingsSpec::class.java)
        action.execute(spec)
        this.httpSettings.set(spec)
    }

    fun archiveSettings(action: Action<ArchiveSettingsSpec>) {
        val spec = objectFactory.newInstance(ArchiveSettingsSpec::class.java)
        action.execute(spec)
        this.archiveSettings.set(spec)
    }

    fun publicSettings(action: Action<PublicSettingsSpec>) {
        val spec = objectFactory.newInstance(PublicSettingsSpec::class.java)
        action.execute(spec)
        this.publicSettings.set(spec)
    }

    fun persistentVolume(action: Action<PersistentVolumeSpec>) {
        val spec = objectFactory.newInstance(PersistentVolumeSpec::class.java)
        action.execute(spec)
        val current = this.persistentVolumes.orNull?.toMutableList() ?: mutableListOf()
        current.add(spec)
        this.persistentVolumes.set(current)
    }

    fun dataImageMount(action: Action<DataImageMountSpec>) {
        val spec = objectFactory.newInstance(DataImageMountSpec::class.java)
        action.execute(spec)
        val current = this.dataImageMounts.orNull?.toMutableList() ?: mutableListOf()
        current.add(spec)
        this.dataImageMounts.set(current)
    }
}
open class PersistentVolumeSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        val mountPath: Property<String> = objectFactory.property()
        val storageClass: Property<String> = objectFactory.property()

        val pvId: Property<Long> = objectFactory.property()
        val claimName: Property<String> = objectFactory.property()
        val sizeInGb: Property<Int> = objectFactory.property()
    }

open class DataImageMountSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        val dataImageAccountId: Property<Long> = objectFactory.property()
        val dataImageId: Property<Long> = objectFactory.property()
        val targetPath: Property<String> = objectFactory.property()

        val dmId: Property<Long> = objectFactory.property()
        val dataImage: Property<String> = objectFactory.property()
        val dataImageName: Property<String> = objectFactory.property()
        val sourcePath: Property<String> = objectFactory.property()
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
        
        /**
         * Prediction timeout, in seconds.
         */
        val predictTimeoutSec: Property<Long> = objectFactory.property<Long>().convention(30)
        
        /**
         * Fit timeout, in seconds.
         */
        val fitTimeoutSec: Property<Long> = objectFactory.property<Long>().convention(600)
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
        val cpuRequest: Property<String> = objectFactory.property()
        
        /**
         * Amount of RAM available to the instance.
         * If the instance exceeds the limit, it will be stopped.
         */
        val memoryLimit: Property<String> = objectFactory.property()
        
        /**
         * Amount of disk memory available to the instance.
         * If the instance exceeds the limit, it will be stopped.
         */
        val ephemeralDiskLimit: Property<String> = objectFactory.property()
        
        /**
         * Whether GPU is requested.
         * Multiple instances can share one GPU.
         */
        val gpuRequested: Property<Boolean> = objectFactory.property()
    }

/**
 * Retry the request transparently to the user if the service returns an error or times out.
 */
open class RetriesConfigSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        /**
         * Maximum number of retry attempts.
         */
        val maxRetries: Property<Int> = objectFactory.property()
        
        /**
         * Timeouts in milliseconds.
         * If a response is not yet received, the platform sends repeated requests to the service
         * after the specified intervals from the first request.
         */
        val timeoutsMs: ListProperty<Long> = objectFactory.listProperty()
        
        /**
         * Maximum number of retries per instance.
         * Recommended value: 1
         */
        val maxRetriesPerInstance: Property<Int> = objectFactory.property()
    }

/**
 * Combine requests into batches to optimize resource usage.
 * The platform accumulates requests during the wait time or until the batch size is reached,
 * then forms a batch and sends it to the service for processing.
 * Only for services that provide the predictBatch method.
 */
open class BatchesConfigSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        /**
         * Maximum number of requests in a batch.
         * 0 or 1 means batching is disabled.
         */
        val batchSize: Property<Int> = objectFactory.property()
        
        /**
         * Maximum wait time for batch accumulation, in milliseconds.
         */
        val timeWaitMs: Property<Long> = objectFactory.property()
        
        /**
         * Maximum request size limit for adding to batch, in bytes.
         * Requests that are too large will be immediately sent for processing.
         */
        val maxLengthToSkip: Property<Long> = objectFactory.property()
    }

/**
 * Save service responses in cache for reuse.
 */
open class CachingSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        val enabled: Property<Boolean> = objectFactory.property()
        
        /**
         * URL for connecting to the MongoDB database where the cache will be stored.
         * Create a database in any cloud service or contact support to create a database in Caila.
         */
        val mongoUri: Property<String> = objectFactory.property()
        
        /**
         * Collection name.
         */
        val collectionName: Property<String> = objectFactory.property()
        
        /**
         * Records limit.
         */
        val recordsLimit: Property<Long> = objectFactory.property()
    }

/**
 * Process high-priority requests first.
 * Request priority is passed in the Z-Priority header.
 */
open class PriorityQueueSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        val enabled: Property<Boolean> = objectFactory.property()
        
        /**
         * How many requests can be sent in parallel to one service instance.
         * The platform manages request ordering only before sending them.
         */
        val concurrencyLevel: Property<Int> = objectFactory.property()
    }

open class ScheduledInstanceCountSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        val startTime: Property<TimePointSpec> = objectFactory.property()
        val endTime: Property<TimePointSpec> = objectFactory.property()
        val minInstances: Property<Int> = objectFactory.property()
        val maxInstances: Property<Int> = objectFactory.property()
    }

open class TimePointSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        val hour: Property<Int> = objectFactory.property()
        val minute: Property<Int> = objectFactory.property()
        val second: Property<Int> = objectFactory.property()
        val nano: Property<Int> = objectFactory.property()
    }

open class AutoScalingConfigurationSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        val minInstanceCount: Property<Int> = objectFactory.property()
        val maxInstanceCount: Property<Int> = objectFactory.property()
        val scheduledInstanceCountSettings: ListProperty<ScheduledInstanceCountSpec> =
            objectFactory.listProperty()
        val cooldownDurationMinutes: Property<Int> = objectFactory.property()
        val scaleUpRequestsPerMinuteThreshold: Property<Int> = objectFactory.property()
        val scaleDownRequestsPerMinuteThreshold: Property<Int> = objectFactory.property()
        val scaleUpLatencyThresholdMs: Property<Int> = objectFactory.property()
        val scaleDownLatencyThresholdMs: Property<Int> = objectFactory.property()
        val scaleUpCpuThresholdMilliCores: Property<Int> = objectFactory.property()
        val scaleDownCpuThresholdMilliCores: Property<Int> = objectFactory.property()
        val scaleUpActiveRequestsThreshold: Property<Int> = objectFactory.property()
        val scaleDownActiveRequestsThreshold: Property<Int> = objectFactory.property()
        val enabled: Property<Boolean> = objectFactory.property()
    }

open class HttpSettingsSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        val isHttpEnabled: Property<Boolean> = objectFactory.property<Boolean>().convention(true)
        
        /**
         * Port that the application web server listens on.
         */
        val httpPort: Property<Int> = objectFactory.property()
        
        /**
         * Path for availability check (healthcheck endpoint).
         */
        val mainPageEndpoint: Property<String> = objectFactory.property()
        
        /**
         * Enable if the service does not support gRPC API.
         * Disable if the service is developed based on MLP SDK.
         */
        val httpInterfaceOnly: Property<Boolean> = objectFactory.property<Boolean>().convention(true)
    }

/**
 * Store request and response history (accessible via API).
 */
open class ArchiveSettingsSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        val enabled: Property<Boolean> = objectFactory.property()
        
        /**
         * Number of stored requests.
         */
        val numberOfArchivedRequests: Property<Long> = objectFactory.property()
        
        /**
         * Whether to use encryption.
         */
        val encryptionEnabled: Property<Boolean> = objectFactory.property()
        
        /**
         * Encryption key (RSA PEM public key).
         */
        val rsaPemPublicKey: Property<String> = objectFactory.property()
    }

open class PublicSettingsSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        val isPublic: Property<Boolean> = objectFactory.property<Boolean>().convention(false)
        val featured: Property<Boolean> = objectFactory.property<Boolean>().convention(false)
        val featuredListOrder: Property<Int> = objectFactory.property<Int>().convention(0)
        val hidden: Property<Boolean> = objectFactory.property<Boolean>().convention(false)
        val publicTestingAllowed: Property<Boolean> = objectFactory.property<Boolean>().convention(false)
        val showPersonalDataDisclaimer: Property<Boolean> = objectFactory.property<Boolean>().convention(false)
    }
