package preset.publisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;
import org.w3c.dom.Document;

public class Publisher {
	 
	private static Logger log = Logger.getLogger(Publisher.class.getName());
	
	private static FileHandler fh;  
	
	public static boolean recursive = false;
	
	private static String CONFIG_NAME = null;
	private static String CONFIG_ROOT = null;
	private static String CONFIG_VALIDATION = null;
	private static long CONFIG_WAIT;
	
	
	/* 
	 * get and set's for private constants
	 * 
	*/
	public static void setConfigName ( String configName) {
		CONFIG_NAME = configName;
	}
	
	public static String getConfigName() {
		return CONFIG_NAME;
	}
	
	public static void setConfigRoot ( String configRoot) {
		CONFIG_ROOT = configRoot;
	}
	
	public static String getConfigRoot() {
		return CONFIG_ROOT;
	}
	
	public static void setConfigValidation ( String configValidation) {
		CONFIG_VALIDATION = configValidation;
	}
	
	public static long getConfigWait() {
		return CONFIG_WAIT;
	}
	public static void setConfigWait ( long configWait) {
		CONFIG_WAIT = configWait;
	}
	
	public static String getConfigValidation() {
		return CONFIG_VALIDATION;
	}
	
	public static void logMessage ( Level logLevel, String message) {
		 
		log.log(logLevel, message);
	 
	}
	 
	public static void main(String[] args)  {
	    
		//get startup arguments
		Path dir = appArgs (args);
        
		//setup logging
		logStart( dir.toString() );
		
		//get some configuration settings from config.xml...
		appConfig();
		
		// start watch
		try {
		
			new Watch(dir).processEvents();
		
		} catch(IOException eIO) {
			
			logMessage (Level.SEVERE, "Can not start watch: " + dir.toString());
		}
	 
	}

	
	
	/*	
	 *  startLog
	 *  
	 *  Configuration settings from app/path/config.xml...
	 *  
	*/
	private static void appConfig() {
		
		try {
			
			String appPath = new File(".").getCanonicalPath();
			
			Document doc = XmlHelper.getDocument( appPath + "/config.xml" );
			
			//set class variables
			CONFIG_NAME 		= XmlHelper.nodeValue(doc, "name" );
			CONFIG_ROOT 		= XmlHelper.nodeValue(doc, "root");
			CONFIG_VALIDATION 	= appPath + "/" + XmlHelper.nodeValue(doc, "validate");
			CONFIG_WAIT			= Long.parseLong(XmlHelper.nodeValue(doc, "wait")) ;
			
		} catch (IOException eIO) {
			
			System.err.println("usage: java preset.publish [-R] dir");
		    System.exit(0);
		
		}
	}
	
	
	
	/*	
	 *  appArgs ( String starup arguments array )
	 *  
	 *  creates an xml file in app path / log  for logging messages
	 *  
	*/
	private static Path appArgs(String[] args) {
		
		// make sure got two arguments (boolean -R recursive, string watch directory path)
		if (args.length == 0 || args.length > 2) {
            usage();
		}
        
		int dirArg = 0;
		
		// use'm arguments
		if (args[0].equals("-R")) {
			
           if (args.length < 2) {
               usage();
           }
           
           recursive = true;
           dirArg++;
		}
		
		// register directory and process its events
		Path dir = Paths.get(args[dirArg]);
		return dir;
		
	}
	
	static void usage() {
		
		System.err.println("usage: java preset.publish [-R] dir");
	    System.exit(0);
	 
	}
	
	
	/*	
	 *  startLog
	 *  
	 *  creates an xml file in app path / log  for logging messages
	 *  
	*/
	private static void logStart(String directory) {
		
		try {
		
			//setup datetime for file name..
			DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			Calendar calobj = Calendar.getInstance();
			
			//setup log file + formatter
			fh = new FileHandler(logDir() + "/preset-publish." + df.format(calobj.getTime()) + ".xml");  
			log.addHandler(fh);
			XMLFormatter formatter = new XMLFormatter();  
			fh.setFormatter(formatter);  
		
			// log start...
			logMessage(Level.INFO, "Publisher started for directory: " + directory);
	        			
		} catch (IOException eIO) {
			
			 System.out.println("Can not create log file in directory: " + directory + ".");
			 
			 System.exit(0); 
		
		}
	
	}

	private static String logDir()  {
		
		String logPath = null;
		
	    try {
			//get application path	    	
	    	logPath = new File(".").getCanonicalPath() + "/log";

	    	//get log directory 
			File lDir = new File(logPath);
			
			//directory exists?
	    	lDir.mkdirs();
	    	
	    } catch(SecurityException se) {
	    	 
	    	 System.out.println("Can not create log directory in application / log.");
			 System.exit(0); 
	    
	    } catch (IOException eIO) {
				
			System.out.println("Can not create log files in application path / log.");
			System.exit(0);
	
	    } 
	    
	    return logPath;
	    
	}
}
