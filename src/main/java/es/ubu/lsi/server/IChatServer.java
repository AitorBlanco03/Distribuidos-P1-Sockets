package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz que define y establece la lógica y las operaciones básicas del
 * servidor del chat.
 * <p>
 * Define la lógica y las operaciones básicas para iniciar y detener el servidor, así como
 * el reenvío de mensajes a todos los usuarios conectados al servidor.
 * </p>
 * 
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.1.2
 */
public interface IChatServer {
	
	/**
	 * Inicia el servidor y comienza a aceptar nuevas conexiones entrantes.
	 */
	void startServer();
	
	/**
	 * Detiene el servidor, cerrando todas las conexiones abiertas y dejando
	 * de aceptar nuevas conexiones entrantes.
	 */
	void shutdownServer();
	
	/**
	 * Reenvía el mensaje recibido a todos los usuarios conectados al servidor.
	 * 
	 * @param msg Mensaje que se desea reenviar a todos los usuarios conectados al servidor.
	 */
	void sendBroadcastMessage(ChatMessage msg);
}