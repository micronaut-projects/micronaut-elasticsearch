plugins {
    id("groovy")
    id("io.micronaut.build.internal.elasticsearch-tests")
}

dependencies {
    testCompileOnly(mn.micronaut.inject.groovy)

    testImplementation(platform(mn.micronaut.core.bom))
    testImplementation(mnTest.micronaut.test.spock)

    // tag::testcontainers-dependencies[]
    testImplementation(libs.testcontainers.elasticsearch)
    // end::testcontainers-dependencies[]

    testImplementation(projects.micronautElasticsearch)
    testRuntimeOnly(mn.logback.classic)
}

tasks.named('test') {
    useJUnitPlatform()
    systemProperty 'elasticsearch.version', libs.versions.managed.elasticsearch.get()
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}
