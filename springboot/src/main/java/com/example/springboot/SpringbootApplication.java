package com.example.springboot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.annotation.PostConstruct;
import java.io.File;

@SpringBootApplication
@ServletComponentScan
public class SpringbootApplication {

    @Value("${file.upload-path}")
    private String uploadPath;

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // 创建上传目录
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 创建图片上传子目录
        File imageDir = new File(uploadPath + "images/");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
    }
}
