package FASInterfaceMain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jdom.JDOMException;

import FCMP.*;
import fasException.*;
import fasUtil.ConfigUtil;
import FAS.connectionKeeper;

public class FASInterfaceMain {

	public static final Logger FASLogger = Logger.getLogger("FASInterfaceMain");
	public static ConfigUtil configUtil;
	
	private static Thread handShakeThread; // �����߳�
	private static Thread mainThread; // ���߳�
	private static FASInterfaceMachine machine; // FAS�ӿڻ�����
	
	public static void main(String[] args)
	{
		// log4j ������
		PropertyConfigurator.configure("./log4j.properties");
		/////////////////////////////  read config file  ////////////////////////////////
		FASLogger.info("0 **************************************************");
		FASLogger.info("Reading the config file: ./Config/LocalConfig.xml");
		try {
			configUtil = ConfigUtil.getConfigUtil();
		} catch (JDOMException | IOException e) {
			FASLogger.error("Cannot read the config file: ./Config/LocalConfig.xml");
			FASLogger.info("Program exit!");
			FASLogger.debug(e.getMessage(), e);
			return;
		} catch (NoAvailablePortException e) {
			FASLogger.error("No available port error!");
			FASLogger.info("Program exit!");
			FASLogger.debug(e.getMessage(), e);
			return;
		} catch (InvalidConfigException e) {
			FASLogger.error("Invalid configuration items error!");
			FASLogger.info("Program exit!");
			FASLogger.debug(e.getMessage(), e);
			return;
		}
		System.out.println("");
		// �˻��������ʾ������
		String prompt = "\\>";
		
		///////////////////////////// 1 �ӿڻ����󴴽����ʼ��  /////////////////////////////
		FASLogger.info("1 **************************************************");
		FASLogger.info("Building the 'Interface' machine...");
		try {
			machine = new FASInterfaceMachine();
		}catch(FASLocalDeviceInitException e){
			FASLogger.error("FASLocalDeviceInitException");
			FASLogger.info("Sources release ...");
			sourcesRelease();
			FASLogger.info("Program exit!");
			FASLogger.debug(e.getMessage(), e);
			return;
		}catch(FASRemoteDeviceConnException e){
			FASLogger.error("FASRemoteDeviceConnException");
			FASLogger.info("Sources release ...");
			sourcesRelease();
			FASLogger.info("Program exit!");
			FASLogger.debug(e.getMessage(), e);
			return;
		}catch(ConfigFASNodeException e){
			FASLogger.error("ConfigZoneAndDeviceException");
			FASLogger.info("Sources release ...");
			sourcesRelease();
			FASLogger.info("Program exit!");
			FASLogger.debug(e.getMessage(), e);
			return;
		}catch(FCMPInitialException e){
			FASLogger.error("FCMPInitialException");
			FASLogger.info("Sources release ...");
			closeFCMP();
			FASLogger.info("Program exit!");
			FASLogger.debug(e.getMessage(), e);
			return;
		}catch(Exception e){
			FASLogger.error("'Interface' machine building failed!");
			FASLogger.info("Sources release ...");
			sourcesRelease();
			FASLogger.info("Program exit!");
			FASLogger.debug(e.getMessage(), e);
			return;
		}
		FASLogger.info("'Interface' machine building succeed!");
		System.out.println("");
		
		// 2 ���ֽ��̴���������
		FASLogger.info("2 **************************************************");
		FASLogger.info("Starting handshake with Siemens FAS panel...");
		handShakeThread = new Thread(
				new connectionKeeper(machine.getFireAlarmSystem().getFasCommChan().getInterfaceFASDevice(), 
						machine.getFireAlarmSystem().getFasCommChan().getSiemensFASDevice(),
						machine.getFireAlarmSystem().getSiemensFASID())
				,"HandShakeThread");
		try{
			// �������ֽ���
			handShakeThread.start(); 
		}catch(Exception e){
			FASLogger.debug(e.getMessage(), e);
			FASLogger.error("Handshake building failed!");
			FASLogger.info("Sources release ...");
			sourcesRelease();
			FASLogger.info("Program exit!");
			return;
		}
		FASLogger.info("Handshake building succeed!");
		System.out.println("");
		
		// 3 ����������
		FASLogger.info("3 **************************************************");
		FASLogger.info("Starting the 'Interface' machine...");
		mainThread = new Thread(machine, "FASMainThread");
		try{
			// ����������
			mainThread.start();
		}catch(Exception e){
			FASLogger.debug(e.getMessage(), e);
			FASLogger.error("'Interface' machine starting failed!");
			FASLogger.info("Sources release ...");
			sourcesRelease();
			FASLogger.info("Program exit!");
			return;
		}
		FASLogger.info("'Interface' machine starting succeed!");
		System.out.println("");
		
		// HMI �˻�����
		try{
			Scanner reader = new Scanner(System.in);
			String inputCommand;
			printHelp();
			System.out.print(prompt);
			while(true){
				inputCommand = reader.nextLine().trim().toLowerCase();
				if(inputCommand.equals("exit") || inputCommand.equals("quit")){
					FASLogger.info("Sources release ...");
					sourcesRelease();
					// �ر�Scanner
					reader.close();
					System.out.println("Good luck, bye-bye!");
					return;
				}else if(inputCommand.equals("help") || inputCommand.equals("h")){
					printHelp();
					System.out.print(prompt);
				}else if(inputCommand.equals("getfcmpclients") || inputCommand.equals("gc")){
					List<Address> l = new ArrayList<Address>();
					machine.getFCMPChan().getCommChannel().getAliveClt(l);
					if(l.size() == 0){
						System.out.println("No alive FCMP client!");
					}
					else{
						l.forEach((addr)->System.out.println(addr.toString()));
					}
					System.out.print(prompt);
				}else if(inputCommand.equals("getfcmpservers") || inputCommand.equals("gs")){
					List<Address> l = new ArrayList<Address>();
					machine.getFCMPChan().getCommChannel().getAliveServer(l);
					if(l.size() == 0){
						System.out.println("No alive FCMP server!");
					}
					else{
						l.forEach((addr)->System.out.println(addr.toString()));
					}
					System.out.print(prompt);
				}else if(inputCommand.equals("fasconnstatus")){
					System.out.println("Every 25 seconds the program "
							+ "checks the connection with the Siemens FAS panel. ");
					System.out.print(prompt);
				}else if(inputCommand.equals("fasstatus") || inputCommand.equals("fs")){
					machine.getFireAlarmSystem().printFASStatus(true);
					System.out.print(prompt);
				}else{
					System.out.println("Wrong command!");
					printHelp();
					System.out.print(prompt);
				}
			}
		} catch (Exception e) {
			FASLogger.debug(e.getMessage(), e);
		}
	}

