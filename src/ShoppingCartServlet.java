import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 101L;
    private EasyCommunicate easyCommunicate;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        easyCommunicate = new EasyCommunicate();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String id = request.getParameter("id");
            String amount = request.getParameter("amount");

            response.setContentType("application/json"); // Response mime type
            String query = "SELECT movies.title, pricing as price, ROUND(pricing.pricing * ?, 2) as total_price\n" +
                    "FROM movies, pricing\n" +
                    "WHERE movies.id = ? AND pricing.movie_id = ?;";

            System.out.println(query);
            JsonArray jsonArray = easyCommunicate.getData2(query,
                    new String[]{"title", "price", "total_price"},
                    new String[]{"title", "price", "total_price"},
                    new Object[]{Integer.parseInt(amount), id, id}, new String[] {"int", "str", "str"});

            System.out.println(jsonArray);
            EasyCommunicate.writeToResponse(request, response, jsonArray.toString());

        } catch (Exception e) {
            EasyCommunicate.handleError(request, response, e);
        }
    }

    private String getQuery(HttpServletRequest request){
        String id = request.getParameter("id");
        String amount = request.getParameter("amount");
        return String.format("SELECT movies.title, pricing as price, ROUND(pricing.pricing * %s, 2) as total_price\n" +
                "FROM movies, pricing\n" +
                "WHERE movies.id = '%s' AND pricing.movie_id = '%s';", amount, id, id);
    }

}
