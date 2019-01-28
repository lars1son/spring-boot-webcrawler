package com.webcrawler.model;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Артем on 27.01.2019.
 */
public class NodesStageInfo {
    private Integer allNodesCount;
    private Integer usedNodesCount;
    private CopyOnWriteArrayList<String> stageCrawlList;

    public NodesStageInfo(CopyOnWriteArrayList<String> toCrawlOneStageNodes) {
        this.stageCrawlList = toCrawlOneStageNodes;
        this.allNodesCount = toCrawlOneStageNodes.size();

    }

    public NodesStageInfo() {
        this.usedNodesCount=0;
        this.allNodesCount=0;
    }

    public boolean isUpdateStageNeeded(){
        return usedNodesCount==allNodesCount;
    }
    public void setStageCrawlList(CopyOnWriteArrayList<String> toCrawlOneStageNodes) {
        this.stageCrawlList = toCrawlOneStageNodes;
        this.allNodesCount = toCrawlOneStageNodes.size();
    }

    public int getAllNodesCount() {
        if (allNodesCount == null)
            return 0;
        return allNodesCount;
    }

    public void setAllNodesCount(int allNodes) {
        this.allNodesCount = allNodes;
    }

    public int getUsedNodesCount() {
        if (usedNodesCount == null)
            return 0;
        return usedNodesCount;

    }

    public synchronized void updateUsedNodesCount() {
        ++this.usedNodesCount;
    }

    public boolean isEmpty() {
        return allNodesCount == null;
    }

    public String getFirstNode() {
        return this.stageCrawlList.remove(0);
    }

    public boolean waitForStageFinish(int activeThreadsCount) {
        return activeThreadsCount + this.getUsedNodesCount() >= allNodesCount;
    }
}
