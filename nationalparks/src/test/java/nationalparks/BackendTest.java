package nationalparks;

import org.junit.Test;

import com.openshift.evg.roadshow.rest.gateway.model.Backend;

import junit.framework.TestCase;

public class BackendTest extends TestCase {

	public BackendTest(String name) {
		super(name);
	}
	
	@Test
	public void testBackend() throws Exception {
		
		Backend testb = new Backend();
		assertEquals(testb.getCenter().getLatitude(),"0");
		assertEquals(testb.getZoom(),1);
		assertEquals(testb.getId(),null);
		
	}
	
}
