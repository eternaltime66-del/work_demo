package org.wx.core.wxBusiness.api.unit;


import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.wx.core.wxBase.annotation.NeedHeader;
import org.wx.core.wxBase.annotation.ParamCheck;
import org.wx.core.wxBase.base.Wx;
import org.wx.core.wxBase.base.WxResult;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBase.unit.HttpRequestUnit;
import org.wx.core.wxBase.unit.HttpServletUnit;
import org.wx.core.wxBase.unit.WordUnit;
import org.wx.core.wxBusiness.account.entity.enums.MemberRole;
import org.wx.core.wxBusiness.code.CodeEnum;
import org.wx.core.wxBusiness.common.entity.WxSuperParam;
import org.wx.core.wxBusiness.log.annotation.WxRequestLog;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * 前端-工具接口
 *
 * @author 无心
 * @since 2021-07-21
 */
@RestController
@RequestMapping("/api/unit")
public class SysUtilController {



    @Value("${app.upload.root-windows:D:/File/Work/Java/002_DP/}")
    private String filepathWin;

    @Value("${app.upload.root-linux:/www/wx/file/}")
    private String filepathLinux;

    /**
     * 发送验证码
     * @param account   账号 邮箱
     * @param type      验证码类型
     * @return WxBaseResult
     */
    @PostMapping("/send/code")
    public WxResult<String> codeSend(
            @NotNull @ParamCheck(msg = "邮箱",pattern = "") String account,
            @NotNull @ParamCheck(msg = "类型", enumPattern = CodeEnum.class) CodeEnum type
    ) {
        String phoneCode = "";
        String action = type.toString();
        System.err.println(phoneCode + account + "开始发送验证码");
        String keys = phoneCode + account;
        String key = "send-code-count-" + DateUtil.today() + ">>>" + keys;
        Long sendCount = Wx.RedisFactory.incr(key, 24L * 60 * 60);
        ErrorFactory.throwError(sendCount != null && sendCount > 10, "24小时内验证码获取次数已达上限");
        String code = Wx.CodeFactory.sendCode(phoneCode,account, CodeEnum.valueOf(action));
        return WxResult.success();
    }

    /**
     * 上传文件
     * @param file 文件流
     * @return
     */
    @PostMapping("/upload")
    @NeedHeader(roles = MemberRole.ADMIN)
    public WxResult<Object> upload(
            @NotNull @RequestParam("file") MultipartFile file
    ) {
        ErrorFactory.throwError(file.isEmpty(), "上传文件不能为空");
        ErrorFactory.throwError(file.getSize() > 5L * 1024 * 1024, "图片不能超过5MB");
        String fileName = file.getOriginalFilename();
        ErrorFactory.throwError(fileName == null || fileName.isBlank(), "文件名不能为空");
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowTypes = Arrays.asList("jpg", "jpeg", "png");

        ErrorFactory.throwError(!allowTypes.contains(fileType), "不支持该格式文件上传");
        ErrorFactory.throwError(fileName.chars().filter(ch -> ch == '.').count() > 1, "文件名不合法，包含多重后缀");
        String contentType = file.getContentType();
        ErrorFactory.throwError(contentType == null || !("image/jpeg".equalsIgnoreCase(contentType)
                || "image/png".equalsIgnoreCase(contentType)), "文件内容不是受支持的图片");
        String filepath = isWinOs()?filepathWin:filepathLinux;
        String realFilePath = filepath + "images/";
        File targetFile = new File(realFilePath);
        if (!targetFile.exists()) {
            ErrorFactory.throwError(!targetFile.mkdirs(), "创建上传目录失败");
        }
        String normalizedExt = "jpeg".equals(fileType) ? "jpg" : fileType;
        String name = WordUnit.nowId(4, 1) + "." + normalizedExt;
        File destination = new File(targetFile, name);
        try {
            file.transferTo(destination);
            HashMap<String,String> map = new HashMap<String,String>();
            String url = String.format("/uploads/images/%s", name);
            map.put("url", url);
            return WxResult.success(map);
        } catch (Exception e) {
            ErrorFactory.throwError("上传异常");

        }
        return WxResult.success();
    }

    public static Boolean isWinOs() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().startsWith("win");
    }

    /**
     * 获取系统参数
     */
    @PostMapping("/config/list")
    @WxRequestLog
    @NeedHeader(roles = {MemberRole.USER})
    public WxResult<List<WxSuperParam>> levelList(
            WxSuperParam entity
    ){

        IPage<WxSuperParam> iPage = Wx.SuperParamService.pageQuery(entity);
        return WxResult.page(iPage);
    }
}

