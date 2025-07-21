package com.github.stepwise.web.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.github.stepwise.utils.MinioService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import java.io.InputStream;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
  private final MinioService minioService;

  @PostMapping("/upload")
  public String uploadFile(@RequestParam("file") MultipartFile file) {
    try {
      String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
      minioService.uploadFile(file, fileName);
      return "Файл успешно загружен: " + fileName;
    } catch (Exception e) {
      return "Ошибка: " + e.getMessage();
    }
  }

  @GetMapping("/download/{fileName}")
  public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileName,
      HttpServletResponse response) {
    try {
      InputStream fileInputStream = minioService.downloadFile(fileName);
      InputStreamResource resource = new InputStreamResource(fileInputStream);
      return ResponseEntity.ok().header("Content-Disposition", "attachment;filename=" + fileName)
          .body(resource);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }
}
