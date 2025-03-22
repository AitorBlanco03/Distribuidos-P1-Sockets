package es.ubu.lsi.common;

import java.io.Serializable;

/**
 * Representa un mensaje dentro del sistema del chat.
 * <p>
 * Cada mensaje dentro del sistema tendrá asociado un tipo, un contenido y una
 * referencia al usuario que lo envío.
 * 
 * @author Raúl Marticorena
 * @author Joaquin P. Seco
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.1.2
 */
public class ChatMessage implements Serializable {
	
	/** Identificador único para serializar los mensajes antes de enviarlos. */
	private static final long serialVersionUID = 7467237896682458959L;
	
	/** Tipo de mensaje. */
	private MessageType messageType;
	
	/** Contenido del mensaje. */
	private String messageContent;
	
	/** Usuario que envío el mensaje dentro del chat. */
	private String messageSender;
	
	/**
	 * Crea e inicializa un nuevo mensaje dentro del sistema del chat.
	 * <p>
	 * Inicializa el mensaje con un tipo, un contenido y una referencia al usuario
	 * que lo envío.
	 * </p>
	 * 
	 * @param messageSender Referencia al usuario que envío el mensaje dentro del sistema del chat.
	 * @param messageType Tipo del mensaje.
	 * @param messageContent Contenido del mensaje.
	 */
	public ChatMessage(String messageSender, MessageType messageType, String messageContent) {
		this.setMessageSender(messageSender);
		this.setMessageType(messageType);
		this.setMessageContent(messageContent);
	}
	
	/**
	 * Devuelve la referencia al usuario que envío el mensaje dentro del sistema
	 * del chat.
	 * 
	 * @return La referencia al usuario que envío el mensaje dentro del sistema del chat.
	 */
	public String getMessageSender() {
		return this.messageSender;
	}
	
	/**
	 * Establece y asigna la referencia al usuario que envío el mensaje dentro del
	 * sistema del chat.
	 * 
	 * @param messageSender La referencia al usuario que envío el mensaje dentro del sistema del chat.
	 */
	public void setMessageSender(String messageSender) {
		this.messageSender = messageSender;
	}
	
	/**
	 * Devuelve el tipo del mensaje.
	 * 
	 * @return El tipo del mensaje.
	 */
	public MessageType getMessageType() {
		return this.messageType;
	}
	
	/**
	 * Establece y asigna el tipo del mensaje.
	 * 
	 * @param messageType El tipo del mensaje.
	 */
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	
	/**
	 * Devuelve el contenido del mensaje.
	 * 
	 * @return El contenido del mensaje.
	 */
	public String getMessageContent() {
		return this.messageContent;
	}
	
	/**
	 * Establece y asigna el contenido del mensaje.
	 * 
	 * @param messageContent El contenido del mensaje.
	 */
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
}