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
@Table("user")
public class User implements IEntity {

    @Column(name = "id", type="int", autoIncrement = true, nullable = false)
    private Integer id;
    @Column(name = "name", type = "varchar(255)", nullable = false)
    private String name;
    @Column(name = "nick", type = "varchar(255)", nullable = false)
    private String nick;
    @Column(name = "password", type = "varchar(255)", nullable = false)
    private String password;
    @Column(name = "created_at", type = "datetime", nullable = false, defaultValue = "now()")
    private Date createdAt;

    public User() {}

    public User(String name, String nick, String password) {
        this.name = name;
        this.nick = nick;
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNick() {
        return nick;
    }

    public String getPassword() {
        return password;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public TableOptions options() {
        return new TableOptions().options("Engine", "InnoDB");
    }

    @Override
    public TableIndices indices() {
        return new TableIndices().primary("id").unique("name");
    }
}
