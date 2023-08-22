import com.google.gson.JsonArray;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;


// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
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

        // Remember `Result` movie list
        HttpSession session = request.getSession();
        JsonObject resultUrlObj = new JsonObject();
        resultUrlObj.addProperty("resultUrl", (String) session.getAttribute("resultUrl"));

        // Follow this format to use Easy Communication
        try {
            response.setContentType("application/json"); // Response mime type
            String id = request.getParameter("id");
            request.getServletContext().log("getting id: " + id);
            String sqlQuery = "SELECT s.name, IFNULL(s.birthYear, 'N/A') AS birthYear, " +
                            "GROUP_CONCAT(m.title ORDER BY m.year DESC, title SEPARATOR ' , ') AS movies, " +
                            "GROUP_CONCAT(m.id ORDER BY m.year DESC, m.title SEPARATOR ' , ') AS movie_ids " +
                            "FROM stars AS s, stars_in_movies AS sim, movies AS m " +
                            "WHERE s.id = sim.starId AND sim.movieId = m.id AND s.id =  ? " +
                            "GROUP BY s.name, s.birthYear";

            JsonArray jsonArray = easyCommunicate.getData2(sqlQuery,
                                    new String[] { "name", "birthYear", "movies", "movie_ids" },
                                    new String[] { "star_name", "star_dob", "star_movies", "movie_ids" },
                                    new Object[]{id}, new String[] {"str"});
            jsonArray.add(resultUrlObj);
            EasyCommunicate.writeToResponse(request, response, jsonArray.toString());

        } catch (Exception e) {
            EasyCommunicate.handleError(request, response, e);
        }

    }

}
