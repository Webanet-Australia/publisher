package preset.publisher;

import java.util.logging.Level;
import org.w3c.dom.Document;


public class Transform {
	
	//details of transform from publish file '
	private String ID;
	private String name;
	private String XML;
	private String XSL;
	private String output;
	
	/*
	 * publish() 
	 * 
	 * do transformation
	 * 
	*/
	public void publish( ) { 
		
		// log start of transformation
		Publisher.logMessage(Level.INFO, "Starting tranformation for file " + ID + ".");
		
		//do transformation
		XmlHelper.transform(XSL, XML, output + name);
		
		// log end of transformation
		Publisher.logMessage(Level.INFO, "Finished tranformation for file " + ID + ".");
	
	}

	
	/*
	 * setPublishFile() 
	 * 
	 * validates and reads transformation file into class variables
	 * 
	*/
	public void setPublishFile( String publishFile ) {
		
				
		// validate publish file 
		XmlHelper.validate(Publisher.getConfigValidation(), publishFile);
		
		
		// get Document object
		Document doc = XmlHelper.getDocument( publishFile );
		
		//set class variables
		this.ID 	= XmlHelper.nodeValue( doc, "id" );
		this.name 	= XmlHelper.nodeValue( doc, "name" );
		this.XML 	= XmlHelper.nodeValue( doc, "xml" );
		this.XSL 	= XmlHelper.nodeValue( doc, "xsl" );
		this.output = XmlHelper.nodeValue( doc, "output" );
	
	}  

}

