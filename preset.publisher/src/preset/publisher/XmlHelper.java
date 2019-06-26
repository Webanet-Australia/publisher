package preset.publisher;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlHelper {

	
	
	/*
	 * 	Document getDocument ( file path+name of Document to return )
	 * 
	 *  Return an XML Document object
	*/
	public static Document getDocument ( String xmlFile ) {
		
		Document doc = null;
		
		try {
			//Get document object
			File fXmlFile = new File( xmlFile );
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
		} catch (IOException eIO) {     
			
			Publisher.logMessage(Level.WARNING, "Error reading publishing file: " + eIO.getMessage() + "."); 

		} catch(SAXException eSax) {
	    
			Publisher.logMessage(Level.WARNING, "SAX Exception: " + eSax.getMessage() + ".");
	    
	    } catch (ParserConfigurationException ePCE) {
	    	
	    	Publisher.logMessage(Level.WARNING, "Parser Exception: " + ePCE + ".");
	    
	    }
		
		return doc;
		
	}

	
	
	
	/*
	 * nodeValue( Document object, String Nodes name) 
	 * 
	 * get a node's value
	 * 
	*/
	public static String nodeValue ( Document xDoc, String nodeName ) {
		
		NodeList nList = xDoc.getElementsByTagName(nodeName);
		String value = nList.item(0).getChildNodes().item(0).getTextContent();
		return value;
		
	}
	
	
	
	/*
	 * validate() 
	 * 
	 * Validate transformation file against xsd schema file
	 * 
	*/
	public static void validate( String xsdFile , String xmlFile) {
	      
		try {

			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        Schema schema = factory.newSchema(new File(xsdFile));
	        Validator validator = schema.newValidator();
	        validator.validate(new StreamSource(new File(xmlFile)));
	      
	      } catch (IOException e){    
	      	
	      	Publisher.logMessage(Level.SEVERE, "Exception: " + e.getMessage());
   
	      } catch(SAXException eSax) {
	       
	    	Publisher.logMessage(Level.SEVERE, "SAX Exception: " + eSax.getMessage());

	      }
	      
	}
	
	

	
	/*
	 * tranform() 
	 * 
	 * XML/XSL Tranformation
	 * 
	*/
	public static void transform(String xslFile, String xmlFile, String outputFile)  {
		
		System.setProperty("javax.xml.transform.TransformerFactory",  
                           "net.sf.saxon.TransformerFactoryImpl");  
  
        TransformerFactory tFactory = TransformerFactory.newInstance();  
        
        try {  
        
        	Transformer transformer =  tFactory.newTransformer(new StreamSource(new File(xslFile)));  
  
            transformer.transform(new StreamSource(new File(xmlFile)),  
                                  new StreamResult(new File(outputFile)));  
        } catch (Exception e) {  
            
        	Publisher.logMessage(Level.WARNING, "Can not transform " + xslFile + "/" + xmlFile + "\n" + e.toString());  
        }  	
        
	 }  

}  

