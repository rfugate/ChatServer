package server.gui.window;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import server.Server;
import server.Server.Sender;
import server.gui.ServerGUI;
import server.gui.ServerPreferences;
import server.gui.ServerTree;

public class ServerSettingsWindow {
	
	private JDialog settingsWindow;
	
	private JPanel mainPanel		= new JPanel();
	private JPanel leftPanel		= new JPanel(new BorderLayout());
	private JPanel rightPanel		= new JPanel(new BorderLayout());
	private JPanel jTreePanel		= new JPanel(new BorderLayout());
	private JPanel userAdminPanel	= new JPanel(new BorderLayout());
	private JPanel motdPanel		= new JPanel(new BorderLayout());
	private JPanel miscItemPanel	= new JPanel(new BorderLayout());
	private JPanel topPanel			= new JPanel(new BorderLayout());
	private JPanel bottomPanel		= new JPanel(new BorderLayout());
	private JPanel userButtonPanel	= new JPanel(new BorderLayout());
	private JPanel banButtonPanel	= new JPanel(new BorderLayout());
	private JPanel miscCkBoxPanel	= new JPanel(new FlowLayout(FlowLayout.LEFT));
	private JPanel finalizePanel	= new JPanel(new BorderLayout());
	
	private JMenuItem createChannel;
	private JMenuItem deleteChannel;
	private JMenuItem editChannel;
	private JMenuItem banSelected;
	private JMenuItem banList;
	private static DefaultTreeModel treeModel;
	private static JTree tree;
	
	/* SettingWindow JDialog Components */
	private JButton addButton;
	private JButton editButton;
	private JButton banListButton;
	private JButton saveButton;
	private JButton cancelButton;
	private JEditorPane motdTextPane;
	private JCheckBox logCkBox;
	private JCheckBox miniCkBox;
	private JCheckBox motdCkBox;

	/* Create JDialog Components */
	private JDialog createJDialog;
	private JTextField createChannelName;
	private JButton createButton;
	
	/* Delete JDialog Components */
	private JDialog deleteJDialog;
	private JButton deleteYesButton;
	private JButton deleteNoButton;
	private JLabel deleteLabel;
	
	/* Edit JDialog Components */
	private JDialog editJDialog;
	private JTextField editChannelName;
	private JButton editSaveButton;
	private JButton editCancelButton;
	
	private boolean sendMOTD = false;
	private boolean saveLog = false;
	private boolean didTreeChange = false;
	private DefaultTreeModel tempModel;
	
	private int buttonLength;
	private int buttonHeight;
	
	public ServerSettingsWindow() throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch(Exception ex){
			System.out.println(ex);
		}
		
		settingsWindow = new JDialog(ServerGUI.getFrame(), "Server Settings");
		
		buttonLength = ServerGUI.getButtonLength();
		buttonHeight = ServerGUI.getButtonHeight();
		
		createPanels();
		createUIObjects();
		
