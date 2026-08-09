package org.wx.core.wxBase.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web 配置类
 * @author Administrator
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*,http://[::1]:*}")
    private String allowedOriginPatterns;

    @Value("${app.upload.root-windows:D:/File/Work/Java/002_DP/}")
    private String filepathWin;

    @Value("${app.upload.root-linux:/www/wx/file/}")
    private String filepathLinux;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(java.util.Arrays.stream(allowedOriginPatterns.split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new))
                .allowedHeaders("*")
                .allowCredentials(false)
                .allowedMethods("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH")
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        boolean isWin = isWinOs();
        String filepath = isWin ? filepathWin.replace("\\", "/") : filepathLinux;
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + filepath);
        // 兼容旧绝对路径 /art/** → app/art
        registry.addResourceHandler("/art/**").addResourceLocations(
                "file:src/main/resources/static/app/art/",
                "classpath:/static/app/art/"
        );
    }

    public static Boolean isWinOs() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().startsWith("win");
    }
}
