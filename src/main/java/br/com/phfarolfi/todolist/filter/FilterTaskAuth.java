package br.com.phfarolfi.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.phfarolfi.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")) {
            var authorization = request.getHeader("Authorization");

            var authEncoded = authorization.substring("Basic".length()).trim();

            byte[] authDecode = Base64.getDecoder().decode(authEncoded);

            var authString = new String(authDecode);

            String[] credentials = authString.split(":");

            if (credentials.length < 2) {
                response.sendError(401, "Missing credentials.");
            } else {
                var username = credentials[0];
                var password = credentials[1];

                // Validate username
                var user = this.userRepository.findByUsername(username);

                if (user == null) {
                    response.sendError(401, "Wrong credentials.");
                } else {
                    // Validate password
                    if (BCrypt.verifyer().verify(password.toCharArray(), user.getPassword()).verified) {
                        request.setAttribute("idUser", user.getId());

                        // Send info forward
                        filterChain.doFilter(request, response);
                    } else {
                        response.sendError(401, "Wrong credentials");
                    }
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
