plugins {
    java
    `java-library`
}

dependencies {
    api(enforcedPlatform(libs.quarkus.platform.bom))

    api(project(":muyun-database"))

    api("io.quarkus:quarkus-rest")
    api("io.quarkus:quarkus-arc")
    api("io.quarkus:quarkus-vertx")
    api("io.quarkus:quarkus-reactive-routes")
    api("io.quarkus:quarkus-rest-jackson")
    api("io.quarkus:quarkus-smallrye-openapi")
    api("io.quarkus:quarkus-scheduler")
    api("io.quarkus:quarkus-smallrye-jwt")
    api("io.quarkus:quarkus-smallrye-jwt-build")

    api(libs.commons.codes)
    api(libs.caffeine)

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}
