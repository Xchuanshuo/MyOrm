package com.legend.orm.demo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author Legend
 * @data by on 18-12-15.
 * @description
 */
public class DemoPartitioning {

    private static final String URI = "jdbc:mysql://localhost:3306/orm" +
            "?user=legend&password=2414605975&useUnicode=true&characterEncoding=UTF-8";

    public static void main(String[] args) {
        DemoLegendDB db = new DemoLegendDB("demo", URI);
        try {
            for (int i=0;i<BookShelf.PARTITIONS;i++) {
                db.create(BookShelf.class, String.valueOf(i));
            }
            List<BookShelf> bookShelfList = new ArrayList<>();
            for (int i=0;i<100;i++) {
                BookShelf bookShelf = new BookShelf("user"+i, "book"+i,
                        "comment"+i, new Date());
                bookShelfList.add(bookShelf);
                // 自动插入相应分表
                db.insert(bookShelf);
            }
            for (int i=0;i<BookShelf.PARTITIONS;i++) {
                System.out.printf("partition %d count %d\n", i, db.count(BookShelf.class, String.valueOf(i)));
            }
            Random random = new Random();
            for (BookShelf bs: bookShelfList) {
                bs.setComment("comment_update"+random.nextInt(100));
                db.update(bs);
            }
            bookShelfList = new ArrayList<>();
            for (int i=0;i<BookShelf.PARTITIONS;i++) {
                bookShelfList.addAll(db.find(BookShelf.class, String.valueOf(i)));
            }
            for (BookShelf bs: bookShelfList) {
                System.out.println(bs.getComment());
            }
            for (BookShelf bs: bookShelfList) {
                db.delete(bs);
            }
        } finally {
            for (int i=0;i<BookShelf.PARTITIONS;i++) {
                db.drop(BookShelf.class, String.valueOf(i));
            }
        }
    }
}
