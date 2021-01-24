package com.justai.jaicf.gateway

import com.justai.jaicf.context.RequestContext

/**
 * Base class for all channels able to process requests from external service.
 * Allows to send [BotGatewayEventRequest] or [BotGatewayQueryRequest] with client identifier to implementations at any time.
 *
 * @see BotGatewayRequest
 *
 * */
abstract class BotGateway {
    abstract fun processGatewayRequest(request: BotGatewayRequest, requestContext: RequestContext)

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    protected fun getRequestTemplateFromResources(request: BotGatewayRequest, resourceName: String) =
        this.javaClass.getResource(resourceName).readText()
            .replace("{{ clientId }}", request.clientId)
            .replace("{{ text }}", request.input)
}
