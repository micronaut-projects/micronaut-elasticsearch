plugins {
    id("io.micronaut.build.internal.bom")
}
micronautBuild {
    micronautBuild {
        // required because elasticsearch-rest-high-level-client was removed
        tasks.named("checkVersionCatalogCompatibility") { onlyIf { false } }
    }
}
