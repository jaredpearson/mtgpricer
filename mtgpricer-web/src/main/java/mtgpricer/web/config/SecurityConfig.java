package mtgpricer.web.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebMvcSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers("/").permitAll()
				.antMatchers("/cards/**").permitAll()
				.antMatchers("/search/**").permitAll()
				.antMatchers("/sets/**").permitAll()
				.antMatchers("/css/**").permitAll()
				.antMatchers("/js/**").permitAll()
				.antMatchers("/manage/**").hasRole("ADMIN")
				.antMatchers("/api/**").hasRole("ADMIN")
				.anyRequest().authenticated()
				.and()
			.logout()
				.logoutUrl("/logout")
				.logoutSuccessUrl("/")
				.permitAll()
				.and()
			.formLogin()
				.permitAll()
				.and()
			.csrf()
				.disable();
	}
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth, DataSource dataSource) throws Exception {
		auth.jdbcAuthentication()
			.passwordEncoder(passwordEncoder())
			.dataSource(dataSource)
			.rolePrefix("ROLE_")
			.usersByUsernameQuery("SELECT username, password, true as enabled FROM mtgpricer.users WHERE username=?")
			.authoritiesByUsernameQuery("SELECT u.username, a.authorization_name as role FROM mtgpricer.user_authorizations a, mtgpricer.users u WHERE u.user_id = a.user_id AND u.username=?");
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}
}
