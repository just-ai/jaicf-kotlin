package com.justai.jaicf.channel.invocationapi

import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.logging.WithLogger
import java.util.*
import kotlin.random.Random

/**
 * Base class for all channels able to process requests from external service.
 * Any class implementing [InvocableBotChannel] receives method [processInvocation] allowing to process query or event requests with client identifier from any external source.
 *
 * @see InvocationRequest
 * @see InvocationQueryRequest
 * @see InvocationEventRequest
 * */
interface InvocableBotChannel : WithLogger {
    /**
     * Processes an [InvocationRequest]
     *
     * @param request an [InvocationRequest]
     * @param requestContext additional general request's data that can be used during the request processing
     *
     * @see InvocableBotChannel
     */
    fun processInvocation(request: InvocationRequest, requestContext: RequestContext)

    /**
     * Provides a messageId for substitution in request template
     * */
    fun provideMessageId(): String = UUID.randomUUID().toString()

    /**
     * Provides a timestamp for substitution in request template
     * */
    fun provideTimestamp(): String = System.currentTimeMillis().toString()

    /**
     * Loads a channel request template from resources and substitutes essential parameters (clientId, input) in request
     *
     * @return serialized JSON with substituted request parameters
     * */
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun getRequestTemplateFromResources(request: InvocationRequest, resourceName: String) =
        this.javaClass.getResource(resourceName).readText()
            .replace("{{ clientId }}", request.clientId)
            .replace("{{ text }}", request.input)

            .replace("\"{{ timestamp }}\"", provideTimestamp())
            .replace("{{ messageId }}", provideMessageId())

            .replace("\"{{ randomInt }}\"", randomInt.toString())
            .replace("\"{{ randomLong }}\"", randomLong.toString())

            .also { logger.trace("Generated template request: $it") }
}

/**
 * Processes request ktor routing extensions or servlet.
 *
 * @param queryParams
 * @param requestData a stringified data sent with request.
 *
 * @see InvocationServlet
 * @see botInvocationRouting
 * */
internal fun InvocableBotChannel.processInvocation(
    queryParams: InvocationQueryParams,
    requestData: String
) = processInvocation(
    request = when (queryParams.type) {
        InvocationRequestType.EVENT -> InvocationEventRequest(queryParams.clientId, queryParams.input, requestData)
        InvocationRequestType.QUERY -> InvocationQueryRequest(queryParams.clientId, queryParams.input, requestData)
    },
    requestContext = RequestContext.fromHttp(requestData.asHttpBotRequest())
)

private val randomInt get() = Random.nextInt()

private val randomLong get() = Random.nextLong()

