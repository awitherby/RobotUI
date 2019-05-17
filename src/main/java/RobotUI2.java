
/**
 * Main class for.
 * Java user interface for the robot so that the user can specify location of the tower, and issue 
 * commands in a windowing environment
 */
/**
 * @author arthur witherby
 *
 */

import javax.imageio.ImageIO;
import javax.swing.*; 
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import com.fazecast.jSerialComm.*;

public class RobotUI2 {
	
	private static final int NUM_TEXT_FIELD = 6;
	private static final String START = "Start";
	private static final String STOP = "Stop";
	private static final String HOME = "Home";
	private static final String SET_CONFIGURATION = "Set Configuration";
	private static final String CONFIG_LOADING = "Read In Loading Bay";
	private static final String HOME_ARDUINO = "1";
	private static final String START_ARDUINO = "2";
	private static final String STOP_ARDUINO = "3";
	private static final String CONFIGURE_ARDUINO = "4";
	private static final String CONFIG_LOADING_ARDUINO = "5";
	private static final String RESUME = "6";
	private static final int NUM_JOINTS = 5;
	private static final int BAUD_RATE =  115200;
	private static final int BIT_RATE = 8;
	private static final int BUTTON_HEIGHT = 40;
	private static final int BUTTON_WIDTH = 100;
	private static final int BUTTON_WIDTH_LARGE = 155;
	private static final int TEXT_HEIGHT = 20;
	private static final int TEXT_BOX_WIDTH = 100;
	private static final int TEXT_LABEL_WIDTH = 210;
	private static final int HEADING_WIDTH = 320;
	private static final int HEADING_HEIGHT = 28;
	private static final int MARGIN = 10;
	private static final int HEADING_FONT_SIZE = HEADING_HEIGHT;
	private static final int OUTPUT_HEIGHT = 4*TEXT_HEIGHT;
	private static final Color TEXT_COLOR = Color.WHITE;
	private static final Color BUTTON_COLOR = Color.CYAN;
	private static final Color HEADING_COLOR = Color.WHITE;
	private static final String BACKGROUND_PATH = "C:/Users/arthu/eclipse-workspace/RobotUI2/src/main/resources/Background.jpg";
	
