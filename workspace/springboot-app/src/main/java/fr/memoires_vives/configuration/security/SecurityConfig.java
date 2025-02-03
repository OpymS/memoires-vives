package fr.memoires_vives.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import fr.memoires_vives.bll.UserDetailsServiceInterface;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private final UserDetailsServiceInterface userDetailsServiceInterface;
	
	
	public SecurityConfig(UserDetailsServiceInterface userDetailsServiceInterface) {
		this.userDetailsServiceInterface = userDetailsServiceInterface;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/home", "/css/**", "/js/**", "/login").permitAll()
				.anyRequest().authenticated()
			)
			.formLogin(login -> login
				.loginPage("/login")
				.defaultSuccessUrl("/", true)
				.permitAll()
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/home")
				.permitAll()
			);
		return http.build();
	}
	
	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user = User.builder()
				.username("user")
				.password(passwordEncoder().encode("password"))
				.roles("USER")
				.build();
		
		return new InMemoryUserDetailsManager(user);
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsServiceInterface);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
