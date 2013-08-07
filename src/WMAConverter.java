import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

public class WMAConverter
{
	private JFrame frame;
	private JButton btnClear;
	private JTextField txtSourceFile;
	private JTextField txtDestFile;
	private JFileChooser fcWMA;
	private JFileChooser fcMP3;
	private JFileChooser fcVLC;
	private File sourceFile;
	private File destFile;
	private File vlcFile;

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				try {
					WMAConverter window = new WMAConverter();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public WMAConverter()
	{
		sourceFile = null;
		destFile = null;
		vlcFile = null;
		fcWMA = new JFileChooser();
		fcWMA.setFileFilter(new FileNameExtensionFilter("WMA Files", "wma"));
		fcMP3 = new JFileChooser();
		fcMP3.setFileFilter(new FileNameExtensionFilter("Mp3 Files", "mp3"));
		fcVLC = new JFileChooser();
		fcVLC.setFileFilter(new FileNameExtensionFilter("VLC", "exe"));
		initialize();
	}

	private void initialize()
	{
		frame = new JFrame("WMA Converter");
		frame.setResizable(false);
		frame.setBounds(100, 100, 380, 250);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblSourceFile = new JLabel("WMA File:");
		lblSourceFile.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSourceFile.setBounds(10, 33, 68, 14);
		frame.getContentPane().add(lblSourceFile);
		
		txtSourceFile = new JTextField();
		txtSourceFile.setEditable(false);
		txtSourceFile.setBounds(88, 30, 185, 20);
		frame.getContentPane().add(txtSourceFile);
		txtSourceFile.setColumns(20);
		
		JButton btnSourceFile = new JButton("Select");
		btnSourceFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fcWMA.showOpenDialog(frame);

	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                sourceFile = fcWMA.getSelectedFile();
	                txtSourceFile.setText(sourceFile.getPath());
	            }
			}
		});
		btnSourceFile.setBounds(283, 29, 81, 20);
		frame.getContentPane().add(btnSourceFile);
		
		JLabel lblDestFile = new JLabel("Mp3 File:");
		lblDestFile.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDestFile.setBounds(10, 78, 68, 14);
		frame.getContentPane().add(lblDestFile);
		
		txtDestFile = new JTextField();
		txtDestFile.setEditable(false);
		txtDestFile.setColumns(20);
		txtDestFile.setBounds(88, 75, 185, 20);
		frame.getContentPane().add(txtDestFile);
		
		JButton btnDestFile = new JButton("Select");
		btnDestFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = fcMP3.showSaveDialog(frame);

	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                destFile = fcMP3.getSelectedFile();
	                txtDestFile.setText(destFile.getPath());
	            }
			}
		});
		btnDestFile.setBounds(283, 74, 81, 20);
		frame.getContentPane().add(btnDestFile);
		
		JButton btnConvert = new JButton("Convert");
		btnConvert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (sourceFile == null || destFile == null) {
					JOptionPane.showMessageDialog(frame, "Missing source or destination file.");
					return;
				}
				if (vlcFile == null) {
					JOptionPane.showMessageDialog(frame, "Location of vlc.exe not set.");
					return;
				}
				String[] cmd = { vlcFile.getPath(), "-I dummy", "-vvv", sourceFile.getPath(), "--sout=#transcode{acodec=mp3,ab=128,channels=2}:standard{access=file,dst=" + destFile.getPath() + "}", "vlc://quit" };
				Integer exitVal = null;
				try {
					System.out.println("1");
					Process p = Runtime.getRuntime().exec(cmd);
					System.out.println("2");
					StreamGobbler s1 = new StreamGobbler("stdin", p.getInputStream ());
				    StreamGobbler s2 = new StreamGobbler("stderr", p.getErrorStream ());
				    s1.start();
				    s2.start();
				    System.out.println(s2.output + s1.output);
					System.out.println("3");
					exitVal = p.waitFor();
					System.out.println("4");
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Unknown error occurred");
					return;
				}
				
				if (exitVal == 0) {
					JOptionPane.showMessageDialog(frame, "Success! :)");
				} else {
					JOptionPane.showMessageDialog(frame, "Failure! :(");
				}
				btnClear.doClick();
			}
		});
		btnConvert.setBounds(67, 128, 127, 49);
		frame.getContentPane().add(btnConvert);
		
		this.btnClear = new JButton("Clear");
		this.btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sourceFile = null;
				destFile = null;
				txtSourceFile.setText("");
				txtDestFile.setText("");
			}
		});
		this.btnClear.setBounds(204, 128, 127, 49);
		frame.getContentPane().add(this.btnClear);
		
		JButton btnSetVlcPath = new JButton("Set VLC Path");
		btnSetVlcPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = fcVLC.showOpenDialog(frame);

	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File tmpVLC = fcVLC.getSelectedFile();
	                if (!tmpVLC.getName().toLowerCase().equals("vlc.exe")) {
	                	JOptionPane.showMessageDialog(frame, "User did not selec vlc.exe");
	                }
	                vlcFile = fcVLC.getSelectedFile();
	            }
			}
		});
		btnSetVlcPath.setBounds(131, 188, 113, 23);
		frame.getContentPane().add(btnSetVlcPath);
	}
	
	private class StreamGobbler implements Runnable
	{
		String name;
		InputStream is;
		Thread thread;
		String output = "";

		public StreamGobbler(String name, InputStream is)
		{
			this.name = name;
			this.is = is;
		}

		public void start()
		{
			thread = new Thread(this);
			thread.start();
		}

		public void run()
		{
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);

				while (true) {
					String s = br.readLine();
					if (s == null)
						break;
					output += s;
				}
				is.close();
			} catch (Exception ex) {
				System.out.println("Problem reading stream " + name + "... :" + ex);
				ex.printStackTrace();
			}
		}
	}
}
