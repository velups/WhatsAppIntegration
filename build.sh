#!/usr/bin/env bash
# Render build script

set -e

echo "Building Spring Boot application..."
mvn clean package -DskipTests

echo "Build completed successfully!"