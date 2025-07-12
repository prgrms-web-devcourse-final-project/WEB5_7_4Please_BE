package com.deal4u.fourplease.domain.file.service.s3;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.file.service.SaveData;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.io.IOException;
import java.io.InputStream;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

class S3FileSaverTest {

    @Test
    @DisplayName("검증이실패하면_이미지를_업로드하지_않는다")
    void doesNotUpload_whenValidationFails() {
        MockMultipartFile file = new MockMultipartFile("file", "file.png", "application/zip",
                // unsupported MIME type
                new byte[]{1, 2, 3});

        String savedName = "test.png";

        FileValidator testFileValidator = Mockito.mock(FileValidator.class);
        doThrow(ErrorCode.INVALID_FILE.toException())
                .when(testFileValidator)
                .valid(savedName, file);

        S3FileUploader testFileUploader = Mockito.mock(S3FileUploader.class);

        S3FileSaver fileSaver = new S3FileSaver(testFileValidator, testFileUploader);

        ThrowableAssert.ThrowingCallable executable = () -> fileSaver.save(
                new SaveData("/test/test", savedName), file);
        assertThatThrownBy(executable).isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.INVALID_FILE.getMessage());
        verify(testFileUploader, times(0)).upload(any(), any(), any());
    }

    @Test
    @DisplayName("업로드한_파일의_데이터를_가지고_오지_못하면_예외가_발생한다")
    void throwsException_whenCannotReadFileData() throws IOException {
        MockMultipartFile file = Mockito.mock(MockMultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("fail to read"));

        FileValidator testFileValidator = Mockito.mock(FileValidator.class);

        S3FileUploader testFileUploader = Mockito.mock(S3FileUploader.class);

        S3FileSaver fileSaver = new S3FileSaver(testFileValidator, testFileUploader);

        ThrowableAssert.ThrowingCallable executable = () -> fileSaver.save(
                new SaveData("/test/test.png", "test"), file);
        assertThatThrownBy(executable).isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.FILE_SAVE_FAILED.getMessage());
        verify(testFileUploader, times(0)).upload(any(), any(), any());
    }

    @Test
    @DisplayName("업로드성공")
    void uploadSucceeds_withValidFile() {
        MockMultipartFile file = new MockMultipartFile("file", "file.png", "image/png",
                // valid MIME type and extension
                new byte[]{1, 2});

        FileValidator testFileValidator = Mockito.mock(FileValidator.class);

        S3FileUploader testFileUploader = Mockito.mock(S3FileUploader.class);

        S3FileSaver fileSaver = new S3FileSaver(testFileValidator, testFileUploader);

        fileSaver.save(new SaveData("/test/test", "test.png"), file);

        S3MetaData s3MetaData = new S3MetaData("image/png", String.valueOf(file.getSize()));
        verify(testFileUploader).upload(any(InputStream.class), eq("/test/test/test.png"),
                eq(s3MetaData));
    }
}