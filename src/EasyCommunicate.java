import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * Note: This class automatically assume that the data source is "java:comp/env/jdbc/moviedb"
 *       Will change the class later if this is no longer the case
 */
public class EasyCommunicate {
    private DataSource dataSource;
    public static JsonArray getData(String sqlQuery, String[] attributeFromSelect)
            throws SQLException, IndexOutOfBoundsException, NamingException {
        return getData(sqlQuery, attributeFromSelect, attributeFromSelect);
    }

    public EasyCommunicate(){
        // define data source
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * sqlQuery = SQL string query that is used to get data from the moviedb
     * attributesInSelect = all sql attributes name inside SELECT (for ex: "name", "birthYear", "movies", "movie_ids")
     * propertiesInJson = all json object properties (for ex: "star_name", "star_dob", "star_movies", "movie_ids")
     *
     * Note: propertiesInJson[i] will be associated with attributesInSelect[i]
     * Note: If propertiesInJson in json is not specified, will run the method above
     *       and assume all properties' name will be the same as attribute name
     */
    public static JsonArray getData(String sqlQuery,
                                    String[] attributesInSelect, String[] propertiesInJson)
                                    throws SQLException, IndexOutOfBoundsException, NamingException {
        if (attributesInSelect.length != propertiesInJson.length){
            throw new IndexOutOfBoundsException("attributesInSelect and propertiesInJson need to have the same length");
        }
        
        // define data source
        DataSource dataSource;
        try {
             dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
            throw e;
        }

        // get data from sql and return JsonArray
        try (Connection conn = dataSource.getConnection(); PreparedStatement queryDB = conn.prepareStatement(sqlQuery)) {

            // Declare our statement
            conn.setAutoCommit(false);
            ResultSet rs = queryDB.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                int i = 0;
                while (i < attributesInSelect.length){
                    String nameSelect = attributesInSelect[i];
                    String nameJson = propertiesInJson[i];
                    jsonObject.addProperty(nameJson,  rs.getString(nameSelect));
                    i++;
                }
                jsonArray.add(jsonObject);
            }

            rs.close();

            return jsonArray;
        }
    }


    public static void postData(String sqlStatement)
            throws SQLException, IndexOutOfBoundsException, NamingException {

        // define data source
        DataSource dataSource;
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
            throw e;
        }

