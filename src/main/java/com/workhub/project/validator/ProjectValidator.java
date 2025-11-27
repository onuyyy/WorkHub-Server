package com.workhub.project.validator;

public interface ProjectValidator {
    void validateExistsProject(Long projectId);
    void validateContractEndDate(Long projectId);
}
