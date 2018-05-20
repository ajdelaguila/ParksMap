package parksmap;

import org.junit.Test;

import com.openshift.evg.roadshow.rest.gateway.model.DataPoint;

import junit.framework.TestCase;

public class DataPointTest extends TestCase{

    public DataPointTest(String name) {
        super( name );
    }
    
	@Test
    public void testDataPoint() throws Exception{
    	DataPoint testdp = new DataPoint("12345","Test Data Point");
    	assertEquals( testdp.getName(), "Test Data Point");
    	
    }	
	
}
