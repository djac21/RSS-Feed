package com.djac21.rssfeed;

public class RssFeedModel {

    private String rssTitle, rssLink, rssDescription;

    public RssFeedModel(String rssTitle, String rssLink, String rssDescription) {
        this.rssTitle = rssTitle;
        this.rssLink = rssLink;
        this.rssDescription = rssDescription;
    }

    public String getRssTitle() {
        return rssTitle;
    }

    public void setRssTitle(String rssTitle) {
        this.rssTitle = rssTitle;
    }

    public String getRssLink() {
        return rssLink;
    }

    public void setRssLink(String rssLink) {
        this.rssLink = rssLink;
    }

    public String getRssDescription() {
        return rssDescription;
    }

    public void setRssDescription(String rssDescription) {
        this.rssDescription = rssDescription;
    }
}