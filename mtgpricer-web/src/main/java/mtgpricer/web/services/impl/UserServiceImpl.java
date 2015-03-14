package mtgpricer.web.services.impl;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import mtgpricer.web.services.PasswordChangeResult;
import mtgpricer.web.services.UserService;

@Service
public class UserServiceImpl implements UserService {
	private final DataSource dataSource;
	private final PasswordEncoder passwordEncoder;
	
	@Autowired
	public UserServiceImpl(DataSource dataSource, PasswordEncoder passwordEncoder) {
		assert dataSource != null;
		assert passwordEncoder != null;
		this.dataSource = dataSource;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Override
	public PasswordChangeResult changePassword(
			String username,
			String currentPassword, 
			String newPassword, 
			String confirmPassword) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(username));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(currentPassword));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(newPassword));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(confirmPassword));
		
		if (!newPassword.equals(confirmPassword)) {
			return new PasswordChangeResult(false, "New password and existing password do not match.");
		}
		
		final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		final String dbPassword = jdbcTemplate.queryForObject(
				"SELECT password FROM mtgpricer.users WHERE username = ?", 
				new Object[]{username}, 
				String.class);
		
		if (!passwordEncoder.matches(currentPassword, dbPassword)) {
			return new PasswordChangeResult(false, "Password given doesn't match current password.");
		}
		
		final String encodedPassword = passwordEncoder.encode(newPassword);
		final int rows = jdbcTemplate.update(
				"UPDATE mtgpricer.users SET password = ? WHERE username = ?", 
				new Object[]{encodedPassword, username}, 
				new int[]{Types.VARCHAR, Types.VARCHAR});
		
		if (rows == 1) {
			return new PasswordChangeResult(true, "Password changed successfully.");
		} else {
			return new PasswordChangeResult(false, "Unable to change the password at this time.");
		}
	}
}
