# A simple typescript generator for openapi-generator

Usage:

```bash
cd typescript-simple
mvn clean package
```

Then go to the directory where you want to generate the typescript client
and run `openapigen.sh`, passing the directory for the generated sources
as a parameter. e.g.:

```bash
../openapigen-typescript-simple/openapigen.sh src/server-api
```
