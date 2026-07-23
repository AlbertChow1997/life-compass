package com.albertchow.lifecompass.upload;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves {@link FileUploadController#UPLOAD_DIR} at {@code /uploads/**}.
 * Needed because that directory lives outside the classpath (see the field's
 * javadoc), so Spring's default {@code classpath:/static/} handler won't see it.
 */
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    /** Maps requests to /uploads/** onto files in the uploads directory on disk. */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + FileUploadController.UPLOAD_DIR + "/");
    }
}
