package iuh.fit.se;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GlassesServerApplication {
	
	private final static Logger logger = LoggerFactory.getLogger(GlassesServerApplication.class.getName());
	
	public static void main(String[] args) {
		SpringApplication.run(GlassesServerApplication.class, args);
		logger.info("GlassesServerApplication Start");
	}
	@Bean
	public ModelMapper modelMapper() {
	    return new ModelMapper();
	}
}
