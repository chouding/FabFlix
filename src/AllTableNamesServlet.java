import com.google.gson.JsonArray;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AllTableNamesServlet", urlPatterns = "/_dashboard/api/table-names")
public class AllTableNamesServlet extends HttpServlet {
    private EasyCommunicate easyCommunicate;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        easyCommunicate = new EasyCommunicate();
    }


    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String query = "SELECT table_name AS name\n" +
                    "FROM information_schema.tables\n" +
                    "WHERE table_type='BASE TABLE' AND table_schema = 'moviedb';";

            JsonArray table_name_arr = easyCommunicate.getData2(query,
                    new String[]{"name"}, new String[]{"name"}, new Object[]{}, new String[]{});

            EasyCommunicate.writeToResponse(request, response, table_name_arr.toString());
        } catch (Exception e) {
            EasyCommunicate.handleError(request, response, e);
        }
    }
}
