package Main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for serialization
 * @author vincent
 *
 */
public class Serializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Serializer.class);
	   
    /**
	    * Helper to serialize idfmodels and typemaps
	    * @param path path to save serialization
	    * @param obj object to serialize
	    */
	    public static void serialize(String path, Object obj){
	    	try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path, false))){
	    		out.writeObject(obj);
	    	} catch (FileNotFoundException e) {
	    		LOGGER.error(e.getStackTrace().toString());
			} catch (IOException e) {
	    		LOGGER.error(e.getStackTrace().toString());
			}
	    	
	    }
	    
	    /**
	     * Helper to deserialize idfmodels and typemaps
	     * @param path path to serialized object
	     */
		public static Object deserialize(String path) {
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))){
				return ois.readObject();
		    } catch (ClassNotFoundException e) {
				LOGGER.error(e.getStackTrace().toString());
			} catch (IOException e) {
				LOGGER.error(e.getStackTrace().toString());
			}
			return null;
		}
}
