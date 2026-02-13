package com.chenliao.chenliaoblog.controller.admin;

import com.chenliao.chenliaoblog.annotation.OperationLog;
import com.chenliao.chenliaoblog.annotation.OperationType;
import com.chenliao.chenliaoblog.config.page.PageRequest;
import com.chenliao.chenliaoblog.config.page.PageResult;
import com.chenliao.chenliaoblog.entity.Tag;
import com.chenliao.chenliaoblog.service.TagService;
import com.chenliao.chenliaoblog.utils.JsonResult;
import com.chenliao.chenliaoblog.utils.PageUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理")
@RestController("adminTagController")
@RequestMapping("/admin/tag")
public class TagController {
    @Autowired
    TagService tagService;

    /**
     * 分页查询列表
     * @param pageRequest
     * @return
     */
    @Operation(summary = "标签列表")
    @PostMapping("list")
    @OperationLog(desc = "分页查询标签列表", operationType = OperationType.SELECT)
    public JsonResult<Object> listPage(@RequestBody PageRequest pageRequest) {
        List<Tag> tagList = tagService.getTagPage(pageRequest);
        PageInfo pageInfo = new PageInfo(tagList);
        PageResult pageResult = PageUtil.getPageResult(pageRequest, pageInfo);
        return JsonResult.success(pageResult);
    }

    /**
     * 添加标签
     * @return
     */
    @Operation(summary = "添加标签")
    @PostMapping("/create")
    @OperationLog(desc = "添加标签", operationType = OperationType.INSERT)
    public JsonResult<Object> tagCreate(@RequestBody Tag tag) {
        int isStatus = tagService.saveTag(tag);
        if (isStatus == 0) {
            return JsonResult.error("添加标签失败");
        }
        return JsonResult.success();
    }

    /**
     * 批量添加标签,最多添加10个
     * @param tags 以字符串的方式，以英文逗号隔开。例如：Java,C语言,Python
     *
     * @return
     */
    @Operation(summary = "批量添加标签")
    @PostMapping("/batchCreate")
    @OperationLog(desc = "批量添加标签", operationType = OperationType.INSERT)
    public JsonResult<Object> batchCreate(@RequestBody Tag tags) {
        try {
            boolean isStatus = tagService.batchAddTag(tags.getTagName());
            if (!isStatus) {
                return JsonResult.error("批量插入失败！");
            }
        }catch (Exception e) {
            return JsonResult.error(e.getMessage());
        }
        return JsonResult.success();
    }

    /**
     * 批量删除标签
     * @param ids
     * @return
     */
    @Operation(summary = "批量删除标签")
    @DeleteMapping("/batchDelete")
    @OperationLog(desc = "批量删除标签", operationType = OperationType.DELETE)
    public JsonResult<Object> batchDelete(@RequestBody String ids) {
        boolean isDelTag = tagService.batchDelTag(ids);
        if (!isDelTag) {
            return JsonResult.error("批量删除标签失败");
        }
        return JsonResult.success();
    }

    /**
     * 修改标签
     * @return
     */
    @Operation(summary = "修改标签")
    @PutMapping("/update")
    @OperationLog(desc = "修改标签", operationType = OperationType.UPDATE)
    public JsonResult<Object> tagUpdate(@RequestBody Tag tag) {
        int isStatus = tagService.updateTag(tag);
        if (isStatus == 0) {
            return JsonResult.error("修改标签失败");
        }
        return JsonResult.success();
    }

    /**
     * 删除
     * @return
     */
    @Operation(summary = "删除标签")
    @DeleteMapping("/delete/{id}")
    @OperationLog(desc = "删除标签", operationType = OperationType.DELETE)
    public JsonResult<Object> tagDelete(@PathVariable(value = "id") int id) {
        tagService.deleteTag(id);
        return JsonResult.success();
    }
}
