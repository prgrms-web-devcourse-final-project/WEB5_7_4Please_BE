package com.deal4u.fourplease.domain.file.service;

import java.net.URL;
import org.springframework.web.multipart.MultipartFile;

public interface FileSaver {

    /**
     * 파일 업로드를 처리하는 메서드입니다.
     *
     * <p>다음과 같은 예외 상황이 발생할 수 있습니다:</p>
     * <ul>
     *   <li>{@link com.deal4u.fourplease.global.exception.ErrorCode#INVALID_FILE}
     *   - 파일이 비어 있거나, 잘못된 파일 이름이 전달된 경우</li>
     *   <li>{@link  com.deal4u.fourplease.global.exception.ErrorCode#FILE_SAVE_FAILED}
     *   - 파일 저장 중 실패한 경우</li>
     * </ul>
     *
     * @param file     저장할 파일
     * @param savePath 저장 경로 및 파일 이름
     * @return 최정 저장된 경로
     * @author 고지훈
     */
    URL save(SavePath savePath, MultipartFile file);
}
