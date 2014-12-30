package server.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import server.Server;
import server.gui.window.ServerSettingsWindow;

public class ServerGUI {
	
	private static JFrame frame = new JFrame();
	
	private static int buttonLength;
	private static int buttonHeight;
	private static JTextArea userConnected;

	private static JTextField portTextField;
	private static JTextField ipTextField;
	private static JTextField passwordTextField;
	
	private static boolean isServerRunning = false;

	private JPanel mainPanel			= new JPanel();
	private JPanel userPanel			= new JPanel(new BorderLayout()); //Needed to prevent word wrap
	private JPanel connectionInfoPanel	= new JPanel();
	private JPanel numberOfUsersPanel	= new JPanel(new GridLayout(1,1));
	
	private JButton startButton;
	private JButton exitButton;
	private JButton settingsButton;
	private JComboBox<Integer> numberOfUsers;
	
	//private SystemTray tray;
	//private TrayIcon trayIcon;
	
	public ServerGUI(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		if(SystemTray.isSupported()){
			System.out.println("System Tray Supported.");
			//tray = SystemTray.getSystemTray();
		}
		setFrame(new JFrame("Server"));
		getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getFrame().addWindowListener(new MyWindowListener());
		
		if("Windows 7".equals(System.getProperty("os.name"))){
			setWindowsVariables();
		}
		if("OS X".equals(System.getProperty("os.name"))){
			setMacVariables();
		}
		
		createPanels();
		createUIObjects();
		
		getFrame().setResizable(false);
		getFrame().pack();
		getFrame().setVisible(true);
		getFrame().setLocationRelativeTo(null);
	}
	
	public static void usersConnected(Map<Socket, String> users, int numberOfUsers){
		Font font = new Font("Verdana", Font.PLAIN, 10);
		getUserConnected().setText("");
		getUserConnected().setFont(font);
		Document doc = getUserConnected().getDocument();
		
		int totalUsers = numberOfUsers;
		int onlineUsers = 0;
		
		for(Entry<Socket, String> entry : users.entrySet()){
			String message;
			String address = entry.getKey().getInetAddress().toString();
			String port = Integer.toString(entry.getKey().getPort());
			
			message = "Client " + (onlineUsers+1) + ": " + entry.getValue() + address + ":" + port;
			try{
				doc.insertString(doc.getLength(),  message + "\n", null);
				Thread.yield();
			} catch (BadLocationException e){
				System.err.println(e);
			}
			
			onlineUsers++; totalUsers--;
		}
		
		for(int i = 0; i < totalUsers; i++){
			String message = "Client " + (onlineUsers + i + 1) + ": --Empty--";
			try{
				doc.insertString(doc.getLength(), message + "\n", null);
				Thread.yield();
			} catch (BadLocationException e){
				System.err.println(e);
			}
		}
	}

	private void createPanels() {
		mainPanel.setPreferredSize(new Dimension(400,170));
		userPanel.setPreferredSize(new Dimension(215,150));
		connectionInfoPanel.setPreferredSize(new Dimension(165,150));
		numberOfUsersPanel.setPreferredSize(new Dimension(75,25));
		getFrame().add(mainPanel);
			mainPanel.add(connectionInfoPanel, BorderLayout.WEST);
			mainPanel.add(userPanel, BorderLayout.EAST);
	}
	
	private void createUIObjects() {
		
		userConnected				= new JTextArea();
		JScrollPane scrollPane		= new JScrollPane(userConnected);
		numberOfUsers				= new JComboBox<Integer>();
		
		userConnected.setEditable(false);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(215,145));
		scrollPane.setOpaque(true);
		userPanel.add(scrollPane);
		
		String address = null;
		try {
			address = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		ipTextField			= createTextField(address, null, 155, 25, connectionInfoPanel);
		portTextField		= createTextField("",ServerPreferences.getPORT_NUMBER(), 155, 25, connectionInfoPanel);
		passwordTextField	= createTextField("", ServerPreferences.getPASSWORD(), 155, 25, connectionInfoPanel);
		numberOfUsers		= createComboBox(ServerPreferences.getSERVER_SIZE(), numberOfUsersPanel, connectionInfoPanel);
		startButton			= createButton("Start", 75, 25, connectionInfoPanel);
		settingsButton		= createButton("Settings", 75, 25, connectionInfoPanel);
		exitButton			= createButton("Exit", 75, 25, connectionInfoPanel);
	}
	
