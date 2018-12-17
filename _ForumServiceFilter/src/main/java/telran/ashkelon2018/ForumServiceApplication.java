package telran.ashkelon2018;

import java.time.LocalDateTime;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import telran.ashkelon2018.forum.dao.UserAccountRepository;
import telran.ashkelon2018.forum.domain.UserAccount;

@SpringBootApplication
public class ForumServiceApplication implements CommandLineRunner{
	
@Autowired	
UserAccountRepository repository;
	
	
	public static void main(String[] args) {
		SpringApplication.run(ForumServiceApplication.class, args);
		
	}

	//create super user with role Admin
	@Override
	public void run(String... args) throws Exception {

		if(!repository.existsById("admin")) {
			String hashPassword=BCrypt.hashpw("admin", BCrypt.gensalt());
			UserAccount userAccount=UserAccount.builder()
					.login("admin")
					.password(hashPassword)
					.firstName("Super")
					.lastName("admin")
					.expdate(LocalDateTime.now().plusYears(25))
					.role("admin")
					.build();
			repository.save(userAccount);
		}
		
	}
}
