package com.oracle.OrdsClient;

import com.fasterxml.jackson.annotation.JsonInclude;

public class PdbCreationRequestModel {
    private String pdb_name;
    private String adminName;
    private String adminPwd;
    private String totalSize;
    private String tempSize;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fileNameConversions;

    public String getPdb_name() {
        return pdb_name;
    }

    public void setPdb_name(String pdb_name) {
        this.pdb_name = pdb_name;
    }

    public String getFileNameConversions() {
        return fileNameConversions;
    }

    public void setFileNameConversions(String fileNameConversions) {
        this.fileNameConversions = fileNameConversions;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminUser(String adminUser) {
        this.adminName = adminUser;
    }

    public String getAdminPwd() {
        return adminPwd;
    }

    public void setAdminPwd(String adminPwd) {
        this.adminPwd = adminPwd;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    public String getTempSize() {
        return tempSize;
    }

    public void setTempSize(String tempSize) {
        this.tempSize = tempSize;
    }

    public String getMethod() { return "CREATE"; }

}
