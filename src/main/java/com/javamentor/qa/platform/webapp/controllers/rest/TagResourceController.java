package com.javamentor.qa.platform.webapp.controllers.rest;


import com.javamentor.qa.platform.models.dto.RelatedTagsDto;
import com.javamentor.qa.platform.service.abstracts.dto.TagDtoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * \* Created with IntelliJ IDEA.
 * \* User: Rustam
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user/tag")
@Api(value = "Работа с тэгами на вопросы", tags = {"Тэг и вопросы"})
public class TagResourceController {

    private final TagDtoService tagDtoService;

    @ApiOperation(value = "Получение списка из 10 тэгов с " +
            "наибольшим количеством вопросов с данным тэгом", tags = {"Получение списка тэгов"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешное получение")})
    @GetMapping("/related")
    public ResponseEntity<List<RelatedTagsDto>> getRelatedTagDto() {
        return new ResponseEntity<>(tagDtoService.getRelatedTagsDto(), HttpStatus.OK);
    }
}
