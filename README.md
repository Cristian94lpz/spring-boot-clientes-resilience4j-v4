# RestClient
RestClient es un cliente HTTP sincrono introducido en Spring Framework 6.1 para
reemplazar al cliente RestTemplate.
RestClient ofrece una API fluida, legible y facil de usar.

## Creacion de una instancia de RestClient

* Podemos obtener un Builder autoconfigurado por Spring en un componente inyectandolo:
```
    @Autowired
    private RestClient.Builder restClientBuilder;
```
Luego podemos llamar al metodo build() para generar la instancia de `RestClient`.

* Tambien podemos obtener una instancia autoconfigurada con el metodo create() de RestClient:

```
    RestClient restClient = RestClient.create();
```
Esta es la implementacion de create():

```
    static RestClient create() {
        return (new DefaultRestClientBuilder()).build();
    }
```

* Podemos crear una instancia de RestClient con una instancia de RestTemplate preconfigurada:
```
    RestTemplate restTemplate;
    RestClient restClient = RestClient.create(restTemplate);
```
## Configuraciones

### Fabrica de ClientRequestFactory

Para ejecutar la solicitud HTTP, RestClient utiliza una biblioteca HTTP de cliente. Estas bibliotecas
se adaptan a traves de la `interfaz ClientRequestFactory`. Existen diversas implementaciones disponibles:

* `JDKClientHttpRequestFactory` para Java HttpClient
* `HttpComponentsClientHttpRequestFactory` para su uso con componentes HTTP de Apache HttpClient
* `JettyClientHttpRequestFactory para Jetty` HttpClient
* `ReactorNettyClientRequestFactory` para el reactor Netty HttpClient
* `SimpleClientHttpRequestFactory` como una configuracion predeterminada simple

Si no se especifica ninguna fabrica de solicitudes cuando RestClient es construido, se utilizara
Apache o Jetty HttpClient si estan disponibles en el classpath. De lo contrario, si el `java.net.http`
modulo se cargara, se utilizara Java HttpClient. Finalmente, se recurrira a la opcion predeterminada
que es simple.

Ejemplo:
```
    RestClient.builder().requestFactory(new HttpRequestFactory())
```
### MessageConverter

En SpringBoot 4 el mapeador predeterminado se convierte en `JsonMapper`, una subclase de `ObjectMapper`. Esto se debe
a una actualizacion de Jackson 2 a Jackson 3. A diferencia de `ObjectMapper`, `JsonMapper` es inmutable, esto 
permite que se pueda compartir de forma segura entre diferentes hilos.
En Jackson 3 el valor predeterminado de serializacion de fechas es en formato `ISO-8601`.En lugar de 
marcas de tiempo como en Jackson 2. Tambien en Jackson 3 las excepciones se vuelven no verificadas, todas
las excepciones de Jackson 3 se extienden de `JacksonException`, que es un `RunTimeExcepction`.
Jackson 3 presenta compatibilidad con la API de fecha y hora Java 8+ (LocalDate, LocalDateTime,etc).

En SpringBoot4 se utiliza el `HttpMessageConverter JacksonJsonHttpMessageConverter` para JsonMapper.
```
JsonMapper jsonMapper = JsonMapper.builder()
    .findAndAddModules()
    .enable(SerializarionFeature.INDENT_OUTPUT)
    .build();

RestClient restClient = RestClient.builder()
    .configureMessageConverters(converters -> converters
        .registerDefaults()
        .jsonMessageConverter(new JacksonJsonHttpMessageConverter(jsonMapper))
        .build();
```
## retrieve()
Con retrieve() podemos ingresar al flujo de trabajo de recuperacion y usar el objeto `RestClient.ResponseSpec`
devuelto para seleccionar entre varias opciones integradas para extraer la respuesta:

