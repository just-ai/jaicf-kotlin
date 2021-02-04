plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    core()
    api("com.google.cloud:google-cloud-dialogflow:0.109.0-alpha")
    api("io.grpc:grpc-okhttp:1.24.0")
}
