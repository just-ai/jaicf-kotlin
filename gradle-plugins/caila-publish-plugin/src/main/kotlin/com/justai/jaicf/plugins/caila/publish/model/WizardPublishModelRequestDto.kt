package com.justai.jaicf.plugins.caila.publish.model

import com.justai.jaicf.plugins.caila.publish.extension.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class WizardPublishModelRequestDto(
    val modelTypeStep: ModelTypeStep,
    val serviceDescriptionStep: ServiceDescriptionStep,
    val imageStep: ImageStep,
    val serviceConfigurationStep: ServiceConfigurationStep,
    val hostingStep: HostingStep
)

@Serializable
data class ModelTypeStep(
    val modelType: String,
    val enableHttpInterface: Boolean
)

@Serializable
data class ServiceDescriptionStep(
    val displayAuthor: String? = null,
    val displayName: String? = null,
    val modelName: String,
    val shortDescription: String? = null,
    val prototype: Boolean = false,
    val taskType: String,
    val languages: List<String> = emptyList(),
    val publicSettingsData: PublicSettingsData
)

@Serializable
data class PublicSettingsData(
    val isPublic: Boolean,
    val featured: Boolean,
    val hidden: Boolean,
    val publicTestingAllowed: Boolean,
    val showPersonalDataDisclaimer: Boolean
)

@Serializable
data class ImageStep(
    val newImage: NewImage? = null,
    val existingImage: ExistingImage? = null
)

@Serializable
data class NewImage(
    val imageSourceType: String = "DOCKER",
    val image: String
)

@Serializable
data class ExistingImage(
    val imageAccountId: Int,
    val imageId: Int
)

@Serializable
data class ServiceConfigurationStep(
    val httpPort: Int,
    val healthCheckEndpoint: String,
    val timeouts: WizardTimeouts,
    val env: String? = null
)

@Serializable
data class WizardTimeouts(
    val podStartTimeoutSec: Int
)

@Serializable
data class HostingStep(
    val existingResourceGroupName: String? = null,
    val newResourceGroup: NewResourceGroup? = null,
    val resourceLimits: WizardResourceLimits
)

@Serializable
data class NewResourceGroup(
    val name: String
)

@Serializable
data class WizardResourceLimits(
    val cpuRequest: String,
    val memoryLimit: String,
    val ephemeralDiskLimit: String,
    val gpuRequested: Boolean
)

/**
 * Creates WizardPublishModelRequestDto from CailaModelSpec
 */
fun createWizardRequest(
    dockerImage: String,
    accountId: Int,
    spec: CailaModelSpec,
    s3EnvVars: Map<String, String>?
): WizardPublishModelRequestDto {
    val existingEnvMap = mutableMapOf<String, String>()

    spec.environmentVariables.orNull?.variables?.orNull?.let { vars ->
        existingEnvMap.putAll(vars)
    }

    s3EnvVars?.let { existingEnvMap.putAll(it) }

    val publicSettings = spec.publicSettings.orNull
    val httpSettings = spec.http.orNull
    val resourceLimits = spec.resourceLimits.orNull
    val timeouts = spec.timeouts.orNull

    return WizardPublishModelRequestDto(
        modelTypeStep = ModelTypeStep(
            modelType = spec.modelType.getOrElse("WEB_APPLICATION"),
            enableHttpInterface = httpSettings?.isHttpEnabled?.getOrElse(true) ?: true
        ),
        serviceDescriptionStep = ServiceDescriptionStep(
            displayAuthor = spec.displayAuthor.orNull,
            displayName = spec.displayName.orNull,
            modelName = spec.name.get(),
            shortDescription = spec.shortDescription.orNull,
            prototype = false,
            taskType = spec.taskType.get(),
            languages = spec.languages.orNull ?: emptyList(),
            publicSettingsData = PublicSettingsData(
                isPublic = publicSettings?.isPublic?.getOrElse(false) ?: false,
                featured = publicSettings?.featured?.getOrElse(false) ?: false,
                hidden = publicSettings?.hidden?.getOrElse(false) ?: false,
                publicTestingAllowed = publicSettings?.publicTestingAllowed?.getOrElse(false) ?: false,
                showPersonalDataDisclaimer = publicSettings?.showPersonalDataDisclaimer?.getOrElse(false) ?: false
            )
        ),
        imageStep = ImageStep(
            newImage = NewImage(
                imageSourceType = "DOCKER",
                image = dockerImage
            )
        ),
        serviceConfigurationStep = ServiceConfigurationStep(
            httpPort = httpSettings?.port?.get() ?: 8080,
            healthCheckEndpoint = httpSettings?.mainPageEndpoint?.getOrElse("/health") ?: "/health",
            timeouts = WizardTimeouts(
                podStartTimeoutSec = timeouts?.podStartTimeoutSec?.get()?.toInt() ?: 120
            ),
            env = if (existingEnvMap.isNotEmpty()) Json.encodeToString(existingEnvMap) else null
        ),
        hostingStep = HostingStep(
            existingResourceGroupName = spec.resourceGroup.orNull ?: "free-pool-quota-for-$accountId",
            newResourceGroup = null,
            resourceLimits = WizardResourceLimits(
                cpuRequest = resourceLimits?.cpuRequest?.getOrElse("100m") ?: "100m",
                memoryLimit = resourceLimits?.memoryLimit?.getOrElse("500Mi") ?: "500Mi",
                ephemeralDiskLimit = resourceLimits?.ephemeralDiskLimit?.getOrElse("100Mi") ?: "100Mi",
                gpuRequested = resourceLimits?.gpuRequested?.getOrElse(false) ?: false
            )
        )
    )
}