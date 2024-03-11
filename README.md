# code-with-quarkus-soap

Genera clases Java a partir de archivos XML y esquemas XML.


```shell
mvn generate-sources
```

Run your application in dev mode that enables live coding using

```shell
mvn package quarkus:dev
```

Create a native executable using:

```shell
mvn package -Pnative
```

The application, packaged as an _über-jar_, is now runnable using:

```shell
./target/code-with-quarkus-soap-1.0.0-runner
```

Validate:

```shell
curl -X POST 'http://localhost:8080/add' \
  --header 'Content-Type: application/json' \
  --data '{"param1": 10, "param2": 20}' | jq
```