
public class Search {
	public static void main(String[] args) {
		StringBuilder query = new StringBuilder();
		for (String arg : args) {
			query.append(arg + " ");
		}
		
		try {
			Database db = new Database("database.properties");
			System.out.println(db.search(query.toString()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
