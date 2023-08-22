import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();
    private final ArrayList<String> employeeURIs = new ArrayList<>();
    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null) {
            if (httpRequest.getRequestURI().contains("employee")){
                httpResponse.sendRedirect("employee-login.html");
            }
            else {
                httpResponse.sendRedirect("login.html");
            }
        } else {
            System.out.println(httpRequest.getSession().getAttribute("user-type"));
            if (httpRequest.getSession().getAttribute("user-type").equals("normal")){
                if (isUrlOnlyForEmployee(httpRequest.getRequestURI())){
                    System.out.println("not allowed");
                    httpResponse.sendRedirect("login.html");
                }
                else{
                    String uri = httpRequest.getRequestURI();
                    System.out.println(uri);
                    System.out.println(uri.contains("search"));
                    if (uri.contains("/api/search")){
                        long startTime = System.nanoTime();
                        chain.doFilter(request, response);
                        long endTime = System.nanoTime();
                        String pathName = request.getServletContext().getRealPath("/") + "TS1.txt";
                        request.getServletContext().log("Time ts: " +  Long.toString(endTime - startTime));
                        request.getServletContext().log("Ts path " + pathName);
                        System.out.println(endTime - startTime);
                        try (FileOutputStream file = new FileOutputStream(pathName, true)){
                            String time = Long.toString(endTime - startTime) + '\n';
                            file.write(time.getBytes());
                            file.flush();
                        }
                    }
                    else{
                        chain.doFilter(request, response);
                    }
                }
            }
            else{
                chain.doFilter(request, response);
            }
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    private boolean isUrlOnlyForEmployee(String requestURI){
        return employeeURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }


    public void init(FilterConfig fConfig) {
        initAllowedURIs();
        initEmployeeURIs();
    }

    private void initAllowedURIs(){
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");

        allowedURIs.add("employee-login.html");
        allowedURIs.add("employee-login.js");
        allowedURIs.add("api/employee-login");


        allowedURIs.add("styles/login.css");
        allowedURIs.add("styles/main-app.css");

        allowedURIs.add("styles/index.css");
        allowedURIs.add("styles/result.css");
        allowedURIs.add("styles/shopping.css");
        allowedURIs.add("styles/payment.css");
        allowedURIs.add("styles/payment-complete.css");

        allowedURIs.add("styles/single-movie.css");
        allowedURIs.add("styles/single-star.css");
    }

    private void initEmployeeURIs(){
        employeeURIs.add("_dashboard/employee-dashboard.html");
        employeeURIs.add("_dashboard/employee-dashboard.js");
        employeeURIs.add("_dashboard/api/table-names");
        employeeURIs.add("_dashboard/api/table-info");

        employeeURIs.add("_dashboard/add-movie.html");
        employeeURIs.add("_dashboard/add-movie.js");
        allowedURIs.add("_dashboard/api/add-movie");

        employeeURIs.add("_dashboard/add-star.html");
        employeeURIs.add("_dashboard/add-star.js");
        allowedURIs.add("_dashboard/api/add-star");
    }



    public void destroy() {
        // ignored.
    }

}
