INSERT INTO posts(title,text,image_path,likes_count,comments_count)
VALUES ('titleNotTitleSome1111','DESCRIPTIIIION','1/image.jpg',2,3),
       ('titleAnother2222','DESCRIPTIIIION11111','2/image.jpg',1,1),
       ('titleDifferent3333','DESCRIPTIIIION22222','3/image.jpg',3,4);
INSERT INTO comments(text,post_id)
VALUES ('TEXXXT1',1),
       ('TEXXXT2',2),
       ('TEXXXT3',3),
       ('TEXXXT11',1),
       ('TEXXXT22',2),
       ('TEXXXT33',3);

INSERT INTO post_tags(post_id,tag) VALUES(1,'river'),(2,'river'),(3,'city'),(3,'house'),(2,'museum'),(2,'java');
