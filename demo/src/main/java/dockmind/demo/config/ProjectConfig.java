package dockmind.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectConfig {

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI().info(
                new Info().title("DocMind ai document intelligence and rag api's ").description("rest apis ")
        );
    }

}
