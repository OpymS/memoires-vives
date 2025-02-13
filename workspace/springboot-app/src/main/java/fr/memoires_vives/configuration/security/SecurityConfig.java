package fr.memoires_vives.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import fr.memoires_vives.bll.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	public SecurityConfig() {
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/home", "/css/**", "/js/**", "/images/public/**", "/login", "/signup").permitAll()
				.anyRequest().authenticated()
			)
			.formLogin(login -> login
				.loginPage("/login")
				.permitAll()
				.defaultSuccessUrl("/", true)
				.failureUrl("/login?error=true")
			)
			.logout(logout -> logout
				.permitAll()
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
				.invalidateHttpSession(true)
				.clearAuthentication(true)
				.deleteCookies("JSESSIONID")
				.logoutSuccessUrl("/home")
			)
			.httpBasic(Customizer.withDefaults())
			.csrf(csrf -> csrf
					.ignoringRequestMatchers("/images/public/**"));
		return http.build();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider(UserService userService) {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
