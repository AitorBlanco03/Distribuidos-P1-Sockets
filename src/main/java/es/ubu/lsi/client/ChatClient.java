package es.ubu.lsi.client;

// Importamos las librerías/paquetes necesarias para su ejecución.
import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.MessageType;

/**
 * Representa un usuario que se conecta al servidor del chat, permitiéndole
 * enviar y recibir mensajes a través de una comunicación basada en sockets.
 * <p>
 * Esta clase se encarga de gestionar la comunicación con el servidor, el envio de mensajes
 * y la recepciones de las respuestas, asegurando una interacción fluida con el servidor.
 * Además, proporciona métodos para establecer y cerrar la conexión de forma segura.
 * </p>
 * 
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.2.0
 */

public class ChatClient implements IChatClient {
	
	/** Dirección IP/Nombre del servidor al que se conectará el cliente. */
	private String serverAddress;
	
	/** Nickname/Nombre del usuario dentro del sistema. */
	private String userName;
	
	/** Puerto en el que el servidor está escuchando conexiones. */
	private int serverPort;
	
	/** Flag para controlar la comunicación con el chat. */
	private boolean isRunning;
	
	/** Socket para gestionar la comunicación con el servidor. */
	private Socket serverConnection;
	
	/** Flujo de salida para enviar los mensajes del usuario al servidor.  */
	private ObjectOutputStream senderMessages;
	
	/** Flujo de entrada para recibir los mensajes del servidor. */
	private ObjectInputStream receiverMessages;
	
	/** Scanner para leer los mensajes ingresados por el usuario desde la consola. */
	private Scanner messageReader;
	
	
	/**
	 * Constructor de la clase.
	 * <p>
	 * Inicializa un nuevo cliente dentro del sistema con la dirección y el puerto
	 * del servidor al que se intentará conectar, junto a su nickname.
	 * </p>
	 * 
	 * @param serverAddress - Dirección IP/Nombre del servidor al que se intentará conectar.
	 * @param serverPort - Puerto en el que servidor estará escuchando las conexiones.
	 * @param userName - Nombre/Nickname del usuario que identificará al usuario dentro del sistema.
	 */
	public ChatClient(String serverAddress, int serverPort, String userName) {
		// Inicializamos la información del servidor que el usuario se conectará.
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		
		// Inicializamos la información dentro del sistema.
		this.userName = userName;
		
		// Inicializamos el Scanner para leer los mensajes ingresados por el usuario desde la consola.
		this.messageReader = new Scanner(System.in);
		
		// Inicialmente, el chat no está en ejecución.
		this.isRunning = false;
	}
	