	private static void sourcesRelease(){
		FASLogger.info("Releasing sources...");
		// ���������߳�
		closeHandShakeThread();
		// �������߼��߳�
		closeMainThread();
		// �ر�FASͨѶ
		closeFAS();
		// �ر�FCMPͨѶ
		closeFCMP();
	}
	private static void closeHandShakeThread(){
		if(handShakeThread != null){
			if(handShakeThread.isAlive()){
				handShakeThread.stop();
				FASLogger.info(handShakeThread.getName() + " has been closed!");
			}
		}
	}
	private static void closeMainThread(){
		if(mainThread != null){
			if(mainThread.isAlive()){
				mainThread.stop(); 
				FASLogger.info(mainThread.getName() + " has been closed!");
			}
		}}
	private static void closeFAS(){
		if(machine != null){
			if(machine.getFireAlarmSystem() != null){
				if(machine.getFireAlarmSystem().getFasCommChan() != null){
					machine.getFireAlarmSystem().getFasCommChan().closeCommChannel();
					FASLogger.info("Disconnect with the FAS panel!");
				}
			}
		}
	}
	private static void closeFCMP(){
		if(machine != null){
			if(machine.getFCMPChan() != null){
				// �رս��������߳�
				if(machine.getFCMPReceiveThread() != null){
					if(machine.getFCMPReceiveThread().isAlive()){
						machine.getFCMPReceiveThread().stop(); 
						FASLogger.info(machine.getFCMPReceiveThread().getName() + " has been closed!");
					}
				}
				// �ر�ͨѶ
				machine.getFCMPChan().closeCommChannel();
				FASLogger.info("Disconnect with FCMP program!");
			}
		}
	}
	
	private static void printHelp(){
		System.out.println("*** FAS interface with Siemens FS20. ***\n"
				+ "All commands:\n"
				+ "\t FASStatus or fs --- Show the status of zones and devices\n"
				+ "\t FASConnStatus --- A tip of checking the connection status with the Siemens FAS panel\n"
				+ "\t GetFCMPClients or gc --- Show all the FCMP clients' address\n"
				+ "\t GetFCMPServers or gs --- Show all the FCMP servers' address\n"
				+ "\t Exit/Quit --- Exit\n"
				+ "\t Help or h --- Open this help file.\n"
				+ "*** Help end. ***\n");
	}
}
