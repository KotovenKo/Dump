package com.javamentor.qa.platform.webapp.controllers.rest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.javamentor.qa.platform.models.entity.user.User;
import com.javamentor.qa.platform.security.JwtAuthenticationProvider;
import com.javamentor.qa.platform.service.abstracts.model.UserService;
import com.javamentor.qa.platform.webapp.controllers.dto.AuthenticationRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@Api(value = "RestController", description = "Controller to authenticate user with JWT")
@ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success|OK"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not found"),
        @ApiResponse(code = 500, message = "Internal error")
})
@RestController
@RequestMapping("/api")
public class AuthenticationResourceController {

    private final UserService userService;
    private final JwtAuthenticationProvider authenticationProvider;

    public AuthenticationResourceController(UserService userService, JwtAuthenticationProvider provider) {
        this.authenticationProvider = provider;
        this.userService = userService;
    }

    @ApiOperation(value = "Get list of all users ", response = Iterable.class, tags = "users")
    @GetMapping("/getall")
    public List<User> users() throws HttpMessageNotWritableException {
        return userService.getAll();
    }

    @ApiOperation(value = "Generate JWT token and authenticate user",
            response = String.class, tags = "token")
    @PostMapping("/auth/token")
    public String token(@RequestBody AuthenticationRequest request) throws NullPointerException {
        String email = Objects.requireNonNull(request.getEmail());
        String password = Objects.requireNonNull(request.getPassword());
        log.info("Email is " + email);

        SecurityContextHolder.getContext().setAuthentication(authenticationProvider
                .authenticate(new UsernamePasswordAuthenticationToken(email, password)));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();

        return JWT.create().withJWTId(user.getEmail()).withSubject(password)
                .sign(Algorithm.HMAC256("PrinceNanadaime".getBytes()));
    }
}