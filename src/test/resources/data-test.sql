DELETE
FROM post_tags;
DELETE
FROM comments;
DELETE
FROM posts;
ALTER TABLE posts
    ALTER COLUMN id RESTART WITH 1;
ALTER TABLE comments
    ALTER COLUMN id RESTART WITH 1;
INSERT INTO posts(title, text, likes_count, comments_count)
VALUES ('test title1', 'test desc1', 2, 3),
       ('test title2', 'test desc2', 1, 1),
       ('test title3', 'test desc3', 3, 4);

INSERT INTO comments(text, post_id)
VALUES ('test comment1', 1),
       ('test comment2', 2),
       ('test comment3', 3),
       ('test comment4', 1),
       ('test comment5', 2),
       ('test comment6', 3);

INSERT INTO post_tags(post_id, tag)
VALUES (1, 'test_tag1'),
       (2, 'test_tag1'),
       (2, 'test_tag2'),
       (3, ('test_tag3'))
