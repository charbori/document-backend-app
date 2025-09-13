package main.blog;

import me.desair.tus.server.TusFileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
public class BlogApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogApplication.class, args);
	}

	@Value("${tus.server.data.directory}")
	protected String tusDataPath;

	@Value("${server.servlet.context-path:/}")
	private String servletContextPath;

	@Bean
	@Profile("!test")  // 테스트 환경에서는 제외
	public TusFileUploadService tusFileUploadService() {
		return new TusFileUploadService()
				.withStoragePath(tusDataPath)
				.withDownloadFeature()
				.withUploadUri("/api/content/tus")
				.withThreadLocalCache(true);
	}
}
