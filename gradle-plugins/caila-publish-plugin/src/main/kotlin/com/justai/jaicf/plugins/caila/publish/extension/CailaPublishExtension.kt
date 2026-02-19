package com.justai.jaicf.plugins.caila.publish.extension

import com.bmuschko.gradle.docker.DockerExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

const val CAILA_BASE_URL = "https://caila.io/api/mlpcore"

open class CailaPublishExtension @Inject constructor(
    private val project: Project,
    objectFactory: ObjectFactory,
) {
    val url: Property<String> = objectFactory.property<String>().convention(CAILA_BASE_URL)

    // Top-level properties for simplified configuration
    val modelName: Property<String> = objectFactory.property()
    val httpPort: Property<Int> = objectFactory.property()

    // Optionally: Image access settings
    val imageAccessMode: Property<String> = objectFactory.property<String>().convention("private")
    val allowDestructiveUpdate: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    // Optionally: Model metadata
    val displayName: Property<String> = objectFactory.property()
    val displayAuthor: Property<String> = objectFactory.property()
    val shortDescription: Property<String> = objectFactory.property()
    val taskType: Property<String> = objectFactory.property<String>().convention("custom")

    val httpClient: HttpClientSpec = objectFactory.newInstance(HttpClientSpec::class.java)
    val image: CailaImageSpec = objectFactory.newInstance(CailaImageSpec::class.java)
    val model: CailaModelSpec = objectFactory.newInstance(CailaModelSpec::class.java)

    fun docker(configuration: DockerExtension.() -> Unit) {
        project.extensions.configure(DockerExtension::class.java, configuration)
    }

    fun httpClient(action: Action<HttpClientSpec>) {
        action.execute(httpClient)
    }

    fun image(action: Action<CailaImageSpec>) {
        action.execute(image)
    }

    fun model(action: Action<CailaModelSpec>) {
        action.execute(model)
    }
}