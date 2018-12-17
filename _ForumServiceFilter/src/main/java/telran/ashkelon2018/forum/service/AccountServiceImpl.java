package telran.ashkelon2018.forum.service;

import java.time.LocalDateTime;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import telran.ashkelon2018.forum.configuration.AccountConfiguration;
import telran.ashkelon2018.forum.configuration.AccountUserCredentials;
import telran.ashkelon2018.forum.dao.UserAccountRepository;
import telran.ashkelon2018.forum.domain.UserAccount;
import telran.ashkelon2018.forum.dto.UserProfileDto;
import telran.ashkelon2018.forum.dto.UserRegDto;
import telran.ashkelon2018.forum.exceptions.UserConflictException;

@Service
public class AccountServiceImpl implements AccountService {

	@Autowired
	UserAccountRepository userRepository;

	@Autowired
	AccountConfiguration accountConfiguration;

	@Override
	public UserProfileDto addUser(UserRegDto userRegDto, String token) {
		AccountUserCredentials credentials = accountConfiguration.tokenDecode(token);
		if (userRepository.existsById(credentials.getLogin())) {
			throw new UserConflictException();// create our exception in UserConflictException
		}
		String hashPassword = BCrypt.hashpw(credentials.getPassword(), BCrypt.gensalt());// BCrypt.gensalt() method for
																							// generate passw
		UserAccount userAccount = UserAccount.builder().login(credentials.getLogin()).password(hashPassword)
				.firstName(userRegDto.getFirstName()).lastName(userRegDto.getLastName()).role("user")
				.expdate(LocalDateTime.now().plusDays(accountConfiguration.getExpPeriod())).build();
		userRepository.save(userAccount);
		return convertToUserProfileDto(userAccount);
	}

	private UserProfileDto convertToUserProfileDto(UserAccount userAccount) {
		return UserProfileDto.builder().firstName(userAccount.getFirstName()).lastName(userAccount.getLastName())
				.login(userAccount.getLogin()).roles(userAccount.getRoles()).build();
	}

	@Override
	public UserProfileDto editUser(UserRegDto userRegDto, String token) {
		AccountUserCredentials credentials = accountConfiguration.tokenDecode(token);
		UserAccount userAccount = userRepository.findById(credentials.getLogin()).get();
		if (credentials.getLogin() == userAccount.getLogin()) {

			if (userRegDto.getFirstName() != null) {
				userAccount.setFirstName(userRegDto.getFirstName());
			}
			if (userRegDto.getLastName() != null) {
				userAccount.setFirstName(userRegDto.getLastName());
			}
			userRepository.save(userAccount);
		}
		return convertToUserProfileDto(userAccount);
	}

	@Override
	public UserProfileDto removeUser(String login, String token) {// кого удалить(login) и кто может удалить(token)
		UserAccount userAccount = userRepository.findById(login).orElse(null);
		AccountUserCredentials credentials = accountConfiguration.tokenDecode(token);
		if (userAccount != null && (userAccount.getRoles().contains("admin")
				|| userAccount.getRoles().contains("moderator") || login.equals(credentials.getLogin()))) {
			userRepository.delete(userAccount);
		}
		return convertToUserProfileDto(userAccount);
	}

	@Override
	public Set<String> addRole(String login, String role, String token) {
		UserAccount userAccount = userRepository.findById(login).orElse(null);
		if (userAccount != null && userAccount.getRoles().contains("admin")) {
			userAccount.addRole(role);
			userRepository.save(userAccount);
		} else {
			return null;
		}
		return userAccount.getRoles();
	}

	@Override
	public Set<String> removeRole(String login, String role, String token) {
		UserAccount userAccount = userRepository.findById(login).orElse(null);
		if (userAccount != null && userAccount.getRoles().contains("admin")) {
			userAccount.removeRole(role);
			userRepository.save(userAccount);
		} else {
			return null;
		}
		return userAccount.getRoles();
	}

	@Override
	public void changePassword(String password, String token) {
		// FIXME
		AccountUserCredentials credentials = accountConfiguration.tokenDecode(token);
		UserAccount userAccount = userRepository.findById(credentials.getLogin()).get();
		if (credentials.getLogin() == userAccount.getLogin()) {// password can change only owner
			String hashpassword = BCrypt.hashpw(password, BCrypt.gensalt());
			userAccount.setPassword(hashpassword);
			userAccount.setExpdate(LocalDateTime.now().plusDays(accountConfiguration.getExpPeriod()));
			userRepository.save(userAccount);
		}
	}

	@Override
	public UserProfileDto login(String token) {
		AccountUserCredentials credentials = accountConfiguration.tokenDecode(token);
		UserAccount userAccount = userRepository.findById(credentials.getLogin()).get();
		return convertToUserProfileDto(userAccount);
	}

}
