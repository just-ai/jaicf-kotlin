package com.justai.jaicf.activator.dialogflow

import com.google.cloud.dialogflow.v2.QueryParameters
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext

/**
 * Use this interface to dynamically provide Dialogflow with desired query parameters.
 * For example to append session entities:
 *
 * object : QueryParametersProvider {
 *   override fun provideParameters(botContext: BotContext, request: BotRequest): QueryParameters {
 *       return QueryParameters.newBuilder().addSessionEntityTypes(
 *           SessionEntityType.newBuilder().setName("color")
 *               .addEntities(
 *                   EntityType.Entity.newBuilder().setValue("#000").addAllSynonyms(listOf("black", "none", "empty"))
 *               )
 *               .addEntities(
 *                   EntityType.Entity.newBuilder().setValue("#fff").addAllSynonyms(listOf("white", "bright"))
 *               )
 *           ).build()
 *       }
 *   }
 *
 * @see QueryParameters.Builder
 */
interface QueryParametersProvider {

    /**
     * Generates a [QueryParameters] instance to append to the current Dialogflow API request.
     * This method will be invoked for each user query right before the requesting a Dialogflow API.
     *
     * @param botContext current context
     * @param request user request
     * @return current query parameters
     */
    fun provideParameters(botContext: BotContext, request: BotRequest): QueryParameters

    companion object {
        val default = object : QueryParametersProvider {
            override fun provideParameters(botContext: BotContext, request: BotRequest) = QueryParameters.getDefaultInstance()
        }
    }
}