	public JButton createButton(String text, int length, int height, Container container){
		JButton button = new JButton(text);
		button.setPreferredSize(new Dimension(length, height));
		button.addActionListener(new ButtonAction());
		container.add(button);
		return button;
	}
	
	public JTextField createTextField(String text, String preferences, int length, int height, Container container){
		JTextField textField = new JTextField(text);
		if(preferences != null) textField.setText(ServerPreferences.prefs.get(preferences, ""));
		textField.setPreferredSize(new Dimension(length, height));
		container.add(textField);
		return textField;
	}
	
	public JComboBox<Integer> createComboBox(String preference, Container myself, Container parent){
		JComboBox<Integer> comboBox = new JComboBox<Integer>();
		for(int i = 2; i <= 25; i++){
			comboBox.addItem(i);
		}
		comboBox.setSelectedItem(ServerPreferences.prefs.getInt(preference, 5));
		DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
		dlcr.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
		comboBox.setRenderer(dlcr);
		myself.add(comboBox);
		parent.add(myself);
		return comboBox;
	}
	
	public class MyWindowListener implements WindowListener{

		public void windowActivated(WindowEvent e) {}
		public void windowClosed(WindowEvent e) {}
		public void windowClosing(WindowEvent e) {
			try {
				if(isServerRunning){
					System.out.println("Closing Server Socket.");
					Server.halt();
					System.out.println("Closed Server Socket.");
				}
			} catch (IOException e1) {
				System.out.println("Server never started.");
			}
			System.exit(0);
		}
		public void windowDeactivated(WindowEvent e) {}
		public void windowDeiconified(WindowEvent e) {}
		public void windowIconified(WindowEvent e) {}
		public void windowOpened(WindowEvent e) {}
		
	}
	
	private class ButtonAction extends AbstractAction{

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src == startButton){
				try {
					//new Server(ipTextField.getText(), Integer.parseInt(portTextField.getText()), passwordTextField.getText(),(int)numberOfUsers.getSelectedItem());
					
					//Check User input
					new Thread(new Server(ipTextField.getText(), Integer.parseInt(portTextField.getText()), passwordTextField.getText(),(int)numberOfUsers.getSelectedItem())).start();
					ServerPreferences.prefs.put(ServerPreferences.getPORT_NUMBER(), portTextField.getText());
					ServerPreferences.prefs.put(ServerPreferences.getPASSWORD(), passwordTextField.getText());
					ServerPreferences.prefs.putInt(ServerPreferences.getSERVER_SIZE(), (int)numberOfUsers.getSelectedItem());
					isServerRunning = true;
				} catch (NumberFormatException | IOException e1) {
					e1.printStackTrace();
				}
			}
			if(src == exitButton){
				try {
					Server.halt();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
			if(src == settingsButton){
				try {
					new ServerSettingsWindow();
				} catch (ClassNotFoundException | InstantiationException| IllegalAccessException | IOException | UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
			}
		}
		
	}

	private void setMacVariables() {
	}

	private void setWindowsVariables() {
		setButtonLength(90);
		setButtonHeight(25);
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		System.out.println("Server: ");
		new ServerPreferences();
		new ServerTree();
		new ServerGUI();
	}
	
	public static JTextArea getUserConnected() {
		return userConnected;
	}

	public static void setUserConnected(JTextArea userConnected) {
		ServerGUI.userConnected = userConnected;
	}

	public static JFrame getFrame() {
		return frame;
	}

	public static void setFrame(JFrame frame) {
		ServerGUI.frame = frame;
	}

	public static int getButtonLength() {
		return buttonLength;
	}

	public static void setButtonLength(int buttonLength) {
		ServerGUI.buttonLength = buttonLength;
	}

	public static int getButtonHeight() {
		return buttonHeight;
	}

	public static void setButtonHeight(int buttonHeight) {
		ServerGUI.buttonHeight = buttonHeight;
	}

	public static JTextField getPortTextField() {
		return portTextField;
	}

	public static void setPortTextField(JTextField portTextField) {
		ServerGUI.portTextField = portTextField;
	}

	public static JTextField getIpTextField() {
		return ipTextField;
	}

	public static void setIpTextField(JTextField ipTextField) {
		ServerGUI.ipTextField = ipTextField;
	}

	public static JTextField getPasswordTextField() {
		return passwordTextField;
	}

	public static void setPasswordTextField(JTextField passwordTextField) {
		ServerGUI.passwordTextField = passwordTextField;
	}
}
