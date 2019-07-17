# LegendORM
一个轻量级的ORM库 提供了常见的一些数据库操作
包括*where、group by、order by、limit、having*等语句的支持 并且支持分库分表
## 基本使用
### 实体类到数据库表的映射
使用注解的方式，并实现IEntity接口
``` java
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
    
    ...

    @Override
    public TableOptions options() {
        return new TableOptions().options("Engine", "InnoDB");
    }

    @Override
    public TableIndices indices() {
        return new TableIndices().primary("id").unique("name");
    }
}
```
使用注解进行进行相应的字段配置后，就可以直接根据Java类自动创建对应的数据库表，包括可以
指定主键，储存引擎，自增列，添加唯一索引等
### crud
``` java
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
```

### 事件监听
```java
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
```
### 分库分表
```java
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
    // 删除
    for (BookShelf bs: bookShelfList) {
        grid.delete(bs);
    }
    for (int k=0;k<grid.size();k++) {
        for (int i=0;i<BookShelf.PARTITIONS-1;i++) {
            // 依次查询出所有分库的分表的行数
            System.out.printf("db %d partition %d count %d\n", k, i,
                    grid.count(BookShelf.class, k, String.valueOf(i)));
        }
    }
```
更多请实例参考https://github.com/Xchuanshuo/MyOrm/tree/master/src/main/java/com/legend/orm/demo
## 说明
这个库是很久之前写的了，在https://github.com/pyloque/ormkids的基础上进行的改造，现在简单的整理一下，拿来当练手的东西挺合适的。