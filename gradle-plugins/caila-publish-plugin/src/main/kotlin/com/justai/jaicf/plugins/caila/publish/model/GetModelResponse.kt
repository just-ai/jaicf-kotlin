package com.justai.jaicf.plugins.caila.publish.model

import kotlinx.serialization.Serializable

@Serializable
data class GetModelResponse(
    val paging: Paging,
    val records: List<ModelRecord>,
)

@Serializable
data class ModelRecord(
    val id: Id,
    val modelAccountName: String = "",
    val modelAccountDisplayName: String = "",
    val modelName: String = "",
    val displayName: String = "",
    val displayAuthor: String = "",
    val imageAccountId: Int = 0,
    val imageId: Int,
    val image: ImageData? = null,
    val modelGroupId: Int? = null,
    val modelGroupName: String? = null,
    val trainingDatasetAccountId: Int? = null,
    val trainingDatasetId: Int? = null,
    val trainingDataset: TrainingDataset? = null,
    val trainingDatasetType: String? = null,
    val trainingFitConfigId: Int? = null,
    val trainingFitConfig: TrainingFitConfig? = null,
    val fitTemplateModelId: Int? = null,
    val composite: Boolean = false,
    val prototype: Boolean = false,
    val supportedTemplates: List<Int>? = null,
    val rejectRequestsIfInactive: Boolean = false,
    val taskType: String = "",
    val trainingModelAccountId: Int? = null,
    val trainingModelId: Int? = null,
    val trainingModelName: String? = null,
    val trainingType: String? = null,
    val config: String = "",
    val env: String = "",
    val additionalFlags: List<String>? = null,
    val fittable: Boolean = false,
    val hostingType: String = "",
    val persistentVolumes: List<PersistentVolume>? = null,
    val dataImageMounts: List<DataImageMount>? = null,
    val resourceGroup: String = "",
    val timeouts: Timeouts? = null,
    val resourceLimits: ResourceLimits? = null,
    val retriesConfig: RetriesConfig? = null,
    val batchesConfig: BatchesConfig? = null,
    val caching: Caching? = null,
    val priorityQueue: PriorityQueue? = null,
    val autoScalingConfiguration: AutoScalingConfiguration? = null,
    val shortDescription: String = "",
    val languages: List<String>? = null,
    val minInstancesCount: Int = 0,
    val publicSettings: PublicSettings? = null,
    val billingSettings: BillingSettings? = null,
    val httpSettings: HttpSettings? = null,
    val archiveSettings: ArchiveSettings? = null,
    val restrictedImageAccess: Boolean = false,
    val lastActivity: Long? = null,
    val favorite: Boolean = false,
    val state: String = "",
    val deploymentPatch: String? = null
)

@Serializable
data class Id(
    val accountId: Int,
    val modelId: Int
)

@Serializable
data class ImageData(
    val id: ImageId,
    val name: String,
    val imageAccountName: String,
    val image: String,
    val accessMode: String
)

@Serializable
data class ImageId(
    val accountId: Int,
    val imageId: Int
)

@Serializable
data class TrainingDataset(
    val id: DatasetId,
    val name: String,
    val datasetAccountName: String,
    val description: String,
    val dataType: String,
    val accessMode: String,
    val warning: String
)

@Serializable
data class DatasetId(
    val accountId: Int,
    val datasetId: Int
)

@Serializable
data class TrainingFitConfig(
    val id: FitConfigId,
    val name: String,
    val isDefault: Boolean,
    val isManual: Boolean,
    val config: String
)

@Serializable
data class FitConfigId(
    val accountId: Int,
    val modelId: Int,
    val configId: Int
)

@Serializable
data class PersistentVolume(
    val pvId: Int,
    val mountPath: String,
    val claimName: String,
    val sizeInGb: Int,
    val storageClass: String
)

@Serializable
data class DataImageMount(
    val dmId: Int,
    val dataImageAccountId: Int,
    val dataImageId: Int,
    val dataImage: String,
    val dataImageName: String,
    val sourcePath: String,
    val targetPath: String
)

@Serializable
data class Timeouts(
    val podStartTimeoutSec: Int? = null,
    val predictTimeoutSec: Int? = null,
    val fitTimeoutSec: Int? = null
)

@Serializable
data class ResourceLimits(
    val cpuRequest: String,
    val memoryLimit: String,
    val ephemeralDiskLimit: String,
    val gpuRequested: Boolean
)

@Serializable
data class RetriesConfig(
    val maxRetries: Int,
    val timeoutsMs: List<Int>,
    val maxRetriesPerInstance: Int
)

@Serializable
data class BatchesConfig(
    val batchSize: Int,
    val timeWaitMs: Int,
    val maxLengthToSkip: Int
)

@Serializable
data class Caching(
    val enabled: Boolean,
    val mongoUri: String,
    val collectionName: String,
    val recordsLimit: Int
)

@Serializable
data class PriorityQueue(
    val enabled: Boolean,
    val concurrencyLevel: Int
)

@Serializable
data class AutoScalingConfiguration(
    val minInstanceCount: Int? = null,
    val maxInstanceCount: Int? = null,
    val scheduledInstanceCountSettings: List<ScheduledInstanceCountSetting>? = null,
    val cooldownDurationMinutes: Int? = null,
    val scaleUpRequestsPerMinuteThreshold: Int? = null,
    val scaleDownRequestsPerMinuteThreshold: Int? = null,
    val scaleUpLatencyThresholdMs: Int? = null,
    val scaleDownLatencyThresholdMs: Int? = null,
    val scaleUpCpuThresholdMilliCores: Int? = null,
    val scaleDownCpuThresholdMilliCores: Int? = null,
    val scaleUpActiveRequestsThreshold: Int? = null,
    val scaleDownActiveRequestsThreshold: Int? = null,
    val enabled: Boolean = false
)

@Serializable
data class ScheduledInstanceCountSetting(
    val startTime: TimePoint,
    val endTime: TimePoint,
    val minInstances: Int,
    val maxInstances: Int
)

@Serializable
data class TimePoint(
    val hour: Int,
    val minute: Int,
    val second: Int,
    val nano: Int
)

@Serializable
data class PublicSettings(
    val isPublic: Boolean,
    val featured: Boolean,
    val featuredListOrder: Int,
    val hidden: Boolean,
    val publicTestingAllowed: Boolean,
    val showPersonalDataDisclaimer: Boolean
)

@Serializable
data class BillingSettings(
    val isBillingEnabled: Boolean = false,
    val billingUnit: String? = null,
    val billingUnitPriceInNanoToken: Long? = null,
    val billingUnitPriceInCurrency: Double? = null,
    val freeUnitQuota: Long? = null
)

@Serializable
data class HttpSettings(
    val isHttpEnabled: Boolean = false,
    val httpPort: Int? = null,
    val mainPageEndpoint: String? = null,
    val httpInterfaceOnly: Boolean = false
)

@Serializable
data class ArchiveSettings(
    val enabled: Boolean = false,
    val numberOfArchivedRequests: Int = 0,
    val encryptionEnabled: Boolean = false,
    val rsaPemPublicKey: String? = null
)