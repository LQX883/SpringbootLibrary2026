package edu.chenjin.library.common;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResult<T>(List<T> records, long total, int page, int size, int pages) {

    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages()
        );
    }

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        int pages = size <= 0 ? 0 : (int) Math.ceil((double) total / size);
        return new PageResult<>(records, total, page, size, pages);
    }
}
