package es.ubu.lsi.common;

/**
 * Enum que define los diferentes tipos de mensajes/comandos que pueden
 * enviarse dentro del sistema de chat.
 *
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.3.0
 */
public enum MessageType {
	
	/**
	 * Mensaje que un usuario envía al servidor o viceversa. Es el mensaje
	 * más común dentro del chat.
	 */
	MESSAGE,
	
	/**
	 * Mensaje que un usuario envía para avisar que ha bloqueado a otro usuario del 
	 * sistema, impidiendo que este último pueda interactuar con él dentro del chat.
	 */
	BAN,
	
	/**
	 * Mensaje que un usuario envía para avisar que ha desbloqueado a otro usuario del
	 * sistema, permitiendo que este último pueda volver a interactuar con él dentro del chat.
	 */
	UNBAN,
	
	/**
	 * Mensaje que un usuario envía al servidor para conectarse al sistema del chat.
	 */
	LOGIN,
	
	/**
	 * Mensaje que un usuario envía al servidor para desconectarse del sistema del chat.
	 */
	LOGOUT,
	
	/**
	 * Mensaje que envía al servidor a los usuarios para avisar de su inminente apagado.
	 */
	SHUTDOWN
}