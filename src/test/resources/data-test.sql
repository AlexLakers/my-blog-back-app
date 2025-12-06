INSERT INTO posts(title,text,image_path,likes_count,comments_count)
VALUES ('test title1','test desc1','1/image.jpg',2,3),
       ('test title2','test desc2','2/image.jpg',1,1),
       ('test title3','test desc3','3/image.jpg',3,4);
INSERT INTO comments(text,post_id)
VALUES ('test comment1',1),
       ('test comment2',2),
       ('test comment3',3),
       ('test comment4',1),
       ('test comment5',2),
       ('test comment6',3);

INSERT INTO post_tags(post_id,tag) VALUES(1,'test_tag1'),(2,'test_tag1'),(2,'test_tag2'),(3,('test_tag3'))
