package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz que define y establece la lógica y las operaciones básicas de los usuarios 
 * dentro del sistema.
 * <p>
 * Define la lógica y las operaciones básicas para conectarse y desconectarse del sistema, así como
 * el envío de mensajes al servidor del sistema.
 * </p>
 * 
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.1.3
 */
public interface ChatClient {
	
	/**
	 * Establece conexión con el servidor del sistema.
	 * 
	 * @return true si la conexión se realizó con éxito, false en caso de error.
	 */
	boolean connect();
	
	/**
	 * Finaliza la conexión con el servidor de forma segura cerrando todos
	 * los recursos utilizados durante la sesión.
	 */
	void disconnect();
	
	/**
	 * Envía un mensaje al servidor del sistema.
	 * 
	 * @param msg Mensaje que se desea enviar al servidor del sistema.
	 */
	void sendMessage(ChatMessage msg);
}