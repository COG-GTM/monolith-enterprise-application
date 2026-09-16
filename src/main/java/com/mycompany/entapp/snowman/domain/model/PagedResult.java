/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import java.util.List;

/**
 * Generic container for a single page of results plus pagination metadata.
 */
public class PagedResult<T> {

    private final List<T> items;
    private final int page;
    private final int pageSize;
    private final long totalCount;
    private final int totalPages;

    public PagedResult(List<T> items, int page, int pageSize, long totalCount) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.totalPages = (int) (totalCount / pageSize);
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasNext() {
        return page < totalPages;
    }

    public boolean isHasPrevious() {
        return page > 1;
    }
}
