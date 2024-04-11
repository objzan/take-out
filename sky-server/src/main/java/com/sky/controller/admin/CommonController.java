package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.MinIOUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private MinIOUtil minIOUtil;

    /**
     * 图片上传接口
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("upload")
    @ApiOperation("图片上传接口")
    public Result<String> upload(MultipartFile file) {
        byte[] fileBytes = null;
        try {
            fileBytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName=UUID.randomUUID().toString()+extension;
            String fileUrl = minIOUtil.upload(fileBytes, fileName);
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
