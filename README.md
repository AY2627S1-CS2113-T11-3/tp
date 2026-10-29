# Bob

**Bob** is a command-line application that helps university lab technicians manage inventory, schedule lab sessions, calculate session preparation requirements, maintain supplier and maintenance contacts, and track student requests, equipment loans, and broken items. It is written in Java and optimized for users who prefer typing fast, single-line commands.

## Setting up in IntelliJ

Prerequisites: JDK 25 (use the exact version), update IntelliJ to the most recent version.

1. **Ensure IntelliJ JDK 25 is defined as an SDK**, as described [here](https://www.google.com/search?q=https://www.jetbrains.com/help/idea/sdk.html%23set-up-jdk) -- this step is not needed if you have used JDK 25 in a previous IntelliJ project.
2. **Import the project _as a Gradle project_**, as described [here](https://se-education.org/guides/tutorials/intellijImportGradleProject.html).
3. **Verify the setup**: After the importing is complete, locate the `src/main/java/seedu/bob/Bob.java` file, right-click it, and choose `Run 'Bob.main()'`. If the setup is correct, you should see something like the below:
   ```text
   > Task :compileJava
   > Task :processResources NO-SOURCE
   > Task :classes

   > Task :seedu.bob.Bob.main()
     ____        _     
    |  _ \      | |    
    | |_) | ___ | |__  
    |  _ < / _ \| '_ \ 
    | |_) | (_) | |_) |
    |____/ \___/|_.__/ 

   What is your name?
   ```
   Type your name and press Enter to start entering commands, then type `Bye` to let the execution proceed to the end.

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.

## Build automation using Gradle

* This project uses Gradle for build automation and dependency management (`build.gradle`).
* To run all checks and build the executable JAR (`build/libs/bob.jar`):
    * On macOS/Linux: `./gradlew check shadowJar`
    * On Windows: `.\gradlew.bat check shadowJar`
* To launch the built JAR directly: `java -jar build/libs/bob.jar`
* If you are new to Gradle, refer to the [Gradle Tutorial at se-education.org/guides](https://se-education.org/guides/tutorials/gradle.html).

## Testing

### I/O redirection tests

* To run _I/O redirection_ tests (aka _Text UI tests_), build with `./gradlew check shadowJar`,
  then run `sh text-ui-test/runtest.sh`. On Windows, use `gradlew.bat check shadowJar`
  and `text-ui-test\runtest.bat`. See [the test instructions](https://www.google.com/search?q=text-ui-test/README.md).

### JUnit tests

* Automated JUnit 5 tests are provided in `src/test/java/seedu/bob/`. Run them using `./gradlew test` (or `.\gradlew.bat test` on Windows).
* If you are new to JUnit, refer to the [JUnit Tutorial at se-education.org/guides](https://se-education.org/guides/tutorials/junit.html).

## Checkstyle

* Checkstyle rules are configured in `config/checkstyle/checkstyle.xml` and run automatically during `./gradlew check`.
* If you are new to Checkstyle, refer to the [Checkstyle Tutorial at se-education.org/guides](https://se-education.org/guides/tutorials/checkstyle.html).

## CI using GitHub Actions

The project uses [GitHub Actions](https://github.com/features/actions) for CI. When you push a commit to this repo or PR against it, GitHub Actions will run automatically to build and verify the code across Windows, macOS, and Ubuntu.

## Documentation

Project documentation is located in the `/docs` folder:

* [User Guide](docs/UserGuide.md)
* [Developer Guide](docs/DeveloperGuide.md)
* [About Us](docs/AboutUs.md)