Para obtener un `ResponseEntity`:
```
    ResponseEntity<Person> entity = restClient.get()
        .uri("/persons/1)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .toEntity(Person.class);
```
O si solo interesa el cuerpo:
```
    ResponseEntity<Person> entity = restClient.get()
        .uri("/persons/1)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(Person.class);
```
Se usa `toBodilessEntity()` cuando se quiere devolver un `ResponseEntity` sin cuerpo. Que seria un `ResponseEntity<Void>`.

Tenga en cuenta que este metodo no ejecuta la solicitud hasta que se llama a una de las funciones
`RestClient.ResponseSpec` devueltas. Utilice las variantes exchange(RestClient.RequestHeadersSpec.ExchangeFunction)
si necesita separar la ejecucion de la solicitud de la extraccion de la respuesta. Por defecto los codigos
de respuesta 4xx generan una `HttpClientErrorException` y los codigos de respuesta 5xx una `HttpServerErrorException`
Para personalizar el manejo de errores, utilice los manejadores onStatus.

## exchange()

Este metodo puede ser util para escenarios avanzados, por ejemplo, para decodificar la respuesta de manera
diferente segun el estado de la respuesta:
```
    Person person = restClient.get()
        uri("/people/1:\")
        .accept(MediaType.APPLICATION_JSON)
        .exhange((request,response)->{
            if(response.getStatusCode().equals(HttpStatus.OK)){
            return deserialize(response.getBody());// getBody() devuelve un InputStream
            }else{
                throw new BusinessException();
            }
        });  
``` 

La respuesta se cierra una vez que se ha invocado la funcion exchange().

## Manejo de errores

RestClient siempre lanza una excepcion que extiende la clase `RestClientException` en caso de que la solicitud
falle debido a una respuesta de error del servidor, un fallo en la decodificacion de la respuesta, o un error
de I/O de bajo nivel (tambien se aplica para RestTemplate).
Las respuestas de error del servidor estan determinadas por status handlers para RestClient y por ResponseErrorHandler
para RestTemplate.
Las excepciones que heredan de `RestClientException` son:
* `ResourceAccessException` : se lanza cuando se produce un error de I/O
* `RestClientResponseException`: clase base para excepciones que contienen datos reales de respuesta HTTP
* `UnknownContentTypeException` : lanzado cuando no hay un adecuado HttpMessageConverter para extraer la respuesta

Excepciones que heredan de RestClientResponseException:
* `HttpStatusCodeException`: clase base abstracta para excepciones basada en un `HttpStatusCode`. De esta clase se
extienden otras dos:
  * `HttpClientErrorException`: se lanza cuando se recibe un codigo HTTP 4xx
  * `HttpServerErrorException`: se lanza cuando se recibe un codigo Http 5xx
* `UnknowHttpStatusCodeException`: se lanza cuando se recibe un codigo de estado HTTP desconocido



Existen dos opciones para manejar excepciones personalizadas:

1. Con el metodo defaultStatusHandler(), para todas las solicitudes HTTP enviadas con el.
2. Para cada solicitud HTTP con el metodo onStatus(), despues de la llamada al metodo retrieve().

### Con defaultStatusHandler()
```
    restClient.builder()
        .baseUrl("/home")
        .defaultHeader(HttpHeaders.AUTHORIZATION,
                        encodeBasic(properties.getUserName(),
                        properties.getPassword()))
        .defaultStatusHandler(
            HttpStatusCode::is4xxClientError,
            (request,response)-> {
                log.error("Client Error Status "+
                response.getStatusCode());
                log.error("Client Error Body "+ new String(response.getBody().readAllBytes()));
            })
        .build();
```
### Con onStatus()
```
    restClient.delete()
        .uri("/2")
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError,
            (req,res) -> log.error("Couldn't delete "+res.getStatusText())
        )
        .toBodilessEntity();
```
## ClientHttpRequestInterceptor

