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
    runtimeOnly(mnLogging.slf4j.jcl.over.slf4j)
    api(mn.micronaut.http)

    implementation(mn.micronaut.jackson.databind)

    testImplementation(mn.groovy.json)
    testImplementation(mnSecurity.micronaut.security)
    testImplementation(mn.reactor)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.elasticsearch)

    constraints {
        api(libs.apache.httpclient5) {
            because("GHSA-hjcp-jmpx-g3qm: elasticsearch-rest5-client brings httpclient5 5.6.1")
        }
        api(libs.apache.httpcore5) {
            because("GHSA-hf6x-8p5f-cgmf: elasticsearch-rest5-client brings httpcore5 5.4")
        }
        api(libs.apache.httpcore5.h2) {
            because("GHSA-v3jc-474w-2wm6: elasticsearch-rest5-client brings httpcore5-h2 5.4")
        }
        api(libs.jackson2.core) {
            because("Keep jackson-core aligned with jackson-databind")
        }
        api(libs.jackson2.databind) {
            because("GHSA-5gvw-p9qm-jgwh: elasticsearch-java brings jackson-databind 2.22.0")
        }
    }
}


tasks {
    named<Test>("test") {
        systemProperty("elasticsearch.version", libs.versions.managed.elasticsearch.get())
    }
}
