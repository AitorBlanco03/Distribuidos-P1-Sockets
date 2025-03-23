<p align="center">
    <img src="https://socialify.git.ci/AitorBlanco03/Distribuidos-P1-Sockets/image?font=Raleway&language=1&logo=https%3A%2F%2Fcdn-icons-png.freepik.com%2F512%2F9866%2F9866722.png&name=1&owner=1&pattern=Signal&theme=Auto" alt="Distribuidos-P1-Sockets" width="640" height="320" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&amp;logo=openjdk&amp;logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&amp;logo=Apache%20Maven&amp;logoColor=white" alt="Apache Maven">
  <img src="https://img.shields.io/badge/Apache%20Ant-A81C7D?style=for-the-badge&amp;logo=Apache%20Ant&amp;logoColor=white" alt="Apache Ant">
</p>

## Descripción del Proyecto.

Chat en tiempo real para la consola, desarrollado en Java y basado en sockets TCP, que permite una comunicación eficiente entre múltiples usuarios dentro de una misma red.

### ¿Qué puedes hacer con este chat?

* **Enviar y recibir mensajes en tiempo real** con otros usuarios conectados.
* **Gestionar bloqueos**, para evitar recibir mensajes de ciertos usuarios.
* **Mensajes de broadcast**, donde todo lo que escribas en el chat se reenviará a todos los usuarios conectados, excepto aquellos que te hayan bloqueado.
* **Manejo de multiples clientes** de manera simultánea gracias a la creación y la gestión de hilos.
* **Desconexión segura**, asegurando que puedas salir sin afectar al sistema o/y al servidor.

## Estructura del Proyecto.

El proyecto se compone de tres partes principales y fundamentales:

### 1. Servidor (`ChatServerImpl.java`)

> **NOTA:** El servidor no diferencia entre mayúsculas y mínusculas, por lo que los comandos se procesan independiente si están en mayúsculas o en minúsculas.

El servidor es el "corazón" del sistema. Se encarga de:

* Escuchar y gestionar las nuevas conexiones entrantes.
* Reenviar los mensajes a todos los usuarios conectados.
* Adminstrar los bloqueos y desbloqueos dentro del sistema.
* Mantener y gestionar el sistema y los recursos compartidos usando bloqueos de lectura y escritura.

### 2. Cliente (`ChatClientImpl.java`)

Es una instancia de un nuevo usuario dentro del sistema. Se encarga de;

* Conectarse al servidor utilizando un puerto.
* Enviar y recibir mensajes de otros usuarios en tiempo real.
* Bloquear y desbloquear a otros usuarios.
* Salir del chat sin problemas.

### 3. Mensajes (`ChatMessage.java`)

Son los diferentes mensajes intercambiados entre el usuario y el servidor. Contiene:

* **El remitente del mensaje.**
* **El contenido del mensaje.**
* **El tipo del mensaje dentro del sistema.**

## Requisitos:

Para poder probar y ejecutar necesitarás:

* **Java 17 o superior.**
* **Apache Maven 3.8.8 o superior.**
* **Apache Ant 1.10.15 o superior.**

## Como ejecutarlo:

### 1. Clona el repositorio:

```
    git https://github.com/AitorBlanco03/Distribuidos-P1-Sockets
    cd Distribuidos-P1-Sockets
```

### 2. Compila el proyecto con Maven.

```
    mvn clean install
```

### 3. Inicia el servidor.

```
    mvn exec:java -Dexec.mainClass="es.ubu.lsi.server.ChatServerImpl"
```

### 4. Conéctate con un cliente.

```
    mvn exec:java -Dexec.mainClass="es.ubu.lsi.client.ChatClientImpl" -Dexec.args="<tu_nombre>"
```

Si quieres indicar la dirección IP o nombre de la máquina:

```
    mvn exec:java -Dexec.mainClass="es.ubu.lsi.client.ChatClientImpl" -Dexec.args="<ip_servidor> <tu_nombre>"
```

## Comandos útiles dentro del chat.

* **Escribir un mensaje** y presionar Enter para enviarlo al resto de usuarios.
* `ban <usuario>` para bloquear un usuario dentro del chat.
* `unban <usuario>` para desbloquear un usuario dentro del chat.
* `logout` para salir del chat de forma segura.
