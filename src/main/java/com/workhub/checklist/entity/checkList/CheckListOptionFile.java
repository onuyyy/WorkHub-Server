package com.workhub.checklist.entity.checkList;

import com.workhub.file.dto.FileUploadResponse;
import com.workhub.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "check_list_option_file")
@Entity
public class CheckListOptionFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_list_option_file_id")
    private Long checkListOptionFileId;

    @Column(name = "file_url", length = 255, nullable = false)
    private String fileUrl;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "file_order")
    private Integer fileOrder;

    @Column(name = "check_list_option_id", nullable = false)  // Option과 연결
    private Long checkListOptionId;

    public static CheckListOptionFile of(Long checkListOptionId, String fileUrl, Integer fileOrder) {
        return CheckListOptionFile.builder()
                .fileUrl(fileUrl)
                .fileName(extractFileName(fileUrl))
                .fileOrder(fileOrder)
                .checkListOptionId(checkListOptionId)
                .build();
    }

    public static CheckListOptionFile fromUpload(Long checkListOptionId,
                                                 FileUploadResponse uploadFile,
                                                 Integer fileOrder) {
        return CheckListOptionFile.builder()
                .fileUrl(uploadFile.fileName())
                .fileName(uploadFile.originalFileName())
                .fileOrder(fileOrder)
                .checkListOptionId(checkListOptionId)
                .build();
    }

    private static String extractFileName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return "unknown";
        }
        int lastSlashIndex = fileUrl.lastIndexOf('/');
        return lastSlashIndex != -1 ? fileUrl.substring(lastSlashIndex + 1) : fileUrl;
    }

    public void updateFile(String fileUrl, Integer fileOrder) {
        if (fileUrl != null) {
            this.fileUrl = fileUrl;
            this.fileName = extractFileName(fileUrl);
        }
        if (fileOrder != null) {
            this.fileOrder = fileOrder;
        }
    }

    public boolean isManagedFile() {
        if (fileUrl == null) {
            return false;
        }
        return !fileUrl.startsWith("http://") && !fileUrl.startsWith("https://");
    }
}