	//private ReadInThread readThread;
	private boolean portFlag = false; //set for debut must change to false 
	private SerialPort port;
	private char[] readBuffer = new char[100];
	private int charInd = 0;
	//note text fields are saved as the first 6 components, output window as 7
	private JFrame window;
	private JTextField[] textFields = new JTextField[NUM_TEXT_FIELD];
	private JTextArea output = new JTextArea();
	private JTextField[] jointFields;
	
	
	public static void main(String args[]) { 
		RobotUI2 ui = new RobotUI2();
	}
	
	
	public RobotUI2(){
		makeNewWindow();
		
	}
	/**
	 * makes the JFrame window
	 * @return
	 */
	public void makeNewWindow(){
		
		window=new JFrame();
		
		//declaration of objects
		JButton start =new JButton(START);
	    JButton stop  =new JButton(STOP);
		JButton home  =new JButton(HOME);
		JButton setConfiguration = new JButton(SET_CONFIGURATION);
		JButton configLoading = new JButton(CONFIG_LOADING);
		JLabel heading = new JLabel("ROBOT CONTROLLER");
		JLabel portsSelect = new JLabel("Select Serial Port");
		JLabel towerXLabel = new JLabel("x co-ordinate for jenga tower:"); 
		JLabel towerYLabel = new JLabel("x co-ordinate for jenga tower:"); 
		JLabel towerAngleLabel = new JLabel("angle of jenga tower in x,y:"); 
		JLabel loadingXLabel = new JLabel("x co-ordinate for loading bay:");  
		JLabel loadingYLabel = new JLabel("y co-ordinate for loading bay:"); 
		JLabel loadingAngleLabel = new JLabel("angle of loading bay in x,y:"); 
		JLabel currentConfigLabel = new JLabel("Robots current configuration:");
		JLabel sentCommands = new JLabel("Console:");
		jointFields = new JTextField[NUM_JOINTS];
		JLabel[] jointLabel = new JLabel[NUM_JOINTS];
			for(int i=0;i<NUM_JOINTS;i++) {
				jointLabel[i]= new JLabel("Joint " + (i+1) + " :");
				jointFields[i] = new JTextField();
				jointFields[i].setEditable(false);
			}
		
		
		JScrollPane scroll = new JScrollPane(output);
	    //scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		SerialPort[] ports = SerialPort.getCommPorts();
		String[] portDescriptors = new String[ports.length];
		for(int i = 0;i<portDescriptors.length;i++) {
			portDescriptors[i] = ports[i].getDescriptivePortName();
		}
		JComboBox<String> serialPorts = new JComboBox((String[])portDescriptors);
		int y=MARGIN;
		//location of objects
		heading.setBounds(MARGIN+5,MARGIN,HEADING_WIDTH,HEADING_HEIGHT);
		y=HEADING_HEIGHT+MARGIN;
		portsSelect.setBounds(MARGIN,y,HEADING_WIDTH,TEXT_HEIGHT);
		y+=TEXT_HEIGHT+MARGIN;
		serialPorts.setBounds(MARGIN,y,HEADING_WIDTH,TEXT_HEIGHT);
		y+=TEXT_HEIGHT+MARGIN;
		configLoading.setBounds(MARGIN,y,HEADING_WIDTH,BUTTON_HEIGHT);
		for(int i=0;i<textFields.length;i++) {
			textFields[i] = new JTextField(10);
			textFields[i].setBounds(TEXT_LABEL_WIDTH+2*MARGIN,y+i*(TEXT_HEIGHT+MARGIN),
									TEXT_BOX_WIDTH, TEXT_HEIGHT);
		}
	
		towerXLabel.setBounds(MARGIN,y,TEXT_LABEL_WIDTH, TEXT_HEIGHT); 
		y+=TEXT_HEIGHT+MARGIN;
		towerYLabel.setBounds(MARGIN,y,TEXT_LABEL_WIDTH, TEXT_HEIGHT);
		y+=TEXT_HEIGHT+MARGIN;
		towerAngleLabel.setBounds(MARGIN,y,TEXT_LABEL_WIDTH, TEXT_HEIGHT);
		y+=TEXT_HEIGHT+MARGIN;
		loadingXLabel.setBounds(MARGIN,y,TEXT_LABEL_WIDTH, TEXT_HEIGHT);
		y+=TEXT_HEIGHT+MARGIN;
		loadingYLabel.setBounds(MARGIN,y,TEXT_LABEL_WIDTH, TEXT_HEIGHT); 
		y+=TEXT_HEIGHT+MARGIN;
		loadingAngleLabel.setBounds(MARGIN,y,TEXT_LABEL_WIDTH, TEXT_HEIGHT); 
		y+=TEXT_HEIGHT+MARGIN;
		
		configLoading.setBounds(MARGIN, y, BUTTON_WIDTH_LARGE, BUTTON_HEIGHT);
		setConfiguration.setBounds(2*MARGIN+BUTTON_WIDTH_LARGE,y,BUTTON_WIDTH_LARGE,BUTTON_HEIGHT);
		y+=BUTTON_HEIGHT+MARGIN;
		start.setBounds(MARGIN,y,BUTTON_WIDTH, BUTTON_HEIGHT);
		stop.setBounds(2*MARGIN+BUTTON_WIDTH,y,BUTTON_WIDTH, BUTTON_HEIGHT);
		home.setBounds(3*MARGIN+2*BUTTON_WIDTH,y,BUTTON_WIDTH, BUTTON_HEIGHT);
		y+=BUTTON_HEIGHT+MARGIN;
		currentConfigLabel.setBounds(MARGIN,y,HEADING_WIDTH,TEXT_HEIGHT);
		y+=TEXT_HEIGHT+MARGIN;
		for(int i=0;i<NUM_JOINTS-2;i++) {
			jointLabel[i].setBounds(MARGIN+i*(MARGIN+TEXT_BOX_WIDTH),y,TEXT_BOX_WIDTH, TEXT_HEIGHT);
			jointFields[i].setBounds(MARGIN+i*(MARGIN+TEXT_BOX_WIDTH),y+TEXT_HEIGHT+MARGIN,
									TEXT_BOX_WIDTH, TEXT_HEIGHT);
		}
		y+=(TEXT_HEIGHT+MARGIN)*2;
		jointLabel[3].setBounds(MARGIN,y,TEXT_BOX_WIDTH, TEXT_HEIGHT);
		jointFields[3].setBounds(MARGIN,y+TEXT_HEIGHT+MARGIN,
									TEXT_BOX_WIDTH, TEXT_HEIGHT);
		jointLabel[4].setBounds(MARGIN*2+TEXT_BOX_WIDTH,y,TEXT_BOX_WIDTH, TEXT_HEIGHT);
		jointFields[4].setBounds(MARGIN*2+TEXT_BOX_WIDTH,y+TEXT_HEIGHT+MARGIN,
									TEXT_BOX_WIDTH, TEXT_HEIGHT);
		y+=(TEXT_HEIGHT+MARGIN)*2;
		//output.setBounds(MARGIN,y,HEADING_WIDTH,OUTPUT_HEIGHT);
		sentCommands.setBounds(MARGIN,y,HEADING_WIDTH,TEXT_HEIGHT);
		y+=(TEXT_HEIGHT+MARGIN);
		scroll.setBounds(MARGIN,y,HEADING_WIDTH,OUTPUT_HEIGHT);
		//
		y+=OUTPUT_HEIGHT+MARGIN;
		//set colors
		for(int i =0;i<textFields.length;i++) {
			textFields[i].setBackground(BUTTON_COLOR);
		}
		for(int i =0;i<jointFields.length;i++) {
			jointFields[i].setBackground(BUTTON_COLOR);
			jointLabel[i].setForeground(TEXT_COLOR);
		}
		try {
			File imageFile = new File(BACKGROUND_PATH);
			BufferedImage myImage = ImageIO.read(imageFile);
			window.setContentPane(new ImagePanel(myImage));
		}
		//if file does not exist
		catch(NullPointerException e) {
			e.printStackTrace();
			window.getContentPane().setBackground(Color.BLACK);
		}
		catch(IOException e) {
			e.printStackTrace();
			window.getContentPane().setBackground(Color.BLACK);
		}
		sentCommands.setForeground(TEXT_COLOR);
		portsSelect.setForeground(TEXT_COLOR);
		heading.setForeground(HEADING_COLOR);
		heading.setFont(new Font(Font.SERIF, Font.BOLD, HEADING_FONT_SIZE));
		setConfiguration.setBackground(BUTTON_COLOR);
		configLoading.setBackground(BUTTON_COLOR);
		start.setBackground(BUTTON_COLOR);
		stop.setBackground(BUTTON_COLOR);
		home.setBackground(BUTTON_COLOR);
		towerXLabel.setForeground(TEXT_COLOR);
		towerYLabel.setForeground(TEXT_COLOR); 
		towerAngleLabel.setForeground(TEXT_COLOR); 
		loadingXLabel.setForeground(TEXT_COLOR); 
		loadingYLabel.setForeground(TEXT_COLOR); 
		loadingAngleLabel.setForeground(TEXT_COLOR); 
		serialPorts.setBackground(BUTTON_COLOR);
		currentConfigLabel.setForeground(TEXT_COLOR);
		//scroll.getViewport().setBackground(BUTTON_COLOR);
		output.setBackground(BUTTON_COLOR);
		output.setEditable(false);
		
		//add components
		for(int i =0;i<textFields.length;i++) {
			window.add(textFields[i]);
		}
		for(int i=0;i<NUM_JOINTS;i++) {
			window.add(jointLabel[i]);
			window.add(jointFields[i]);
		}
		window.add(scroll);
		//window.add(output);
		window.add(heading);
		window.add(portsSelect);
		window.add(start);
		window.add(stop);
		window.add(home);
		window.add(setConfiguration);
		window.add(towerXLabel);
		window.add(towerYLabel);
		window.add(towerAngleLabel);
		window.add(loadingXLabel);
		window.add(loadingYLabel);
		window.add(loadingAngleLabel);
		window.add(currentConfigLabel);
		window.add(serialPorts);
		window.add(sentCommands);
		window.add(configLoading);
	
		//add listeners 
		start.addActionListener(new EventHandler());
		stop.addActionListener(new EventHandler());
		home.addActionListener(new EventHandler());
		configLoading.addActionListener(new EventHandler());
		setConfiguration.addActionListener(new EventHandler());
		serialPorts.addActionListener(new PortSelect());
		window.addWindowListener(new WindowExit());
		// set up frames initial location 
		window.setTitle("Robot Control Interface");
		window.setSize(HEADING_WIDTH+3*MARGIN+5,y+35);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		window.setLocation(dim.width/2-window.getSize().width/2, dim.height/2-window.getSize().height/2);
		window.setLayout(null); 
		window.setVisible(true);  

	}

	
	
