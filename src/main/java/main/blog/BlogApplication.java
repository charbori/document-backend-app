package main.blog;

import me.desair.tus.server.TusFileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableCaching
public class BlogApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogApplication.class, args);
	}

	@Value("${tus.server.data.directory}")
	protected String tusDataPath;

	@Value("#{servletContext.contextPath}")
	private String servletContextPath;

	@Bean
	public TusFileUploadService tusFileUploadService() {
		return new TusFileUploadService()
				.withStoragePath(tusDataPath)
				.withDownloadFeature()
				.withUploadUri("/api/content/tus")
				.withThreadLocalCache(true);
	}
}
