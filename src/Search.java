import java.io.FileInputStream;
import java.util.Properties;


public class Search {
	public static void main(String[] args) {
		StringBuilder query = new StringBuilder();
		for (String arg : args) {
			query.append(arg + " ");
		}
		
		try {
			Properties props = new Properties();
			FileInputStream in = new FileInputStream("database.properties");
			props.load(in);
			in.close();
			Database db = new Database(props);
			System.out.println(db.search(query.toString()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
