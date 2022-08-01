package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basepath;
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        log.info(file.toString());
        String originalFilename=file.getOriginalFilename();
        String suffix=originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用UUID重新生成文件名，防止重复
        String fileName= UUID.randomUUID().toString()+suffix;
        //创建目录对象
        File dir=new File(basepath);
        if(!dir.exists()){
            //目录不存在，需要创建
            dir.mkdirs();
        }
        file.transferTo(new File(basepath+fileName));
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        //输入流 通过输入流读取文件内容
        FileInputStream fileInputStream=new FileInputStream(new File(basepath+name));
        //输出流 通过输出流将文件写回浏览器，并展示图片
        response.setContentType("image/jpeg");
        ServletOutputStream outputStream=response.getOutputStream();
        int len=0;
        byte[] bytes=new byte[1024];
        while((len=fileInputStream.read(bytes))!=-1){
            outputStream.write(bytes,0,len);
            outputStream.flush();
        }
        outputStream.close();
        fileInputStream.close();
    }

}
