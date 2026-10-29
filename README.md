# Cliente / Servidor UDP con DatagramSocket y DatagramPacket

Aplicación sencilla en Java donde un cliente le pide datos a un servidor usando UDP,
y el servidor responde con un valor de cada tipo de dato de Java.

Solo usa las clases que ya trae Java (`java.net` y `java.io`), sin librerías externas.

La respuesta contiene los **ocho tipos primitivos de Java** (`byte`, `short`, `int`,
`long`, `float`, `double`, `char` y `boolean`), además de un `String` y un arreglo
de enteros como ejemplos de tipos de referencia.

## Archivos

| Archivo         | Qué hace                                                        |
|-----------------|-----------------------------------------------------------------|
| `Servidor.java` | Escucha en el puerto 5000 y responde con los tipos de datos.    |
| `Cliente.java`  | Envía la petición al servidor y muestra lo que recibe.          |

## Cómo ejecutarlo

Funciona con cualquier versión actual del JDK. Abrir dos terminales en la carpeta del proyecto.

Terminal 1 (servidor):

```
javac Servidor.java
java Servidor
```

Terminal 2 (cliente):

```
javac Cliente.java
java Cliente
```

Para conectarse a un servidor en otra computadora de la misma red, se pasa su IP:

```
java Cliente 192.168.1.50
```

## Flujo de la aplicación

```
        CLIENTE                                   SERVIDOR
           |                                         |
           |                          new DatagramSocket(5000)
           |                          receive()  (queda esperando)
           |                                         |
  new DatagramSocket()                               |
  send("DATOS") ------- DatagramPacket ------------> |
           |                                         |
  receive() (espera                       arma los bytes con
  máximo 5 segundos)                      DataOutputStream
           |                                         |
           | <------ DatagramPacket (56 bytes) ----- send()
           |                                         |
  lee los bytes con                       vuelve a esperar
  DataInputStream                         otra petición
  e imprime los datos
```

1. **El servidor abre un `DatagramSocket` en el puerto 5000** y se queda bloqueado en
   `receive()` esperando un paquete. En UDP no hay conexión previa como en TCP; el servidor
   solo espera paquetes.
2. **El cliente abre su propio `DatagramSocket`** sin indicar puerto (el sistema le asigna uno)
   y envía un `DatagramPacket` con el texto `DATOS` a la IP del servidor y al puerto 5000.
3. **El servidor recibe el paquete.** El `DatagramPacket` recibido trae la IP y el puerto del
   cliente (`getAddress()` y `getPort()`), y con eso sabe a quién responder.
4. **El servidor convierte los datos a bytes.** Un datagrama solo transporta un arreglo de bytes,
   por eso se usa `DataOutputStream`, que escribe cada tipo de dato de Java como bytes.
5. **El servidor envía la respuesta** en un nuevo `DatagramPacket` dirigido al cliente y vuelve
   a esperar la siguiente petición.
6. **El cliente recibe la respuesta** y la lee con `DataInputStream` **en el mismo orden** en que
   el servidor la escribió. Si el orden no coincide, los valores salen incorrectos.
7. Como UDP no garantiza que el paquete llegue, el cliente usa `setSoTimeout(5000)`: si en
   5 segundos no hay respuesta, muestra un mensaje en lugar de quedarse esperando.

## Conceptos de red demostrados

- Comunicación entre un cliente y un servidor mediante **UDP**.
- Uso de una dirección IP y un número de puerto para identificar el destino.
- Envío y recepción de datagramas sin establecer previamente una conexión.
- Conversión de los datos a bytes para poder transportarlos por la red.
- Uso de la IP y el puerto de origen para enviar la respuesta al cliente.
- Uso de un tiempo límite porque UDP no garantiza la entrega de los paquetes.

UDP tampoco garantiza que los datagramas lleguen en orden o que no se dupliquen. A
cambio, tiene menos sobrecarga que TCP. En esta práctica toda la respuesta ocupa
solamente 56 bytes, por lo que puede transportarse en un único datagrama.

## Contenido de la respuesta

| Tipo      | Valor          | Bytes                          |
|-----------|----------------|--------------------------------|
| byte      | 127            | 1                              |
| short     | 32000          | 2                              |
| int       | 1              | 4                              |
| long      | 129039023      | 8                              |
| float     | 1.23           | 4                              |
| double    | 12.342         | 8                              |
| char      | C              | 2                              |
| boolean   | true           | 1                              |
| String    | Hola           | 2 (longitud) + 4 (texto) = 6   |
| int[]     | [1, 2, 3, 4]   | 4 (tamaño) + 4 x 4 = 20        |
| **Total** |                | **56**                         |

## Prueba de escritorio

Cliente:

```
Peticion enviada a localhost/127.0.0.1:5000
Recibo de servidor:
byte: 127
short: 32000
int: 1
long: 129039023
float: 1.23
double: 12.342
char: C
boolean: true
String: Hola
arrayInt: [1, 2, 3, 4]
```

Servidor:

```
Servidor UDP escuchando en el puerto 5000
Peticion de /127.0.0.1:53963 -> DATOS
Respuesta enviada (56 bytes)
```

El puerto del cliente (53963) cambia en cada ejecución porque lo asigna el sistema operativo.