		settingsWindow.pack();
		settingsWindow.setVisible(true);
		settingsWindow.setResizable(false);
		settingsWindow.setLocationRelativeTo(ServerGUI.getFrame());
	}
	
	private void createPanels() {
		mainPanel.setPreferredSize(new Dimension(400,400));
		leftPanel.setPreferredSize(new Dimension(190,390));
		rightPanel.setPreferredSize(new Dimension(195,390));
		
		//Left Panel
		jTreePanel.setPreferredSize(new Dimension(190,245));
		motdPanel.setPreferredSize(new Dimension(190,140));
		
		//Right Panel
		userAdminPanel.setPreferredSize(new Dimension(195,200));
		miscItemPanel.setPreferredSize(new Dimension(195,185));
			topPanel.setPreferredSize(new Dimension(195,55));
			bottomPanel.setPreferredSize(new Dimension(195,130));
			userButtonPanel.setPreferredSize(new Dimension(125,buttonHeight));
			banButtonPanel.setPreferredSize(new Dimension(195,buttonHeight));
			miscCkBoxPanel.setPreferredSize(new Dimension(195,105));
			finalizePanel.setPreferredSize(new Dimension(125,buttonHeight));
			
		
		settingsWindow.add(mainPanel);
			mainPanel.add(leftPanel);
				leftPanel.add(jTreePanel, BorderLayout.NORTH);
				leftPanel.add(motdPanel, BorderLayout.SOUTH);
			mainPanel.add(rightPanel);
				rightPanel.add(userAdminPanel, BorderLayout.NORTH);
				rightPanel.add(miscItemPanel, BorderLayout.SOUTH);
					miscItemPanel.add(topPanel,BorderLayout.NORTH);
						topPanel.add(userButtonPanel, BorderLayout.NORTH);
						topPanel.add(banButtonPanel, BorderLayout.SOUTH);
					miscItemPanel.add(bottomPanel, BorderLayout.SOUTH);
						bottomPanel.add(miscCkBoxPanel, BorderLayout.NORTH);
						bottomPanel.add(finalizePanel, BorderLayout.SOUTH);
	}

	private void createUIObjects(){
		addButton	 = createButton("Add", buttonLength, buttonHeight, userButtonPanel, BorderLayout.WEST);
		editButton	 = createButton("Edit", buttonLength, buttonHeight, userButtonPanel, BorderLayout.EAST);
		banListButton= createButton("Ban List", 195, buttonHeight, banButtonPanel, BorderLayout.CENTER);
		saveButton	 = createButton("Save", buttonLength, buttonHeight, finalizePanel, BorderLayout.WEST);
		cancelButton = createButton("Cancel", buttonLength, buttonHeight, finalizePanel, BorderLayout.EAST);
		
		treeModel = ServerTree.getTreeModel();
		tree = new JTree(treeModel);
		tree.setCellRenderer(new MyRenderer());
		expandAllNodes(tree, 0, tree.getRowCount());
		tree.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				if(SwingUtilities.isRightMouseButton(e)){
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);
					if(pathBounds != null && pathBounds.contains(e.getX(), e.getY())){
						JPopupMenu menu = new JPopupMenu();
						createChannel = new JMenuItem("Create");
						createChannel.addActionListener(new MenuActionListener());
						menu.add(createChannel);
						deleteChannel = new JMenuItem("Delete");
						deleteChannel.addActionListener(new MenuActionListener());
						menu.add(deleteChannel);
						editChannel = new JMenuItem("Edit");
						editChannel.addActionListener(new MenuActionListener());
						menu.add(editChannel);
						menu.show(tree, pathBounds.x, pathBounds.y + pathBounds.height);
					}
				}
			}
		});
		JScrollPane treeScrollPane = new JScrollPane(tree);
		treeScrollPane.setPreferredSize(new Dimension(190,245));
		jTreePanel.add(treeScrollPane);
		
		motdTextPane = new JEditorPane();
		motdTextPane.setEnabled(ServerPreferences.prefs.getBoolean(ServerPreferences.getMOTD_CKBOX(), false));
		motdTextPane.setText("Message of the Day");
		JScrollPane motdScrollPane = new JScrollPane(motdTextPane);
		motdScrollPane.setPreferredSize(new Dimension(190,100));
		motdPanel.add(motdScrollPane);
		
		JEditorPane userAdminPane = new JEditorPane();
		userAdminPane.setText("Admin Users:");
		JScrollPane userAdminScrollPane = new JScrollPane(userAdminPane);
		userAdminScrollPane.setPreferredSize(new Dimension(125,200));
		userAdminPanel.add(userAdminScrollPane);
		
		logCkBox	= createCheckBox("Log Messages", ServerPreferences.getLOG_MESSAGES(), miscCkBoxPanel);
		miniCkBox	= createCheckBox("Minimize to System Tray", ServerPreferences.getMINI_SYS_TRAY(), miscCkBoxPanel);
		motdCkBox	= createCheckBox("Message of the Day", ServerPreferences.getMOTD_CKBOX(), miscCkBoxPanel);
	}
	private static void expandAllNodes(JTree tree, int startingIndex, int rowCount){
		for(int i = startingIndex; i < rowCount; i++){
			tree.expandRow(i);
		}
		
		if(tree.getRowCount() != rowCount){
			expandAllNodes(tree, rowCount, tree.getRowCount());
		}
	}
	
	private JCheckBox createCheckBox(String text, String preference, JPanel panel){
		JCheckBox checkBox = new JCheckBox(text);
		checkBox.setSelected(ServerPreferences.prefs.getBoolean(preference, false));
		checkBox.addActionListener(new CheckBoxAction());
		panel.add(checkBox);
		return checkBox;
	}

	private JButton createButton(String text, int length, int height, Container container, String position) {
		JButton button = new JButton(text);
		button.setPreferredSize(new Dimension(length, height));
		button.addActionListener(new ButtonAction());
		container.add(button, position);
		return button;
	}
	
	private JTextField createTextField(String text, int length, int height, Container container){
		JTextField textField = new JTextField(text);
		textField.setPreferredSize(new Dimension(length, height));
		textField.addActionListener(new ButtonAction());
		container.add(textField);
		return textField;
	}
	
	private JLabel createLabel(String text, Container container){
		JLabel label = new JLabel(text);
		container.add(label);
		return label;
	}
	
	private void createDialog(JDialog owner, JDialog myself, Boolean isVisible){
		myself.pack();
		myself.setLocationRelativeTo(owner);
		myself.setVisible(isVisible);
	}
	
	private Container createContainer(JDialog owner, int length, int height){
		Container container = owner.getContentPane();
		container.setPreferredSize(new Dimension(length, height));
		container.setLayout(new FlowLayout());
		return container;
	}
	
	public static boolean enabledMOTD(boolean sendMOTD){
		if(sendMOTD == true)
			return true;
		return false;
	}
	
	public class MenuActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src == createChannel){
				createJDialog		= new JDialog(settingsWindow, "Create Channel", true);
				Container pane		= createContainer(createJDialog, 300, 35);
				createChannelName	= createTextField("", 200, 25, pane);
				createButton		= createButton("Create", 85, 25, pane, null);
				
				createDialog(settingsWindow, createJDialog, true);
				
			} else if(src == deleteChannel){
				deleteJDialog		= new JDialog(settingsWindow, "Delete Channel", true);
				Container pane		= createContainer(deleteJDialog, 275, 55);
				deleteLabel			= createLabel("Are you sure you want to delete this channel?", pane);
				deleteYesButton		= createButton("Yes", buttonLength, buttonHeight, pane, null);
				deleteNoButton		= createButton("No", buttonLength, buttonHeight, pane, null);
				
				createDialog(settingsWindow, deleteJDialog, true);
				
			} else if(src == editChannel){
				editJDialog			= new JDialog(settingsWindow, "Edit Channel", true);
				Container pane		= createContainer(editJDialog, 300, 65);
				JLabel label		= createLabel("Channel Name: ", pane);
				editChannelName		= createTextField(tree.getLastSelectedPathComponent().toString(), 200, 25, pane);
				editSaveButton		= createButton("Save", buttonLength, buttonHeight, pane, null);
				editCancelButton	= createButton("Cancel", buttonLength, buttonHeight, pane, null);
				
				createDialog(settingsWindow, editJDialog, true);
			}
		}
	}
	
	public class CheckBoxAction implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src == logCkBox){
				System.out.println("Log Messages!");
				AbstractButton abstractButton = (AbstractButton) src;
				if(abstractButton.getModel().isSelected()){ //If Check box is checked then save preference to true
					ServerPreferences.prefs.putBoolean(ServerPreferences.getLOG_MESSAGES(), true);
				} else {
					ServerPreferences.prefs.putBoolean(ServerPreferences.getLOG_MESSAGES(), false);
				}
				
			} else if(src == miniCkBox){
				System.out.println("Minize to System Tray!");
				AbstractButton abstractButton = (AbstractButton) src;
				if(abstractButton.getModel().isSelected()){
					ServerPreferences.prefs.putBoolean(ServerPreferences.getMINI_SYS_TRAY(), true);
				} else {
					ServerPreferences.prefs.putBoolean(ServerPreferences.getMINI_SYS_TRAY(), false);
				}
			} else if(src == motdCkBox){
				System.out.println("Message of the Day!");
				AbstractButton abstractButton = (AbstractButton) e.getSource();
				sendMOTD = abstractButton.getModel().isSelected();
				if(sendMOTD){
					ServerPreferences.prefs.putBoolean(ServerPreferences.getMOTD_CKBOX(), true);
				} else {
					ServerPreferences.prefs.putBoolean(ServerPreferences.getMOTD_CKBOX(), false);
				}
				motdTextPane.setEnabled(sendMOTD);
			}
		}
	}
	
	public class ButtonAction extends AbstractAction{

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src == createButton || src == createChannelName){
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(createChannelName.getText());
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				ServerTree.createChannel(child, parent);
				tree.expandPath(new TreePath(parent.getPath()));
				createJDialog.dispose();
			} else if(src == deleteYesButton){
				TreePath[] paths = tree.getSelectionPaths();
				ServerTree.deleteChannel(paths);
				deleteJDialog.dispose();
			} else if(src == deleteNoButton){
				deleteJDialog.dispose();
			} else if(src == editSaveButton || src == editChannelName ){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				ServerTree.editChannel(node, editChannelName.getText());
				editJDialog.dispose();
			} else if(src == editCancelButton){
				editJDialog.dispose();
			} else if(src == addButton){
				// Add Admin Users
			} else if(src == editButton){
				// Edit Admin Users
			} else if(src == saveButton){
				//Tree Changed
				//Send Update if the Model Changed
				settingsWindow.dispose();
			} else if(src == cancelButton){
				settingsWindow.dispose();
			} 
		}
	}
	
	private static class MyRenderer extends DefaultTreeCellRenderer{
		
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus){
			super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			String s = node.getUserObject().toString();
			if(Server.getUsersStatus().containsKey(s)){
				if(Server.getUsersStatus().get(s).equals("Available")){
					setIcon(ServerTree.getUserAvailIcon());
				} else if (Server.getUsersStatus().get(s).equals("Busy")){
					 setIcon(ServerTree.getUserBusyIcon());
				} else if (Server.getUsersStatus().get(s).equals("Away")){
					setIcon(ServerTree.getUserAwayIcon());
				} else if (Server.getUsersStatus().get(s).equals("Batman")){
					setIcon(ServerTree.getUserBatmanIcon());
				}
			} else if(node.isRoot()){
				setIcon(ServerTree.getRootNodeIcon());
			} else if(node.isLeaf()){
				setIcon(ServerTree.getChannelClosedIcon());
			} else if(!node.isLeaf()){
				setIcon(ServerTree.getChannelOpenIcon());
			}
			return this;
		}
	}
}
