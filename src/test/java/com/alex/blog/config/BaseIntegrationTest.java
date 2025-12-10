package com.alex.blog.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestDataSourceConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @BeforeEach
    void setUp() {
        cleanDB();
        setDB();

    }

    void cleanDB() {
        jdbcTemplate.update("DELETE FROM post_tags");
        jdbcTemplate.update("DELETE FROM comments");
        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");
    }

    void setDB() {
        jdbcTemplate.update("""
                               
                 INSERT INTO posts(title,text,image_path,likes_count,comments_count)
                        VALUES ('test title1','test desc1','1/image.jpg',2,3),
                                ('test title2','test desc2','2/image.jpg',1,1),
                        ('test title3','test desc3','3/image.jpg',3,4)
                """);
        jdbcTemplate.update("""
                INSERT INTO comments(text,post_id)
                        VALUES ('test comment1',1),
                                ('test comment2',2),
                        ('test comment3',3),
                        ('test comment4',1),
                        ('test comment5',2),
                        ('test comment6',3)
                """);

        jdbcTemplate.update("""
                INSERT INTO post_tags(post_id,tag) VALUES(1,'test_tag1'),(2,'test_tag1'),(2,'test_tag2'),(3,('test_tag3'))
                """);
    }
}
