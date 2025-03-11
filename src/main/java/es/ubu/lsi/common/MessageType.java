package es.ubu.lsi.common;

/**
 * Enum que define los diferentes tipos de mensajes que pueden 
 * llegar a enviarse dentro del chat.
 * 
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.1.0
 */

public enum MessageType {
	
	/** Mensaje normal/estándar que envía un usuario dentro del chat. */
	MESSAGE,
	
	/** Mensaje de usuario para bloquear los mensajes de otro usuario. */
	BAN,
	
	/** Mensaje de usuario para volver a recibir mensajes de un usuario bloqueado. */
	UNBAN,
	
	/** Mensaje de usuario para desconectarse del chat. */
	LOGOUT,
	
	/** Mensaje del sistema para notificar y apagar el servidor. */
	SHUTDOWN
	
}