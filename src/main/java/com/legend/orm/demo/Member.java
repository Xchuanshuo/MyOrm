package com.legend.orm.demo;

import com.legend.orm.core.interfaces.IEntity;
import com.legend.orm.core.annotation.Column;
import com.legend.orm.core.annotation.Table;
import com.legend.orm.core.model.TableIndices;
import com.legend.orm.core.model.TableOptions;

import java.util.Date;

/**
 * @author Legend
 * @data by on 18-12-13.
 * @description
 */
@Table("member")
public class Member implements IEntity {

    @Column(name = "user_id", type = "int", nullable = false)
    private Integer userId;
    @Column(name = "group_id", type = "int", nullable = false)
    private Integer groupId;
    @Column(name = "title", type = "varchar(255)")
    private String title;
    @Column(name = "created_at", type = "datetime", nullable = false, defaultValue = "now()")
    private Date createdAt;

    public Member() {
    }

    public Member(Integer userId, Integer groupId, String title, Date createdAt) {
        this.userId = userId;
        this.groupId = groupId;
        this.title = title;
        this.createdAt = createdAt;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public String getTitle() {
        return title;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public TableOptions options() {
        return new TableOptions().options("engine", "innodb");
    }

    @Override
    public TableIndices indices() {
        return new TableIndices().primary("user_id", "group_id");
    }

    @Override
    public String table() {
        return "member";
    }
}
