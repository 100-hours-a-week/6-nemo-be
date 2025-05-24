package kr.ai.nemo.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ai.nemo.auth.exception.AuthErrorCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String ERROR_RESPONSE_TEMPLATE = "{\"code\": %d, \"message\": \"%s\", \"data\": null}";
  private static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";

  private final JwtProvider jwtProvider;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {

    String authHeader = request.getHeader(AUTHORIZATION_HEADER);

    if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
      String token = authHeader.substring(BEARER_PREFIX.length());
      try {
        jwtProvider.validateToken(token);
        Long userId = jwtProvider.getUserIdFromToken(token);
        UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(userId, null, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (ExpiredJwtException e) {
        setErrorResponse(response, AuthErrorCode.EXPIRED_TOKEN.getHttpStatus().value(), AuthErrorCode.EXPIRED_TOKEN.getMessage());
        return;
      } catch (JwtException | IllegalArgumentException e) {
        setErrorResponse(response, AuthErrorCode.INVALID_TOKEN.getHttpStatus().value(), AuthErrorCode.INVALID_TOKEN.getMessage());
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private void setErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
    response.setStatus(status);
    response.setContentType(APPLICATION_JSON_UTF8);

    String json = String.format(
        ERROR_RESPONSE_TEMPLATE, status, message
    );

    response.getWriter().write(json);
  }
}
