package es.ubu.lsi.client;

// Importamos las librerías/paquetes necesarias para su ejecución.
import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz que define la lógica y las operaciones básicas de un
 * usuario dentro del chat.
 * 
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.1.0
 */

public interface IChatClient {
	
	/**
	 * Inicializa la conexión del usuario con el servidor.
	 * 
	 * @return true si la conexión fue exitosa, false si ocurrio algún error.
	 */
	boolean connect();
	
	/**
	 * Envía un mensaje al servidor.
	 * 
	 * @param msg - El mensaje a enviar al servidor.
	 */
	void sendMessage(ChatMessage msg);
	
	/**
	 * Desconecta el usuario del servidor.
	 * <p>
	 * Cierra la conexión con el servidor de manera segura.
	 * </p>
	 */
	void disconnect();
}