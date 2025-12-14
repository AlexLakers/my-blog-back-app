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

INSERT INTO tags(name) VALUES('river'),('mountain'),('city'),('house'),('museum'),('java');

INSERT INTO posts_tags(post_id,tag_id) VALUES(1,1),(1,2),(1,4),(2,2),(2,5),(2,6),(3,3),(3,4);



