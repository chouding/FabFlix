import java.io.IOException;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpSession;

@WebServlet("/movie-suggestion")
public class MovieSuggestion extends HttpServlet {
    /*
     *
     * Match the query against superheroes and return a JSON response.
     *
     * For example, if the query is "star":
     * The JSON response look like this:
     * [
     * 	{ "value": "Star Wars (2022)", "data": { "movieId": 421 } },
     * 	{ "value": "Star Trek (2023)", "data": { "movieId": 298 } },
     *  ...
     * ]
     *
     * The format is like this because it can be directly used by the
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     *
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession();
            session.setAttribute("resultUrl", request.getParameter("url")+request.getQueryString());

            // setup the response json arrray
            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String query = request.getParameter("query");

            // return the empty json array if query is null or empty
            if (query == null || query.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }

            // Tokenize query
            String[] tokens = query.split("\\s+");

            // SQL query for full-text search
            String sqlQuery ="SELECT id, title, year FROM movies WHERE MATCH (title) AGAINST ('";
            for (int i = 0; i < tokens.length; i++) {
                if (i < tokens.length-1) {
                    sqlQuery += '+' + tokens[i] + "*" + " ";
                }
                else {
                    sqlQuery += '+' + tokens[i] + "*' IN BOOLEAN MODE)";
                }
            }
            sqlQuery += " ORDER BY title LIMIT 10;";

            System.out.println(sqlQuery);

            // Establish connection to database and apply query
            Connection conn = null;
            String jdbcURL="jdbc:mysql://localhost:3306/moviedb";
            try {
                conn = DriverManager.getConnection(jdbcURL,"mytestuser",
                        "My6$Password");
            } catch (SQLException e) {
                e.printStackTrace();
                throw e;
            }

            try (PreparedStatement queryDB = conn.prepareStatement(sqlQuery)) {

                // Declare our statement
                conn.setAutoCommit(false);
                ResultSet rs = queryDB.executeQuery();
                while (rs.next()) {
                    JsonObject jsonObject = generateJsonObject(rs.getString("id"),
                            rs.getString("title"), Integer.parseInt(rs.getString("year")));
                    jsonArray.add(jsonObject);
                }
                rs.close();
            }

            response.getWriter().write(jsonArray.toString());
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        }
    }

    /*
     * Generate the JSON Object from movie to be like this format:
     * {
     *   "value": "Movie Name",
     *   "data": { "movieId": 7 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movieId, String movieTitle, Integer movieYear) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieTitle + " (" + movieYear + ")");

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieId", movieId);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }


}
