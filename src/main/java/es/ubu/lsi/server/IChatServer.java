package es.ubu.lsi.server;

// Importamos las librerías/paquetes necesarias para su ejecución.
import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz que define la lógica y las operaciones básicas del servidor.
 * 
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.1.0
 */

public interface IChatServer {
	
	/**
	 * Inicializa y arranca el servidor, esperando y aceptando las conexiones entrantes.
	 * <p>
	 * Por cada usuario, se encarga de crear un hilo para manejar su comunicación de forma
	 * independiente.
	 * <p>
	 */
	void startServer();
	
	/**
	 * Finaliza el servidor cerrando todas las comunicaciones abiertas con los usuarios y
	 * liberando consigo todos los recursos utilizados.
	 */
	void shutdownServer();
	
	/**
	 * Reenvia el mensaje recibido a todos los usuarios conectados.
	 * 
	 * @param message - El mensaje que será reenviado a todos los usuarios conectados.
	 */
	void sendBroadcastMessage(ChatMessage message);
	
	/**
	 * Elimina un usuario de la lista de usuarios conectados, indicando que ese usuario
	 * ha cerrado sesión o se ha desconectado del sistema.
	 * 
	 * @param userName - El usuario que ha cerrado sesión o se ha desconectado del sistema.
	 */
	void removeConnectUser(String userName);
}