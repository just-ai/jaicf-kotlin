package com.justai.jaicf.activator.llm.interceptor

interface InterceptorFactory {
    val envFlag: String
    fun create(env: Map<String, String?>): Interceptor
}


object Interceptors {
    private val factories = linkedMapOf<String, InterceptorFactory>() // сохраняем порядок регистрации

    fun register(factory: InterceptorFactory) {
        factories[factory.envFlag] = factory
    }

    fun registerAll(vararg list: InterceptorFactory) {
        list.forEach(::register)
    }

    fun all(): Collection<InterceptorFactory> = factories.values
}


object InterceptorConfigLoader {

    fun registerDefaultInterceptors() {
        Interceptors.run {
            registerAll(
                LoggingInterceptor.Factory(),
                TracingInterceptor.Factory()
            )
        }
    }

    fun fromEnv(env: Map<String, String?> = System.getenv()): List<Interceptor> {
        fun isOn(flag: String) = env[flag]?.equals("true", ignoreCase = true) == true
        registerDefaultInterceptors()
        return Interceptors
            .all()
            .filter { isOn(it.envFlag) }
            .map { it.create(env) }
    }
}