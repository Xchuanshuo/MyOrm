package com.legend.orm.demo;

import com.legend.orm.core.interfaces.IEntity;
import com.legend.orm.core.model.Meta;

import java.util.*;

/**
 * @author Legend
 * @data by on 18-12-15.
 * @description
 */
public class DemoSharding {

    private static final String URI_1 = "jdbc:mysql://localhost:3306/orm" +
            "?user=legend&password=2414605975&useUnicode=true&characterEncoding=UTF-8";
    private static final String URI_2 = "jdbc:mysql://localhost:3306/orma" +
            "?user=legend&password=2414605975&useUnicode=true&characterEncoding=UTF-8";
    private static final String URI_3 = "jdbc:mysql://localhost:3306/ormb" +
            "?user=legend&password=2414605975&useUnicode=true&characterEncoding=UTF-8";

    private static DemoLegendDB[] dbs = new DemoLegendDB[3];

    static {
        Map<Class<? extends IEntity>, Meta> metas = new HashMap<>();
        dbs[0] = new DemoLegendDB("demo-1", URI_1);
        dbs[1] = new DemoLegendDB("demo-2", URI_2);
        dbs[2] = new DemoLegendDB("demo-3", URI_3);
    }

    public static void main(String[] args) {
        GridDemoDB grid = new GridDemoDB(dbs);
        try {
            for (int k=0;k<BookShelf.PARTITIONS-1;k++) {
                grid.create(BookShelf.class, String.valueOf(k));
            }
            List<BookShelf> bookShelfList = new ArrayList<>();
            for (int i=0;i<100;i++) {
                BookShelf bs = new BookShelf("user" + i, "book" + i, "comment" + i, new Date());
                bookShelfList.add(bs);
                grid.insert(bs);
            }
            for (int k=0;k<grid.size();k++) {
                for (int i = 0; i < BookShelf.PARTITIONS-1; i++) {
                    // 依次查询出所有分库的分表的行数
                    System.out.printf("db %d partition %d count %d\n", k, i,
                            grid.count(BookShelf.class, k, String.valueOf(i)));
                }
            }
            Random random = new Random();
            for (BookShelf bs: bookShelfList) {
                bs.setComment("comment_update_" + random.nextInt(100));
                // 更新，自动分发到相应的分库中的分表
                grid.update(bs);
            }
            for (BookShelf bs: bookShelfList) {
                // 主键查询 自动分到相应的分库中的分表
                bs = grid.get(BookShelf.class, bs.getUserId(), bs.getBookId());
                System.out.println(bs.getComment());
            }
//            for (BookShelf bs: bookShelfList) {
//                grid.delete(bs);
//            }
            for (int k=0;k<grid.size();k++) {
                for (int i=0;i<BookShelf.PARTITIONS-1;i++) {
                    // 依次查询出所有分库的分表的行数
                    System.out.printf("db %d partition %d count %d\n", k, i,
                            grid.count(BookShelf.class, k, String.valueOf(i)));
                }
            }
        } finally {
//            for (int k=0;k<BookShelf.PARTITIONS;k++) {
//                grid.drop(BookShelf.class, String.valueOf(k));
//            }
        }
    }
}
