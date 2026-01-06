rootProject.name = "wallet-app"

// microservices
include("account-service")
//include("transaction-service")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}