package es.ubu.lsi.client;

import java.io.*;
import java.net.*;
import java.util.*;
import es.ubu.lsi.common.*;
import java.text.SimpleDateFormat;

/**
 * Cliente de chat que permite a un usuario conectarse a un servidor, enviar y recibir
 * mensajes en tiempo real.
 * <p>
 * Se encarga de gestionar la comunicación con el servidor, el envío y la recepción de
 * mensajes, asegurando una comunicación fluida y eficiente con el servidor.
 * </p>
 * 
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.3.1
 */
public class ChatClientImpl implements ChatClient {
	
	/** Dirección IP o Nombre del servidor al que se conectará el usuario. */
	private String serverHost;
	
	/** Nombre del usuario dentro del sistema. */
	private String username;
	
	/** Puerto del servidor donde estará escuchando las conexiones entrantes. */
	private int serverPort;
	
	/** Flag para controlar y gestionar la comunicación con el servidor. */
	private boolean isConnected;
	
	/** Socket para gestionar la comunicación del usuario con el servidor. */
	private Socket socketConnection;
	
	/** Flujo de entrada para recibir los nuevos mensajes provenientes del servidor. */
	private ObjectInputStream inputMessage;
	
	/** Flujo de salida para enviar los mensajes al servidor. */
	private ObjectOutputStream outputMessage;
	
	/** Scanner para leer los mensajes de los usuarios ingresados desde la consola. */
	private Scanner messageReader;
	
