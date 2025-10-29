package com.justai.jaicf.plugins.caila.publish.extension

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class CailaImageSpec
    @Inject
    constructor(
        objectFactory: ObjectFactory,
    ) {
        /**
         * Docker image path on docker.io, eq. `hello-world`
         * */
        @get:Input
        @Optional
        val image: Property<String> = objectFactory.property()

        /**
         * Your Caila image name
         * */
        @get:Input
        val name: Property<String> = objectFactory.property()

        @get:Input
        val accessMode: Property<String> = objectFactory.property()

        /**
         * If true, existing image with same name will be deleted
         * along with attached models before re-publishing. Default false.
         */
        @get:Input
        @Optional
        val allowDestructiveUpdate: Property<Boolean> = objectFactory.property()
    }
