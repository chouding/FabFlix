import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 101L;
    private EasyCommunicate easyCommunicate;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        easyCommunicate = new EasyCommunicate();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        try {
            String f_name = request.getParameter("first-name");
            String l_name = request.getParameter("last-name");
            String c_num = request.getParameter("credit-num");
            String exp = request.getParameter("credit-exp");

            response.setContentType("application/json"); // Response mime type
            String verifyQuery = "SELECT EXISTS(\n" +
                    "\tSELECT *\n" +
                    "    FROM creditcards c\n" +
                    "    WHERE c.id= ? AND c.firstName = ? AND \n" +
                    "\t\tc.lastName = ? AND c.expiration= ?) as isIn";
            JsonObject jsonObject = easyCommunicate.getData2(
                    verifyQuery, new String[] {"isIn"}, new String[] {"isIn"},
                    new Object[]{c_num, f_name, l_name, exp}, new String[] {"str", "str", "str", "date"}).get(0).getAsJsonObject();
            JsonObject responseJsonObject = new JsonObject();
            String isIn = jsonObject.get("isIn").toString();

            System.out.println(isIn);
            if (isIn.equals("\"0\"")){
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid credit information");
                EasyCommunicate.writeToResponse(request, response, responseJsonObject.toString());
            }
            else {
                String customerId = request.getSession().getAttribute("user_id").toString();
                String movieId = request.getParameter("movie_id");
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();

                String insert = "INSERT INTO sales VALUES(NULL, ?, ?, ?);";
                int amount = Integer.parseInt(request.getParameter("amount"));
                for (int i = 0; i < amount; i++){
                    System.out.println(dateFormat.format(date));
                    easyCommunicate.postData2(insert, new Object[]{Integer.parseInt(customerId),
                                    movieId, dateFormat.format(date)},
                            new String[]{"int", "str", "date"});
                }
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
                EasyCommunicate.writeToResponse(request, response, responseJsonObject.toString());
            }
        } catch (Exception e) {
            EasyCommunicate.handleError(request, response, e);
        }
    }
}
