package section4;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class Server extends Thread {
	
	public static final int SERVER_PORT = 23001;
	public static int clientId = 1;
	private ServerSocket ss;
	private ConcurrentHashMap<Socket, Integer> mapOfClients;
	
	public Server(int port) throws IOException {
		ss = new ServerSocket(port);
		mapOfClients = new ConcurrentHashMap<Socket, Integer>();
	}
	
	public void run() {
		System.out.println("Server initialized and listening to incoming client connections at port " + ss.getLocalPort());
		while(true) {
			try {
				Socket socket = ss.accept();
				System.out.println("Server received a new connection from client " + socket.getRemoteSocketAddress().toString());
				mapOfClients.put(socket, clientId++);
				System.out.println("Total number of clients: " + mapOfClients.size());
				(new Slave(socket)).start();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String arg[]) throws Exception{
		Server s = new Server(SERVER_PORT);
		s.start();
	}
	
	class Slave extends Thread {
		Socket socket;
		
		public Slave(Socket cs) {
			this.socket = cs;
		}
		
		public void run() {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				while(true) {
					String inMessage = "";
					SomeGameObject sgo;
					Object x;
					// busy wait for the response to come from client
					do {
						x = ois.readObject();
						if(x != null) {
							break;
						}
					} while(true);
					
					sgo = (SomeGameObject)x;
					inMessage = sgo.playerName + " "+ sgo.x + " " + sgo.y;
					System.out.println("Message from client: " + inMessage);
					if(sgo != null) {
						sgo.x += 20; sgo.y += 20; sgo.playerName = "Modified " + sgo.playerName;
						//sgoOutput = new SomeGameObject(sgo.x+20, sgo.y+20, "Modified " + sgo.playerName);
						oos.writeObject(sgo);
					}
				}
			} catch (EOFException e) {
				//e.printStackTrace();
			} catch (Exception ee) {
				ee.printStackTrace();
			} finally {
				// There is no way to find out if the remote socket has closed unless we read or write from/to 
				// input/output stream in which it returns -1 or throws an IO Exception. Hence if there is an 
				// exception, it is better to close the socket.
				try {
					System.out.println("Client disconnected. Closing all resources cleanly.");
					socket.close();
					mapOfClients.remove(socket);
					System.out.println("Total number of clients: " + mapOfClients.size());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
	}

}

