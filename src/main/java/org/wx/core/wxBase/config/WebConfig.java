package org.wx.core.wxBase.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web 配置类
 * @author Administrator
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许独立静态站跨域调后端 API（token 在 Header，不依赖 Cookie）
                .allowedOriginPatterns("*")
                .allowedHeaders("*")
                .allowCredentials(false)
                .allowedMethods("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH")
                .maxAge(3600);
    }

    public final String filepathWin = "D:\\File\\Work\\Java\\002_DP\\";
    public final String filepathLinux = "/www/wx/file/";


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
