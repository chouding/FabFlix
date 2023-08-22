import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AddStarServlet", urlPatterns = "/_dashboard/api/add-star")
public class AddStarServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
         String name = request.getParameter("star-name");
         String year = request.getParameter("birth-year");
         if (year.equals("")){
             year = "NULl";
        }

        try {
            JsonObject json_obj = new JsonObject();
            String query = "CALL add_star(?, ?, ?)";
            String msg = EasyCommunicate.callStoredProperty(query,
                    new Object[]{name, Integer.parseInt(year)},
                    new String[]{"str", "int"});
            json_obj.addProperty("message", msg);

            EasyCommunicate.writeToResponse(request, response, json_obj.toString());
        } catch (Exception e) {
            EasyCommunicate.handleError(request, response, e);
        }
    }
}
