import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "EmployeeLoginServlet", urlPatterns = "/_dashboard/api/employee-login")
public class EmployeeLoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    private EasyCommunicate easyCommunicate;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        easyCommunicate = new EasyCommunicate();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        /*
         Check database for inputted email and password
         */
        JsonObject responseJsonObject = new JsonObject();

        // Check recaptcha
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        }
        catch (Exception e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Recaptcha Verification Error");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        String sqlQuery = "SELECT * FROM employees WHERE email = ?";

        try {
            JsonArray jsonArray = easyCommunicate.getData2(
                    sqlQuery,
                    new String[] {
                            "email",
                            "password",
                            "fullname"
                    }, new String[] {
                            "email",
                            "password",
                            "fullname"
                    }, new Object[] {email}, new String[] {"str"});

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
                request.getSession().setAttribute("user", new User(email));
                request.getSession().setAttribute("user-type", "employee");

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
