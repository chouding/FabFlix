import com.google.gson.JsonArray;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "GenresServlet", urlPatterns = "/api/genres")
public class GenresServlet extends HttpServlet {
    private static final long serialVersionUID = 101L;
    private EasyCommunicate easyCommunicate;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        easyCommunicate = new EasyCommunicate();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response){
        try {
            response.setContentType("application/json"); // Response mime type

            String query = "SELECT DISTINCT name\n" +
                    "FROM genres;";

            JsonArray jsonArray = easyCommunicate.getData2(query,
                    new String[]{"name"}, new String[]{"name"}, new Object[]{}, new String[]{});

            EasyCommunicate.writeToResponse(request, response, jsonArray.toString());

        } catch (Exception e) {
            EasyCommunicate.handleError(request, response, e);
        }
    }
    
}