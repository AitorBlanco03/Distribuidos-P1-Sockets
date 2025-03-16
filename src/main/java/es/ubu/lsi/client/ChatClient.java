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
 * Cliente de chat que permite a un usuario conectarse a un servidor, enviar
 * mensajes y recibir mensajes en tiempo real.
 * <p>
 * Se encarga de gestionar la comunicación con el servidor, el envío de mensajes
 * y la recepción de respuestas, asegurando una comunicación fluida y eficiente con 
 * el servidor.
 * </p>
 * 
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.3.0
 */

public class ChatClient implements IChatClient {
	
	/** Dirección IP/Nombre del servidor al que se conectará el cliente. */
	private String serverAddress;
	
	/** Nickname/Nombre del usuario dentro del chat. */
	private String userName;
	
	/** Puerto en el que el servidor está escuchando sus conexiones. */
	private int serverPort;
	
	/** Flag para controlar/gestionar la comunicación con el servidor. */
	private boolean isRunning;
	
	/** Socket para controlar/gestionar la comunicación con el servidor. */
	private Socket serverConnection;
	
	/** Flujo de entrada para recibir los mensajes del servidor. */
	private ObjectInputStream receiverMessages;
	
	/** Flujo de salida para enviar los mensajes al servidor. */
	private ObjectOutputStream senderMessages;
	
	/** Scanner para leer los mensajes ingresados por el usuario. */
	private Scanner messageReader;
	
	/**
	 * Constructor de clase.
	 * <p>
	 * Inicializa un nuevo cliente dentro del sistema, estableciendo la dirección IP o el
	 * nombre del servidor, junto con el puerto al que se intentará conectar y el nickname que
	 * indentificará al usuario.
	 * </p>
	 * 
	 * @param serverAddress - Dirección IP o Nombre del servidor al que se realizará la conexión.
	 * @param serverPort - Puerto en el que el servidor estará escuchando las conexiones entrantes.
	 * @param userName - Nombre o Nickname que identificará al usuario dentro del sistema.
	 */
	public ChatClient(String serverAddress, int serverPort, String userName) {
		// Inicializamos la información del servidor que el usuario desea conectarse.
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		// Inicializamos la información del usuario dentro del sistema.
		this.userName = userName;
		// Inicializamos el scanner para leer los mensajes ingresados por el usuario.
		this.messageReader = new Scanner(System.in);
		// Inicialmente el usuario creado no estará conectado al chat.
		this.isRunning = false;
	}
	
