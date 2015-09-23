#!/bin/bash
unzip -o ./target/acap25-0.1.jar "*.so" -d ./native
java -ea -Djava.library.path=./native -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -jar target/acap25-0.1.jar
