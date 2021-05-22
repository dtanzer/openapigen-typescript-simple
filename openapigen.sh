#!/bin/bash

if [ -z "$1" ]
	then
		echo "Usage: openapigen.sh [directory]"
		echo "Where [directory] is where the generator will put the generated sources."
		exit
fi

BASEDIR=$(dirname "$0")

echo "Removing previously generated API at $1"
rm -rf $1

echo "Generating API at $1"
java -cp $BASEDIR/openapi-generator-cli.jar:$BASEDIR/typescript-simple/target/typescript-simple-openapi-generator-1.0.0.jar \
org.openapitools.codegen.OpenAPIGenerator \
generate -g typescript-simple \
-i http://localhost:8080/v3/api-docs/ -o $1
# --global-property debugModels
# --global-property debugOperations
