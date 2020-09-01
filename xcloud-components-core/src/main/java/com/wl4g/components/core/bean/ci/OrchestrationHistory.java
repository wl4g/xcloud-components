package com.wl4g.components.core.bean.ci;

import com.wl4g.components.core.bean.BaseBean;

import java.util.List;

public class OrchestrationHistory extends BaseBean {
    private static final long serialVersionUID = 6815608076300843748L;

    private String runId;

    private Integer status;

    private String info;

    private Long costTime;

    private List<TaskHistory> taskHistories;

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId == null ? null : runId.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info == null ? null : info.trim();
    }

    public List<TaskHistory> getTaskHistories() {
        return taskHistories;
    }

    public void setTaskHistories(List<TaskHistory> taskHistories) {
        this.taskHistories = taskHistories;
    }

    public Long getCostTime() {
        return costTime;
    }

    public void setCostTime(Long costTime) {
        this.costTime = costTime;
    }
}