	/**
	 * Inicializa y establece la conexión del usuario con el servidor.
	 * <p>
	 * Crea un socket para comunicarse con el servidor y configura los flujos de
	 * entrada y salida para el envio y recepción de mensajes.
	 * <p>
	 * 
	 * @return true si la conexión del usuario con el servidor fue exitosa, false si ocurrió un error.
	 */
	@Override
	public boolean connect() {
		try {
			// Intentamos establecer conexión con el servidor con la dirección y el puerto recibidos.
			serverConnection = new Socket(serverAddress, serverPort);
			
			// Inicializamos los flujos de entrada para recibir y enviar los mensajes.
			senderMessages = new ObjectOutputStream(serverConnection.getOutputStream());
			receiverMessages = new ObjectInputStream(serverConnection.getInputStream());
			
			// Informamos que el usuario se ha conectado con el servidor.
			System.out.println("[SYSTEM]: ¡Bienvenido! Se ha establecido la conexión con el servidor.");
			System.out.println("[SERVER]: Preparando su sesión...");
			
			// Una vez conectado con el servidor, el chat está en ejecución e inicializamos su listener.
			isRunning = true;
			new Thread(new ChatListener(userName)).start();
			
			// Devolvemos un true, ya que la conexión con el servidor se ha establecido correctamente.
			return true;
		} catch (UnknownHostException e) {
			System.err.println("[ERROR]: Servidor no encontrado: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("[ERROR]: Error al conectarse con el servidor: " + e.getMessage());
		}
		// Si ocurre algún error devolvemos un false, ya que la conexión con el servidor ha fallado.
		return false;
	}
	
	/**
	 * Envía un mensaje al servidor del chat.
	 * <p>
	 * El mensaje debe de ser tipo {@link ChatMessage} e incluir tanto la referencia del usuario
	 * emisor así como el tipo y el contenido del mensaje. Se envía a través del flujo de salida del socket.
	 * </p>
	 * 
	 * @param msg - El mensaje del usuario que se enviaría al servidor.
	 */
	@Override
	public void sendMessage(ChatMessage msg) {
		try {
			// Comprobamos que la conexión con el servidor está activa antes de proceder a mandar el mensaje.
			if (senderMessages != null && isRunning) {
				// Enviamos el mensaje del usuario al servidor.
				senderMessages.writeObject(msg);
				senderMessages.flush();
			}
		} catch (IOException e) {
			System.err.println("[ERROR]: Error al enviar el mensaje: " + e.getMessage());
		}
	}
	
	/**
	 * Desconecta al usuario del servidor de forma segura.
	 * <p>
	 * Este método, cierra de forma segura todos los recursos utilizados durante la comunicación
	 * con el servidor antes de cerrar la sesión del usuario.
	 * </p>
	 */
	@Override
	public void disconnect() {
		try {
			// Si los flujos de entrada y salida de mensajes estan abiertos, los cerramos de manera segura.
			if (receiverMessages != null) receiverMessages.close();
			if (senderMessages != null) senderMessages.close();
			
			// Si el scanner para leer mensajes esta abierto, lo cerramos de manera segura.
			if (messageReader != null) messageReader.close();
			
			// Si la conexión con el servidor esta abierta, la cerramos de manera segura.
			if (serverConnection != null) serverConnection.close();
			
			// Informamos que ha cerrado la sesión del usuario se ha desconectado correctamente.
			System.out.println("[SYSTEM]: Te has desconectado correctamente del servidor.");
		} catch (IOException e) {
			// Si ocurre un error al cerrar los recursos, informamos que ha ocurrido un error.
			System.err.println("[ERROR]: Error al cerrar la conexión: " + e.getMessage());
		} finally {
			// Nos aseguramos que el chat del usuario ya no este en ejecución.
			isRunning = false;
		}
	}
	
	/**
	 * Bloquea a otro usuario para dejar de recibir sus mensajes por consola.
	 * 
	 * @param userToBan - El nombre del usuario que se desea bloquear.
	 */
	private void banUser(String userToBan) {
		// Informamos que el bloqueo ha sido bloqueado.
		System.out.println("[SYSTEM]: Has bloqueado a " + userToBan);
		
		// Creamos y mandamos al servidor, con solo el usuario objetivo en el contenido del mensaje.
		ChatMessage banMsg = new ChatMessage(userName, MessageType.BAN, userToBan);
		sendMessage(banMsg);
	}
	
	/**
	 * Desbloquea un usuario previamente bloqueado.
	 * 
	 * @param userToUnban - El nombre del usuario que se desea desbloquear.
	 */
	private void unbanUser(String userToUnban) {
		// Informamos que el bloqueo ha sido bloqueado.
		System.out.println("[SYSTEM]: Has desbloqueado a " + userToUnban);
		
		// Creamos y mandamos al servidor, con solo el usuario objetivo en el contenido del mensaje.
		ChatMessage unbanMsg = new ChatMessage(userName, MessageType.UNBAN, userToUnban);
		sendMessage(unbanMsg);
	}
	
	/**
	 * Clase interna que define el hilo para escuchar tanto la entrada de la consola 
	 * como los mensajes del servidor.
	 * <p>
	 * Este hilo procesa la entrada del usuario y los mensajes que el servidor envía al
	 * cliente.
	 * </p>
	 * 
	 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
	 * @version 1.0.0
	 */
	private class ChatListener implements Runnable {
		
		/** Nickname/Nombre del usuario dentro del sistema. */
		private String userName;
		
		/**
		 * Constructor para el ChatListener.
		 * <p>
		 * Hilo para escuchar tanto la entrada de la consola  como los 
		 * mensajes del servidor.
		 * </p>
		 * 
		 * @param userID - El identificador único del usuario dentro del sistema.
		 */
		public ChatListener(String userName) {
			this.userName = userName;
		}
		
		/**
		 * Escucha indefinidamente los mensajes enviados desde el servidor y muestra
		 * esos mensajes en la consola. El hilo continuará ejecutándose hasta que el
		 * usuario se desconecta con el comando 'logout'.
		 */
		@Override
		public void run() {
			while (isRunning) {
				try {
					// Comprobamos si hay algún mensaje pendientes desde el servidor.
					if (receiverMessages.available() > 0) {
						// Leemos y mostramos el mensaje recibido del servidor.
						ChatMessage recievedMessage = (ChatMessage) receiverMessages.readObject();
						if (recievedMessage != null) {
							System.out.println("[" + recievedMessage.getUserSender().toUpperCase() + "]: " + recievedMessage.getMessageContent());
						}
					}
					System.out.println("[YOU]: ");
					
					// Si el usuario ha escrito algo en la consola, lo leemos y procesamos antes de mandarlo al servidor.
					if (messageReader.hasNextLine()) {
						String userInput = messageReader.nextLine();
						processUserInput(userInput);
					}
					
				} catch (IOException | ClassNotFoundException e) {
					// Si ocurre un error al recibir o procesar el mensaje, lo mostramos en la consola.
					System.err.println("[ERROR]: Error al recibir o al procesar el mensaje: " + e.getMessage());
				}
			}
		}
		
		/**
		 * Procesa la entrada del usuario. Dependiendo de lo que el usuario ingrese, ejecutará
		 * diferentes acciones como enviar enviar mensajes, bloquear o desbloquear usuarios.
		 * 
		 * @param userInput - La entrada del usuario desde la consola.
		 */
		private void processUserInput(String userInput) {
			// Si el usuario escribe "logout", cerramos la conexión con el servidor.
			if (userInput.equalsIgnoreCase("logout")) {
				disconnect();
			}
			
			// Si el usuario quiere bloquear a otro usuario (ban [usuario]), notificamos al servidor los agentes involucrados.
			else if (userInput.startsWith("ban ")) {
				// Obtenemos el usuario que se desea bloquear.
				String userToBan = userInput.split(" ")[1];
				banUser(userToBan);
			}
			
			// Si el usuario quiere desbloquear a otro usuario (unban [usuario]), notificamos al servidor los agentes involucrados.
			else if (userInput.startsWith("unban ")) {
				// Obtenemos el usuario que se desea desbloquar.
				String userToUnban = userInput.split(" ")[1];
				unbanUser(userToUnban);
			}
			
			// Si no es ninguno de los comandos anteriores, lo tratamos como un mensaje normal/estándar.
			else {
				// Creamos un mensaje de tipo MESSAGE con el contenido del usuario.
				ChatMessage msg = new ChatMessage(userName, MessageType.MESSAGE, userInput);
				// Enviamos el mensaje del usuario con el servidor.
				sendMessage(msg);
			}
		}
	}
	
	/**
	 * Main de la clase ChatClient.
	 * <p>
	 * Se encarga de inicializar un nuevo usuario dentro del sistema.
	 * </p>
	 * @param args - Argumentos recibido a traves de línea de comando.
	 */
	public static void main(String[] args) {
		// Comprobamos que se haya proporcionado al menos un argumento que es el nickname del usuario.
		if (args.length < 1) {
			System.err.println("[ERROR]: Debes proporcionar al menos tu nickame.");
			System.exit(1);
		}
		
		// Definimos la dirección del servidor, por defecto es localhost.
		String serverAddress = "localhost";
		
		// Si se ha proporcionado la dirección del servidor, la utilizaremos.
		if (args.length >= 2) serverAddress = args[0];
		
		// Obtenemos el último argumento recibido que será el nickname del usuario.
		String userName = args[args.length - 1];
		
		// Definimos el puerto del servidor, por defecto es 1500.
		int serverPort = 1500;
		
		// Creamos un nuevo usuario dentro del sistema usando los datos proporcionados.
		ChatClient newUser = new ChatClient(serverAddress, serverPort, userName);
		
		// Intentamos conectar al usuario al servidor del sistema.
		if (newUser.connect()) {
			System.out.println("[SYSTEM]: Conexión establecida. ¡Estás listo para comenzar a chatear!");
		} else {
			System.err.println("[SYSTEM]: Saliendo...");
		}
	}
}