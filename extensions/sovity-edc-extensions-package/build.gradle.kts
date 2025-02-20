val edcVersion: String by project
val edcGroup: String by project
val restAssured: String by project

plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    // Policies
    api(project(":extensions:policy-referring-connector"))
    api(project(":extensions:policy-time-interval"))
    api(project(":extensions:policy-always-true"))

    // API Extensions
    api(project(":extensions:contract-agreement-transfer-api"))
    api(project(":extensions:edc-ui-config"))
    api(project(":extensions:last-commit-info"))
    api(project(":extensions:wrapper:wrapper"))
}

val sovityEdcExtensionGroup: String by project
group = sovityEdcExtensionGroup

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}