        // get data from sql and return JsonArray
        try (Connection conn = dataSource.getConnection();  PreparedStatement preparedStatement = conn.prepareStatement(sqlStatement)) {
            // Declare our statement
            conn.setAutoCommit(false);
            preparedStatement.executeUpdate();
            conn.commit();
        }
    }

    public static String callStoredProperty(String sqlStatement, Object[] all_data, String[] types) throws NamingException, SQLException {

        // define data source
        DataSource dataSource;
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
            throw e;
        }

        // get data from sql and return JsonArray
        try (Connection conn = dataSource.getConnection(); CallableStatement preparedStatement= conn.prepareCall(sqlStatement) ) {
            // Declare our statement
            int n = all_data.length;
            for (int i = 1; i <= n; i++){
                String type = types[i-1];
                Object data = all_data[i-1];
                switch (type) {
                    case "str":
                        preparedStatement.setString(i, (String) data);
                        break;
                    case "int":
                        preparedStatement.setInt(i, (Integer) data);
                        break;
                    case "float":
                        preparedStatement.setFloat(i, (Float) data);
                        break;
                    case "date":
                        String dateString = (String) data;
                        preparedStatement.setDate(i, Date.valueOf(dateString));
                        break;
                }
                preparedStatement.registerOutParameter(n + 1, java.sql.Types.VARCHAR);
            }
            System.out.println(preparedStatement.toString());
            conn.setAutoCommit(false);
            preparedStatement.execute();
            conn.commit();
            try {
                return preparedStatement.getString(n + 1);
            }
            catch (SQLException e){
                return e.getMessage();
            }
        }
        catch (SQLException e){
            throw e;
        }
    }



    public static void writeToResponse(HttpServletRequest request, HttpServletResponse response, String msg)
        throws IOException
    {
        try (PrintWriter out = response.getWriter()) {
            out.write(msg);
            request.getServletContext().log("Message: \"" + msg + "\" successfully sent!");
            response.setStatus(200);
        }

    }

    public static void handleError(HttpServletRequest request, HttpServletResponse response, Exception e){
        try(PrintWriter out = response.getWriter()) {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } catch (IOException ignore){}
    }

    public JsonArray getData2(String sqlQuery,
                                    String[] attributesInSelect, String[] propertiesInJson,
                                     Object[] all_data, String[] types)
            throws SQLException, IndexOutOfBoundsException, NamingException {
        if (attributesInSelect.length != propertiesInJson.length){
            throw new IndexOutOfBoundsException("attributesInSelect and propertiesInJson need to have the same length");
        }

        // get data from sql and return JsonArray
        try (Connection conn = dataSource.getConnection(); PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery)) {
            int n = all_data.length;
            for (int i = 1; i <= n; i++){
                String type = types[i-1];
                Object data = all_data[i-1];
                switch (type) {
                    case "str":
                        preparedStatement.setString(i, (String) data);
                        break;
                    case "int":
                        preparedStatement.setInt(i, (Integer) data);
                        break;
                    case "float":
                        preparedStatement.setFloat(i, (Float) data);
                        break;
                    case "date":
                        String dateString = (String) data;
                        preparedStatement.setDate(i, Date.valueOf(dateString));
                        break;
                }
            }
            // Declare our statement
            conn.setAutoCommit(false);
            ResultSet rs = preparedStatement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                int i = 0;
                while (i < attributesInSelect.length){

                    String nameSelect = attributesInSelect[i];
                    String nameJson = propertiesInJson[i];

                    jsonObject.addProperty(nameJson,  rs.getString(nameSelect));
                    i++;
                }
                jsonArray.add(jsonObject);
            }

            rs.close();

            return jsonArray;
        }
    }
    public JsonArray getSearchData(String sqlQuery,
                                     String[] attributesInSelect, String[] propertiesInJson,
                                     Object[] all_data, String[] types,
                                     String strFile, HttpServletRequest request)
            throws SQLException, IndexOutOfBoundsException, NamingException {

        request.getServletContext().log("got into getSearchData");
        if (attributesInSelect.length != propertiesInJson.length){
            throw new IndexOutOfBoundsException("attributesInSelect and propertiesInJson need to have the same length");
        }

        // get data from sql and return JsonArray
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery);
             FileOutputStream file = new FileOutputStream(strFile, true)) {
            request.getServletContext().log("Manage to established all connections");
            int n = all_data.length;
            for (int i = 1; i <= n; i++){
                String type = types[i-1];
                Object data = all_data[i-1];
                switch (type) {
                    case "str":
                        preparedStatement.setString(i, (String) data);
                        break;
                    case "int":
                        preparedStatement.setInt(i, (Integer) data);
                        break;
                    case "float":
                        preparedStatement.setFloat(i, (Float) data);
                        break;
                    case "date":
                        String dateString = (String) data;
                        preparedStatement.setDate(i, Date.valueOf(dateString));
                        break;
                }
            }
            // Declare our statement
            request.getServletContext().log("Before SQL exec");
            conn.setAutoCommit(false);
            long startTime = System.nanoTime();
            ResultSet rs = preparedStatement.executeQuery();
            request.getServletContext().log("After SQL exec");
            long endTime = System.nanoTime();

            // write time to TJ1 file
            String time = Long.toString(endTime - startTime) + '\n';
            file.write(time.getBytes());
            file.flush();
            request.getServletContext().log("Wrote to TJ");

            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                int i = 0;
                while (i < attributesInSelect.length){

                    String nameSelect = attributesInSelect[i];
                    String nameJson = propertiesInJson[i];

                    jsonObject.addProperty(nameJson,  rs.getString(nameSelect));
                    i++;
                }
                jsonArray.add(jsonObject);
            }
            rs.close();
            return jsonArray;
        } catch (FileNotFoundException e) {
            request.getServletContext().log("File not found");
            throw new RuntimeException(e);
        } catch (IOException e) {
            request.getServletContext().log("IO Exception");
            throw new RuntimeException(e);
        }
    }




    public void postData2(String sqlStatement, Object[] all_data, String[] types)
            throws SQLException, IndexOutOfBoundsException, NamingException {


        // get data from sql and return JsonArray
        try (Connection conn = dataSource.getConnection();  PreparedStatement preparedStatement = conn.prepareStatement(sqlStatement)) {
            // Declare our statement
            int n = all_data.length;
            for (int i = 1; i <= n; i++){
                String type = types[i-1];
                Object data = all_data[i-1];
                switch (type) {
                    case "str":
                        preparedStatement.setString(i, (String) data);
                        break;
                    case "int":
                        preparedStatement.setInt(i, (Integer) data);
                        break;
                    case "float":
                        preparedStatement.setFloat(i, (Float) data);
                        break;
                    case "date":
                        String dateString = (String) data;
                        preparedStatement.setDate(i, Date.valueOf(dateString));
                        break;
                }
            }
            conn.setAutoCommit(false);
            preparedStatement.executeUpdate();
            conn.commit();
        }
    }
}
