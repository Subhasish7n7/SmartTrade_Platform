package smarttrade.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class smartTradeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(smartTradeBackendApplication.class, args);
	}

}
