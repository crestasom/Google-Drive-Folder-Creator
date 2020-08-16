package googledrive;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DriveFolderCreatorUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	JPanel mainPanel, rootFolderPanel, csvFilePanel, actionPanel;
	JButton csvFileBtn, okBtn, resetBtn;

	HintTextField root;
	JLabel rootFolderLbl, csvFileLbl;
	JFileChooser fc;
	String rootDir, csvDir;
	File csvFolder;

	public static void main(String[] args) {
		if (!DriveFolderCreator.checkCredentialFolder()) {
			JOptionPane.showMessageDialog(null,
					"Created Folder: '" + DriveFolderCreator.CREDENTIALS_FOLDER.getAbsolutePath() + "'. Copy file '"
							+ DriveFolderCreator.CLIENT_SECRET_FILE_NAME
							+ "' into this folder.. and rerun this program!!");
			return;
		}
		new DriveFolderCreatorUI();
	}

	public DriveFolderCreatorUI() {
		super("Drive Folder Creator");
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		rootFolderLbl = new JLabel("Enter parent Folder to upload");
		root = new HintTextField("Select parent Folder to upload");
		rootFolderPanel = new JPanel();
		rootFolderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		rootFolderPanel.add(rootFolderLbl);
		rootFolderPanel.add(root);

		csvFilePanel = new JPanel();
		csvFilePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		csvFileBtn = new JButton("Choose CSV File");
		csvFileBtn.addActionListener(this);
		csvFilePanel.add(csvFileBtn);
		csvFileLbl = new JLabel("No File Selected");
		csvFilePanel.add(csvFileLbl);

		actionPanel = new JPanel();
		actionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		okBtn = new JButton("Done");
		okBtn.addActionListener(this);
		actionPanel.add(okBtn);
		resetBtn = new JButton("Reset");
		resetBtn.addActionListener(this);
		actionPanel.add(resetBtn);

		mainPanel.add(rootFolderPanel);
		mainPanel.add(Box.createVerticalGlue());
		mainPanel.add(csvFilePanel);
		mainPanel.add(Box.createVerticalGlue());
		mainPanel.add(actionPanel);
		add(mainPanel);
		setResizable(false);
		setSize(500, 250);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(csvFileBtn)) {
			JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(new java.io.File(System.getProperty("user.home"))); // start at application current
																						// directory
			FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV file", "csv");
			fc.setFileFilter(filter);
			int returnVal = fc.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				csvFolder = fc.getSelectedFile();
				csvDir = csvFolder.getAbsolutePath();
				csvFileLbl.setText(csvDir);
			}
		} else if (e.getSource().equals(resetBtn)) {

			csvDir = null;
			csvFileLbl.setText("No File Selected");
		} else if (e.getSource().equals(okBtn)) {
			if (csvDir != null && !root.getText().isBlank()) {
				DriveFolderCreator.createDriveFolder(csvDir, root.getText());
				JOptionPane.showMessageDialog(null, "Folder Created Successfully");
			} else
				JOptionPane.showMessageDialog(null, "Please enter parent folder and choose csv file");
		}

	}
}

class HintTextField extends JTextField implements FocusListener {

	private final String hint;
	private boolean showingHint;

	public HintTextField(final String hint) {
		super(hint);
		this.hint = hint;
		this.showingHint = true;
		super.addFocusListener(this);
	}

	@Override
	public String getText() {
		return showingHint ? "" : super.getText();
	}

	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub
		if (this.getText().isEmpty()) {
			super.setText("");
			showingHint = false;
		}
	}

	public void focusLost(FocusEvent e) {
		// TODO Auto-generated method stub
		if (this.getText().isEmpty()) {
			super.setText(hint);
			showingHint = true;
		}
	}
}