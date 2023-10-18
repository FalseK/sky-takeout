package com.sky.controller.admin;
import cn.hutool.core.util.StrUtil;
import com.sky.constant.SystemConstant;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/admin/common")
public class CommonController {

    @PostMapping("/upload")
    public Result<String> imageUpload(MultipartFile file){
        try {
            // 获取原始文件名称
            String originalFilename = file.getOriginalFilename();
            // 生成新文件名
            String fileName = createNewFileName(originalFilename);
            // 保存文件
            file.transferTo(new File(SystemConstant.IMAGE_UPLOAD_DIR, fileName));
            // 返回结果
            log.debug("文件上传成功，{}", fileName);
            return Result.success("/img" + fileName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }

    }

    private String createNewFileName(String originalFilename) {
        // 获取后缀
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        // 生成目录
        String name = UUID.randomUUID().toString();
        // 判断目录是否存在
        File dir = new File(SystemConstant.IMAGE_UPLOAD_DIR, StrUtil.format("/dishes/"));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 生成文件名
        return StrUtil.format("/dishes/{}.{}", name, suffix);
    }

}
