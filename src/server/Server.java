package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import server.gui.ServerGUI;
import server.gui.ServerTree;

public class Server implements Runnable
{
	public static ServerSocket serverSocket;
	public Socket client;

	private static int totalUsers;
	private static int connectedUsers;
	
	private static Map<Socket, String> userConnected = new HashMap<Socket, String>();
	private static Map<String, String> userStatus = new HashMap<String, String>();
	private static Map<String, String> userChannel = new HashMap<String, String>();
	
	private static Set<String> userNodes = new HashSet<String>();
	
	private final static Object lock = new Object();
	
	public Server(String userIPAddress, int userPortNum, String userPassword, int numberOfUsers) throws IOException{
		totalUsers = numberOfUsers;
		ServerGUI.usersConnected(userConnected, totalUsers);
		serverSocket = new ServerSocket(userPortNum);
	}
	
	public void run() {
		while(true){
			try {
				client = serverSocket.accept();
			} catch (IOException e) {
				System.err.println("Server Socket Closed");
				break;
			}
			if(userConnected.containsKey(this.client)){
				//IPAddress Already Connected
				System.out.println("IP Address already connected..");
			} else if(isFull()) {
				//Server Full. Respond with JDialog box?
			} else {
				//Add Client to HashMap<Socket, String> and Start Receiver Thread
				userConnected.put(client, null);
				new Thread(new Receiver(client)).start();
			}
		}
	}

	public static boolean isFull(){
		if(totalUsers == connectedUsers){
			return true;
		}
		return false;
	}
	
	public static void treeChanged(DefaultTreeModel treeModel){
		new Thread(new Sender(treeModel, null)).start();
	}
	
	public static void halt() throws IOException{
		ServerTree.clearAllUsers(userConnected);
		serverSocket.close();
	}
	
	public static Object convertByte(byte raw[]) throws ClassNotFoundException, IOException{
		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object object = ois.readObject();
		return object;
	}
	
	public static byte[] convertObject(Object object) throws IOException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
		if(object.getClass().equals(DefaultTreeModel.class)){
			objectOut.writeObject(object);
			return byteOut.toByteArray();
		}
		return null;
	}
	
	public static Set<String> getUserNodes() {
		return userNodes;
	}

	public static void setUserNodes(Set<String> userNodes) {
		Server.userNodes = userNodes;
	}
	
	public static HashMap<String, String> getUsersStatus(){
		return (HashMap<String, String>) userStatus;
	}
	
	public static class Sender implements Runnable{
		Object object;
		Socket client;
		
		ObjectOutputStream out;
		
		public Sender(Object object, Socket client){
			this.object = object;
			this.client = client;
		}
		
		public void run(){
			Socket tempClient = null;
			try{
				if(client == null){ //Send to Everyone
					for(Entry<Socket, String> entry: userConnected.entrySet()){
						tempClient = entry.getKey();
						synchronized(lock){
							out = new ObjectOutputStream(tempClient.getOutputStream());
							out.writeObject(object);
						}
					}
				} else { //Send to specific channel
					String channel = ServerTree.getUsersInChannels().get(userConnected.get(client));
					for(Entry<Socket, String> entry: userConnected.entrySet()){
						if(ServerTree.getUsersInChannels().get(entry.getValue()) == channel){
							synchronized(lock){
								if(object.getClass().equals(Byte[].class)){
									if(entry.getValue() != userConnected.get(client)){
										out = new ObjectOutputStream(entry.getKey().getOutputStream());
										out.writeObject(object);
									}
								} else {
									out = new ObjectOutputStream(entry.getKey().getOutputStream());
									out.writeObject(object);
								}
								
							}
						}
					}
				}
			} catch (IOException e) {
				System.out.println("Client: " + tempClient + " Disconnected.");
			}
		}
	}
	
	public class Receiver implements Runnable{
		Socket client = null;
		
		ObjectInputStream in;
		
		public Receiver(Socket s){
			this.client = s;
		}
		
		public String constructMessage(String parts[]){
			StringBuilder sb = new StringBuilder();
			for(int i = 1; i < parts.length; i++){
				sb.append(parts[i] + " ");
			}
			String message = sb.toString().trim();
			return message;
		}
		
		public void run(){
			while(true){
				try {
					in = new ObjectInputStream(client.getInputStream());
					Object o = in.readObject();
					if(o.getClass().equals(String.class)){
						String message = (String) o;
						String parts[] = message.split("\\s+");
						if(parts[0].equals("Public:")){
							message = constructMessage(parts);
							new Thread(new Sender(message, client)).start();
						} else if(parts[0].equals("Private:")){
						} else if(parts[0].equals("User:")){
							String userName = constructMessage(parts);
							for(Entry<Socket, String> entry : userConnected.entrySet()){
								if(entry.getKey().getInetAddress() == client.getInetAddress()){
									connectedUsers++;
									entry.setValue(userName);
									userStatus.put(userName, "Available");
									ServerGUI.usersConnected(userConnected, totalUsers);
									ServerTree.createUser(userName, (DefaultMutableTreeNode) ServerTree.getTreeModel().getRoot());
									//userChannel.put(userName, ServerTree.getTreeModel().getRoot().toString());
									ServerTree.usersInChannels();
									new Thread(new Sender(userStatus, null)).start();
								}
							}
						} else if(parts[0].equals("Status:")){
							userStatus.put(userConnected.get(client), parts[1]);
							new Thread(new Sender(userStatus, null)).start();
						} else if(parts[0].equals("Moved:")){
						}
					} else if(o.getClass().equals(DefaultTreeModel.class)){
						ServerTree.setTreeModel(o);
						ServerTree.usersInChannels();
					} else if(o.getClass().equals(Byte[].class)){
						Byte[] message = (Byte[]) o;
						new Thread(new Sender(message, client)).start();
					}
				} catch (IOException | ClassNotFoundException e) {
					//Client Closed Socket
					try {
						in.close();
						ServerTree.clearOneUser(userConnected.get(client));
						userStatus.remove(userConnected.get(client));
						userChannel.remove(userConnected.get(client));
						userConnected.remove(client);
						connectedUsers--;
						ServerGUI.usersConnected(userConnected, totalUsers); ///  <----- UPDATES CONNECTED LIST
						this.client.close();
						break;
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
}