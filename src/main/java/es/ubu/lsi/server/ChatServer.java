package es.ubu.lsi.server;

// Importamos las librerías/paquetes necesarias para su ejecución.
import es.ubu.lsi.common.ChatMessage;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Servidor de chat que gestiona las conexiones de los clientes y reenvía los mensajes.
 * 
 * @author Aitor Blanco Fernández
 * @version 1.1.0
 */
public class ChatServer implements IChatServer {

    /** Puerto en el que el servidor escuchará las conexiones entrantes. */
    private int port;

    /** Lista de manejadores de clientes. */
    private List<ClientHandler> clientHandlers;

    /** Flag para controlar si el servidor está en ejecución. */
    private boolean isRunning;

    /**
     * Constructor de la clase.
     * 
     * @param port Puerto en el que el servidor escuchará las conexiones entrantes.
     */
    public ChatServer(int port) {
        this.port = port;
        this.clientHandlers = Collections.synchronizedList(new ArrayList<>());
        this.isRunning = false;
    }

    /**
     * Inicia el servidor y comienza a aceptar conexiones de clientes.
     */
    @Override
    public void startServer() {
        isRunning = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[SERVER]: Servidor iniciado en el puerto " + port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER]: Nueva conexión aceptada.");
                ClientHandler handler = new ClientHandler(clientSocket);
                clientHandlers.add(handler);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("[ERROR]: Error en el servidor: " + e.getMessage());
        }
    }

    /**
     * Finaliza el servidor y cierra todas las conexiones activas.
     */
    @Override
    public void shutdownServer() {
        isRunning = false;
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                handler.closeConnection();
            }
            clientHandlers.clear();
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
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                handler.sendMessage(message);
            }
        }
    }

    /**
     * Maneja la comunicación con un cliente específico.
     */
    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                ChatMessage message;
                while ((message = (ChatMessage) in.readObject()) != null) {
                    sendBroadcastMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("[ERROR]: Error en la comunicación con el cliente: " + e.getMessage());
            } finally {
                closeConnection();
            }
        }

        public void sendMessage(ChatMessage message) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                System.err.println("[ERROR]: Error al enviar mensaje: " + e.getMessage());
            }
        }

        public void closeConnection() {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("[ERROR]: Error al cerrar los recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Método principal para iniciar el servidor.
     */
    public static void main(String[] args) {
        final int DEFAULT_PORT = 1500;
        ChatServer server = new ChatServer(DEFAULT_PORT);
        server.startServer();
    }
}
