pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven  ("https://jitpack.io" )
        maven ("https://www.jitpack.io")
        maven ("https://storage.zego.im/maven")
    }
}

rootProject.name = "ChitChat"
include(":app")
 