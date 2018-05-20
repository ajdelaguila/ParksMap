package mlbparks;

import org.junit.Test;

import com.openshift.evg.roadshow.model.Coordinates;
import com.openshift.evg.roadshow.model.MLBPark;

import junit.framework.TestCase;

public class MLBParkTest extends TestCase{

    public MLBParkTest(String name) {
        super( name );
    }
    
    @Test
    public void testMLBPark1() throws Exception{
    	MLBPark testmlb = new MLBPark();
    	assertEquals( testmlb.getId(), null);
    	testmlb.setBallpark("Wrigley Field");
    	testmlb.setId(12345);
    	assertEquals( testmlb.getBallpark(), "Wrigley Field");
    	assertEquals( testmlb.getId(), 12345);
    	
    }
    
    @Test
    public void testMLBPark2() throws Exception{
    	MLBPark testmlb = new MLBPark();
    	assertEquals( testmlb.getName(), null);
    	testmlb.setPosition(new Coordinates("12345","6789"));
    	testmlb.setName("New Name");
    	assertEquals( testmlb.getPosition().getLatitude(), "12345");
    	assertEquals( testmlb.getName(), "New Name");
    	
    }
	
}

