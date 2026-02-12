package com.chenliao.chenliaoblog.controller.admin;

import com.chenliao.chenliaoblog.config.page.PageRequest;
import com.chenliao.chenliaoblog.config.page.PageResult;
import com.chenliao.chenliaoblog.entity.Notice;
import com.chenliao.chenliaoblog.service.NoticeService;
import com.chenliao.chenliaoblog.utils.JsonResult;
import com.chenliao.chenliaoblog.utils.PageUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公告管理")
@RestController
@RequestMapping("/admin/notice")
public class NoticeController {
    @Autowired
    NoticeService noticeService;

    /**
     * 分页查询列表
     * @param pageRequest
     * @return
     */
    @Operation(summary = "公告列表")
    @PostMapping("/list")
    public JsonResult<Object> listPage(@RequestBody  PageRequest pageRequest) {
        List<Notice> noticeList = noticeService.getNoticePage(pageRequest);
        PageInfo pageInfo = new PageInfo(noticeList);
        PageResult pageResult = PageUtil.getPageResult(pageRequest, pageInfo);
        return JsonResult.success(pageResult);
    }

    /**
     * 添加公告
     * @return
     */
    @Operation(summary = "添加公告")
    @PostMapping("/create")
    public JsonResult<Object> categoryCreate(@RequestBody  Notice notice) {
        int isStatus = noticeService.saveNotice(notice);
        if (isStatus == 0) {
            return JsonResult.error("添加公告失败");
        }
        return JsonResult.success();
    }

    /**
     * 修改公告
     * @return
     */
    @Operation(summary = "修改公告")
    @PostMapping("/update")
    public JsonResult<Object> categoryUpdate(@RequestBody  Notice notice) {
        int isStatus = noticeService.updateNotice(notice);
        if (isStatus == 0) {
            return JsonResult.error("修改公告失败");
        }
        return JsonResult.success();
    }

    /**
     * 删除
     * @return
     */
    @Operation(summary = "删除公告")
    @PostMapping("/delete/{id}")
    public JsonResult<Object> categoryDelete(@PathVariable(value = "id") int id) {
        noticeService.deleteNotice(id);
        return JsonResult.success();
    }

}