La `interfaz ClientHttpRequestInterceptor` es un contrato para interceptar solicitudes HTTP del lado
del cliente. Las implementaciones se pueden registrar con RestClient o RestTemplate para modificar
la solicitud saliente y/o la respuesta entrante.
La interfaz contiene el metodo intercept(), que intercepta la solicitud dada y devuelve una respuesta.
El proporcionado `ClientHttpRequestExecution` permite que el interceptor pase la solicitud y la respuesta
a la siguiente entidad de la cadena.

Un `ClientHttpRequestInterceptor` se puede definir o registrar con el metodo requestInterceptor().
Podemos crear una clase que implemente `ClientHttpRequestInterceptor` y registrarla con requestInterceptor()
en la instancia de `RestClient`.
```
    public class RestClientInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request,
                                            byte[] body,
                                            ClientHttpRequestExecution execution) throw IOExcepction{
            return execution.execute(request,body);
        }
    }
```
Luego
```
    restClient.requestInterceptor( new RestClientInterceptor()).build();
```
O podemos usar un `Consumer`:
```
    RestClient.builder()
        .requestInterceptor((request,body,execution)->{
                return execution.execute(request,body);
        })
        .build();
```
# Resilience4j

## CircuitBreaker

El CircuitBreaker se implementa mediante una maquina de estados finitos con tres estados normales:
`CLOSED`, `OPEN`, `HALF_OPEN`, y tres estados especiales: `METRICS_ONLY`, `DISABLED` y `FORCED_OPEN`.

El CircuitBreaker utiliza una ventana deslizante para almacenar y agregar el resultado de las llamadas.
Puede elegir entre una ventana deslizante basada en el conteo y una basada en el tiempo. La ventana deslizante
basada en el conteo agrega el resultado de las ultimas N llamadas. La ventana deslizante basada en
el tiempo agrega el resultado de las llamadas de los ultimos N segundos.

Una ventana deslizante es un registro historico de solicitudes recientes que se utilizan para calcular
la tasa de fallos. Impulsa la decision del disyuntor de cambiar de estado descartando continuamente los
datos de las solicitudes antiguas a medida que llegan las nuevas para mantener las metricas relevantes.

### Ventana deslizante basada en el recuento

La ventana deslizante basada en el conteo se implementa con una matriz circular de N mediciones.
Si el tamaño de la ventana de conteo es 10, la matriz circular siempre tendra 10 mediciones. La
ventana deslizante actualiza incrementalmente la agregacion total. La matriz se actualiza cuando 
se registra un nuevo resultado de llamada. Cuando se elimina la medicion mas antigua, esta se resta
de la agregacion total y el deposito se reinicia.

### Ventana deslizante basada en el tiempo

La ventana deslizante basada en el tiempo se implementa con una matriz circular de N agregaciones parciales
(cubos). Si el tamaño de la ventana de tiempo es de 10 segundos, el arreglo circular siempre tiene 10 
agregaciones parciales (cubetas). Cada cubeta agrega el resultado de todas las llamadas que ocurren en
un segundo de epoca determinado (agregacion parcial). La primera cubeta de arreglo circular almacena los
resultados de las llamadas del segundo de epoca actual. Las demas agregaciones parciales almacenan los resultados
de las llamadas de los segundos anteriores. La ventana deslizante no almacena los resultados de las 
llamadas individualmente, sino que actualiza de forma incremental las agregaciones parciales (cubeta) y una
agregacion total.

### Umbrales de tasa de fallos y tasa de llamadas lentas

El estado del disyuntor cambia de `CLOSED` a `OPEN` cuando la tasa de fallos es igual o superior a un
umbral configurable. Por ejemplo, cuando fallan mas del 50% de las llamadas registradas. Por defecto 
todas las excepciones se consideran fallos.

El disyuntor tambien cambia de `CLOSED` a `OPEN` cuando el porcentaje de llamadas lentas es igual o superior
a un umbral configurable. Por ejemplo, cuando mas del 50% de las llamadas registradas duran mas de 5 
segundos. Esto ayuda a reducir la carga en un sistema externo antes de que deje de responder.

