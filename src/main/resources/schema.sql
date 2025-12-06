CREATE TABLE IF NOT EXISTS posts
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
    title          VARCHAR(256)                        NOT NULL,
    text           TEXT                                NOT NULL,
    image_path     VARCHAR(512),
    likes_count    BIGINT    DEFAULT 0,
    comments_count BIGINT    DEFAULT 0,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_posts PRIMARY KEY (id),
    CONSTRAINT chk_posts_likes_count CHECK (likes_count >= 0),
    CONSTRAINT chk_posts_comments_count CHECK (comments_count >= 0),
    CONSTRAINT uk_tags_name UNIQUE (title)
);


CREATE TABLE IF NOT EXISTS post_tags
(
    post_id BIGINT NOT NULL,
    tag  VARCHAR(256) NOT NULL,

    CONSTRAINT fk_posts_tags_posts FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT pk_posts_tags PRIMARY KEY (post_id, tag)
);





CREATE TABLE IF NOT EXISTS comments
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
    text       TEXT                                NOT NULL,
    post_id    BIGINT                              NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_comments PRIMARY KEY (id)
);