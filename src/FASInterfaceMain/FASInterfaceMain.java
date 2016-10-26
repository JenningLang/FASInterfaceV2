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
	
	private static Thread handShakeThread; // 握手线程
	private static Thread mainThread; // 主线程
	private static FASInterfaceMachine machine; // FAS接口机对象
	
	private static boolean standAloneFlag = false; // 运行模式，true 单机，false 联网 

	// 人机界面的提示符定义
	private static final String prompt = "\\>";
	
	public static void main(String[] args)
	{
		// log4j 读属性
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
		
		///////////////////////////// 1 接口机对象创建与初始化  /////////////////////////////
		FASLogger.info("1 **************************************************");
		FASLogger.info("Building the 'Interface' machine...");
		machine = new FASInterfaceMachine();

		FASLogger.info("Try to build a connection with the FAS panel...");
		try {
			machine.fireAlarmSystemConfig();
		}catch(FASLocalDeviceInitException e){
			FASLogger.error("FASLocalDeviceInitException");
			FASLogger.info("Sources release ...");
			sourcesRelease();
			FASLogger.info("Program exit!");
			return;
		}catch(FASRemoteDeviceConnException e){
			FASLogger.error("FASRemoteDeviceConnException");
			FASLogger.info("Sources release ...");
			sourcesRelease();
			FASLogger.info("Program exit!");
			return;
		}catch(ConfigFASNodeException e){
			FASLogger.error("ConfigZoneAndDeviceException");
			FASLogger.info("Sources release ...");
			sourcesRelease();
			FASLogger.info("Program exit!");
			return;
		}catch(Exception e){
			FASLogger.error("Failed to build a connection with the FAS panel!");
			FASLogger.info("Sources release ...");
			sourcesRelease();
			FASLogger.info("Program exit!");
			return;
		}
		
		FASLogger.info("Try to build an FCMP appliction...");
		try {
			machine.fcmpConfig();
		}catch(Exception e){
			FASLogger.error("FCMPInitialException");
			FASLogger.info("Program will run in stand-alone mode!");
			standAloneFlag = true;
		}
		FASLogger.info("'Interface' machine building succeed!");
		System.out.println("");
		
		///////////////////////////// 2 握手进程创建与启动  ///////////////////////////// 
		FASLogger.info("2 **************************************************");
		FASLogger.info("Starting handshake with Siemens FAS panel...");
		handShakeThread = new Thread(
				new connectionKeeper(machine.getFireAlarmSystem().getFasCommChan().getInterfaceFASDevice(), 
						machine.getFireAlarmSystem().getFasCommChan().getSiemensFASDevice(),
						machine.getFireAlarmSystem().getSiemensFASID())
				,"HandShakeThread");
		try{
			// 启动握手进程
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
		
		/////////////////////////////  3 启动主进程    /////////////////////////////
		if(standAloneFlag == false){
			FASLogger.info("3 **************************************************");
			FASLogger.info("Starting the 'Interface' machine...");
			mainThread = new Thread(machine, "FASMainThread");
			try{
				// 启动主进程
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
		}
		// HMI 人机交互
		boolean treeFlag = true;
		boolean fullFlag = true;
		try{
			Scanner reader = new Scanner(System.in);
			String inputCommand;
			printHelp();
			System.out.print(prompt);
			while(true){
				inputCommand = reader.nextLine().trim().toLowerCase();
				switch(inputCommand)
				{
					// 退出
					case "exit":
					case "quit":
					case "e":
					case "q":
						FASLogger.info("Sources release ...");
						sourcesRelease();
						// 关闭Scanner
						reader.close();
						System.out.println("Good luck, bye-bye!");
						return;
					// 帮助
					case "help":
					case "h":
						printHelp();
						System.out.print(prompt);
						break;
					// 获取FCMP所有客户端节点地址
					case "getfcmpclients":
					case "gc":
						if(standAloneFlag == false){
							List<Address> cl = new ArrayList<Address>();
							machine.getFCMPChan().getCommChannel().getAliveClt(cl);
							if(cl.size() == 0){
								System.out.println("No alive FCMP client!");
							}
							else{
								cl.forEach((addr)->System.out.println(addr.toString()));
							}
							System.out.print(prompt);
						}else{
							System.out.println("In stand-alone mode!");
							System.out.print(prompt);
						}
						break;
					// 获取FCMP所有服务器节点地址
					case "getfcmpservers":
					case "gs":
						if(standAloneFlag == false){
							List<Address> sl = new ArrayList<Address>();
							machine.getFCMPChan().getCommChannel().getAliveServer(sl);
							if(sl.size() == 0){
								System.out.println("No alive FCMP server!");
							}
							else{
								sl.forEach((addr)->System.out.println(addr.toString()));
							}
							System.out.print(prompt);
						}else{
							System.out.println("In stand-alone mode!");
							System.out.print(prompt);
						}
						break;
					// FAS 状态查询
					case "fasstatus":
					case "fs":
						machine.getFireAlarmSystem().printFASStatus(treeFlag, fullFlag);
						System.out.print(prompt);
						break;
					case "fasstatus -f":
					case "fs -f":
						fullFlag = true;
						machine.getFireAlarmSystem().printFASStatus(treeFlag, fullFlag);
						System.out.print(prompt);
						break;
					case "fasstatus -a":
					case "fs -a":
						fullFlag = false;
						machine.getFireAlarmSystem().printFASStatus(treeFlag, fullFlag);
						System.out.print(prompt);
						break;
					case "":
						System.out.print(prompt);
						break;
					// 错误指令
					default:
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
		// 结束握手线程
		closeHandShakeThread();
		// 结束主逻辑线程
		closeMainThread();
		// 关闭FAS通讯
		closeFAS();
		// 关闭FCMP通讯
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
				// 关闭接受数据线程
				if(machine.getFCMPReceiveThread() != null){
					if(machine.getFCMPReceiveThread().isAlive()){
						machine.getFCMPReceiveThread().stop(); 
						FASLogger.info(machine.getFCMPReceiveThread().getName() + " has been closed!");
					}
				}
				// 关闭通讯
				machine.getFCMPChan().closeCommChannel();
				FASLogger.info("Disconnect with FCMP program!");
			}
		}
	}
	
	private static void printHelp(){
		System.out.println("*** FAS interface with Siemens FS20. ***\n"
				+ "All commands:\n"
				+ "\t FASStatus or fs (-f or -a) --- Show the status of zones and devices\n"
				+ "\t GetFCMPClients or gc --- Show all the FCMP clients' address\n"
				+ "\t GetFCMPServers or gs --- Show all the FCMP servers' address\n"
				+ "\t Exit/Quit --- Exit\n"
				+ "\t Help or h --- Open this help file.\n"
				+ "*** Help end. ***\n");
	}
}
