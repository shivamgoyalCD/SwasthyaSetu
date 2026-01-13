package com.swasthyasetu.common.dtos;

import java.util.List;

public class PageResponse<T> {
  private List<T> items;
  private String nextCursor;

  public PageResponse() {
  }

  public PageResponse(List<T> items, String nextCursor) {
    this.items = items;
    this.nextCursor = nextCursor;
  }

  public List<T> getItems() {
    return items;
  }

  public void setItems(List<T> items) {
    this.items = items;
  }

  public String getNextCursor() {
    return nextCursor;
  }

  public void setNextCursor(String nextCursor) {
    this.nextCursor = nextCursor;
  }
}
