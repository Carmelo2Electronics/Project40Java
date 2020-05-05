package project40java;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import com.fazecast.jSerialComm.SerialPort;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.awt.Color;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

//iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
public class Project40Java {
	public static void main(String[] args) {
		MarcoPrincipal marcoPrincipal =new MarcoPrincipal();
		marcoPrincipal.setVisible(true);
	}
}
//iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii

//99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
class MarcoPrincipal extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	private JButton btnGPIO1=new JButton ("GPIO_1");
	private JButton btnGPIO2=new JButton ("GPIO_2");
	private JButton btnGPIO3=new JButton ("GPIO_3");	
	private JLabel bienvenida=new JLabel("              WELCOME              ");
	private GPIO gpio;
	
	public MarcoPrincipal() {
		boolean banderaSO=new SistemaOperativo().getBanderaSO();	
		if(banderaSO) {								//estas en Linux
			gpio=new GPIO(banderaSO);
		}else {										//estas en Windows
			gpio=new GPIO(banderaSO, "Windows");
		}
		
		gpio.GPIOlow(0);		
		gpio.GPIOlow(1);
		gpio.GPIOlow(2);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);			//esto es para detener la libreria Pi4J
		setTitle("MAIN");
		setResizable(false);
		setBounds(20, 50, 300, 120);
		setLayout(new FlowLayout());		
		bienvenida.setFont(new Font("Courier", Font.BOLD, 18));		
		add(bienvenida);
		add(btnGPIO1);
		add(btnGPIO2);		
		add(btnGPIO3);
		btnGPIO1.addActionListener(this);
		btnGPIO2.addActionListener(this);
		btnGPIO3.addActionListener(this);	
		
		addWindowListener(new WindowAdapter() {							//esto es para detener la libreria Pi4J
	        public void windowClosing(WindowEvent event) {
	    		int opcion=JOptionPane.showConfirmDialog(null, "You want to close the entire program", 
	    				"CONFIRMATION", JOptionPane.OK_CANCEL_OPTION);

	    		if(opcion==0) {
	    			operacionCierre();
	    		}	        	
	        }
	    });
	}	
	public void operacionCierre() {
		gpio.GPIOlow(0);		
		gpio.GPIOlow(1);
		gpio.GPIOlow(2);
		gpio.OffGpioController();	
		dispose();
		System.exit(0);	
	}
	public void actionPerformed(ActionEvent e) {		
		if(e.getSource()==btnGPIO1) {
			btnGPIO1.setEnabled(false);
			new MarcoGPIO(0, btnGPIO1, gpio );			
		}
		if(e.getSource()==btnGPIO2) {
			btnGPIO2.setEnabled(false);
			new MarcoGPIO(1, btnGPIO2, gpio);			
		}		
		if(e.getSource()==btnGPIO3) {
			btnGPIO3.setEnabled(false);
			new MarcoGPIO(2, btnGPIO3, gpio);		
		}
	}	
}
//99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999

//-----------------------------------------------------------------------------------
class MarcoGPIO extends JFrame{
	
	private static final long serialVersionUID = 1L;
	private PanelGPIO panel;
	private NameUserAndPin nameUserAndPin ;
	private JButton btnGPIO;
	private GPIO gpio; 
	private int pin;

	public MarcoGPIO(int pin, JButton btnGPIO, GPIO gpio){		
		this.btnGPIO=btnGPIO;
		this.gpio=gpio;
		this.pin=pin;	
		
		nameUserAndPin=new NameUserAndPin(pin);		
		gpio.GPIOlow(pin);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setTitle(nameUserAndPin.getNameUser() + " " + nameUserAndPin.getNameGPIO() + " ");	
		setBounds(20, 50, 700, 240);
		setResizable(false);		

		panel=new PanelGPIO(nameUserAndPin, pin, gpio);
		add(panel);	
		setVisible(true);
		
		addWindowListener(new WindowAdapter() {							//esto es para detener la libreria Pi4J
	        public void windowClosing(WindowEvent event) {
	            exitGPIO();
	        }
	    });
	}	
	public void exitGPIO() {	
		gpio.GPIOlow(pin);		
		btnGPIO.setEnabled(true);
		dispose();
	}
}
//-----------------------------------------------------------------------------------

//lllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll
class PanelGPIO extends JPanel implements ActionListener, Runnable {
	
	private static final long serialVersionUID = 1L;
	private JButton STARTbtn=new JButton("START");
	private JButton ABORTbtn=new JButton("ABORT");
	private JButton CONFIGUREbtn=new JButton("CONFIGURE");
	private JButton LIGHTbtn=new JButton("LIGHT ON/OFF");
	private JButton DELAYbtn=new JButton("DELAY");
	private boolean banEncApaLed1=true;
	private JLabel EtqTimeON=new JLabel("");
	private JLabel EtqTimeOFF=new JLabel("");
	private JButton PILOTObtn=new JButton("    ");		
	private ConfiguracionGPIO configuracion;		
	private boolean banCiclos=true;	
	private JLabel EtqHrIni=new JLabel("");
	private JLabel EtqHrFin=new JLabel("");		
	private Timer timerDelay;	
	private VentanaDelayGPIO ventanaDelay;
	private int cicloTiempoON;
	private int cicloTiempoOFF;
	private int numeroDeCiclos;
	private int cicloNumero;
	private String patron = "    CURRENT CYCLE: %04d                             ";
	private String cadenaCuentaCiclos=String.format(patron,cicloNumero);
	private NameUserAndPin nameUserAndPin ;
	private int pin;
	private GPIO gpio;
	private ScheduledExecutorService schd;
	
