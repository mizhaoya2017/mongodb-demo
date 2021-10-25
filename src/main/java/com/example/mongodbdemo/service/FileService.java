package com.example.mongodbdemo.service;

import com.example.mongodbdemo.data.FileProperties;
import com.example.mongodbdemo.data.vo.PathResultVO;
import com.example.mongodbdemo.excepition.FileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @Author xieyitong  <xieyitong@situdata.com>
 * @Date 2020/8/15 14:29
 **/
@Service
public class FileService {

    private final Path fileStorageLocation; // 文件在本地存储的地址
    private String fileprop; // 配置地址

    @Autowired
    public FileService(FileProperties fileProperties) {
        this.fileStorageLocation = Paths.get(fileProperties.getUploadDir()).toAbsolutePath().normalize();
        this.fileprop = fileProperties.getUploadDir();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * 存储文件到系统
     *
     * @param file 文件
     * @return 文件名
     */
    public PathResultVO storeFile(String orderId, MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            String path = Paths.get(this.fileStorageLocation + File.separator + orderId).toString();
            Files.createDirectories(Paths.get(this.fileStorageLocation + File.separator + orderId));
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = Paths.get(this.fileStorageLocation + File.separator + orderId).resolve(fileName);
            // Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            PathResultVO pathResultVO = new PathResultVO();
            pathResultVO.setFileName(fileName);
            pathResultVO.setPath(fileprop);

            return pathResultVO;
        } catch (IOException ex) {
            throw new FileException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    /**
     * 加载文件
     * @param fileName 文件名
     * @return 文件
     */
    public Resource loadFileAsResource(String orderId, String fileName) {
        try {
            Path filePath = Paths.get(this.fileStorageLocation + File.separator + orderId).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new FileException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileException("File not found " + fileName, ex);
        }
    }

    public String getPath(){
        return this.fileStorageLocation.toString();
    }
}