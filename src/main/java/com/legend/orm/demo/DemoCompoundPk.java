package com.legend.orm.demo;

import com.legend.orm.core.crud.LegendBase;

import java.util.*;

/**
 * @author Legend
 * @data by on 18-12-14.
 * @description
 */
public class DemoCompoundPk {

    private static final String URI = "jdbc:mysql://localhost:3306/orm" +
            "?user=legend&password=2414605975&useUnicode=true&characterEncoding=UTF-8";

    public static void main(String[] args) {
        DemoLegendDB db = new DemoLegendDB("demo", URI);
        try {
            db.create(Member.class);
            Member member = new Member(1, 2, "boss", null);
            db.insert(member);
            member = db.get(Member.class, 1, 2);
            System.out.println(member.getTitle());
            member = new Member(2, 2, "manager", new Date());
            db.insert(member);
            System.out.println(member.getTitle());
            long count = db.count(Member.class);
            System.out.println("总行数："+count);
            List<Member> memberList = db.find(Member.class);
            memberList.forEach(m-> System.out.printf("%d %d %s %s\n", m.getUserId(), m.getGroupId(),
                    m.getTitle(), m.getCreatedAt()));
            member = new Member(2, 3, "manager", new Date());
            db.insert(member);
            // 条件查询
            memberList = db.find(Member.class, LegendBase.eq_("group_id"), 2);
            memberList.forEach(m-> System.out.printf("%d %d %s %s\n", m.getUserId(), m.getGroupId(),
                    m.getTitle(), m.getCreatedAt()));

            Map<String, Object> setters = new HashMap<>();
            setters.put("title", "employee");
            db.update(Member.class, setters, 2, 3);
            member = db.get(Member.class, 2, 3);
            System.out.println(member.getTitle());
            db.delete(Member.class, 1, 2);
            db.delete(Member.class, 2, 2);
            db.delete(Member.class, 2, 3);
            count = db.count(Member.class);
            System.out.println(count);
        } finally {
            db.drop(Member.class);
        }
    }

}
