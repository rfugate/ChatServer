package server.gui;

import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.tree.DefaultTreeModel;

public class ServerPreferences {
	
	public static Preferences prefs;
	
	private static String PORT_NUMBER	= "Port Number";
	private static String PASSWORD		= "Password";
	private static String SERVER_SIZE	= "Server Size";
	private static String TREE_SETUP	= "Tree Setup"; // Requires Object decoding/encoding to work
	private static String MOTD_SETUP	= "MOTD Setup"; // ^
	private static String ADMIN_USERS	= "Admin Users"; // ^
	private static String LOG_MESSAGES	= "Log Messages"; //Done
	private static String MINI_SYS_TRAY	= "Minimize to System Tray"; //Done
	private static String MOTD_CKBOX	= "MOTD Check Box"; //Done
	
	//private static byte[] byteTreeModel;
	//private static DefaultTreeModel treeModel;
	
	public ServerPreferences(){
		prefs = Preferences.userNodeForPackage(this.getClass());
		
		//DefaultMutableTreeNode main = new DefaultMutableTreeNode("Server:");
		//treeModel = new DefaultTreeModel(main);
		
		prefs.get(PORT_NUMBER, "1918");
		prefs.get(PASSWORD, "");
		prefs.getInt(SERVER_SIZE, 1);
		//prefs.getByteArray(TREE_SETUP, convertObject(treeModel));
		prefs.getBoolean(LOG_MESSAGES, false);
		prefs.getBoolean(MINI_SYS_TRAY, false);
		prefs.getBoolean(MOTD_CKBOX, false);
		System.out.println("Server Preferences Initialized");
	}

	public static Preferences getPrefs() {
		return prefs;
	}

	public static void setPrefs(Preferences prefs) {
		ServerPreferences.prefs = prefs;
	}

	public static String getPORT_NUMBER() {
		return PORT_NUMBER;
	}

	public static void setPORT_NUMBER(String portNum) {
		ServerPreferences.prefs.put(PORT_NUMBER, portNum);
	}

	public static String getPASSWORD() {
		return PASSWORD;
	}

	public static void setPASSWORD(String password) {
		ServerPreferences.prefs.put(PASSWORD, password);
	}

	public static String getSERVER_SIZE() {
		return SERVER_SIZE;
	}

	public static void setSERVER_SIZE(int size) {
		ServerPreferences.prefs.putInt(SERVER_SIZE, size);;
	}

	public static String getTREE_SETUP(){
		return TREE_SETUP;
		
	}

	public static void setTREE_SETUP(DefaultTreeModel treeModel) throws IOException {
		// Something.
	}

	public static String getMOTD_SETUP() { // <-- Still needs setup
		return MOTD_SETUP;
	}

	public static void setMOTD_SETUP(String mOTD_SETUP) { // <-- Still needs setup
		MOTD_SETUP = mOTD_SETUP;
	}

	public static String getADMIN_USERS() { // <-- Still needs setup
		return ADMIN_USERS;
	}

	public static void setADMIN_USERS(String aDMIN_USERS) { // <-- Still needs setup
		ADMIN_USERS = aDMIN_USERS;
	}

	public static String getLOG_MESSAGES() {
		return LOG_MESSAGES;
	}

	public static void setLOG_MESSAGES(String lOG_MESSAGES) {
		LOG_MESSAGES = lOG_MESSAGES;
	}

	public static String getMINI_SYS_TRAY() {
		return MINI_SYS_TRAY;
	}

	public static void setMINI_SYS_TRAY(String mINI_SYS_TRAY) {
		MINI_SYS_TRAY = mINI_SYS_TRAY;
	}

	public static String getMOTD_CKBOX() {
		return MOTD_CKBOX;
	}

	public static void setMOTD_CKBOX(String mOTD_CKBOX) {
		MOTD_CKBOX = mOTD_CKBOX;
	}
}
