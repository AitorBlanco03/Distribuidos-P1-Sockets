package es.ubu.lsi.server;

import java.io.*;
import java.net.*;
import java.util.*;
import es.ubu.lsi.common.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.locks.*;

/**
 * Servidor del chat que gestiona las conexiones de los usuarios y el
 * reenvío de mensajes, permitiendo además a los usuarios bloquear a
 * otros usuarios.
 *
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.4.0
 */
public class ChatServer implements IChatServer {
	
	/** Puerto del servidor encargado de escuchar y aceptar las conexiones entrantes. */
	private int serverPort;
	
	/** Registro para almacenar y gestionar los diferentes usuarios conectados al servidor. */
	private final List<UserSession> connectedUsers;
	
	/** Registro para almacenar y gestionar los bloqueos de cada uno de los usuarios conectados. */
	private final Map<String, HashSet<String>> blockedUsers;
	
	/** Flag para controlar y gestionar el estado del servidor. */
	private boolean isRunning;
	
	/** Locks para controlar el acceso de los recusos compartidos del servidor. */
	private final ReadWriteLock systemLock = new ReentrantReadWriteLock();
	private final Lock readLock = systemLock.readLock();
	private final Lock writeLock = systemLock.writeLock();
	
	/**
	 * Constructor de la clase ChatServer.
	 * <p>
	 * Se encarga de crear e inicializar una instancia del servidor del chat.
	 * </p>
	 * 
	 * @param serverPort Puerto del servidor encargado de escuchar y aceptar nuevas conexiones entrantes.
	 */
	public ChatServer(int serverPort) {
		this.serverPort = serverPort;
		this.connectedUsers = new ArrayList<>();
		this.blockedUsers = new HashMap<>();
		this.isRunning = false;
	}
	
