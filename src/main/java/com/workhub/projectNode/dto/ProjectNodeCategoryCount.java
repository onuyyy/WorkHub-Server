package com.workhub.projectNode.dto;

import com.workhub.projectNode.entity.NodeCategory;

public record ProjectNodeCategoryCount(
        NodeCategory nodeCategory,
        Long totalNodes,
        Long completedNodes
) {}
