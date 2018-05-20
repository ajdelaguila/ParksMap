/*
 * To Run Test with specified URL's use the following:
 * mvn clean verify -DargLine="-Dnationalparks.base.url=<URL1> -Dmlbparks.base.url=<URL2> -Dparksmap.base.url=<URL3>"
 */

package parksmap;

import org.junit.Test;
import org.junit.experimental.categories.Category;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import junit.framework.TestCase;

@Category(IntegrationTest.class)
public class EndpointsTest extends TestCase {

	private String parksurl = (System.getProperty("parksmap.base.url") == null) ? "http://parksmap-parksmap-project-example.apps.xiki-mitzi.acumena-os.com" : System.getProperty("parksmap.base.url");
	private String naturl = (System.getProperty("nationalparks.base.url") == null) ? "http://nationalparks-parksmap-project-example.apps.xiki-mitzi.acumena-os.com" : System.getProperty("nationalparks.base.url");
	private String mlburl = (System.getProperty("mlbparks.base.url") == null) ? "http://mlbparks-parksmap-project-example.apps.xiki-mitzi.acumena-os.com" : System.getProperty("mlbparks.base.url");
	private String healthpath = "/ws/healthz/";
	
	@Test
	public void testMLBEndpoint() throws Exception {
		 System.out.println(mlburl);
		 
		  String data = new RestTemplate()
		                          .getForObject(
		                          mlburl + healthpath,
		                          String.class );
		  
		  System.out.println(data);
		  assertEquals(data, "OK");		
	}

	@Test
	public void testNatParkEndpoint() throws Exception {

		  String data = new RestTemplate()
		                          .getForObject(
		                          naturl + healthpath,
		                          String.class );
		  
		  System.out.println(data);
		  assertEquals(data, "OK");		
	}

	@Test
	public void testParksMapEndpoint() throws Exception {

		  String data = new RestTemplate()
		                          .getForObject(
		                          parksurl + healthpath,
		                          String.class );
		  
		  System.out.println(data);
		  assertEquals(data, "OK");		
	}
	
}
