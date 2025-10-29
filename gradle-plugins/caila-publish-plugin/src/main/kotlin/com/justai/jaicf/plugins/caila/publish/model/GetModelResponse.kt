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
    val modelAccountName: String,
    val modelAccountDisplayName: String,
    val modelName: String,
    val displayName: String,
    val displayAuthor: String,
    val imageAccountId: Int,
    val imageId: Int,
    val image: ImageData,
    val modelGroupId: Int,
    val modelGroupName: String,
    val trainingDatasetAccountId: Int,
    val trainingDatasetId: Int,
    val trainingDataset: TrainingDataset,
    val trainingDatasetType: String,
    val trainingFitConfigId: Int,
    val trainingFitConfig: TrainingFitConfig,
    val fitTemplateModelId: Int,
    val composite: Boolean,
    val prototype: Boolean,
    val supportedTemplates: List<Int>,
    val rejectRequestsIfInactive: Boolean,
    val taskType: String,
    val trainingModelAccountId: Int,
    val trainingModelId: Int,
    val trainingModelName: String,
    val trainingType: String,
    val config: String,
    val env: String,
    val additionalFlags: List<String>,
    val fittable: Boolean,
    val hostingType: String,
    val persistentVolumes: List<PersistentVolume>,
    val dataImageMounts: List<DataImageMount>,
    val resourceGroup: String,
    val timeouts: Timeouts,
    val resourceLimits: ResourceLimits,
    val retriesConfig: RetriesConfig,
    val batchesConfig: BatchesConfig,
    val caching: Caching,
    val priorityQueue: PriorityQueue,
    val autoScalingConfiguration: AutoScalingConfiguration,
    val shortDescription: String,
    val languages: List<String>,
    val minInstancesCount: Int,
    val publicSettings: PublicSettings,
    val billingSettings: BillingSettings,
    val httpSettings: HttpSettings,
    val archiveSettings: ArchiveSettings,
    val restrictedImageAccess: Boolean,
    val lastActivity: Long,
    val favorite: Boolean,
    val state: String,
    val deploymentPatch: String
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
    val podStartTimeoutSec: Int,
    val predictTimeoutSec: Int,
    val fitTimeoutSec: Int
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
    val minInstanceCount: Int,
    val maxInstanceCount: Int,
    val scheduledInstanceCountSettings: List<ScheduledInstanceCountSetting>,
    val cooldownDurationMinutes: Int,
    val scaleUpRequestsPerMinuteThreshold: Int,
    val scaleDownRequestsPerMinuteThreshold: Int,
    val scaleUpLatencyThresholdMs: Int,
    val scaleDownLatencyThresholdMs: Int,
    val scaleUpCpuThresholdMilliCores: Int,
    val scaleDownCpuThresholdMilliCores: Int,
    val scaleUpActiveRequestsThreshold: Int,
    val scaleDownActiveRequestsThreshold: Int,
    val enabled: Boolean
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
    val isBillingEnabled: Boolean,
    val billingUnit: String,
    val billingUnitPriceInNanoToken: Long,
    val billingUnitPriceInCurrency: Long,
    val freeUnitQuota: Long
)

@Serializable
data class HttpSettings(
    val isHttpEnabled: Boolean,
    val httpPort: Int,
    val mainPageEndpoint: String,
    val httpInterfaceOnly: Boolean
)

@Serializable
data class ArchiveSettings(
    val enabled: Boolean,
    val numberOfArchivedRequests: Int,
    val encryptionEnabled: Boolean,
    val rsaPemPublicKey: String
)