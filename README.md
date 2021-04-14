# Model Anima

Minecraft Bedrock model and animation parser.

# How to add to your workspace

Insert the `plugins` block just below `buildscript`.

```gradle
plugins {
    id 'com.github.johnrengelman.shadow' version "4.0.4"
}
```

Add the shade configuration, repository, and the model anima dependency.

```gradle
configurations {
    shade
}

repositories {
    maven {
        name = "JitPack"
        url = "https://jitpack.io"
    }
}

dependencies {
    compileOnly fg.deobf("com.github.Ocelot5836:ModelAnima:${project.modelAnima}:api")
    runtimeOnly fg.deobf("com.github.Ocelot5836:ModelAnima:${project.modelAnima}")
    shade "com.github.Ocelot5836:ModelAnima:${project.modelAnima}"
}
```

These remaining settings are added to allow the jar to build properly.

```gradle
shadowJar {
    configurations = [project.configurations.shade]
    relocate 'io.github.ocelot', 'your.project.lib.ocelot'
}

reobf {
    shadowJar {}
}

artifacts {
    archives jar
    archives shadowJar
}

build.dependsOn reobfShadowJar
```

Finally, choose the version of Model Anima you wish to use and add the following to the `gradle.properties`

```properties
modelAnima=ModelAnimaVersion
```