La tasa de fallos y la tasa de llamadas lentas solo se pueden calcular si se ha registrado un numero
minimo de llamadas.

CircuitBreaker rechaza las llamadas con un `CallNotPermittedException` cuando esta en estado `OPEN`.
Despues de que haya transcurrido un tiempo de espera, el estado del CircuitBreaker cambia de `OPEN` a
`HALF_OPEN` y permite un numero configurable de llamadas para ver si el backend sigue sin estar disponible
o ha vuelto a estar disponible. Las llamadas posteriores se rechazan con un `CallNotPermittedException` hasta
que se hayan completado todas las llamadas permitidas. Si la tasa de fallos o la tasa de llamadas lentas es
igual o superior al umbral configurado, el estado vuelve a ser `OPEN`. Si la tasa de fallos y la tasa de 
llamadas lentas son inferiores al umbral, el estado vuelve a ser `CLOSED`.

El failureRate se calcula como:
```
    FR = (llamadas fallidas / llamadas totales)*100
```
Como llamadas totales se toma sliding-window-size.
Tener cuidado de confundir con minimum-number-of-calls, esta propiedad se usa para determinar cual es el minimo
numero de llamadas para empezar a calcular el fail rate. Para que funcione deberia ser la mitad de sliding-window-size,
de esa manera si fallan la mitad de llamadas de sliding-window-size, se abre el circuito aunque no llegue al tamano de
ventana deslizante completo.

### Propiedades configurables

* **failureRateThreshold**: configura el umbral de tasa de fallos en porcentaje. Valor predeterminado: 50
* **slowCallRateThreshold**: configura un umbral en porcentaje. CircuitBreaker considera una llamada como lenta
cuando la duracion de la llamada es mayor al valor de esta propiedad. Valor predeterminado: 100
* **slowCallDurationThreshold**: configura el umbral de duracion por encima del cual las llamadas se consideran
lentas y aumenta la tasa de llamadas lentas. Valor predeterminado: 60000 [ms]
* **permittedNumberOfCallsInHalfOpenState**: configura el numero de llamadas permitidas cuando el interruptor de 
circuito esta semiabierto. Valor predeterminado: 10
* **maxWaitDurationInHalfOpenState**: configura una duracion maxima de espera que controla el tiempo maximo que
un disyuntor puede permanecer en estado semiabierto antes de pasar a estado abierto. El valor de 0 
significa que el disyuntor esperaria indefinidamente en estado semiabierto hasta que se hayan completado todas
las llamadas permitidas. Valor predeterminado: 0 [ms]
* **slidingWindowType**: configura el tipo de ventana deslizante que se utiliza para registrar el resultado
de las llamadas cuando se cierra el CircuitBreaker. Tipos: `COUNT_BASED` y `TIME_BASED`. Valor predeterminado: `COUNT_BASED`
* **slidingWindowSize**: configura el tamano de la ventana deslizante que se utiliza para registrar el resultado
de las llamadas cuando se cierra el CircuitBreaker.
* **minimunNumberOfCalls**: configura el numero minimo de llamadas necesarias (por periodo de ventana deslizante)
antes de que el CircuitBreaker pueda calcular la tasa de error o la tasa de llamadas lentas. Valor predeterminado: 100
* **waitDurationInOpenSate**: el tiempo que el CircuitBreaker debe esperar antes de pasar de estado OPEN a HALF_OPEN. 
Valor predeterminado: 60000 [ms]
* **automaticTransitionFromOpenHalfEnabled**: si se establece en verdadero, el CircuitBreaker pasa automaticamente
del estado abierto al semiabierto, sin necesidad de realizar ninguna llamada para activar la transicion. Se crea
un hilo para supervisar todas las instancias de CircuitBreakers y hacer que pasen al estado semiabierto
una vez transcurrido el tiempo de espera especificado en waitDurationInOpenState. En cambio, si se establece
en falso, la transicion al estado semiabierto solo se produce si se realiza una llamada, incluso despues de
trascurrido waitDurationInOpenState. Valor predeterminado: false
* **recordException**: una lista de excepciones que se registran como fallos y que, por lo tanto, aumentan la tasa de fallos.
* **ignoreExceptions**: una lista de excepciones que se ignoran y que no se consideran ni un fracaso ni un exito.
* **recordFailurePredicate**: un predicado personalizado que evalua si una excepcion debe registrarse como un fallo.
* **ignoreExceptionPredicate**: un predicado personalizado que evalua si una exception debe ignorarse y no considerarse
ni un fallo ni un exito.

