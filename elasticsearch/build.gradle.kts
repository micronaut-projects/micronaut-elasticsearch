plugins {
    id("io.micronaut.build.internal.elasticsearch-module")
}

dependencies {
    annotationProcessor(mn.micronaut.graal)

    compileOnly(libs.graal.svm)
    implementation(mn.micronaut.management)
    api(libs.managed.elasticsearch.java) {
        exclude(group="org.elasticsearch.client", module = "elasticsearch-rest-client")
    }
    implementation(libs.managed.elasticsearch.rest.client) {
        exclude(group="commons-logging", module = "commons-logging")
    }
    runtimeOnly(libs.jcl.over.slf4j)
    api(mn.micronaut.http)

    implementation(mn.micronaut.jackson.databind)

    testImplementation(libs.testcontainers.elasticsearch)
    testImplementation(mn.groovy.json)
    testImplementation(mnSecurity.micronaut.security)
    testImplementation(mn.reactor)
}


tasks {
    named<Test>("test") {
        systemProperty("elasticsearch.version", libs.versions.managed.elasticsearch.get())
    }
}

micronautBuild {
    binaryCompatibility {
        enabled.set(true)
        baselineVersion.set("5.0.0-M6")
    }
}
