package com.justai.jaicf.plugins.caila.publish.model

import com.justai.jaicf.plugins.caila.publish.extension.*
import kotlinx.serialization.Serializable

@Serializable
data class PublishModelRequestDto(
    val modelName: String,
    val imageId: Int,
    val imageAccountId: Int,
    val taskType: String,
    val modelType: String? = null,
    val displayName: String? = null,
    val displayAuthor: String? = null,
    val rejectRequestsIfInactive: Boolean? = null,
    val config: String? = null,
    val env: String? = null,
    val fittable: Boolean? = null,
    val hostingType: String? = null,
    val resourceGroup: String? = null,
    val shortDescription: String? = null,
    val minInstancesCount: Int = 1,
    val startTimeSec: Double? = null,
    val additionalFlags: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val aliases: List<String> = emptyList(),
    val persistentVolumes: List<PersistentVolumeDto> = emptyList(),
    val dataImageMounts: List<DataImageMountDto> = emptyList(),
    val timeouts: TimeoutsDto? = null,
    val resourceLimits: ResourceLimitsDto? = null,
    val retriesConfig: RetriesConfigDto? = null,
    val batchesConfig: BatchesConfigDto? = null,
    val caching: CachingDto? = null,
    val priorityQueue: PriorityQueueDto? = null,
    val autoScalingConfiguration: AutoScalingConfigurationDto? = null,
    val httpSettings: HttpSettingsDto? = null,
    val archiveSettings: ArchiveSettingsDto? = null,
    val publicSettings: PublicSettingsDto? = null,
) {
    constructor(
        imageId: Int,
        imageAccountId: Int,
        spec: CailaModelSpec,
    ) : this(
        modelName = spec.name.get(),
        imageId = imageId,
        imageAccountId = imageAccountId,
        taskType = spec.taskType.get(),
        modelType = spec.modelType.orNull,
        displayName = spec.displayName.orNull,
        displayAuthor = spec.displayAuthor.orNull,
        rejectRequestsIfInactive = spec.rejectRequestsIfInactive.orNull,
        config = spec.config.orNull,
        env = combineEnvVariables(spec.env.orNull, spec.environmentVariables.orNull?.toEnvString()),
        fittable = spec.fittable.orNull,
        hostingType = spec.hostingType.orNull,
        resourceGroup = spec.resourceGroup.orNull,
        shortDescription = spec.shortDescription.orNull,
        minInstancesCount = spec.minInstancesCount.orElse(1).get(),
        startTimeSec = spec.startTimeSec.orNull,
        additionalFlags = spec.additionalFlags.orNull ?: emptyList(),
        languages = spec.languages.orNull ?: emptyList(),
        aliases = spec.aliases.orNull ?: emptyList(),
        persistentVolumes = spec.persistentVolumes.orNull?.map { PersistentVolumeDto(it) } ?: emptyList(),
        dataImageMounts = spec.dataImageMounts.orNull?.map { DataImageMountDto(it) } ?: emptyList(),
        timeouts = spec.timeouts.orNull?.let { TimeoutsDto(it) },
        resourceLimits = spec.resourceLimits.orNull?.let { ResourceLimitsDto(it) },
        retriesConfig = spec.retriesConfig.orNull?.let { RetriesConfigDto(it) },
        batchesConfig = spec.batchesConfig.orNull?.let { BatchesConfigDto(it) },
        caching = spec.caching.orNull?.let { CachingDto(it) },
        priorityQueue = spec.priorityQueue.orNull?.let { PriorityQueueDto(it) },
        autoScalingConfiguration = spec.autoScalingConfiguration.orNull?.let { AutoScalingConfigurationDto(it) },
        httpSettings = spec.http.orNull?.let { HttpSettingsDto(it) },
        archiveSettings = spec.archiveSettings.orNull?.let { ArchiveSettingsDto(it) },
        publicSettings = spec.publicSettings.orNull?.let { PublicSettingsDto(it) },
    )
}

@Serializable
data class TimePointDto(
    val hour: Int? = null,
    val minute: Int? = null,
    val second: Int? = null,
    val nano: Int? = null,
) {
    constructor(spec: TimePointSpec) : this(
        hour = spec.hour.orNull,
        minute = spec.minute.orNull,
        second = spec.second.orNull,
        nano = spec.nano.orNull,
    )
}

