package com.legend.orm.demo;

import com.legend.orm.core.crud.LegendBase;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Legend
 * @data by on 18-12-14.
 * @description
 */
public class DemoComplexQuery {

    private static final String URI = "jdbc:mysql://localhost:3306/orm" +
            "?user=legend&password=2414605975&useUnicode=true&characterEncoding=UTF-8";

    public static void main(String[] args) {
        DemoLegendDB db = new DemoLegendDB("orm", URI);
        try {
            db.create(Exam.class);
            Random random = new Random();
            for (int i=0;i<100;i++) {
                long userId = Math.abs(random.nextLong());
                Exam exam = new Exam(userId, random.nextInt(100), random.nextInt(100), random.nextInt(100),
                        random.nextInt(100), random.nextInt(100), random.nextInt(100));
                db.insert(exam);
            }
            // 查询总行数
            System.out.println(db.count(Exam.class));
            List<Exam> examList = db.find(Exam.class, LegendBase.ge_("math"), 50);
            System.out.println("math <= 50: "+examList.size());
            long count = db.count(Exam.class, LegendBase.ge_("math"), 50);
            System.out.println("【count】math <= 50: "+count);

            examList = db.find(Exam.class, LegendBase.or(LegendBase.gt_("math"), LegendBase.between_("physics"), LegendBase.lt_("chemistry")),
                        50, 60, 80, 60);
            System.out.println("math>50 || physics between 60 and 80 || chemistry < 60: "+examList.size());
            count = db.count(Exam.class, LegendBase.or(LegendBase.gt_("math"), LegendBase.between_("physics"), LegendBase.lt_("chemistry")),
                    50, 60, 80, 60);

            System.out.println("【count】math>50 || physics between 60 and 80 || chemistry < 60: "+count);

            // group by math/10
            LegendBase legendBase = LegendBase.select().field("(math div 10) * 10 as mathx", "count(1)").table("exam").groupBy("mathx")
                    .having(LegendBase.gt_("count(1)")).orderBy("count(1)", "desc"); // 复杂sql构造
            System.out.println(legendBase.sql());
            Map<Integer, Integer> rank = new LinkedHashMap<>();
            db.any(Exam.class, legendBase, stmt -> {
                stmt.setInt(1, 0);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    rank.put(rs.getInt(1), rs.getInt(2));
                }
                return rs;
            });
            rank.forEach((mathx, c) -> System.out.printf("[%d-%d] = %d\n", mathx, mathx+10, c));

        } finally {
            db.drop(Exam.class);
        }
    }
}
