package com.legend.orm.demo;

/**
 * @author Legend
 * @data by on 18-12-14.
 * @description
 */
public class DemoEvent {

    private static final String URI = "jdbc:mysql://localhost:3306/orm" +
            "?user=legend&password=2414605975&useUnicode=true&characterEncoding=UTF-8";

    public static void main(String[] args) {
        DemoLegendDB db = new DemoLegendDB("db", URI);
        // 全局事件回调
        db.getListenerHandler().on(ctx -> {
            System.out.printf("【global】db=%s sql=%s cost=%dus\n", ctx.db().name(),
                    ctx.q().sql(), ctx.duration());
            return true;
        });
        try {
            db.create(User.class);
            // 访问回调 execute方法内部的所有ORM操作都会回调
            db.getListenerHandler().scope(ctx -> {
                System.out.printf("【local】db=%s sql=%s cost=%dus\n", ctx.db().name(),
                        ctx.q().sql(), ctx.duration());
                return true;
            }).execute(() -> {
                db.count(User.class);
                db.find(User.class);
            });
        } finally {
            db.drop(User.class);
        }
    }
}
