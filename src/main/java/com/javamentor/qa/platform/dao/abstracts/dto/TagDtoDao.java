package com.javamentor.qa.platform.dao.abstracts.dto;

import com.javamentor.qa.platform.models.dto.TagDto;

import java.util.List;

public interface TagDtoDao {
    List<TagDto> getTrackedTagById(Long id);

    List<TagDto> getIgnoreTagById(Long id);
}
