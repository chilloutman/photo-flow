# photo-flow
ZHAW project: Photography Workflow Application

# Getting Started

## Without Eclipse Gradle Plugin

1. Generate eclipse files from the command line: `gradle eclipse`
2. In Eclipse: `Right Click -> Import... -> Existing Projects`
3. Select all modules (`core`, `app`, ...)
3. Done

## With Eclipse Gradle Plugin
1. In Eclipse: `Right Click -> Import... -> Gradle Projects`
2. Select `photo-flow`
3. Click `Build Model`
4. Select all modules (`core`, `app`, ...)
5. Uncheck all the checkboxes, expept for `executeAfter`. There, change `afterEclipseImport` to `eclipse`.
6. In the Eclipse menu: `Window -> Show View -> Other... -> Gradle Tasks`
7. Done
