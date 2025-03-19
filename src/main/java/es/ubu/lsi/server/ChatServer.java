package es.ubu.lsi.server;

// Importamos las liberías/paquetes para el funcionamiento del servidor.
import java.io.*;
import java.net.*;
import java.util.*;
import es.ubu.lsi.common.*;
import java.text.SimpleDateFormat;

/**
 * Servidor del chat que se encarga de gestionar las conexiones de los usuarios y reenvía
 * los mensajes a todos los usuarios conectados.
 * 
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.2.1
 */
public class ChatServer implements IChatServer {
	
	/** Puerto del servidor encargado de escuchar las nuevas conexiones entrantes. */
	private int serverPort;
	
	/** Lista interna para registrar y gestionar los diferentes usuarios conectados al servidor. */
	private final List<UserSession> connectedUsers;
	
	/** Flag para controlar y gestionar el estado del servidor. */
	private boolean isRunning;
	
	/**
	 * Constructor de la clase ChatServer.
	 * 
	 * @param serverPort Puerto del servidor encargado de escuchar las nuevas conexiones entrantes.
	 */
	public ChatServer(int serverPort) {
		this.serverPort = serverPort;
		this.connectedUsers = new ArrayList<>();
		isRunning = false;
	}
	
	/**
	 * Inicia el servidor y comienza a aceptar nuevas conexiones entrantes.
	 */
	@Override
	public void startServer() {
		isRunning = true;
		// Intentamos iniciar el servidor en el puerto correspondiente.
		try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
			System.out.println("[" + getCurrentTime() + "][SERVER]: Servidor iniciado en el puerto " + serverPort);
			
			// Mientras que el servidor se encuentre activo, escucha y acepta nuevas conexiones entrantes.
			while (isRunning) {
				// Aceptamos las nuevas conexiones y las gestionamos en hilos independientes.
				Socket userSocket = serverSocket.accept();
				UserSession newUser = new UserSession(userSocket);
				addUser(newUser);
				new Thread(newUser).start();
			}
		} catch (IOException e) {
			System.err.println("[" + getCurrentTime() + "][ERROR]: Error al iniciar el servidor: " + e.getMessage());
		}
	}
	
	/**
	 * Detiene el servidor, cerrando todas las comunicaciones abiertas y dejando
	 * de aceptar nuevas conexiones entrantes.
	 */
	@Override
	public void shutdownServer() {
		// Detenemos el servidor para dejar de aceptar nuevas conexiones entrantes.
		isRunning = false;
		
		// Cerramos de forma segura todas las comunicaciones abiertas.
		synchronized (connectedUsers) {
			for (UserSession user : connectedUsers) {
				user.closeConnection();
			}
			connectedUsers.clear();
		}
		System.out.println("[" + getCurrentTime() + "][SERVER]: Servidor detenido y apagado");
	}
	
	/**
	 * Reenvía el mensaje recibido a todos los usuarios conectados al servidor.
	 * 
	 * @param msg Mensaje que se desea reenviar a todos los usuarios conectados al servidor.
	 */
	@Override
	public void sendBroadcastMessage(ChatMessage msg) {
		// Reenvíamos a todos los usuarios conectados el mensaje recibido.
		synchronized (connectedUsers) {
			for (UserSession user : connectedUsers) {
				user.sendMessage(msg);
			}
		}
	}
	
	/**
	 * Establece la conexión con el nuevo usuario y lo agrega a la lista de usuarios
	 * conectados al servidor.
	 * 
	 * @param newUser El nuevo usuario que se conecta al servidor.
	 */
	public void addUser(UserSession newUser) {
		// Establecemos conexión con el usuario y lo agregamos a la lista de conectados.
		synchronized (connectedUsers) {
			connectedUsers.add(newUser);
		}
	}
	
	/**
	 * Desconecta a un usuario del servidor y lo elimina de la lista de usuarios conectados.
	 * 
	 * @param user El usuario que se va ha desconectar del servidor.
	 */
	
	public void removeUser(UserSession user) {
		// Desconectamos al usuario y lo eliminamos de la lista de conectados.
		synchronized (connectedUsers) {
			connectedUsers.remove(user);
		}
	}
	
	/**
	 * Obtiene la hora actual del sistema.
	 * 
	 * @return La hora actual del sistema.
	 */
	public String getCurrentTime() {
		// Formateamos la hora del sistema mostrando horas, minutos y segundos.
		SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
		// Devolvemos la hora del sistema.
		return currentTime.format(new Date());
	}
	
	/**
	 * Hilo encargado de gestionar la sesión y la comunicación de un usuario
	 * de manera independiente.
	 * 
	 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
	 * @version 1.1.0
	 */
	private class UserSession implements Runnable {
		
		/** Socket para gestionar la comunicación con el usuario. */
		private final Socket userSocket;
		
		/** Flujo de entrada para recibir los mensajes entrantes. */
		private ObjectInputStream inputMessage;
		
		/** Flujo de salida para enviar los mensajes. */
		private ObjectOutputStream outputMessage;
		
		/** Nombre del usuario dentro del sistema. */
		private String username;
		
		/**
		 * Constructor de la clase UsuarioSession.
		 * <p>
		 * Crea e inicializa el hilo encargado de gestionar la sesión y la comunicación
		 * de un usuario de manera independiente.
		 * </p>
		 * 
		 * @param userSocket Socket para gestionar la comunicación con el usuario.
		 */
		public UserSession(Socket userSocket) {
			this.userSocket = userSocket;
		}
		
		/**
		 * Gestiona el hilo encargado y responsable de gestión la sesión y la comunicación 
		 * de un usuario.
		 */
		@Override
		public void run() {
			try {
				// Inicializamos los flujos para recibir y enviar los mensajes.
				outputMessage = new ObjectOutputStream(userSocket.getOutputStream());
				inputMessage = new ObjectInputStream(userSocket.getInputStream());
				
				// Recibimos el nombre del usuario como primer mensaje y le damos la bienvenida al chat.
				ChatMessage initialMessage = (ChatMessage) inputMessage.readObject();
				this.username = initialMessage.getMessageSender();
				System.out.println("[" + getCurrentTime() + "][SERVER]: " + username + " se ha unido al chat.");
				sendBroadcastMessage(new ChatMessage("SERVER", MessageType.MESSAGE, username + " se ha unido al chat."));
				
				// Mantenemos la sesión y comunicación hasta el usuario decida desconectarse.
				while (true) {
					// Leemos el mensaje entrante y lo procesamos antes de reenviarlo al resto de usuarios.
					ChatMessage msg = (ChatMessage) inputMessage.readObject();
					if (msg.getMessageType() == MessageType.LOGOUT) {
						System.out.println("[" + getCurrentTime() + "][SERVER]: " + username + " ha salido del chat.");
						sendBroadcastMessage(new ChatMessage("SERVER", MessageType.MESSAGE, username + " ha salido del chat."));
						break;
					}
					sendBroadcastMessage(msg);
				}
			} catch (IOException | ClassNotFoundException e) {
				System.err.print("[" + getCurrentTime() + "][SERVER]: Error en la comunicación con " + username + ": " + e.getMessage());
			} finally {
				// Haya decido desconectarse o haya ocurrido un error, desconectamos al usuario del sistema.
				closeConnection();
			}
		}
		
		/**
		 * Reenvía el mensaje recibido al usuario.
		 * 
		 * @param msg Mensaje que se desea reenviar al usuario.
		 */
		public void sendMessage(ChatMessage msg) {
			try {
				// Comprobamos la conexión con el usuario este activa antes de mandar el mensaje.
				if (outputMessage != null) {
					// Envíamos el mensaje del usuario al servidor del sistema.
					outputMessage.writeObject(msg);
					outputMessage.flush();
				}
			} catch (IOException e) {
				System.err.println("[" + getCurrentTime() + "][ERROR]: Error al enviar el mensaje al usuario " + username + ": " + e.getMessage());
			}
		}
		
		/**
		 * Cierra de manera segura la sesión y comunicación con el usuario.
		 */
		public void closeConnection() {
			try {
				// Intentamos cerrar todos los recursos utilizados.
				if (inputMessage != null) inputMessage.close();
				if (outputMessage != null) outputMessage.close();
				if (userSocket != null) userSocket.close();
			} catch (IOException e) {
				System.err.println("[" + getCurrentTime() + "][ERROR]: Error al cerrar la conexión de " + username + ": " + e.getMessage());
			} finally {
				// Hayamos cerrado correctamente los recursos o haya ocurrido un error, eliminamos al usuario de la lista de conectados.
				removeUser(this);
			}
		}
	}
	
	/**
	 * Método principal de la clase ChatServer.
	 * <p>
	 * Este es el punto de entrada de la clase. Se encarga de inicializar al
	 * servidor del chat y ponerlo en marcha.
	 * </p>
	 * 
	 * @param args Argumentos recibidos de linea de comando (no utilizados en este caso).
	 */
	public static void main(String[] args) {
		// Inicializamos el servidor del chat y lo ponemos en marcha en el puerto 1500.
		final int DEFAULT_SERVER_PORT = 1500;
		ChatServer server = new ChatServer(DEFAULT_SERVER_PORT);
		server.startServer();
	}
}