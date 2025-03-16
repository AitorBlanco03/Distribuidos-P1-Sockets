package es.ubu.lsi.server;

// Importamos las librerías/paquetes necesarias para su ejecución.
import java.io.*;
import java.net.*;
import java.util.*;
import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.MessageType;

/**
 * Servidor de chat que gestiona las conexiones de los clientes y reenvía
 * los mensajes a todos los usuarios conectados.
 * 
 * @author <a href="abf1005@alu.ubu.es">Aitor Blanco Fernández</a>
 * @version 1.2.0
 */
public class ChatServer implements IChatServer {
	
	/** Puerto en el que el servidor escuchará las conexiones entrantes. */
	private int serverPort;
	
	/** Lista para manejar y gestionar los usuarios conectados al servidor. */
	private final List<ClientHandler> connectedUsers;
	
	/** Flag para controlar y gestionar el estado del servidor. */
	private boolean isRunning;
	
	/**
     * Constructor de la clase.
     * 
     * @param port Puerto en el que el servidor escuchará las conexiones entrantes.
     */
	public ChatServer(int serverPort) {
		this.serverPort = serverPort;
		this.connectedUsers = new ArrayList<>();
		isRunning = false;
	}
	
	/**
	 * Inicializa el servidor y comienza a aceptar conexiones de usuarios.
	 */
	@Override
	public void startServer() {
		isRunning = true;
		// Intentamos iniciar el servidor en el puerto correspondiente.
		try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
			System.out.println("[SERVER]: Servidor iniciado en el puerto " + serverPort);
			
			// Mientras que el servidor esta activo, escuchará las conexiones entrantes.
			while (isRunning) {
				// Aceptamos y gestionamos las nuevas conexiones entrantes.
				Socket clientSocket = serverSocket.accept();
				ClientHandler newUser = new ClientHandler(clientSocket);
				addUser(newUser);
				new Thread(newUser).start();
			}
		} catch (IOException e) {
			System.err.println("[ERROR]: Error al iniciar el servidor: " + e.getMessage());
		}
	}
	
	/**
     * Finaliza el servidor y cierra todas las conexiones activas.
     */
	@Override
	public void shutdownServer() {
		isRunning = false;
		// Cerramos todas las conexiones activas con los usuarios.
		synchronized (connectedUsers) {
			for (ClientHandler user : connectedUsers) {
				user.closeConnection();
			}
			connectedUsers.clear();
		}
		System.out.println("[SERVER]: Servidor apagado.");
	}
	
	/**
     * Reenvía un mensaje a todos los clientes conectados.
     * 
     * @param message El mensaje que será reenviado a todos los clientes conectados.
     */
	@Override
	public void sendBroadcastMessage(ChatMessage message) {
		// Reenviamos a todos los clientes conectados el mensaje rercibido.
		synchronized (connectedUsers) {
			for (ClientHandler user : connectedUsers) {
                user.sendMessage(message);
            }
		}
	}
	
	/**
	 * Conecta un nuevo usuario al servidor, añadiendole a la lista de
	 * usuarios conectados al servidor.
	 */
	public void addUser(ClientHandler user) {
		// Añadimos al usuario a la lista de usuarios conectados al servidor.
		synchronized (connectedUsers) {
			connectedUsers.add(user);
		}
	}
	
	/**
	 * Desconecta a un usuario del servidor, quitandole de la lista de
	 * usuarios conectados al servidor.
	 */
	public void removeUser(ClientHandler user) {
		// Eliminamos al usuario de la lista de usuarios conectados del servidor.
		synchronized (connectedUsers) {
			connectedUsers.remove(user);
		}
	}
	
	/**
	 * Hilo para maneja la comunicación del cliente de manera independiente.
	 */
	private class ClientHandler implements Runnable {
		
		/** Socket para manejar la comunicación con el cliente. */
		private final Socket clientSocket;
		
		/** Flujo de entrada para recibir los mensajes entrantes. */
		private ObjectInputStream inputMessages;
		
		/** Flujo de salida para mandar los mensajes. */
		private ObjectOutputStream outputMessages;
		
		/** Nombre del usuario dentro del sistema. */
		private String username;
		
		/**
		 * Constructor de clase que inicializa el hilo para tratar y manejar
		 * la comunicación del cliente de manera independiente.
		 * 
		 * @param clientSocket - Socket para manejar la comunicación con el cliente.
		 */
		public ClientHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}
		
		/**
		 * Maneja y gestiona la comunicación del cliente de manera independiente.
		 */
		@Override
		public void run() {
			try {
				// Inicializamos los flujos para recibir y enviar mensajes.
				outputMessages = new ObjectOutputStream(clientSocket.getOutputStream());
				inputMessages = new ObjectInputStream(clientSocket.getInputStream());
				
				// Recibimos el nombre del usuario como primer mensaje y le dame la bienvenida al chat.
				ChatMessage initialMessage = (ChatMessage) inputMessages.readObject();
				this.username = initialMessage.getUserSender();
				System.out.println("[SERVER]: " + username + " se ha conectado.");
				sendBroadcastMessage(new ChatMessage("SERVER", MessageType.MESSAGE, username + " se ha unido al chat."));
				
				// Mantenemos la comunicación con el cliente hasta el cliente decida salir del chat.
				while (true) {
					ChatMessage message = (ChatMessage) inputMessages.readObject();
					if (message.getMessageType() == MessageType.LOGOUT) {
						System.out.println("[SERVER]: " + username + " ha salido del chat.");
						sendBroadcastMessage(new ChatMessage("SERVER", MessageType.MESSAGE, username + " ha salido del chat."));
						break;
					}
					sendBroadcastMessage(message);
				}
			} catch (IOException | ClassNotFoundException e) {
				System.err.println("[ERROR]: Error en la comunicación con " + username + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		}
		
		/**
		 * Reenvia el mensaje proporcionado al cliente.
		 * 
		 * @param message - Mensaje a reenviar al resto de usuarios conectados.
		 */
		public void sendMessage(ChatMessage message) {
			try {
				// Intentamos reenviar el mensaje proporcionado al cliente.
				outputMessages.writeObject(message);
				outputMessages.flush();
			} catch(IOException e) {
				System.err.println("[ERROR]: No se pudo enviar mensaje a " + username + ": " + e.getMessage());
			}
		}
		
		/**
		 * Cierra de manera segura la comunicación con el cliente, cerrando
		 * consigo de forma segura todos los recursos utilizados.
		 */
		public void closeConnection() {
			try {
				if (inputMessages != null) inputMessages.close();
				if (outputMessages != null) outputMessages.close();
				if (clientSocket != null) clientSocket.close();
			} catch (IOException e) {
                System.err.println("[ERROR]: Error al cerrar la conexión de " + username + ": " + e.getMessage());
			} finally {
				removeUser(this);
			}
		}
	}
	
	/** Main de la clase. */
	public static void main(String[] args) {
        int port = 1500;
        ChatServer server = new ChatServer(port);
        server.startServer();
    }
}