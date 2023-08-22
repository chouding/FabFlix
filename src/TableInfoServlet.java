import com.google.gson.JsonArray;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "TableInfoServlet", urlPatterns = "/_dashboard/api/table-info")
public class TableInfoServlet extends HttpServlet {
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
            String table_name = request.getParameter("table-name");

            String query = "SELECT DISTINCT COLUMN_NAME column_name, DATA_TYPE type\n" +
                    "FROM INFORMATION_SCHEMA.COLUMNS\n" +
                    "WHERE TABLE_NAME = ?;";


            JsonArray table_name_arr = easyCommunicate.getData2(query,
                    new String[]{"column_name", "type"}, new String[]{"column_name", "type"},
                    new Object[] {table_name}, new String[] {"str"});

            EasyCommunicate.writeToResponse(request, response, table_name_arr.toString());
        } catch (Exception e) {
            EasyCommunicate.handleError(request, response, e);
        }
    }
}
