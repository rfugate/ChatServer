package server.gui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import server.Server;

public class ServerTree {
	
	private static Set<String> channelNodes = new HashSet<String>();
	private static Set<String> userNodes 	= new HashSet<String>();
	private static HashMap<String, String> usersInChannels = new HashMap<String, String>();
	
	private static JTree tree;
	private static DefaultTreeModel treeModel;
	private static DefaultMutableTreeNode mainNode;
	private static DefaultMutableTreeNode channelNode;
	private static DefaultMutableTreeNode userNode;
	
	private static String userName;
	
	/* User Icons */
	static ImageIcon userAvailIcon;
	static ImageIcon userBusyIcon;

	static ImageIcon userAwayIcon;
	static ImageIcon userBatmanIcon;
	
	static ImageIcon channelOpenIcon;
	static ImageIcon channelClosedIcon;
	
	static ImageIcon rootNodeIcon;
	
	/* Channel Icons */
	// Main Channel Icon
	// Sub Channel Icon
	// Password Sub Channel Icon
	
	public ServerTree() throws ClassNotFoundException, IOException{
		mainNode = new DefaultMutableTreeNode("Server:");
		treeModel = new DefaultTreeModel(mainNode);
		treeModel = (DefaultTreeModel) convertByte(ServerPreferences.prefs.getByteArray(ServerPreferences.getTREE_SETUP(), convertObject(treeModel)));
		tree = new JTree(treeModel);
		
		userAvailIcon	= createImageIcon("/UserIconAvail.png");
		userBusyIcon	= createImageIcon("/UserIconBusy.png");
		userAwayIcon	= createImageIcon("/UserIconAway.png");
		userBatmanIcon	= createImageIcon("/UserIconBatman.png");
		
		channelClosedIcon = createImageIcon("/ChannelClosed.png");
		channelOpenIcon = createImageIcon("/ChannelOpen.png");
		
		rootNodeIcon = createImageIcon("/RootNode.png");
	}
	
	public static void createChannel(DefaultMutableTreeNode child, DefaultMutableTreeNode parent){
		DefaultTreeModel model = ServerTree.getTreeModel();
		model.insertNodeInto(child, parent, parent.getChildCount());
		ServerTree.setTreeModel(model);
	}
	
	public static void deleteChannel(TreePath[] path){
		DefaultMutableTreeNode node;
		DefaultTreeModel model = (DefaultTreeModel) ServerTree.getTreeModel();
		for(int i = 0; i < path.length; i++){
			node = (DefaultMutableTreeNode) path[i].getLastPathComponent();
			model.removeNodeFromParent(node);
		}
		
		ServerTree.setTreeModel(model);
	}
	
	public static void editChannel(DefaultMutableTreeNode node, String edit){
		DefaultTreeModel model = ServerTree.getTreeModel();
		node.setUserObject(edit);
		model.nodeChanged(node);
		ServerTree.setTreeModel(model);
	}
	
	public static void createUser(String name, DefaultMutableTreeNode node){
		userNodes.add(name);
		userNode = new DefaultMutableTreeNode(name);
		
		DefaultTreeModel tempModel = ServerTree.getTreeModel();
		
		mainNode = node;
		tempModel.insertNodeInto(userNode, mainNode, 0);
		ServerTree.setTreeModel(tempModel);
	}
	
	public static DefaultTreeModel deleteUserNode(DefaultMutableTreeNode node, DefaultTreeModel model, String s){
		int childCount = node.getChildCount();

		for(int i = 0; i < childCount; i++){
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
			if(childNode.getChildCount() > 0){
				deleteUserNode(childNode, model, s);
			} else {
				if(childNode.toString().equals(s)){
					model.removeNodeFromParent(childNode);
					childCount--; i--;
				}
			}
		}
		return model;
	}

	public static void clearOneUser(String name){
		userNodes.remove(name);
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) ServerTree.getTreeModel().getRoot();
		DefaultTreeModel model = (DefaultTreeModel) ServerTree.getTreeModel();
		
		model = deleteUserNode(rootNode, model, name);
		
		ServerTree.setTreeModel(model);
	}
	
	public static void clearAllUsers(Map<Socket, String> usersConnected){
		
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) ServerTree.getTreeModel().getRoot();
		DefaultTreeModel model = (DefaultTreeModel) ServerTree.getTreeModel();
		
		for(Entry<Socket, String> entry : usersConnected.entrySet()){
			String name = entry.getValue();
			userNodes.remove(name);
			model = deleteUserNode(rootNode, model, name);
		}
		ServerTree.setTreeModel(model);
		
	}
	
	public static void usersInChannels(){
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) getTreeModel().getRoot();
		for(String entry: userNodes){
			searchUserInChannel(entry, root);
		}
	}
	
	public static void searchUserInChannel(String user, DefaultMutableTreeNode root){
		int childCount = root.getChildCount();
		
		for(int i = 0; i < childCount; i++){
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) root.getChildAt(i);
			if(childNode.getChildCount() > 0){
				searchUserInChannel(user, childNode);
			} else {
				if(childNode.toString().equals(user)){
					usersInChannels.put(user, childNode.getParent().toString());
				}
			}
		}
	}
	
	public static HashMap<String, String> getUsersInChannels() {
		return usersInChannels;
	}

	public static void setUsersInChannels(HashMap<String, String> usersInChannels) {
		ServerTree.usersInChannels = usersInChannels;
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
	
	public static Object convertByte(byte raw[]) throws ClassNotFoundException, IOException{
		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object object = ois.readObject();
		return object;
	}

	private ImageIcon createImageIcon(String path) throws IOException {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null){
			ImageIcon imageIcon = new ImageIcon(imgURL);
			Image image = imageIcon.getImage();
			Image newimg = image.getScaledInstance(15, 15, java.awt.Image.SCALE_SMOOTH);
			imageIcon = new ImageIcon(newimg);
			return imageIcon;
		} else {
			System.err.println("Couldn't find the file: " + path);
			return null;
		}
	}

	public static DefaultTreeModel getTreeModel() {
		return treeModel;
	}

	public static void setTreeModel(Object object){
		ServerTree.treeModel = (DefaultTreeModel) object;
		try {
			ServerPreferences.prefs.putByteArray(ServerPreferences.getTREE_SETUP(), convertObject(ServerTree.treeModel));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Server.treeChanged(ServerTree.treeModel);
	}

	public static ImageIcon getUserAvailIcon() {
		return userAvailIcon;
	}

	public static void setUserAvailIcon(ImageIcon userAvailIcon) {
		ServerTree.userAvailIcon = userAvailIcon;
	}

	public static ImageIcon getChannelOpenIcon() {
		return channelOpenIcon;
	}

	public static void setChannelOpenIcon(ImageIcon channelOpenIcon) {
		ServerTree.channelOpenIcon = channelOpenIcon;
	}

	public static ImageIcon getChannelClosedIcon() {
		return channelClosedIcon;
	}

	public static void setChannelClosedIcon(ImageIcon channelClosedIcon) {
		ServerTree.channelClosedIcon = channelClosedIcon;
	}

	public static ImageIcon getRootNodeIcon() {
		return rootNodeIcon;
	}

	public static void setRootNodeIcon(ImageIcon rootNodeIcon) {
		ServerTree.rootNodeIcon = rootNodeIcon;
	}
	
	public static ImageIcon getUserBatmanIcon(){
		return userBatmanIcon;
	}
	
	public static ImageIcon getUserBusyIcon() {
		return userBusyIcon;
	}

	public static ImageIcon getUserAwayIcon() {
		return userAwayIcon;
	}
}