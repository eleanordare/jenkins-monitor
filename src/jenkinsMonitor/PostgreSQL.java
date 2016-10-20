package jenkinsMonitor;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class PostgreSQL {

	public static ArrayList<JenkinsInstance> connect() {

		ArrayList<JenkinsInstance> instances = new ArrayList<JenkinsInstance>();
		
		try {
			Class.forName("org.postgresql.Driver");

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		Connection connection = null;
		

		try {
			connection = DriverManager.getConnection(
					"jdbc:postgresql://127.0.0.1:5432/jenkins","root", null);
						
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM data");
			while (rs.next())
			{
			   JenkinsInstance cur = new JenkinsInstance(rs.getString(1), rs.getInt(2), rs.getString(3), rs.getString(4));
			   instances.add(cur);
			} rs.close();
			st.close();

		} catch (SQLException e) {

			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
		}
		
		return instances;
	}

	
	
	
	public static void main(String[] args) {	
		
		ArrayList<JenkinsInstance> instances = connect();
		for (JenkinsInstance j : instances) {
			Jenkins.main(j.host, j.port, j.username, j.password);
		}
		
		return;
	}
	
}
