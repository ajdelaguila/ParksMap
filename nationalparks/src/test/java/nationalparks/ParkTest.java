package nationalparks;

import org.junit.Test;

import com.openshift.evg.roadshow.parks.model.Park;

import junit.framework.TestCase;

public class ParkTest extends TestCase {

	 public ParkTest(String name) {
	        super( name );
	    }
	    
		@Test
	    public void testPark() throws Exception{
	    	Park mypark = new Park();
	    	
	    	assertEquals( mypark.getToponymName(), null);
	    	mypark.setToponymName("testname");
	    	assertEquals( mypark.getToponymName(), "testname");
	    	
	    }
	
}
