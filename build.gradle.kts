plugins {
    id("java")
    id("maven-publish")

    // 🛑 CORREÇÃO: Usando o ID e a versão CORRETOS conforme a documentação oficial.
    id("org.openjfx.javafxplugin") version "0.1.0"

    // 🔥 SHADOW (fat jar)
    id("com.github.johnrengelman.shadow") version "8.1.1"
    //id("com.gradleup.shadow") version "8.3.5"
}

group = "megalodonte"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}


// 🛑 2. CONFIGURA O PLUGIN DO JAVAFX
javafx {
    // Define a versão do JavaFX para ser usada em todos os módulos
    version = "17" // Mantida a versão 17.0.10.

    // Lista os módulos JavaFX que sua biblioteca PRECISA para compilar.
    // O plugin adiciona automaticamente a dependência para a sua plataforma de build.

    //esse meu projeto como é simples, só o modulo de controls e graphics foi o suficiente
    //modules("javafx.controls", "javafx.graphics", "javafx.fxml", "javafx.media", "javafx.web")
    modules("javafx.controls", "javafx.graphics")
}

dependencies {
    // Dependências de teste (mantidas)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Mockito
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")

    implementation("megalodonte:megalodonte-base:1.0.0-beta")
    implementation("megalodonte:megalodonte-components:1.0.0-beta")
    implementation("megalodonte:megalodonte-reactivity:1.0.0-beta")
    implementation("megalodonte:megalodonte-router:1.0.0-beta")
    implementation("megalodonte:megalodonte-theme:1.0.0-beta")

    // Apache Commons Net for FTP
    implementation("commons-net:commons-net:3.9.0")

    // Apache FtpServer: implementação completa do protocolo FTP
    implementation("org.apache.ftpserver:ftpserver-core:1.2.0")

    // Dependências JavaFX removidas (agora gerenciadas pelo bloco 'javafx { ... }')
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    enabled = true
    archiveBaseName.set("adb-file-pusher")

    manifest {
        attributes(
            "Implementation-Title" to "JavaFX adb-file-pusher app",
            "Implementation-Version" to project.version
        )
    }
}

//no caso vai copiar os jar dinamicamente que a aplicação ta usando
// Crie uma tarefa para copiar todas as dependências de runtime
val copyDeps = tasks.register<Copy>("copyDependencies") {
    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("dependencies"))

    // Evita duplicar o que já vai estar no JRE via JLink
    exclude("org/openjfx/**")
}

tasks.register<Exec>("createInstaller") {
    group = "distribution"
    description = "Gera o instalador .deb usando o script shell."

    // Garante que o JAR seja buildado antes de rodar o script
//    dependsOn("jar")
   // dependsOn("shadowJar")
    dependsOn("jar", "copyDependencies")

    // Define o diretório de execução como a raiz do projeto
    workingDir = projectDir

    // Comando para rodar o script
    commandLine("./scripts/linux/create-installer-using-gradlew.sh")
}

// Configuração de Publicação (mantida)
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "adb-file-pusher"
        }
    }
}