# Wiremock 

## Prioridad de coincidencia
Si tienes varios stubs similares configurados, WireMock utiliza una regla estricta de prioridades para decidir cuál 
devuelve. Siempre priorizará la regla más específica:

1. URL + método + cuerpo + encabezados
2. URL + método + cuerpo
3. URL + método + encabezados
4. URL + método

Nota: Cuidado con el registro de stubs de Wiremock, no se registra en el orden correcto de solicitud, tener
presente la marca de tiempo para determinar el orden de ejecucion, cuando se usa escenarios.


# Apache HTTPComponents V5

El caso principal en el que el cliente puede duplicar o reintentar una solicitud automaticamente es cuando
ocurre un error de red en fase inicial, antes de que el servidor haya procesado o devuelto una respuesta.
El mecanismo de manejo de reintento automatico en la version 4 era `HttpRequestRetryHandler`, y fue cambiado
en la version 5 por `HttpRequestRetryStrategy`. Este ultimo maneja reintentos por I/O excepciones y codigos HTTP
de reintento como 503 Service Unavailable y 429 Too Many Request.
Por defecto, HttpClient 5 se inicializa con `DefaultHttpRequestRetryStrategy`, y presenta las siguientes configuraciones:
* reintenta exactamente 1 vez
* hace una pausa de 1s entre intentos
* no reintenta excepciones fatales como `UnknownHostException`, `ConnectException`, `SSLException`, `NoRouteToHostException` y `InterruptedIOException`.
* solo volvera a intentarlo si el metodo de solicitud se considera idempotente (GET, PUT, DELETE, HEAD). Metodos no
idempotentes como POST no se vuelven a intentar.
* gestiona automaticamente el encabezado `Retry-After` de la respuesta si esta presente.

El paquete donde se encuentras las excepciones lanzadas por spring es `org.springframework.web.client`, para mas informacion.


# Git

## Tipos de confirmacion

* **feat**: confirmacion que anade una nueva caracteristica
* **fix**: confirmacion que corrige un error
* **refactor**: codigo refactorizado que no corrige un error ni añade una funcion, sino que reescribe/reestructura el codigo
* **chore**: cambios que no estan relacionados con una correccion o caracteristica y que no modifican los
archivos src o test; confirmaciones diversas (actualizar dependencias o modificar el archivo .gitignore, etc)
* **perf**: confirmaciones orientadas a mejorar el rendimiento
* **ci**: confirmaciones relacionadas con la integracion continua
* **ops**: confirmaciones que afectan a componentes operativos como infraestructura, despliegue, copias de seguridad, recuperacion, etc
* **build**: cambios que afectan al sistema de compilacion, herramienta de compilacion, canalizacion de CI, dependencias, version, etc
* **docs**: confimaciones que afectan a la documentacion, como el archivo README
* **style**: cambios que no afectan el significado del codigo, probablemente relacionados con el formato del codigo,
como espacios en blanco, puntos y comas faltantes, etc
* **revert**: revierte una confirmacion anterior
* **test**: confirmaciones que añaden pruebas faltantes o corrigen pruebas existentes

## Sintaxis
```
    <tipo> [(contexto opcional)] : descripcion
``` 
Ejemplo:
```
    fix(auth): correccion en la validacion de password
```