	public PanelGPIO( NameUserAndPin nameUserAndPin, int pin, GPIO gpio) {
		
		this.nameUserAndPin=nameUserAndPin;
		this.pin=pin;
		this.gpio=gpio;
		
		EtqTimeON.setFont(new Font("Courier", Font.BOLD, 18));
		EtqTimeOFF.setFont(new Font("Courier", Font.BOLD, 18));			
		EtqHrIni.setFont(new Font("Courier", Font.BOLD, 18));
		EtqHrFin.setFont(new Font("Courier", Font.BOLD, 18));
				
		add(new JLabel("                                                           ")).setFont(new Font("Courier", Font.BOLD, 20));
		add(new JLabel("SETTINGS ")).setFont(new Font("Courier", Font.BOLD, 20));
		add(STARTbtn);
		add(ABORTbtn);
		add(CONFIGUREbtn);
		add(LIGHTbtn);
		add(PILOTObtn);
		add(DELAYbtn);		
		add(EtqTimeON);
		add(EtqTimeOFF);	
		add(EtqHrIni);
		add(EtqHrFin);
				
		STARTbtn.addActionListener(this);
		ABORTbtn.addActionListener(this);
		CONFIGUREbtn.addActionListener(this);
		LIGHTbtn.addActionListener(this);
		DELAYbtn.addActionListener(this);

		STARTbtn.setEnabled(false);					//desactivo boton START
		ABORTbtn.setEnabled(false);					//desactivo boton DESTROYES
		DELAYbtn.setEnabled(false);					//desactivo boton DELAY
		CONFIGUREbtn.setEnabled(true);				//activo boton CONFIGURE
		LIGHTbtn.setEnabled(true);					//activo boton LIGHTbtn

		gpio.GPIOlow(pin);
		PILOTObtn.setBackground(Color.BLACK);
	}
	public void actionPerformed(ActionEvent e) {
//----------------
		if(e.getSource()==ABORTbtn) {						
			EtqHrIni.setText("");						//borro etiqueta tiempo fin
			EtqHrFin.setText("");						//borro etiqueta tiempo inicio			
			EtqTimeON.setText("");						//borro etiqueta tiempo alto
			EtqTimeOFF.setText("");						//borro etiqueta tiempo bajo
			
			STARTbtn.setEnabled(false);					//desactivo boton START
			DELAYbtn.setEnabled(false);					//desactivo boton DELAY
			ABORTbtn.setEnabled(false);					//desactivo boton DESTROYES
			CONFIGUREbtn.setEnabled(true);				//activo boton CONFIGURE
			LIGHTbtn.setEnabled(true);					//activo boton ON/OFF
			
			STARTbtn.setBackground(null);				//indico el programa ya no corre START y quito color verde 
			DELAYbtn.setBackground(null);				//indico el programa ya no corre DELAY y quito color verde
								
			banCiclos=false;							//evito que corran mas ciclos ON/OFF en el Thread
			
			if (timerDelay != null) {					//si estas usando el Delay es diferente de null
				if(timerDelay.isRunning()) {
					timerDelay.stop(); 					
				}				
			}			
			if(!schd.isShutdown()) {
				schd.shutdown();
			}			
		}
//----------------
		
//----------------		
		if(e.getSource()==CONFIGUREbtn) {		
			EtqHrIni.setText("");						//borro etiqueta tiempo fin
			EtqHrFin.setText("");						//borro etiqueta tiempo inicio			
			EtqTimeON.setText("");						//borro etiqueta tiempo alto
			EtqTimeOFF.setText("");						//borro etiqueta tiempo bajo
			
			configuracion=new ConfiguracionGPIO(nameUserAndPin);			//cada vez que generas este objeto dameBanConig() se hace true		
			banCiclos=true;													//esta es la bandera del thread
			cicloTiempoON=configuracion.dameEncendido();
			cicloTiempoOFF=configuracion.dameApagado();
			numeroDeCiclos=configuracion.dameCiclos();
			
			if(configuracion.dameBanConfig()==true) {
				STARTbtn.setEnabled(true);
				DELAYbtn.setEnabled(true);
				ABORTbtn.setEnabled(false);			
				EtqTimeON.setText(configuracion.dameCadena1());
				EtqTimeOFF.setText(configuracion.dameCadena2());	
			}
		}
//----------------
		
//----------------
		if(e.getSource()==LIGHTbtn) {	
			if(banEncApaLed1==true) {			
				PILOTObtn.setBackground(Color.WHITE);
				gpio.GPIOhigh(pin);
			}
			else{
				PILOTObtn.setBackground(Color.BLACK);
				gpio.GPIOlow(pin);
			}
			banEncApaLed1=!banEncApaLed1;			
		}
//----------------	
		
//----------------				
		if(e.getSource()==STARTbtn) {
			STARTbtn.setEnabled(false);
			DELAYbtn.setEnabled(false);
			ABORTbtn.setEnabled(true);				
			CONFIGUREbtn.setEnabled(false);				
			LIGHTbtn.setEnabled(false);			
			EtqHrIni.setText(" START TIME: "+ new Hora().dameHora() + "                           ");			
			cicloNumero=0;
			cadenaCuentaCiclos=String.format(patron,cicloNumero);
			EtqHrFin.setText(cadenaCuentaCiclos);		
			STARTbtn.setBackground(Color.GREEN);
			
			schd = Executors.newScheduledThreadPool(1);
			schd.execute(this);
		}
//----------------	
		
//----------------		
		if(e.getSource()==DELAYbtn) {			
			ventanaDelay=new VentanaDelayGPIO();					
			if(ventanaDelay.dameBandera()==true) {		
				EtqHrIni.setText(" START TIME: "+ ventanaDelay.dameCadenaDelay() + "                           ");
				timerDelay=new Timer(1000,this); 		//cada segundo hago una comparacion			
				STARTbtn.setEnabled(false);
				DELAYbtn.setEnabled(false);
				ABORTbtn.setEnabled(true);				
				CONFIGUREbtn.setEnabled(false);				
				LIGHTbtn.setEnabled(true);				//puedes prender y apagar el led mientras esperas
				DELAYbtn.setBackground(Color.GREEN);	//el Delay esta corriendo
				timerDelay.start();			
			}
		}		
		if(e.getSource()==timerDelay) {
			if(new Hora().dameHora().equals(ventanaDelay.dameCadenaDelay())) {					
				timerDelay.stop();			
				LIGHTbtn.setEnabled(false);					
				cicloNumero=0;
				cadenaCuentaCiclos=String.format(patron,cicloNumero);
				EtqHrFin.setText(cadenaCuentaCiclos);			

				schd = Executors.newScheduledThreadPool(1);
				schd.execute(this);
			}
		}		
	}
	
//----------------

//----------------
	public void run() {
		gpio.GPIOlow(pin);	
		PILOTObtn.setBackground(Color.BLACK);
					
		for(cicloNumero=1; cicloNumero<=numeroDeCiclos; cicloNumero++) { 			
			if(banCiclos==false) {
				break;
			}		
			PILOTObtn.setBackground(Color.WHITE);
			try {
					gpio.GPIOhigh(pin);
					Thread.sleep(cicloTiempoON);					
				} catch (InterruptedException e) {						
					e.printStackTrace();
				}	
				PILOTObtn.setBackground(Color.BLACK);
			try {
					gpio.GPIOlow(pin);
					Thread.sleep(cicloTiempoOFF);
				} catch (InterruptedException e) {
						e.printStackTrace();
				}			
			cadenaCuentaCiclos=String.format(patron,cicloNumero);
			EtqHrFin.setText(cadenaCuentaCiclos);
		}		
		EtqHrFin.setText("");		
		banCiclos=false;		
		if(cicloNumero-1==numeroDeCiclos) {
			EtqHrFin.setText("     END TIME: "+ new Hora().dameHora() + ("                             "));
		}
		if(!schd.isShutdown()) {
			schd.shutdown();
		}
		STARTbtn.setEnabled(false);
		DELAYbtn.setEnabled(false);
		ABORTbtn.setEnabled(false);				
		CONFIGUREbtn.setEnabled(true);				
		LIGHTbtn.setEnabled(true);
		STARTbtn.setBackground(null);
		DELAYbtn.setBackground(null);
		PILOTObtn.setBackground(Color.BLACK);	
		gpio.GPIOlow(pin);
	}
//----------------		
}
//lllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll

//Uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu
class ConfiguracionGPIO{

	private JTextField jTextField[]= new JTextField[7];	//hrsON, minON, segON, hrsOFF, minOFF,segOFF, ciclos
	private int valores[]=new int[7];	
	private int i;
	private boolean banConfig=true;
	private JLabel ENCENDIDOetq=new JLabel("LIGHT ON");
	private JLabel APAGADOetq=new JLabel("LIGHT OFF");
	private JLabel CICLOSetq= new JLabel("CYCLES");
	String patron1 = "               LIGHT ON   %02d:%02d:%02d                 ";
	String patron2 = "                LIGHT OFF  %02d:%02d:%02d   CYCLES %04d    ";
	String Cadena1;
	String Cadena2;
	
	public ConfiguracionGPIO( NameUserAndPin nameUserAndPin){			
		for(i=0; i<7; i++) {
			jTextField[i]=new JTextField();
		}		
		Object[] message = {
				"   ",
				ENCENDIDOetq,			
				"hours", jTextField[0],
			    "minutes" , jTextField[1],
			    "seconds", jTextField[2],
			    "   ",
			    APAGADOetq,						//APAGADO es la etiqueta del letrero "LIGHT OFF"			    
			    "hours",jTextField[3],
			    "minutes",jTextField[4],
			    "seconds",jTextField[5],
			    "   ",			    
			    CICLOSetq,jTextField[6],		//NumDeCiclos es la etiqueta del letrero "CYCLES"	     
			    "  ",
			};

		int option = JOptionPane.showConfirmDialog(null, message, nameUserAndPin.getNameUser() + " " + nameUserAndPin.getNameGPIO() + " ", JOptionPane.OK_CANCEL_OPTION);
				
		if (option == JOptionPane.OK_OPTION ){
			for(i=0; i<7; i++) {
				try {
					valores[i]=Integer.valueOf(jTextField[i].getText());
				}catch (Exception e){
					jTextField[i].setText("");
					}					
				}
								
			Cadena1=String.format(patron1, valores[0], valores[1], valores[2]);
			Cadena2=String.format(patron2, valores[3], valores[4], valores[5], valores[6]);
		
			if(valores[6]<=0) {
				error("the cycles field can not be zero");
			}			
			if(valores[0]+valores[1]+valores[2]<=0) {
				error("the light time off can not be zero");
			}		
			if(valores[3]+valores[4]+valores[5]<=0) {
				error("the light time on can not be zero");
			}				
		}else {
			error("canceled dialog box");
		}	
	}	
	public int dameEncendido(){		
		return (valores[0]*3600 + valores[1]*60 + valores[2])*1000;		//todo sale en mseg
	}	
	public int dameApagado(){
		return (valores[3]*3600 + valores[4]*60 + valores[5])*1000;			//todo sale en mseg
	}
	public int dameCiclos(){
		return valores[6];
	}	
	public void error(String cadenaError) {
		JOptionPane.showMessageDialog(null, cadenaError,"ERROR",JOptionPane.ERROR_MESSAGE);
		banConfig=false;
	}	
	public boolean dameBanConfig() {
		return banConfig;
	}
	public String dameCadena1() {
		return Cadena1;
	}
	public String dameCadena2() {
		return Cadena2;
	}
}
//Uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu

//yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy
class NameUserAndPin{
	
	private JTextField field1;			//nombre
	private JLabel NomEtq=new JLabel("NAME");
	private String nameUser;
	private int pin;

	public NameUserAndPin(int pin) {	
		this.pin=pin;
		field1 = new JTextField();
		Object[] message = {NomEtq,field1};
		JOptionPane.showMessageDialog(null, message, "YOUR NAME", JOptionPane.QUESTION_MESSAGE);
		
		if(field1.getText().isEmpty()==true){
			field1.setText("ANONYMOUS");
			nameUser=field1.getText();			
		}else {
			nameUser=field1.getText();
		}	
	}
	public String getNameUser() {
		return nameUser;
	}
	public String getNameGPIO() {
		return "GPIO_" + (pin+1);
	}
}
//yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy

//wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww
class Hora{
	
	private Calendar now;
	private String formato="%02d:%02d:%02d";
	private String HoraCadena;

	public Hora() {				
		now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		HoraCadena=String.format(formato,hour, minute, second);
	}	
	public String dameHora() {
		return HoraCadena;
	}	
}
//wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww

//ppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp
class VentanaDelayGPIO {
	
	private JTextField campoHora=new JTextField();
	private JTextField campoMinuto=new JTextField();
	private JTextField campoSegundo=new JTextField();	
	private int delayHora;
	private int delayMinuto;
	private int delaySegundo;
	private String cadenaDelay;
	private String formato="%02d:%02d:%02d";
	private Font font=new Font("Courier", Font.BOLD, 20);
	private JLabel etqHoraCadena =new JLabel();
	private boolean banVentaDelay;
		
	public VentanaDelayGPIO() {
		
		banVentaDelay=false;
		
		Object[] message = {			
				"hours", campoHora,
			    "minutes" , campoMinuto,
			    "seconds", campoSegundo,
			    "   ",
			};
		
		int option = JOptionPane.showConfirmDialog(null, message, "DELAY (24 hour time)", JOptionPane.OK_CANCEL_OPTION);
		
		if (option == JOptionPane.OK_OPTION ) {
			
			if(campoHora.getText().isEmpty()==true){campoHora.setText("0");}	
			if(campoMinuto.getText().isEmpty()==true){campoMinuto.setText("0");}
			if(campoSegundo.getText().isEmpty()==true){campoSegundo.setText("0");}	
			
			delayHora= Integer.valueOf(campoHora.getText());
			delayMinuto= Integer.valueOf(campoMinuto.getText());
			delaySegundo= Integer.valueOf(campoSegundo.getText());
			
			mensajeConfirmacion();			
		}		
	}	
	public void mensajeConfirmacion() {		
		cadenaDelay=String.format(formato, delayHora, delayMinuto, delaySegundo);		
		etqHoraCadena.setFont(font);
		etqHoraCadena.setText(cadenaDelay);		
		
		int option=JOptionPane.showConfirmDialog(null, etqHoraCadena, "CONFIRM",JOptionPane.OK_CANCEL_OPTION);

		if (option!= JOptionPane.OK_OPTION ) {
			new VentanaDelayGPIO();
		}
		if(option == JOptionPane.OK_OPTION) {
			banVentaDelay=true;			//esta bandera se hace cierta hasta que confirmas la eleccion
		}
	}	
	public String dameCadenaDelay() {
		return cadenaDelay;
	}	
	public boolean dameBandera() {
		return banVentaDelay;
	}
}
//ppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp

//tttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt
class GPIO{
	
	private GpioController gpioController;	
	private GpioPinDigitalOutput [] gpioPinDigitalOutput= new GpioPinDigitalOutput[3];
	private boolean banderaSO;	
	private SendString sendString;
	
	public GPIO(boolean banderaSO) {
		this.banderaSO=banderaSO;
		gpioController = GpioFactory.getInstance();
		gpioPinDigitalOutput[0]=gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_01, "01", PinState.LOW);
		gpioPinDigitalOutput[1]=gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_02, "02", PinState.LOW);
		gpioPinDigitalOutput[2]=gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_03, "03", PinState.LOW);
	}
	public GPIO(boolean banderaSO, String j) {	
		this.banderaSO=banderaSO;
		sendString=new SendString(new LookingPortsConfigure().getSelectedPort());
	}
	public void OffGpioController() {
		if(banderaSO) {
			gpioController.shutdown();
		}else {
			//System.out.println("no hay nada que apagar");
		}
	}
	public void GPIOhigh(int i) {
		if(banderaSO==true) {
			gpioPinDigitalOutput[i].high();
		}else {
			sendString.stringSend(i+" HIGH" + "\n");
		}			
	}
	public void GPIOlow(int i) {
		if(banderaSO==true) {
			gpioPinDigitalOutput[i].low();
		}else {
			sendString.stringSend(i+" LOW" + "\n");
		}	
	}
}
//tttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt

//oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
class LookingPortsConfigure{
	
	private SerialPort[] serialPortArray;	
	private String[] dataPort;	
	private Object selectedPortObject;	
	private SerialPort serialPort;
	private String selection;

	public LookingPortsConfigure() {	
		serialPortArray= SerialPort.getCommPorts();			
		dataPort = new String[serialPortArray.length];
				
		if(serialPortArray.length==0) {
			JOptionPane.showMessageDialog(null, "No busy comm port", "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}		
		for (int i = 0; i < serialPortArray.length; ++i) {
			dataPort[i]=i + "  "+
			serialPortArray[i].getSystemPortName()+ "  " +
			serialPortArray[i].getDescriptivePortName()+ "  " +
			serialPortArray[i].getPortDescription();
		}		
		selectedPortObject = JOptionPane.showInputDialog(null,"Choose port", "PORTS", JOptionPane.QUESTION_MESSAGE, null, dataPort,"Seleccione");				
		if(selectedPortObject==null){
			JOptionPane.showMessageDialog(null, "You did not select port", "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}else {
			selection=selectedPortObject.toString().substring(0,1);			
			serialPort=SerialPort.getCommPort(serialPortArray[Integer.parseInt(selection)].getSystemPortName());		
			serialPort.setComPortParameters(9600, 8, 1, 0);		//port configuration
			serialPort.openPort();	
		}
	}		
	public SerialPort getSelectedPort() {
		return serialPort;
	}
}
//oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

//pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp
class SendString {
	
	private SerialPort serialPort;
	private int stringLegth;
	private byte[] newData;

	public SendString (SerialPort serialPort) {
		this.serialPort=serialPort;
	}		
	public void stringSend(String stringToSend)  {		
		stringLegth=stringToSend.length();		
		newData = new byte[stringLegth];	
		newData=stringToSend.getBytes(StandardCharsets.ISO_8859_1);
		serialPort.writeBytes(newData, stringLegth);
	}
}
//pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp

//555555555555555555555555555555555555555555555555555555555555555555555555555555555555
class SistemaOperativo{
	private boolean banderaSO;
	public SistemaOperativo() {
		if(System.getProperty("os.name").equals("Linux")) {
			banderaSO=true;		//Linux
		}else {
			banderaSO=false;	//Windows
		}
	}	
	public boolean getBanderaSO() {
		return banderaSO;
	}
}
//555555555555555555555555555555555555555555555555555555555555555555555555555555555555