	/**
	 * this class handles the selection of the Serial Port 
	 * @author Arthur Witherby
	 *
	 */
	private class PortSelect implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			port = SerialPort.getCommPort("COM3");
			port.openPort();
		
			port.setComPortParameters(BAUD_RATE, BIT_RATE, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
			port.addDataListener(new Reader());
			/*readThread = new ReadInThread();
			new Thread(readThread).start();*/
			portFlag = true;
		}
	}
	
	
	
	/**
	 * this class handles input events
	 * @author Arthur Witherby
	 *
	 */
	private class WindowExit implements WindowListener{

		public void windowOpened(WindowEvent e) {}
		public void windowClosing(WindowEvent e) {
			if(portFlag) {
				port.closePort();
				portFlag = false;	
			}
			
		}
		public void windowClosed(WindowEvent e) {}
		public void windowIconified(WindowEvent e) {}
		public void windowDeiconified(WindowEvent e) {}
		public void windowActivated(WindowEvent e) {}
		public void windowDeactivated(WindowEvent e) {}
	}
	
	
	private class EventHandler implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			if(!portFlag) {
				output.append("Error: Please specify the Serial Port." +"\n");
				return;
			}
			String command = e.getActionCommand();
			

				if(command.equals(CONFIG_LOADING)) {
					output.append("Command sent " + "CONFIG_LOADING :" + CONFIG_LOADING_ARDUINO +"\n");
					writeToArduino(CONFIG_LOADING_ARDUINO);	
				}
				
				if(command.equals(START)){
					output.append("Command sent " + "START_ARDUINO :" + START_ARDUINO +"\n");
					writeToArduino(START_ARDUINO);	
				}
				else if(command.equals(STOP)){
					output.append("Command sent " + "STOP_ARDUINO :" + STOP_ARDUINO +"\n");
					writeToArduino(STOP_ARDUINO);
				}
				else if(command.equals(HOME)) {
					writeToArduino(HOME_ARDUINO);
					output.append("Command sent " + "HOME_ARDUINO :" + HOME_ARDUINO +"\n");
				}
				else if(command.equals(SET_CONFIGURATION)){
					output.append("Command sent " + "CONFIGURE_ARDUINO :" + CONFIGURE_ARDUINO +"\n");
					output.append(getConfigString());
					writeToArduino(CONFIGURE_ARDUINO);
					for(int i=0;i<NUM_TEXT_FIELD;i++) {
						writeToArduino(" ");
						writeToArduino(textFields[i].getText());
					}
					//writeToArduino(getConfigString());
				}
			}
		
		private void writeToArduino(String out) {
			byte[] bts = out.getBytes(Charset.forName("UTF-8"));
			port.writeBytes(bts, 1);	
			
		}
		private void writeToArduino(int out) {
			writeToArduino(Integer.toString(out));	
		}
		
		
		/**
		 * @return, the string to configure the Robot
		 */
		private String getConfigString() {
			String configString = "";
			
			for(int i=0;i<NUM_TEXT_FIELD;i++) {
				
				configString+= textFields[i].getText();
				if(i<NUM_TEXT_FIELD-1) {
					configString+=",";
				}
			
					
				
			}
			return configString+"\n";
		}
	}
	private class Reader implements SerialPortDataListener{

		public int getListeningEvents() {
			// TODO Auto-generated method stub
			return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
		}

		public void serialEvent(SerialPortEvent event) {
		    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
		         return;
		      byte[] newData = new byte[port.bytesAvailable()];
		      int numRead = port.readBytes(newData, newData.length);
		   
			char[] inData = (new String(newData,Charset.forName("ASCII"))).toCharArray(); 
		    
			if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
		         return;
			} 
			//System.out.println(inData);
			for(int i=0;i<inData.length;i++){
			
				readBuffer[charInd]=inData[i];
				
				if(readBuffer[charInd]=='\n') {
					String outString=new String(readBuffer);
					if(readBuffer[1]=='$') {
						writeConfig(outString.substring(2,charInd-2));
					}
					else {
						output.append(outString);
					}
					charInd=0;
					readBuffer = new char[100];
				}
				charInd++;
			}
			return;
		}
		private void writeConfig(String config) {
			String[] in = config.split(",");
			for(int i=0;i<in.length;i++) {
				jointFields[i].setText(in[i]); 
			}

		}
	}
	/**
	 * Class taken from stackoverflow.com/questions/1064977/setting-background-images-in-jframe
	 * @author arthu
	 *
	 */
	class ImagePanel extends JComponent {
	    private Image image;
	    public ImagePanel(Image image) {
	        this.image = image;
	    }
	    @Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        g.drawImage(image, 0, 0, this);
	    }
	}

}

