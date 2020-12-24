# Proyecto: jn-quarkus-camel-gateway-base

Este es el arquetipo base de Quarkus 1.7 para el desarrollo un gateway base con la capacidad de orquestar.

Para mayor Referencia visitar los sitios: 

* [Product Documentation for Red Hat build of Quarkus 1.7](https://access.redhat.com/documentation/en-us/red_hat_build_of_quarkus/1.7/)
* [Quarkus Site](https://quarkus.io/)


## Requerimientos

Para usar este arquetipo, es suficiente tener instalado en tu maquina:

* JDK 11
* [Maven 3.6.3](https://maven.apache.org/index.html)
* Openshift Client (oc)

Así mismo, necesitas configurar tu Maven de acuerdo a la guía de Red Hat: [Getting started with Red Hat build of Quarkus](https://access.redhat.com/documentation/en-us/red_hat_build_of_quarkus/1.7/html/getting_started_with_red_hat_build_of_quarkus/index) en el capitulo 2.1.

Si quieres hacer un uso avanzado de Quarkus y compilar de manera nativa. Necesitas usar [GraalVM](https://www.graalvm.org) de la versión 11 para tu sistema operativo, así como, sus componentes adicionales: [Native Image](https://www.graalvm.org/reference-manual/native-image/), [LLVM toolchain](https://www.graalvm.org/reference-manual/llvm/Compiling/#llvm-toolchain-for-compiling-cc) y [Wasm](https://www.graalvm.org/reference-manual/wasm/).

Si deseas crear las imagenes nativas desde un contenedor tendrás que tener instalado [Docker](https://www.docker.com) y revisar las guías correspondientes de Quarkus y Red Hat.


## Comandos de quarkus para desarrollar

### Ejecución local de la aplicación (en tu maquina)

Puedes ejecutar la aplicación localmente con el siguiente comando, ademas no es necesario que vuelvas a compilar para tomar los cambios que hagas:
```
./mvnw clean quarkus:dev
```

Si estas corriendo varios microservicios en tu máquina puedes cambiar el puerto de escucha con el siguiente comando:
```
./mvnw clean quarkus:dev -Dquarkus.http.port=9090
```

La forma más facil de probar la aplicación directamente es usando curl:
```
curl -X GET http://localhost:9090/v1/hello-controller/hello
```

### Para desplegar la aplicación en Openshift

Para realizar tus despliegues en OpenShift, es necesario que con la utilería oc hayas hecho lo siguiente previamente:

```
$ oc login https://algun.servidor.openshift.com
$ oc project my-project
```
Con los dos comandos anteriores estarás seguro de que has hecho login y te encuentras en el projecto donde quieras hacer tu despliegue ([Aqui esta la guía de oc](https://docs.openshift.com/container-platform/3.11/cli_reference/get_started_cli.html)).

#### Primer paso: Crear los recursos de tu aplicación

Para los recursos de la aplicación se ha creado la carpeta `src/main/kubernetes`

La configuración de tu archivo `src/main/resources/application.properties` la usamos para el desarrollo local en tu máquina. Para la inyección de configuraciones se ha creado el archivo `src/main/kubernetes/openshift.yml` que contiene la configuración de tu micro servicio en openshift. La configuración lo monta posteriormente en el pod en `deployments/config/application.properties`.

Tu configmap para OpenShift se encuentra definido en el archivo `src/main/kubernetes/openshift.yml` y será creado automáticamente como parte de tu despliegue. Tienes que tener en cuenta solamente dos cosas para lo siguiente:

1. Tu ConfigMap tiene un nombre cuando lo defines en el archivo `openshift.yml` como se muestra a continuación una sección del archivo y puntualmente estamos hablando de la etiqueta `name`:
```
apiVersion: v1
kind: ConfigMap
metadata:
  labels:
    app: jn-quarkus-camel-gateway-base
    group: com.javanes.micro
    provider: javanes
  name: cm-jn-quarkus-camel-gateway-base
```
2. El nombre que pongas a esa etiqueta debe de ser el mismo que pones en el archivo `application.properties` para la siguiente seccion:
```
# quarkus.kubernetes-config.enabled=true
# quarkus.kubernetes-config.config-maps=cm-jn-quarkus-camel-gateway-base
quarkus.openshift.mounts.my-volume.path=/deployments/config
quarkus.openshift.config-map-volumes.my-volume.config-map-name=cm-jn-quarkus-camel-gateway-base
```

#### Segundo paso: Desplegar tu aplicación

Para hacer el despliegue ejecuta este comando para que puedas ver tu contenedor dentro de OpenShift:
```
./mvnw clean package \
-Dquarkus.container-image.build=true \
-Dquarkus.kubernetes.deploy=true \
-Dquarkus.openshift.expose=false \
-DskipTests \
-Dquarkus.openshift.labels.app.openshift.io/runtime=java
``````
Si deseas que se cree una ruta de manera automática para acceder al servicio entonces deberás cambiar `-Dquarkus.openshift.expose=true`.

### Empaquetar y correr la aplicación

La aplicación puede ser empaquetada usando `./mvnw package`. que crea el archivo `jn-quarkus-camel-gateway-base-1.0-SNAPSHOT-runner.jar` en el directorio `/target`.
Contempla que no es un _über-jar_ y que las dependencias se encuentran en el directorio `target/lib`..

La aplicación puede correr directamente con el siguiente comando: 
```
java -jar target/jn-quarkus-camel-gateway-base-1.0-SNAPSHOT-runner.jar
```


### Creación de un ejecutable nativo (en tu maquina)

Para crear un paquete nativo en tu maquina (para tu sistema operativo), es necesario tener instalado GraalVM, así como la variable de ambiente debe de apuntar al directorio de base, ejemplo:
```
export GRAALVM_HOME=/Library/Java/JavaVirtualMachines/graalvm-ce-java11-20.3.0/Contents/Home
```

Se usa el siguiente comando: `./mvnw package -Pnative` para crear el binario.

### Creación de un ejecutable nativo con un contenedor (para linux)

Si no cuentas con GraalVM, pero si con Docker. Se puede correr una contrucción nativa dentro de un contenedor, para lo cual debes de correr el siguiente comando:
```
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

El ejecutable resultante es una aplicación nativa Linux y puedes ejecutar tu aplicación nativa con: `./target/jn-quarkus-camel-gateway-base-1.0-SNAPSHOT-runner`

Para mayor referencia: https://quarkus.io/guides/building-native-image.

### Creación de un contenedor nativo para OpenShift

Para crear el contenedor deberás ejecutar el comando siguiente:
```
./mvnw clean package \
-Pnative \
-Dquarkus.native.container-build=true \
-Dquarkus.container-image.build=true \
-Dquarkus.kubernetes.deploy=true \
-Dquarkus.openshift.expose=true \
-DskipTests \
-Dquarkus.openshift.labels.app.openshift.io/runtime=native
```
Si por algun motivo ves *OOMKilled* en tu contenedor donde se hizo la costrucción de tu contenedor. Tendras que aumentar en el yaml el tamaño de la memoria asignada en el *BuildConfig*, con 4Gb deberá de ser suficiente siendo 8 el ideal.

El proceso de compilación con 500 milicores tardará aproximadamente 18 minutos.


# Arquetipo

A continuación se explica la estructura del arquetipo y sus reglas generales de uso.

## Características instaladas
* **camel-quarkus-main**. Librería principal de camel
* **camel-quarkus-xml-io**. Librería para definiciones XML.
* **camel-quarkus-direct**. Librería para enviar solicitudes directas a una ruta.
* **camel-quarkus-log**. Bitacora de camel.
* **camel-quarkus-http**. Componente para hacer llamados sobre HTTP.
* **camel-quarkus-rest**. Definición de elementos REST.
* **camel-quarkus-microprofile-health**. Endpoints para la salud del servicio.
* **camel-quarkus-microprofile-metrics**. Métricas del servicio.
* **camel-quarkus-jackson**. Librería para realizar marshall y unmarshall de elementos JSON.

## Estructura de paquetes

La estructura de paquetes es como sigue:
```
com.javanes.micro.<archetipe.name> -> Paquete Base.
  |- pojo -> Paquete VO (Value Objects, POJOs) que se usan dentro del servicio.
  |- enums -> Paquete destinado a enums.
  |- processor -> Paquete de processors para las funcionalidades java que se incluyen en las rutas.
```

La estructura final del paquete base es la siguiente:

Si en el pom.xml existe 
```
  <artifactId>jn-quarkus-camel-gateway-base</artifactId>
```

Entonces el paquete base será:
```
com.javanes.micro.quarkus.camel.gateway
```

## Manejo de excepciones

Para el arquetipo fue creado un processor en `com.javanes.micro.quarkus.camel.gateway.processor` llamada `AppExceptionProcessor` la cual hace el manejo de excepciones y es mandada a llamar dentro de las rutas en la sección `onException` como un proceso `<process ref="exceptionProcessor">`, aqui la sección donde se define: 
```
<!-- Crea un control de errores para esta ruta. -->
<onException>
   <description>Control de excepciones.</description>
   <exception>java.lang.Exception</exception>
   <handled><constant>true</constant></handled>
   <process ref="exceptionProcessor">
         <description>Interpreta el error y lo envuelve en un pojo "AppExceptionResponse"</description>
   </process>
   <marshal>
         <description>Convierte el resultado a JSON.</description>
         <json library="Jackson" unmarshalTypeName="com.javanes.micro.quarkus.camel.pojo.AppExceptionResponse"/>
   </marshal>
   <to uri="log:exchangeLog?level=WARN&amp;multiline=true&amp;showAll=true&amp;style=Fixed"/>
</onException>
```

La razón es que esta clase permite tener un control de errores adecuado para la aplicación que se encadena a través de las capas pudiendo ser certero en el manejo de todos los errores conocidos por la aplicación, para ello se ha creado ademas una enumeración en el paquete `com.javanes.micro.quarkus.camel.gateway.enums` que te permitirá crear un diccionario de errores HTTP, códigos de retorno y mensajes para la aplicación cliente.

## Configuración de la aplicación

Con Camel lo único que hay que hacer es poner dentro del archivo `application.properties` las propiedades que sean necesarias para tu aplicación y usarlas dentro de las rutas con doble llave `{{mi.propiedad}}`


## Como crear un nuevo Processor

Los Processor son la base de Camel, lo único que tienes que hacer es crear una clase dentro  del paquete `com.javanes.micro.quarkus.camel.gateway.processor` donde se usa la anotación `@Named` para proveerle el nombre que usaras para llamarlo dentro de las rutas.
```
@ApplicationScoped
@Named("nombreProcessor")
@RegisterForReflection
public class MiProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
       // Aqui tu funcionalidad
    }

}
```

Dentro de este proyecto ya tienes dos ejemplos muy sencillos de Processors `AppExceptionProcessor` y `AppPrepareCallProcessor` que puedes seguir como guias para crear mas de ser necesario.

## Como crear un nuevo End Point

Los End Points se encuentran definidos en el archivo`src/main/resources/rests/rests.xml` por lo que únicamente tendrás que agregar endpoints y enviarlos a su ruta correspondiente.

Por conveniencia para un microservicio de backend se genera una sola ruta que soporta tódos los métodos correspondientes a ese microservicio. Camel tiene la característica de ordenar todos los datos de la llamada y hacer una llamada límpia al backend.

1.- Crea una nueva ruta base en el archivo `rests.xml`.
```
<rest id="greeting" path="/v1/hello-controller">
</rest>
```
2.- Agrega los métodos dentro de esa ruta, en el caso que reciban parametros es importante definirlos, de esta manera los tendrás definidos como `headers` dentro de tu ruta y podras usarlos para tomar decisiones.
```
<rest id="greeting" path="/v1/hello-controller">
   <get uri="/hello">
   </get>
   <post uri="/hello">
      <param name="body" type="body"/>
   </post>
</rest>
```
3.- Envía la llamada a una ruta definida en el archivo `routes.xml` a través de `direct`.
```
<rest id="greeting" path="/v1/hello-controller">
   <get uri="/hello">
      <to uri="direct:hello-route" >
            <description>A la ruta get hello</description>
      </to>
   </get>
   <post uri="/hello">
      <param name="body" type="body"/>
      <to uri="direct:hello-route">
            <description>A la ruta post hello</description>
      </to>
   </post>
</rest>
```

## Como crear una ruta

Las rutas se encuentran definidas en el archivo `src/main/resources/routes/routes.xml` por lo que únicamente tendrás que nuevas rutas dentro de este archivo.

Para la creación de rutas te recomiendo que tengas en cuenta los siguientes elementos:

```
    <route id="hello-route">
        <onException>
        </onException>
        <!-- Comienzo de la ruta -->
        <from uri="direct:hello-route">
            <description>Desde el rests.xml</description>
        </from>
        <to uri="{{custom.hello.url}}?bridgeEndpoint=true&amp;throwExceptionOnFailure=false"/>
    </route>
```

1.- La ruta se define entre `<route></route>`.
2.- El control de excepciones de la ruta se encuentra entre `<onException></onException>`.
3.- From es la forma de mandar a la ruta, de tal manera que si en tu `rests.xml` tienes un `<to uri="direct:hello-route"/>` seguramente lo recibirás en este archivo en `<from uri="direct:hello-route"/>`.
4.- Para mandar a llamar un servicio de backend se usa la siguiente nomenclatura:
```
<to uri="http://localhost:8080?bridgeEndpoint=true&amp;throwExceptionOnFailure=false"/>
```
Dentro de este arquetipo hemos puesto por facilidad `{{custom.hello.url}}` y la hemos definido dentro del archivo `application.properties` a fin de que pueda ser modificada adecuadamente cuando tu microservicio pase entre los diversos ambientes (DEV,STG,QA,ETC) y puedas tener esas definiciones dentro de tu ConfigMap.

* La opción `bridgeEndPoint` es para que se se haga la solicitud a lo que esta escrito en esta linea y no se tome el valor del encabezado Exchange.HTTP_URI.
* La opción `throwExceptionOnFailure=false` le indica al microservicio que no lance una excepción en caso de que reciba un StatusCode diferente de 1XX o 2XX de todos modos respete la respuesta del microservicio llamado.