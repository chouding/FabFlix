import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mysql.cj.x.protobuf.MysqlxPrepare;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatConversionException;
import java.util.Random;

public class XMLMovieParser {
    Document dom;

    Document actorsDom;

    Document castsDom;

    HashMap<String, String> xml_movie_info = new HashMap<>();
    HashMap<String, String> star_info = new HashMap<>();
    HashSet<String> star_id = new HashSet<>();
    HashMap<String, String> movie_info = new HashMap<>();
    HashSet<String> movie_id = new HashSet<>();
    HashMap<String, Integer> genre_info = new HashMap<>();
    HashMap<String, String> cat_to_genre = new HashMap<>();
    int max_genre_id = 0;
    int totalGenreRel = 0;
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";


    private void batchParse() throws SQLException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, FileNotFoundException {
        Connection conn = null;
        String jdbcURL="jdbc:mysql://localhost:3306/moviedb";
        try {
            conn = DriverManager.getConnection(jdbcURL,"mytestuser",
                    "My6$Password");
            parseStarInfo(conn);
            parseMovieInfo(conn);
            parseCastInfo(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void parseStarInfo(Connection conn) throws SQLException, FileNotFoundException {
        String sqlQuery = "INSERT INTO stars VALUES (?, ?, ?)";
        try (PreparedStatement queryDb = conn.prepareStatement(sqlQuery);
             PrintWriter star_dup = new PrintWriter("./log/StarDuplicates.txt")) {
            try {
                conn.setAutoCommit(false);
                Element documentElement = actorsDom.getDocumentElement();

                NodeList actors = documentElement.getElementsByTagName("actor");

                int numInserted = actors.getLength();
                int numDup = 0;

                for (int i = 0; i < actors.getLength(); i++){
                    Element element = (Element) actors.item(i);
                    String name;
                    int birthYear;

                    try {
                        name = getTextValue(element, "stagename");
                    } catch (NullPointerException e) {
                        continue;
                    }
                    try {
                        birthYear = getIntValue(element, "dob");
                    } catch (NullPointerException | NumberFormatException e) {
                        birthYear = 0;
                    }

                    // Duplicate star
                    if (star_info.containsKey(name)) {
                        String duplicateStarInfo = String.format("Star id: %s, name: %s, birthYear: %s",
                                null, name, birthYear==0 ? "null" : birthYear);
                        star_dup.println(duplicateStarInfo);
                        numInserted--;
                        numDup++;
                    }

                    // Unique star
                    else {
                        String id = null;
                        while (id == null && !star_id.contains(id)) {
                            StringBuilder new_id = new StringBuilder();
                            Random random = new Random();
                            for (int k = 0; k < 10; k++) {
                                int index = random.nextInt(ALPHA_NUMERIC_STRING.length());
                                new_id.append(ALPHA_NUMERIC_STRING.charAt(index));
                            }
                            id = new_id.toString();
                        }

                        star_id.add(id);
                        star_info.put(name, id);

                        queryDb.setString(1, id);
                        queryDb.setString(2, name);
                        queryDb.setInt(3, birthYear);
                        queryDb.addBatch();
                    }
                }
                queryDb.executeBatch();
                conn.commit();
                System.out.println("Total stars added: " + numInserted);
                System.out.println("Total duplicate stars: " + numDup);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        String id = null;
        while (id == null && !movie_id.contains(id)) {
            StringBuilder new_id = new StringBuilder();
            Random random = new Random();
            for (int k = 0; k < 10; k++) {
                int index = random.nextInt(ALPHA_NUMERIC_STRING.length());
                new_id.append(ALPHA_NUMERIC_STRING.charAt(index));
            }
            id = new_id.toString();
        }


    }

    private void parseMovieInfo(Connection conn) throws SQLException, FileNotFoundException {
        String sqlQuery = "INSERT INTO movies VALUES (?, ?, ?, ?)";
        String sqlRatingStr = "INSERT INTO ratings (movieId, rating, numVotes) VALUES (?, 0.0, 0)";
        String sqlPricingStr = "INSERT INTO pricing VALUE (?, 10);";
        String sqlQuery1 = "INSERT INTO genres (id, name) VALUES (?, ?)";
        String sqlQuery2 = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";
        try (PreparedStatement movie_query = conn.prepareStatement(sqlQuery);
             PreparedStatement query1 = conn.prepareStatement(sqlQuery1);
             PreparedStatement query2 = conn.prepareStatement(sqlQuery2);

             PreparedStatement sql_rating = conn.prepareStatement(sqlRatingStr);
             PreparedStatement sql_pricing = conn.prepareStatement(sqlPricingStr);
             PrintWriter dup = new PrintWriter("./log/MovieDuplicates.txt");
             PrintWriter err = new PrintWriter("./log/MovieInconsistencies.txt")) {

            try {
                conn.setAutoCommit(false);
                Element documentElement = dom.getDocumentElement();

                NodeList directoryFilms = documentElement.getElementsByTagName("directorfilms");
                int total_movie = 0;
                int dup_movie = 0;
                int init_genre = genre_info.size();
                for (int i = 0; i < directoryFilms.getLength(); i++){
                    Element parentElement = (Element) directoryFilms.item(i);
                    Element childElement1 = (Element) parentElement.getElementsByTagName("director").item(0);
                    String dirname = getTextValue(childElement1, "dirname");

                    if (dirname == null){
                        err.println("<director> tag error: no director specified");
                        continue;
                    }
                    Element childElement2 = (Element) parentElement.getElementsByTagName("films").item(0);
                    NodeList filmInfo = childElement2.getElementsByTagName("film");

                    for (int j = 0; j < filmInfo.getLength(); j++){
                        Element film = (Element) filmInfo.item(j);
                        String title;
                        try {
                            title = getTextValue(film, "t");
                            if (title == null){
                                err.println("<t> tag error: no movie title specified");
                                continue;
                            }
                        }
                        catch (NullPointerException e){
                            err.println("<t> tag missing");
                            continue;
                        }


                        int year;
                        try{
                            year = getIntValue(film, "year");
                        }
                        catch (NumberFormatException e){
                            err.println("<year>: year in incorrect format for title=" + title);
                            continue;
                        }


                        String movie_key = title + year + dirname;
                        if (movie_info.containsKey(movie_key)){
                            dup.println("Duplicate Entry For " + title + " by " + dirname + " year " + year);
                            dup_movie += 1;
                        }
                        else {
                            String id = null;
                            while (id == null && !movie_id.contains(id)) {
                                StringBuilder new_id = new StringBuilder();
                                Random random = new Random();
                                for (int k = 0; k < 10; k++) {
                                    int index = random.nextInt(ALPHA_NUMERIC_STRING.length());
                                    new_id.append(ALPHA_NUMERIC_STRING.charAt(index));
                                }
                                id = new_id.toString();
                            }

                            movie_id.add(id);
                            movie_info.put(movie_key, id);

                            String fid = getTextValue(film, "fid");
                            xml_movie_info.put(fid, id);

                            movie_query.setString(1, id);
                            movie_query.setString(2, title);
                            movie_query.setInt(3, year);
                            movie_query.setString(4, dirname);
                            movie_query.addBatch();

                            // add to ratings
                            sql_rating.setString(1, id);
                            sql_rating.addBatch();

                            // add to pricing
                            sql_pricing.setString(1, id);
                            sql_pricing.addBatch();

                            total_movie += 1;
                            parseGenreFromFilm(film, title, id, query1, query2);
                        }
                    }
                }

                System.out.println("Total movie added: " + total_movie);
                System.out.println("Total new genre: " + (genre_info.size() - init_genre));
                System.out.println("Total genres_in_movies added: " + totalGenreRel);
                System.out.println("Total duplicate movies: " + dup_movie);
                movie_query.executeBatch();
                conn.commit();
                query1.executeBatch();
                query2.executeBatch();
                sql_rating.executeBatch();
                sql_pricing.executeBatch();
                conn.commit();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseGenreFromFilm(Element element, String title, String id,
                                    PreparedStatement query1, PreparedStatement query2) throws SQLException, FileNotFoundException {
        try (PrintWriter err = new PrintWriter("./log/GenreInconsistencies.txt")) {
            try {
                NodeList cats = element.getElementsByTagName("cats");
                if (cats.getLength() == 0){
                    err.println("<cats> is empty for " + title);
                    return;
                }
                Element cats_element = (Element) cats.item(0);
                NodeList cat_list = cats_element.getElementsByTagName("cat");
                for (int j = 0; j < cat_list.getLength(); j++) {
                    String genre =  cat_list.item(j).getTextContent().toLowerCase();
                    if (genre.equals("")){
                        err.println("<cat> is empty for " + title);
                        continue;
                    }

                    if (!genre_info.containsKey(genre)){
                        int new_id = max_genre_id + 1;
                        genre_info.put(genre, new_id);
                        max_genre_id += 1;

                        query1.setInt(1, new_id);
                        query1.setString(2, genre);
                        query1.addBatch();
                    }


                    query2.setInt(1, genre_info.get(genre));
                    query2.setString(2, id);
                    query2.addBatch();
                    totalGenreRel += 1;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseCastInfo(Connection conn) throws SQLException, FileNotFoundException {
        String sqlQuery = "INSERT INTO stars_in_movies VALUES(?,?)";
        int numInserted = 0;
        int starsNotFound = 0;
        int movieNotFound = 0;
        int movieEmpty = 0;

        try (PreparedStatement queryDb = conn.prepareStatement(sqlQuery);
             PrintWriter star_not_found = new PrintWriter("./log/StarNotFound.txt");
             PrintWriter movie_not_found = new PrintWriter("./log/MovieNotFound.txt");
             PrintWriter movie_empty = new PrintWriter("./log/MovieEmpty.txt")) {
            try {
                conn.setAutoCommit(false);
                Element documentElement = castsDom.getDocumentElement();

                NodeList directorFilms = documentElement.getElementsByTagName("dirfilms");
                for (int i = 0; i < directorFilms.getLength(); i++) {
                    Element parentElement = (Element) directorFilms.item(i);

                    NodeList filmc = parentElement.getElementsByTagName("filmc");
                    for (int k = 0; k < filmc.getLength(); k++){
                        Element filmcElement = (Element) filmc.item(k);
                        boolean uniqueNotFoundMovie = true;
                        boolean foundActor = false;

                        NodeList movieInfo = filmcElement.getElementsByTagName("m");
                        for (int j = 0; j < movieInfo.getLength(); j++) {
                            Element movie = (Element) movieInfo.item(j);
                            String title;
                            String actor;
                            try {
                                title = getTextValue(movie, "t");
                            } catch (NullPointerException e) {
                                continue;
                            }
                            try {
                                actor = getTextValue(movie, "a");
                            } catch (NullPointerException e) {
                                continue;
                            }

                            String fid = getTextValue(movie, "f");

                            if (star_info.containsKey(actor) && xml_movie_info.containsKey(fid)) {
                                foundActor = true;
                                queryDb.setString(1, star_info.get(actor));
                                queryDb.setString(2, xml_movie_info.get(fid));
                                queryDb.addBatch();
                                numInserted++;
                            }
                            else {
                                if (!(star_info.containsKey(actor))) {
                                    star_not_found.println(fid + " " + actor);
                                    starsNotFound++;
                                }
                                if (!(xml_movie_info.containsKey(fid))) {
                                    if (uniqueNotFoundMovie) {
                                        movie_not_found.println(fid);
                                        movieNotFound++;
                                        uniqueNotFoundMovie = false;
                                    }
                                }
                            }
                        }
                        if (!foundActor) {
                            movie_empty.println(getTextValue( (Element) movieInfo.item(0),"f") );
                            movieEmpty++;
                        }
                    }
                }
                queryDb.executeBatch();
                conn.commit();
                System.out.println("Total stars_in_movies added: " + numInserted);
                System.out.println("Total movies not found: " + movieNotFound);
                System.out.println("Total stars not found: " + starsNotFound);
                System.out.println("Total movies with no stars: " + movieEmpty);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            textVal = nodeList.item(0).getFirstChild().getNodeValue();

        }
        return textVal;
    }

    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        return Integer.parseInt(getTextValue(ele, tagName));
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("./xml/mains243.xml");
            actorsDom = documentBuilder.parse("./xml/actors63.xml");
            castsDom = documentBuilder.parse("./xml/casts124.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void initStarInfo() throws Exception {
        // Establish database connection
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:" + "mysql" + ":///" + "moviedb",
                "mytestuser", "My6$Password");

        try {
            Statement select = connection.createStatement();
            String sqlQuery = "SELECT id, name FROM stars";
            ResultSet result = select.executeQuery(sqlQuery);
            while (result.next()) {
                star_info.put(result.getString("name"),
                        result.getString("id"));
                star_id.add(result.getString("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void initCatGenreHash(){
        cat_to_genre.put("Susp", "thriller");
        cat_to_genre.put("CnR", "cops and robbers");
        cat_to_genre.put("Dram", "drama");
        cat_to_genre.put("West", "western");
        cat_to_genre.put("Myst", "mystery");
        cat_to_genre.put("S.F.", "science fiction");
        cat_to_genre.put("Advt", "adventure");
        cat_to_genre.put("Horr", "horror");
        cat_to_genre.put("Romt", "romantic");
        cat_to_genre.put("Comd", "comedy");
        cat_to_genre.put("Musc", "musical");
        cat_to_genre.put("Docu", "documentary");
        cat_to_genre.put("Porn", "pornography, including soft");
        cat_to_genre.put("Noir", "black");
        cat_to_genre.put("BioP", "biographical Picture");
        cat_to_genre.put("TV", "TV show");
        cat_to_genre.put("TVs", "TV series");
        cat_to_genre.put("TVm", "TV miniseries");
    }

    private void initGenreInfo() throws SQLException, NamingException {
        String query = "SELECT id, name FROM genres;";

        String[] attributesInSelect = new String[] {"id", "name"};

        String[] propertiesInJson = new String[] {"id", "name"};
        JsonArray arr = new JsonArray();

        Connection conn = null;
        String jdbcURL="jdbc:mysql://localhost:3306/moviedb";
        try {
            conn = DriverManager.getConnection(jdbcURL,"mytestuser",
                    "My6$Password");
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        try (PreparedStatement queryDB = conn.prepareStatement(query)) {
            // Declare our statement
            conn.setAutoCommit(false);
            ResultSet rs = queryDB.executeQuery();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                int i = 0;
                while (i < attributesInSelect.length){
                    String nameSelect = attributesInSelect[i];
                    String nameJson = propertiesInJson[i];
                    jsonObject.addProperty(nameJson,  rs.getString(nameSelect));
                    i++;
                }
                arr.add(jsonObject);
            }
            rs.close();
        }

        for (JsonElement item : arr){
            JsonObject obj = item.getAsJsonObject();
            String name = obj.get("name").getAsString();
            int id = obj.get("id").getAsInt();
            genre_info.put(name, id);
            if (id > max_genre_id){
                max_genre_id = id;
            }
        }
    }

    private void initMovieInfo() throws SQLException, NamingException {
        String query = "SELECT * FROM movies;";
        String[] attributesInSelect = new String[] {"title", "year", "director", "id"};
        String[] propertiesInJson = new String[] {"title", "year", "director", "id"};
        JsonArray arr = new JsonArray();

        Connection conn = null;
        String jdbcURL="jdbc:mysql://localhost:3306/moviedb";
        try {
            conn = DriverManager.getConnection(jdbcURL,"mytestuser",
                    "My6$Password");
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        try (PreparedStatement queryDB = conn.prepareStatement(query)) {

            // Declare our statement
            conn.setAutoCommit(false);
            ResultSet rs = queryDB.executeQuery();
            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                int i = 0;
                while (i < attributesInSelect.length){
                    String nameSelect = attributesInSelect[i];
                    String nameJson = propertiesInJson[i];
                    jsonObject.addProperty(nameJson,  rs.getString(nameSelect));
                    i++;
                }
                arr.add(jsonObject);
            }
            rs.close();
        }

        for (JsonElement item: arr){
            JsonObject obj = item.getAsJsonObject();
            String title = obj.get("title").getAsString();
            String year = obj.get("year").getAsString();
            String dir = obj.get("director").getAsString();
            String id = obj.get("id").getAsString();

            movie_info.put(title+year+dir, id);
            movie_id.add(id);
        }
    }

    public void ParseXMLMovie() {
        try {
            final long startTime = System.currentTimeMillis();
            initStarInfo();
            initCatGenreHash();
            initGenreInfo();
            initMovieInfo();
            parseXmlFile();
            batchParse();
            final long endTime = System.currentTimeMillis();
            System.out.println("Total execution time: " + (endTime - startTime));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}

