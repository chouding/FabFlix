import com.google.gson.JsonArray;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jdi.StringReference;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;


@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 100L;
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
        HttpSession session = request.getSession();
        session.setAttribute("resultUrl", request.getParameter("url")+request.getQueryString());


        // Follow this format to use Easy Communication
        try {
            response.setContentType("application/json"); // Response mime type
            ArrayList<Object> objects = new ArrayList<>();
            ArrayList<String> types = new ArrayList<>();
            String query = getSimMovieSearchQuery(request, objects, types);
            String filepath = request.getServletContext().getRealPath("/") + "TJ1.txt";
            request.getServletContext().log("Tj path after first created using getreal path" + filepath);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("resultUrl", (String) session.getAttribute("resultUrl"));
            JsonArray jsonArray = easyCommunicate.getSearchData(query,
                    new String[]{"id", "title", "year", "director", "rating", "genres", "stars", "star_ids", "num_movies" },
                    new String[]{"movie_id", "title", "year", "director", "rating", "genres", "stars", "star_ids", "num_movies" },
                    objects.toArray(), types.toArray(new String[types.size()]),
                    filepath, request);
            request.getServletContext().log("Successfully run query");


            jsonArray.add(jsonObject);
            EasyCommunicate.writeToResponse(request, response, jsonArray.toString());


        } catch (Exception e) {
            EasyCommunicate.handleError(request, response, e);
        }
    }


    private String getSimMovieSearchQuery(HttpServletRequest request, ArrayList<Object> objects,
                                          ArrayList<String> types){
        int limit = !(request.getParameter("limit").equals("null"))
                ? Integer.parseInt(request.getParameter("limit"))
                : 25;

        int page = !(request.getParameter("page").equals("null"))
                ? Integer.parseInt(request.getParameter("page"))
                : 1;

        int offset = (page-1) * limit;

        return getSimMovieSearchQuery(request, offset, limit, objects, types);
    }

    private String getSimMovieSearchQuery(HttpServletRequest request, int start, int end, ArrayList<Object> objects,
                                          ArrayList<String> types) {
        String year = request.getParameter("year");
        String title = request.getParameter("title");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String genres = request.getParameter("genres");
        String prefix = request.getParameter("prefix");
        String sortBy = request.getParameter("sort");

        String add_conds = "";
        if (!prefix.equals("") && !prefix.equals("null")){
            if (prefix.equals("*")){
                String new_cond = " AND LOWER(title) REGEXP '^[^a-zA-Z1-9].*' ";
                add_conds = add_conds.concat(new_cond);
            }
            else {
                String new_cond = " AND LOWER(title) LIKE ? ";
                add_conds = add_conds.concat(new_cond);

                objects.add(prefix + "%");
                types.add("str");
            }
        }
        else if (!title.equals("") && !title.equals("null")) {
            String new_cond = " AND MATCH(title) AGAINST (";

            String[] tokens = title.split("\\s+");
            for (int i = 0; i < tokens.length; i++) {
                if (i < tokens.length-1) {
                    objects.add("+" + tokens[i] + "* ");
                    new_cond += "? ";
                }
                else {
                    objects.add("+" + tokens[i] + "*");
                    new_cond += "?";
                }
                types.add("str");
            }
            new_cond += " IN BOOLEAN MODE) ";
            add_conds = add_conds.concat(new_cond);
        }

        if (!director.equals("") && !director.equals("null")) {
            String new_cond = " AND LOWER(director) LIKE ? ";
            add_conds = add_conds.concat(new_cond);

            objects.add("%" + director +"%");
            types.add("str");
        }

        if (!year.equals("") && !year.equals("null")) {
            String new_cond = " AND year= ? ";
            add_conds = add_conds.concat(new_cond);

            objects.add(Integer.parseInt(year));
            types.add("int");
        }

        String genres_cond = !genres.equals("") && !genres.equals("null")? " AND LOWER(genres) LIKE ? " : "";
        if (!genres_cond.equals("")){
            objects.add("%" + genres +"%");
            types.add("str");
        }

        // supply start and end
        objects.add(start);
        types.add("int");
        objects.add(end);
        types.add("int");

        String star_cond = !star.equals("") && !star.equals("null")? " AND LOWER(MS.stars) LIKE ? " : "";
        if (!star_cond.equals("")){
            objects.add("%" + star +"%");
            types.add("str");
        }


        // sort conditions is parsed in to getSortCond to select sorting mechanism to prevent sql injection
        String sort_cond = getSortCond(sortBy);


        String query = "WITH movie_genres AS (\n" +
                "SELECT m.id,\n" +
                "\t\tm.title,\n" +
                "\t\tm.year,\n" +
                "\t\tm.director,\n" +
                "\t\tGROUP_CONCAT(g.name ORDER BY name SEPARATOR ' , ') genres\n" +
                "FROM movies m JOIN genres_in_movies gim ON m.id = gim.movieId " + add_conds +
                "\tJOIN genres g ON gim.genreId = g.id\n" +
                "\tGROUP BY m.id, m.title, m.year, m.director\n" +
                "), " +
                "filtered as (" +
                "SELECT id," +
                "\t\ttitle," +
                "\t\tyear," +
                "\t\tdirector," +
                "\t\tgenres," +
                "\t\tCOUNT(*) over () num_movies, IFNULL(r.rating, 0) " +
                "FROM movie_genres LEFT JOIN ratings r ON movie_genres.id = r.movieId " +
                "WHERE 1=1 " + genres_cond + sort_cond + "\nLIMIT ?, ?),\n" +
                "movie_stars as (\n" +
                "SELECT m.id, title, year,director, genres, " +
                "GROUP_CONCAT(s.name ORDER BY movie_count DESC, name ASC SEPARATOR ' , ') as stars, " +
                "GROUP_CONCAT(s.id ORDER BY movie_count DESC, name ASC SEPARATOR ' , ') as star_ids\n" +
                "FROM filtered m join stars_in_movies sim on m.id = sim.movieId join stars as s on s.id = sim.starId JOIN (SELECT starId, COUNT(*) as movie_count FROM stars_in_movies GROUP BY starId) as mc\n" +
                "\t\tON mc.starId = s.id\n" +
                "GROUP BY m.id, m.title, m.year, m.director ORDER BY title" +
                ")\n" +
                "SELECT MG.id, MG.title, MG.year, MG.director, " +
                "\t\tIFNULL(rating, 0) rating, " +
                "\t\tSUBSTRING_INDEX(MG.genres, \" , \", 3) genres, " +
                "\t\tSUBSTRING_INDEX(MS.stars, \" , \", 3) stars, " +
                "\t\tSUBSTRING_INDEX(MS.star_ids, \" , \", 3) star_ids, MG.num_movies\n" +
                "FROM filtered MG JOIN movie_stars MS on MG.id = MS.id " +
                "LEFT JOIN ratings r ON MG.id = r.movieId\n" +
                "WHERE 1=1" + star_cond +
                sort_cond;


        return query;
    }

    private String getSortCond(String sortBy) {
        String result;
        switch (sortBy) {
            case "1":
            default:
                result = "\nORDER BY title, rating DESC";
                break;
            case "2":
                result = "\nORDER BY title, rating";
                break;
            case "3":
                result = "\nORDER BY title DESC, rating DESC";
                break;
            case "4":
                result = "\nORDER BY title DESC, rating";
                break;
            case "5":
                result = "\nORDER BY rating DESC, title";
                break;
            case "6":
                result = "\nORDER BY rating DESC, title DESC";
                break;
            case "7":
                result = "\nORDER BY rating, title";
                break;
            case "8":
                result = "\nORDER BY rating, title DESC";
                break;
        }
        return result;
    }
}