	/**
	 * Inicializa y establece la conexión con el servidor.
	 * <p>
	 * Crea e inicializa un socket para comunicarse con el servidor y configura los
	 * diferentes flujos de entrada y salida para enviar y recibir mensajes.
	 * </p>
	 * 
	 * @return true si la conexión con el servidor fue exitosa, false si ocurrió algún error.
	 */
	@Override
	public boolean connect() {
		try {
			// Intentamos establecer conexión con el servidor usa su puerto y dirección.
			serverConnection = new Socket(serverAddress, serverPort);
			// Inicializamos los flujos de entrada y salida para recibir y enviar mensajes.
			receiverMessages = new ObjectInputStream(serverConnection.getInputStream());
			senderMessages = new ObjectOutputStream(serverConnection.getOutputStream());
			// Informamos al usuario que se ha conectado correctamente con el servidor.
			System.out.println("[SYSTEM]: ¡Bienvenido! Se ha establecido la conexión con el servidor.");
			System.out.println("[SERVER]: Preparando su sesión...");
			// Una vez conectado con el servidor, el usuario está conectado al chat.
			isRunning = true;
			// Inicializamos los hilos para enviar y recibir mensajes al servidor.
			new Thread(new UserInputHandler()).start();
			new Thread(new ServerListenerThread()).start();
			// Devolvemos un True, ya que la conexión se ha establecido correctamente.
			return true;
		} catch (UnknownHostException e) {
			System.err.println("[ERROR]: Servidor no encontrado: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("[ERROR]: Error al conectarse con el servidor: " + e.getMessage());
		}
		// Si ocurre algún error durante la conexión con el servidor, devolvemos un False.
		return false;
	}
	
	/**
	 * Envía un mensaje al servidor del chat.
	 * 
	 * @param msg - Mensaje del usuario que se envía al servidor del chat.
	 */
	@Override
	public void sendMessage(ChatMessage msg) {
		try {
			// Comprobamos la conexión con el chat antes de mandar el mensaje.
			if (senderMessages != null && isRunning) {
				// Envíamos el mensaje del usuario al servidor del chat.
				senderMessages.writeObject(msg);
				senderMessages.flush();
			}
		} catch (IOException e) {
			System.err.println("[ERROR]: Error al enviar el mensaje: " + e.getMessage());
		}
	}
	
	/**
	 * Desconecta al usuario del chat y del servidor de forma segura.
	 * <p>
	 * Cierra de forma segura todos los recursos utilizados durante la
	 * comunicación.
	 * </p>
	 */
	@Override
	public void disconnect() {
		try {
			// Enviamos un mensaje de LOGOUT al servidor
	        if (senderMessages != null && isRunning) {
	            ChatMessage logoutMessage = new ChatMessage(userName, MessageType.LOGOUT, "");
	            sendMessage(logoutMessage);
	        }
			// Cerramos de forma segura los flujos de entrada y salida de mensajes.
			if (receiverMessages != null) receiverMessages.close();
			if (senderMessages != null) senderMessages.close();
			// Cerramos de forma segura el scanner para leer los mensajes del usuario.
			if (messageReader != null) messageReader.close();
			// Cerramos de forma segura la conexión con el chat y el servidor.
			if (serverConnection != null) serverConnection.close();
			// Informamos que se ha desconectado de forma segura del chat y servidor.
			System.out.println("[SYSTEM]: Desconectado del chat y del servidor con éxito.");
		} catch (IOException e) {
			System.err.println("[ERROR]: Error al cerrar la conexión: " + e.getMessage());
		} finally {
			// Nos aseguramos de cerrar los hilos de ejecución y el chat.
			isRunning = false;
		}
	}
	
	
	
	/**
	 * Hilo encargado de gestionar la entrada del usuario desde la consola.
	 * <p>
	 * Este hilo se encarga de leer los mensajes que el usuario escribe y los procesa
	 * para enviarlos al servidor.
	 * </p>
	 * 
	 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
	 * @version 1.0.0
	 */
	private class UserInputHandler implements Runnable {
		
		/**
		 * Ejecuta el hilo que escucha y procesa la entrada del usuario.
		 * <p>
		 * Mientras que la conexión con el servidor y el chat esté activa, el hilo
		 * lee los mensajes que el usuario escribe en la consola y los procesa para
		 * enviarlos al servidor.
		 * </p>
		 */
		@Override
		public void run() {
			// Mantenemos activo el hilo mientras que el cliente este conectado.
			while (isRunning) {
				if (messageReader.hasNextLine()) {
					// Leemos y procesamos la entrada del usuario antes de mandarla al servidor.
					String userInput = messageReader.nextLine();
					processUserInput(userInput);
				}
			}
		}
		
		/**
		 * Procesa la entrada del usuario desde consola antes de mandarla
		 * al servidor.
		 * 
		 * @param userInput - Entrada del usuario desde consola.
		 */
		private void processUserInput(String userInput) {
			// Convertimos la entrada del usuario, para ignorar las posibles mayúsculas.
			String lowerCaseInput = userInput.toLowerCase();
			/*
			 * Miramos si el usuario escribe "logout", si es así desconectaremos 
			 * al usuario del chat y del servidor.
			 */
			if (lowerCaseInput.equals("logout")) disconnect();
			/*
			 * Miramos si el usuario escribe "ban [usuario]" y desea bloquear
			 * a otro usuario.
			 */
			else if (lowerCaseInput.startsWith("ban ")) {
				// Extraemos el usuario que se desea bloquear.
				String userToBan = userInput.substring(4).trim().toLowerCase();
				// Comprobamos que el usuario a bloquear no esté vacío.
				if (!userToBan.isEmpty()) {
					banUser(userToBan);
				} else {
					System.err.println("[ERROR]: Se debe especificar un usuario para bloquear.");
				}
			}
			/*
			 * Miramos si el usuario escribe "unban [usuario]" y desea desbloquear
			 * a otro usuario.
			 */
			else if (lowerCaseInput.startsWith("unban ")) {
				// Extraemos el ususario que se desea desbloquear.
				String userToUnban = userInput.substring(6).trim().toLowerCase();
				// Comprobamos que el usuario a desbloquear no esté vacío.
				if (!userToUnban.isEmpty()) {
					unbanUser(userToUnban);
				} else {
					System.err.println("[ERROR]: Se debe especificar un usuario para desbloquear.");
				}
			}
			// Si no es ninguno de los casos anteriores, entonces lo trataremos como un simple mensaje.
			else {
				ChatMessage msg = new ChatMessage(userName, MessageType.MESSAGE, userInput);
				sendMessage(msg);
			}
		}
		
		/**
		 * Informa al servidor que un usuario desea bloquear a otro usuario para
		 * dejar de recibir sus mensajes dentro del chat.
		 * 
		 * @param userToBan - Nombre o Nickname que el usuario desea bloquear.
		 */
		private void banUser(String userToBan) {
	        // Creamos el mensaje de tipo BAN con el nombre del usuario a bloquear.
	        ChatMessage banMsg = new ChatMessage(userName, MessageType.BAN, userToBan);
	        
	        // Enviamos el mensaje de bloqueo al servidor.
	        sendMessage(banMsg);
	    }
		
		/**
		 * Informa al servidor que un usuario desea desbloquear a otro usuario
		 * para volver a recibir sus mensajes dentro del chat.
		 * 
		 * @param userToUnban - Nombre o Nickname que el usuario desea bloquear.
		 */
		private void unbanUser(String userToUnban) {
	        // Creamos el mensaje de tipo UNBAN con el nombre del usuario a desbloquear.
	        ChatMessage unbanMsg = new ChatMessage(userName, MessageType.UNBAN, userToUnban);
	        
	        // Enviamos el mensaje de desbloqueo al servidor.
	        sendMessage(unbanMsg);
	    }
	}
	
	/**
	 * Hilo que se encarga de escuchar y mostrar los mensajes entrantes del servidor.
	 * <p>
	 * Este hilo se encarga de escuchar los mensajes del servidor y mostrarlo
	 * a medida de que estos van llegando.
	 * </p>
	 * 
	 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
	 * @version 1.1.0
	 */
	private class ServerListenerThread implements Runnable {
		
		/**
		 * Ejecuta el hilo que escucha y muestra los mensajes entrantes del servidor.
		 * <p>
		 * Mientras que la conexión con el servidor y el chat esté activa, el hilo
		 * escucha los mensajes del servidor y mostrarlo a medida de que estos van llegando.
		 * </p>
		 */
		@Override
		public void run() {
			// Mantenemos activo el hilo mientras que el cliente este conectado.
			while (isRunning) {
				try {
					// Intentamos recibir el mensaje del servidor.
					ChatMessage receivedMessage = (ChatMessage) receiverMessages.readObject();
					if (receivedMessage != null) {
						// Mostramos por pantalla el mensaje recibido del usuario.
						System.out.println("\n[" + receivedMessage.getUserSender().toUpperCase() + "]: " 
						 + receivedMessage.getMessageContent());
					}
				} catch (IOException | ClassNotFoundException e) {
					System.err.println("[ERROR]: Error al recibir mensaje del servidor: " + e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Main de la clase ChatClient.
	 * <p>
	 * Inicializa un usuario dentro del sistema y lo conecta al servidor y el chat.
	 * </p>
	 * 
	 * @param args - Argumentos recibidos a través de línea de comandos:
	 *<ul>
	 * 	<li><b>Si se proporciona un argumento:</b> El argumento será el nickname del usuario.</li>
	 *	<li><b>Si se proporcionan dos argumentos:</b>
	 *		<ul>
	 *			<li>El primero es la dirección del servidor a conectar.</li>
	 *			<li>El segundo es el nickname del usuario.</li>
	 *		</ul>
	 *	</li>
	 *<ul>
	 */
	public static void main(String[] args) {
		// Definimos los valores por defecto para la conexión con el servidor.
		final String DEFAULT_SERVER_ADDRESS = "localhost";
		final int DEFAULT_SERVER_PORT = 1500;
		// Comprobamos la cantidad de argumentos ingresados a través de la línea de comandos.
		if (args.length < 1 || args.length > 2) {
			System.err.println("[ERROR]: Uso incorrecto. Formato esperado:\n"
					+ "\t- 1 Argumento -> Nickname del usuario.\n"
					+ "\t- 2 Argumentos -> Dirección del servidor y nickname del usuario.");
			System.exit(1);
		}
		// Asignamos los valores en función de los argumentos proporcionados.
		String serverAddress = (args.length == 2) ? args[0] : DEFAULT_SERVER_ADDRESS;
		String userName = (args.length == 2) ? args[1] : args[0];
		// Creamos un nuevo usuario con sus correspondientes datos.
		ChatClient newUser = new ChatClient(serverAddress, DEFAULT_SERVER_PORT, userName);
		// Intentamos conectar el usuario al servidor correspondiente.
		if (newUser.connect()) {
			System.out.println("[SYSTEM]: Conexión establecida. ¡Estás listo para comenzar a chatear!");
			// Enviamos al servidor el username del usuario para iniciar la comunicación.
			ChatMessage loginMessage = new ChatMessage(userName, MessageType.MESSAGE, "");
            newUser.sendMessage(loginMessage);
		} else {
			System.err.println("[SYSTEM]: Saliendo del sistema...");
			System.exit(1);
		}
	}
}