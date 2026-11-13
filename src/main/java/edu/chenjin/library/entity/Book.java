package edu.chenjin.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "book")
public class Book extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 60)
    private String author;

    @Column(length = 30)
    private String type;

    @Column(length = 60)
    private String publisher;

    @Column(length = 30, unique = true)
    private String isbn;

    @Column(name = "loan_days")
    private Integer days = 30;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies = 0;

    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies = 0;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(length = 2000)
    private String introduction;

    @Column(name = "shelf_no", length = 30)
    private String shelfNo;

    @PrePersist
    public void initStatus() {
        if (status == null) {
            status = "ACTIVE";
        }
        if (availableCopies == null) {
            availableCopies = totalCopies == null ? 0 : totalCopies;
        }
    }

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

    public Integer getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
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
