package com.jay.practice.spring.security.config;

import com.jay.practice.spring.security.filter.StopwatchFilter;
import com.jay.practice.spring.security.filter.TesterAuthenticationFilter;
import com.jay.practice.spring.security.jwt.JwtAuthenticationFilter;
import com.jay.practice.spring.security.jwt.JwtAuthorizationFilter;
import com.jay.practice.spring.security.jwt.JwtProperties;
import com.jay.practice.spring.security.user.User;
import com.jay.practice.spring.security.user.UserRepository;
import com.jay.practice.spring.security.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security 설정 Config
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;

    private final UserRepository userRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // stopwath filter
        http.addFilterBefore(
                new StopwatchFilter(),
                WebAsyncManagerIntegrationFilter.class
        );
        // tester authentication filter
//        http.addFilterBefore(
//                new TesterAuthenticationFilter(this.authenticationManager()),
//                UsernamePasswordAuthenticationFilter.class
//        );

        // basic authentication
        http.httpBasic().disable(); // basic authentication filter 비활성화
//        http.httpBasic(); // basic authentication filter 활성화
        // csrf / 인증 받은 페이지인지 확인할 수 있는 토큰 / Thymeleaf에서 자동으로 제공해준다.
        http.csrf();
        // remember-me / 기본 설정은 2주 - 장시간 유지되는 remember-me cookie 가 생성된다. / 서버를 재시작할 경우 쿠키는 유실된다.
        http.rememberMe();

        // stateless
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // jwt filter
        http.addFilterBefore(
                new JwtAuthenticationFilter(authenticationManager()),
                UsernamePasswordAuthenticationFilter.class
        ).addFilterBefore(
                new JwtAuthorizationFilter(userRepository),
                BasicAuthenticationFilter.class
        );

        // anonymous - 인증되지 않은 사용자에 대해 익명의 토큰을 발급
//        http.anonymous();
        // authorization
        http.authorizeRequests()
                // /와 /home은 모두에게 허용
                .antMatchers("/", "/home", "/signup").permitAll()
                // hello 페이지는 USER 롤을 가진 유저에게만 허용
                .antMatchers("/note").hasRole("USER")
                .antMatchers("/admin").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/notice").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/notice").hasRole("ADMIN")
                .anyRequest().authenticated();
        // login
        http.formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .permitAll(); // 모두 허용
        // logout
        http.logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies(JwtProperties.COOKIE_NAME);
    }

    @Override
    public void configure(WebSecurity web) {
        // 정적 리소스 spring security 대상에서 제외
//        web.ignoring().antMatchers("/images/**", "/css/**"); // 아래 코드와 같은 코드입니다.
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());

//        아래 내용은 현재 코드와 유사하나 filter 자체를 통과하나 안하냐의 차이이므로 성능에서 차이가 있다.
//        http.authorizeRequests()
//                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
    }

    /**
     * UserDetailsService 구현
     *
     * @return UserDetailsService
     */
    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException(username);
            }
            return user;
        };
    }
}
