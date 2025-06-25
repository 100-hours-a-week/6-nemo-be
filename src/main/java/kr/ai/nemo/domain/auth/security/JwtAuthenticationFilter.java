package kr.ai.nemo.domain.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ai.nemo.domain.auth.exception.AuthErrorCode;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.user.exception.UserErrorCode;
import kr.ai.nemo.domain.user.exception.UserException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String ACCESS_TOKEN = "access_token";
  private static final String ERROR_RESPONSE_TEMPLATE = "{\"code\": %d, \"message\": \"%s\", \"data\": null}";
  private static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";

  private final JwtProvider jwtProvider;
  private final CustomUserDetailsService customUserDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {

    String token = null;

    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (ACCESS_TOKEN.equals(cookie.getName())) {
          token = cookie.getValue();
          break;
        }
      }
    }

    if (token != null) {
      try {
        jwtProvider.validateToken(token);
        Long userId = jwtProvider.getUserIdFromToken(token);
        CustomUserDetails userDetails = customUserDetailsService.loadUserById(userId);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

      } catch (ExpiredJwtException e) {
        setErrorResponse(response, AuthErrorCode.EXPIRED_TOKEN.getHttpStatus().value(), AuthErrorCode.EXPIRED_TOKEN.getMessage());
        return;
      } catch (JwtException | IllegalArgumentException e) {
        setErrorResponse(response, AuthErrorCode.INVALID_TOKEN.getHttpStatus().value(), AuthErrorCode.INVALID_TOKEN.getMessage());
        return;
      } catch (UserException e) {
        if (e.getErrorCode() == UserErrorCode.USER_WITHDRAWN) {
          setErrorResponse(response, UserErrorCode.USER_WITHDRAWN.getHttpStatus().value(), UserErrorCode.USER_WITHDRAWN.getMessage());
        } else {
          setErrorResponse(response, UserErrorCode.USER_NOT_FOUND.getHttpStatus().value(), UserErrorCode.USER_NOT_FOUND.getMessage());
        }
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
