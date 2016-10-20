package jenkinsMonitor;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Jenkins {

	public static boolean checkRunning(String host, int port, int timeout) {
	    try (Socket socket = new Socket()) {
	        socket.connect(new InetSocketAddress(host, port), timeout);
	        return true;
	    } catch (IOException e) {
	        return false; // Either timeout or unreachable or failed DNS lookup.
	    }
	}
	
	
	public static long getBusyExecutors(String host, int port, String username, String password) {
		
		JSONParser parser = new JSONParser();
		long busyExecutors = 0;
		
		try {         
            URL jsonURL = new URL("http://" + host + ":" + port + "/api/json?depth=1"); // URL to Parse
            URLConnection yc = jsonURL.openConnection();
            String header = "Basic " + new String(DatatypeConverter.parseBase64Binary(username + ":" + password));
            yc.addRequestProperty("Authorization", header);
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {    
            	JSONObject output = (JSONObject) parser.parse(inputLine);
            	
            JSONArray assignedLabels = (JSONArray) output.get("assignedLabels");
            JSONObject assignedLabelsObj = (JSONObject) assignedLabels.get(0);
            busyExecutors = (long) assignedLabelsObj.get("busyExecutors");
            
            return busyExecutors;            		
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
        	e.printStackTrace();
        }
		
		return busyExecutors;
	}
	
	
	public static String[] getJenkinsCrumb(String host, int port, String username, String password) {
		
		JSONParser parser = new JSONParser();
		String[] crumb = new String[2];
		
		try {         
            URL jsonURL = new URL("http://" + host + ":" + port + "/crumbIssuer/api/json"); // URL to Parse
            URLConnection yc = jsonURL.openConnection();
            String header = "Basic " + new String(DatatypeConverter.parseBase64Binary(username + ":" + password));
            yc.addRequestProperty("Authorization", header);
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {    
            	JSONObject output = (JSONObject) parser.parse(inputLine);
            	
            crumb[0] = (String) output.get("crumbRequestField");
            crumb[1] = (String) output.get("crumb");
            
            return crumb;            		
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
        	e.printStackTrace();
        }
		
		return crumb;
	}
	
	
	
	public static Date getLatestHit(String host, int port, String username, String password) {
		
		JSONParser parser = new JSONParser();
		
		try {         
            URL jsonURL = new URL("http://" + host + ":" + port + "/monitoring?format=json&period=tout"); // URL to Parse
            URLConnection yc = jsonURL.openConnection();
            String header = "Basic " + new String(DatatypeConverter.parseBase64Binary(username + ":" + password));
            yc.addRequestProperty("Authorization", header);
            InputStreamReader inStream = new InputStreamReader(yc.getInputStream());
            BufferedReader in = new BufferedReader(inStream);
            
            String inputLine;
            String fullLine = "";
            while ((inputLine = in.readLine()) != null) { 
            	fullLine += inputLine;
            	if ( inputLine.contains(("\n"))) {
            		in.close();
            		break;
            	}
            }
            
            JSONObject output = (JSONObject) parser.parse(fullLine);
            	            
        	ArrayList<Date> dates = new ArrayList<Date>();
            JSONArray list = (JSONArray) output.get("list");
            for (Object o : list) {
            	JSONObject out = (JSONObject) o;
            	String lineDate = (String) out.get("startDate");
            	String inputDate = lineDate.split(" ")[0];
            	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            	Date finalDate = dateFormat.parse(inputDate);
            	dates.add(finalDate);
            }
            
                       
            return Collections.max(dates);            		

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
        	e.printStackTrace();
        } catch (java.text.ParseException e) {
			e.printStackTrace();
		}
			
		return null;
	}
	
	
	
public static void restartInstance(String host, int port, String username, String password, String[] crumb) {
				
		try {         
			String url = "http://" + host + ":" + port + "/safeRestart";
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			//add request header
			con.setRequestMethod("POST");
            String header = "Basic " + new String(DatatypeConverter.parseBase64Binary(username + ":" + password));
			con.setRequestProperty("Authorization", header);
			con.setRequestProperty(crumb[0], crumb[1]);

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			System.out.println(con.getResponseCode());
			wr.flush();
			wr.close();
			return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		return;
	}

public static void stopInstance(String host, int port, String username, String password, String[] crumb) {
	
	try {         
		String url = "http://" + host + ":" + port + "/safeExit";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//add request header
		con.setRequestMethod("POST");
        String header = "Basic " + new String(DatatypeConverter.parseBase64Binary(username + ":" + password));
		con.setRequestProperty("Authorization", header);
		con.setRequestProperty(crumb[0], crumb[1]);

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		System.out.println(con.getResponseCode());
		wr.flush();
		wr.close();
		return;
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
	
	return;
}
	
	
	public static void main(String host, int port, String username, String password)
	{
		Authenticator.setDefault (new Authenticator() {
		    protected PasswordAuthentication getPasswordAuthentication() {
		        return new PasswordAuthentication ("admin", "admin".toCharArray());
		    }
		});
		
		
		if (!checkRunning(host, port, 60)) {
			System.out.println(host + ":" + port + " is not running.");
			return;
		}
		
		Date latest = getLatestHit(host, port, username, password);
		String[] crumb = getJenkinsCrumb(host, port, username, password);
		long busyExecutors = getBusyExecutors(host, port, username, password);
		
		Date arbitraryDate = new Date();
		
		if (latest.before(arbitraryDate) && busyExecutors == 0) {
			System.out.println("Shutting down unused Jenkins instance --> " + host + ":" + port);
			stopInstance(host, port, username, password, crumb);
		}
		else {
			System.out.println("Jenkins instance is up and running --> " + host + ":" + port);
		}
	    
	}
	
};

