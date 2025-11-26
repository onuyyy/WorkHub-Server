package com.workhub.file.api;

import com.workhub.file.dto.FileUploadResponse;
import com.workhub.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "File", description = "파일 업로드 및 다운로드 API")
public interface FileApi {

    @Operation(
            summary = "파일 업로드",
            description = "파일을 AWS S3에 업로드합니다. 업로드된 파일은 UUID 기반의 고유한 파일명으로 저장됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "파일 업로드 성공",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (파일이 없거나 형식이 잘못됨)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (S3 업로드 실패)"
            )
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<FileUploadResponse>> uploadFile(
            @Parameter(description = "업로드할 파일", required = true)
            @RequestPart("file") List<MultipartFile> files
    );

    @Operation(
            summary = "파일 다운로드 URL 조회",
            description = "S3에 저장된 파일의 Presigned URL을 생성합니다. 생성된 URL은 10분간 유효합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Presigned URL 생성 성공",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "파일을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (Presigned URL 생성 실패)"
            )
    })
    @GetMapping("/get-files")
    public ApiResponse<List<FileUploadResponse>> getFileUrl(
            @Parameter(description = "S3에 저장된 파일명 리스트", required = true, example = "87bf751a-bbf0-4670-a49e-1950ec7f8be9.pdf")
            @RequestParam("fileNames") List<String> fileNames
    );
}
