package section4;

import java.net.*;
import processing.core.*;
import java.io.*;


public class Client extends PApplet implements Runnable{
	
	public static final int SERVER_PORT = 23001;
	
	Socket client;
	int id;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	SomeGameObject clientGameObject;
	
	public Client(String serverName, int port, int id) throws IOException {
		client = new Socket(serverName, port);
		this.id = id;
		oos = new ObjectOutputStream(client.getOutputStream());
		ois = new ObjectInputStream(client.getInputStream());
		clientGameObject = new SomeGameObject();
	}
	
	public Object sendAndReceiveData(Object message) {
		try {
			oos.writeObject(message);
			Object x;
			// busy wait for the response to come from server
			do {
				x = ois.readObject();
				if(x != null) {
					break;
				}
			} while(true);
			return x;
		} catch(EOFException e) {
			System.out.println("Looks like server is down.");
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void run() {
		try {
			for (int i = 1; i<=10; i++) {
				Object recvd;
				recvd = sendAndReceiveData(clientGameObject);
				if(recvd == null) {
					System.out.println("Something went wrong. Received null fromserver.");
				} else if(recvd instanceof SomeGameObject) {
					clientGameObject = (SomeGameObject)recvd;
				}
				Thread.sleep(5000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				client.close();
				exit();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void settings() {
		size(700, 700);
	}
	
	public void setup() {
		background(0);
	}
	
	public void draw() {
		if(clientGameObject!=null) {
			background(0); // clear the earlier state and draw the new state
			text("Client " + id, 10, 10); // display the client id
			rect(clientGameObject.x, clientGameObject.y, 50, 50);
			text(clientGameObject.playerName, clientGameObject.x, clientGameObject.y - 5);
		}
	}
	
	public static void main(String arg[]) {
		try {
			int clientNum = 0;
			if(arg != null && arg.length > 0) {
				clientNum = Integer.parseInt(arg[0]);
			}
			Client c = new Client("localhost", SERVER_PORT, clientNum);
			new Thread(c).start();
			PApplet.runSketch(new String[]{"client"}, c);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
