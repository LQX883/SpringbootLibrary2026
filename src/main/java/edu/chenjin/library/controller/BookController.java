package edu.chenjin.library.controller;

import edu.chenjin.library.aspect.LogOperation;
import edu.chenjin.library.common.PageResult;
import edu.chenjin.library.common.Result;
import edu.chenjin.library.dto.BookRequest;
import edu.chenjin.library.dto.BookView;
import edu.chenjin.library.dto.StatusRequest;
import edu.chenjin.library.security.RequireRole;
import edu.chenjin.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Result<PageResult<BookView>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(bookService.page(keyword, type, status, onlyAvailable, page, size));
    }

    @GetMapping("/types")
    public Result<List<String>> types() {
        return Result.ok(bookService.types());
    }

    @GetMapping("/{id}")
    public Result<BookView> detail(@PathVariable Long id) {
        return Result.ok(bookService.detail(id));
    }

    @PostMapping
    @RequireRole({"ADMIN", "OPERATOR"})
    @LogOperation(module = "图书", action = "新增", description = "新增图书")
    public Result<BookView> create(@Valid @RequestBody BookRequest request) {
        return Result.ok("图书新增成功", bookService.create(request));
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN", "OPERATOR"})
    @LogOperation(module = "图书", action = "编辑", description = "编辑图书信息")
    public Result<BookView> update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return Result.ok("图书信息已更新", bookService.update(id, request));
    }

    @PutMapping("/{id}/status")
    @RequireRole({"ADMIN", "OPERATOR"})
    @LogOperation(module = "图书", action = "上下架", description = "修改图书上下架状态")
    public Result<BookView> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return Result.ok("图书状态已更新", bookService.changeStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "OPERATOR"})
    @LogOperation(module = "图书", action = "删除", description = "删除图书")
    public Result<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/cover")
    @RequireRole({"ADMIN", "OPERATOR"})
    @LogOperation(module = "图书附件", action = "上传封面", description = "上传图书封面图片")
    public Result<BookView> uploadCover(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return Result.ok("封面已更新", bookService.uploadCover(id, file));
    }

    @PostMapping("/{id}/gallery")
    @RequireRole({"ADMIN", "OPERATOR"})
    @LogOperation(module = "图书附件", action = "上传图片", description = "上传图书相关图片")
    public Result<BookView> uploadGallery(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files) {
        return Result.ok("相关图片已上传", bookService.uploadGallery(id, files));
    }

    @DeleteMapping("/{id}/gallery/{attachmentId}")
    @RequireRole({"ADMIN", "OPERATOR"})
    @LogOperation(module = "图书附件", action = "删除图片", description = "删除图书相关图片")
    public Result<BookView> deleteAttachment(@PathVariable Long id, @PathVariable Long attachmentId) {
        bookService.deleteAttachment(id, attachmentId);
        return Result.ok("图片已删除", bookService.detail(id));
    }
}
