package main.blog.config;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import main.blog.domain.service.CustomUserDetailsService;
import main.blog.filter.JwtAuthFilter;
import main.blog.util.CustomAccessDeniedHandler;
import main.blog.util.CustomAuthenticationEntryPoint;
import main.blog.util.JwtTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import main.blog.filter.ApiAuthFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.formLogin((auth) -> auth.disable());
        http.httpBasic((basic) -> basic.disable());
        http.csrf((auth) -> auth.disable());
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(Arrays.asList("*"));
            configuration.setAllowedMethods(Arrays.asList("*"));
            configuration.setAllowedHeaders(Arrays.asList("*"));
            return configuration;
        }));

        // 세션 고정보호 hacker modified session is protected
        http.sessionManagement((auth) -> auth
                .sessionFixation().changeSessionId()
        );

        // /api 도메인 체크
        //http.addFilterBefore(new ApiAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new JwtAuthFilter(customUserDetailsService, jwtTokenUtil), UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling((execptionHandlling) -> execptionHandlling
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
        );

        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/api/**", "/metrics").permitAll()
                .requestMatchers("/oauth2/callback").permitAll()
                .requestMatchers("/auth/authorize").permitAll()
                .requestMatchers("/Callback").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/info/terms", "/info/privacy").permitAll()

                .requestMatchers("/post/regist", "/post/*/edit").hasAnyRole("ADMIN", "SUPER_ADMIN", "USER")
                .requestMatchers("/", "/images/**","/icon/**", "/css/**", "/js/**", "/post/*", "/error", "/404").permitAll()
                .requestMatchers("/join", "/login","/loginAction", "joinAction").permitAll()
                .requestMatchers("/mypage/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/admin").hasRole("ADMIN")
                //.anyRequest().authenticated()
                .anyRequest().permitAll()
        );

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
