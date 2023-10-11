package br.com.georgeneves.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.georgeneves.todolist.user.IUserRepository;
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        //Pegar a autenticação (Usuarioe Senha)
        var authorization = request.getHeader("Authorization");

        var authEncoded = authorization.substring("Basic".length()).trim();

        byte[] authDecoded =  Base64.getDecoder().decode(authEncoded);

        var authString = new String(authDecoded);

        String[] credentials = authString.split(":");
        String username = credentials[0];
        String password = credentials[1];

        // Validar se o usuário existe

        var user = this.userRepository.findByUsername(username);
        if(user == null){
            response.sendError(401);
        } else {
            //Validar senha
            var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
            if (passwordVerify.verified) {
                filterChain.doFilter(request,response);
            } else{
                response.sendError(401);
            }
        }
    }
}