@Serializable
data class PersistentVolumeDto(
    val mountPath: String,
    val storageClass: String,
    val pvId: Long? = null,
    val claimName: String? = null,
    val sizeInGb: Int? = null,
) {
    constructor(spec: PersistentVolumeSpec) : this(
        mountPath = spec.mountPath.get(),
        storageClass = spec.storageClass.get(),
        pvId = spec.pvId.orNull,
        claimName = spec.claimName.orNull,
        sizeInGb = spec.sizeInGb.orNull,
    )
}

@Serializable
data class DataImageMountDto(
    val dataImageAccountId: Long,
    val dataImageId: Long,
    val targetPath: String,
    val dmId: Long? = null,
    val dataImage: String? = null,
    val dataImageName: String? = null,
    val sourcePath: String? = null,
) {
    constructor(spec: DataImageMountSpec) : this(
        dataImageAccountId = spec.dataImageAccountId.get(),
        dataImageId = spec.dataImageId.get(),
        targetPath = spec.targetPath.get(),
        dmId = spec.dmId.orNull,
        dataImage = spec.dataImage.orNull,
        dataImageName = spec.dataImageName.orNull,
        sourcePath = spec.sourcePath.orNull,
    )
}

@Serializable
data class TimeoutsDto(
    val podStartTimeoutSec: Long,
    val predictTimeoutSec: Long,
    val fitTimeoutSec: Long? = null,
) {
    constructor(spec: TimeoutsSpec) : this(
        podStartTimeoutSec = spec.podStartTimeoutSec.get(),
        predictTimeoutSec = spec.predictTimeoutSec.get(),
        fitTimeoutSec = spec.fitTimeoutSec.orNull,
    )
}

@Serializable
data class ResourceLimitsDto(
    val cpuRequest: String,
    val memoryLimit: String,
    val ephemeralDiskLimit: String,
    val gpuRequested: Boolean,
) {
    constructor(spec: ResourceLimitsSpec) : this(
        cpuRequest = spec.cpuRequest.get(),
        memoryLimit = spec.memoryLimit.get(),
        ephemeralDiskLimit = spec.ephemeralDiskLimit.get(),
        gpuRequested = spec.gpuRequested.get(),
    )
}

@Serializable
data class RetriesConfigDto(
    val maxRetries: Int,
    val timeoutsMs: List<Long>,
    val maxRetriesPerInstance: Int? = null,
) {
    constructor(spec: RetriesConfigSpec) : this(
        maxRetries = spec.maxRetries.get(),
        timeoutsMs = spec.timeoutsMs.get(),
        maxRetriesPerInstance = spec.maxRetriesPerInstance.orNull,
    )
}

@Serializable
data class BatchesConfigDto(
    val batchSize: Int,
    val timeWaitMs: Long,
    val maxLengthToSkip: Long? = null,
) {
    constructor(spec: BatchesConfigSpec) : this(
        batchSize = spec.batchSize.get(),
        timeWaitMs = spec.timeWaitMs.get(),
        maxLengthToSkip = spec.maxLengthToSkip.orNull,
    )
}

@Serializable
data class CachingDto(
    val enabled: Boolean,
    val mongoUri: String,
    val collectionName: String,
    val recordsLimit: Long,
) {
    constructor(spec: CachingSpec) : this(
        enabled = spec.enabled.get(),
        mongoUri = spec.mongoUri.get(),
        collectionName = spec.collectionName.get(),
        recordsLimit = spec.recordsLimit.get(),
    )
}

/**
 * Process high-priority requests first. Request priority is passed in the Z-Priority header.
 */
@Serializable
data class PriorityQueueDto(
    val enabled: Boolean,
    /**
     * How many requests can be sent in parallel to one service instance.
     * The platform manages request ordering only before sending them.
     */
    val concurrencyLevel: Int,
) {
    constructor(spec: PriorityQueueSpec) : this(
        enabled = spec.enabled.get(),
        concurrencyLevel = spec.concurrencyLevel.get(),
    )
}

@Serializable
data class ScheduledInstanceCountDto(
    val startTime: TimePointDto? = null,
    val endTime: TimePointDto? = null,
    val minInstances: Int? = null,
    val maxInstances: Int? = null,
) {
    constructor(spec: ScheduledInstanceCountSpec) : this(
        startTime = spec.startTime.orNull?.let { TimePointDto(it) },
        endTime = spec.endTime.orNull?.let { TimePointDto(it) },
        minInstances = spec.minInstances.orNull,
        maxInstances = spec.maxInstances.orNull,
    )
}

