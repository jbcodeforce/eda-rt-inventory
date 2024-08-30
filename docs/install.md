# Installation

## Pre-requisites

Tested on Windows Linux Server 2.

* [Install jbang cli for ](https://www.jbang.dev/) using the downloqd instructions.
* [Install Open Java 11 on your computer if you do not have it, using jbang](https://www.jbang.dev/documentation/guide/latest/javaversions.html). 

    ```sh
    jbang jdk install -o --fresh --quiet --verbose 11
    ```

* Install [quarkus CLI](https://quarkus.io/) using jbang app:

    ```sh
    jbang app install --name quarkus io.quarkus:quarkus-cli:3.2.12.Final:runner
    ```