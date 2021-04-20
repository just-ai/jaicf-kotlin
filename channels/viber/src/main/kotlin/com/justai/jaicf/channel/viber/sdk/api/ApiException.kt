package com.justai.jaicf.channel.viber.sdk.api

import com.justai.jaicf.channel.viber.sdk.api.request.ErrorResponse
import java.util.concurrent.ExecutionException

class ApiException(response: ErrorResponse) : ExecutionException(response.statusMessage)
