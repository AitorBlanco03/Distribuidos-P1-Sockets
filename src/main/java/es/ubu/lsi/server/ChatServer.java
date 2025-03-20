package es.ubu.lsi.server;

// Importamos las librerías/paquetes para el funcionamiento del servidor.
import java.io.*;
import java.net.*;
import java.util.*;
import es.ubu.lsi.common.*;
import java.text.SimpleDateFormat;

/**
 * Servidor del chat que gestiona las conexiones de los usuarios y el reenvío
 * de mensajes, permitiendo además a los usuarios bloquear a otros para dejar
 * de recibir sus mensajes.
 *
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.3.0
 */
public class ChatServer implements IChatServer {
	
	/** Puerto del servidor encargado de escuchar y aceptar las nuevas conexiones entrantes. */
	private int serverPort;
	
	/** Lista interna para almacenar y gestionar los diferentes usuarios conectados al servidor. */
	private final List<UserSession> connectedUsers;
	
	/** Tabla interna para almacenar y gestionar los usuarios bloqueados por cada usuario conectado. */
	private final Map<String, HashSet<String>> blockedUsers;
	
	/** Flag para controlar y gestionar el estado del servidor. */
	private boolean isRunning;
	
	/**
	 * Constructor de la clase ChatServer.
	 * 
	 * @param serverPort Puerto del servidor encargado de escuchar y aceptar las nuevas conexiones entrantes.
	 */
	public ChatServer(int serverPort) {
		this.serverPort = serverPort;
		this.connectedUsers = new ArrayList<>();
		this.blockedUsers = new HashMap<>();
		this.isRunning = false;
	}
	
