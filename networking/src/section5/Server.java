package section5;

import java.net.*;
import section4.SomeGameObject;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Server extends Thread {
	
	public static final int SERVER_PORT = 23001;
	public static int clientId = 1;
	private ServerSocket ss;
	public static ConcurrentHashMap<Socket, Integer> mapOfClients;
	
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
				ArrayList<SomeGameObject> sharedQueue = new ArrayList<SomeGameObject>();
				(new SlaveWriter(socket, sharedQueue)).start();
				(new SlaveReader(socket, sharedQueue)).start();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String arg[]) throws Exception{
		Server s = new Server(SERVER_PORT);
		s.start();
	}
	
}

// The idea is that client sends multiple game objects of various shapes.
// The server then understands the game object shape and doubles its size and sends it back.
// All this happens asyncly i.e. one thread does not wait for the other
class SlaveReader extends Thread {
	Socket socket;
	ObjectInputStream ois;
	ArrayList<SomeGameObject> sharedQueue;
	
	public SlaveReader(Socket cs, ArrayList<SomeGameObject> sharedQueue) throws IOException {
		this.socket = cs;
		this.ois = new ObjectInputStream(socket.getInputStream());
		this.sharedQueue = sharedQueue;
	}
	
	public void run() {
		try {
			while(true) {
				Object x;
				// busy wait for the response to come from client
				do {
					x = ois.readObject();
					if(x != null) {
						break;
					}
				} while(true);
				System.out.println("Reading from client...");
				synchronized(sharedQueue) {
					sharedQueue.add((SomeGameObject)x); // this won't notify the writer because we need async communication
				}
				System.out.println("Shared queue size = " + sharedQueue.size());
				Thread.sleep(1000);
				if(socket.isClosed()) {
					break;
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
				Server.mapOfClients.remove(socket);
				System.out.println("Total number of clients: " + Server.mapOfClients.size());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}


class SlaveWriter extends Thread {
	Socket socket;
	ObjectOutputStream oos;
	ArrayList<SomeGameObject> sharedQueue;
	
	public SlaveWriter(Socket cs, ArrayList<SomeGameObject> sharedQueue) throws IOException {
		this.socket = cs;
		this.oos = new ObjectOutputStream(socket.getOutputStream());
		this.sharedQueue = sharedQueue;
	}
	
	public void run() {
		try {
			while(true) {
				System.out.println("Writing to client...");
				synchronized(sharedQueue) {
					if (sharedQueue.size() != 0) {
						SomeGameObject sgo = sharedQueue.get(0);
						sgo.x += 100; //sgo.y += 20; //sgo.playerName = "Modified " + sgo.playerName;
						oos.writeObject(sgo);
						sharedQueue.remove(0);
					}
				}
				Thread.sleep(1500);
				if (socket.isClosed()) {
					break;
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
				Server.mapOfClients.remove(socket);
				System.out.println("Total number of clients: " + Server.mapOfClients.size());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}