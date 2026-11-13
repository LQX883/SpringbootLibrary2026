package edu.chenjin.library.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class BookRequest {

    @NotBlank(message = "请输入书名")
    @Size(max = 100, message = "书名不能超过 100 个字符")
    private String title;

    @NotBlank(message = "请输入作者")
    @Size(max = 60, message = "作者不能超过 60 个字符")
    private String author;

    private String type;
    private String publisher;
    private String isbn;

    @Min(value = 1, message = "借阅天数至少为 1 天")
    private Integer days = 30;

    @DecimalMin(value = "0.00", message = "价格不能为负数")
    private BigDecimal price;

    @Min(value = 0, message = "馆藏数量不能为负数")
    private Integer totalCopies = 1;

    private String status = "ACTIVE";
    private String introduction;
    private String shelfNo;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getShelfNo() {
        return shelfNo;
    }

    public void setShelfNo(String shelfNo) {
        this.shelfNo = shelfNo;
    }
}