	/**
	 * Inicia el servidor y comienza a escuchar y aceptar las nuevas conexiones entrantes.
	 * <p>
	 * El servidor comienza a escuchar y aceptar las nuevas conexiones entrantes del servidor.
	 * Por cada nueva conexión acepta, se crea una nueva sesión para gestionar su comunicación de manera independiente.
	 * Este proceso se repite indefinidamente siempre y cuando el servidor se encuentre activo.
	 * </p>
	 */
	@Override
	public void startServer() {
		// Intentamos iniciar el servidor del chat y su puerto para empezar a escuchar las nuevas conexiones entrantes.
		try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
			isRunning = true;
			System.out.println("[" + getCurrentTime() + "][SERVER]: Servidor iniciado en el puerto " + serverPort);
			
			// Mientras que el usuario este activo, escucha y acepta las nuevas conexiones entrantes.
			while(isRunning) {
				// Para cada conexiones aceptada, creamos una nueva sesión para gestionar la de manera independiente.
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
	 * Detiene el servidor y deja de escuchar y aceptar las nuevas conexiones entrantes.
	 * <p>
	 * Deja de escuchar y aceptar nuevas conexiones entrantes, y también se encarga de
	 * cerrar todos los recursos utilizados para las sesiones y comunicaciones con los
	 * usuarios.
	 * </p>
	 */
	@Override
	public void shutdownServer() {
		// Detenemos el servidor para dejar de escuchar y aceptar nuevas conexiones entrantes.
		isRunning = false;
		
		// Obtenemos el bloqueo de escritura para liberar todos los recursos del servidor.
		writeLock.lock();
		try {
			// Cerramos todos los recursos utilizados para las sesiones y comunicaciones con los usuarios.
			for (UserSession user : connectedUsers) {
				user.closeConnection();
			}
			connectedUsers.clear();
			blockedUsers.clear();
		} finally {
			// Finalmente liberamos el bloqueo de escritura utilizado.
			writeLock.unlock();
		}
	}
	
	/**
	 * Reenvía un mensaje recibido a todos los usuarios conectados, excepto a aquellos usuarios que
	 * tienen o han bloqueado el remitente del mensaje.
	 * 
	 * @param msg Mensaje recibido que se desea reenviar dentro del sistema.
	 */
	@Override
	public void sendBroadcastMessage(ChatMessage msg) {
		// Obtenemos el bloqueo de lectura para leer todos los usuarios conectados al sistema.
		readLock.lock();
		try {
			// Reenvíamos el mensajes para todos menos aquellos que tiene bloqueado el remitente.
			for (UserSession user : connectedUsers) {
				if (!isBlocked(msg.getMessageSender(), user.getUsername())) {
					user.sendMessage(msg);
				}
			}
		} finally {
			// Finalmente liberamos el bloqueo de lectura utilizado.
			readLock.unlock();
		}
	}
	
	/**
	 * Añade un nuevo usuario al sistema, actualizando los registros para añadir la
	 * información del usuario dentro del sistema.
	 * 
	 * @param newUser Nuevo usuario al sistema que se desea añadir.
	 */
	public void addUser(UserSession newUser) {
		// Obtenemos el bloqueo de escritura para añadir nueva información al sistema.
		writeLock.lock();
		try {
			// Añadimos la información del nuevo usuario dentro del sistema.
			connectedUsers.add(newUser);
			blockedUsers.putIfAbsent(newUser.getUsername(), new HashSet<>());
		} finally {
			// Finalmente liberamos el bloqueo de escritura utilizado.
			writeLock.unlock();
		}
	}
	
	/**
	 * Elimina un usuario del sistema, actualizando los registros para eliminar la
	 * información que se desconectó del sistema.
	 * 
	 * @param user Usuario que se desconectó del sistema.
	 */
	public void removeUser(UserSession user) {
		// Obtenemos el bloqueo de escritura para eliminar información del sistema.
		writeLock.lock();
		try {
			// Eliminamos la información del usuario dentro del sistema.
			connectedUsers.remove(user);
			blockedUsers.remove(user.getUsername());
		} finally {
			// Finalmente liberamos el bloqueo de escritura utilizado.
			writeLock.unlock();
		}
	}
	
	/**
	 * Comprueba si un usuario ha bloqueado a otro dentro del sistema.
	 * 
	 * @param userSender Usuario del sistema que envío el mensaje.
	 * @param userReceiver Usuario del sistema que recibiría el mensaje.
	 * @return true si el receptor del mensaje tiene bloqueado al remitente, false en caso contrario.
	 */
	private boolean isBlocked(String userSender, String userReceiver) {
		// Obtenemos el bloqueo de lectura para consultar la información del posible bloqueo entre usuarios.
		readLock.lock();
		try {
			// Consultamos si el recepto del mensaje tiene bloqueado al remitente.
			return blockedUsers.getOrDefault(userReceiver, new HashSet<>()).contains(userSender);
		} finally {
			// Liberamos el bloqueo de lectura que hemos utilizado.
			readLock.unlock();
		}
	}
	
	/**
	 * Obtiene la hora actual del sistema.
	 * 
	 * @return La hora actual del sistema.
	 */
	public String getCurrentTime() {
		// Formateamos y devolvemos la hora del sistema mostrando "Horas:Minutos:Segundos".
		SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
		return currentTime.format(new Date());
	}
	
	/**
	 * Clase encargada de gestionar la sesión del usuario dentro del sistema, así como
	 * la comunicación entre el usuario y el servidor.
	 * 
	 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
	 * @version 1.3.0
	 */
	public class UserSession implements Runnable {
		
		/** Socket para gestionar la comunicación entre el usuario y el servidor. */
		private final Socket userSocket;
		
		/** Flujo de entrada para recibir los mensajes de los usuarios. */
		private ObjectInputStream inputMessage;
		
		/** Flujo de salida para enviar los mensajes a los usuarios. */
		private ObjectOutputStream outputMessage;
		
		/** Nombre del usuario dentro del sistema. */
		private String username;
		
		/**
		 * Constructor de la clase UserSession.
		 * <p>
		 * Inicializa los recursos necesarios para gestionar la sesión del usuario dentro
		 * del sistema, así como la comunicación entre el usuario y el servidor de manera
		 * independiente.
		 * </p>
		 * 
		 * @param userSocket Socket para gestionar la sesión y comunicación con el usuario.
		 */
		public UserSession(Socket userSocket) {
			this.userSocket = userSocket;
		}
		
		/**
		 * Inicializa un nuevo hilo, y se encarga de gestionar la sesión del usuario
		 * dentro del sistema, así como la comunicación entre el usuario y el servidor
		 * de manera independiente.
		 */
		@Override
		public void run() {
			try {
				// Inicializamos los flujos para recibir y mandar los mensajes a los usuarios.
				outputMessage = new ObjectOutputStream(userSocket.getOutputStream());
				inputMessage = new ObjectInputStream(userSocket.getInputStream());
				
				// Recibimos el nombre del usuario como primer mensaje y le damos la bienvenida al chat.
				ChatMessage initialMessage = (ChatMessage) inputMessage.readObject();
				this.username = initialMessage.getMessageSender();
				System.out.println("[" + getCurrentTime() + "][SERVER]: " + username + " se ha unido al chat.");
				sendBroadcastMessage(new ChatMessage("SERVER", MessageType.MESSAGE, username + " se ha unido al chat."));
				
				// Mantenemos la sesión y la comunicación con el servidor hasta que el usuario decida desconectarse.
				while (true) {
					// Leemos el nuevo mensaje entrante y lo procesamos antes de reenvíarlo al resto de usuarios.
					ChatMessage newMessage = (ChatMessage) inputMessage.readObject();
					handleNewMessage(newMessage);
				}
				
			} catch (IOException | ClassNotFoundException e) {
				System.err.print("[" + getCurrentTime() + "][SERVER]: Error en la comunicación con " + username + ": " + e.getMessage());
			} finally {
				// Haya decido desconectarse o haya ocurrido un error, desconectamos al usuario del sistema.
				closeConnection();
			}
		}
		
		/**
		 * Procesa el mensaje recibido del usuario antes de ser reenviado al
		 * resto de usuarios conectados.
		 * 
		 * @param newMessage Nuevo mensaje recibido que se desea procesar.
		 */
		private void handleNewMessage(ChatMessage newMessage) {
			
			// Procesamos el mensaje según el tipo recibido.
			switch (newMessage.getMessageType()) {
			
				// Si el usuario ha decidido desconectarse, gestionamos su desconexión del sistema y del servidor.
				case LOGOUT:
					handleLogout();
					return;
					
				// Si el usuario desea bloquear a otro, gestionamos el bloqueo dentro del sistema.
				case BAN:
					handleBlock(newMessage.getMessageContent());
					break;
					
					// Si el usuario desea desbloquear a otro, gestionamos el desbloqueo dentro del sistema.
				case UNBAN:
					handleUnblock(newMessage.getMessageContent());
					break;
					
				// Cualquier otro mensaje del usuario, gestionamos como un mensaje normal.
				default:
					sendBroadcastMessage(newMessage);
					break;
			}
		}
		
		/**
		 * Gestiona la desconexión de un usuario dentro del sistema.
		 */
		private void handleLogout() {
			System.out.println("[" + getCurrentTime() + "][SERVER]: " + username + " ha salido del chat.");
			ChatMessage logoutNotification = new ChatMessage("SERVER", MessageType.MESSAGE, username + " ha salido del chat.");
			sendBroadcastMessage(logoutNotification);
		}
		
		/**
		 * Gestiona una solicitud de un bloqueo dentro de un usuario.
		 * 
		 * @param userToBlock Usuario que se desea bloquear dentro del sistema.
		 */
		private void handleBlock(String userToBlock) {
			// Obtenemos el bloqueo de escritura para gestionar la solictud de un bloqueo dentro de un usuario.
			writeLock.lock();
			try {
				// Miramos si el usuario a bloquear no esta conectado, mandamos un mensaje de error al usuario.
				if (!isUserConnected(userToBlock)) {
					ChatMessage errorNotification = new ChatMessage("SERVER", MessageType.MESSAGE, 
							"No se puede bloquear a " + userToBlock + " porque no está conectado.");
					sendMessage(errorNotification);
					return;
				}
				
				// Miramos si el usuario a bloquear ya lo tiene bloqueado.
				blockedUsers.putIfAbsent(username, new HashSet<>());
				HashSet<String> userBlockList = blockedUsers.get(username);
				if (userBlockList.contains(userToBlock)) {
					ChatMessage errorNotification = new ChatMessage("SERVER", MessageType.MESSAGE, 
							"No se puede bloquear a " + userToBlock + " porque ya lo has bloqueado.");
					sendMessage(errorNotification);
					return;
				}
				
				// Si el bloqueo es valido, lo añadimos el usuario es bloqueado y avisamos al sistema.
				userBlockList.add(userToBlock);
				System.out.println("[" + getCurrentTime() + "][SERVER]: " + username + " ha bloqueado a " + userToBlock + ".");
				sendBroadcastMessage(new ChatMessage("SERVER", MessageType.MESSAGE, username + " ha bloqueado a " + userToBlock + "."));
			} finally {
				// Liberamos el bloqueo de escritura que hemos utilizado.
				writeLock.unlock();
			}
		}
		
		/**
		 * Gestiona una solictud de un desbloqueo dentro del sistema.
		 * 
		 * @param userToUnblock Usuario que se desea desbloquear dentro del sistema.
		 */
		private void handleUnblock(String userToUnblock) {
			// Obtenemos el bloqueo de escritura para gestionar la solicitud del desbloqueo dentro del sistema.
			writeLock.lock();
			try {
				// Miramos si el usuario a desbloquear no esta conectado, mandamos un mensaje de error al usuario.
				if (!isUserConnected(userToUnblock)) {
					ChatMessage errorNotification = new ChatMessage("SERVER", MessageType.MESSAGE, 
							"No se puede desbloquear a " + userToUnblock + " porque no está conectado.");
					sendMessage(errorNotification);
					return;
				}
				
				// Miramos si el usuario a desbloquear estaba realmente estaba bloqueado en el sistema.
				blockedUsers.putIfAbsent(username, new HashSet<>());
				HashSet<String> userBlockList = blockedUsers.get(username);
				if (!userBlockList.contains(userToUnblock)) {
					ChatMessage errorNotification = new ChatMessage("SERVER", MessageType.MESSAGE, 
							"No se puede desbloquear a " + userToUnblock + " porque no está bloqueado.");
					sendMessage(errorNotification);
                    return;
                }
				
				// Si el desbloqueo es válido, lo efectuamos y avisamos al resto de usuarios conectados.
				userBlockList.remove(userToUnblock);
                System.out.println("[" + getCurrentTime() + "][SERVER]: " + username + " ha desbloqueado a " + userToUnblock + ".");
                sendBroadcastMessage(new ChatMessage("SERVER", MessageType.MESSAGE, username + " ha desbloqueado a " + userToUnblock + "."));
			} finally {
				// Liberamos el bloqueo de escritura utilizado.
				writeLock.unlock();
			}
		}
		
		/**
		 * Comprueba si un usuario está conectado en el sistema.
		 */
		private boolean isUserConnected(String user) {
			// Obtenemos el bloqueo de lectura para comprobar el estado de un usuario dentro del sistema.
			readLock.lock();
			try {
				// Miramos si el usuario esta conectado en el sistema, mirando si su nombre esta en el sistema.
				for (UserSession userSession : connectedUsers) {
					if (user.equalsIgnoreCase(userSession.getUsername())) {
						return true;
					}
				}
				return false;
			} finally {
				// Liberamos el bloqueo de lectura utilizado.
				readLock.unlock();
			}
		}
		
		/**
		 * Cierra la sesión y la comunicación con el usuario de forma segura.
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
		 * Obtiene el nombre del usuario dentro del sistema.
		 * 
		 * @return Nombre del usuario dentro del sistema.
		 */
		public String getUsername() {
			return this.username;
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