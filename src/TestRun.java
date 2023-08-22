import javax.naming.NamingException;
import java.sql.SQLException;

public class TestRun {
    public static void main(String[] args) throws SQLException, NamingException {
        String current = System.getProperty("user.dir");
        System.out.println(current);
        new XMLMovieParser().ParseXMLMovie();
    }
}
