# Model Anima

Minecraft Bedrock model and animation parser.

# How to add to your workspace

Insert the `plugins` block just below `buildscript`.

```gradle
plugins {
    id 'com.github.johnrengelman.shadow' version "4.0.4"
}
```

Add the shade configuration, repository, and the model anima dependency. Note `runtimeOnly` and `compileOnly` are used to allow sources to attach properly.

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
    compileOnly "com.github.Ocelot5836:ModelAnima:${project.modelAnima}"
    runtimeOnly fg.deobf("com.github.Ocelot5836:ModelAnima:${project.modelAnima}")
    shade fg.deobf("com.github.Ocelot5836:ModelAnima:${project.modelAnima}")
}
```

These remaining settings are added to allow the jar to build properly.

```gradle
shadowJar {
    configurations = [project.configurations.shade]
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

## Optional

If you want, you can add this so Model Anima will be compiled into a different package to allow better compatibility.

```gradle
shadowJar {
    configurations = [project.configurations.shade]
    relocate 'io.github.ocelot', 'your.project.lib.ocelot'
}
```
