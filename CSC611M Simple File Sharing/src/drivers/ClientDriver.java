package drivers;

import indexer.Index;
import indexer.Resolution;

import java.io.IOException;

import dropos.Config;
import dropos.DropClient;

public class ClientDriver {
	public static void main(String[] args) throws IOException {
		
		// When the application is exited properly (clicking the button to exit, or closing the window), the index is updated and written down.
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	        	Index startUp = Index.startUp();
	    		Index now = Index.directory();
	    		
	    		Resolution resolution = Resolution.compare(startUp, now);
	    		if (resolution.countChanges() > 0)
	    			System.out.println(resolution);
	    		else
	    			System.out.println("[Client] There were no changes on the directory.");
	        	now.write();
	    		
	    		
	        }
	    }, "Shutdown-thread"));
		
		Config.initialize();
		DropClient c = new DropClient();
		c.run();
		
		
	}
}
