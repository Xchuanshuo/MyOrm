package com.legend.orm.demo;

import com.legend.orm.core.crud.LegendBase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Legend
 * @data by on 18-12-13.
 * @description
 */
public class DemoSimplePk {

    private static final String URI = "jdbc:mysql://localhost:3306/orm" +
            "?user=legend&password=2414605975&useUnicode=true&characterEncoding=UTF-8";

    public DemoSimplePk() {
    }

    public static void main(String[] args) {
        DemoLegendDB db = new DemoLegendDB("orm", URI);
        try {
            // 创建数据库
            db.create(User.class);
            User user1 = new User("aas fra562mjework", "hejllos", "传说salegend");
//            User user2 = new User("bb fjramework", "wojrlds", "传asasalegendsas奇数");
            // 插入一条数据
            db.insert(user1);
//            db.insert(user2);
            System.out.println(user1.getId());
            // 根据ID查询一条记录
            User user = db.get(User.class, user1.getId());
            System.out.printf("%s %s %s %s %s\n", user.getId(), user.getName(),
                    user.getNick(), user.getPassword(), user.getCreatedAt());
            // 根据id范围统计数据行数
            long count = db.count(User.class, LegendBase.between_("id"), 13, 40);
            System.out.println("总行数："+count);
            // 查询数据表全部数据
            List<User> userList = db.find(User.class);
            System.out.println(userList.size());
            userList.forEach(u -> System.out.printf("%s %s %s %s %s\n", u.getId(), u.getName(),
                    u.getNick(), u.getPassword(), u.getCreatedAt()));
            // 指定字段 和更新条件 对进行数据更新
            Map<String, Object> setters = new HashMap<>();
            setters.put("password", "legendsasas请求as");
            db.update(User.class, setters, LegendBase.lt_("id"), 18);
            // 常用的查询 链式操作
            // 根据指定字段进行模糊查询，根据name进行排序；
            // 分页指定查询页码数，指定每页数据行数；根据password进行分组
            userList = db.groupBy("password")
                    .offset(2)
                    .limit(5)
                    .orderBy("name")
                    .find(User.class, LegendBase.searchList(Arrays.asList("name", "nick", "password"), "legend"));
            System.out.println(userList.size());
            userList.forEach(u -> System.out.printf("%s %s %s %s %s\n", u.getId(), u.getName(),
                    u.getNick(), u.getPassword(), u.getCreatedAt()));
            count = db.groupBy("password").limit(10).orderBy("name").count(User.class,
                    LegendBase.searchList(Arrays.asList("name", "nick", "password"), "legend"));
            // 根据id删除数据
            db.delete(User.class, 1);
            db.delete(User.class, 2);
            count = db.count(User.class);
            System.out.println("总行数："+count);
            // 查询前10条记录指定字段最d大值 不指定offset默认是0
            long max = db.limit(10).max(User.class, "id");
            System.out.println("max: "+max);
            // 查询前10条记录指定字段最小值
            long min = db.offset(0).limit(10).min(User.class, "id");
            System.out.println("min: "+min);
            // 前10条记录指定字段求和
            long sum = db.sum(User.class, "id");
            System.out.println("sum: "+sum);
            // ... 求平均数
            long average = db.average(User.class, "id");
            System.out.println("average: "+average);
            long counts = db.groupBy("nick").count(User.class);
            System.out.println(counts);
        } finally {
            // 删除数据表
            db.drop(User.class);
        }
    }
}
