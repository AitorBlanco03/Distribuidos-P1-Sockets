package es.ubu.lsi.common;

// Importamos las librerías/paquetes necesarías para crear y serializar los mensajes del chat.
import java.io.Serializable;

/**
 * Representa un mensaje dentro del chat.
 * <p>
 * Cada mensaje del chat tendrá asociado un tipo, un contenido y una referencia
 * al usuario que lo envío dentro del chat.
 * </p>
 *
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.1.1
 */

public class ChatMessage implements Serializable {
	
	/** Identificador único para serializar los mensajes del chat antes de enviarlos. */
	private static final long serialVersionUID = 7467237896682458959L;
	
	/** Tipo del mensaje dentro del chat. */
	private MessageType messageType;
	
	/** Contenido del mensaje dentro del chat. */
	private String messageContent;
	
	/** Referencia al usuario que envío el mensaje dentro del chat. */
	private String userSender;
	
	/**
	 * Crea e inicializa un nuevo mensaje dentro del chat con una referencia al usuario
	 * que lo envío, su tipo y su contenido.
	 * 
	 * @param userSender - Referencia al usuario que envío el mensaje dentro del chat.
	 * @param messageType - Tipo del mensaje dentro del chat.
	 * @param messageContent - Contenido del mensaje dentro del chat.
	 */
	public ChatMessage(String userSender, MessageType messageType, String messageContent) {
		this.setUserSender(userSender);
		this.setMessageType(messageType);
		this.setMessageContent(messageContent);
	}
	
	/**
	 * Devuelve la referencia al usuario que envío el mensaje dentro del chat.
	 * 
	 * @return La referencia al usuario que envío el mensaje dentro del chat.
	 */
	public String getUserSender() {
		return this.userSender;
	}
	
	/**
	 * Establece y asigna una nueva referencia al usuario que envío el mensaje.
	 * 
	 * @param userSender - Nueva referencia del usuario que envío el mensaje.
	 */
	public void setUserSender(String userSender) {
		this.userSender = userSender;
	}
	
	/**
	 * Devuelve el tipo asociado al mensaje dentro del chat.
	 * 
	 * @return El tipo asociado al mensaje.
	 */
	public MessageType getMessageType() {
		return this.messageType;
	}
	
	/**
	 * Establece y asigna un nuevo tipo al mensaje dentro del chat.
	 * 
	 * @param messageType - El nuevo tipo del mensaje dentro del chat.
	 */
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	
	/**
	 * Obtiene el contenido asociado al mensaje del chat.
	 * 
	 * @return El contenido del mensaje.
	 */
	public String getMessageContent() {
		return this.messageContent;
	}
	
	/**
	 * Establece y asigna un nuevo contenido al mensaje.
	 * 
	 * @param messageContent - El nuevo contenido del mensaje.
	 */
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
}