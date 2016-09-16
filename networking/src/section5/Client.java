package section5;

import java.net.*;
import section4.SomeGameObject;
import processing.core.*;
import java.io.*;


public class Client extends PApplet {
	
	public static final int SERVER_PORT = 23001;
	
	Socket client;
	int id;
	public static SomeGameObject square;
	public static SomeGameObject rectangle;
	public static SomeGameObject circle;
	public static Boolean closeClient = false;
	
	public Client(String serverName, int port, int id) throws IOException {
		client = new Socket(serverName, port);
		this.id = id;
		square = new SomeGameObject(200, 200, "square");
		rectangle = new SomeGameObject(200, 400, "rectangle");
		circle = new SomeGameObject(200, 600, "circle");
	}
	
	public void settings() {
		size(700, 700);
	}
	
	public void setup() {
		background(0);
	}
	
	public void draw() {
		if(closeClient) {
			exit();
		}
		background(0); // clear the earlier state and draw the new state
		text("Client " + id, 10, 10); // display the client id
		if(square!=null) {
			rect(square.x, square.y, 50, 50);
		}
		if(circle!=null) {
			ellipse(circle.x, circle.y, 50, 50);
		}
		if(rectangle!=null) {
			rect(rectangle.x, rectangle.y, 50, 70);
		}
	}
	
	public static void main(String arg[]) {
		try {
			int clientNum = 0;
			if(arg != null && arg.length > 0) {
				clientNum = Integer.parseInt(arg[0]);
			}
			Client c = new Client("localhost", SERVER_PORT, clientNum);
			ClientWriter cw = new ClientWriter(c.client);
			ClientReader cr = new ClientReader(c.client);
			cw.start();
			cr.start();
			PApplet.runSketch(new String[]{"client"}, c);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ClientReader extends Thread {
	Socket socket;
	ObjectInputStream ois;
	
	public ClientReader(Socket cs) throws IOException {
		this.socket = cs;
		this.ois = new ObjectInputStream(socket.getInputStream());
	}
	
	public void run() {
		// busy wait for the response to come from server
		try {
			SomeGameObject x;
			do {
				if(socket.isClosed()) {
					break;
				}
				x = (SomeGameObject)ois.readObject();
				if(x != null) {
					System.out.println("Reading from server...");
					if(x.playerName.equals("square")) {
						synchronized(Client.square) {
							Client.square = x;
						}
					}
					if(x.playerName.equals("circle")) {
						synchronized(Client.circle) {
							Client.circle = x;
						}
					}
					if(x.playerName.equals("rectangle")) {
						synchronized(Client.rectangle) {
							Client.rectangle = x;
						}
					}
				}
				
			} while(true);
		} catch (EOFException e) {
			System.out.println("Error reading data from server");
		} catch(SocketException se) {
			//ignore because writer would have closed the socket
		} catch (Exception ee) {
			ee.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
	}
}

class ClientWriter extends Thread {
	
	Socket socket;
	ObjectOutputStream oos;
	//ArrayList<SomeGameObject> sharedQueue;
	
	public ClientWriter(Socket cs) throws IOException {
		this.socket = cs;
		this.oos = new ObjectOutputStream(socket.getOutputStream());
	}
	
	public void run() {
		// within the writer thread, send multiple game objects without waiting for server to respond
		try {
			for(int i=0; i<=3; i++) {
				System.out.println("Writing to server");
				oos.writeObject(Client.square);
				oos.writeObject(Client.circle);
				oos.writeObject(Client.rectangle);
				Thread.sleep(6000);
			}
		} catch (EOFException e) {
			System.out.println("Looks like server is down.");
		} catch(Exception ee) {
			ee.printStackTrace();
		} finally {
			try {
				System.out.println("Closing client socket");
				socket.getInputStream().close();
				socket.getOutputStream().close();
				socket.close();
				synchronized(Client.closeClient) {
					Client.closeClient = true;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
	}
}