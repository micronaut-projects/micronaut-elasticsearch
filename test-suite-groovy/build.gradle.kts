plugins {
    id("groovy")
    id("io.micronaut.build.internal.elasticsearch-tests")
}

dependencies {
    testCompileOnly(mn.micronaut.inject.groovy)

    testImplementation(platform(mn.micronaut.core.bom))
    testImplementation(mnTest.micronaut.test.spock)

    // tag::testcontainers-dependencies[]
    // end::testcontainers-dependencies[]
    testImplementation(libs.apache.http.client)
    testImplementation(libs.apache.http.async.client)
    testImplementation(projects.micronautElasticsearch)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnTest.junit.platform.launcher)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.elasticsearch)
}