	/**
	 * Inicia el servidor y, escuchar y aceptar conexiones de nuevos usuarios.
	 * <p>
	 * El servidor comienza a escuchar conexiones entrantes en el puerto correspondiente.
	 * Por cada nueva conexión aceptada, se crea una sesión de usuario y se lanza un hilo para gestionarla.
	 * Este proceso se repite continuamente mientras que el servidor se encuentre en ejecución.
	 * </p>
	 */
	@Override
	public void startServer() {
		// Intentamos iniciar el servidor en el puerto correspondiente.
		try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
			isRunning = true;
			System.out.println("[" + getCurrentTime() + "][SERVER]: Servidor iniciado en el puerto " + serverPort);
			
			// Mientras que el servidor este en ejecución, escuchamos y aceptamos nuevas conexiones entrantes.
			while(isRunning) {
				// Aceptamos las nuevas conexiones, creamos una sesión y la gestionamos en un hilo independiente.
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
	 * Detiene el servidor, dejando de aceptar nuevas conexiones entrante y cerrando todas las
	 * comunicaciones abiertas. Además, se encarga de cerrar correctamente todos los recursos
	 * utilizados para las comunicaciones y conexiones con los usuarios.
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
		
		// Eliminamos todos los bloqueos de los usuarios al detener el servidor.
		synchronized (blockedUsers) {
			blockedUsers.clear();
		}
		
		System.out.println("[" + getCurrentTime() + "][SERVER]: Servidor detenido y apagado");
	}
	
	/**
	 * Reenvía el mensaje recibido a todos los usuarios conectados, excepto a aquellos que han
	 * bloqueado al remitente del mensaje.
	 * 
	 * @param msg Mensaje recibido que se desea reenviar.
	 */
	@Override
	public void sendBroadcastMessage(ChatMessage msg) {
		// Reenvíamos el mensaje recibido a todos los usuarios conectados, excepto a aquellos
		// que han bloqueado al remitente del mensaje.
		synchronized (connectedUsers) {
			for (UserSession user : connectedUsers) {
				if (!isBlocked(msg.getMessageSender(), user.getUsername())) {
					user.sendMessage(msg);
				}
			}
		}
	}
	
	/**
	 * Añade la conexión con el usuario al sistema y actualiza la lista de usuarios
	 * conectados y la tabla de bloqueos del servidor.
	 * 
	 * @param newUser Nuevo usuario que se conectó al servidor.
	 */
	public void addUser(UserSession newUser) {
		// Actualizamos la lista de usuarios conectados al servidor, agregandolé en el proceso.
		synchronized (connectedUsers) {
			connectedUsers.add(newUser);
		}
		
		// Actualizamos la tabla de bloqueos, agregandole un nuevo campo para gestionar sus bloqueos.
		synchronized (blockedUsers) {
			blockedUsers.put(newUser.getUsername(), new HashSet<>());
		}
	}
	
	/**
	 * Elimina la conexión con el usuario del sistema y lo elimina de la lista de usuarios
	 * conectados y de la lista de bloqueos del servidor.
	 * 
	 * @param user Usuario que se desconectó del servidor.
	 */
	public void removeUser(UserSession user) {
		// Actualizamos la lista de usuarios conectados del servidor, eliminandolé en el proceso.
		synchronized (connectedUsers) {
			connectedUsers.remove(user);
		}
		// Actualizamos la lista de bloqueos, eliminando consigo los bloqueos del usuario.
		synchronized (blockedUsers) {
			blockedUsers.remove(user.getUsername());
		}
	}
	
	/**
	 * Comprueba si un usuario ha bloqueado otro, para dejar de recibir
	 * sus mensajes dentro del chat.
	 * 
	 * @param userSender Usuario del sistema que envío el mensaje en el chat.
	 * @param userReceiver Usuario del sistema que lo recibiría.
	 * @return true si el receptor ha bloqueado al remitente, false en caso contrario.
	 */
	private boolean isBlocked(String userSender, String userReceiver) {
		// Obtenemos la lista de bloqueos del usuario que recibiría el mensaje.
		HashSet<String> receiverBlockedList = blockedUsers.get(userReceiver);
		// Si el receptor no tiene bloqueos, entonces no le tiene bloqueado.
		if (receiverBlockedList == null) return false;
		// Si el receptor tiene bloqueos, comprobamos que no le tiene bloqueado.
		for (String userBlocked : receiverBlockedList) {
			if (userBlocked.equals(userSender)) return true;
		}
		// Si el remitente no se encuentre entre sus bloqueos, entonces no esta bloqueado.
		return false;
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
	 * Hilo encargado de gestionar la sesión del usuario dentro del sistema, así como
	 * la comunicación entre el usuario y el servidor de manera independiente.
	 * 
	 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
	 * @version 1.2.0
	 */
	private class UserSession implements Runnable {
		
		/** Socket para gestionar la comunicación entre el usuario y el servidor. */
		private final Socket userSocket;
		
		/** Flujo de entrada para recibir los mensajes entrantes del usuario. */
		private ObjectInputStream inputMessage;
		
		/** Flujo de salida para enviar y reenviar los mensajes al usuario. */
		private ObjectOutputStream outputMessage;
		
		/** Nombre del usuario dentro del sistema. */
		private String username;
		
		/**
		 * Constructor de la clase UserSession.
		 * <p>
		 * Inicializa los recursos necesarios para gestionar la sesión del usuario 
		 * dentro del sistema, así como la comunicación entre el usuario y el servidor 
		 * de manera independiente.
		 * </p>
		 * 
		 * @param userSocket Socket para gestionar la sesión y la comunicación con el usuario.
		 */
		public UserSession(Socket userSocket) {
			this.userSocket = userSocket;
		}
		
		/**
		 * Inicializa el hilo, y se encarga de gestionar la sesión del usuario 
		 * dentro del sistema, así como la comunicación entre el usuario y el servidor 
		 * de manera independiente.
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
		 * Procesa el mensaje recibido del usuario antes de ser reenviado
		 * al resto de usuarios conectados.
		 * 
		 * @param newMessage Nuevo mensaje recibido que se desea procesar.
		 */
		private void handleNewMessage(ChatMessage newMessage) {
			
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
		 * Gestiona la solicitud de bloqueo de un usuario.
		 * <p>
		 * Permite que un usuario bloquee a otro dentro del chat, evitando así que reciba sus mensajes
		 * dentro del chat.
		 * </p>
		 * 
		 * @param userToBlock Nombre del usuario que se desea bloquear.
		 */
		private void handleBlock(String userToBlock) {
			// Registramos el bloqueo del usuario dentro del servidor y sistema.
			synchronized (blockedUsers) {
				HashSet<String> userBlockList = blockedUsers.get(username);
				if (userBlockList != null) {
		            userBlockList.add(userToBlock);
		        }
			}
			// Informamos a todos los usuarios conectados sobre el bloqueo realizado.
			System.out.println("[" + getCurrentTime() + "][SERVER]: " + username + " ha bloqueado a " + userToBlock + ".");
			ChatMessage blockNotification = new ChatMessage("SERVER", MessageType.MESSAGE, username + " ha bloqueado a " + userToBlock + ".");
			sendBroadcastMessage(blockNotification);
		}
		
		/**
		 * Gestiona la solicitud de desbloqueo de un usuario.
		 * <p>
		 * Permite que un usuario desbloque a otro usuario del chat, permitiendo así que vuelva a recibir
		 * sus mensajes dentro del chat.
		 * </p>
		 * 
		 * @param userToUnblock Nombre del usuario que se desea desbloquear.
		 */
		private void handleUnblock(String userToUnblock) {
			// Registramos el desbloqueo del usuario dentro del servidor y sistema.
			synchronized (blockedUsers) {
		        HashSet<String> userBlockList = blockedUsers.get(username);
		        if (userBlockList != null) {
		        	userBlockList.remove(userToUnblock);
		        }
		    }
			// Informamos a todos los usuarios conetctados sobre el desbloqueo realizado.
			System.out.println("[" + getCurrentTime() + "][SERVER]: " + username + " ha desbloqueado a " + userToUnblock + ".");
			ChatMessage unblockNotification = new ChatMessage("SERVER", MessageType.MESSAGE, username + " ha desbloqueado a " + userToUnblock + ".");
			sendBroadcastMessage(unblockNotification);
		}
		
		/**
		 * Cierra la sesión y comunicación con el usuario de forma segura.
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