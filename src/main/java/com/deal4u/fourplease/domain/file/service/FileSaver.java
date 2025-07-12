package com.deal4u.fourplease.domain.file.service;

import java.net.URL;
import org.springframework.web.multipart.MultipartFile;

public interface FileSaver {

    URL save(SaveData saveData, MultipartFile file);
}
