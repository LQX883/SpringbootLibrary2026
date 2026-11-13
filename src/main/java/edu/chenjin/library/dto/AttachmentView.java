package edu.chenjin.library.dto;

import edu.chenjin.library.entity.BookAttachment;

public record AttachmentView(
        Long id,
        String url,
        String originalName,
        String imageType,
        Long fileSize,
        Integer sortOrder
) {

    public static AttachmentView from(BookAttachment attachment) {
        return new AttachmentView(
                attachment.getId(),
                attachment.getUrl(),
                attachment.getOriginalName(),
                attachment.getImageType(),
                attachment.getFileSize(),
                attachment.getSortOrder()
        );
    }
}
