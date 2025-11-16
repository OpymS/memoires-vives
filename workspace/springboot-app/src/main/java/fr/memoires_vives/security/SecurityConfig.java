package fr.memoires_vives.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${security.remember-me.key}")
	private String rememberMeKey;

	public SecurityConfig() {
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, PersistentTokenRepository tokenRepository,
			CustomUserDetailsService userDetailsService, CustomAuthenticationFailureHandler failureHandler)
			throws Exception {
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/home", "/css/**", "/js/**", "/images/public/**", "/uploads/**", "/login",
						"/signup", "/logout", "/error", "/error/**", "/memory", "/about", "/try", "/api/memory/**",
						"/upload-error", "/forgot-password/**", "/legal-notices", "/privacy-policy", "/conditions")
				.permitAll().requestMatchers("/admin/**").hasRole("ADMIN").anyRequest().authenticated())
				.formLogin(login -> login.loginPage("/login").permitAll().defaultSuccessUrl("/", true)
						.failureHandler(failureHandler))
				.logout(logout -> logout.permitAll().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
						.invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID")
						.logoutSuccessUrl("/"))
				.httpBasic(Customizer.withDefaults()).csrf(csrf -> csrf.ignoringRequestMatchers("/images/public/**"))
				.with(new RememberMeConfigurer<>(),
						rememberMe -> rememberMe.tokenRepository(tokenRepository).userDetailsService(userDetailsService)
								.rememberMeParameter("remember-me").key(rememberMeKey).alwaysRemember(false)
								.tokenValiditySeconds(14 * 24 * 60 * 60));
		return http.build();
	}

	@Bean
	DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService) {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
		JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
		tokenRepository.setDataSource(dataSource);
		return tokenRepository;
	}

}
