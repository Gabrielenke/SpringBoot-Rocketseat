package br.com.gabrielenke.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.gabrielenke.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")) {
            String authorization = request.getHeader("Authorization");

            String authEncoded = authorization.substring("Basic".length()).trim();

            byte[] authDecode = Base64.getDecoder().decode(authEncoded);

            String authDecodedString = new String(authDecode);


//        [user,password] -> [0] = user, [1] = password
            String[] credentials = authDecodedString.split(":");
            String username = credentials[0];
            String password = credentials[1];
            var user = this.userRepository.findByUsername(username);

            if (user == null) {
                response.sendError(401, "User not found");
            } else {
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword().toCharArray());
                if (passwordVerify.verified) {
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);

                } else {
                    response.sendError(401, "Password incorrect");
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }


    }
}
