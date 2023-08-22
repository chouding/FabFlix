import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/_dashboard/api/add-movie")
public class AddMovieServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movie_name = request.getParameter("movie-name");
        String movie_year = request.getParameter("movie-year");
        String movie_director = request.getParameter("movie-director");

        String star_name = request.getParameter("star-name");
        String genre_name = request.getParameter("genre-name");

        if (movie_year.equals("")){
            movie_year = "NULl";
        }



        try {
            JsonObject json_obj = new JsonObject();
            String query = "CALL add_movie(?, ?, ?, ?, ?, ?)";

            String msg = EasyCommunicate.callStoredProperty(query,
                    new Object[]{movie_name, Integer.parseInt(movie_year), movie_director, star_name, genre_name},
                    new String[]{"str", "int", "str", "str", "str"});

            json_obj.addProperty("message", msg);

            EasyCommunicate.writeToResponse(request, response, json_obj.toString());
        } catch (Exception e) {
            EasyCommunicate.handleError(request, response, e);
        }
    }
}
