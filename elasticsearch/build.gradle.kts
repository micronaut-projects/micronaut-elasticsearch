plugins {
    id("io.micronaut.build.internal.elasticsearch-module")
}

dependencies {
    annotationProcessor(mn.micronaut.graal)

    compileOnly(libs.graal.svm)
    implementation(mn.micronaut.management)
    api(libs.managed.elasticsearch.java)
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
        enabled.set(false)
    }
}
