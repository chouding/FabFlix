//public class SinglePerson {
//    public static void main(String[] args)
//    {
//        System.out.println("testing");
//    }
//
//}

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Remember `Result` movie list
        HttpSession session = request.getSession();
        JsonObject resultUrlObj = new JsonObject();
        resultUrlObj.addProperty("resultUrl", (String) session.getAttribute("resultUrl"));

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"

            // Movie Id, Movie Title, Movie Year, Movie Director, Movie Genres (Sorted Alphabetically)
            String movieGenresQuery = "SELECT m.id, m.title, m.year, m.director, " +
                                      "GROUP_CONCAT(g.name ORDER BY name SEPARATOR ' , ') AS genres \n" +
                                      "FROM movies as m, genres_in_movies as gim, genres as g \n" +
                                      "WHERE m.id = gim.movieId AND gim.genreId = g.id AND m.id = ? \n" +
                                      "GROUP BY m.id, m.title, m.year, m.director \n" +
                                      "ORDER BY m.title";

            // Movie Stars, Movie Rating
            String movieStarsQuery = "SELECT m.id, \n" +
                    "GROUP_CONCAT(s.name ORDER BY movie_count DESC, name ASC SEPARATOR ' , ') as stars, \n" +
                    "GROUP_CONCAT(s.id ORDER BY movie_count DESC, name ASC SEPARATOR ' , ') as star_ids, \n" +
                    "IFNULL(r.rating, 0) as rating\n" +
                    "FROM movies m JOIN stars_in_movies sim\n" +
                    "\t\tON m.id = sim.movieId AND m.id = ?\n" +
                    "\tJOIN stars s \n" +
                    "\t\tON s.id = sim.starId\n" +
                    "    JOIN (SELECT starId, COUNT(*) as movie_count FROM stars_in_movies GROUP BY starId) mc\n" +
                    "\t\tON s.id = mc.starId\n" +
                    "\tLEFT JOIN ratings r\n" +
                    "\t\tON m.id = r.movieId\n" +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating ";

            PreparedStatement statementOne = conn.prepareStatement(movieGenresQuery);
            PreparedStatement statementTwo = conn.prepareStatement(movieStarsQuery);
            statementOne.setString(1, id);
            statementTwo.setString(1, id);

            ResultSet rsOne = statementOne.executeQuery();
            ResultSet rsTwo = statementTwo.executeQuery();

            JsonArray jsonArray = new JsonArray();

            JsonObject jsonObject = new JsonObject();

            if (rsOne.next()) {
                String movieId = rsOne.getString("id");
                String movieTitle = rsOne.getString("title");
                String movieYear = rsOne.getString("year");
                String movieDirector = rsOne.getString("director");
                String movieGenres = rsOne.getString("genres");

                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_genres", movieGenres);
            }

            if (rsTwo.next()) {
                String movieStars = rsTwo.getString("stars");
                String movieStarIds = rsTwo.getString("star_ids");
                String movieRating = rsTwo.getString("rating");

                jsonObject.addProperty("movie_stars", movieStars);
                jsonObject.addProperty("star_ids", movieStarIds);
                jsonObject.addProperty("movie_rating", movieRating);
            }

            jsonArray.add(jsonObject);
            jsonArray.add(resultUrlObj);

            rsOne.close();
            rsTwo.close();
            statementOne.close();
            statementTwo.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}

