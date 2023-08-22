import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private EasyCommunicate easyCommunicate;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        easyCommunicate = new EasyCommunicate();
    }


    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get attributes from request
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        // Initialize response object
        JsonObject responseJsonObject = new JsonObject();

        // Check recaptcha
        if (gRecaptchaResponse != null){
            try {
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            }
            catch (Exception e) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Recaptcha Verification Error");
                response.getWriter().write(responseJsonObject.toString());
                return;
            }
        }

        String sqlQuery = "SELECT * FROM customers WHERE email = ?";

        // Validate email and password
        try {
            /*
            Check database for inputted email and password
            */
          
            JsonArray jsonArray = easyCommunicate.getData2(
                    sqlQuery,
                    new String[] {
                            "id",
                            "firstName",
                            "lastName",
                            "ccId",
                            "address",
                            "email",
                            "password"
                    },
                    new String[] {
                            "user_id",
                            "first_name",
                            "last_name",
                            "cc_id",
                            "address",
                            "email",
                            "password"
                    }, new Object[]{email}, new String[]{"str"});

            // Validate email and password
            boolean validLogin = false;
            if (jsonArray.size() > 0) {
                String encryptedPassword = jsonArray.get(0).getAsJsonObject().get("password").getAsString();
                validLogin = VerifyPassword.verifyCredentials(email, password, encryptedPassword);
            }

            if (validLogin)
            {
                // Login success:

                // set this user into the session
                String id = jsonArray.get(0).getAsJsonObject().get("user_id").getAsString();
                request.getSession().setAttribute("user", new User(email));
                request.getSession().setAttribute("user_id", id);
                request.getSession().setAttribute("user-type", "normal");


                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

            } else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Login failed");
                // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                if (jsonArray.isEmpty()) {
                    responseJsonObject.addProperty("message", "Email " + email + " doesn't exist.");
                } else {
                    responseJsonObject.addProperty("message", "Incorrect password.");
                }
            }
        } catch (Exception e) {
            EasyCommunicate.handleError(request, response, e);
        } finally {
            response.getWriter().write(responseJsonObject.toString());
        }
    }
}
