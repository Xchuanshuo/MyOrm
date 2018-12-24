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
            db.create(User.class);
            User user1 = new User("aas fra562mjework", "hejllos", "传说salegend");
//            User user2 = new User("bb fjramework", "wojrlds", "传asasalegendsas奇数");
            db.insert(user1);
//            db.insert(user2);
//            System.out.println(user1.getId());
//            User user = db.get(User.class, user1.getId());
//            System.out.printf("%s %s %s %s %s\n", user.getId(), user.getName(),
//                    user.getNick(), user.getPassword(), user.getCreatedAt());
            long count = db.count(User.class, LegendBase.between_("id"), 13, 40);
            System.out.println("总行数："+count);
            List<User> userList = db.find(User.class);
            System.out.println(userList.size());
            userList.forEach(u -> System.out.printf("%s %s %s %s %s\n", u.getId(), u.getName(),
                    u.getNick(), u.getPassword(), u.getCreatedAt()));
            Map<String, Object> setters = new HashMap<>();
            setters.put("password", "legendsasas请求as");
            db.update(User.class, setters, LegendBase.lt_("id"), 18);
            userList = db.groupBy("password").offset(2).limit(5).orderBy("name").find(User.class,
                    LegendBase.searchList(Arrays.asList("name", "nick", "password"), "legend"));
            System.out.println(userList.size());
            userList.forEach(u -> System.out.printf("%s %s %s %s %s\n", u.getId(), u.getName(),
                    u.getNick(), u.getPassword(), u.getCreatedAt()));
            count = db.groupBy("password").limit(10).orderBy("name").count(User.class,
                    LegendBase.searchList(Arrays.asList("name", "nick", "password"), "legend"));
//            db.delete(User.class, 1);
//            db.delete(User.class, 2);
//            count = db.count(User.class);
            System.out.println("总行数："+count);
            long max = db.limit(10).max(User.class, "id");
            System.out.println("max: "+max);
            long min = db.offset(0).limit(10).min(User.class, "id");
            System.out.println("min: "+min);
            long sum = db.sum(User.class, "id");
            System.out.println("sum: "+sum);
            long average = db.average(User.class, "id");
            System.out.println("average: "+average);
            long counts = db.groupBy("nick").count(User.class);
            System.out.println(counts);
        } finally {
//            db.drop(User.class);
        }
    }
}
