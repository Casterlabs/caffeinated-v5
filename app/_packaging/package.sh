#!/bin/bash
set -e

cd ..
./mvnw package

echo ""
echo ""

cd _packaging
java -jar jcup-bundler.jar