@Serializable
data class AutoScalingConfigurationDto(
    val enabled: Boolean,
    val minInstanceCount: Int,
    val maxInstanceCount: Int? = null,
    val scheduledInstanceCountSettings: List<ScheduledInstanceCountDto>? = null,
    val cooldownDurationMinutes: Int? = null,
    val scaleUpRequestsPerMinuteThreshold: Int? = null,
    val scaleDownRequestsPerMinuteThreshold: Int? = null,
    val scaleUpLatencyThresholdMs: Int? = null,
    val scaleDownLatencyThresholdMs: Int? = null,
    val scaleUpCpuThresholdMilliCores: Int? = null,
    val scaleDownCpuThresholdMilliCores: Int? = null,
    val scaleUpActiveRequestsThreshold: Int? = null,
    val scaleDownActiveRequestsThreshold: Int? = null,
) {
    constructor(spec: AutoScalingConfigurationSpec) : this(
        enabled = spec.enabled.get(),
        minInstanceCount = spec.minInstanceCount.get(),
        maxInstanceCount = spec.maxInstanceCount.orNull,
        scheduledInstanceCountSettings = spec.scheduledInstanceCountSettings.orNull?.map { ScheduledInstanceCountDto(it) },
        cooldownDurationMinutes = spec.cooldownDurationMinutes.orNull,
        scaleUpRequestsPerMinuteThreshold = spec.scaleUpRequestsPerMinuteThreshold.orNull,
        scaleDownRequestsPerMinuteThreshold = spec.scaleDownRequestsPerMinuteThreshold.orNull,
        scaleUpLatencyThresholdMs = spec.scaleUpLatencyThresholdMs.orNull,
        scaleDownLatencyThresholdMs = spec.scaleDownLatencyThresholdMs.orNull,
        scaleUpCpuThresholdMilliCores = spec.scaleUpCpuThresholdMilliCores.orNull,
        scaleDownCpuThresholdMilliCores = spec.scaleDownCpuThresholdMilliCores.orNull,
        scaleUpActiveRequestsThreshold = spec.scaleUpActiveRequestsThreshold.orNull,
        scaleDownActiveRequestsThreshold = spec.scaleDownActiveRequestsThreshold.orNull,
    )
}

@Serializable
data class HttpSettingsDto(
    val isHttpEnabled: Boolean? = null,
    val httpPort: Int? = null,
    val mainPageEndpoint: String? = null,
    val httpInterfaceOnly: Boolean? = null,
) {
    constructor(spec: HttpSettingsSpec) : this(
        isHttpEnabled = spec.isHttpEnabled.orNull,
        httpPort = spec.port.orNull,
        mainPageEndpoint = spec.mainPageEndpoint.orNull,
        httpInterfaceOnly = spec.interfaceOnly.orNull,
    )
}

@Serializable
data class ArchiveSettingsDto(
    val enabled: Boolean? = null,
    val numberOfArchivedRequests: Long? = null,
    val encryptionEnabled: Boolean? = null,
    val rsaPemPublicKey: String? = null,
) {
    constructor(spec: ArchiveSettingsSpec) : this(
        enabled = spec.enabled.orNull,
        numberOfArchivedRequests = spec.numberOfArchivedRequests.orNull,
        encryptionEnabled = spec.encryptionEnabled.orNull,
        rsaPemPublicKey = spec.rsaPemPublicKey.orNull,
    )
}

@Serializable
data class PublicSettingsDto(
    val isPublic: Boolean,
    val featured: Boolean,
    val featuredListOrder: Int,
    val hidden: Boolean,
    val publicTestingAllowed: Boolean,
    val showPersonalDataDisclaimer: Boolean,
) {
    constructor(spec: PublicSettingsSpec) : this(
        isPublic = spec.isPublic.get(),
        featured = spec.featured.get(),
        featuredListOrder = spec.featuredListOrder.get(),
        hidden = spec.hidden.get(),
        publicTestingAllowed = spec.publicTestingAllowed.get(),
        showPersonalDataDisclaimer = spec.showPersonalDataDisclaimer.get(),
    )
}

/**
 * Combines legacy env string and new environmentVariables spec into a single env string.
 * If both are present, they are concatenated with a newline.
 */
private fun combineEnvVariables(legacyEnv: String?, envVariablesString: String?): String? {
    return when {
        legacyEnv != null && envVariablesString != null -> "$legacyEnv\n$envVariablesString"
        legacyEnv != null -> legacyEnv
        envVariablesString != null -> envVariablesString
        else -> null
    }
}