	/**
	 * Constructor de la clase ChatClient.
	 * 
	 * @param serverHost Dirección IP o Nombre del servidor al que el usuario quiere conectarse.
	 * @param serverPort Puerto del servidor donde estará escuchando las conexiones entrantes.
	 * @param username Nombre del usuario dentro del sistema.
	 */
	public ChatClientImpl(String serverHost, int serverPort, String username) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.username = username;
		this.messageReader = new Scanner(System.in);
		this.isConnected = false;
	}
	
	/**
	 * Establece conexión con el servidor del sistema.
	 * 
	 * @return true si la conexión se realizó con éxito, false en caso contrario.
	 */
	@Override
	public boolean connect() {
		try {
			// Intentamos establecer conexión e inicializamos los recursos para comunicarse con el servidor.
			socketConnection = new Socket(serverHost, serverPort);
			inputMessage = new ObjectInputStream(socketConnection.getInputStream());
			outputMessage = new ObjectOutputStream(socketConnection.getOutputStream());
			
			System.out.println("[" + getCurrentTime() + "][SYSTEM]: ¡Bienvenido! Se ha establecido la conexión con el servidor.");
			System.out.println("[" + getCurrentTime() + "][SERVER]: Preparando su sesión...");
			isConnected = true;
			
			// Inicializamos los hilos para enviar y recibir mensajes al servidor.
			new Thread(new ChatClientSender()).start();
			new Thread(new ChatClientListener()).start();
			return true;
		} catch (UnknownHostException e) {
			System.err.println("[" + getCurrentTime() + "][ERROR]: Abortando conexión. Servidor no encontrado: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("[" + getCurrentTime() + "][ERROR]: Error al conectarse con el servidor: " + e.getMessage());
		}
		// Si ocurre algún error durante la conexión, interpretaremos que la conexión ha fallado.
		return false;
	}
	
	/**
	 * Finaliza la conexión con el servidor de forma segura cerrando todos los
	 * recursos utilizados durante su sesión.
	 */
	@Override
	public void disconnect() {
		try {
			System.out.println("[" + getCurrentTime() + "][SERVER]: Cerrando su sesión...");
			// Antes de desconectar al usuario, avisamos al servidor para evitar posibles problemas.
			if (outputMessage != null && isConnected) {
				ChatMessage logoutMessage = new ChatMessage(username, MessageType.LOGOUT, "");
				sendMessage(logoutMessage);
			}
			
			// Intentamos cerrar de forma segura todos los recursos utilizados durante su sesión.
			if (inputMessage != null) inputMessage.close();
			if (outputMessage != null) outputMessage.close();
			if (messageReader != null) messageReader.close();
			if (socketConnection != null) socketConnection.close();
			
			System.out.println("[" + getCurrentTime() + "][SYSTEM]: ¡Adios! Se ha cerrado conexión correctamente con el servidor.");
		} catch (IOException e) {
			System.err.println("[" + getCurrentTime() + "][ERROR]: Error al cerrar la conexión: " + e.getMessage());
		} finally {
			// Nos aseguramos de cerrar la conexión con el servidor independiente que haya o no ocurrido algún error.
			isConnected = false;
		}
	}
	
	/**
	 * Envía un mensaje al servidor del sistema.
	 * 
	 * @param msg Mensaje que se desea enviar al servidor del sistema.
	 */
	@Override
	public void sendMessage(ChatMessage msg) {
		try {
			// Comprobamos la conexión con el servidor este activa antes de mandar el mensaje.
			if (outputMessage != null && isConnected) {
				// Envíamos el mensaje del usuario al servidor del sistema.
				outputMessage.writeObject(msg);
				outputMessage.flush();
			}
		} catch (IOException e) {
			System.err.println("[" + getCurrentTime() + "][ERROR]: Error al enviar el mensaje al servidor: " + e.getMessage());
		}
	}
	
	/**
	 * Obtiene la hora actual del sistema.
	 * 
	 * @return La hora actual del sistema.
	 */
	public static String getCurrentTime() {
		// Formateamos la hora del sistema mostrando horas, minutos y segundos.
		SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
		// Devolvemos la hora del sistema.
		return currentTime.format(new Date());
	}
	
	/**
	 * Hilo responsable de leer los mensajes del usuario desde la consola, procesarlos y
	 * luego posteriormente enviarlos al servidor del sistema.
	 * 
	 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
	 * @version 1.1.0
	 */
	private class ChatClientSender implements Runnable {
		
		/**
		 * Ejecuta el hilo, y se encarga de leer y procesar los mensajes del usuario desde
		 * consola y enviarlos al servidor.
		 */
		@Override
		public void run() {
			// Mantenemos activo el hilo mientras que el usuario esté conectado al servidor.
			while (isConnected) {
				if (messageReader.hasNextLine()) {
					
					// Leemos y procesamos el mensaje del usuario antes de enviarlo al servidor.
					String userMessage = messageReader.nextLine();
					processUserMessage(userMessage);
				}
			}
		}
		
		/**
		 * Procesa el mensaje del usuario desde la consola antes de enviarlo al
		 * servidor del sistema.
		 */
		private void processUserMessage(String userMessage) {
			// Primero pasamos el mensaje a mínusculas para ignorar posibles mayúsculas dentro del mensaje.
			String lowerCaseMessage = userMessage.trim().toLowerCase();
			
			// Si el mensaje es "logout", desconectamos al usuario del servidor del sistema.
			if ("logout".equals(lowerCaseMessage)) disconnect();
			
			// Si el mensaje empieza con el comando "ban", procesamos el mensaje para obtener el usuario a bloquear.
			else if (lowerCaseMessage.startsWith("ban")) {
				if (lowerCaseMessage.equalsIgnoreCase("ban")) {
					System.err.println("[" + getCurrentTime() + "][ERROR]: Debes especificar el usuario a bloquear.");
				} else if (!lowerCaseMessage.startsWith("ban ")) {
					sendMessage(new ChatMessage(username, MessageType.MESSAGE, userMessage));
				} else {
					processBanCommand(userMessage.substring(4).trim());
				}
			}
			
			// Si el mensaje empieza con el comando "unban", procesamos el mensaje para obtener el usuario a desbloquear.
			else if (lowerCaseMessage.startsWith("unban")) {
				if (lowerCaseMessage.equalsIgnoreCase("unban")) {
					System.err.println("[" + getCurrentTime() + "][ERROR]: Debes especificar el usuario a desbloquear.");
				} else if (!lowerCaseMessage.startsWith("unban ")) {
					sendMessage(new ChatMessage(username, MessageType.MESSAGE, userMessage));
				} else {
					processUnbanCommand(userMessage.substring(6).trim());
				}
			}
			
			// Cualquier otro mensaje, se enviará directamente al servidor del sistema.
			else sendMessage(new ChatMessage(username, MessageType.MESSAGE, userMessage));
		}
		
		/**
		 * Procesa el mensaje de bloqueo para obtener el usuario a bloquear.
		 * 
		 * @param userToBan Usuario que se desea bloquear.
		 */
		private void processBanCommand(String userToBan) {
			// Si el usuario a bloquear es el propio usuario, mandamos un mensaje de error.
			if (userToBan.equalsIgnoreCase(username)) {
				System.err.println("[" + getCurrentTime() + "][ERROR]: No puedes bloquearte a ti mismo.");
			}
			
			// Si es otro usuario, lo enviamos al servidor del sistema.
			else sendMessage(new ChatMessage(username, MessageType.BAN, userToBan));
		}
		
		/**
		 * Procesa el mensaje de desbloqueo para obtener el usuario a desbloquear.
		 * 
		 * @param userToUnban Usuario que se desea desbloquear.
		 */
		private void processUnbanCommand(String userToUnban) {
			// Si el usuario a bloquear es el propio usuario, mandamos un mensaje de error.
			if (userToUnban.equalsIgnoreCase(username)) {
				System.err.println("[" + getCurrentTime() + "][ERROR]: No puedes desbloquearte a ti mismo.");
			}
			
			// Si es otro usuario, lo envíamos al servidor del sistema.
			else sendMessage(new ChatMessage(username, MessageType.UNBAN, userToUnban));
		}
	}
	
	/**
	 * Hilo que se encarga de escuchar y recibir los mensajes del servidor y mostrarlos a medida
	 * que estos van llegando.
	 * 
	 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
	 * @version 1.1.1
	 */
	private class ChatClientListener implements Runnable {
		
		/**
		 * Ejecuta el hilo que se encarga de escuchar y recibir los mensajes así como
		 * mostrarlos a medida que estos van llegando.
		 */
		@Override
		public void run() {
			// Mantenemos activo este hilo mientras que el usuario este conectado al servidor.
			while (isConnected) {
				try {
					// Intentamos recibir el mensaje recibido del servidor.
					ChatMessage newMessage = (ChatMessage) inputMessage.readObject();
					if (newMessage != null) {
						// Mostramos por pantalla el mensaje recibido del servidor del sistema.
						System.out.println("[" + getCurrentTime() + "][" + newMessage.getMessageSender().toUpperCase() + "]: " 
								 + newMessage.getMessageContent());
					}
				} catch (IOException | ClassNotFoundException e) {
					System.err.println("[" + getCurrentTime() + "][ERROR]: Error al recibir mensaje del servidor: " + e.getMessage());
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
	 *</ul>
	 */
	public static void main(String[] args) {
		// Definimos los valores por defecto para la conexión con el servidor.
		final String DEFAULT_SERVER_ADDRESS = "localhost";
		final int DEFAULT_SERVER_PORT = 1500;
		
		// Comprobamos la cantidad de argumentos ingresados a través de la línea de comandos.
		if (args.length < 1 || args.length > 2) {
			System.err.println("[" + getCurrentTime() + "][ERROR]: Uso incorrecto. Formato esperado:\n"
					+ "\t- 1 Argumento -> Nickname del usuario.\n"
					+ "\t- 2 Argumentos -> Dirección del servidor y nickname del usuario.");
			System.exit(1);
		}
		
		// Asignamos los valores en función de los argumentos proporcionados.
		String serverAddress = (args.length == 2) ? args[0] : DEFAULT_SERVER_ADDRESS;
		String userName = (args.length == 2) ? args[1] : args[0];
		
		// Creamos un nuevo usuario con sus correspondientes datos.
		ChatClientImpl newUser = new ChatClientImpl(serverAddress, DEFAULT_SERVER_PORT, userName);
		
		// Intentamos conectar el usuario al servidor correspondiente.
		if (newUser.connect()) {
			// Enviamos al servidor el username del usuario para iniciar la comunicación.
			ChatMessage loginMessage = new ChatMessage(userName, MessageType.LOGIN, "");
            newUser.sendMessage(loginMessage);
		} else {
			System.err.println("[" + getCurrentTime() + "][SYSTEM]: Saliendo del sistema...");
			System.exit(1);
		}
	}
}