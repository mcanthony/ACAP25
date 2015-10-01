#!/bin/bash
unzip -o ./import/* "*.so" -d ./native
java -cp "target/acap25-0.1.jar:import/*" -Djava.library.path=./native -Dorg.slf4j.simpleLogger.defaultLogLevel=debug org.anhonesteffort.p25.ACAP25
