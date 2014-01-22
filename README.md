pdf-splitter
============

Program that opens a single PDF and accepts inputs to split it into multiple PDFs

## How to build

### Requirements

* Java
* Maven

### Build executable jar

* From root directory, run mvn clean install
* Change to target directory
* The executable jar is the one ending with "-with-dependencies"

## How to run

* run java -jar full_jar.jar original_file
* send in new page definitions (one per line) in the format startPage endPage outputFileName