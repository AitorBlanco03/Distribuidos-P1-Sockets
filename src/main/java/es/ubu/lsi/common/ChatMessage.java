package es.ubu.lsi.common;

// Importamos las librerías/paquetes necesarias para su ejecución.
import java.io.Serializable;

/**
 * Representa un mensaje dentro del sistema del chat.
 * <br><br>
 * Cada mensaje del chat tiene un tipo, un contenido y una referencia al
 * usuario que lo envío.
 * 
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.1.0
 */

public class ChatMessage implements Serializable {
	
	/** Identificador único para serializar los mensajes antes de enviarlos. */
	private static final long serialVersionUID = 7467237896682458959L;
	
	/** Tipo asociado al mensaje. */
	private MessageType messageType;
	
	/** Contenido asociado al mensaje. */
	private String messageContent;
	
	/** Referencia al usuario que envío el mensaje. */
	private String userSender;
	
	/**
	 * Crea un nuevo mensaje dentro del sistema del chat con una referencia al
	 * usuario que lo envío, el tipo del mensaje y su contenido.
	 * 
	 * @param senderUserID - Referencia al usuario que envío el mensaje dentro del chat.
	 * @param messageType - Tipo del mensaje.
	 * @param messageContent - Contenido del mensaje.
	 */
	public ChatMessage(String userSender, MessageType messageType, String messageContent) {
		this.setUserSender(userSender);
		this.setMessageType(messageType);
		this.setMessageContent(messageContent);
	}
	
	/**
	 * Devuelve la referencia del usuario que envío el mensaje.
	 * 
	 * @return La referencia del usuario que envío el mensaje.
	 */
	public String getUserSender() {
		return this.userSender;
	}
	
	/**
	 * Establece y asigna la referencia del usuario que envío el mensaje.
	 * 
	 * @param userSenderID - La nueva referencia del usuario que envió el mensaje,
	 */
	public void setUserSender(String userSender) {
		this.userSender = userSender;
	}
	
	/**
	 * Devuelve el tipo asociado al mensaje.
	 * 
	 * @return El tipo asociado al mensaje.
	 */
	public MessageType getMessageType() {
		return this.messageType;
	}
	
	/**
	 * Establece y asigna el tipo del mensaje.
	 * 
	 * @param messageType - El tipo del mensaje.
	 */
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	
	/**
	 * Obtiene el contenido del mensaje.
	 * 
	 * @return El contenido del mensaje.
	 */
	public String getMessageContent() {
		return this.messageContent;
	}
	
	/**
	 * Establece y asigna el contenido del mensaje.
	 * 
	 * @param messageContent - El contenido del mensaje.
	 */
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
}